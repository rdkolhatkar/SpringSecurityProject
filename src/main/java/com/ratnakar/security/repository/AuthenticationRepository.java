package com.ratnakar.security.repository;

import com.ratnakar.security.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationRepository extends JpaRepository<Users, Integer> {
    Users findByUsername(String username);
}
