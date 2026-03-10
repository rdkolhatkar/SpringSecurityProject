package com.ratnakar.oauth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
    @GetMapping("/hello")
    public String greet(){
        return "Welcome! We're glad to see you here";
    }
}
