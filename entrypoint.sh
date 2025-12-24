#!/bin/bash

set -e

# Function to generate UUID (with fallbacks)
generate_uuid() {
    # Try uuidgen first
    if command -v uuidgen >/dev/null 2>&1; then
        uuidgen
    # Try /proc/sys/kernel/random/uuid (Linux)
    elif [ -f /proc/sys/kernel/random/uuid ]; then
        cat /proc/sys/kernel/random/uuid
    # Use openssl as fallback
    elif command -v openssl >/dev/null 2>&1; then
        openssl rand -hex 16 | sed 's/\(.\{8\}\)\(.\{4\}\)\(.\{4\}\)\(.\{4\}\)\(.\{12\}\)/\1-\2-\3-\4-\5/'
    # Final fallback: generate simple UUID-like string
    else
        # Generate a simple UUID v4 equivalent
        local timestamp=$(date +%s)
        local random=$(od -An -t x -N 8 /dev/urandom | tr -d ' ')
        echo "${timestamp:0:8}-${random:0:4}-4${random:4:3}-a${random:7:3}-${timestamp:8}${random:11}"
    fi
}

# Function to check if jq is available
check_jq() {
    if command -v jq >/dev/null 2>&1; then
        return 0
    else
        echo "⚠️  jq not found, will use manual JSON editing"
        return 1
    fi
}

echo "================================"
echo "Dragon Egg Lightning Plugin Server"
echo "Initializing..."
echo "================================"

# Ensure plugins directory exists
mkdir -p /data/plugins

# List all files in plugins directory for debugging
echo "📂 Checking plugins directory..."
ls -la /data/plugins/

# Check if plugins directory is empty (volume mount might have cleared it)
if [ ! "$(ls -A /data/plugins/)" ]; then
    echo "⚠️  Plugins directory is empty, checking for plugin in base image..."

    # Check multiple possible locations where the plugin might be in the base image
    PLUGIN_FOUND_IN_IMAGE=false

    if [ -f "/opt/minecraft/plugins/DragonEggLightning.jar" ]; then
        echo "✅ Found plugin in /opt/minecraft/plugins/"
        cp /opt/minecraft/plugins/DragonEggLightning.jar /data/plugins/
        PLUGIN_FOUND_IN_IMAGE=true
    elif [ -f "/plugins/DragonEggLightning.jar" ]; then
        echo "✅ Found plugin in /plugins/"
        cp /plugins/DragonEggLightning.jar /data/plugins/
        PLUGIN_FOUND_IN_IMAGE=true
    elif [ -f "/data/plugins-backup/DragonEggLightning.jar" ]; then
        echo "✅ Found plugin in /data/plugins-backup/"
        cp /data/plugins-backup/DragonEggLightning.jar /data/plugins/
        PLUGIN_FOUND_IN_IMAGE=true
    fi

    if [ "$PLUGIN_FOUND_IN_IMAGE" = false ]; then
        echo "❌ Plugin not found in base image"
        echo "Checking for any JAR files in common locations..."
        find /opt/minecraft -name "*.jar" -type f 2>/dev/null || true
        find / -name "DragonEggLightning*.jar" -type f 2>/dev/null || true
    else
        echo "✅ Plugin copied to volume successfully"
    fi
fi

# Ensure ops.json exists and is valid
if [ ! -f /data/ops.json ]; then
    echo "Creating fresh ops.json..."
    echo "[]" > /data/ops.json
fi

# Check if ops.json is valid JSON
if ! jq empty /data/ops.json 2>/dev/null; then
    echo "⚠️  ops.json is corrupted, recreating..."
    echo "[]" > /data/ops.json
fi

# Set up admin user only if not already present
if [ -n "${ADMIN_USERNAME}" ]; then
    echo "Setting up admin user: ${ADMIN_USERNAME}"

    if check_jq; then
        # Check if user already exists
        if jq -r ".[] | select(.name == \"${ADMIN_USERNAME}\") | .name" /data/ops.json 2>/dev/null | grep -q "${ADMIN_USERNAME}"; then
            echo "✅ Admin user '${ADMIN_USERNAME}' already exists"
        else
            # Generate UUID and add user
            ADMIN_UUID=$(generate_uuid)
            jq --arg name "${ADMIN_USERNAME}" --arg uuid "${ADMIN_UUID}" \
               '. += [{"name": $name, "uuid": $uuid, "level": 4, "bypassesPlayerLimit": false}]' \
               /data/ops.json > /data/ops.json.tmp
            mv /data/ops.json.tmp /data/ops.json
            echo "✅ Admin user '${ADMIN_USERNAME}' added to ops.json"
        fi
    else
        # Manual JSON editing without jq
        if ! grep -q "${ADMIN_USERNAME}" /data/ops.json 2>/dev/null; then
            ADMIN_UUID=$(generate_uuid)
            echo '[{"name":"'"${ADMIN_USERNAME}"'","uuid":"'"${ADMIN_UUID}"'","level":4,"bypassesPlayerLimit":false}]' > /data/ops.json
            echo "✅ Admin user '${ADMIN_USERNAME}' added to ops.json (manual method)"
        else
            echo "✅ Admin user '${ADMIN_USERNAME}' already exists"
        fi
    fi
elif [ ! -s /data/ops.json ] || [ "$(jq length /data/ops.json 2>/dev/null || echo 0)" = "0" ]; then
    # Set up default admin if ops.json is empty
    echo "Setting up default admin user: admin"

    ADMIN_UUID=$(generate_uuid)
    echo '[{"name":"admin","uuid":"'"${ADMIN_UUID}"'","level":4,"bypassesPlayerLimit":false}]' > /data/ops.json
    echo "✅ Default admin user 'admin' added to ops.json"
fi

# Ensure server.properties exists
if [ ! -f /data/server.properties ]; then
    echo "Creating server.properties..."

    cat > /data/server.properties << EOF
server-name=${SERVER_NAME:-Dragon Egg Lightning Server}
gamemode=survival
difficulty=normal
max-players=10
online-mode=false
white-list=false
pvp=true
spawn-protection=16
motd=${MOTD:-Dragon Egg Lightning Plugin Server}
view-distance=10
simulation-distance=10
enable-command-block=false
enable-rcon=true
rcon.port=${RCON_PORT:-25575}
rcon.password=${RCON_PASSWORD:-dragon123}
EOF
    echo "✅ server.properties created"
else
    echo "✅ server.properties already exists"
fi

# Check memory
MEMORY=${MEMORYSIZE:-2G}
echo "✅ Memory allocation: $MEMORY"

# Final plugin check
echo "🔍 Final check for Dragon Egg Lightning plugin..."
PLUGIN_FOUND=false

if [ -f /data/plugins/DragonEggLightning.jar ]; then
    echo "✅ Dragon Egg Lightning plugin found: DragonEggLightning.jar"
    echo "   Size: $(du -h /data/plugins/DragonEggLightning.jar | cut -f1)"
    PLUGIN_FOUND=true
fi

if [ "$PLUGIN_FOUND" = false ]; then
    echo "❌ Dragon Egg Lightning plugin NOT FOUND!"
    echo "Available files in /data/plugins/:"
    ls -la /data/plugins/
    echo "❌ Server cannot start without plugin!"
    exit 1
fi

echo ""
echo "================================"
echo "Starting PaperMC Server"
echo "Admin: ${ADMIN_USERNAME:-admin}"
echo "Memory: $MEMORY"
echo "================================"
echo ""

# Start server
exec java -Xms${MEMORY} -Xmx${MEMORY} ${PAPERMC_FLAGS} -jar /opt/minecraft/paperspigot.jar nogui
