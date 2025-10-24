package com.peanuts.anki.study.dto;

import com.peanuts.anki.card.dto.CardDTO;
import java.util.List;

public record StudySessionDTO(
        Long deckId,
        List<CardDTO> dueCards,
        int totalDue,
        int newCards,
        int reviewCards
) {
}
