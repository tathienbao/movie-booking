#!/bin/bash

# Local Development Script
# Automatically builds and runs the Movie Booking API locally

set -e

echo "🚀 Movie Booking API - Local Development Runner"
echo "================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
IMAGE_NAME="movie-booking-api"
CONTAINER_NAME="movie-booking-api-local"
PORT=8080

# Step 1: Clean up old containers
echo "🧹 Cleaning up old containers..."
if docker ps -a | grep -q $CONTAINER_NAME; then
    echo "   Stopping old container..."
    docker stop $CONTAINER_NAME 2>/dev/null || true
    echo "   Removing old container..."
    docker rm $CONTAINER_NAME 2>/dev/null || true
    echo "   ${GREEN}✓${NC} Old container removed"
else
    echo "   No old containers found"
fi
echo ""

# Step 2: Build Maven package
echo "📦 Building Maven package..."
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "   ${GREEN}✓${NC} Maven build successful"
else
    echo "   ${RED}✗${NC} Maven build failed"
    exit 1
fi
echo ""

# Step 3: Build Docker image
echo "🐳 Building Docker image..."
docker build -t $IMAGE_NAME:latest .
if [ $? -eq 0 ]; then
    echo "   ${GREEN}✓${NC} Docker image built successfully"
else
    echo "   ${RED}✗${NC} Docker build failed"
    exit 1
fi
echo ""

# Step 4: Run container
echo "▶️  Starting container..."
docker run -d \
    -p $PORT:8080 \
    --name $CONTAINER_NAME \
    $IMAGE_NAME:latest

if [ $? -eq 0 ]; then
    echo "   ${GREEN}✓${NC} Container started"
else
    echo "   ${RED}✗${NC} Failed to start container"
    exit 1
fi
echo ""

# Step 5: Wait for application to start
echo "⏳ Waiting for application to start..."
sleep 8
echo ""

# Step 6: Test API
echo "🧪 Testing API..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/api/movies)

if [ "$RESPONSE" = "200" ]; then
    echo "   ${GREEN}✓${NC} API is responding (HTTP $RESPONSE)"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "${GREEN}✅ SUCCESS!${NC} Application is running"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📍 API URL: http://localhost:$PORT/api/movies"
    echo "📊 Container: $CONTAINER_NAME"
    echo ""
    echo "Commands:"
    echo "  View logs:    docker logs $CONTAINER_NAME"
    echo "  Stop:         docker stop $CONTAINER_NAME"
    echo "  Remove:       docker rm $CONTAINER_NAME"
    echo ""
    echo "Test the API:"
    echo "  curl http://localhost:$PORT/api/movies"
    echo ""
else
    echo "   ${RED}✗${NC} API not responding (HTTP $RESPONSE)"
    echo ""
    echo "📋 Container logs:"
    docker logs $CONTAINER_NAME
    exit 1
fi
