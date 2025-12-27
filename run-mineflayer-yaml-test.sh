#!/bin/bash

# =============================================================================
# MINEFLAYER CLIENT + YAML TEST INTEGRATION DEMO
# =============================================================================
# This script demonstrates how to run the Mineflayer client with YAML test stories
# for the DragonEggLightning plugin integration testing

set -e

echo "🎮 ========================================"
echo "🎮 MINEFLAYER + YAML INTEGRATION DEMO"
echo "🎮 ========================================"
echo ""

# Check dependencies
echo "🔍 Checking dependencies..."

# Check if Node.js is available
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is required but not installed"
    exit 1
fi

# Check if npm dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing Mineflayer dependencies..."
    npm install mineflayer mineflayer-pathfinder
fi

# Check if PaperMC server JAR exists
if [ ! -f "server-data/paper-*.jar" ]; then
    echo "❌ PaperMC server JAR not found in server-data/"
    echo "   Please ensure you have a PaperMC server JAR file"
    exit 1
fi

echo "✅ Dependencies verified"
echo ""

# Show available YAML test stories
echo "📋 Available YAML test stories:"
echo ""
ls -la src/test/resources/test-stories/*.yaml | while read line; do
    filename=$(echo "$line" | awk '{print $9}' | sed 's|src/test/resources/test-stories/||')
    echo "   📄 $filename"
done
echo ""

# Create a Mineflayer-integrated YAML test story
echo "🎯 Creating Mineflayer-integrated YAML test story..."
cat > src/test/resources/test-stories/mineflayer-integration-test.yaml << 'EOF'
story:
  name: "Mineflayer Client Integration Test"
  description: "Test DragonEggLightning plugin with real Mineflayer client"

setup:
  server:
    type: "PAPER"
    version: "1.21.8"
    online_mode: false
    rcon_enabled: true
    rcon_port: 25575
    rcon_password: "dragon123"

  players:
    - name: "mineflayer_test"
      op: true
      position: [0, 64, 0]
      items:
        - "dragon_egg 5"

steps:
  - name: "Start PaperMC Server"
    action: "start_server"
    timeout: 300

  - name: "Start Mineflayer Client"
    action: "start_mineflayer_client"
    host: "localhost"
    port: 25565
    username: "mineflayer_test"

  - name: "Connect Mineflayer Player"
    action: "connect_player"
    player: "mineflayer_test"

  - name: "Give Dragon Eggs via Mineflayer"
    action: "mineflayer_give_item"
    player: "mineflayer_test"
    item: "dragon_egg"
    count: 3

  - name: "Equip Dragon Egg in Offhand"
    action: "mineflayer_equip_item"
    player: "mineflayer_test"
    item: "dragon_egg"
    slot: "offhand"

  - name: "Execute Lightning Ability"
    action: "mineflayer_command"
    player: "mineflayer_test"
    command: "ability 1"

  - name: "Spawn Test Entity"
    action: "spawn_entity"
    entity_type: "zombie"
    name: "TestZombie"
    position: [5, 64, 5]

  - name: "Check Entity Health"
    action: "get_entity_info"
    entity_name: "TestZombie"
    expected_contains: "Health: 14"  # Should be 20 - 6 hearts damage

  - name: "Stop Mineflayer Client"
    action: "stop_mineflayer_client"
    player: "mineflayer_test"

cleanup:
  action: "stop_server"

expected_results:
  mineflayer_connected: true
  lightning_damage_applied: true
  plugin_responds: true
EOF

echo "✅ Created mineflayer-integration-test.yaml"
echo ""

# Method 1: Run YAML tests with Maven (existing framework)
echo "🔧 Method 1: Running YAML tests with Maven..."
echo ""
echo "Command: mvn test -Dtest=YamlDslTest"
echo ""
echo "This will:"
echo "  📋 Load YAML test stories from src/test/resources/test-stories/"
echo "  🖥️  Start PaperMC server"
echo "  🎮 Execute test steps defined in YAML"
echo "  ✅ Validate expected results"
echo ""

# Method 2: Run Mineflayer client standalone
echo "🎮 Method 2: Running Mineflayer client standalone..."
echo ""
echo "Command: node mineflayer-client.js <host> <port> <username>"
echo ""
echo "Examples:"
echo "  node mineflayer-client.js localhost 25565 mineflayer_test"
echo "  node mineflayer-client.js 192.168.1.100 25565 test_player"
echo ""

# Method 3: Combined YAML + Mineflayer workflow
echo "🚀 Method 3: Combined YAML + Mineflayer workflow..."
echo ""
echo "Step 1: Start PaperMC server"
echo "  ./start-server.sh"
echo ""
echo "Step 2: Run YAML test with Mineflayer integration"
echo "  mvn test -Dtest=YamlDslTest#testRunMineflayerStory"
echo ""
echo "Step 3: Or run specific YAML story"
echo "  mvn test -Dtest=YamlDslTest#testRunYamlStory -Dstory=mineflayer-integration-test.yaml"
echo ""

# Live demonstration
echo "🎬 LIVE DEMONSTRATION:"
echo ""

# Start server in background
echo "🖥️  Starting PaperMC server..."
timeout 60s java -jar server-data/paper-*.jar --nogui > server.log 2>&1 &
SERVER_PID=$!
sleep 10

# Wait for server to be ready
echo "⏳ Waiting for server to be ready..."
for i in {1..30}; do
    if nc -z localhost 25565 2>/dev/null; then
        echo "✅ Server is ready!"
        break
    fi
    sleep 2
done

# Run Mineflayer client
echo "🎮 Starting Mineflayer client..."
echo ""
echo "Command being executed:"
echo "  node mineflayer-client.js localhost 25565 mineflayer_test"
echo ""
echo "Expected output:"
echo "  [Mineflayer] mineflayer_test logged in to localhost:25565"
echo "  [Mineflayer] Connected successfully"
echo "  [Mineflayer] Giving 3 dragon_egg to mineflayer_test"
echo "  [Mineflayer] Executing command: give @s dragon_egg 3"
echo "  [Mineflayer] Equipping dragon_egg to offhand"
echo "  [Mineflayer] Executing command: replaceitem entity @s offhand dragon_egg"
echo "  [Mineflayer] Executing command: ability 1"
echo "  [Mineflayer] Disconnected mineflayer_test"
echo ""
echo "Actual output:"
node mineflayer-client.js localhost 25565 mineflayer_test

# Stop server
echo ""
echo "🧹 Stopping server..."
kill $SERVER_PID 2>/dev/null || true
sleep 2

echo ""
echo "🎉 ========================================"
echo "🎉 MINEFLAYER + YAML INTEGRATION COMPLETE"
echo "🎉 ========================================"
echo ""
echo "Summary of what you can do:"
echo ""
echo "1. 📋 YAML Test Stories: Edit files in src/test/resources/test-stories/"
echo "   - lightning-ability-test.yaml"
echo "   - plugin-version-test.yaml"
echo "   - mineflayer-integration-test.yaml (newly created)"
echo ""
echo "2. 🎮 Run Mineflayer Client:"
echo "   node mineflayer-client.js <host> <port> <username>"
echo ""
echo "3. 🧪 Run YAML Tests:"
echo "   mvn test -Dtest=YamlDslTest"
echo ""
echo "4. 🚀 Full Integration:"
echo "   Start server + run YAML tests with real client"
echo ""
echo "Files created/modified:"
echo "  ✅ run-mineflayer-yaml-test.sh (this script)"
echo "  ✅ mineflayer-client.js (Mineflayer client)"
echo "  ✅ src/test/resources/test-stories/mineflayer-integration-test.yaml"
echo ""
echo "The integration is now complete and ready for use!"
