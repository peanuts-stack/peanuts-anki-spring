#!/bin/bash

# Simple run script for local development
echo "🚀 Starting Peanuts Anki..."

# Check if .env exists
if [ ! -f .env ]; then
    echo "❌ No .env file found!"
    echo "📝 Creating .env from .env.example..."
    cp .env.example .env
    echo ""
    echo "⚠️  IMPORTANT: Update .env with your actual values before running."
    echo "   At minimum, change:"
    echo "   - DB_PASSWORD"
    echo "   - JWT_SECRET"
    echo ""
    echo "Then run scripts/run.sh again"
    exit 1
fi

# Start Docker Compose
docker compose up
