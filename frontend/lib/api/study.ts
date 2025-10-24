import { apiRequest } from './client';
import { StudySessionDTO, ReviewRequest, ReviewResponse } from '../types/study';

export async function startStudySession(deckId: number): Promise<StudySessionDTO> {
  return apiRequest<StudySessionDTO>(`/api/study/decks/${deckId}/start`, {
    method: 'POST',
  });
}

export async function reviewCard(cardId: number, request: ReviewRequest): Promise<ReviewResponse> {
  return apiRequest<ReviewResponse>(`/api/study/cards/${cardId}/review`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}
