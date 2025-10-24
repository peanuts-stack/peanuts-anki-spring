package com.peanuts.anki.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckId(Long deckId);

    long countByDeckId(Long deckId);

    void deleteByDeckId(Long deckId);

    List<Card> findByDeckIdAndNextReviewDateBeforeOrderByNextReviewDate(Long deckId, LocalDateTime now);
}
