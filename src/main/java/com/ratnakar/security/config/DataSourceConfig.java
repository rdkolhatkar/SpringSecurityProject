package com.ratnakar.security.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * DataSourceConfig
 *
 * Configures two DataSources:
 *  1. PRIMARY   — MySQL      (reads from spring.datasource.*)
 *  2. FAILOVER  — PostgreSQL (reads from app.datasource.postgresql.*)
 *
 * All incoming requests are routed to MySQL by default.
 * If MySQL is unavailable (connection fails), the router transparently
 * falls back to PostgreSQL — no application code change is needed.
 *
 * WHY initializationFailTimeout = -1 on MySQL?
 *   Disables HikariCP's eager pool validation at startup.
 *   If MySQL is down when the app starts, HikariCP would otherwise throw
 *   PoolInitializationException and crash the entire app context.
 *   With -1, the bean is created successfully regardless of MySQL state.
 *
 * WHY SET search_path on every PostgreSQL connection?
 *   PostgreSQL schema names are case-sensitive when created with double quotes.
 *   Our schema is "SecureVault" (mixed case). The JDBC URL option
 *   currentSchema=SecureVault gets lowercased internally by the driver.
 *   Explicitly running SET search_path TO "SecureVault" on each connection
 *   guarantees the exact casing is preserved and unqualified table names
 *   like "users" resolve to "SecureVault".users correctly.
 */
@Configuration
public class DataSourceConfig {

    // ---------------------------------------------------------------
    // Enum used as routing key
    // ---------------------------------------------------------------
    public enum DataSourceType {
        MYSQL, POSTGRESQL
    }

    // ---------------------------------------------------------------
    // ThreadLocal that holds the active DataSource key per request
    // ---------------------------------------------------------------
    public static class DataSourceContextHolder {
        private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

        public static void setDataSourceType(DataSourceType type) {
            CONTEXT.set(type);
        }

        public static DataSourceType getDataSourceType() {
            return CONTEXT.get();
        }

        public static void clearDataSourceType() {
            CONTEXT.remove();
        }
    }

    // ---------------------------------------------------------------
    // MySQL properties — read from spring.datasource.*
    // ---------------------------------------------------------------
    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    // ---------------------------------------------------------------
    // PostgreSQL properties — read from app.datasource.postgresql.*
    // ---------------------------------------------------------------
    @Value("${app.datasource.postgresql.url}")
    private String postgresqlUrl;

    @Value("${app.datasource.postgresql.username}")
    private String postgresqlUsername;

    @Value("${app.datasource.postgresql.password}")
    private String postgresqlPassword;

    @Value("${app.datasource.postgresql.driver-class-name}")
    private String postgresqlDriverClassName;

    // ---------------------------------------------------------------
    // MySQL HikariCP DataSource bean
    // ---------------------------------------------------------------
    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlUrl);
        config.setUsername(mysqlUsername);
        config.setPassword(mysqlPassword);
        config.setDriverClassName(mysqlDriverClassName);
        config.setPoolName("MySQLHikariPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(3000);
        config.setValidationTimeout(2000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        // -1 = do not fail at startup if MySQL is unreachable
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }

    // ---------------------------------------------------------------
    // PostgreSQL HikariCP DataSource bean
    //
    // WHY connectionInitSql:
    //   Sets search_path on EVERY new connection from the pool.
    //   This is the most reliable way to ensure "SecureVault" schema
    //   (exact case) is always active, regardless of JDBC URL parsing.
    // ---------------------------------------------------------------
    @Bean(name = "postgresqlDataSource")
    public DataSource postgresqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresqlUrl);
        config.setUsername(postgresqlUsername);
        config.setPassword(postgresqlPassword);
        config.setDriverClassName(postgresqlDriverClassName);
        config.setPoolName("PostgreSQLHikariPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);
        config.setValidationTimeout(2000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        config.setInitializationFailTimeout(0);
        // KEY FIX: Runs this SQL on every new connection from the pool.
        // Guarantees "SecureVault" schema (exact mixed-case) is always the
        // active search path — unqualified "users" resolves to "SecureVault".users
        config.setConnectionInitSql("SET search_path TO \"SecureVault\"");
        return new HikariDataSource(config);
    }

    // ---------------------------------------------------------------
    // Failover Routing DataSource — @Primary so JPA uses this bean
    // ---------------------------------------------------------------
    @Primary
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("mysqlDataSource")      DataSource mysqlDataSource,
            @Qualifier("postgresqlDataSource") DataSource postgresqlDataSource) {

        FailoverRoutingDataSource routingDataSource =
                new FailoverRoutingDataSource(mysqlDataSource, postgresqlDataSource);

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MYSQL,      mysqlDataSource);
        targetDataSources.put(DataSourceType.POSTGRESQL,  postgresqlDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(mysqlDataSource);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    // ---------------------------------------------------------------
    // FailoverRoutingDataSource
    // ---------------------------------------------------------------
    static class FailoverRoutingDataSource extends AbstractRoutingDataSource {

        private final DataSource mysqlDataSource;
        private final DataSource postgresqlDataSource;

        FailoverRoutingDataSource(DataSource mysqlDataSource,
                                  DataSource postgresqlDataSource) {
            this.mysqlDataSource      = mysqlDataSource;
            this.postgresqlDataSource = postgresqlDataSource;
        }

        @Override
        protected Object determineCurrentLookupKey() {
            DataSourceType type = DataSourceContextHolder.getDataSourceType();
            return (type != null) ? type : DataSourceType.MYSQL;
        }

        @Override
        public Connection getConnection() throws SQLException {
            try {
                DataSourceContextHolder.setDataSourceType(DataSourceType.MYSQL);
                Connection connection = mysqlDataSource.getConnection();
                System.out.println("[DataSource Router] Connected to PRIMARY — MySQL");
                return connection;
            } catch (SQLException mysqlException) {
                System.err.println("[DataSource Router] MySQL UNAVAILABLE — " +
                        "switching to FAILOVER PostgreSQL. Reason: " + mysqlException.getMessage());
                try {
                    DataSourceContextHolder.setDataSourceType(DataSourceType.POSTGRESQL);
                    Connection connection = postgresqlDataSource.getConnection();
                    System.out.println("[DataSource Router] Connected to FAILOVER — PostgreSQL");
                    return connection;
                } catch (SQLException postgresqlException) {
                    System.err.println("[DataSource Router] PostgreSQL FAILOVER also FAILED: "
                            + postgresqlException.getMessage());
                    throw new SQLException(
                            "Both PRIMARY (MySQL) and FAILOVER (PostgreSQL) datasources are unavailable. " +
                                    "MySQL error: "      + mysqlException.getMessage()      + " | " +
                                    "PostgreSQL error: " + postgresqlException.getMessage(),
                            postgresqlException);
                }
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            try {
                DataSourceContextHolder.setDataSourceType(DataSourceType.MYSQL);
                Connection connection = mysqlDataSource.getConnection(username, password);
                System.out.println("[DataSource Router] Connected to PRIMARY — MySQL");
                return connection;
            } catch (SQLException mysqlException) {
                System.err.println("[DataSource Router] MySQL UNAVAILABLE — " +
                        "switching to FAILOVER PostgreSQL. Reason: " + mysqlException.getMessage());
                try {
                    DataSourceContextHolder.setDataSourceType(DataSourceType.POSTGRESQL);
                    Connection connection = postgresqlDataSource.getConnection(username, password);
                    System.out.println("[DataSource Router] Connected to FAILOVER — PostgreSQL");
                    return connection;
                } catch (SQLException postgresqlException) {
                    throw new SQLException(
                            "Both PRIMARY (MySQL) and FAILOVER (PostgreSQL) datasources are unavailable.",
                            postgresqlException);
                }
            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }
    }
}