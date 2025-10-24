# Building a Full-Stack Anki Clone: From Zero to Production

> **A comprehensive guide to building real-world applications that iterate fast and scale smart**

**Author:** Peanuts Stack
**Project:** Peanuts Anki - Spaced Repetition Learning App
**Stack:** Spring Boot 3.2 + Next.js 14 + PostgreSQL + Docker
**Philosophy:** Weekend projects with production-grade architecture

---

## Table of Contents

- [PART I: Introduction & Philosophy](#part-i-introduction--philosophy)
- [PART II: Architecture Overview](#part-ii-architecture-overview)
- [PART III: Backend Deep Dive](#part-iii-backend-deep-dive)
- [PART IV: Frontend Deep Dive](#part-iv-frontend-deep-dive)
- [PART V: Docker & Environment Setup](#part-v-docker--environment-setup)
- [PART VI: Data Flow & Request Lifecycle](#part-vi-data-flow--request-lifecycle)
- [PART VII: Scaling Considerations](#part-vii-scaling-considerations)
- [PART VIII: Common Patterns & Best Practices](#part-viii-common-patterns--best-practices)
- [PART IX: Debugging & Troubleshooting](#part-ix-debugging--troubleshooting)
- [PART X: Next Steps & Learning Path](#part-x-next-steps--learning-path)
- [Appendix](#appendix)

---

# PART I: Introduction & Philosophy

## 1. What You'll Learn

This tutorial teaches you how to build a complete full-stack application from scratch. Not a toy example—a real application with authentication, database persistence, complex business logic, and production deployment considerations.

**Specifically, you'll master:**

- **Backend Development** - Spring Boot REST APIs, JPA/Hibernate ORM, service-layer architecture, transaction management
- **Frontend Development** - Next.js 14 App Router, TypeScript, client-server component patterns, API integration
- **Database Design** - PostgreSQL schema design, relationships, query optimization
- **Authentication** - JWT token-based auth, security configuration, CORS handling
- **Docker & DevOps** - Multi-stage builds, docker-compose orchestration, dev/prod environments
- **System Design** - Request lifecycle, data flow, scaling patterns, architecture trade-offs

**What makes this different:**

Most tutorials teach you *syntax*. This teaches you *thinking*. You'll understand:
- Why we chose each technology
- When to simplify vs when to add complexity
- How to make decisions that enable fast iteration AND future scaling
- What production-grade code looks like (even when simplified for learning)

## 2. Why This Project Exists

### The Problem with Traditional Learning

Traditional software engineering education follows one of two extremes:

**Extreme 1: Toy Examples**
- "Build a todo app with Firebase"
- Works in 10 minutes
- Zero understanding of production systems
- Breaks when you try to add real features

**Extreme 2: Enterprise Complexity**
- "Microservices with Kubernetes and Kafka"
- Takes weeks to set up
- Overwhelming for learners
- Over-engineered for 90% of projects

### Our Philosophy: The Middle Path

We believe the best way to learn is building **real applications** with **production awareness** but **tutorial simplicity**.

**Key Principles:**

1. **$0 to $5/mo Philosophy**
   - Develop locally with Docker (free)
   - Deploy to VPS for $5/month (not $500/month AWS)
   - No vendor lock-in—runs anywhere Docker runs

2. **Iterate Fast, Scale Smart**
   - Start with a monolith (not microservices)
   - But organize code so extraction is easy later
   - Use patterns that scale even when simplified

3. **Bare-Bones But Correct**
   - Minimal code to understand easily
   - But using industry-standard patterns
   - No shortcuts that teach bad habits

4. **Tutorial-Friendly, Production-Aware**
   - Explain every decision
   - Show the simple version AND the production version
   - Teach when to use which

## 3. Design Philosophy

### Explicit Over Magic

```java
// ✅ Explicit - You can see what's happening
@Transactional
public CardDTO createCard(Long deckId, CreateCardRequest request) {
    if (request.front() == null || request.front().isBlank()) {
        throw new IllegalArgumentException("Front text is required");
    }

    Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new RuntimeException("Deck not found"));

    Card card = Card.builder()
            .front(request.front())
            .back(request.back())
            .deck(deck)
            .build();

    return CardDTO.from(cardRepository.save(card));
}

// ❌ Magic - Too much abstraction for learning
@PostMapping
@Validated
public CardDTO createCard(@Valid @RequestBody CreateCardRequest request) {
    return cardService.create(request);  // Where's the validation? The error handling?
}
```

**Why explicit matters:**
- Learners see the full picture
- Debugging is easier
- You understand what frameworks do for you

### Convention Over Configuration

We leverage framework conventions smartly:

```java
// Spring Data JPA generates this query automatically from the method name
List<Card> findByDeckIdAndNextReviewDateBeforeOrderByNextReviewDate(
    Long deckId,
    LocalDateTime now
);

// You don't write:
// @Query("SELECT c FROM Card c WHERE c.deck.id = :deckId AND c.nextReviewDate < :now ORDER BY c.nextReviewDate")
```

**When to use conventions:**
- Industry-standard patterns (everyone understands them)
- Well-documented behavior (easy to look up)
- Saves boilerplate without hiding logic

### Symmetry Between Layers

Frontend structure mirrors backend structure:

```
Backend:                Frontend:
/auth                   /lib/api/auth.ts
  AuthController          login(), register()

/deck                   /lib/api/decks.ts
  DeckController          getUserDecks(), createDeck()

/card                   /lib/api/cards.ts
  CardController          getDeckCards(), createCard()
```

**Why symmetry helps:**
- Easier to navigate codebase
- Clear where to add features
- Frontend devs can predict backend structure (and vice versa)

## 4. What We Built

### Feature Overview

**Peanuts Anki** is a spaced repetition learning application inspired by Anki, the popular flashcard app used by millions of students.

**Core Features:**
- ✅ User registration and authentication (JWT-based)
- ✅ Deck management (create, view, update, delete)
- ✅ Flashcard CRUD (add cards to decks)
- ✅ Study sessions with spaced repetition (SM-2 algorithm)
- ✅ Quality ratings (0-5 scale) affecting card scheduling
- ✅ Progress tracking (new vs review cards)

**What's NOT Included (For Simplicity):**
- ❌ Deck sharing between users
- ❌ Rich media (images, audio)
- ❌ Advanced statistics
- ❌ Mobile apps
- ❌ Offline support

### The Spaced Repetition Algorithm

The core of Anki is the **SM-2 (SuperMemo 2) algorithm**, a scientifically-backed method for optimizing long-term memory retention.

**How it works:**

```
New card:
  Show immediately → User rates quality (0-5)

If quality ≥ 3 (correct):
  Show again after: 1 day → 6 days → ~2 weeks → ~1 month → exponential growth

If quality < 3 (incorrect):
  Reset to 1 day (relearning)

Each review adjusts the "ease factor" based on how well you knew it.
```

**Why this matters:**
- You see cards right before you forget them
- Optimal for long-term retention
- Scientifically proven to work

We'll dive deep into the implementation in Part III.

### Tech Stack Decision Matrix

| Technology | Why We Chose It | Alternative Considered | Why Not the Alternative |
|------------|----------------|----------------------|------------------------|
| **Spring Boot 3.2** | Industry standard, excellent conventions, mature ecosystem | Node.js + Express | Less structure, more boilerplate for enterprise patterns |
| **Next.js 14** | Modern React framework, server components, excellent DX | Create React App | No SSR, no file-based routing, deprecated |
| **PostgreSQL** | Production-ready, ACID compliant, rich feature set | MongoDB | Relational data fits our domain better (users → decks → cards) |
| **Docker** | Dev/prod parity, isolated dependencies, portable | Local installs | "Works on my machine" problems |
| **TypeScript** | Type safety, better tooling, self-documenting | JavaScript | Too easy to make mistakes at scale |
| **Tailwind CSS** | Utility-first, fast prototyping, no CSS files to manage | Styled Components | More boilerplate, harder for beginners |

### Project Statistics

```
Backend:
  - 6 entities (User, Account, Deck, Card + SM-2 fields)
  - 3 services (Auth, Deck, Card, Study)
  - 4 controllers
  - ~800 lines of Java code (excluding config/tests)

Frontend:
  - 6 pages (home, login, register, decks, deck detail, study)
  - 4 API modules
  - 4 TypeScript type definitions
  - ~600 lines of TypeScript/React code

Infrastructure:
  - 3 Docker services (postgres, backend, frontend)
  - 2 Dockerfiles per service (dev + prod)
  - 1 docker-compose.yml (orchestration)
```

**Total: ~1500 lines of application code**

Compare this to enterprise applications (50,000+ lines) or toy examples (100 lines). This is the sweet spot for learning.

---

# PART II: Architecture Overview

## 5. The Big Picture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         BROWSER                              │
│  (User interacts with application via web browser)           │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP Requests
                     │ (localhost:3000)
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND CONTAINER                         │
│  ┌────────────────────────────────────────────────────────┐  │
│  │           Next.js 14 (Node 20 Alpine)                  │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐  │  │
│  │  │ /app         │  │ /lib/api     │  │ /lib/types  │  │  │
│  │  │ (Pages/      │  │ (API         │  │ (TypeScript │  │  │
│  │  │  Routes)     │  │  Clients)    │  │  Types)     │  │  │
│  │  └──────────────┘  └──────────────┘  └─────────────┘  │  │
│  │              Renders HTML + hydrates React              │  │
│  └────────────────────────────────────────────────────────┘  │
│    Port: 3000  |  Volume: ./frontend:/app (hot reload)       │
└────────────────────┬──────────────────────────────────────── ┘
                     │ HTTP API Calls
                     │ (localhost:8080/api/*)
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND CONTAINER                          │
│  ┌────────────────────────────────────────────────────────┐  │
│  │        Spring Boot 3.2 (Gradle 8.5 + JDK 17)           │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐ │  │
│  │  │Controllers│  │ Services │  │Repositories│  │Entities│ │  │
│  │  │ (REST    │→ │ (Business│→ │  (Data    │→ │ (JPA)  │ │  │
│  │  │  API)    │  │  Logic)  │  │  Access)  │  │        │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └────────┘ │  │
│  │                                                         │  │
│  │  Security: JWT (no filter yet), CORS enabled           │  │
│  └────────────────────────────────────────────────────────┘  │
│    Port: 8080  |  Volume: ./backend:/app (hot reload)        │
└────────────────────┬──────────────────────────────────────── ┘
                     │ JDBC Connection
                     │ (postgres:5432)
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   DATABASE CONTAINER                          │
│  ┌────────────────────────────────────────────────────────┐  │
│  │              PostgreSQL 15 Alpine                       │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐            │  │
│  │  │  users   │  │  decks   │  │  cards   │            │  │
│  │  │table     │  │table     │  │table     │            │  │
│  │  └──────────┘  └──────────┘  └──────────┘            │  │
│  │  Schema auto-generated by Hibernate (ddl-auto: update) │  │
│  └────────────────────────────────────────────────────────┘  │
│    Port: 5432  |  Volume: anki-db (persistent storage)       │
└─────────────────────────────────────────────────────────────┘

All containers connected via Docker network: anki-network
```

### Request Flow: High-Level

```
1. User Action (Browser)
   ↓
2. Frontend React Component (Client-side JavaScript)
   ↓
3. API Client Function (lib/api/*.ts)
   ↓
4. HTTP Request (fetch)
   ↓
   ┌─── Docker Network ───┐
   ↓
5. Spring Boot Controller (@RestController)
   ↓
6. Service Layer (@Service, @Transactional)
   ↓
7. Repository (Spring Data JPA)
   ↓
8. Hibernate ORM (generates SQL)
   ↓
9. PostgreSQL Database (executes query)
   ↓
   [ Response flows back up the chain ]
   ↓
10. JSON Response → Frontend → User sees result
```

### Development vs Production Architecture

**Development (What We're Building):**
```
┌──────────────┐
│ Your Browser │
│ localhost:   │
│ 3000 (FE)    │────┐
│ 8080 (BE)    │    │
└──────────────┘    │
                    ▼
         ┌─────────────────────┐
         │ Docker Compose      │
         │ ┌─────┐ ┌─────┐    │
         │ │ FE  │ │ BE  │ DB │
         │ └─────┘ └─────┘    │
         │   Hot reload        │
         └─────────────────────┘

Cost: $0 (runs on your laptop)
```

**Production (Future Path):**
```
┌──────────────┐
│   Internet   │
│   Users      │
└───────┬──────┘
        ▼
┌─────────────────────┐
│  NGINX (Port 80/443)│
│  - SSL termination  │
│  - Rate limiting    │
│  - Load balancing   │
└─────────┬───────────┘
          ├─────── /api/* ──────→ Backend (scaled)
          └─────── /* ───────────→ Frontend (SSR)
                                    ↓
                            PostgreSQL (separate host)

Cost: $5-15/mo (VPS deployment)
```

## 6. Technology Choices & Why

### Backend: Spring Boot 3.2

**What is Spring Boot?**

Spring Boot is an opinionated framework built on top of the Spring Framework. It provides:
- Auto-configuration (sensible defaults for common tasks)
- Embedded web server (Tomcat) - no separate setup
- Dependency injection - manage object creation
- Production-ready features (health checks, metrics)

**Why Spring Boot for this project?**

1. **Convention Over Configuration**
   ```java
   @SpringBootApplication  // This one annotation does:
   public class AnkiApplication {  // - Component scanning
       public static void main(String[] args) {  // - Auto-configuration
           SpringApplication.run(AnkiApplication.class, args);  // - Bean creation
       }
   }
   ```

2. **Mature Ecosystem**
   - Spring Data JPA - database abstraction
   - Spring Security - authentication/authorization
   - Spring Boot DevTools - hot reload
   - Massive community, tons of resources

3. **Enterprise Patterns Built-In**
   - Dependency injection
   - Transaction management
   - Exception handling
   - Validation

4. **Easy to Learn, Hard to Outgrow**
   - Start simple (what we're doing)
   - Add complexity as needed (microservices, messaging, etc.)

**Alternatives Considered:**

| Framework | Pros | Cons | Why Not? |
|-----------|------|------|----------|
| Node.js + Express | JavaScript everywhere, fast prototyping | Less structure, DIY dependency injection | Harder to enforce good patterns for learners |
| Django (Python) | Admin panel out-of-box, ORM included | Python slower than Java, less type safety | Great choice, but we wanted JVM ecosystem |
| .NET Core | Excellent tooling, similar to Spring | Windows-centric ecosystem (changing) | Spring Boot has more cloud-native adoption |

### Frontend: Next.js 14 (App Router)

**What is Next.js?**

Next.js is a React framework that adds:
- File-based routing (folders = URLs)
- Server-side rendering (SEO, performance)
- API routes (optional backend in same repo)
- Image optimization, font loading, etc.

**Why Next.js 14 specifically?**

1. **App Router (New in Next.js 13/14)**
   ```
   Old Pages Router:           New App Router:
   pages/                      app/
     index.tsx     →             page.tsx (/)
     login.tsx     →             login/
                                   page.tsx (/login)
     decks/                      decks/
       [id].tsx    →               [id]/
                                     page.tsx (/decks/123)
   ```

   **App Router advantages:**
   - Layouts that persist across pages
   - Server components by default (less JavaScript sent to browser)
   - Nested routes (folder structure = URL structure)

2. **Server Components**
   ```tsx
   // This runs on the server (no JavaScript sent to browser)
   export default async function DeckPage() {
       const decks = await fetchDecks();  // Can call DB directly!
       return <DeckList decks={decks} />;
   }

   // This runs in the browser (interactive)
   'use client';
   export default function DeckForm() {
       const [name, setName] = useState('');
       return <form>...</form>;
   }
   ```

   **Why this matters:**
   - Faster page loads (less JavaScript)
   - Better SEO (content rendered server-side)
   - Clear separation (server data fetching vs client interactivity)

3. **TypeScript First-Class Support**
   ```tsx
   // Type safety from API to UI
   async function getDeck(id: number): Promise<DeckDTO> {
       // ...
   }
   ```

**Why Not Create React App?**

Create React App (CRA) was the standard for years, but:
- ❌ No server-side rendering
- ❌ No file-based routing
- ❌ No built-in API routes
- ❌ Officially deprecated (React team recommends frameworks)

### Database: PostgreSQL 15

**What is PostgreSQL?**

PostgreSQL is an open-source relational database known for:
- ACID compliance (Atomicity, Consistency, Isolation, Durability)
- Rich data types (JSON, arrays, UUIDs, etc.)
- Advanced features (full-text search, geospatial data)
- Excellent performance

**Why PostgreSQL for Anki?**

1. **Relational Data is a Perfect Fit**
   ```sql
   User ──1:N──→ Deck ──1:N──→ Card

   A user HAS MANY decks
   A deck HAS MANY cards
   A card BELONGS TO one deck
   ```

   This is classic relational modeling. NoSQL would add complexity without benefits.

2. **Free and Open Source**
   - No licensing costs
   - Runs anywhere (cloud, VPS, laptop)
   - Not tied to a vendor

3. **Production-Grade**
   ```
   Users: Instagram, Uber, Netflix, Apple
   Scale: Handles billions of rows
   Uptime: 99.99%+ in production
   ```

**Why Not MongoDB (NoSQL)?**

MongoDB is great for:
- ✅ Flexible schemas (data structure changes frequently)
- ✅ Nested documents (one query instead of joins)
- ✅ Horizontal scaling (sharding built-in)

But our use case:
- ❌ Schema is stable (users, decks, cards don't change much)
- ❌ We WANT relational integrity (can't delete deck if cards exist)
- ❌ Joins are simple (one level deep: deck → cards)

**Database Design Philosophy:**

```sql
-- ✅ Normalized (avoid duplication)
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    deck_id BIGINT REFERENCES decks(id)  -- Foreign key
);

-- ❌ Denormalized (duplicates deck info in every card)
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    front TEXT,
    back TEXT,
    deck_id BIGINT,
    deck_name TEXT,      -- Duplication!
    deck_description TEXT -- Duplication!
);
```

**Why normalization:**
- Update deck name once, reflects in all cards
- Data integrity enforced by foreign keys
- Smaller database size

### Docker: Development & Deployment

**What is Docker?**

Docker packages your application and its dependencies into a "container" - a lightweight, portable, isolated environment.

```
Without Docker:                With Docker:
┌─────────────────┐            ┌─────────────────┐
│ Your Machine    │            │ Your Machine    │
│ - Java 17       │            │ - Docker        │
│ - Node 20       │            │   ┌───────────┐ │
│ - Postgres 15   │            │   │ Container │ │
│ - Gradle 8.5    │            │   │ - Java 17 │ │
│ - npm           │            │   │ - Gradle  │ │
│                 │            │   │ - Source  │ │
│ "Works on my    │            │   └───────────┘ │
│  machine!"      │            │   ┌───────────┐ │
└─────────────────┘            │   │ Container │ │
                               │   │ - Node 20 │ │
                               │   │ - Next.js │ │
                               │   └───────────┘ │
                               │   ┌───────────┐ │
                               │   │ Container │ │
                               │   │ - Postgres│ │
                               │   └───────────┘ │
                               │                 │
                               │ "Works          │
                               │  everywhere!"   │
                               └─────────────────┘
```

**Why Docker for This Project?**

1. **Dev/Prod Parity**
   ```bash
   # Development (your laptop)
   docker compose up

   # Production (VPS)
   docker compose -f docker-compose.prod.yml up -d

   # Same containers, different configuration
   ```

2. **One Command to Rule Them All**
   ```bash
   # Instead of:
   # Terminal 1: Start PostgreSQL
   # Terminal 2: ./gradlew bootRun
   # Terminal 3: npm run dev

   # Just:
   docker compose up
   ```

3. **Isolated Dependencies**
   ```
   Project A needs Node 16
   Project B needs Node 20

   Without Docker: Version conflicts, nvm, pain
   With Docker: Each project has its own container, zero conflicts
   ```

4. **Hot Reload During Development**
   ```yaml
   volumes:
     - ./backend:/app  # Your changes → instantly in container
   ```

### TypeScript: Type Safety

**What is TypeScript?**

TypeScript is JavaScript with types. It compiles to JavaScript but catches errors at compile-time.

```typescript
// JavaScript (runtime error)
function createDeck(name, description) {
    return fetch('/api/decks', {
        method: 'POST',
        body: JSON.stringify({ nam: name })  // Typo! Only fails at runtime
    });
}

// TypeScript (compile-time error)
interface CreateDeckRequest {
    name: string;
    description: string;
}

function createDeck(request: CreateDeckRequest): Promise<DeckDTO> {
    return fetch('/api/decks', {
        method: 'POST',
        body: JSON.stringify({ nam: request.name })  // ERROR: "nam" doesn't exist
    });
}
```

**Why TypeScript for Frontend?**

1. **Catch Bugs Before Runtime**
   ```typescript
   // Backend changes CardDTO
   interface CardDTO {
       id: number;
       front: string;
       back: string;
       deckId: number;  // Added this field
   }

   // TypeScript immediately shows all places that need updating
   // JavaScript: Fails silently or at runtime
   ```

2. **Better IDE Support**
   - Autocomplete knows what fields exist
   - Hover to see function signatures
   - Refactor with confidence

3. **Self-Documenting**
   ```typescript
   // Clear contract
   async function reviewCard(
       cardId: number,
       request: ReviewRequest
   ): Promise<ReviewResponse>

   // vs JavaScript
   async function reviewCard(cardId, request) {
       // What type is cardId? What fields does request have?
       // Have to read the implementation to know
   }
   ```

### Tailwind CSS: Utility-First Styling

**What is Tailwind?**

Tailwind provides low-level utility classes instead of pre-made components.

```html
<!-- Traditional CSS -->
<style>
.card {
    background-color: white;
    padding: 1.5rem;
    border-radius: 0.5rem;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
</style>
<div class="card">Content</div>

<!-- Tailwind -->
<div class="bg-white p-6 rounded-lg shadow-md">Content</div>
```

**Why Tailwind?**

1. **No Context Switching**
   - Write styles in JSX (no separate CSS files)
   - See exactly what's applied (no hunting through stylesheets)

2. **Responsive by Default**
   ```html
   <div class="w-full md:w-1/2 lg:w-1/3">
       <!-- Full width on mobile -->
       <!-- Half width on tablets -->
       <!-- Third width on desktop -->
   </div>
   ```

3. **Consistency**
   - Spacing: `p-4` = 1rem, `p-6` = 1.5rem (standardized scale)
   - Colors: `bg-blue-500`, `bg-blue-600` (consistent palette)

4. **Fast Prototyping**
   ```html
   <!-- Build a form in seconds -->
   <form class="max-w-md mx-auto p-6 bg-white rounded-lg shadow-md">
       <input class="w-full px-3 py-2 border rounded" />
       <button class="w-full bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">
           Submit
       </button>
   </form>
   ```

**Why Not CSS-in-JS (Styled Components)?**

Styled Components is great, but:
- More boilerplate (define component, write CSS, export)
- Harder for beginners (need to understand both React AND CSS-in-JS)
- Tailwind is more common in modern stacks (better for learning/job market)

## 7. Project Structure Deep Dive

### Backend Structure

```
backend/
├── src/
│   └── main/
│       ├── java/com/peanuts/anki/
│       │   ├── AnkiApplication.java        # Entry point
│       │   ├── auth/                       # Authentication module
│       │   │   ├── User.java              # Entity
│       │   │   ├── UserRepository.java    # Data access
│       │   │   ├── AuthService.java       # Business logic
│       │   │   ├── AuthController.java    # REST endpoints
│       │   │   └── dto/                   # Request/Response objects
│       │   │       ├── LoginRequest.java
│       │   │       ├── RegisterRequest.java
│       │   │       └── AuthResponse.java
│       │   ├── deck/                      # Deck module
│       │   │   ├── Deck.java
│       │   │   ├── DeckRepository.java
│       │   │   ├── DeckService.java
│       │   │   ├── DeckController.java
│       │   │   └── dto/
│       │   ├── card/                      # Card module
│       │   │   ├── Card.java
│       │   │   ├── CardRepository.java
│       │   │   ├── CardService.java
│       │   │   ├── CardController.java
│       │   │   └── dto/
│       │   ├── study/                     # Study session module
│       │   │   ├── StudyService.java
│       │   │   ├── StudyController.java
│       │   │   └── dto/
│       │   ├── security/                  # Cross-cutting security
│       │   │   ├── SecurityConfig.java    # Spring Security setup
│       │   │   └── JwtUtil.java          # JWT helper
│       │   └── config/                    # Application config
│       │       └── WebConfig.java         # CORS, etc.
│       └── resources/
│           ├── application.yml            # Main config
│           └── application-dev.yml        # Dev overrides
├── build.gradle                           # Dependencies
├── Dockerfile                             # Production build
└── Dockerfile.dev                         # Development build
```

**Why Organize by Feature (not Layer)?**

```
❌ Organizing by Layer (traditional):
/entities
  User.java
  Deck.java
  Card.java
/repositories
  UserRepository.java
  DeckRepository.java
  CardRepository.java
/services
  ...
/controllers
  ...

Problem:
- Related code scattered across folders
- Hard to see module boundaries
- Difficult to extract to microservice later

✅ Organizing by Feature (modern):
/auth
  User.java
  UserRepository.java
  AuthService.java
  AuthController.java
/deck
  Deck.java
  DeckRepository.java
  DeckService.java
  DeckController.java

Benefits:
- All deck code in one place
- Clear module boundaries
- Easy to extract /deck into Deck Service later
- Understand a feature by reading one folder
```

**The Layered Architecture Pattern:**

Within each module, we follow the same pattern:

```
Request Flow:
┌──────────────┐
│  Controller  │  ← Handles HTTP (routing, request/response)
└──────┬───────┘
       │ calls
       ▼
┌──────────────┐
│   Service    │  ← Business logic, transactions
└──────┬───────┘
       │ uses
       ▼
┌──────────────┐
│  Repository  │  ← Data access (SQL abstraction)
└──────┬───────┘
       │ operates on
       ▼
┌──────────────┐
│   Entity     │  ← Database table mapping
└──────────────┘
```

**Why This Separation?**

```java
// ✅ Good - Each layer has one responsibility
@RestController
public class DeckController {
    public ResponseEntity<DeckDTO> getDeck(@PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeck(id));  // Just routing
    }
}

@Service
public class DeckService {
    @Transactional(readOnly = true)
    public DeckDTO getDeck(Long deckId) {
        Deck deck = deckRepository.findById(deckId)  // Business logic
                .orElseThrow(() -> new RuntimeException("Deck not found"));
        return DeckDTO.from(deck);
    }
}

public interface DeckRepository extends JpaRepository<Deck, Long> {
    // Just data access, Spring generates implementation
}

// ❌ Bad - Everything in controller
@RestController
public class DeckController {
    public ResponseEntity<DeckDTO> getDeck(@PathVariable Long id) {
        // SQL in controller? Transaction management? Error handling?
        // This gets messy fast
    }
}
```

### Frontend Structure

```
frontend/
├── app/                           # Next.js App Router
│   ├── layout.tsx                # Root layout (wraps all pages)
│   ├── page.tsx                  # Home page (/)
│   ├── globals.css               # Tailwind imports
│   ├── login/
│   │   └── page.tsx             # /login
│   ├── register/
│   │   └── page.tsx             # /register
│   ├── decks/
│   │   ├── page.tsx             # /decks (list)
│   │   └── [id]/                # /decks/123 (dynamic route)
│   │       ├── page.tsx         # Deck detail
│   │       └── study/
│   │           └── page.tsx     # /decks/123/study
├── lib/                          # Business logic
│   ├── api/                      # API clients (mirrors backend)
│   │   ├── client.ts            # Base fetch wrapper
│   │   ├── auth.ts              # Authentication API
│   │   ├── decks.ts             # Deck API
│   │   ├── cards.ts             # Card API
│   │   └── study.ts             # Study API
│   └── types/                    # TypeScript types (mirror backend DTOs)
│       ├── auth.ts
│       ├── deck.ts
│       ├── card.ts
│       └── study.ts
├── components/                   # (Empty for this tutorial)
│   # We kept components inline for simplicity
├── public/                       # Static assets
├── .env.local                    # Local environment variables
├── next.config.mjs              # Next.js configuration
├── tailwind.config.ts           # Tailwind configuration
├── tsconfig.json                # TypeScript configuration
├── package.json                 # Dependencies
├── Dockerfile                   # Production build
└── Dockerfile.dev              # Development build
```

**App Router File Conventions:**

```
app/
├── layout.tsx          # Wraps all pages (nav, footer)
├── page.tsx            # The actual page content
├── loading.tsx         # Shown while page loads
├── error.tsx           # Error boundary
└── not-found.tsx       # 404 page
```

**Dynamic Routes:**

```
app/
└── decks/
    └── [id]/           # [brackets] = dynamic segment
        └── page.tsx

// In page.tsx:
export default function DeckDetailPage() {
    const params = useParams();
    const deckId = params.id;  // "123" from /decks/123
}
```

**Why `lib/` for Non-Component Code?**

```
app/          ← UI components (pages, layouts)
lib/          ← Business logic (API calls, utilities)
components/   ← Reusable UI components
public/       ← Static files (images, fonts)
```

This separation keeps concerns clean:
- `app/` = "what the user sees"
- `lib/` = "how we get the data"
- `components/` = "reusable pieces"

**Why We Skipped `components/` for This Tutorial:**

```tsx
// ❌ Over-abstraction for simple UI
// components/deck/DeckCard.tsx
export function DeckCard({ deck }: DeckCardProps) {
    return <div>...</div>;
}

// app/decks/page.tsx
import { DeckCard } from '@/components/deck/DeckCard';
{decks.map(deck => <DeckCard key={deck.id} deck={deck} />)}

// ✅ Inline for clarity (tutorial-friendly)
// app/decks/page.tsx
{decks.map(deck => (
    <div key={deck.id} className="bg-white shadow-md rounded p-6">
        <h3>{deck.name}</h3>
        <p>{deck.description}</p>
    </div>
))}
```

**When to extract to components:**
- Reused in multiple places
- Complex logic that deserves its own file
- You want to test it in isolation

For a weekend project, inline is often clearer.

---

*[To be continued in the next message due to length...]*

Would you like me to continue with PART III: Backend Deep Dive?
# PART III: Backend Deep Dive

## 8. Spring Boot Fundamentals

### How Spring Boot Actually Works

When you run `SpringApplication.run(AnkiApplication.class, args)`, Spring Boot performs a complex startup sequence:

```
1. Classpath Scanning
   ↓
   Finds all classes annotated with @Component, @Service, @Repository, @Controller
   
2. Auto-Configuration
   ↓
   Detects dependencies (Spring Data JPA, PostgreSQL driver, etc.)
   Automatically configures beans (DataSource, EntityManager, etc.)
   
3. Dependency Injection
   ↓
   Creates instances of all beans
   Wires dependencies (constructor injection, field injection)
   
4. Embedded Server Startup
   ↓
   Starts Tomcat on port 8080
   Registers all @Controller endpoints
   
5. Application Ready
   ↓
   Listens for HTTP requests
```

### The Magic of @SpringBootApplication

```java
@SpringBootApplication  // This ONE annotation is actually THREE:
public class AnkiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnkiApplication.class, args);
    }
}

// Equivalent to:
@Configuration          // "This class has bean definitions"
@EnableAutoConfiguration // "Auto-configure based on classpath"
@ComponentScan         // "Scan this package and subpackages for components"
public class AnkiApplication { ... }
```

**What @ComponentScan does:**

```
com.peanuts.anki/
├── AnkiApplication.java (@SpringBootApplication)
├── auth/
│   ├── AuthService.java (@Service)         ← Found and registered
│   └── UserRepository.java (@Repository)   ← Found and registered
├── deck/
│   ├── DeckController.java (@RestController) ← Found and registered
│   └── DeckService.java (@Service)          ← Found and registered
```

Spring creates ONE instance of each (singleton by default) and manages their lifecycle.

### Dependency Injection Explained

**The Problem Without DI:**

```java
// ❌ Tightly coupled - hard to test, hard to change
public class DeckController {
    private DeckService deckService = new DeckService();  // Creating own dependency
    
    public DeckDTO getDeck(Long id) {
        return deckService.getDeck(id);
    }
}

// To test DeckController, you MUST use the real DeckService
// Can't mock it, can't substitute a test version
```

**The Solution With DI:**

```java
// ✅ Loosely coupled - Spring provides the dependency
@RestController
public class DeckController {
    private final DeckService deckService;  // Declare what you need
    
    // Spring calls this constructor and passes DeckService instance
    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }
}

// Or use Lombok to generate the constructor:
@RestController
@RequiredArgsConstructor  // Generates constructor for all 'final' fields
public class DeckController {
    private final DeckService deckService;
}
```

**How Spring Resolves Dependencies:**

```
Spring's Dependency Resolution Algorithm:

1. DeckController needs DeckService
   ↓
2. Is there a @Service bean of type DeckService? YES → Use it
   ↓
3. DeckService needs DeckRepository
   ↓
4. Is there a @Repository bean of type DeckRepository? YES → Use it
   ↓
5. Create DeckRepository → Inject into DeckService → Inject into DeckController
```

**What if there are multiple beans of the same type?**

```java
// Two implementations of UserService
@Service("userServiceV1")
public class UserServiceV1 implements UserService { }

@Service("userServiceV2")
public class UserServiceV2 implements UserService { }

// Specify which one you want:
@RestController
public class UserController {
    private final UserService userService;
    
    public UserController(@Qualifier("userServiceV2") UserService userService) {
        this.userService = userService;
    }
}
```

### Key Annotations Explained

**@RestController vs @Controller:**

```java
@RestController  // = @Controller + @ResponseBody (every method returns data, not views)
public class DeckController {
    @GetMapping("/api/decks")
    public List<DeckDTO> getDecks() {
        return deckService.getUserDecks();  // Automatically converted to JSON
    }
}

@Controller  // Returns view names (HTML templates)
public class WebController {
    @GetMapping("/home")
    public String home() {
        return "home";  // Renders home.html template
    }
}
```

**@Service - Where Business Logic Lives:**

```java
@Service  // Marks this as a service layer component
@RequiredArgsConstructor
@Slf4j  // Lombok generates 'log' field
@Transactional(readOnly = true)  // Default: all methods are read-only transactions
public class DeckService {
    
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    
    // Read operation (uses class-level @Transactional)
    public List<DeckDTO> getUserDecks(Long userId) {
        log.info("Fetching decks for user: {}", userId);
        return deckRepository.findByOwnerId(userId)
                .stream()
                .map(DeckDTO::from)
                .toList();
    }
    
    // Write operation (overrides class-level to allow writes)
    @Transactional
    public DeckDTO createDeck(Long userId, CreateDeckRequest request) {
        log.info("Creating deck for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Deck deck = Deck.builder()
                .name(request.name())
                .description(request.description())
                .owner(user)
                .build();
        
        deck = deckRepository.save(deck);
        return DeckDTO.from(deck);
    }
}
```

**@Repository - Data Access Layer:**

```java
@Repository  // Marks this as a data access component
public interface DeckRepository extends JpaRepository<Deck, Long> {
    
    // Spring Data JPA generates implementation automatically
    List<Deck> findByOwnerId(Long userId);
    
    // Translates to SQL:
    // SELECT * FROM decks WHERE user_id = ?
}
```

**@Transactional - Database Transaction Boundaries:**

```java
@Transactional  // What this does:
public void someMethod() {
    // BEGIN TRANSACTION
    
    try {
        // Your code here
        repository.save(entity);
        
        // COMMIT (if no exception)
    } catch (Exception e) {
        // ROLLBACK (if exception thrown)
        throw e;
    }
}
```

**Why @Transactional matters:**

```java
// ❌ Without @Transactional - data inconsistency risk
public void transferCards(Long fromDeckId, Long toDeckId, List<Long> cardIds) {
    cardRepository.updateDeck(cardIds, toDeckId);  // Succeeds
    deckRepository.updateCardCount(fromDeckId, -cardIds.size());  // FAILS
    
    // Cards moved, but deck counts wrong! Data inconsistent.
}

// ✅ With @Transactional - all-or-nothing
@Transactional
public void transferCards(Long fromDeckId, Long toDeckId, List<Long> cardIds) {
    cardRepository.updateDeck(cardIds, toDeckId);
    deckRepository.updateCardCount(fromDeckId, -cardIds.size());
    
    // If second call fails, BOTH are rolled back. Data stays consistent.
}
```

**readOnly optimization:**

```java
@Transactional(readOnly = true)  // Tells database "this won't modify data"
public List<DeckDTO> getUserDecks(Long userId) {
    // Database can:
    // - Skip locking rows
    // - Use read replicas
    // - Optimize query plans
    return deckRepository.findByOwnerId(userId)...;
}
```

## 9. Database Layer (JPA + Hibernate)

### Entity Design

An entity is a Java class that maps to a database table:

```java
@Entity  // Marks this class as a JPA entity
@Table(name = "cards")  // Table name (optional, defaults to class name)
@Getter  // Lombok: generates getters
@Setter  // Lombok: generates setters
@NoArgsConstructor  // Lombok: generates no-arg constructor (JPA requires it)
@AllArgsConstructor  // Lombok: generates all-args constructor
@Builder  // Lombok: generates builder pattern
public class Card {

    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @ManyToOne(fetch = FetchType.LAZY)  // Many cards → one deck
    @JoinColumn(name = "deck_id", nullable = false)  // Foreign key column
    private Deck deck;

    @Builder.Default  // Use this value when building with @Builder
    private Integer repetitions = 0;

    @Builder.Default
    private Double easeFactor = 2.5;

    @Builder.Default
    private Integer interval = 1;

    @Builder.Default
    private LocalDateTime nextReviewDate = LocalDateTime.now();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate  // Called before UPDATE queries
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**This generates the SQL:**

```sql
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    deck_id BIGINT NOT NULL REFERENCES decks(id),
    repetitions INTEGER DEFAULT 0,
    ease_factor DOUBLE PRECISION DEFAULT 2.5,
    interval INTEGER DEFAULT 1,
    next_review_date TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### Annotation Deep Dive

**@GeneratedValue Strategies:**

```java
// IDENTITY (what we use) - Database auto-increment
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

// PostgreSQL: Uses SERIAL (sequence-based auto-increment)
// Insert: INSERT INTO cards (front, back) VALUES (?, ?)
// Database generates ID automatically

// Alternative: SEQUENCE (more control)
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_seq")
@SequenceGenerator(name = "card_seq", sequenceName = "card_sequence")
private Long id;

// Allows pre-allocation (better for batch inserts)
```

**@Column Options:**

```java
@Column(
    nullable = false,        // NOT NULL constraint
    unique = true,           // UNIQUE constraint
    length = 255,            // VARCHAR(255) - for String
    columnDefinition = "TEXT" // Override default type
)
private String email;

// Without @Column, Hibernate uses defaults:
// - Column name = field name (camelCase → snake_case)
// - Type = mapped from Java type (String → VARCHAR(255), Integer → INTEGER)
// - Nullable = true
```

**@ManyToOne Explained:**

```
Deck ──1:N──→ Card

One deck has many cards
Each card belongs to one deck
```

```java
@Entity
public class Card {
    @ManyToOne(fetch = FetchType.LAZY)  // Don't load deck unless explicitly accessed
    @JoinColumn(name = "deck_id")       // Column name in cards table
    private Deck deck;
}

// Generated SQL:
// ALTER TABLE cards ADD CONSTRAINT fk_deck 
//     FOREIGN KEY (deck_id) REFERENCES decks(id);
```

**FetchType.LAZY vs EAGER:**

```java
// LAZY (recommended) - Don't load related entity until accessed
@ManyToOne(fetch = FetchType.LAZY)
private Deck deck;

Card card = cardRepository.findById(1L).get();
// SELECT * FROM cards WHERE id = 1
// (deck not loaded yet)

String deckName = card.getDeck().getName();
// Now triggers: SELECT * FROM decks WHERE id = ?
// (loads deck on-demand)

// EAGER (can cause performance issues) - Always load related entity
@ManyToOne(fetch = FetchType.EAGER)
private Deck deck;

Card card = cardRepository.findById(1L).get();
// SELECT c.*, d.* FROM cards c LEFT JOIN decks d ON c.deck_id = d.id WHERE c.id = 1
// (loads both card AND deck in one query)

// Problem: If you fetch 100 cards, you load 100 decks even if you don't need them
```

**Why We Avoided @OneToMany:**

```java
// We COULD have added this to Deck:
@Entity
public class Deck {
    @OneToMany(mappedBy = "deck")
    private List<Card> cards;
}

// Then you could do:
Deck deck = deckRepository.findById(1L).get();
List<Card> cards = deck.getCards();  // Loads all cards

// But this creates problems:
// 1. N+1 query problem (load deck, then N queries for cards)
// 2. Accidentally load all cards when you just wanted the deck
// 3. Harder to understand what queries are running

// ✅ Instead, we query explicitly:
List<Card> cards = cardRepository.findByDeckId(1L);
// Clear what's happening: one query, get cards for deck
```

### Spring Data JPA Magic

Spring Data JPA generates repository implementations from method names:

```java
public interface CardRepository extends JpaRepository<Card, Long> {
    
    // Method name → SQL query
    List<Card> findByDeckId(Long deckId);
    // SELECT * FROM cards WHERE deck_id = ?
    
    long countByDeckId(Long deckId);
    // SELECT COUNT(*) FROM cards WHERE deck_id = ?
    
    void deleteByDeckId(Long deckId);
    // DELETE FROM cards WHERE deck_id = ?
    
    List<Card> findByDeckIdAndNextReviewDateBeforeOrderByNextReviewDate(
        Long deckId,
        LocalDateTime now
    );
    // SELECT * FROM cards 
    // WHERE deck_id = ? AND next_review_date < ? 
    // ORDER BY next_review_date ASC
}
```

**How Spring Parses Method Names:**

```
findByDeckIdAndNextReviewDateBeforeOrderByNextReviewDate
│    │ │     │  │                │     │                │
│    │ │     │  │                │     │                └─ Property to order by
│    │ │     │  │                │     └─ Keyword: ORDER BY
│    │ │     │  │                └─ Keyword: BEFORE (less than)
│    │ │     │  └─ Property: nextReviewDate
│    │ │     └─ Keyword: AND
│    │ └─ Property: deckId
│    └─ Keyword: BY
└─ Operation: find

Keywords:
- find / get / query / read → SELECT
- count → COUNT
- delete / remove → DELETE
- By → WHERE clause starts
- And / Or → SQL AND / OR
- Before / After → < / >
- LessThan / GreaterThan → < / >
- Between → BETWEEN
- Like → LIKE
- OrderBy → ORDER BY
```

**When Method Names Get Too Long:**

```java
// ❌ Unreadable
List<Card> findByDeckIdAndNextReviewDateBeforeAndRepetitionsGreaterThanAndEaseFactorLessThanOrderByNextReviewDateAsc(...);

// ✅ Use @Query for complex queries
@Query("SELECT c FROM Card c WHERE c.deck.id = :deckId AND c.nextReviewDate < :now AND c.repetitions > :reps ORDER BY c.nextReviewDate")
List<Card> findDueCardsWithFilters(@Param("deckId") Long deckId, @Param("now") LocalDateTime now, @Param("reps") int reps);
```

### Hibernate ORM

Hibernate sits between your Java code and the database, translating objects ↔ SQL:

```
Java Code:                      SQL:
─────────────────────────────  ──────────────────────────────
cardRepository.save(card)   →  INSERT INTO cards (...) VALUES (...)
                               or UPDATE cards SET ... WHERE id = ?

cardRepository.findById(1L) →  SELECT * FROM cards WHERE id = 1

cardRepository.deleteById(1L)→ DELETE FROM cards WHERE id = 1
```

**Session and Persistence Context:**

```java
@Transactional
public void updateCard(Long cardId, String newFront) {
    Card card = cardRepository.findById(cardId).get();
    // Hibernate loads card into "session" (in-memory cache)
    
    card.setFront(newFront);
    // Card is "dirty" (modified but not yet saved to DB)
    
    // No cardRepository.save() needed!
    // When @Transactional ends, Hibernate flushes changes to DB automatically
}

// This feature is called "dirty checking"
```

**Why ddl-auto: update (and why it's not for production):**

```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Hibernate auto-generates schema changes
```

**What this does:**

```
On application startup:
1. Hibernate reads all @Entity classes
2. Generates expected database schema
3. Compares with actual database schema
4. Executes ALTER TABLE statements to match

Example:
- You add 'interval' field to Card.java
- Hibernate detects difference
- Runs: ALTER TABLE cards ADD COLUMN interval INTEGER
```

**Why it's great for development:**

- ✅ No manual SQL scripts
- ✅ Database always matches code
- ✅ Iterate fast

**Why it's terrible for production:**

- ❌ Can't review changes before execution
- ❌ No rollback plan
- ❌ May drop data unintentionally
- ❌ No version control of schema

**Production alternative: Flyway/Liquibase**

```sql
-- V1__create_cards_table.sql
CREATE TABLE cards (...);

-- V2__add_interval_column.sql
ALTER TABLE cards ADD COLUMN interval INTEGER DEFAULT 1;
```

- Version-controlled SQL files
- Incremental migrations
- Rollback support
- Clear audit trail

## 10. Service Layer Architecture

### Why Services Exist

```
Without Service Layer (Controller does everything):
┌──────────────┐
│  Controller  │
│ - Routing    │
│ - Validation │
│ - DB access  │  ← Too many responsibilities!
│ - Txn mgmt   │
│ - Logging    │
│ - Error hdl  │
└──────────────┘

With Service Layer (Separation of Concerns):
┌──────────────┐
│  Controller  │ ← Routing only (thin layer)
└──────┬───────┘
       │
┌──────▼───────┐
│   Service    │ ← Business logic (thick layer)
│ - Validation │
│ - Txn mgmt   │
│ - Logging    │
│ - Error hdl  │
└──────┬───────┘
       │
┌──────▼───────┐
│  Repository  │ ← Data access only
└──────────────┘
```

**Benefits:**

1. **Single Responsibility Principle**
   - Controller: HTTP concerns (routing, status codes)
   - Service: Business logic (validation, algorithms)
   - Repository: Data access (queries)

2. **Reusability**
   ```java
   // Service can be called from:
   - REST Controller
   - GraphQL Resolver
   - Scheduled Jobs
   - Message Listeners
   
   // If logic was in controller, you'd have to duplicate it
   ```

3. **Testability**
   ```java
   // Easy to test service in isolation
   @Test
   void testCreateDeck() {
       DeckService service = new DeckService(mockRepo, mockUserRepo);
       DeckDTO result = service.createDeck(1L, request);
       assertEquals("Expected", result.name());
   }
   
   // No need to mock HTTP requests, response objects, etc.
   ```

4. **Transaction Boundaries**
   ```java
   @Service
   @Transactional(readOnly = true)  // Declare transactions at service level
   public class DeckService {
       @Transactional
       public void complexOperation() {
           // Multiple repository calls in ONE transaction
       }
   }
   ```

### Transaction Management Deep Dive

**What is a Database Transaction?**

A transaction is a unit of work that's atomic (all-or-nothing):

```sql
BEGIN TRANSACTION;

INSERT INTO decks (name, user_id) VALUES ('Spanish', 1);
INSERT INTO cards (front, back, deck_id) VALUES ('hola', 'hello', 1);
INSERT INTO cards (front, back, deck_id) VALUES ('adiós', 'goodbye', 1);

COMMIT;  -- All three succeed

-- OR --

ROLLBACK;  -- None of them succeed
```

**ACID Properties:**

- **Atomicity:** All operations succeed or all fail
- **Consistency:** Database constraints are always satisfied
- **Isolation:** Concurrent transactions don't interfere
- **Durability:** Committed data survives crashes

**How @Transactional Works:**

```java
@Transactional
public DeckDTO createDeck(Long userId, CreateDeckRequest request) {
    // Spring creates a proxy that wraps this method:
    
    // BEGIN TRANSACTION
    try {
        User user = userRepository.findById(userId)...  // SQL SELECT
        Deck deck = deckRepository.save(Deck.builder()...);  // SQL INSERT
        
        return DeckDTO.from(deck);
        
        // COMMIT (if we reach here)
    } catch (Exception e) {
        // ROLLBACK (if exception thrown)
        throw e;
    }
}
```

**Class-Level vs Method-Level:**

```java
@Service
@Transactional(readOnly = true)  // ← Default for all methods
public class CardService {
    
    // Inherits readOnly = true
    public List<CardDTO> getDeckCards(Long deckId) {
        return cardRepository.findByDeckId(deckId)...;
    }
    
    // Overrides to readOnly = false
    @Transactional  // ← Write transaction
    public CardDTO createCard(Long deckId, CreateCardRequest request) {
        // Can modify database
    }
}
```

**Rollback Behavior:**

```java
@Transactional
public void transferCards(Long sourceDeckId, Long targetDeckId, List<Long> cardIds) {
    for (Long cardId : cardIds) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        card.setDeck(deckRepository.findById(targetDeckId).orElseThrow());
        cardRepository.save(card);
        
        if (someCondition) {
            throw new RuntimeException("Transfer failed!");
            // ALL previous saves in this transaction are rolled back
        }
    }
    // If we reach here, ALL saves are committed together
}
```

**Transaction Propagation (Advanced):**

```java
@Transactional
public void methodA() {
    // Transaction started
    
    methodB();  // Uses same transaction
    
    // Transaction committed
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void methodB() {
    // New transaction started (separate from methodA)
    
    // Committed independently
}
```

### Validation Pattern

We chose manual validation over annotations for clarity:

```java
// ✅ Explicit (tutorial-friendly)
@Transactional
public CardDTO createCard(Long deckId, CreateCardRequest request) {
    // You can SEE the validation logic
    if (request.front() == null || request.front().isBlank()) {
        throw new IllegalArgumentException("Front text is required");
    }
    if (request.back() == null || request.back().isBlank()) {
        throw new IllegalArgumentException("Back text is required");
    }
    
    // Continue...
}

// ❌ Annotation-based (more magic)
public record CreateCardRequest(
    @NotBlank(message = "Front text is required")
    String front,
    
    @NotBlank(message = "Back text is required")
    String back
) {}

@Transactional
public CardDTO createCard(@Valid CreateCardRequest request) {
    // Where does validation happen? (Hidden by framework)
}
```

**When to use annotation validation:**

- Large enterprise apps (consistency across many endpoints)
- Complex validation rules (cross-field validation)
- You want declarative, DRY validation

**When to use manual validation:**

- Learning projects (see how validation works)
- Simple rules (null/blank checks)
- You want full control over error messages

### Service Implementation Pattern

```java
@Service
@RequiredArgsConstructor  // Lombok: constructor injection
@Slf4j  // Lombok: creates 'log' field
@Transactional(readOnly = true)  // Default: read-only
public class StudyService {

    // Dependencies (injected via constructor)
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    // Read method (uses class-level readOnly = true)
    public StudySessionDTO startStudySession(Long deckId) {
        log.info("Starting study session for deck: {}", deckId);
        
        validateDeckExists(deckId);
        
        List<Card> dueCards = cardRepository
            .findByDeckIdAndNextReviewDateBeforeOrderByNextReviewDate(
                deckId, 
                LocalDateTime.now()
            );
        
        int newCount = countNewCards(dueCards);
        int reviewCount = dueCards.size() - newCount;
        
        List<CardDTO> cardDTOs = dueCards.stream()
                .map(CardDTO::from)
                .toList();
        
        return new StudySessionDTO(deckId, cardDTOs, dueCards.size(), newCount, reviewCount);
    }

    // Write method (overrides to readOnly = false)
    @Transactional
    public ReviewResponse reviewCard(Long cardId, ReviewRequest request) {
        log.info("Reviewing card: {} with quality: {}", cardId, request.quality());
        
        validateQuality(request.quality());
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        applySpacedRepetition(card, request.quality());
        card = cardRepository.save(card);
        
        log.info("Card reviewed: {}, next review: {}", cardId, card.getNextReviewDate());
        
        return new ReviewResponse(
                CardDTO.from(card),
                card.getNextReviewDate(),
                card.getInterval(),
                false
        );
    }

    // Private helper methods (not exposed as public API)
    private void validateDeckExists(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new RuntimeException("Deck not found");
        }
    }

    private int countNewCards(List<Card> cards) {
        return (int) cards.stream()
                .filter(card -> card.getRepetitions() == 0)
                .count();
    }

    private void validateQuality(Integer quality) {
        if (quality == null || quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }
    }

    private void applySpacedRepetition(Card card, int quality) {
        boolean failed = quality < 3;
        
        if (failed) {
            resetCard(card);
        } else {
            advanceCard(card);
        }
        
        updateEaseFactor(card, quality);
        scheduleNextReview(card);
    }

    // More helper methods...
}
```

**Key Patterns:**

1. **Logging:** Use SLF4J for structured logging
2. **Validation:** Early return/throw on invalid input
3. **Extraction:** Break complex logic into private methods
4. **Naming:** Methods should read like sentences


## 11. Controller Layer (REST API)

### RESTful Design Principles

REST (Representational State Transfer) is a convention for designing web APIs:

```
Resource: Deck
Base URL: /api/decks

HTTP Method  | URL                  | Action              | Request Body       | Response
─────────────|──────────────────────|─────────────────────|────────────────────|──────────────
GET          | /api/decks           | List all decks      | None               | List<DeckDTO>
GET          | /api/decks/{id}      | Get one deck        | None               | DeckDTO
POST         | /api/decks           | Create deck         | CreateDeckRequest  | DeckDTO
PUT          | /api/decks/{id}      | Update deck         | UpdateDeckRequest  | DeckDTO
DELETE       | /api/decks/{id}      | Delete deck         | None               | 204 No Content
```

**Why REST?**

- ✅ Standard convention (everyone understands it)
- ✅ URLs represent resources (nouns, not verbs)
- ✅ HTTP methods have meaning (GET = read, POST = create, etc.)
- ✅ Stateless (each request has all info needed)

**RESTful URL Design:**

```
✅ Good REST URLs (resources/nouns):
GET    /api/decks
POST   /api/decks/{deckId}/cards
GET    /api/decks/{deckId}/cards/{cardId}

❌ Bad URLs (actions/verbs):
GET    /api/getDeck?id=1
POST   /api/createCard
GET    /api/fetchCardById
```

### Controller Implementation

```java
@RestController  // = @Controller + @ResponseBody (returns data, not views)
@RequestMapping("/api/decks")  // Base path for all methods
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    // GET /api/decks
    @GetMapping
    public ResponseEntity<List<DeckDTO>> getUserDecks(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(deckService.getUserDecks(userId));
    }

    // GET /api/decks/123
    @GetMapping("/{id}")
    public ResponseEntity<DeckDTO> getDeck(@PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeck(id));
    }

    // POST /api/decks
    @PostMapping
    public ResponseEntity<DeckDTO> createDeck(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateDeckRequest request) {
        return ResponseEntity.ok(deckService.createDeck(userId, request));
    }

    // PUT /api/decks/123
    @PutMapping("/{id}")
    public ResponseEntity<DeckDTO> updateDeck(
            @PathVariable Long id,
            @RequestBody UpdateDeckRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(id, request));
    }

    // DELETE /api/decks/123
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();  // 204 status
    }
}
```

### Annotation Breakdown

**@PathVariable - URL Segments:**

```java
@GetMapping("/decks/{deckId}/cards/{cardId}")
public CardDTO getCard(
    @PathVariable Long deckId,    // Matches {deckId} in URL
    @PathVariable Long cardId     // Matches {cardId} in URL
) {
    // GET /decks/5/cards/10
    // deckId = 5, cardId = 10
}

// Can rename if needed:
@GetMapping("/decks/{id}")
public DeckDTO getDeck(@PathVariable("id") Long deckId) {
    // Parameter name doesn't have to match
}
```

**@RequestBody - JSON Payload:**

```java
@PostMapping("/decks")
public DeckDTO createDeck(@RequestBody CreateDeckRequest request) {
    // Receives JSON like:
    // {
    //   "name": "Spanish Vocabulary",
    //   "description": "Common Spanish words"
    // }
    
    // Spring automatically deserializes JSON → CreateDeckRequest object
    // request.name() = "Spanish Vocabulary"
    // request.description() = "Common Spanish words"
}
```

**@RequestHeader - HTTP Headers:**

```java
@GetMapping("/decks")
public List<DeckDTO> getDecks(@RequestHeader("X-User-Id") Long userId) {
    // Reads from HTTP header:
    // GET /api/decks
    // X-User-Id: 123
    
    // userId = 123
}

// Optional headers:
@GetMapping("/decks")
public List<DeckDTO> getDecks(
    @RequestHeader(value = "X-User-Id", required = false) Long userId
) {
    // userId will be null if header not present
}
```

**@RequestParam - Query Parameters:**

```java
@GetMapping("/search")
public List<DeckDTO> searchDecks(
    @RequestParam String query,
    @RequestParam(defaultValue = "10") int limit
) {
    // GET /api/search?query=spanish&limit=20
    // query = "spanish"
    // limit = 20
    
    // GET /api/search?query=spanish
    // query = "spanish"
    // limit = 10 (default)
}
```

### Response Patterns

**ResponseEntity - Control HTTP Response:**

```java
// 200 OK with body
return ResponseEntity.ok(deckDTO);
// HTTP/1.1 200 OK
// Content-Type: application/json
// { "id": 1, "name": "..." }

// 201 Created with Location header
return ResponseEntity
    .created(URI.create("/api/decks/" + deck.getId()))
    .body(deckDTO);
// HTTP/1.1 201 Created
// Location: /api/decks/5

// 204 No Content
return ResponseEntity.noContent().build();
// HTTP/1.1 204 No Content
// (empty body)

// 404 Not Found
return ResponseEntity.notFound().build();

// Custom status
return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
```

**Why Use ResponseEntity?**

```java
// ✅ With ResponseEntity - full control
@GetMapping("/{id}")
public ResponseEntity<DeckDTO> getDeck(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(deckService.getDeck(id));
    } catch (NotFoundException e) {
        return ResponseEntity.notFound().build();  // 404
    }
}

// ❌ Without ResponseEntity - always 200 or 500
@GetMapping("/{id}")
public DeckDTO getDeck(@PathVariable Long id) {
    return deckService.getDeck(id);  // Always 200 if no exception
}
```

### Why DTOs (Data Transfer Objects)

**Problem: Exposing Entities Directly**

```java
// ❌ Bad - Controller returns Entity
@GetMapping("/decks/{id}")
public Deck getDeck(@PathVariable Long id) {
    return deckRepository.findById(id).get();
}

// Issues:
// 1. JSON includes internal fields (password hashes, audit fields)
// 2. Lazy-loaded relationships cause errors or N+1 queries
// 3. API tied to database schema (can't change DB without breaking API)
// 4. Circular references (Deck → User → Decks → Users...)
```

**Solution: DTOs**

```java
// ✅ Good - Controller returns DTO
@GetMapping("/decks/{id}")
public ResponseEntity<DeckDTO> getDeck(@PathVariable Long id) {
    Deck deck = deckRepository.findById(id).orElseThrow();
    return ResponseEntity.ok(DeckDTO.from(deck));  // Entity → DTO
}

// DTO only includes what API consumers need
public record DeckDTO(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt
) {
    public static DeckDTO from(Deck deck) {
        return new DeckDTO(
            deck.getId(),
            deck.getName(),
            deck.getDescription(),
            deck.getCreatedAt()
        );
    }
}

// Benefits:
// ✅ API contract independent of database
// ✅ No sensitive data leakage
// ✅ No lazy-load issues
// ✅ Can add computed fields
```

**Java Records for DTOs:**

```java
// Modern Java (Java 14+)
public record DeckDTO(Long id, String name, String description) {}

// Equivalent to:
public class DeckDTO {
    private final Long id;
    private final String name;
    private final String description;
    
    public DeckDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    public Long id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    
    @Override
    public boolean equals(Object obj) { ... }
    @Override
    public int hashCode() { ... }
    @Override
    public String toString() { ... }
}

// Records are perfect for DTOs:
// - Immutable by default
// - Concise syntax
// - Auto-generated equals/hashCode/toString
```

## 12. Security Configuration

### Current Setup (Simplified for Weekend Project)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Hash passwords
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF (stateless API)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // No sessions
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // Allow all requests (for now)
            );

        return http.build();
    }
}
```

**What This Does:**

1. **Disables CSRF Protection**
   ```java
   .csrf(AbstractHttpConfigurer::disable)
   
   // CSRF (Cross-Site Request Forgery) protects session-based auth
   // We use JWT (stateless), so CSRF doesn't apply
   ```

2. **Stateless Sessions**
   ```java
   .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
   
   // Don't create HTTP sessions
   // Each request must have auth token (JWT)
   ```

3. **Permits All Requests**
   ```java
   .anyRequest().permitAll()
   
   // No authentication required (simplified for tutorial)
   ```

### Password Hashing with BCrypt

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Usage in AuthService:
String hashedPassword = passwordEncoder.encode("user's password");
// Stores: $2a$10$N9qo8uLOickgx2ZMRZoMye...

// Later, during login:
boolean matches = passwordEncoder.matches("user's password", hashedPassword);
// Uses same algorithm + salt to verify
```

**Why BCrypt?**

- ✅ Designed for passwords (slow by design, prevents brute-force)
- ✅ Auto-generates salt (each hash is unique)
- ✅ Configurable rounds (can increase as CPUs get faster)

**vs MD5/SHA (DON'T USE FOR PASSWORDS):**

```java
// ❌ MD5/SHA - fast, no salt, easily cracked
String hash = MessageDigest.getInstance("MD5").digest("password");

// ✅ BCrypt - slow, salted, secure
String hash = BCryptPasswordEncoder.encode("password");
```

### JWT Utility (Token Generation)

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())  // User ID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

**JWT Structure:**

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjMiLCJpYXQiOjE2OTk5OTk5OTksImV4cCI6MTcwMDYwNDc5OX0.signature
│        Header        │        Payload (user ID, dates)        │  Signature  │

Header (Base64): {"alg":"HS512"}
Payload (Base64): {"sub":"123","iat":1699999999,"exp":1700604799}
Signature: HMACSHA512(header + payload, secret)
```

**Why JWT?**

- ✅ Stateless (server doesn't store sessions)
- ✅ Scalable (no shared session store needed)
- ✅ Self-contained (all info in token)

**Production Path (Not Implemented):**

```java
// JWT Authentication Filter (runs on every request)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String token = extractToken(request);
        
        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // Set authentication in Spring Security context
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userId, null, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}

// Then change security config:
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()  // Login/register open
    .anyRequest().authenticated()  // Everything else requires JWT
)
```

### CORS Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Apply to all API routes
                .allowedOrigins("http://localhost:3000")  // Frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
```

**Why CORS Exists:**

```
Same-Origin Policy (Browser Security):

http://localhost:3000 (frontend)
       ↓ fetch()
http://localhost:8080/api/decks (backend)

Problem: Different ports = different origins
Browser blocks the request by default
```

**CORS Headers Fix It:**

```
Browser: "Can I call localhost:8080 from localhost:3000?"
         Sends OPTIONS /api/decks (preflight request)

Server:  Access-Control-Allow-Origin: http://localhost:3000
         Access-Control-Allow-Methods: GET, POST, PUT, DELETE
         
Browser: "OK, allowed!" Sends actual GET /api/decks
```

**Production CORS:**

```java
// Environment-specific origins
@Value("${cors.allowed-origins}")
private String allowedOrigins;

registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins.split(","))
        // Development: http://localhost:3000
        // Production: https://myapp.com
```

## 13. Spaced Repetition Algorithm (SM-2)

### The Algorithm Explained

SM-2 (SuperMemo 2) is a scientifically-validated algorithm for optimal memory retention:

```
Basic Principle:
- Show cards right before you forget them
- If you remember easily → longer interval
- If you struggle → shorter interval
- Interval grows exponentially for well-known items
```

**Quality Ratings (0-5):**

```
0 - Complete blackout (no recall)
1 - Incorrect, but recognized upon seeing answer
2 - Incorrect, but seemed familiar
3 - Correct, but with difficulty
4 - Correct, with hesitation
5 - Perfect recall
```

**SM-2 State Variables:**

```java
@Entity
public class Card {
    private Integer repetitions = 0;      // How many times reviewed successfully
    private Double easeFactor = 2.5;      // Difficulty multiplier (1.3 to 2.5+)
    private Integer interval = 1;         // Days until next review
    private LocalDateTime nextReviewDate; // When to show again
}
```

**Algorithm Flow:**

```
New Card (repetitions = 0):
│
├─ Quality < 3 (incorrect)
│  └─→ repetitions = 0, interval = 1
│      Show again tomorrow
│
└─ Quality ≥ 3 (correct)
   └─→ repetitions = 1, interval = 1
       Show again tomorrow
       
First Review (repetitions = 1):
│
├─ Quality < 3
│  └─→ Reset to new card (repetitions = 0)
│
└─ Quality ≥ 3
   └─→ repetitions = 2, interval = 6
       Show again in 6 days
       
Subsequent Reviews (repetitions ≥ 2):
│
├─ Quality < 3
│  └─→ Reset to new card
│
└─ Quality ≥ 3
   └─→ repetitions++
       interval = interval * easeFactor
       
       Example:
       Day 0:  New card
       Day 1:  First review (interval = 1)
       Day 7:  Second review (interval = 6)
       Day 22: Third review (interval = 6 * 2.5 = 15)
       Day 59: Fourth review (interval = 15 * 2.5 = 37.5 ≈ 37)
       ...exponential growth...
```

**Ease Factor Adjustment:**

```
After each review, adjust difficulty based on performance:

easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))

Examples:
Quality 5 (perfect): EF + 0.1 = easier next time (longer intervals)
Quality 4 (good):    EF + 0.0 = no change
Quality 3 (hard):    EF - 0.14 = harder next time (shorter intervals)
Quality < 3:         Reset card

Minimum EF = 1.3 (prevents intervals from becoming too short)
```

### Implementation

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudyService {

    private final CardRepository cardRepository;

    @Transactional
    public ReviewResponse reviewCard(Long cardId, ReviewRequest request) {
        validateQuality(request.quality());
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        applySpacedRepetition(card, request.quality());
        card = cardRepository.save(card);
        
        return new ReviewResponse(
                CardDTO.from(card),
                card.getNextReviewDate(),
                card.getInterval(),
                false
        );
    }

    private void applySpacedRepetition(Card card, int quality) {
        boolean failed = quality < 3;
        
        if (failed) {
            resetCard(card);
        } else {
            advanceCard(card);
        }
        
        updateEaseFactor(card, quality);
        scheduleNextReview(card);
    }

    private void resetCard(Card card) {
        card.setRepetitions(0);
        card.setInterval(1);
    }

    private void advanceCard(Card card) {
        int newInterval = calculateInterval(card);
        card.setInterval(newInterval);
        card.setRepetitions(card.getRepetitions() + 1);
    }

    private int calculateInterval(Card card) {
        int reps = card.getRepetitions();
        
        if (reps == 0) return 1;          // First time: 1 day
        if (reps == 1) return 6;          // Second time: 6 days
        
        // Third time onwards: exponential
        return (int) Math.round(card.getInterval() * card.getEaseFactor());
    }

    private void updateEaseFactor(Card card, int quality) {
        double adjustment = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
        double newEaseFactor = card.getEaseFactor() + adjustment;
        card.setEaseFactor(Math.max(1.3, newEaseFactor));  // Floor at 1.3
    }

    private void scheduleNextReview(Card card) {
        LocalDateTime nextReview = LocalDateTime.now().plusDays(card.getInterval());
        card.setNextReviewDate(nextReview);
    }
}
```

**Why This Organization?**

- ✅ `applySpacedRepetition()` - High-level orchestration (easy to understand flow)
- ✅ `resetCard()`, `advanceCard()` - State changes (clear what happens)
- ✅ `calculateInterval()` - Algorithm logic (SM-2 math isolated)
- ✅ `updateEaseFactor()` - Difficulty adjustment (formula in one place)
- ✅ Each method has one job (testable, maintainable)

**Alternative Approaches:**

```java
// ❌ All logic in one method (hard to test, hard to understand)
private void applySpacedRepetition(Card card, int quality) {
    if (quality < 3) {
        card.setRepetitions(0);
        card.setInterval(1);
    } else {
        if (card.getRepetitions() == 0) {
            card.setInterval(1);
        } else if (card.getRepetitions() == 1) {
            card.setInterval(6);
        } else {
            card.setInterval((int) Math.round(card.getInterval() * card.getEaseFactor()));
        }
        card.setRepetitions(card.getRepetitions() + 1);
    }
    double newEF = card.getEaseFactor() + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
    card.setEaseFactor(Math.max(1.3, newEF));
    card.setNextReviewDate(LocalDateTime.now().plusDays(card.getInterval()));
}
```

**Trade-offs:**

| Current (Extracted Methods) | Alternative (One Big Method) |
|----------------------------|------------------------------|
| More lines of code | Fewer lines |
| Easy to test individual pieces | Must test entire flow |
| Clear what each step does | Need comments to explain |
| Easy to modify one part | Changes affect everything |

For learning projects, **clarity > conciseness**.

---

*Due to length, continuing in next section...*

---

## PART IV: FRONTEND DEEP DIVE

### Next.js 14 App Router

Next.js 14 introduced the App Router, a fundamental shift from the Pages Router. Understanding this is crucial:

**Pages Router (Old Way)**:
```
pages/
  index.tsx        → /
  about.tsx        → /about
  posts/[id].tsx   → /posts/123
```

**App Router (Our Way)**:
```
app/
  page.tsx         → /
  about/page.tsx   → /about
  posts/[id]/page.tsx → /posts/123
```

**Why App Router?**
1. **Server Components by Default**: Better performance, smaller bundle sizes
2. **Layouts**: Share UI across routes without re-rendering
3. **Streaming**: Progressive page rendering
4. **Better Data Fetching**: Integrated with React Suspense

**File Conventions**:
```
app/
  layout.tsx       - UI shared across routes (wraps page.tsx)
  page.tsx         - Unique UI for a route, makes route publicly accessible
  loading.tsx      - Loading UI with Suspense
  error.tsx        - Error UI boundary
  not-found.tsx    - 404 UI
```

### Server vs Client Components

This is the biggest mental shift in Next.js 14:

**Server Components (Default)**:
- Run on the server during build or request
- Can directly access backend resources (databases, file system)
- Cannot use browser APIs (localStorage, window, etc.)
- Cannot use React hooks (useState, useEffect, etc.)
- Zero JavaScript sent to client for these components

**Client Components**:
- Run in the browser
- Can use hooks, event listeners, browser APIs
- Must opt-in with `'use client'` directive at top of file

**Example from our Login Page** (`/frontend/app/login/page.tsx`):
```typescript
'use client';  // ← This makes it a client component

import { useState } from 'react';  // ← Can now use hooks

export default function LoginPage() {
  const [username, setUsername] = useState('');  // ← useState works
  const [password, setPassword] = useState('');
  
  const handleSubmit = async (e: React.FormEvent) => {  // ← Event handlers work
    e.preventDefault();
    // ...
  };
  
  return (
    <form onSubmit={handleSubmit}>  {/* ← Can attach event listeners */}
      {/* ... */}
    </form>
  );
}
```

**When to use each?**
- **Server Component**: Static content, data fetching, SEO-critical pages
- **Client Component**: Forms, interactive UI, state management, browser APIs

### TypeScript Type Safety Across Network Boundary

One of our strongest patterns is **mirroring backend DTOs in frontend types**. This catches errors at compile time instead of runtime.

**Backend DTO** (`/backend/src/main/java/com/peanuts/anki/card/dto/CardDTO.java`):
```java
public record CardDTO(
    Long id,
    String front,
    String back,
    Long deckId,
    Integer repetitions,
    Double easeFactor,
    Integer interval,
    LocalDateTime nextReviewDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**Frontend Type** (`/frontend/lib/types/card.ts`):
```typescript
export interface CardDTO {
  id: number;
  front: string;
  back: string;
  deckId: number;
  repetitions: number;
  easeFactor: number;
  interval: number;
  nextReviewDate: string;  // Note: Java LocalDateTime becomes ISO string
  createdAt: string;
  updatedAt: string;
}
```

**Type Mapping Table**:
```
Java Type          →  TypeScript Type
─────────────────────────────────────
Long, Integer      →  number
String             →  string
Boolean            →  boolean
LocalDateTime      →  string (ISO 8601)
Double             →  number
List<T>            →  T[]
```

**Why This Matters**:
```typescript
// Without types (dangerous):
const card = await fetch('/api/cards/1').then(r => r.json());
console.log(card.frnt);  // Typo! Runtime error or undefined

// With types (safe):
const card: CardDTO = await apiRequest('/api/cards/1');
console.log(card.frnt);  // ← TypeScript error: Property 'frnt' does not exist
console.log(card.front);  // ← Autocomplete works, compile-time safety
```

### API Client Architecture

Our API client (`/frontend/lib/api/client.ts`) handles cross-cutting concerns:

```typescript
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export async function apiRequest<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const userId = typeof window !== 'undefined' 
    ? localStorage.getItem('userId') 
    : null;

  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(userId && { 'X-User-Id': userId }),
      ...options?.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`API error: ${response.statusText}`);
  }

  // Handle empty responses (204 No Content, DELETE operations)
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return undefined as any;
  }

  return response.json();
}
```

**Key Patterns Explained**:

1. **Environment-Aware URL**:
   ```typescript
   const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
   ```
   - `NEXT_PUBLIC_` prefix makes env var available to browser
   - Fallback to localhost for local development
   - Can override in docker-compose.yml or .env.local

2. **SSR Safety Check**:
   ```typescript
   const userId = typeof window !== 'undefined' 
     ? localStorage.getItem('userId') 
     : null;
   ```
   - `window` doesn't exist during server-side rendering
   - Check prevents "window is not defined" errors
   - Returns null on server (fine, since user isn't logged in during SSR anyway)

3. **Empty Response Handling**:
   ```typescript
   if (response.status === 204 || response.headers.get('content-length') === '0') {
     return undefined as any;
   }
   ```
   - DELETE operations return 204 No Content (no body)
   - Calling `.json()` on empty response throws error
   - This pattern caught a real bug during development!

4. **Generic Type Parameter**:
   ```typescript
   export async function apiRequest<T>(...): Promise<T>
   ```
   - Caller specifies return type: `apiRequest<CardDTO>(...)`
   - TypeScript knows the return type without manual casting
   - Enables autocomplete and type checking

**Usage in Feature Modules**:

`/frontend/lib/api/cards.ts`:
```typescript
import { apiRequest } from './client';
import type { CardDTO, CreateCardRequest } from '../types/card';

export async function getDeckCards(deckId: number): Promise<CardDTO[]> {
  return apiRequest<CardDTO[]>(`/api/decks/${deckId}/cards`);
}

export async function createCard(
  deckId: number,
  request: CreateCardRequest
): Promise<CardDTO> {
  return apiRequest<CardDTO>(`/api/decks/${deckId}/cards`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function deleteCard(deckId: number, cardId: number): Promise<void> {
  return apiRequest<void>(`/api/decks/${deckId}/cards/${cardId}`, {
    method: 'DELETE',
  });
}
```

**Benefits of This Architecture**:
- **Single Source of Truth**: All API calls go through one function
- **Centralized Auth**: User ID header added automatically
- **Type Safety**: Generics enforce correct return types
- **Easy Testing**: Mock `apiRequest` to test all API calls
- **Environment Flexibility**: One env var changes all API calls

### State Management Pattern

We use **local state with useState** instead of global state (Redux, Zustand, etc.). Why?

**For a Weekend Project**:
- ✅ Simple, no boilerplate
- ✅ Easy to understand for learners
- ✅ Sufficient for small apps
- ✅ Built into React

**Example: Deck List Page** (`/frontend/app/decks/page.tsx`):
```typescript
'use client';

import { useEffect, useState } from 'react';
import { getUserDecks, createDeck, deleteDeck } from '@/lib/api/decks';
import type { DeckDTO } from '@/lib/types/deck';

export default function DecksPage() {
  const [decks, setDecks] = useState<DeckDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [newDeckName, setNewDeckName] = useState('');

  // Load decks on mount
  useEffect(() => {
    async function loadDecks() {
      try {
        const data = await getUserDecks();
        setDecks(data);
      } catch (error) {
        console.error('Failed to load decks:', error);
      } finally {
        setLoading(false);
      }
    }
    loadDecks();
  }, []);  // Empty array = run once on mount

  // Create new deck
  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDeckName.trim()) return;

    try {
      const newDeck = await createDeck({ name: newDeckName });
      setDecks([...decks, newDeck]);  // Optimistic update
      setNewDeckName('');
    } catch (error) {
      console.error('Failed to create deck:', error);
    }
  };

  // Delete deck
  const handleDelete = async (id: number) => {
    try {
      await deleteDeck(id);
      setDecks(decks.filter(d => d.id !== id));  // Optimistic update
    } catch (error) {
      console.error('Failed to delete deck:', error);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h1>My Decks</h1>
      
      {/* Create Form */}
      <form onSubmit={handleCreate}>
        <input
          type="text"
          value={newDeckName}
          onChange={(e) => setNewDeckName(e.target.value)}
          placeholder="New deck name"
        />
        <button type="submit">Create Deck</button>
      </form>

      {/* Deck List */}
      <ul>
        {decks.map(deck => (
          <li key={deck.id}>
            <a href={`/decks/${deck.id}`}>{deck.name}</a>
            <button onClick={() => handleDelete(deck.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
```

**State Management Patterns**:

1. **Loading States**:
   ```typescript
   const [loading, setLoading] = useState(true);
   // ...
   if (loading) return <div>Loading...</div>;
   ```
   - Always show loading indicator during async operations
   - Prevents "flash of empty content"

2. **Optimistic Updates**:
   ```typescript
   const handleDelete = async (id: number) => {
     await deleteDeck(id);
     setDecks(decks.filter(d => d.id !== id));  // UI updates immediately
   };
   ```
   - Update UI immediately, don't wait for server
   - Better UX (feels instant)
   - In production, add error handling to revert on failure

3. **Form State**:
   ```typescript
   const [newDeckName, setNewDeckName] = useState('');
   
   <input
     value={newDeckName}
     onChange={(e) => setNewDeckName(e.target.value)}
   />
   ```
   - Controlled component pattern
   - React state is source of truth
   - Enables validation, formatting, etc.

**When to Upgrade to Global State**:
- Sharing state across many unrelated components
- Avoiding prop drilling (passing props through 5+ levels)
- Complex state logic (use useReducer first)
- Persistence requirements (localStorage, IndexedDB)

### Dynamic Routing

Next.js App Router uses **folder-based dynamic routing** with `[param]` syntax.

**Our Route Structure**:
```
app/
  decks/
    page.tsx              → /decks (list all decks)
    [id]/
      page.tsx            → /decks/123 (deck detail)
      study/
        page.tsx          → /decks/123/study (study session)
```

**Deck Detail Page** (`/frontend/app/decks/[id]/page.tsx`):
```typescript
'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { getDeck } from '@/lib/api/decks';
import { getDeckCards, createCard, deleteCard } from '@/lib/api/cards';
import type { DeckDTO } from '@/lib/types/deck';
import type { CardDTO } from '@/lib/types/card';

export default function DeckDetailPage() {
  const params = useParams();
  const deckId = Number(params.id);  // Extract ID from URL
  
  const [deck, setDeck] = useState<DeckDTO | null>(null);
  const [cards, setCards] = useState<CardDTO[]>([]);
  
  useEffect(() => {
    async function loadData() {
      const [deckData, cardsData] = await Promise.all([
        getDeck(deckId),
        getDeckCards(deckId),
      ]);
      setDeck(deckData);
      setCards(cardsData);
    }
    loadData();
  }, [deckId]);  // Re-run if ID changes

  // ... rest of component
}
```

**Key Concepts**:

1. **`useParams()` Hook**:
   ```typescript
   const params = useParams();  // { id: "123" }
   const deckId = Number(params.id);  // Convert to number
   ```
   - Extracts route parameters from URL
   - Always returns strings (URL is text)
   - Convert to appropriate type (number, etc.)

2. **Parallel Data Fetching**:
   ```typescript
   const [deckData, cardsData] = await Promise.all([
     getDeck(deckId),
     getDeckCards(deckId),
   ]);
   ```
   - Fetch both resources simultaneously
   - Faster than sequential: ~100ms vs ~200ms
   - Use when requests are independent

3. **Type Safety with Dynamic Routes**:
   ```typescript
   // Type-safe navigation
   import { useRouter } from 'next/navigation';
   const router = useRouter();
   router.push(`/decks/${deckId}/study`);
   
   // Type-safe links
   <Link href={`/decks/${deck.id}`}>{deck.name}</Link>
   ```

### Study Session Implementation

The study page demonstrates **complex state management** and **real-time algorithm application**.

**Study Page** (`/frontend/app/decks/[id]/study/page.tsx`):
```typescript
'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { startStudySession, reviewCard } from '@/lib/api/study';
import type { StudySessionDTO, ReviewRequest } from '@/lib/types/study';

export default function StudyPage() {
  const params = useParams();
  const router = useRouter();
  const deckId = Number(params.id);

  const [session, setSession] = useState<StudySessionDTO | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [loading, setLoading] = useState(true);

  // Initialize study session
  useEffect(() => {
    async function initSession() {
      try {
        const data = await startStudySession(deckId);
        setSession(data);
        setLoading(false);
      } catch (error) {
        console.error('Failed to start session:', error);
      }
    }
    initSession();
  }, [deckId]);

  // Handle quality rating submission
  const handleReview = async (quality: number) => {
    if (!session || !session.cardsToReview[currentIndex]) return;

    const card = session.cardsToReview[currentIndex];
    const request: ReviewRequest = { quality };

    try {
      await reviewCard(card.id, request);
      
      // Move to next card or finish session
      if (currentIndex < session.cardsToReview.length - 1) {
        setCurrentIndex(currentIndex + 1);
        setShowAnswer(false);
      } else {
        router.push(`/decks/${deckId}`);  // Back to deck detail
      }
    } catch (error) {
      console.error('Failed to review card:', error);
    }
  };

  if (loading) return <div>Loading study session...</div>;
  if (!session || session.cardsToReview.length === 0) {
    return <div>No cards to review!</div>;
  }

  const currentCard = session.cardsToReview[currentIndex];
  const progress = `${currentIndex + 1} / ${session.cardsToReview.length}`;

  return (
    <div>
      <h1>Study Session</h1>
      <p>Progress: {progress}</p>

      {/* Card Display */}
      <div>
        <h2>Question</h2>
        <p>{currentCard.front}</p>
      </div>

      {showAnswer ? (
        <>
          <div>
            <h2>Answer</h2>
            <p>{currentCard.back}</p>
          </div>

          {/* Quality Ratings (SM-2 scale) */}
          <div>
            <h3>How well did you know this?</h3>
            <button onClick={() => handleReview(0)}>0 - Total Blackout</button>
            <button onClick={() => handleReview(1)}>1 - Incorrect, but familiar</button>
            <button onClick={() => handleReview(2)}>2 - Incorrect, but easy to recall</button>
            <button onClick={() => handleReview(3)}>3 - Correct, with difficulty</button>
            <button onClick={() => handleReview(4)}>4 - Correct, with hesitation</button>
            <button onClick={() => handleReview(5)}>5 - Perfect recall</button>
          </div>
        </>
      ) : (
        <button onClick={() => setShowAnswer(true)}>Show Answer</button>
      )}
    </div>
  );
}
```

**State Flow Diagram**:
```
┌─────────────┐
│  Loading    │ ← useEffect fetches session
└──────┬──────┘
       │
       v
┌─────────────┐
│ Show Front  │ ← User sees question
└──────┬──────┘
       │ Click "Show Answer"
       v
┌─────────────┐
│ Show Answer │ ← User sees answer + quality buttons
└──────┬──────┘
       │ Click quality (0-5)
       v
┌─────────────┐
│ Submit      │ ← POST to /api/study/cards/{id}/review
│ Review      │   Backend applies SM-2 algorithm
└──────┬──────┘
       │
       ├─ More cards? → Next card (reset to Show Front)
       └─ No cards?   → Redirect to deck detail
```

**Key Patterns**:

1. **Progressive Disclosure**:
   ```typescript
   const [showAnswer, setShowAnswer] = useState(false);
   
   {showAnswer ? (
     // Show answer + quality buttons
   ) : (
     <button onClick={() => setShowAnswer(true)}>Show Answer</button>
   )}
   ```
   - Don't show answer until user ready
   - Forces active recall (better learning)

2. **Session Progress Tracking**:
   ```typescript
   const [currentIndex, setCurrentIndex] = useState(0);
   const progress = `${currentIndex + 1} / ${session.cardsToReview.length}`;
   ```
   - Zero-indexed array, but display as 1-indexed (UX)
   - Show user how much left

3. **Completion Handling**:
   ```typescript
   if (currentIndex < session.cardsToReview.length - 1) {
     setCurrentIndex(currentIndex + 1);  // Next card
   } else {
     router.push(`/decks/${deckId}`);    // Finished!
   }
   ```
   - Check bounds before incrementing
   - Redirect on completion

4. **Quality Rating UX**:
   ```typescript
   <button onClick={() => handleReview(0)}>0 - Total Blackout</button>
   <button onClick={() => handleReview(3)}>3 - Correct, with difficulty</button>
   <button onClick={() => handleReview(5)}>5 - Perfect recall</button>
   ```
   - Explicit descriptions for each quality level
   - Users understand what each number means
   - Maps directly to SM-2 algorithm quality scale

### Form Handling Best Practices

Our forms follow consistent patterns for validation and submission.

**Create Card Form** (from `/frontend/app/decks/[id]/page.tsx`):
```typescript
const [newCard, setNewCard] = useState({ front: '', back: '' });
const [error, setError] = useState('');

const handleCreateCard = async (e: React.FormEvent) => {
  e.preventDefault();  // Don't reload page
  
  // Client-side validation
  if (!newCard.front.trim() || !newCard.back.trim()) {
    setError('Both front and back are required');
    return;
  }
  
  setError('');  // Clear previous errors
  
  try {
    const created = await createCard(deckId, newCard);
    setCards([...cards, created]);  // Add to list
    setNewCard({ front: '', back: '' });  // Reset form
  } catch (error) {
    setError('Failed to create card');
    console.error(error);
  }
};

return (
  <form onSubmit={handleCreateCard}>
    {error && <div style={{ color: 'red' }}>{error}</div>}
    
    <input
      type="text"
      value={newCard.front}
      onChange={(e) => setNewCard({ ...newCard, front: e.target.value })}
      placeholder="Front (question)"
    />
    
    <input
      type="text"
      value={newCard.back}
      onChange={(e) => setNewCard({ ...newCard, back: e.target.value })}
      placeholder="Back (answer)"
    />
    
    <button type="submit">Add Card</button>
  </form>
);
```

**Form Handling Checklist**:
- ✅ `e.preventDefault()` to stop page reload
- ✅ Client-side validation before API call
- ✅ Error state for user feedback
- ✅ Reset form on success
- ✅ Optimistic UI update (add to list immediately)
- ✅ Controlled components (`value` + `onChange`)

### Styling with Tailwind CSS

We use **Tailwind CSS** for utility-first styling. Here's why:

**Traditional CSS**:
```css
/* styles.css */
.card-container {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.12);
}
```
```tsx
<div className="card-container">...</div>
```

**Tailwind CSS**:
```tsx
<div className="flex flex-col p-4 bg-white rounded-lg shadow-md">
  ...
</div>
```

**Benefits**:
- No separate CSS files
- No naming conflicts
- No unused CSS (Tailwind purges automatically)
- Responsive design is easy: `md:flex-row` (flex-row on medium screens+)
- Consistent design system (spacing scale, colors, etc.)

**Our Global Styles** (`/frontend/app/globals.css`):
```css
@tailwind base;      /* Reset, base styles */
@tailwind components;  /* Component classes */
@tailwind utilities;   /* Utility classes */

/* Custom base styles */
body {
  font-family: var(--font-geist-sans);
  background-color: #f9fafb;
}
```

**Configuration** (`/frontend/tailwind.config.ts`):
```typescript
const config: Config = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {},  // Add custom colors, spacing, etc.
  },
  plugins: [],
};
```

**Scaling Path**:
- **Now**: Inline utility classes
- **Later**: Extract to components (`<Card>`, `<Button>`)
- **Production**: Add `@layer components` for reusable styles


---

## PART V: DOCKER & ENVIRONMENT SETUP

### Why Docker from Day One?

Most tutorials say "get it working locally first, add Docker later." We did the opposite. Why?

**Dev/Prod Parity (12-Factor App Principle)**:
```
Developer's Machine              Production Server
─────────────────                ────────────────
macOS, Node 18, Postgres 14  vs  Linux, Node 20, Postgres 15
Works locally but fails in prod  ❌
```

With Docker:
```
Developer's Machine              Production Server
─────────────────                ────────────────
Docker, same images          →   Docker, same images
Identical environments       ✅
```

**Benefits**:
- **No "Works on My Machine"**: Same containers everywhere
- **Fast Onboarding**: New dev runs `docker compose up`, done
- **Easy Cleanup**: `docker compose down -v` removes everything
- **Isolated Dependencies**: Multiple projects, no conflicts
- **Production-Like**: Test real postgres, not sqlite

### Docker Fundamentals

**Docker Image**: Read-only template with application code, runtime, libraries
**Docker Container**: Running instance of an image
**Docker Compose**: Tool to define multi-container applications

**Analogy**:
- **Image** = Class definition (blueprint)
- **Container** = Object instance (running application)

**Example Flow**:
```bash
# 1. Build image from Dockerfile
docker build -t my-app .

# 2. Run container from image
docker run -p 8080:8080 my-app

# 3. With compose (multiple containers)
docker compose up
```

### Docker Compose Deep Dive

Our `docker-compose.yml` orchestrates 3 services: postgres, backend, frontend.

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine         # Use official image (don't build)
    container_name: anki-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB}     # From .env file
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "${POSTGRES_PORT}:5432"       # Host:Container
    volumes:
      - postgres_data:/var/lib/postgresql/data  # Persist data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile.dev      # Use dev dockerfile
    container_name: anki-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}
    ports:
      - "${BACKEND_PORT}:8080"
    volumes:
      - ./backend:/app                # Mount code for hot reload
      - /app/build                    # Don't mount build dir
      - /app/.gradle                  # Don't mount gradle cache
    depends_on:
      postgres:
        condition: service_healthy    # Wait for postgres ready
    command: ./gradlew bootRun --continuous  # Auto-restart on changes

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.dev
    container_name: anki-frontend
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080  # Important: localhost, not backend!
    ports:
      - "${FRONTEND_PORT}:3000"
    volumes:
      - ./frontend:/app
      - /app/node_modules             # Don't mount node_modules
      - /app/.next                    # Don't mount build cache
    command: npm run dev

volumes:
  postgres_data:                      # Named volume for data persistence
```

**Key Concepts Explained**:

1. **Service Names as Hostnames**:
   ```yaml
   SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/...
   #                                       ↑
   #                            Docker network hostname
   ```
   - Inside Docker network, services talk via service names
   - `backend` can reach postgres at `postgres:5432`
   - `frontend` **cannot** use `backend:8080` (explained below)

2. **Port Mapping** (`HOST:CONTAINER`):
   ```yaml
   ports:
     - "8080:8080"  # localhost:8080 → container:8080
   ```
   - First number: Port on your machine
   - Second number: Port inside container
   - Example: `3001:3000` runs Next.js on port 3001 locally

3. **Volume Mounts**:
   ```yaml
   volumes:
     - ./backend:/app           # Bind mount (sync local ↔ container)
     - /app/build              # Anonymous volume (don't sync)
   ```
   
   **Types of Volumes**:
   - **Bind Mount** (`./backend:/app`): Two-way sync, changes reflect immediately
   - **Anonymous Volume** (`/app/build`): Isolate folder from bind mount
   - **Named Volume** (`postgres_data:/var/lib/postgresql/data`): Persist data
   
   **Why Both?**
   ```yaml
   volumes:
     - ./backend:/app      # Sync code changes
     - /app/build         # Don't sync build artifacts (huge, unnecessary)
   ```
   - Without `/app/build`, your local `build/` would overwrite container's
   - Anonymous volume takes precedence for that specific path

4. **Environment Variables**:
   ```yaml
   environment:
     POSTGRES_DB: ${POSTGRES_DB}  # From .env file
   ```
   
   **`.env` file** (root directory):
   ```bash
   POSTGRES_DB=anki_db
   POSTGRES_USER=anki_user
   POSTGRES_PASSWORD=anki_pass_dev
   POSTGRES_PORT=5432
   BACKEND_PORT=8080
   FRONTEND_PORT=3000
   JWT_SECRET=your-secret-key-change-in-production
   JWT_EXPIRATION=86400000
   ```
   
   - Never commit `.env` (add to `.gitignore`)
   - Provide `.env.example` with dummy values
   - Different values for dev/staging/prod

5. **Health Checks**:
   ```yaml
   healthcheck:
     test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
     interval: 5s
   ```
   - Checks if postgres is actually ready (not just started)
   - Backend waits via `depends_on: postgres: condition: service_healthy`
   - Prevents connection errors during startup race conditions

6. **depends_on with condition**:
   ```yaml
   depends_on:
     postgres:
       condition: service_healthy  # Wait for healthcheck to pass
   ```
   - Old way: `depends_on: [postgres]` (only waits for start, not ready)
   - New way: Waits until postgres accepting connections
   - Prevents backend crash on startup

### Development Dockerfile

**Backend Dockerfile.dev** (`/backend/Dockerfile.dev`):
```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy gradle wrapper files first (cache optimization)
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# Copy dependency declarations (cache optimization)
COPY build.gradle settings.gradle ./

# Download dependencies (cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon

# Copy application code
COPY . .

# Expose port
EXPOSE 8080

# Run with auto-reload
CMD ["./gradlew", "bootRun", "--continuous"]
```

**Frontend Dockerfile.dev** (`/frontend/Dockerfile.dev`):
```dockerfile
FROM node:20-alpine

WORKDIR /app

# Copy package files first (cache optimization)
COPY package.json package-lock.json ./

# Install dependencies (cached unless package.json changes)
RUN npm ci

# Copy application code
COPY . .

# Expose port
EXPOSE 3000

# Run dev server
CMD ["npm", "run", "dev"]
```

**Layer Caching Optimization**:
```dockerfile
# ❌ BAD: Copies everything, dependencies re-download on ANY code change
COPY . .
RUN npm install

# ✅ GOOD: Dependencies cached unless package.json changes
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
```

**Why This Matters**:
- Code changes often (every file save)
- Dependencies change rarely (when you add packages)
- Cache dependency layer → faster rebuilds (5s vs 2min)

**Docker Build Cache Flow**:
```
┌─────────────────────┐
│ COPY package.json   │ ← Hash matches cache? Use cached layer
├─────────────────────┤
│ RUN npm ci          │ ← Cached! Skip npm install
├─────────────────────┤
│ COPY . .            │ ← Hash changed (code updated) → rebuild from here
├─────────────────────┤
│ CMD npm run dev     │
└─────────────────────┘
```

### Production Dockerfile

**Frontend Dockerfile (Production)** (`/frontend/Dockerfile`):
```dockerfile
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build  # Creates optimized production build

# Stage 2: Runtime
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Copy only production dependencies
COPY --from=builder /app/package.json ./
COPY --from=builder /app/package-lock.json ./
RUN npm ci --only=production

# Copy built app
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

EXPOSE 3000

CMD ["node", "server.js"]
```

**Multi-Stage Build Benefits**:
- **Smaller Image**: Final image doesn't include dev dependencies, source code
- **Security**: Fewer packages = smaller attack surface
- **Performance**: Optimized production build

**Size Comparison**:
```
Single-stage build:   850 MB  (includes node_modules, source, build tools)
Multi-stage build:    180 MB  (only runtime dependencies + built files)
```

**next.config.mjs** (required for standalone):
```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',  // ← Bundles everything into .next/standalone
};

export default nextConfig;
```

### Docker Networking Gotcha (Critical!)

**The Problem**:
```yaml
frontend:
  environment:
    NEXT_PUBLIC_API_URL: http://backend:8080  # ❌ WRONG!
```

**Why it fails**:
```
┌───────────────────────────────────────────────────────┐
│ Docker Network (containers can see each other)       │
│                                                       │
│  ┌────────────┐           ┌─────────────┐           │
│  │  frontend  │           │   backend   │           │
│  │  :3000     │←──────────│   :8080     │           │
│  └────────────┘  ✅ Works  └─────────────┘           │
│        │                                              │
└────────┼──────────────────────────────────────────────┘
         │
         │ ❌ Browser runs HERE (outside Docker)
         │    Cannot resolve "backend" hostname
         │
    ┌────▼─────┐
    │ Browser  │
    │ (Your PC)│
    └──────────┘
```

**The Fix**:
```yaml
frontend:
  environment:
    NEXT_PUBLIC_API_URL: http://localhost:8080  # ✅ CORRECT!
```

**Why it works**:
- Frontend container serves files to browser at `localhost:3000`
- Browser runs on your machine, outside Docker
- When browser makes API calls, it uses `localhost:8080`
- Docker maps `localhost:8080` → `backend:8080` via port binding

**Rule of Thumb**:
- **Server-to-server** (inside Docker): Use service names (`backend`, `postgres`)
- **Browser-to-server** (outside Docker): Use `localhost` or actual domain

### Environment Variable Management

**Three Layers of Configuration**:

1. **Docker Compose** (`.env` file):
   ```bash
   POSTGRES_DB=anki_db
   BACKEND_PORT=8080
   ```
   - Used by docker-compose.yml
   - Controls ports, container names, etc.

2. **Backend** (`application.properties`):
   ```properties
   spring.datasource.url=${SPRING_DATASOURCE_URL}
   jwt.secret=${JWT_SECRET}
   ```
   - Spring Boot reads from environment variables
   - Set via docker-compose.yml `environment:` section

3. **Frontend** (`.env.local`):
   ```bash
   NEXT_PUBLIC_API_URL=http://localhost:8080
   ```
   - Next.js reads `NEXT_PUBLIC_*` variables
   - Embedded into browser bundle at **build time**
   - Can also set via docker-compose.yml

**Environment Priority**:
```
docker-compose.yml environment: > .env.local > .env > defaults
```

**Security Note**:
- **Backend env vars**: Safe, only on server
- **Frontend NEXT_PUBLIC_**: Exposed to browser! Never put secrets here
  ```bash
  NEXT_PUBLIC_API_URL=...      # ✅ OK (public info)
  NEXT_PUBLIC_API_KEY=secret   # ❌ EXPOSED TO BROWSER!
  ```

### Common Docker Commands

**Start everything**:
```bash
docker compose up         # Foreground (see logs)
docker compose up -d      # Background (detached)
docker compose up --build # Rebuild images first
```

**View logs**:
```bash
docker compose logs                    # All services
docker compose logs backend            # Specific service
docker compose logs -f backend         # Follow (live tail)
docker compose logs --tail=50 backend  # Last 50 lines
```

**Stop containers**:
```bash
docker compose down              # Stop containers
docker compose down -v           # Stop + delete volumes (fresh start)
```

**Restart single service**:
```bash
docker compose restart backend
```

**Rebuild single service**:
```bash
docker compose up --build -d backend
```

**Execute commands in container**:
```bash
docker compose exec backend ./gradlew test    # Run tests
docker compose exec postgres psql -U anki_user anki_db  # Open postgres shell
```

**Clean up everything**:
```bash
docker compose down -v              # Stop containers + delete volumes
docker system prune -a --volumes    # Delete all unused images/volumes (nuclear option)
```

### Hot Reload Setup

**How it works**:

1. **Volume Mount**: Code changes on host sync to container
   ```yaml
   volumes:
     - ./backend:/app  # Local changes → Container
   ```

2. **Auto-Restart Command**:
   ```yaml
   command: ./gradlew bootRun --continuous  # Backend
   command: npm run dev                     # Frontend
   ```

3. **Watch Mode**:
   - Gradle `--continuous`: Watches for file changes, restarts Spring Boot
   - Next.js dev server: Built-in Fast Refresh, instant updates

**Test it**:
1. Run `docker compose up`
2. Edit `/backend/src/main/java/com/peanuts/anki/deck/DeckService.java`
3. Save file
4. Watch logs: "Restarting Spring Boot application..."
5. Changes live in ~5 seconds

**Troubleshooting Hot Reload**:

**Backend not restarting?**
```bash
# Check if files are syncing
docker compose exec backend ls -la /app/src/main/java/com/peanuts/anki/deck/

# Restart manually
docker compose restart backend
```

**Frontend changes not appearing?**
```bash
# Clear Next.js cache
docker compose exec frontend rm -rf .next
docker compose restart frontend
```

**Volume permission issues (Linux)?**
```yaml
# Add user mapping to docker-compose.yml
user: "${UID}:${GID}"
```

### Database Persistence

**Named Volumes** persist data across container restarts:
```yaml
volumes:
  postgres_data:  # Define named volume

services:
  postgres:
    volumes:
      - postgres_data:/var/lib/postgresql/data  # Use it
```

**What happens**:
```bash
docker compose up     # Create volume, postgres writes data
docker compose down   # Stop container, volume persists
docker compose up     # Start again, data still there ✅
```

**Fresh start**:
```bash
docker compose down -v  # -v deletes volumes
docker compose up       # Clean database
```

**Inspect volume**:
```bash
docker volume ls                           # List all volumes
docker volume inspect peanuts-anki-spring_postgres_data
```

**Backup database**:
```bash
docker compose exec postgres pg_dump -U anki_user anki_db > backup.sql
```

**Restore database**:
```bash
docker compose exec -T postgres psql -U anki_user anki_db < backup.sql
```

### Development Workflow

**Typical Day**:
```bash
# Morning: Start everything
docker compose up -d

# Make code changes (auto-reloads)
# Edit files in ./backend or ./frontend

# View logs if something breaks
docker compose logs -f backend

# Need fresh database?
docker compose down -v
docker compose up -d

# End of day: Stop containers
docker compose down
```

**Adding Dependencies**:

**Backend** (add Gradle dependency):
```bash
# 1. Edit build.gradle (add dependency)
vim backend/build.gradle

# 2. Rebuild container (dependencies cached in Dockerfile)
docker compose up --build -d backend
```

**Frontend** (add npm package):
```bash
# Option 1: Add to container
docker compose exec frontend npm install axios

# Option 2: Add locally (faster)
cd frontend
npm install axios
docker compose restart frontend
```

**Running Tests**:
```bash
# Backend
docker compose exec backend ./gradlew test

# Frontend
docker compose exec frontend npm test
```

### Docker Best Practices

**Dockerfile**:
- ✅ Use specific image versions (`node:20-alpine`, not `node:latest`)
- ✅ Order layers by frequency of change (dependencies first, code last)
- ✅ Use `.dockerignore` to exclude unnecessary files
- ✅ Multi-stage builds for production

**docker-compose.yml**:
- ✅ Use health checks for dependencies
- ✅ Named volumes for data persistence
- ✅ Environment variables from `.env` file
- ✅ Container names for easier debugging

**Security**:
- ✅ Don't commit `.env` files (use `.env.example`)
- ✅ Use secrets management in production (Docker secrets, Vault)
- ✅ Run containers as non-root user (production)
- ✅ Scan images for vulnerabilities (`docker scan`)

**Performance**:
- ✅ Use Alpine images when possible (smaller)
- ✅ Layer caching optimization
- ✅ Anonymous volumes for build artifacts
- ✅ Limit container resources (`mem_limit`, `cpus`)


---

## PART VI: DATA FLOW & REQUEST LIFECYCLE

### Complete Request Flow: Study Session

Let's trace a complete study session from browser to database and back. This shows how all layers work together.

**User Action**: Click "Study Deck" button

```
┌──────────────────────────────────────────────────────────────────────────┐
│ BROWSER (React Component)                                                │
├──────────────────────────────────────────────────────────────────────────┤
│ 1. User clicks "Study" button on deck detail page                        │
│ 2. Next.js router navigates to /decks/123/study                         │
│ 3. StudyPage component mounts                                           │
│ 4. useEffect runs on mount                                              │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ FRONTEND API LAYER (lib/api/study.ts)                                   │
├──────────────────────────────────────────────────────────────────────────┤
│ 5. startStudySession(deckId) called                                     │
│ 6. Calls apiRequest<StudySessionDTO>(                                   │
│      `/api/study/decks/${deckId}/start`,                               │
│      { method: 'POST' }                                                 │
│    )                                                                     │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ API CLIENT (lib/api/client.ts)                                          │
├──────────────────────────────────────────────────────────────────────────┤
│ 7. Get userId from localStorage: "1"                                    │
│ 8. Build request:                                                        │
│    URL: http://localhost:8080/api/study/decks/123/start                │
│    Method: POST                                                          │
│    Headers:                                                              │
│      Content-Type: application/json                                     │
│      X-User-Id: 1                                                       │
│ 9. Send fetch request                                                    │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ HTTP POST
                                 │ ───────────────────────────────────────►
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ SPRING BOOT (Backend Container)                                         │
├──────────────────────────────────────────────────────────────────────────┤
│ 10. DispatcherServlet receives request                                  │
│ 11. CORS filter checks origin (localhost:3000) → Allow                  │
│ 12. Security filter chain → permitAll() → Allow                         │
│ 13. Request mapping: POST /api/study/decks/123/start                   │
│     → StudyController.startStudySession(deckId, userId)                │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ CONTROLLER LAYER (StudyController.java)                                 │
├──────────────────────────────────────────────────────────────────────────┤
│ 14. Method signature:                                                    │
│     public ResponseEntity<StudySessionDTO> startStudySession(           │
│         @PathVariable Long deckId,                                      │
│         @RequestHeader("X-User-Id") Long userId                        │
│     )                                                                    │
│                                                                          │
│ 15. Extract parameters:                                                 │
│     deckId = 123 (from URL path)                                        │
│     userId = 1 (from header)                                            │
│                                                                          │
│ 16. Call service:                                                        │
│     StudySessionDTO session = studyService.startSession(deckId, userId);│
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ SERVICE LAYER (StudyService.java)                                       │
├──────────────────────────────────────────────────────────────────────────┤
│ 17. @Transactional method begins                                        │
│     (Spring starts database transaction)                                │
│                                                                          │
│ 18. Verify deck exists:                                                 │
│     Deck deck = deckRepository.findById(123)                            │
│                                 .orElseThrow(() -> ...)                 │
│                                                                          │
│ 19. Verify user owns deck:                                              │
│     if (!deck.getUser().getId().equals(userId))                        │
│         throw new IllegalArgumentException(...)                         │
│                                                                          │
│ 20. Find cards due for review:                                          │
│     List<Card> dueCards = cardRepository                                │
│         .findByDeckIdAndNextReviewDateBeforeOrderByNextReviewDate(     │
│             123,                                                         │
│             LocalDateTime.now()                                         │
│         );                                                               │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ SELECT * FROM cards WHERE ...
                                 │ ───────────────────────────────────────►
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ POSTGRESQL (Database Container)                                         │
├──────────────────────────────────────────────────────────────────────────┤
│ 21. Execute query:                                                       │
│     SELECT * FROM cards                                                  │
│     WHERE deck_id = 123                                                  │
│       AND next_review_date <= '2025-10-25T10:30:00'                    │
│     ORDER BY next_review_date;                                          │
│                                                                          │
│ 22. Result set: 5 cards found                                           │
│     [Card(id=1, front="What is JPA?", ...),                            │
│      Card(id=2, front="What is Docker?", ...),                         │
│      ...]                                                                │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ Result set
                                 │ ◄───────────────────────────────────────
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ HIBERNATE (ORM Layer)                                                    │
├──────────────────────────────────────────────────────────────────────────┤
│ 23. Map result set to Card entities                                     │
│ 24. Populate relationships (lazy-loaded if needed)                      │
│ 25. Return List<Card> to service                                        │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ SERVICE LAYER (continued)                                                │
├──────────────────────────────────────────────────────────────────────────┤
│ 26. Build DTO:                                                           │
│     StudySessionDTO sessionDTO = StudySessionDTO.builder()              │
│         .deckId(deck.getId())                                           │
│         .deckName(deck.getName())                                       │
│         .totalCards(dueCards.size())                                    │
│         .cardsToReview(                                                 │
│             dueCards.stream()                                           │
│                 .map(this::convertToDTO)                               │
│                 .toList()                                               │
│         )                                                                │
│         .build();                                                        │
│                                                                          │
│ 27. @Transactional method ends                                          │
│     (Spring commits transaction)                                        │
│                                                                          │
│ 28. Return sessionDTO to controller                                     │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ CONTROLLER LAYER (continued)                                             │
├──────────────────────────────────────────────────────────────────────────┤
│ 29. Wrap in ResponseEntity:                                              │
│     return ResponseEntity.ok(sessionDTO);                               │
│                                                                          │
│ 30. Spring serializes to JSON:                                          │
│     {                                                                    │
│       "deckId": 123,                                                     │
│       "deckName": "Java Fundamentals",                                  │
│       "totalCards": 5,                                                   │
│       "cardsToReview": [                                                │
│         {                                                                │
│           "id": 1,                                                       │
│           "front": "What is JPA?",                                      │
│           "back": "Java Persistence API...",                           │
│           "easeFactor": 2.5,                                            │
│           ...                                                            │
│         },                                                               │
│         ...                                                              │
│       ]                                                                  │
│     }                                                                    │
│                                                                          │
│ 31. HTTP Response:                                                       │
│     Status: 200 OK                                                       │
│     Content-Type: application/json                                      │
│     Body: [JSON above]                                                   │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ HTTP 200 OK + JSON
                                 │ ◄───────────────────────────────────────
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ API CLIENT (continued)                                                   │
├──────────────────────────────────────────────────────────────────────────┤
│ 32. Receive response                                                     │
│ 33. Check response.ok → true                                            │
│ 34. Parse JSON: const data = await response.json()                      │
│ 35. TypeScript validates shape matches StudySessionDTO                  │
│ 36. Return typed data to caller                                         │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ REACT COMPONENT (continued)                                              │
├──────────────────────────────────────────────────────────────────────────┤
│ 37. setSession(data) updates state                                      │
│ 38. setLoading(false) hides loading indicator                           │
│ 39. Component re-renders with session data                              │
│ 40. User sees first card:                                               │
│     ┌────────────────────────────────────┐                             │
│     │  Question 1/5                      │                             │
│     │  What is JPA?                      │                             │
│     │  [Show Answer]                     │                             │
│     └────────────────────────────────────┘                             │
└──────────────────────────────────────────────────────────────────────────┘
```

**Total Time**: ~50-200ms (depending on network, database, query complexity)

**Breakdown**:
- Network latency: 5-20ms (local), 50-200ms (production)
- Database query: 5-50ms (indexed query on small dataset)
- Serialization/deserialization: 5-10ms
- React render: 5-10ms

### Card Review Flow (With SM-2 Algorithm)

**User Action**: Click quality rating after seeing answer

```
┌──────────────────────────────────────────────────────────────────────────┐
│ BROWSER                                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ 1. User clicks "4 - Correct, with hesitation"                           │
│ 2. handleReview(4) called                                               │
│ 3. reviewCard(cardId, { quality: 4 })                                   │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ POST /api/study/cards/1/review
                                 │ Body: { "quality": 4 }
                                 │ ───────────────────────────────────────►
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ CONTROLLER                                                                │
├──────────────────────────────────────────────────────────────────────────┤
│ 4. StudyController.reviewCard(cardId=1, request={ quality: 4 })        │
│ 5. Call studyService.reviewCard(1, 4)                                   │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 v
┌──────────────────────────────────────────────────────────────────────────┐
│ SERVICE LAYER (SM-2 Algorithm Application)                              │
├──────────────────────────────────────────────────────────────────────────┤
│ 6. @Transactional starts                                                │
│                                                                          │
│ 7. Load card from database:                                             │
│    Card card = cardRepository.findById(1)                               │
│                              .orElseThrow(...)                          │
│    Current state:                                                        │
│      repetitions = 2                                                     │
│      easeFactor = 2.36                                                  │
│      interval = 6                                                        │
│                                                                          │
│ 8. Apply SM-2 algorithm (quality = 4):                                  │
│                                                                          │
│    a) Check if failed (quality < 3):                                    │
│       4 >= 3 → Not failed, proceed to advance                           │
│                                                                          │
│    b) Advance card (quality >= 3):                                      │
│       card.setRepetitions(card.getRepetitions() + 1)  → 3               │
│                                                                          │
│    c) Calculate new interval:                                           │
│       reps = 3                                                           │
│       if (reps == 0) return 1;        // First time                     │
│       if (reps == 1) return 6;        // Second time                    │
│       // Third+ time: exponential                                       │
│       interval = round(6 * 2.36) = 14 days                             │
│       card.setInterval(14)                                              │
│                                                                          │
│    d) Update ease factor:                                               │
│       newEF = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))│
│       newEF = 2.36 + (0.1 - (5 - 4) * (0.08 + (5 - 4) * 0.02))        │
│       newEF = 2.36 + (0.1 - 1 * (0.08 + 1 * 0.02))                     │
│       newEF = 2.36 + (0.1 - 0.1) = 2.36  (stays same for quality 4)    │
│       card.setEaseFactor(2.36)                                          │
│                                                                          │
│    e) Schedule next review:                                             │
│       nextDate = LocalDateTime.now().plusDays(14)                       │
│       card.setNextReviewDate(nextDate)  → 2025-11-08                   │
│                                                                          │
│ 9. Save card:                                                            │
│    cardRepository.save(card)                                            │
│    (Hibernate dirty checking auto-saves on transaction commit)          │
│                                                                          │
│ 10. Build response:                                                      │
│     ReviewResponse response = ReviewResponse.builder()                  │
│         .success(true)                                                   │
│         .message("Card reviewed successfully")                          │
│         .nextReviewDate(nextDate)                                       │
│         .build();                                                        │
│                                                                          │
│ 11. @Transactional commits                                              │
│     (Database UPDATE executed now)                                      │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ UPDATE cards SET
                                 │ repetitions = 3,
                                 │ ease_factor = 2.36,
                                 │ interval = 14,
                                 │ next_review_date = '2025-11-08'
                                 │ WHERE id = 1;
                                 │ ───────────────────────────────────────►
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ DATABASE                                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ 12. Execute UPDATE                                                       │
│ 13. Commit transaction                                                   │
│ 14. Card now scheduled for Nov 8 (14 days later)                       │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ Success
                                 │ ◄───────────────────────────────────────
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ CONTROLLER (continued)                                                    │
├──────────────────────────────────────────────────────────────────────────┤
│ 15. Return ResponseEntity.ok(response)                                   │
│     JSON: {                                                              │
│       "success": true,                                                   │
│       "message": "Card reviewed successfully",                          │
│       "nextReviewDate": "2025-11-08T10:30:00"                          │
│     }                                                                    │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 │ HTTP 200 OK + JSON
                                 │ ◄───────────────────────────────────────
                                 │
┌──────────────────────────────────────────────────────────────────────────┐
│ BROWSER (continued)                                                       │
├──────────────────────────────────────────────────────────────────────────┤
│ 16. Receive response                                                     │
│ 17. Update UI state:                                                     │
│     if (currentIndex < totalCards - 1) {                                │
│       setCurrentIndex(currentIndex + 1)  // Next card                   │
│       setShowAnswer(false)               // Hide answer                 │
│     } else {                                                             │
│       router.push(`/decks/${deckId}`)    // Session complete           │
│     }                                                                    │
│ 18. User sees next card or redirects to deck list                       │
└──────────────────────────────────────────────────────────────────────────┘
```

**SM-2 Quality Ratings Explained**:
```
Quality │ Meaning                        │ Effect on Ease Factor
────────┼────────────────────────────────┼─────────────────────────
   0    │ Complete blackout              │ Decrease significantly
   1    │ Incorrect, but familiar        │ Decrease moderately
   2    │ Incorrect, but easy to recall  │ Decrease slightly
   3    │ Correct, with difficulty       │ Maintain or slight decrease
   4    │ Correct, with hesitation       │ Maintain
   5    │ Perfect recall                 │ Increase

Quality < 3: Card reset (repetitions → 0, interval → 1 day)
Quality >= 3: Card advanced (repetitions++, interval grows exponentially)
```

### Authentication Flow

**User Registration**:
```
Browser                     Backend                    Database
───────                     ───────                    ────────
                            
POST /api/auth/register
{                           
  "username": "john",       
  "email": "john@ex.com",   
  "password": "secret123"   
}                           
        │                   
        │                   AuthController.register()
        │                   │
        │                   UserService.register()
        │                   │
        │                   ├─ Check if username exists
        │                   │  SELECT * FROM users
        │                   │  WHERE username = 'john'
        │                   │                     │
        │                   │                     └─────► Query
        │                   │                            Empty result ✓
        │                   │  ◄────────────────────────
        │                   │
        │                   ├─ Hash password
        │                   │  BCrypt.hashpw("secret123")
        │                   │  → "$2a$10$abc...xyz"
        │                   │
        │                   ├─ Create user entity
        │                   │  User(username, email, hashedPassword)
        │                   │
        │                   └─ Save to database
        │                      userRepository.save(user)
        │                                         │
        │                                         └─────► INSERT INTO users...
        │                                                user.id = 1
        │                   ◄─────────────────────────────
        │                   │
        │                   Generate JWT
        │                   JwtUtil.generateToken(user.getId())
        │                   │
        │                   Build response
        │                   AuthResponse(userId=1, token="eyJ...", username="john")
        │                   │
        ◄───────────────────┘
        │
{       │
  "userId": 1,
  "token": "eyJhbGc...",
  "username": "john"
}
        │
Store in localStorage:
  userId → "1"
  token → "eyJhbGc..."
```

**Using JWT Token** (current simplified approach):
```
Browser                     Backend
───────                     ───────

GET /api/decks
Headers:
  X-User-Id: 1              SecurityConfig: permitAll()
        │                   (No JWT verification)
        │                   │
        │                   DeckController gets userId from header
        │                   │
        │                   DeckService.getUserDecks(userId=1)
        │                   │
        ◄───────────────────┘
[list of decks]
```

**Production JWT Flow** (what we'd add later):
```
Browser                     JwtAuthFilter               Controller
───────                     ─────────────               ──────────

GET /api/decks
Headers:
  Authorization: Bearer eyJ...
        │                   
        │                   Extract token
        │                   Validate signature
        │                   Extract userId from claims
        │                   Set SecurityContext
        │                           │
        │                           │             DeckController
        │                           │             @AuthenticationPrincipal userId
        │                           └─────────────►
        │                                         getUserDecks(userId)
        ◄─────────────────────────────────────────
[list of decks]
```

**Why We Simplified**:
- Learning focus: Algorithm and architecture, not security
- Weekend timeline: Full JWT takes time to implement correctly
- Tokens still generated: Shows JWT concepts, ready to add filter later
- Clear upgrade path: Add JwtAuthFilter, change permitAll() → authenticated()

### Error Handling Flow

**Validation Error**:
```
Browser                     Backend                    Response
───────                     ───────                    ────────

POST /api/decks/123/cards
{
  "front": "",              CardService.createCard()
  "back": "Answer"          │
}                           if (front.isBlank())
        │                       throw IllegalArgumentException
        │                   │
        │                   Spring exception handler
        │                   @ExceptionHandler catches it
        │                   │
        ◄───────────────────┘
        │
HTTP 400 Bad Request
{
  "error": "Front text is required",
  "timestamp": "2025-10-25T10:30:00"
}
        │
Display error message in UI
```

**Not Found Error**:
```
POST /api/study/decks/999/start
(deck doesn't exist)
        │
        DeckRepository.findById(999)
            .orElseThrow(() -> new ResourceNotFoundException(...))
        │
        Exception handler returns 404
        │
HTTP 404 Not Found
{
  "error": "Deck not found with id: 999"
}
```

**Database Error**:
```
Hibernate tries to save
Database constraint violation
(e.g., duplicate username)
        │
DataIntegrityViolationException
        │
Spring translates to appropriate HTTP status
        │
HTTP 409 Conflict
{
  "error": "Username already exists"
}
```

### Transaction Boundaries

**Understanding @Transactional**:

```
Without @Transactional:
─────────────────────────
Service method {
  Card card = repo.findById(1);        // Transaction 1 (auto-commit)
  card.setFront("New front");          // No transaction
  repo.save(card);                     // Transaction 2 (auto-commit)
  
  // Problem: Changes between queries not atomic!
  // Another request could modify card between lines
}

With @Transactional:
────────────────────
@Transactional
Service method {
  // ──────── Transaction starts ────────
  Card card = repo.findById(1);        │
  card.setFront("New front");          │ All or nothing
  repo.save(card);                     │
  // ──────── Transaction commits ───────
  
  // All operations atomic, isolated from other requests
}
```

**Read-Only Optimization**:
```java
@Transactional(readOnly = true)  // Class level
public class DeckService {
    
    public List<DeckDTO> getUserDecks(Long userId) {
        // Read-only transaction:
        // - Hibernate skips dirty checking (faster)
        // - Database can optimize (no write locks)
        return deckRepository.findByUserId(userId)
            .stream()
            .map(this::convertToDTO)
            .toList();
    }
    
    @Transactional  // Override for write operations
    public DeckDTO createDeck(Long userId, CreateDeckRequest request) {
        // Read-write transaction:
        // - Full dirty checking
        // - Write locks acquired
        Deck deck = Deck.builder()
            .name(request.name())
            .user(userRepository.getReferenceById(userId))
            .build();
        return convertToDTO(deckRepository.save(deck));
    }
}
```

**Benefits**:
- 5-15% performance improvement on reads (no dirty checking)
- Clear intent: Method signature shows if it modifies data
- Safety: Write operations fail if accidentally called in readOnly transaction

### Lazy Loading & N+1 Query Problem

**The N+1 Problem**:
```java
// Bad: N+1 queries
List<Deck> decks = deckRepository.findByUserId(userId);  // 1 query
for (Deck deck : decks) {
    deck.getCards().size();  // N queries (one per deck)
}
// Total: 1 + N queries

// Good: Join fetch
@Query("SELECT d FROM Deck d LEFT JOIN FETCH d.cards WHERE d.user.id = :userId")
List<Deck> findByUserIdWithCards(@Param("userId") Long userId);  // 1 query
// Total: 1 query
```

**Our Approach**: We kept it simple
- No `@OneToMany` on Deck → Card
- Explicitly fetch cards when needed: `cardRepository.findByDeckId(deckId)`
- More queries but simpler code, fine for small datasets

**When to Optimize**:
- 100+ decks per user → Add pagination
- 1000+ cards per deck → Add join fetch
- Slow queries → Add database indexes
- Profile first, optimize second


---

## PART VII: SCALING CONSIDERATIONS

### What We Built Right for Scale

**1. Stateless Backend**
```
✅ No session storage in memory
✅ JWT-based authentication (tokens client-side)
✅ Can scale horizontally: Add more backend containers

Load Balancer
      │
      ├──► Backend Instance 1 ─┐
      ├──► Backend Instance 2 ─┼──► Shared Postgres
      └──► Backend Instance 3 ─┘

Any request can go to any instance
```

**2. Database Connection Pooling**
```
Spring Boot (HikariCP built-in):
  Maximum pool size: 10 connections
  Minimum idle: 5 connections
  
Reuses connections instead of creating new ones:
  Request 1 → Borrow connection → Execute query → Return to pool
  Request 2 → Reuse same connection → Fast!
```

**3. Feature-Based Organization**
```
✅ Easy to extract microservices later:

Monolith (now):           Microservices (later):
backend/                  deck-service/
  deck/                     deck/
  card/                   card-service/
  study/                    card/
  user/                   study-service/
                            study/
                          user-service/
                            user/

Each feature already isolated, minimal refactoring needed
```

**4. Docker from Day One**
```
✅ Already containerized
✅ Easy to deploy anywhere (AWS, GCP, your own server)
✅ CI/CD ready (build image, push to registry, deploy)
```

### Simplified Trade-offs (Intentional)

**1. No Caching Layer**
```
Current:                   Production:
Browser → Backend → DB     Browser → Backend → Redis → DB
                                           ↑
                                      Cache frequently
                                      accessed data
                                      
When to add:
- Database queries slow (>100ms)
- Same data requested repeatedly
- Read-heavy workload

Libraries: Spring Cache + Redis
```

**2. No Pagination**
```
Current:                   Production:
GET /api/decks             GET /api/decks?page=0&size=20
→ Returns ALL decks        → Returns 20 decks + total count

When to add:
- Users have >50 decks
- API responses >1MB
- Slow list endpoints

Implementation: Spring Data's Pageable
```

**3. Simplified Security**
```
Current:                   Production:
permitAll()                authenticated() + JWT filter
X-User-Id header           Extract user from token
Manual user passing        @AuthenticationPrincipal

When to add:
- Public deployment
- Real user data
- Before launching

Add: JwtAuthenticationFilter
```

**4. No File Uploads (Images on Cards)**
```
Current: Text only         Production: Upload to S3/Cloud Storage
                           Store URL in database
                           
When to add:
- Users want images on cards
- Flashcards with diagrams

Libraries: Spring Multipart + AWS S3 SDK
```

**5. No Full-Text Search**
```
Current:                   Production:
WHERE name LIKE '%java%'   ElasticSearch / PostgreSQL full-text search
                           
When to add:
- 1000+ cards
- Complex search needs (fuzzy matching, relevance)
- Search feels slow

PostgreSQL has good full-text search built-in
```

### Path to Production Checklist

**Infrastructure**:
- [ ] Add environment-specific configs (dev, staging, prod)
- [ ] Set up CI/CD pipeline (GitHub Actions, CircleCI)
- [ ] Configure cloud provider (AWS ECS, Google Cloud Run, etc.)
- [ ] Set up database backups (automated, daily)
- [ ] Add monitoring (Prometheus, Grafana, CloudWatch)
- [ ] Configure logging aggregation (ELK stack, CloudWatch Logs)
- [ ] Set up SSL/TLS certificates (Let's Encrypt, AWS ACM)
- [ ] Domain name and DNS configuration

**Security**:
- [ ] Implement JWT authentication filter
- [ ] Add rate limiting (prevent abuse)
- [ ] Enable HTTPS only (redirect HTTP → HTTPS)
- [ ] Set up CORS for production domain only
- [ ] Add input sanitization (prevent XSS)
- [ ] Configure security headers (CSP, X-Frame-Options, etc.)
- [ ] Rotate JWT secrets regularly
- [ ] Add password reset flow (email verification)
- [ ] Implement account lockout after failed login attempts
- [ ] Add CAPTCHA to registration (prevent bots)

**Database**:
- [ ] Add database migrations (Flyway or Liquibase)
- [ ] Create database indexes for performance
- [ ] Set up read replicas (if needed)
- [ ] Configure connection pool tuning
- [ ] Add database monitoring
- [ ] Plan backup/restore procedures
- [ ] Test disaster recovery

**Application**:
- [ ] Add comprehensive error handling
- [ ] Implement request/response logging
- [ ] Add health check endpoints (/actuator/health)
- [ ] Configure graceful shutdown
- [ ] Add metrics collection (response times, error rates)
- [ ] Implement feature flags (turn features on/off without deploy)
- [ ] Add API versioning (/api/v1/, /api/v2/)

**Testing**:
- [ ] Unit tests for services (JUnit + Mockito)
- [ ] Integration tests for APIs (Spring Boot Test)
- [ ] Frontend component tests (React Testing Library)
- [ ] End-to-end tests (Playwright, Cypress)
- [ ] Load testing (JMeter, k6)
- [ ] Security testing (OWASP ZAP)

**Performance**:
- [ ] Add caching layer (Redis)
- [ ] Implement pagination on list endpoints
- [ ] Add database indexes
- [ ] Optimize N+1 queries
- [ ] Enable gzip compression
- [ ] Add CDN for static assets
- [ ] Implement lazy loading in frontend
- [ ] Optimize images (WebP format, responsive sizes)

**User Experience**:
- [ ] Add loading skeletons (instead of spinners)
- [ ] Implement optimistic updates
- [ ] Add error boundaries in React
- [ ] Offline support (service workers)
- [ ] Toast notifications for actions
- [ ] Keyboard shortcuts
- [ ] Dark mode support
- [ ] Accessibility (ARIA labels, keyboard navigation)

### Scaling Timeline

**Week 1-2**: Local development (current state)
**Week 3-4**: Add tests, fix bugs found
**Week 5**: Deploy to staging environment
**Week 6**: Add monitoring, logging
**Week 7**: Security hardening (JWT filter, rate limiting)
**Week 8**: Performance optimization (caching, indexes)
**Week 9**: Load testing, fix bottlenecks
**Week 10**: Production deployment

**Month 2-3**: Real users, gather feedback
- Add pagination (if users have many decks)
- Add search (if users request it)
- Image uploads (if users want visual cards)

**Month 4-6**: Scale based on metrics
- Add caching if database queries slow
- Add read replicas if database CPU high
- Add CDN if static asset loading slow
- Extract microservices if monolith too large

**Metrics to Track**:
- Response time (p50, p95, p99)
- Error rate (target: <0.1%)
- Database query time
- Concurrent users
- Memory usage
- CPU usage

---

## PART VIII: COMMON PATTERNS & BEST PRACTICES

### Naming Conventions

**Java (Backend)**:
```
Classes: PascalCase
  ✅ DeckService, UserController, CardDTO
  ❌ deckService, user_controller

Methods: camelCase (verb-first)
  ✅ getUserDecks(), createCard(), convertToDTO()
  ❌ GetUserDecks(), user_decks(), deckConversion()

Variables: camelCase (noun)
  ✅ deckId, createdDeck, userList
  ❌ DeckId, created_deck, user_list

Constants: UPPER_SNAKE_CASE
  ✅ MAX_DECK_SIZE, DEFAULT_EASE_FACTOR
  ❌ maxDeckSize, default_ease_factor

Database: snake_case
  ✅ next_review_date, ease_factor, created_at
  ❌ nextReviewDate, easeFactor, createdAt
```

**TypeScript (Frontend)**:
```
Components: PascalCase
  ✅ LoginPage, DeckList, CardDetail
  ❌ loginPage, deck_list

Functions: camelCase
  ✅ handleSubmit, getUserDecks, formatDate
  ❌ HandleSubmit, get_user_decks

Variables: camelCase
  ✅ deckId, isLoading, cardList
  ❌ DeckId, is_loading, card_list

Types/Interfaces: PascalCase
  ✅ DeckDTO, CreateCardRequest, StudySessionDTO
  ❌ deckDTO, createCardRequest

Constants: UPPER_SNAKE_CASE or camelCase
  ✅ API_URL, MAX_CARDS, apiUrl, maxCards
```

**Files**:
```
Backend: PascalCase matching class name
  ✅ DeckService.java, CardController.java
  ❌ deck-service.java, card_controller.java

Frontend: 
  - Components: PascalCase or kebab-case
    ✅ LoginPage.tsx, login-page.tsx
  - Utilities: camelCase or kebab-case
    ✅ apiClient.ts, api-client.ts
```

### File Organization

**Backend (Feature-Based)**:
```
com.peanuts.anki/
  deck/
    Deck.java              (Entity)
    DeckRepository.java    (Data access)
    DeckService.java       (Business logic)
    DeckController.java    (HTTP endpoints)
    dto/
      DeckDTO.java
      CreateDeckRequest.java
      UpdateDeckRequest.java
  card/
    Card.java
    CardRepository.java
    CardService.java
    CardController.java
    dto/
      CardDTO.java
      ...
  config/
    SecurityConfig.java
    WebConfig.java
  util/
    JwtUtil.java
```

**Benefits**:
- All related code together
- Easy to find things (look in feature folder)
- Easy to delete feature (remove folder)
- Easy to extract to microservice

**Alternative (Layer-Based)** - We avoided:
```
com.peanuts.anki/
  entity/
    Deck.java
    Card.java
    User.java
  repository/
    DeckRepository.java
    CardRepository.java
  service/
    DeckService.java
    CardService.java
  controller/
    DeckController.java
    CardController.java
```
❌ Changes require editing 4 different folders
❌ Hard to understand feature scope
❌ Tightly coupled

**Frontend (App Router)**:
```
app/
  (auth)/           (Route group, doesn't affect URL)
    login/
      page.tsx
    register/
      page.tsx
  decks/
    page.tsx        (List)
    [id]/
      page.tsx      (Detail)
      study/
        page.tsx    (Study session)
lib/
  api/
    client.ts       (Base API client)
    decks.ts        (Deck API calls)
    cards.ts        (Card API calls)
    study.ts        (Study API calls)
  types/
    deck.ts
    card.ts
    study.ts
components/        (Shared components, when needed)
  ui/
    Button.tsx
    Card.tsx
```

### DTO Pattern Rationale

**Why DTOs?**
```
Without DTO (exposing entity directly):
─────────────────────────────────────────
@GetMapping("/decks/{id}")
public Deck getDeck(@PathVariable Long id) {
    return deckRepository.findById(id);
}

Problems:
❌ Exposes internal structure (Jackson serializes ALL fields)
❌ Circular references (Deck → Cards → Deck → ...)
❌ Lazy loading errors (cards not loaded → Jackson fails)
❌ Can't change entity without breaking API
❌ Security risk (might expose sensitive fields)

With DTO (our approach):
─────────────────────────
@GetMapping("/decks/{id}")
public DeckDTO getDeck(@PathVariable Long id) {
    Deck deck = deckRepository.findById(id);
    return convertToDTO(deck);  // Explicit mapping
}

Benefits:
✅ Control exactly what's sent to client
✅ API stable even if entity changes
✅ No circular references
✅ No lazy loading issues
✅ Clear contract (DTO = API response shape)
```

**DTO Conversion Patterns**:
```java
// Simple manual mapping (our approach)
private DeckDTO convertToDTO(Deck deck) {
    return DeckDTO.builder()
        .id(deck.getId())
        .name(deck.getName())
        .createdAt(deck.getCreatedAt())
        .updatedAt(deck.getUpdatedAt())
        .build();
}

// Alternative: MapStruct (for larger projects)
@Mapper
interface DeckMapper {
    DeckDTO toDTO(Deck deck);
    Deck toEntity(CreateDeckRequest request);
}
```

### Error Message Guidelines

**Good Error Messages**:
```
✅ "Deck not found with id: 123"
✅ "Front text is required"
✅ "Username already exists"
✅ "Quality must be between 0 and 5, got: 7"

❌ "Error occurred"
❌ "Invalid input"
❌ "null"
❌ "Internal server error" (for user-facing errors)
```

**Principles**:
1. **Specific**: Say what's wrong
2. **Actionable**: Tell user how to fix
3. **Contextual**: Include relevant values
4. **User-friendly**: No stack traces in API responses

**Error Response Format**:
```json
{
  "error": "Front text is required",
  "field": "front",
  "timestamp": "2025-10-25T10:30:00",
  "path": "/api/decks/123/cards"
}
```

### Configuration Management

**Environment Variables > Hardcoded Values**:
```java
// ❌ Bad: Hardcoded
String dbUrl = "jdbc:postgresql://localhost:5432/anki_db";

// ✅ Good: Environment variable
@Value("${spring.datasource.url}")
private String dbUrl;
```

**Configuration Hierarchy**:
```
1. Environment variables (highest priority)
2. application-{profile}.properties
3. application.properties
4. Default values (lowest priority)

Example:
  spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/anki_db}
                         └─ env var ───────────────┘ └─ default value ──────────────────────┘
```

**Frontend Configuration**:
```typescript
// ✅ Good: Environment variable with fallback
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// ❌ Bad: Hardcoded
const API_URL = 'http://localhost:8080';
```

### Logging Best Practices

**What to Log**:
```java
// ✅ Log important events
log.info("User {} created deck: {}", userId, deck.getName());
log.info("Study session started for deck {}", deckId);

// ✅ Log errors with context
log.error("Failed to create card for deck {}: {}", deckId, e.getMessage(), e);

// ❌ Don't log everything
log.debug("Entering method getUserDecks");  // Too verbose
log.debug("deckId: {}", deckId);            // Clutter
```

**Log Levels**:
```
ERROR: Something failed, needs attention
WARN:  Something unexpected, but recoverable
INFO:  Important business events
DEBUG: Detailed troubleshooting info (disabled in prod)
TRACE: Very detailed (disabled in prod)
```

**Logging Sensitive Data**:
```java
// ❌ Never log passwords
log.info("User login: {}", password);

// ❌ Never log full JWT tokens
log.info("Token: {}", token);

// ✅ Log usernames (not sensitive in our app)
log.info("User {} logged in", username);

// ✅ Log IDs
log.info("Created card {} in deck {}", cardId, deckId);
```

---

## PART IX: DEBUGGING & TROUBLESHOOTING

### Common Issues & Solutions

**1. Frontend Can't Connect to Backend**

**Symptom**:
```
Failed to fetch
TypeError: NetworkError when attempting to fetch resource
```

**Checklist**:
```bash
# Is backend running?
docker compose ps
# Should show: anki-backend (healthy)

# Is backend accessible?
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}

# Check backend logs
docker compose logs backend --tail=50

# Is NEXT_PUBLIC_API_URL correct?
# In docker-compose.yml, should be:
NEXT_PUBLIC_API_URL: http://localhost:8080
# NOT: http://backend:8080 (browser can't resolve)
```

**2. CORS Errors**

**Symptom**:
```
Access to fetch at 'http://localhost:8080/api/decks' from origin 
'http://localhost:3000' has been blocked by CORS policy
```

**Fix**:
```bash
# Check WebConfig.java exists and has:
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

# Restart backend after adding CORS config
docker compose restart backend
```

**3. Database Connection Failed**

**Symptom**:
```
org.postgresql.util.PSQLException: Connection refused
```

**Checklist**:
```bash
# Is postgres running?
docker compose ps postgres
# Should show: anki-postgres (healthy)

# Check postgres logs
docker compose logs postgres

# Is backend waiting for postgres?
# In docker-compose.yml:
depends_on:
  postgres:
    condition: service_healthy  # ← Important

# Try connecting manually
docker compose exec postgres psql -U anki_user anki_db
# Should open postgres shell
```

**4. Hot Reload Not Working**

**Backend**:
```bash
# Check volume mount exists
docker compose config | grep -A 3 "backend:" | grep volumes
# Should show: - ./backend:/app

# Check gradle command
docker compose config | grep -A 5 "backend:" | grep command
# Should show: ./gradlew bootRun --continuous

# Restart with rebuild
docker compose up --build -d backend
```

**Frontend**:
```bash
# Check volume mount
docker compose exec frontend ls -la /app
# Should show your source files

# Clear Next.js cache
docker compose exec frontend rm -rf .next
docker compose restart frontend
```

**5. 403 Forbidden on All Endpoints**

**Symptom**:
```
POST /api/decks → 403 Forbidden
```

**Fix**:
```java
// Check SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()  // ← Should be permitAll for now
);

// NOT: .anyRequest().authenticated()
```

**6. JSON Parse Error on DELETE**

**Symptom**:
```
Failed to execute 'json' on 'Response': Unexpected end of JSON input
```

**Fix**:
```typescript
// In lib/api/client.ts, check for empty response:
if (response.status === 204 || response.headers.get('content-length') === '0') {
  return undefined as any;
}
return response.json();
```

**7. TypeScript Errors in Frontend**

**Symptom**:
```
Property 'frnt' does not exist on type 'CardDTO'
```

**Fix**:
```typescript
// Check spelling matches backend DTO
interface CardDTO {
  front: string;  // ← Must match Java record field name
  back: string;
}

// Check types match
interface CardDTO {
  id: number;           // Java: Long
  easeFactor: number;   // Java: Double
  createdAt: string;    // Java: LocalDateTime
}
```

### Debugging Tools

**Backend**:
```bash
# View all logs
docker compose logs -f backend

# Filter for errors
docker compose logs backend | grep ERROR

# Execute commands in container
docker compose exec backend ./gradlew test
docker compose exec backend sh  # Open shell

# Check environment variables
docker compose exec backend env | grep SPRING
```

**Frontend**:
```bash
# View Next.js logs
docker compose logs -f frontend

# Check build errors
docker compose exec frontend npm run build

# Check TypeScript errors
docker compose exec frontend npx tsc --noEmit
```

**Database**:
```bash
# Open postgres shell
docker compose exec postgres psql -U anki_user anki_db

# Run queries
SELECT * FROM users;
SELECT * FROM decks WHERE user_id = 1;
SELECT * FROM cards WHERE deck_id = 1 ORDER BY next_review_date;

# Check indexes
\d cards  # Show table structure and indexes
```

**Network**:
```bash
# Check what's listening on ports
lsof -i :8080  # Backend
lsof -i :3000  # Frontend
lsof -i :5432  # Postgres

# Kill process on port
kill -9 $(lsof -t -i:8080)

# Test endpoint with curl
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"test123"}'
```

**Browser DevTools**:
```
Network Tab:
  - Check request URL (correct endpoint?)
  - Check request headers (Content-Type, X-User-Id?)
  - Check request body (valid JSON?)
  - Check response status (200, 400, 500?)
  - Check response body (error message?)

Console Tab:
  - Check for JavaScript errors
  - Check API response data
  - console.log() debugging

Application Tab:
  - Check localStorage (userId, token)
  - Clear storage to test fresh state
```

---

## PART X: NEXT STEPS & LEARNING PATH

### Feature Ideas to Implement

**Easy (1-2 hours each)**:
- [ ] Edit deck name
- [ ] Edit card (front/back)
- [ ] Card count display on deck list
- [ ] Sort decks by name/created date
- [ ] Delete account
- [ ] Logout functionality
- [ ] Show cards due today count
- [ ] Basic statistics (total cards, cards reviewed today)

**Medium (4-8 hours each)**:
- [ ] Pagination for deck list
- [ ] Search decks by name
- [ ] Filter cards (all, due, learned)
- [ ] Tag system for cards/decks
- [ ] Import/export decks (JSON format)
- [ ] Bulk card creation (CSV upload)
- [ ] Undo last review
- [ ] Study session statistics (accuracy, time spent)
- [ ] Email verification on registration
- [ ] Password reset flow

**Hard (1-2 days each)**:
- [ ] JWT authentication filter (replace permitAll)
- [ ] Image upload for cards (store in S3)
- [ ] Audio pronunciation for cards
- [ ] Shared decks (public deck library)
- [ ] Spaced repetition algorithm v2 (FSRS, SM-17)
- [ ] Mobile app (React Native, Flutter)
- [ ] Offline mode (service workers, IndexedDB)
- [ ] Real-time study sessions (WebSocket, multiple users)
- [ ] Advanced statistics dashboard (charts, progress tracking)
- [ ] AI-generated cards (OpenAI API)

### Concepts to Learn Deeper

**Backend**:
- [ ] Spring Security (authentication, authorization)
- [ ] Spring Data JPA advanced (specifications, projections)
- [ ] Database migrations (Flyway, Liquibase)
- [ ] Testing (JUnit, Mockito, TestContainers)
- [ ] Caching (Redis, Spring Cache)
- [ ] Message queues (RabbitMQ, Kafka)
- [ ] Observability (Prometheus, Grafana, distributed tracing)
- [ ] API documentation (OpenAPI/Swagger)

**Frontend**:
- [ ] React Server Components (Next.js 14+)
- [ ] State management (Zustand, Redux Toolkit)
- [ ] Testing (Jest, React Testing Library, Playwright)
- [ ] Performance optimization (code splitting, lazy loading)
- [ ] Accessibility (WCAG guidelines, screen readers)
- [ ] PWA (progressive web apps)
- [ ] TypeScript advanced (generics, utility types)

**DevOps**:
- [ ] CI/CD (GitHub Actions, Jenkins, CircleCI)
- [ ] Cloud deployment (AWS, GCP, Azure)
- [ ] Kubernetes (container orchestration)
- [ ] Infrastructure as Code (Terraform, CloudFormation)
- [ ] Monitoring (Datadog, New Relic, CloudWatch)
- [ ] Log aggregation (ELK stack, Splunk)

**Database**:
- [ ] Database indexing strategies
- [ ] Query optimization (EXPLAIN, query plans)
- [ ] Database replication (read replicas, multi-master)
- [ ] Sharding strategies
- [ ] NoSQL databases (MongoDB, DynamoDB)
- [ ] Database transactions (isolation levels, ACID)

### Learning Resources

**Spring Boot**:
- Official Spring Guides: https://spring.io/guides
- Baeldung Spring Tutorials: https://www.baeldung.com/spring-tutorial
- Book: "Spring Boot in Action" by Craig Walls

**Next.js**:
- Official Next.js Learn: https://nextjs.org/learn
- Next.js Documentation: https://nextjs.org/docs
- Book: "Next.js in Action" (coming soon)

**Docker**:
- Docker Official Tutorial: https://docs.docker.com/get-started/
- Book: "Docker Deep Dive" by Nigel Poulton

**System Design**:
- Book: "Designing Data-Intensive Applications" by Martin Kleppmann
- System Design Primer: https://github.com/donnemartin/system-design-primer

**Spaced Repetition**:
- SuperMemo article: https://supermemo.com/en/blog/application-of-a-computer-to-improve-the-results-obtained-in-working-with-the-supermemo-method
- FSRS algorithm: https://github.com/open-spaced-repetition/fsrs4anki

### Exercises to Reinforce Learning

**1. Add a Feature End-to-End**
- Pick: "Edit card" functionality
- Steps:
  1. Add PUT endpoint in CardController
  2. Implement updateCard() in CardService
  3. Add update form in frontend
  4. Test the full flow
  5. Handle errors (card not found, validation)
- **Learning**: Full-stack flow, CRUD operations

**2. Refactor for Testability**
- Write unit tests for StudyService.applySpacedRepetition()
- Mock CardRepository
- Test all quality levels (0-5)
- Test edge cases (repetitions=0, easeFactor boundaries)
- **Learning**: Unit testing, mocking, TDD

**3. Add Pagination**
- Modify GET /api/decks to accept ?page=0&size=20
- Use Spring Data's Pageable
- Update frontend to show pagination controls
- **Learning**: Pagination pattern, API design

**4. Implement JWT Filter**
- Create JwtAuthenticationFilter
- Extract user ID from token
- Set SecurityContext
- Change permitAll() → authenticated()
- **Learning**: Spring Security, authentication flow

**5. Optimize Database Query**
- Find N+1 query (enable Hibernate SQL logging)
- Fix with join fetch or batch loading
- Measure performance improvement
- **Learning**: JPA/Hibernate, query optimization

**6. Deploy to Cloud**
- Choose platform (AWS, Heroku, Render)
- Set up production database (RDS, Postgres as a service)
- Configure environment variables
- Set up CI/CD to auto-deploy on push
- **Learning**: Cloud deployment, DevOps

---

## APPENDIX

### A. Complete API Reference

**Authentication**:
```
POST /api/auth/register
  Body: { username, email, password }
  Response: { userId, token, username }

POST /api/auth/login
  Body: { username, password }
  Response: { userId, token, username }
```

**Decks**:
```
GET /api/users/{userId}/decks
  Response: [{ id, name, createdAt, updatedAt }]

GET /api/decks/{deckId}
  Response: { id, name, createdAt, updatedAt }

POST /api/users/{userId}/decks
  Body: { name, description? }
  Response: { id, name, createdAt, updatedAt }

PUT /api/decks/{deckId}
  Body: { name, description? }
  Response: { id, name, createdAt, updatedAt }

DELETE /api/decks/{deckId}
  Response: 204 No Content
```

**Cards**:
```
GET /api/decks/{deckId}/cards
  Response: [{ id, front, back, repetitions, easeFactor, interval, nextReviewDate, ... }]

POST /api/decks/{deckId}/cards
  Body: { front, back }
  Response: { id, front, back, repetitions, ... }

PUT /api/decks/{deckId}/cards/{cardId}
  Body: { front, back }
  Response: { id, front, back, ... }

DELETE /api/decks/{deckId}/cards/{cardId}
  Response: 204 No Content
```

**Study**:
```
POST /api/study/decks/{deckId}/start
  Response: { deckId, deckName, totalCards, cardsToReview: [...] }

POST /api/study/cards/{cardId}/review
  Body: { quality }  (0-5)
  Response: { success, message, nextReviewDate }
```

### B. Database Schema

```sql
-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Decks
CREATE TABLE decks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Cards
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    deck_id BIGINT NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    
    -- SM-2 algorithm fields
    repetitions INTEGER NOT NULL DEFAULT 0,
    ease_factor DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval INTEGER NOT NULL DEFAULT 1,
    next_review_date TIMESTAMP NOT NULL DEFAULT NOW(),
    
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes (recommended for production)
CREATE INDEX idx_decks_user_id ON decks(user_id);
CREATE INDEX idx_cards_deck_id ON cards(deck_id);
CREATE INDEX idx_cards_next_review ON cards(deck_id, next_review_date);
```

### C. Environment Variables Reference

**.env (docker-compose)**:
```bash
# Database
POSTGRES_DB=anki_db
POSTGRES_USER=anki_user
POSTGRES_PASSWORD=anki_pass_dev
POSTGRES_PORT=5432

# Backend
BACKEND_PORT=8080
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# Frontend
FRONTEND_PORT=3000
```

**application.properties (Spring Boot)**:
```properties
# Database
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}

# Logging
logging.level.root=INFO
logging.level.com.peanuts.anki=DEBUG
```

**.env.local (Next.js)**:
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### D. SM-2 Algorithm Reference

**Input**: Quality rating (0-5)
**Output**: Updated card with new interval and ease factor

**Algorithm**:
```
If quality < 3 (failed):
  repetitions = 0
  interval = 1
else (passed):
  repetitions = repetitions + 1
  
  if repetitions == 0:
    interval = 1
  else if repetitions == 1:
    interval = 6
  else:
    interval = round(previous_interval * ease_factor)

ease_factor = ease_factor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
ease_factor = max(1.3, ease_factor)  # Floor at 1.3

next_review_date = today + interval days
```

**Example Progression** (all quality 5):
```
Review 0: interval = 1 day,  next review in 1 day
Review 1: interval = 6 days, next review in 6 days
Review 2: interval = 15 days (6 * 2.6), next review in 15 days
Review 3: interval = 39 days (15 * 2.6)
Review 4: interval = 101 days (39 * 2.6)
...
```

### E. Useful Docker Commands Cheat Sheet

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f [service]

# Stop all services
docker compose down

# Stop and remove volumes (fresh start)
docker compose down -v

# Rebuild and restart
docker compose up --build -d

# Execute command in container
docker compose exec [service] [command]

# List running containers
docker compose ps

# View resource usage
docker stats

# Remove all unused images/volumes
docker system prune -a --volumes

# Backup database
docker compose exec postgres pg_dump -U anki_user anki_db > backup.sql

# Restore database
docker compose exec -T postgres psql -U anki_user anki_db < backup.sql
```

---

## CONCLUSION

You've now seen a complete, production-aware full-stack application built from scratch. The key takeaways:

1. **Start Simple, Plan for Scale**: We built the simplest version that works, but made decisions that won't block us later
2. **Feature-Based Organization**: Keeps code maintainable as the project grows
3. **Type Safety Across Boundaries**: TypeScript + Java types prevent entire classes of bugs
4. **Docker from Day One**: Eliminates environment issues, ready to deploy
5. **Learning by Doing**: This project teaches real software engineering, not just coding

**Remember**: 
- Every production system started as a simple weekend project
- Optimization is important, but premature optimization wastes time
- Good architecture enables iteration, perfect architecture prevents shipping
- Build, learn, iterate, repeat

Now go build something! 🚀

---

**Document Version**: 1.0
**Last Updated**: October 25, 2025
**Project**: peanuts-anki-spring
**Author**: Built with Claude Code

