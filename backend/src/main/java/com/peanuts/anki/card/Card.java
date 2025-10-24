package com.peanuts.anki.card;

import com.peanuts.anki.deck.Deck;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Builder.Default
    private Integer repetitions = 0;

    @Builder.Default
    private Double easeFactor = 2.5;

    @Builder.Default
    private Integer interval = 1;

    @Builder.Default
    private LocalDateTime nextReviewDate = LocalDateTime.now();

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
