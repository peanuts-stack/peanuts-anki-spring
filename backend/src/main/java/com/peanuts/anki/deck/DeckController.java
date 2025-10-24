package com.peanuts.anki.deck;

import com.peanuts.anki.deck.dto.CreateDeckRequest;
import com.peanuts.anki.deck.dto.DeckDTO;
import com.peanuts.anki.deck.dto.UpdateDeckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public ResponseEntity<List<DeckDTO>> getUserDecks(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(deckService.getUserDecks(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckDTO> getDeck(@PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeck(id));
    }

    @PostMapping
    public ResponseEntity<DeckDTO> createDeck(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateDeckRequest request) {
        return ResponseEntity.ok(deckService.createDeck(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckDTO> updateDeck(
            @PathVariable Long id,
            @RequestBody UpdateDeckRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}