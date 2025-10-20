# Peanuts Anki

[![Docker Build](https://github.com/peanuts-stack/peanuts-anki-spring/actions/workflows/docker.yml/badge.svg)](https://github.com/peanuts-stack/peanuts-anki-spring/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **Peanuts Stack Project #1**: Enterprise features, peanut costs.

Anki-style spaced repetition language learning app. Built to showcase building on minimal hardware and avoiding the cloud provider trap.

## Why This Project?

Teaching developers to build weekend startups without burning money:
- **$0 to run locally** with Docker Compose
- **$5/mo in production** on a VPS
- **No vendor lock-in** - runs anywhere Docker runs
- **Real features, real code** - not a toy demo

## Tech Stack

- **Backend**: Spring Boot 3.2 + Gradle + Java 17
- **Frontend**: Next.js 14 + TypeScript + Tailwind
- **Database**: PostgreSQL 15
- **Auth**: JWT (stateless)
- **Deployment**: Docker Compose → VPS → Docker Swarm

## Project Structure
```
peanuts-anki-spring/
├── backend/          # Spring Boot API (open in IntelliJ)
├── frontend/         # Next.js app (open in VSCode)
├── scripts/          # Utility scripts
├── docs/             # Blog posts and guides
└── docker-compose.yml
```

## Quick Start
```bash
git clone git@github.com:peanuts-stack/peanuts-anki-spring.git
cd peanuts-anki-spring
scripts/run.sh
```

Visit: http://localhost:3000

**First time?** The script will create `.env` for you - update it with real values before running again.

## Evolution Stages

- [ ] **Stage 0**: Local development (`docker compose up`)
- [ ] **Stage 1**: VPS deployment ($10/mo)
- [ ] **Stage 2**: Docker Swarm (horizontal scaling)
- [ ] **Stage 3**: Multi-region (when we actually need it)

## Blog Series

1. [Building It - Weekend 0→1](#) - Coming soon
2. [Deploying to VPS - $5/mo Production](#) - Coming soon
3. [Scaling to 10K Users](#) - Coming soon
4. [Lessons Learned](#) - Coming soon

## Cost Breakdown

| Stage | Monthly Cost | Users Supported |
|-------|--------------|-----------------|
| Local Dev | $0           | N/A |
| VPS (Single) | $5           | 1-1K |
| VPS (Swarm) | $15          | 1K-10K |
| AWS Equivalent | $150+        | Same |

## Development

### Backend (IntelliJ)
```bash
cd backend
./gradlew bootRun
```

### Frontend (VSCode)
```bash
cd frontend
npm run dev
```

### Both via Docker
```bash
scripts/run.sh
```

## Features

- [ ] User authentication (JWT)
- [ ] Create and manage decks
- [ ] Add flashcards (manual)
- [ ] Spaced repetition algorithm (SM-2)
- [ ] Study sessions
- [ ] Progress tracking
- [ ] AI-generated content (OpenAI)
- [ ] Deck sharing

## Part of Peanuts Stack

This project is part of the [Peanuts Stack](https://github.com/peanuts-stack) series - building production-ready apps for peanuts. Learn to ship fast, scale smart, and avoid the cloud provider trap.

### Other Projects in the Series
- **Project #1**: Peanuts Anki (Spring Boot) - *You are here*
- **Project #2**: Coming soon (Go)
- **Project #3**: Coming soon (Django)

## Contributing

This is a learning project, but PRs welcome! Please open an issue first to discuss changes.

## License

MIT
