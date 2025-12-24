#!/bin/bash

# Test script for Dragon Egg Lightning plugin
# This script automates basic functionality testing

set -e

echo "================================"
echo "Testing Dragon Egg Lightning Plugin"
echo "================================"

# Check if server is running
if ! docker ps | grep -q papermc-dragonegg; then
    echo "✗ Server is not running!"
    echo "  Please run ./start-server.sh first"
    exit 1
fi

echo "✓ Server is running"

# Wait for server to be fully started
echo "Waiting for server to be ready..."
sleep 10

# Run unit tests
echo "Running unit tests..."
mvn test

if [ $? -eq 0 ]; then
    echo "✓ Unit tests passed!"
else
    echo "✗ Unit tests failed!"
    exit 1
fi

echo ""
echo "================================"
echo "Manual Testing Instructions"
echo "================================"
echo ""
echo "1. Connect to the Minecraft server (localhost:25565)"
echo ""
echo "2. Get a Dragon Egg:"
echo "   /give @p minecraft:dragon_egg"
echo ""
echo "3. Hold the Dragon Egg in your offhand (F key)"
echo ""
echo "4. Test the ability:"
echo "   /ability 1"
echo ""
echo "5. Verify the following:"
echo "   ✓ Purple lightning strikes appear"
echo "   ✓ Lightning deals 1.5 hearts (3 HP) per strike"
echo "   ✓ Three strikes occur with 0.5s intervals"
echo "   ✓ HUD shows cooldown (59s, 58s, etc.)"
echo "   ✓ HUD shows 'Lightning ready' when ready"
echo "   ✓ Ability fails without Dragon Egg in offhand"
echo "   ✓ Ability fails on cooldown"
echo ""
echo "6. Test edge cases:"
echo "   ✓ No targets in range - should show error message"
echo "   ✓ Switch items during casting - should cancel"
echo "   ✓ Dead targets - should cancel strikes"
echo ""
echo "7. Stop the server when done:"
echo "   ./stop-server.sh"
echo ""
echo "================================"
echo "Testing Complete!"
echo "================================"
