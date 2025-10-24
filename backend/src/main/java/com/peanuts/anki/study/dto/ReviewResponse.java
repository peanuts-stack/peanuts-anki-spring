package com.peanuts.anki.study.dto;

import com.peanuts.anki.card.dto.CardDTO;
import java.time.LocalDateTime;

public record ReviewResponse(
        CardDTO card,
        LocalDateTime nextReviewDate,
        int interval,
        boolean sessionComplete
) {
}
