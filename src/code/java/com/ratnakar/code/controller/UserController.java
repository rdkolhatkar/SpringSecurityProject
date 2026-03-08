package com.ratnakar.code.controller;


import com.ratnakar.code.dto.ApiResponse;
import com.ratnakar.code.dto.RegisterRequest;
import com.ratnakar.code.entity.User;
import com.ratnakar.code.service.JwtAuthService;
import com.ratnakar.code.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtAuthService jwtAuthService;

    /**
     * POST /api/auth/register
     *
     * Publicly accessible — no credentials needed.
     * Stores the password as a BCrypt hash in the DB.
     *
     * Request body:
     * {
     *   "username": "john",
     *   "password": "secret123"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @Valid @RequestBody RegisterRequest request) {

        User saved = userService.registerUser(request);

        Map<String, Object> data = Map.of(
                "id",       saved.getId(),
                "username", saved.getUsername(),
                "role",     saved.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", data));
    }

    @PostMapping("/login")
    public String login(@RequestBody User user){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );
        if(authentication.isAuthenticated()){
            return jwtAuthService.generateJwtAuthToken(user.getUsername());
        } else {
            return "Login Failed";
        }
    }
}
