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