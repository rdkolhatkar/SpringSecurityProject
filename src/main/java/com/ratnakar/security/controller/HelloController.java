package com.ratnakar.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // By default 'spring-boot-starter-security' will provide you the username as 'user' and a random password in your Spring Console like 'Using generated security password: 3f8c9a2e-xxxx-xxxx' for login.
    // You can customize your username and password from your 'application.properties' or 'application.yaml' file.
    @GetMapping("/hello")
    public String greet(HttpServletRequest request) {
        return "Hello World "+request.getSession().getId(); // 'request.getSession().getId()' wit this we can fetch the current session id.
        // session id will change for every new login and logout
    }
    // By adding this 'spring-boot-starter-security' dependency in our project, we have enabled default login feature.
    // When we will hit the URL 'http://localhost:8091/hello' on our browser then Spring Security will ask us for username and password before redirecting to the hello page.
    // Default username for
    @GetMapping("/about")
    public String about(HttpServletRequest request) {
        return "Ratnakar "+request.getSession().getId();
    }

}
