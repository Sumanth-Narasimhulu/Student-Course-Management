package com.studentmanagement.Security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
@Component
public class JwtService {
    private final SecretKey secretKey;
    public JwtService(@Value("${jwt.secret}") String secret){
       secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(String userName){
        return Jwts.builder().subject(userName).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+1000*60*60)).signWith(secretKey).compact();
    }
    public Claims extractAllClaims(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
    public String extractUserName(String token){
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }
    public Date extractExpiry(String token){
        Claims claims = extractAllClaims(token);
        return claims.getExpiration();
    }
    public boolean isTokenExpired(String token){
        return extractExpiry(token).before(new Date());
    }
    public boolean isValid(String token , String userName){
        return !isTokenExpired(token) && extractUserName(token).equals(userName);
    }
}
