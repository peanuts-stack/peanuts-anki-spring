export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4">Peanuts Anki</h1>
        <p className="text-gray-600 mb-8">
          Spaced repetition learning app
        </p>
        <div className="flex gap-4 justify-center">
          <a
            href="/login"
            className="bg-blue-500 text-white px-6 py-2 rounded hover:bg-blue-600"
          >
            Login
          </a>
          <a
            href="/register"
            className="bg-gray-500 text-white px-6 py-2 rounded hover:bg-gray-600"
          >
            Register
          </a>
        </div>
      </div>
    </main>
  );
}
