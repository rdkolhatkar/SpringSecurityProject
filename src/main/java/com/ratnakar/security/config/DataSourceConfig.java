package com.ratnakar.security.config;

/*
 =============================================================================
 IMPORT SECTION EXPLAINED
 =============================================================================
*/

import com.zaxxer.hikari.HikariConfig;
// HikariConfig is configuration class for HikariCP connection pool.

import com.zaxxer.hikari.HikariDataSource;
// HikariDataSource is actual DataSource implementation provided by HikariCP.
// It manages database connections efficiently.

import org.springframework.beans.factory.annotation.Qualifier;
// Used when multiple beans of same type exist.
// We use it to specify which DataSource bean to inject.

import org.springframework.beans.factory.annotation.Value;
// @Value reads values from application.properties file.

import org.springframework.context.annotation.Bean;
// @Bean tells Spring to create and manage this object in IoC container.

import org.springframework.context.annotation.Configuration;
// @Configuration marks this class as Spring configuration class.

import org.springframework.context.annotation.Primary;
// @Primary tells Spring: if multiple beans of same type exist,
// use this one by default.

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
// AbstractRoutingDataSource is a special Spring class.
// It allows dynamic switching between multiple DataSources at runtime.

import javax.sql.DataSource;
// Standard Java interface representing a database connection source.

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 =============================================================================
 DataSourceConfig
 =============================================================================

 This is one of the most important configuration classes in your project.

 WHAT THIS CLASS DOES:
 ----------------------
 1. Creates MySQL DataSource (Primary DB)
 2. Creates PostgreSQL DataSource (Failover DB)
 3. Creates Routing DataSource that switches between them
 4. Implements automatic FAILOVER logic

 WHY WE NEED THIS?
 ------------------
 Normally Spring Boot supports only ONE DataSource.
 But in real enterprise applications:
 - We may want high availability
 - We may want backup DB
 - We may want read/write separation

 This class makes your application PRODUCTION LEVEL.

 @Configuration:
 ---------------
 Tells Spring:
 "This class contains @Bean methods.
 Create and manage those beans."
 */
@Configuration
public class DataSourceConfig {

    /*
     =========================================================================
     ENUM USED AS ROUTING KEY
     =========================================================================

     This enum acts as a KEY to identify which DataSource to use.

     Think of it like:
     MYSQL → Primary Database
     POSTGRESQL → Backup Database
    */
    public enum DataSourceType {
        MYSQL, POSTGRESQL
    }

    /*
     =========================================================================
     THREAD LOCAL CONTEXT HOLDER
     =========================================================================

     What is ThreadLocal?
     ---------------------
     ThreadLocal stores data per thread.

     Why needed?
     -----------
     Each HTTP request runs in separate thread.
     We must remember which DB that thread should use.

     This prevents cross-request data leakage.
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
            CONTEXT.remove(); // Important to prevent memory leaks
        }
    }

    /*
     =========================================================================
     MYSQL PROPERTIES
     =========================================================================

     These values come from application.properties:

     spring.datasource.url=
     spring.datasource.username=
     spring.datasource.password=
     spring.datasource.driver-class-name=
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
     POSTGRESQL PROPERTIES
     =========================================================================

     These come from:
     app.datasource.postgresql.*
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

     @Bean(name = "mysqlDataSource")
     This creates a bean with specific name.

     HikariCP is used as connection pool.

     WHAT IS CONNECTION POOL?
     --------------------------
     Instead of opening new DB connection for every request,
     we keep some ready connections in memory.

     This improves performance drastically.
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
         VERY IMPORTANT:
         ----------------
         setInitializationFailTimeout(-1)

         If MySQL is DOWN at startup:
         - HikariCP will NOT fail application startup.

         Without this:
         Application would crash.

         This makes failover possible.
        */
        config.setInitializationFailTimeout(-1);

        return new HikariDataSource(config);
    }

    /*
     =========================================================================
     POSTGRESQL DATASOURCE BEAN
     =========================================================================

     Used as FAILOVER database.
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
         CRITICAL FIX FOR POSTGRES:

         PostgreSQL schema names are case sensitive.

         Without this:
         "SecureVault" may become "securevault"

         This ensures correct schema is used ALWAYS.
        */
        config.setConnectionInitSql("SET search_path TO \"SecureVault\"");

        return new HikariDataSource(config);
    }

    /*
     =========================================================================
     ROUTING DATASOURCE
     =========================================================================

     This is the most important bean.

     @Primary:
     ---------
     If multiple DataSource beans exist,
     Spring will use this one by default.

     JPA will use this bean automatically.
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
     CUSTOM ROUTING CLASS
     =========================================================================

     Extends AbstractRoutingDataSource.

     AbstractRoutingDataSource internally calls:
     determineCurrentLookupKey()

     Based on returned key, it chooses DataSource.
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
         This method tells Spring which DataSource key to use.
        */
        @Override
        protected Object determineCurrentLookupKey() {

            DataSourceType type = DataSourceContextHolder.getDataSourceType();

            return (type != null) ? type : DataSourceType.MYSQL;
        }

        /*
         OVERRIDDEN getConnection()

         This is where FAILOVER happens.
        */
        @Override
        public Connection getConnection() throws SQLException {

            try {
                DataSourceContextHolder.setDataSourceType(DataSourceType.MYSQL);

                Connection connection = mysqlDataSource.getConnection();

                System.out.println("[Router] Connected to MySQL");

                return connection;

            } catch (SQLException mysqlException) {

                System.err.println("[Router] MySQL DOWN → Switching to PostgreSQL");

                try {
                    DataSourceContextHolder.setDataSourceType(DataSourceType.POSTGRESQL);

                    Connection connection = postgresqlDataSource.getConnection();

                    System.out.println("[Router] Connected to PostgreSQL (Failover)");

                    return connection;

                } catch (SQLException postgresqlException) {

                    throw new SQLException(
                            "Both MySQL and PostgreSQL are down.",
                            postgresqlException);
                }

            } finally {
                DataSourceContextHolder.clearDataSourceType();
            }
        }
    }
}