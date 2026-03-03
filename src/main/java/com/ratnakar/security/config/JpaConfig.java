package com.ratnakar.security.config;

/*
 =============================================================================
 IMPORTS EXPLAINED
 =============================================================================
*/

import org.springframework.beans.factory.annotation.Autowired;
// Used to inject beans automatically from Spring container.

import org.springframework.beans.factory.annotation.Qualifier;
// Used when multiple beans of same type exist.
// We use it to specifically inject routingDataSource.

import org.springframework.context.annotation.Bean;
// Marks a method as Spring bean producer.

import org.springframework.context.annotation.Configuration;
// Marks this class as configuration class.

import org.springframework.orm.jpa.JpaTransactionManager;
// Manages database transactions for JPA (commit / rollback).

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
// Factory bean that creates EntityManagerFactory.
// This connects Hibernate to our DataSource.

import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
// Adapter that tells Spring we are using Hibernate as JPA provider.

import jakarta.persistence.EntityManagerFactory;
// Core JPA interface used for managing persistence context.

import javax.sql.DataSource;
// Standard Java interface for database connections.

import java.util.HashMap;
import java.util.Map;

/**
 =============================================================================
 JpaConfig
 =============================================================================

 WHY THIS CLASS EXISTS?
 -----------------------

 Normally Spring Boot auto-configures JPA automatically.

 But in your project:
 - We are using RoutingDataSource
 - We are using Failover
 - We are disabling DDL
 - We are manually controlling dialect

 So we override default auto configuration.

 This gives us full control over Hibernate behavior.

 @Configuration:
 ----------------
 Tells Spring:
 "This class contains custom configuration beans."
 */
@Configuration
public class JpaConfig {

    /*
     =========================================================================
     Inject RoutingDataSource
     =========================================================================

     We explicitly inject routingDataSource.

     Why?
     ----
     Because we want Hibernate to use our custom Failover DataSource.

     If we don't do this:
     Spring Boot may auto-detect wrong DataSource.
    */
    @Autowired
    @Qualifier("routingDataSource")
    private DataSource routingDataSource;

    /*
     =========================================================================
     ENTITY MANAGER FACTORY
     =========================================================================

     WHAT IS EntityManagerFactory?
     -------------------------------
     It is the main JPA component.

     Think of it like:
     "Factory that creates EntityManager objects."

     EntityManager:
     --------------
     Used to:
     - Save entities
     - Fetch entities
     - Update
     - Delete

     Hibernate works internally using EntityManager.
    */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        LocalContainerEntityManagerFactoryBean em =
                new LocalContainerEntityManagerFactoryBean();

        /*
         Connect Hibernate to routingDataSource.
        */
        em.setDataSource(routingDataSource);

        /*
         Tell Hibernate where our Entity classes are located.

         This package contains:
         - Users entity
         - Any other @Entity classes
        */
        em.setPackagesToScan("com.ratnakar.security.model");

        /*
         Tell Spring that we are using Hibernate.
        */
        HibernateJpaVendorAdapter vendorAdapter =
                new HibernateJpaVendorAdapter();

        em.setJpaVendorAdapter(vendorAdapter);

        /*
         =========================================================================
         HIBERNATE PROPERTIES
         =========================================================================
        */
        Map<String, Object> properties = new HashMap<>();

        /*
         -------------------------------------------------------------------------
         1️⃣ Disable DDL Auto
         -------------------------------------------------------------------------

         hibernate.hbm2ddl.auto = none

         Why?
         ----
         If enabled:
         Hibernate tries to create/modify tables automatically.

         Problem in Failover scenario:
         - If MySQL is down
         - Hibernate connects to PostgreSQL
         - It may generate MySQL-specific SQL like:
           engine=InnoDB
         - PostgreSQL will reject it.

         So we disable automatic DDL completely.

         Both DB schemas are managed using SQL scripts instead.
        */
        properties.put("hibernate.hbm2ddl.auto", "none");

        /*
         -------------------------------------------------------------------------
         2️⃣ Show SQL in Console
         -------------------------------------------------------------------------

         Useful for debugging.
         Shows SQL queries generated by Hibernate.
        */
        properties.put("hibernate.show_sql", "true");

        /*
         -------------------------------------------------------------------------
         3️⃣ CRITICAL FIX — Disable JDBC Metadata Probing
         -------------------------------------------------------------------------

         hibernate.temp.use_jdbc_metadata_defaults = false

         WHY?
         ----
         At startup, Hibernate tries to detect:
         - Database dialect
         - LOB support

         To check LOB support, it calls:
         connection.createClob()

         PostgreSQL driver DOES NOT support createClob().
         It throws:
         SQLFeatureNotSupportedException

         HikariCP then marks that connection as broken.

         This wastes pool connections.

         Setting this to false:
         - Skips metadata probing
         - Prevents createClob() call
         - Prevents startup errors
        */
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", false);

        /*
         -------------------------------------------------------------------------
         4️⃣ Explicit Dialect Configuration
         -------------------------------------------------------------------------

         Since we disabled metadata probing,
         Hibernate cannot auto-detect dialect.

         So we manually set:
         MySQLDialect

         Why MySQLDialect?
         -------------------
         MySQL is PRIMARY DB.

         Also:
         The SQL used in your app (basic CRUD)
         is compatible with PostgreSQL.

         No MySQL-specific syntax is used.
        */
        properties.put("hibernate.dialect",
                "org.hibernate.dialect.MySQLDialect");

        /*
         IMPORTANT:
         Do NOT set hibernate.default_schema.

         Hibernate lowercases schema names.
         That breaks PostgreSQL mixed-case schema.
        */

        em.setJpaPropertyMap(properties);

        return em;
    }

    /*
     =========================================================================
     TRANSACTION MANAGER
     =========================================================================

     WHAT IS TRANSACTION?
     ---------------------
     A transaction ensures:

     Either:
     - All DB operations succeed

     Or:
     - All operations rollback

     Example:
     If saving user fails halfway,
     DB should not be partially updated.

     JpaTransactionManager handles:
     - Commit
     - Rollback
    */
    @Bean
    public JpaTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager transactionManager =
                new JpaTransactionManager();

        transactionManager.setEntityManagerFactory(entityManagerFactory);

        return transactionManager;
    }
}