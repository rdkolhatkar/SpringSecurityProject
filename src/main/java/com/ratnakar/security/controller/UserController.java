package com.ratnakar.security.controller;

import com.ratnakar.security.model.Users;
import com.ratnakar.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    // OPTIMIZATION: Changed return type from Users to ResponseEntity<Users>
    // so the API returns a proper HTTP 201 Created status on successful registration
    // instead of always returning 200 OK.
    // Old signature (kept for reference):
    // public Users register(@RequestBody Users user)
    @PostMapping("/user/register")
    public ResponseEntity<Users> register(@RequestBody Users user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println(user.getPassword());
        Users savedUser = userService.saveUserIntoDB(user);
        return ResponseEntity.status(201).body(savedUser);
    }
}