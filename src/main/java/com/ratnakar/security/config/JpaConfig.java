package com.ratnakar.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * JpaConfig
 *
 * Provides a custom EntityManagerFactory explicitly wired to the
 * routingDataSource (@Primary bean).
 *
 * WHY THIS IS NEEDED:
 * Spring Boot's JPA auto-config inspects the @Primary DataSource at startup.
 * With RoutingDataSource as @Primary, Hibernate picks up MySQL's database name
 * ("secure_vault") and uses it as a schema prefix — producing "secure_vault.users"
 * even when PostgreSQL is the active connection.
 *
 * WHY ddl-auto = none:
 * With a RoutingDataSource, Hibernate runs DDL against whichever DB is active
 * at startup. If MySQL is down, DDL runs against PostgreSQL using MySQL dialect —
 * producing invalid SQL like "engine=InnoDB" that PostgreSQL rejects.
 * Setting none disables all Hibernate-managed DDL. Both schemas are managed via
 * SQL scripts instead.
 *
 * WHY hibernate.temp.use_jdbc_metadata_defaults = false:
 * When MySQL is down and PostgreSQL is the active connection at startup,
 * Hibernate calls createClob() on the PostgreSQL connection to probe LOB support.
 * PostgreSQL's JDBC driver does not implement createClob() and throws:
 *   SQLFeatureNotSupportedException: Method PgConnection.createClob() is not yet implemented
 * HikariCP then marks that connection as broken, wasting a pool connection.
 * Setting this property to false tells Hibernate to skip all JDBC metadata probing
 * (LOB support, dialect detection via connection) and rely on explicit configuration instead.
 */
@Configuration
public class JpaConfig {

    @Autowired
    @Qualifier("routingDataSource")
    private DataSource routingDataSource;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDataSource);
        em.setPackagesToScan("com.ratnakar.security.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();

        // Disable DDL — both DB schemas managed via SQL scripts, not Hibernate.
        // Prevents MySQL-dialect DDL (engine=InnoDB) from running against PostgreSQL.
        properties.put("hibernate.hbm2ddl.auto", "none");

        properties.put("hibernate.show_sql", "true");

        // FIX: Prevents Hibernate from probing JDBC metadata on the connection at startup.
        // Without this, Hibernate calls createClob() on the PostgreSQL connection to check
        // LOB support — PostgreSQL does not implement this method and throws
        // SQLFeatureNotSupportedException, which HikariCP treats as a broken connection.
        // Setting false skips all JDBC metadata probing entirely.
        // Side effect: dialect must be configured explicitly (done below).
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", false);

        // Required when use_jdbc_metadata_defaults=false — Hibernate cannot
        // auto-detect dialect without JDBC metadata, so we set it explicitly.
        // MySQL is the primary DB so MySQLDialect is correct for query generation.
        // The simple SQL this app runs (SELECT/INSERT/UPDATE on users) is fully
        // compatible with PostgreSQL too — no MySQL-specific syntax is used.
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        // DO NOT set hibernate.default_schema — it gets lowercased by Hibernate
        // and prefixes all table names, breaking cross-DB compatibility.

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}