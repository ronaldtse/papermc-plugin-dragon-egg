#!/bin/bash

# Start Paper Minecraft server with Dragon Egg Lightning plugin

set -e

echo "================================"
echo "Starting Paper MC Server"
echo "================================"

# Check if plugin JAR exists (with the correct filename from pom.xml finalName)
if [ ! -f "target/DragonEggLightning-1.0.0-plugin-java21.jar" ]; then
    echo "✗ Plugin JAR not found!"
    echo "  Please run ./build.sh first"
    exit 1
fi

echo "✓ Plugin JAR found"

# Start Docker container
echo "Starting Docker container..."
docker-compose up -d

echo ""
echo "================================"
echo "Server Starting!"
echo "================================"
echo ""
echo "Server will be available on port 25565"
echo ""
echo "Useful commands:"
echo "  View logs:      docker logs -f papermc-dragonegg"
echo "  Server console: docker attach papermc-dragonegg"
echo "  Stop server:    ./stop-server.sh"
echo ""
echo "Waiting for server to start (this may take a minute)..."
sleep 5
docker logs papermc-dragonegg
