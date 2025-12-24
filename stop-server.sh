#!/bin/bash

# Stop Paper Minecraft server

echo "================================"
echo "Stopping Paper MC Server"
echo "================================"

# Stop Docker container
docker-compose down

echo ""
echo "Server stopped successfully!"
echo ""
echo "To start again: ./start-server.sh"
