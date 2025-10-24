import { CardDTO } from "./card";

export interface StudySessionDTO {
  deckId: number;
  dueCards: CardDTO[];
  totalDue: number;
  newCards: number;
  reviewCards: number;
}

export interface ReviewRequest {
  quality: number;
}

export interface ReviewResponse {
  card: CardDTO;
  nextReviewDate: string;
  interval: number;
  sessionComplete: boolean;
}
