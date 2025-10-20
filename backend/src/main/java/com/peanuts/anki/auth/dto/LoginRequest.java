package com.peanuts.anki.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}
