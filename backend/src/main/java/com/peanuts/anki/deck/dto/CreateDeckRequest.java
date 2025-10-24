package com.peanuts.anki.deck.dto;

public record CreateDeckRequest(
        String name,
        String description
) {}