'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getDeck } from '@/lib/api/decks';
import { getDeckCards, createCard, deleteCard } from '@/lib/api/cards';
import { DeckDTO } from '@/lib/types/deck';
import { CardDTO } from '@/lib/types/card';

export default function DeckDetailPage() {
  const params = useParams();
  const router = useRouter();
  const deckId = Number(params.id);

  const [deck, setDeck] = useState<DeckDTO | null>(null);
  const [cards, setCards] = useState<CardDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [front, setFront] = useState('');
  const [back, setBack] = useState('');

  useEffect(() => {
    loadData();
  }, [deckId]);

  async function loadData() {
    try {
      const [deckData, cardsData] = await Promise.all([
        getDeck(deckId),
        getDeckCards(deckId),
      ]);
      setDeck(deckData);
      setCards(cardsData);
    } catch (err) {
      console.error('Failed to load data:', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateCard(e: React.FormEvent) {
    e.preventDefault();
    try {
      await createCard(deckId, { front, back });
      setFront('');
      setBack('');
      setShowForm(false);
      loadData();
    } catch (err) {
      console.error('Failed to create card:', err);
    }
  }

  async function handleDeleteCard(cardId: number) {
    if (!confirm('Delete this card?')) return;
    try {
      await deleteCard(deckId, cardId);
      loadData();
    } catch (err) {
      console.error('Failed to delete card:', err);
    }
  }

  if (loading) {
    return <div className="flex justify-center items-center min-h-screen">Loading...</div>;
  }

  if (!deck) {
    return <div className="flex justify-center items-center min-h-screen">Deck not found</div>;
  }

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-4xl mx-auto">
        <button onClick={() => router.push('/decks')} className="mb-4 text-blue-500 hover:underline">
          ← Back to Decks
        </button>

        <div className="bg-white shadow-md rounded p-6 mb-8">
          <h1 className="text-3xl font-bold mb-2">{deck.name}</h1>
          <p className="text-gray-600 mb-4">{deck.description}</p>
          <div className="flex gap-2">
            <button
              onClick={() => setShowForm(!showForm)}
              className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
            >
              {showForm ? 'Cancel' : 'Add Card'}
            </button>
            <button
              onClick={() => router.push(`/decks/${deckId}/study`)}
              className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
            >
              Study ({cards.length} cards)
            </button>
          </div>
        </div>

        {showForm && (
          <form onSubmit={handleCreateCard} className="bg-white shadow-md rounded p-6 mb-8">
            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">Front</label>
              <textarea
                value={front}
                onChange={(e) => setFront(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                rows={3}
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">Back</label>
              <textarea
                value={back}
                onChange={(e) => setBack(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                rows={3}
                required
              />
            </div>
            <button type="submit" className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600">
              Create Card
            </button>
          </form>
        )}

        <div className="grid gap-4">
          {cards.map((card) => (
            <div key={card.id} className="bg-white shadow-md rounded p-6">
              <div className="mb-4">
                <h3 className="text-sm font-bold text-gray-600 mb-1">FRONT</h3>
                <p>{card.front}</p>
              </div>
              <div className="mb-4">
                <h3 className="text-sm font-bold text-gray-600 mb-1">BACK</h3>
                <p>{card.back}</p>
              </div>
              <button
                onClick={() => handleDeleteCard(card.id)}
                className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
              >
                Delete
              </button>
            </div>
          ))}
        </div>

        {cards.length === 0 && (
          <p className="text-center text-gray-600 mt-8">
            No cards yet. Add some to start studying!
          </p>
        )}
      </div>
    </main>
  );
}
