import { apiRequest } from './client';
import { DeckDTO, CreateDeckRequest, UpdateDeckRequest } from '../types/deck';

export async function getUserDecks(): Promise<DeckDTO[]> {
  return apiRequest<DeckDTO[]>('/api/decks');
}

export async function getDeck(id: number): Promise<DeckDTO> {
  return apiRequest<DeckDTO>(`/api/decks/${id}`);
}

export async function createDeck(request: CreateDeckRequest): Promise<DeckDTO> {
  return apiRequest<DeckDTO>('/api/decks', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function updateDeck(id: number, request: UpdateDeckRequest): Promise<DeckDTO> {
  return apiRequest<DeckDTO>(`/api/decks/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  });
}

export async function deleteDeck(id: number): Promise<void> {
  await apiRequest<void>(`/api/decks/${id}`, {
    method: 'DELETE',
  });
}
