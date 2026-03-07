package com.ratnakar.code.service;


import com.ratnakar.code.dto.RegisterRequest;
import com.ratnakar.code.entity.User;
import com.ratnakar.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // BCryptPasswordEncoder bean

    /**
     * Registers a new user.
     * - Checks username uniqueness
     * - Encodes the plain-text password with BCrypt before persisting
     */
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username '" + request.getUsername() + "' is already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
                .role("ROLE_USER")
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getUsername());
        return saved;
    }
}
