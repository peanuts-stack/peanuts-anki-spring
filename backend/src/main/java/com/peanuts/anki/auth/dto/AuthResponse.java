package com.peanuts.anki.auth.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email
) {}