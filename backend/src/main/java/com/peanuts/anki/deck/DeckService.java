package com.peanuts.anki.deck;

import com.peanuts.anki.auth.User;
import com.peanuts.anki.auth.UserRepository;
import com.peanuts.anki.deck.dto.CreateDeckRequest;
import com.peanuts.anki.deck.dto.DeckDTO;
import com.peanuts.anki.deck.dto.UpdateDeckRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    public List<DeckDTO> getUserDecks(Long userId) {
        log.info("Fetching decks for user: {}", userId);
        return deckRepository.findByOwnerId(userId)
                .stream()
                .map(DeckDTO::from)
                .toList();
    }

    public DeckDTO getDeck(Long deckId) {
        log.info("Fetching deck: {}", deckId);
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));
        return DeckDTO.from(deck);
    }

    @Transactional
    public DeckDTO createDeck(Long userId, CreateDeckRequest request) {
        log.info("Creating deck for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Deck deck = Deck.builder()
                .name(request.name())
                .description(request.description())
                .owner(user)
                .build();

        deck = deckRepository.save(deck);
        log.info("Deck created: {}", deck.getId());

        return DeckDTO.from(deck);
    }

    @Transactional
    public DeckDTO updateDeck(Long deckId, UpdateDeckRequest request) {
        log.info("Updating deck: {}", deckId);

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        deck.setName(request.name());
        deck.setDescription(request.description());

        deck = deckRepository.save(deck);
        log.info("Deck updated: {}", deckId);

        return DeckDTO.from(deck);
    }

    @Transactional
    public void deleteDeck(Long deckId) {
        log.info("Deleting deck: {}", deckId);
        deckRepository.deleteById(deckId);
    }
}