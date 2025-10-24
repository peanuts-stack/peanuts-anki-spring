export interface DeckDTO {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}

export interface CreateDeckRequest {
  name: string;
  description: string;
}

export interface UpdateDeckRequest {
  name: string;
  description: string;
}
