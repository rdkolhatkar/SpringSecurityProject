package com.ratnakar.security.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
// No schema attribute — Hibernate generates plain "users" with no prefix.
// Each database resolves the table via its JDBC URL context:
//   MySQL:      jdbc:mysql://localhost:3306/secure_vault          → secure_vault.users
//   PostgreSQL: jdbc:postgresql://...?currentSchema=SecureVault  → SecureVault.users
@Table(name = "users")
public class Users {
    @Id
    private int id;
    private String username;
    private String password;
    // OPTIMIZATION: Added 'role' field to match the MySQL and PostgreSQL schema.
    // Previously missing from the entity — caused mismatch between DB table and JPA model.
    // UserPrincipal was hardcoding role as "USER"; now it reads the actual role from DB.
    private String role;
}