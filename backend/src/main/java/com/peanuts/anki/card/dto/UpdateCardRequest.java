package com.peanuts.anki.card.dto;

public record UpdateCardRequest(
        String front,
        String back
) {
}
