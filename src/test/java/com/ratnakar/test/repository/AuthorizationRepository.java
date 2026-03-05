package com.ratnakar.test.repository;

import com.ratnakar.test.entity.UsersData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationRepository extends JpaRepository<UsersData, Integer> {
    UsersData findByUsername(String username);
}
