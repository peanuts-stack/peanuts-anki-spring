package com.peanuts.anki.study;

import com.peanuts.anki.study.dto.ReviewRequest;
import com.peanuts.anki.study.dto.ReviewResponse;
import com.peanuts.anki.study.dto.StudySessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/decks/{deckId}/start")
    public ResponseEntity<StudySessionDTO> startStudySession(@PathVariable Long deckId) {
        return ResponseEntity.ok(studyService.startStudySession(deckId));
    }

    @PostMapping("/cards/{cardId}/review")
    public ResponseEntity<ReviewResponse> reviewCard(
            @PathVariable Long cardId,
            @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(studyService.reviewCard(cardId, request));
    }
}
