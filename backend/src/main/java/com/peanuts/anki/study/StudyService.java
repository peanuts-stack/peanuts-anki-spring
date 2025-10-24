package com.peanuts.anki.study;

import com.peanuts.anki.card.Card;
import com.peanuts.anki.card.CardRepository;
import com.peanuts.anki.card.dto.CardDTO;
import com.peanuts.anki.deck.DeckRepository;
import com.peanuts.anki.study.dto.ReviewRequest;
import com.peanuts.anki.study.dto.ReviewResponse;
import com.peanuts.anki.study.dto.StudySessionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudyService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    public StudySessionDTO startStudySession(Long deckId) {
        log.info("Starting study session for deck: {}", deckId);

        validateDeckExists(deckId);

        List<Card> dueCards = cardRepository.findByDeckIdAndNextReviewDateBeforeOrderByNextReviewDate(deckId, LocalDateTime.now());
        int newCount = countNewCards(dueCards);
        int reviewCount = dueCards.size() - newCount;

        List<CardDTO> cardDTOs = dueCards.stream()
                .map(CardDTO::from)
                .toList();

        return new StudySessionDTO(deckId, cardDTOs, dueCards.size(), newCount, reviewCount);
    }

    @Transactional
    public ReviewResponse reviewCard(Long cardId, ReviewRequest request) {
        log.info("Reviewing card: {} with quality: {}", cardId, request.quality());

        validateQuality(request.quality());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        applySpacedRepetition(card, request.quality());
        card = cardRepository.save(card);

        log.info("Card reviewed: {}, next review: {}", cardId, card.getNextReviewDate());

        return new ReviewResponse(
                CardDTO.from(card),
                card.getNextReviewDate(),
                card.getInterval(),
                false
        );
    }

    private void validateDeckExists(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new RuntimeException("Deck not found");
        }
    }

    private int countNewCards(List<Card> cards) {
        return (int) cards.stream()
                .filter(card -> card.getRepetitions() == 0)
                .count();
    }

    private void validateQuality(Integer quality) {
        if (quality == null || quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }
    }

    private void applySpacedRepetition(Card card, int quality) {
        boolean failed = quality < 3;

        if (failed) {
            resetCard(card);
        } else {
            advanceCard(card);
        }

        updateEaseFactor(card, quality);
        scheduleNextReview(card);
    }

    private void resetCard(Card card) {
        card.setRepetitions(0);
        card.setInterval(1);
    }

    private void advanceCard(Card card) {
        int newInterval = calculateInterval(card);
        card.setInterval(newInterval);
        card.setRepetitions(card.getRepetitions() + 1);
    }

    private int calculateInterval(Card card) {
        int reps = card.getRepetitions();

        if (reps == 0) return 1;
        if (reps == 1) return 6;

        return (int) Math.round(card.getInterval() * card.getEaseFactor());
    }

    private void updateEaseFactor(Card card, int quality) {
        double adjustment = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
        double newEaseFactor = card.getEaseFactor() + adjustment;
        card.setEaseFactor(Math.max(1.3, newEaseFactor));
    }

    private void scheduleNextReview(Card card) {
        LocalDateTime nextReview = LocalDateTime.now().plusDays(card.getInterval());
        card.setNextReviewDate(nextReview);
    }
}
