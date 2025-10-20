package com.peanuts.anki.auth.dto;

public record RegisterRequest(
        String email,
        String password
) {}