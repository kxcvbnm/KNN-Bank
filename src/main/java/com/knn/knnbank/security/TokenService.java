package com.knn.knnbank.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;



@Service
public class TokenService {
    
    @Value("${jwt.secret.string}")
    private String JWT_SECRET;

    @Value("${jwt.expiration.time}")
    private Long JWT_EXPIRATION;

    private SecretKey key;



    @PostConstruct
    private void init() {
       
        byte[] keyByte = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyByte, "HmacSHA256");

    }

    public String generateToken(String email) {

        return Jwts.builder()
            .subject(email)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .signWith(key)
            .compact();
    }

    public String getUsernameFromToken(String token) {

        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {

        return claimsTFunction
            .apply(Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {

        return extractClaims(token, Claims::getExpiration)
            .before(new Date());
    }

}
