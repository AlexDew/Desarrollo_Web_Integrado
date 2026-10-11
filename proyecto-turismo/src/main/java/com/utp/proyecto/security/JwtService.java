package com.utp.proyecto.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-ms:900000}") long expirationMs) {
        if (secret.isBlank()) {
            throw new IllegalStateException("Configure JWT_SECRET con una clave Base64 segura de al menos 32 bytes");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS debe ser mayor que cero");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET debe estar codificada en Base64", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe representar al menos 32 bytes");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generarToken(String email) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public String obtenerSubject(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
