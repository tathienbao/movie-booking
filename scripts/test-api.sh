#!/bin/bash

# Quick API Test Script
# Tests the running API without rebuilding

PORT=8080

echo "🧪 Testing Movie Booking API..."
echo ""

# Test if container is running
if ! docker ps | grep -q "movie-booking-api-local"; then
    echo "❌ Container is not running!"
    echo "   Run: ./scripts/run-local.sh"
    exit 1
fi

echo "📍 Testing: http://localhost:$PORT/api/movies"
echo ""

# Get all movies
echo "1️⃣  GET /api/movies (Get all movies)"
curl -s http://localhost:$PORT/api/movies | jq '.' || curl -s http://localhost:$PORT/api/movies
echo ""
echo ""

# Get specific movie
echo "2️⃣  GET /api/movies/1 (Get movie by ID)"
curl -s http://localhost:$PORT/api/movies/1 | jq '.' || curl -s http://localhost:$PORT/api/movies/1
echo ""
echo ""

echo "✅ API tests completed!"
echo ""
echo "More commands:"
echo "  View logs: docker logs movie-booking-api-local"
echo "  Stop:      docker stop movie-booking-api-local"
