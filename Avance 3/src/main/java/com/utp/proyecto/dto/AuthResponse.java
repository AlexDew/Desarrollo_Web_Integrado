package com.utp.proyecto.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        String email,
        Set<String> roles) {
}
