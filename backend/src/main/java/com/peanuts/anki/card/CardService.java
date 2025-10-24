package com.peanuts.anki.card;

import com.peanuts.anki.card.dto.CardDTO;
import com.peanuts.anki.card.dto.CreateCardRequest;
import com.peanuts.anki.card.dto.UpdateCardRequest;
import com.peanuts.anki.deck.Deck;
import com.peanuts.anki.deck.DeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    public List<CardDTO> getDeckCards(Long deckId) {
        log.info("Fetching cards for deck: {}", deckId);
        return cardRepository.findByDeckId(deckId)
                .stream()
                .map(CardDTO::from)
                .toList();
    }

    public CardDTO getCard(Long cardId) {
        log.info("Fetching card: {}", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        return CardDTO.from(card);
    }

    @Transactional
    public CardDTO createCard(Long deckId, CreateCardRequest request) {
        log.info("Creating card for deck: {}", deckId);

        if (request.front() == null || request.front().isBlank()) {
            throw new IllegalArgumentException("Front text is required");
        }
        if (request.back() == null || request.back().isBlank()) {
            throw new IllegalArgumentException("Back text is required");
        }

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        Card card = Card.builder()
                .front(request.front())
                .back(request.back())
                .deck(deck)
                .build();

        card = cardRepository.save(card);
        log.info("Card created: {}", card.getId());

        return CardDTO.from(card);
    }

    @Transactional
    public CardDTO updateCard(Long cardId, UpdateCardRequest request) {
        log.info("Updating card: {}", cardId);

        if (request.front() == null || request.front().isBlank()) {
            throw new IllegalArgumentException("Front text is required");
        }
        if (request.back() == null || request.back().isBlank()) {
            throw new IllegalArgumentException("Back text is required");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setFront(request.front());
        card.setBack(request.back());

        card = cardRepository.save(card);
        log.info("Card updated: {}", cardId);

        return CardDTO.from(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        log.info("Deleting card: {}", cardId);
        cardRepository.deleteById(cardId);
    }

    public long getCardCount(Long deckId) {
        return cardRepository.countByDeckId(deckId);
    }
}
