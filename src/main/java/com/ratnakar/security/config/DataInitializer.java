package com.ratnakar.security.config;

import com.ratnakar.security.model.Users;
import com.ratnakar.security.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer
 *
 * Runs once on application startup (CommandLineRunner).
 * Checks if the seed users already exist in the DB — if not, inserts them
 * with BCrypt-hashed passwords (strength 12).
 *
 * WHY THIS IS NEEDED:
 * The MySQL seed SQL uses plain-text passwords (admin123, user123, manager123).
 * The app uses BCryptPasswordEncoder(12) for authentication.
 * Plain-text passwords cause: "Encoded password does not look like BCrypt"
 * and authentication FAILS. This initializer ensures correct hashed passwords
 * are always present without requiring manual SQL updates.
 *
 * SAFE TO RUN MULTIPLE TIMES:
 * Uses existsById() check before inserting — will not overwrite existing records.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed admin_user if not already present
        if (!authenticationRepository.existsById(1)) {
            Users admin = new Users();
            admin.setId(1);
            admin.setUsername("admin_user");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            authenticationRepository.save(admin);
            System.out.println("[DataInitializer] Seeded user: admin_user");
        }

        // Seed normal_user if not already present
        if (!authenticationRepository.existsById(2)) {
            Users user = new Users();
            user.setId(2);
            user.setUsername("normal_user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            authenticationRepository.save(user);
            System.out.println("[DataInitializer] Seeded user: normal_user");
        }

        // Seed manager_user if not already present
        if (!authenticationRepository.existsById(3)) {
            Users manager = new Users();
            manager.setId(3);
            manager.setUsername("manager_user");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole("MANAGER");
            authenticationRepository.save(manager);
            System.out.println("[DataInitializer] Seeded user: manager_user");
        }

        System.out.println("[DataInitializer] Seed check complete.");
    }
}
