package com.ratnakar.code.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtAuthService {
    /*
        JJWT library used to create and process JSON Web Token tokens in Java applications.
        1) jjwt-api – Provides the public APIs used to create, parse, and validate JWT tokens in Java.
        2) jjwt-impl – Contains the internal implementation logic required by the API to actually process JWT operations.
        3) jjwt-jackson – Uses the Jackson library to serialize and deserialize JWT payloads to/from JSON.
    */
    public String generateJwtAuthToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        //return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + 1000*60*3)).signWith(getKey(), SignatureAlgorithm.ES256).compact();
        return "";
    }

}
