package com.peanuts.anki.card.dto;

import com.peanuts.anki.card.Card;
import java.time.LocalDateTime;

public record CardDTO(
        Long id,
        String front,
        String back,
        Long deckId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CardDTO from(Card card) {
        return new CardDTO(
                card.getId(),
                card.getFront(),
                card.getBack(),
                card.getDeck().getId(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
