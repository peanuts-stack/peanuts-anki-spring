export interface CardDTO {
  id: number;
  front: string;
  back: string;
  deckId: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCardRequest {
  front: string;
  back: string;
}

export interface UpdateCardRequest {
  front: string;
  back: string;
}
