package com.peanuts.anki.deck.dto;

public record UpdateDeckRequest(
        String name,
        String description
) {}