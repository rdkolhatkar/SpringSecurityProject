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
 * WHY manual HikariConfig instead of @ConfigurationProperties + DataSourceBuilder?
 *   HikariCP requires the property name 'jdbcUrl' (not 'url') when driverClassName
 *   is also set. Spring Boot's DataSourceBuilder uses 'url' internally and translates
 *   it for simple cases, but when combined with a custom RoutingDataSource the
 *   translation is skipped and HikariCP throws "jdbcUrl is required with driverClassName".
 *   Reading values explicitly with @Value and constructing HikariConfig manually
 *   avoids this entirely.
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
    // Manually constructs HikariConfig to ensure 'jdbcUrl' is set
    // correctly (HikariCP requires 'jdbcUrl', not 'url').
    // ---------------------------------------------------------------
    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlUrl);                        // HikariCP needs jdbcUrl
        config.setUsername(mysqlUsername);
        config.setPassword(mysqlPassword);
        config.setDriverClassName(mysqlDriverClassName);
        config.setPoolName("MySQLHikariPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(3000);                  // 3s — fail fast for failover
        config.setValidationTimeout(2000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }

    // ---------------------------------------------------------------
    // PostgreSQL HikariCP DataSource bean
    // ---------------------------------------------------------------
    @Bean(name = "postgresqlDataSource")
    public DataSource postgresqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresqlUrl);                   // HikariCP needs jdbcUrl
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
        // Default route is always MySQL
        routingDataSource.setDefaultTargetDataSource(mysqlDataSource);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    // ---------------------------------------------------------------
    // FailoverRoutingDataSource
    //
    // Extends AbstractRoutingDataSource.
    // getConnection() tries MySQL first; on SQLException it falls over
    // to PostgreSQL automatically.
    // ---------------------------------------------------------------
    static class FailoverRoutingDataSource extends AbstractRoutingDataSource {

        private final DataSource mysqlDataSource;
        private final DataSource postgresqlDataSource;

        FailoverRoutingDataSource(DataSource mysqlDataSource,
                                  DataSource postgresqlDataSource) {
            this.mysqlDataSource      = mysqlDataSource;
            this.postgresqlDataSource = postgresqlDataSource;
        }

        // Returns the routing key for the current thread.
        // If nothing is set explicitly, MYSQL is the default route.
        @Override
        protected Object determineCurrentLookupKey() {
            DataSourceType type = DataSourceContextHolder.getDataSourceType();
            return (type != null) ? type : DataSourceType.MYSQL;
        }

        // Override getConnection() to implement failover:
        // 1. Try MySQL (primary)
        // 2. If SQLException → switch to PostgreSQL, log warning, retry
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
                // Always clean ThreadLocal to prevent memory leaks across pooled threads
                DataSourceContextHolder.clearDataSourceType();
            }
        }

        // Overload: getConnection(username, password) — same failover logic
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