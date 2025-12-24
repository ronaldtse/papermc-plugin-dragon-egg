#!/bin/bash

# Build script for Dragon Egg Lightning plugin

set -e

echo "================================"
echo "Building Dragon Egg Lightning Plugin"
echo "================================"

# Clean and build
echo "Running Maven clean and package..."
mvn clean package

# Check if JAR was created (with the correct filename from pom.xml finalName)
if [ -f "target/DragonEggLightning-1.0.0-plugin-java21.jar" ]; then
    echo "✓ Plugin JAR created successfully!"
    echo "  Location: target/DragonEggLightning-1.0.0-plugin-java21.jar"
else
    echo "✗ Failed to create plugin JAR"
    echo "Checking for alternative JAR files..."
    ls -la target/
    exit 1
fi

echo ""
echo "================================"
echo "Build Complete!"
echo "================================"
echo ""
echo "Next steps:"
echo "  1. Start server: ./start-server.sh"
echo "  2. Stop server: ./stop-server.sh"
echo "  3. View logs: docker logs -f papermc-dragonegg"
