package com.ratnakar.code.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtAuthService {

    private final String secretKey;

    public JwtAuthService() {
        secretKey = generateSecretKey();
    }

    public String generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error Generating the Key", e);
        }
    }

    public String generateJwtAuthToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)               // ✅ replaces deprecated setClaims()
                .subject(username)            // ✅ replaces deprecated setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))        // ✅ replaces deprecated setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 3))  // ✅ replaces deprecated setExpiration()
                .signWith(getKey())           // ✅ replaces deprecated signWith(key, algorithm) — key type auto-resolves to HS256
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);  // ✅ returns SecretKey, not Key
    }

    // ✅ Fixed - returns SecretKey
    private SecretKey getSecurityKey() {        // ✅ SecretKey instead of Key
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);    // already returns SecretKey, just fix the return type
    }

    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    // ✅ Fixed - JJWT 0.12.x modern approach
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecurityKey())         // ✅ replaces setSigningKey()
                .build()
                .parseSignedClaims(token)             // ✅ replaces parseClaimsJwt() — correct for signed tokens
                .getPayload();                        // ✅ replaces getBody()
    }


    // ✅ Completed - extracts username using extractUserName()
    public String extractUserNameFromToken(String token) {
        return extractUserName(token);                          // reuses extractUserName() via extractClaim()
    }

    // ✅ Completed - validates token against UserDetails
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserNameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); // ✅ checks username match + expiry
    }

    // ✅ Completed - checks if token expiration date is before now
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());     // ✅ true if expired
    }

    // ✅ Completed - extracts expiration date from token claims
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);      // ✅ reuses extractClaim() with getExpiration
    }
}