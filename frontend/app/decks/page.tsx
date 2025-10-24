'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getUserDecks, createDeck, deleteDeck } from '@/lib/api/decks';
import { DeckDTO } from '@/lib/types/deck';

export default function DecksPage() {
  const router = useRouter();
  const [decks, setDecks] = useState<DeckDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  useEffect(() => {
    loadDecks();
  }, []);

  async function loadDecks() {
    try {
      const data = await getUserDecks();
      setDecks(data);
    } catch (err) {
      console.error('Failed to load decks:', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    try {
      await createDeck({ name, description });
      setName('');
      setDescription('');
      setShowForm(false);
      loadDecks();
    } catch (err) {
      console.error('Failed to create deck:', err);
    }
  }

  async function handleDelete(id: number) {
    if (!confirm('Delete this deck?')) return;
    try {
      await deleteDeck(id);
      loadDecks();
    } catch (err) {
      console.error('Failed to delete deck:', err);
    }
  }

  if (loading) {
    return <div className="flex justify-center items-center min-h-screen">Loading...</div>;
  }

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">My Decks</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            {showForm ? 'Cancel' : 'Create Deck'}
          </button>
        </div>

        {showForm && (
          <form onSubmit={handleCreate} className="bg-white shadow-md rounded p-6 mb-8">
            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">Name</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 text-sm font-bold mb-2">Description</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                rows={3}
              />
            </div>
            <button type="submit" className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600">
              Create
            </button>
          </form>
        )}

        <div className="grid gap-4">
          {decks.map((deck) => (
            <div key={deck.id} className="bg-white shadow-md rounded p-6">
              <h3 className="text-xl font-bold mb-2">{deck.name}</h3>
              <p className="text-gray-600 mb-4">{deck.description}</p>
              <div className="flex gap-2">
                <button
                  onClick={() => router.push(`/decks/${deck.id}`)}
                  className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
                >
                  View Cards
                </button>
                <button
                  onClick={() => router.push(`/decks/${deck.id}/study`)}
                  className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
                >
                  Study
                </button>
                <button
                  onClick={() => handleDelete(deck.id)}
                  className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>

        {decks.length === 0 && (
          <p className="text-center text-gray-600 mt-8">
            No decks yet. Create one to get started!
          </p>
        )}
      </div>
    </main>
  );
}
