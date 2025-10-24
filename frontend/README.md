# Peanuts Anki Frontend

Next.js 14 frontend for the Peanuts Anki spaced repetition learning app.

## Tech Stack

- **Next.js 14** - App Router
- **TypeScript** - Type safety
- **Tailwind CSS** - Utility-first styling
- **Native Fetch** - API client (no dependencies)

## Structure

```
frontend/
├── app/                    # Next.js App Router pages
│   ├── page.tsx           # Home page
│   ├── login/             # Login page
│   ├── register/          # Register page
│   ├── decks/             # Deck list and detail pages
│   └── [id]/study/        # Study session page
├── lib/
│   ├── api/               # API client functions
│   │   ├── client.ts      # Base API request function
│   │   ├── auth.ts        # Auth API calls
│   │   ├── decks.ts       # Deck API calls
│   │   ├── cards.ts       # Card API calls
│   │   └── study.ts       # Study API calls
│   └── types/             # TypeScript types (mirror backend DTOs)
│       ├── auth.ts
│       ├── deck.ts
│       ├── card.ts
│       └── study.ts
└── components/            # (Empty - all components inline for tutorial simplicity)
```

## Development

### With Docker (recommended)

```bash
# From project root
./scripts/run.sh

# Access at http://localhost:3000
```

### Without Docker

```bash
# Install dependencies
npm install

# Create .env.local
cp .env.local.example .env.local
# Edit .env.local and set: NEXT_PUBLIC_API_URL=http://localhost:8080

# Start dev server
npm run dev

# Access at http://localhost:3000
```

## Environment Variables

### Docker (auto-configured in docker-compose.yml)
- `NEXT_PUBLIC_API_URL=http://backend:8080`

### Local development (.env.local)
- `NEXT_PUBLIC_API_URL=http://localhost:8080`

### Production
- `NEXT_PUBLIC_API_URL=https://api.yourdomain.com`

## Features

- ✅ User registration and login (JWT)
- ✅ Deck CRUD (create, view, delete)
- ✅ Card CRUD (add, view, delete)
- ✅ Study sessions with spaced repetition (SM-2)
- ✅ Quality ratings (0-5) for card reviews

## API Integration

All API calls go through `lib/api/client.ts` which:
- Reads API URL from environment variable
- Adds authentication headers (X-User-Id from localStorage)
- Handles errors consistently

Example:
```typescript
import { getUserDecks } from '@/lib/api/decks';

const decks = await getUserDecks();
```

## Pages

### Public Routes
- `/` - Landing page
- `/login` - Login form
- `/register` - Registration form

### Protected Routes
- `/decks` - List of user's decks
- `/decks/[id]` - Deck detail with cards
- `/decks/[id]/study` - Study session

## Building for Production

```bash
npm run build
npm start
```

Or use Docker:
```bash
docker build -f Dockerfile -t peanuts-anki-frontend .
docker run -p 3000:3000 -e NEXT_PUBLIC_API_URL=http://backend:8080 peanuts-anki-frontend
```
