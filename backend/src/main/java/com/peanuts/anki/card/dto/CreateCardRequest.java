package com.peanuts.anki.card.dto;

public record CreateCardRequest(
        String front,
        String back
) {
}
