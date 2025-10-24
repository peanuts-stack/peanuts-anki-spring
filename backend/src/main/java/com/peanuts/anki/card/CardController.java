package com.peanuts.anki.card;

import com.peanuts.anki.card.dto.CardDTO;
import com.peanuts.anki.card.dto.CreateCardRequest;
import com.peanuts.anki.card.dto.UpdateCardRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<CardDTO>> getDeckCards(@PathVariable Long deckId) {
        return ResponseEntity.ok(cardService.getDeckCards(deckId));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDTO> getCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @PostMapping
    public ResponseEntity<CardDTO> createCard(
            @PathVariable Long deckId,
            @RequestBody CreateCardRequest request) {
        return ResponseEntity.ok(cardService.createCard(deckId, request));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDTO> updateCard(
            @PathVariable Long cardId,
            @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(cardId, request));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
