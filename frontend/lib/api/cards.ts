import { apiRequest } from './client';
import { CardDTO, CreateCardRequest, UpdateCardRequest } from '../types/card';

export async function getDeckCards(deckId: number): Promise<CardDTO[]> {
  return apiRequest<CardDTO[]>(`/api/decks/${deckId}/cards`);
}

export async function getCard(deckId: number, cardId: number): Promise<CardDTO> {
  return apiRequest<CardDTO>(`/api/decks/${deckId}/cards/${cardId}`);
}

export async function createCard(deckId: number, request: CreateCardRequest): Promise<CardDTO> {
  return apiRequest<CardDTO>(`/api/decks/${deckId}/cards`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function updateCard(deckId: number, cardId: number, request: UpdateCardRequest): Promise<CardDTO> {
  return apiRequest<CardDTO>(`/api/decks/${deckId}/cards/${cardId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export async function deleteCard(deckId: number, cardId: number): Promise<void> {
  await apiRequest<void>(`/api/decks/${deckId}/cards/${cardId}`, {
    method: 'DELETE',
  });
}
