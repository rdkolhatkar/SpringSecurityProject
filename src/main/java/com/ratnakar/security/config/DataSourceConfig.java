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

@Configuration
public class DataSourceConfig {

    /*
     =========================================================================
     ENUM — ROUTING KEY
     =========================================================================
     Identifies which DataSource to use for the current thread.
    */
    public enum DataSourceType {
        MYSQL, POSTGRESQL
    }

    /*
     =========================================================================
     THREAD LOCAL CONTEXT HOLDER
     =========================================================================
     Stores which DB the current thread should use.
     Each HTTP request runs in its own thread — this prevents cross-request leakage.
    */
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

    /*
     =========================================================================
     MYSQL PROPERTIES — from application.yaml spring.datasource.*
     =========================================================================
    */
    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    /*
     =========================================================================
     POSTGRESQL PROPERTIES — from application.yaml app.datasource.postgresql.*
     =========================================================================
    */
    @Value("${app.datasource.postgresql.url}")
    private String postgresqlUrl;

    @Value("${app.datasource.postgresql.username}")
    private String postgresqlUsername;

    @Value("${app.datasource.postgresql.password}")
    private String postgresqlPassword;

    @Value("${app.datasource.postgresql.driver-class-name}")
    private String postgresqlDriverClassName;

    /*
     =========================================================================
     MYSQL DATASOURCE BEAN
     =========================================================================
     Primary database.
     setInitializationFailTimeout(-1) → app won't crash if MySQL is down at startup.
    */
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

        /*
         CRITICAL:
         -1 means: if MySQL is DOWN at startup, don't fail — just start with empty pool.
         This makes failover possible from the very first request.
        */
        config.setInitializationFailTimeout(-1);

        return new HikariDataSource(config);
    }

    /*
     =========================================================================
     POSTGRESQL DATASOURCE BEAN
     =========================================================================
     Failover database.

     connectionInitSql sets the schema on every new connection.
     This is the correct way to handle mixed-case PostgreSQL schema names
     because currentSchema= in the JDBC URL lowercases the name internally.
    */
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

        /*
         CRITICAL FIX FOR POSTGRESQL SCHEMA:
         Sends SET search_path at connection time.
         Preserves exact mixed-case schema name "SecureVault".
         Do NOT use currentSchema= in JDBC URL — it lowercases the name.
        */
        config.setConnectionInitSql("SET search_path TO \"SecureVault\"");

        return new HikariDataSource(config);
    }

    /*
     =========================================================================
     ROUTING DATASOURCE BEAN
     =========================================================================
     @Primary → JPA and Spring will use this DataSource by default.
     Delegates to FailoverRoutingDataSource which handles MySQL → PostgreSQL switching.
    */
    @Primary
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("mysqlDataSource") DataSource mysqlDataSource,
            @Qualifier("postgresqlDataSource") DataSource postgresqlDataSource) {

        FailoverRoutingDataSource routingDataSource =
                new FailoverRoutingDataSource(mysqlDataSource, postgresqlDataSource);

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MYSQL, mysqlDataSource);
        targetDataSources.put(DataSourceType.POSTGRESQL, postgresqlDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(mysqlDataSource);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    /*
     =========================================================================
     FAILOVER ROUTING DATASOURCE
     =========================================================================
     Extends AbstractRoutingDataSource.

     TWO responsibilities:
     1. determineCurrentLookupKey() → tells Spring which DS key to use
        (used by AbstractRoutingDataSource internally for JPA operations)

     2. getConnection() / getConnection(user, pass) → actual failover logic
        (tries MySQL first, falls back to PostgreSQL on failure)

     IMPORTANT DESIGN DECISION:
     ---------------------------
     We do NOT clear ThreadLocal in finally inside getConnection().

     Why?
     Because JPA calls determineCurrentLookupKey() AFTER getConnection().
     If we clear in finally, the key becomes null and routes back to MySQL
     even when we just failed over to PostgreSQL.

     ThreadLocal is cleared by:
     - DataInitializer (after seeding)
     - Request completion (via filter — recommended for production)
    */
    static class FailoverRoutingDataSource extends AbstractRoutingDataSource {

        private final DataSource mysqlDataSource;
        private final DataSource postgresqlDataSource;

        FailoverRoutingDataSource(DataSource mysqlDataSource,
                                  DataSource postgresqlDataSource) {
            this.mysqlDataSource = mysqlDataSource;
            this.postgresqlDataSource = postgresqlDataSource;
        }

        /*
         Called by AbstractRoutingDataSource to pick the target DataSource.
         Returns the key stored in ThreadLocal.
         Defaults to MYSQL if nothing is set.
        */
        @Override
        protected Object determineCurrentLookupKey() {
            DataSourceType type = DataSourceContextHolder.getDataSourceType();
            return (type != null) ? type : DataSourceType.MYSQL;
        }

        /*
         -------------------------------------------------------------------------
         FIX 1: getConnection() — no-arg version
         -------------------------------------------------------------------------
         BEFORE (broken):
         - Set MYSQL in context
         - Try MySQL
         - On failure: set POSTGRESQL
         - finally: ALWAYS cleared context ← this was the bug
         - determineCurrentLookupKey() then returned null → defaulted to MYSQL

         AFTER (fixed):
         - Try MySQL directly on the raw DataSource (bypasses routing)
         - On success: set MYSQL in context (for determineCurrentLookupKey)
         - On failure: set POSTGRESQL in context (persists for JPA routing)
         - Only clear on total failure (both DBs down)
        */
        @Override
        public Connection getConnection() throws SQLException {

            try {
                Connection connection = mysqlDataSource.getConnection();
                DataSourceContextHolder.setDataSourceType(DataSourceType.MYSQL);
                System.out.println("[Router] Connected to MySQL");
                return connection;

            } catch (SQLException mysqlException) {
                System.err.println("[Router] MySQL DOWN → Switching to PostgreSQL. Reason: "
                        + mysqlException.getMessage());

                try {
                    Connection connection = postgresqlDataSource.getConnection();
                    // CRITICAL: Set POSTGRESQL in context and DO NOT clear it.
                    // determineCurrentLookupKey() needs this to stay set
                    // so JPA routes subsequent operations to PostgreSQL correctly.
                    DataSourceContextHolder.setDataSourceType(DataSourceType.POSTGRESQL);
                    System.out.println("[Router] Connected to PostgreSQL (Failover)");
                    return connection;

                } catch (SQLException postgresqlException) {
                    // Both DBs are down — safe to clear context now
                    DataSourceContextHolder.clearDataSourceType();
                    throw new SQLException(
                            "Both MySQL and PostgreSQL are unavailable.",
                            postgresqlException
                    );
                }
            }
        }

        /*
         -------------------------------------------------------------------------
         FIX 2: getConnection(username, password) — overloaded version
         -------------------------------------------------------------------------
         Hibernate/JPA sometimes calls this overloaded version instead of
         the no-arg version. Without this override, it bypasses failover
         entirely and hits the default AbstractRoutingDataSource implementation
         which does NOT have our failover logic.
        */
        @Override
        public Connection getConnection(String username, String password) throws SQLException {

            try {
                Connection connection = mysqlDataSource.getConnection(username, password);
                DataSourceContextHolder.setDataSourceType(DataSourceType.MYSQL);
                System.out.println("[Router] Connected to MySQL (with credentials)");
                return connection;

            } catch (SQLException mysqlException) {
                System.err.println("[Router] MySQL DOWN → Switching to PostgreSQL (with credentials). Reason: "
                        + mysqlException.getMessage());

                try {
                    Connection connection = postgresqlDataSource.getConnection(username, password);
                    DataSourceContextHolder.setDataSourceType(DataSourceType.POSTGRESQL);
                    System.out.println("[Router] Connected to PostgreSQL (Failover, with credentials)");
                    return connection;

                } catch (SQLException postgresqlException) {
                    DataSourceContextHolder.clearDataSourceType();
                    throw new SQLException(
                            "Both MySQL and PostgreSQL are unavailable.",
                            postgresqlException
                    );
                }
            }
        }
    }
}