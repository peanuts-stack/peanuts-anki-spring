package com.peanuts.anki.deck.dto;

import com.peanuts.anki.deck.Deck;
import java.time.LocalDateTime;

public record DeckDTO(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static DeckDTO from(Deck deck) {
        return new DeckDTO(
                deck.getId(),
                deck.getName(),
                deck.getDescription(),
                deck.getCreatedAt()
        );
    }
}