package com.clinic_api.repository.security;

import com.clinic_api.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    // Gerar token com Role
    public String generateToken(String email, Role role) {
        return Jwts.builder()
            .setSubject(email)
            .claim("role", role.toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(getSigningKey())
            .compact();
    }

    // Extrair Role do token (SEGURO - vem do JWT)
    public Role extractRole(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        return Role.valueOf(claims.get("role", String.class));
    }

    // Extrair Email do token
    public String extractEmail(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.getSubject();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}

