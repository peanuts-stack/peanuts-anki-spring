'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { startStudySession, reviewCard } from '@/lib/api/study';
import { CardDTO } from '@/lib/types/card';

export default function StudyPage() {
  const params = useParams();
  const router = useRouter();
  const deckId = Number(params.id);

  const [cards, setCards] = useState<CardDTO[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [showBack, setShowBack] = useState(false);
  const [loading, setLoading] = useState(true);
  const [sessionComplete, setSessionComplete] = useState(false);

  useEffect(() => {
    loadSession();
  }, [deckId]);

  async function loadSession() {
    try {
      const session = await startStudySession(deckId);
      if (session.dueCards.length === 0) {
        setSessionComplete(true);
      } else {
        setCards(session.dueCards);
      }
    } catch (err) {
      console.error('Failed to start session:', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleReview(quality: number) {
    const currentCard = cards[currentIndex];
    try {
      await reviewCard(currentCard.id, { quality });

      if (currentIndex + 1 >= cards.length) {
        setSessionComplete(true);
      } else {
        setCurrentIndex(currentIndex + 1);
        setShowBack(false);
      }
    } catch (err) {
      console.error('Failed to review card:', err);
    }
  }

  if (loading) {
    return <div className="flex justify-center items-center min-h-screen">Loading...</div>;
  }

  if (sessionComplete || cards.length === 0) {
    return (
      <main className="flex min-h-screen items-center justify-center p-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold mb-4">Session Complete!</h1>
          <p className="text-gray-600 mb-8">No more cards due for review.</p>
          <button
            onClick={() => router.push('/decks')}
            className="bg-blue-500 text-white px-6 py-3 rounded hover:bg-blue-600"
          >
            Back to Decks
          </button>
        </div>
      </main>
    );
  }

  const currentCard = cards[currentIndex];

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-2xl mx-auto">
        <div className="mb-4 flex justify-between items-center">
          <button onClick={() => router.push(`/decks/${deckId}`)} className="text-blue-500 hover:underline">
            ← Back to Deck
          </button>
          <span className="text-gray-600">
            Card {currentIndex + 1} of {cards.length}
          </span>
        </div>

        <div className="bg-white shadow-lg rounded-lg p-8 min-h-[400px] flex flex-col justify-center">
          <div className="mb-8">
            <h3 className="text-sm font-bold text-gray-600 mb-2">FRONT</h3>
            <p className="text-2xl">{currentCard.front}</p>
          </div>

          {showBack && (
            <div className="mb-8 border-t pt-8">
              <h3 className="text-sm font-bold text-gray-600 mb-2">BACK</h3>
              <p className="text-2xl">{currentCard.back}</p>
            </div>
          )}

          {!showBack ? (
            <button
              onClick={() => setShowBack(true)}
              className="bg-blue-500 text-white px-6 py-3 rounded hover:bg-blue-600"
            >
              Show Answer
            </button>
          ) : (
            <div>
              <p className="text-sm text-gray-600 mb-4 text-center">How well did you know this?</p>
              <div className="grid grid-cols-3 gap-2">
                <button
                  onClick={() => handleReview(0)}
                  className="bg-red-500 text-white px-4 py-3 rounded hover:bg-red-600"
                >
                  Again (0)
                </button>
                <button
                  onClick={() => handleReview(3)}
                  className="bg-yellow-500 text-white px-4 py-3 rounded hover:bg-yellow-600"
                >
                  Hard (3)
                </button>
                <button
                  onClick={() => handleReview(4)}
                  className="bg-blue-500 text-white px-4 py-3 rounded hover:bg-blue-600"
                >
                  Good (4)
                </button>
              </div>
              <button
                onClick={() => handleReview(5)}
                className="w-full mt-2 bg-green-500 text-white px-4 py-3 rounded hover:bg-green-600"
              >
                Easy (5)
              </button>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
