package com.dragonegg.lightning.pilaf;

import java.util.*;

/**
 * Real server backend for PILAF framework
 *
 * Integration testing with actual Paper servers using RCON.
 * Connects to a Docker-hosted Minecraft server via RCON protocol.
 *
 * Configuration via constructor or environment variables:
 * - RCON_HOST: Server hostname (default: localhost)
 * - RCON_PORT: RCON port (default: 25575)
 * - RCON_PASSWORD: RCON password (default: dragon123)
 */
public class RealServerBackend implements PilafBackend {

    private boolean initialized;
    private RconClient rconClient;
    private final String host;
    private final int port;
    private final String password;

    /**
     * Create with default configuration from environment variables
     */
    public RealServerBackend() {
        this(
            System.getenv().getOrDefault("RCON_HOST", "localhost"),
            Integer.parseInt(System.getenv().getOrDefault("RCON_PORT", "25575")),
            System.getenv().getOrDefault("RCON_PASSWORD", "dragon123")
        );
    }

    /**
     * Create with explicit configuration
     */
    public RealServerBackend(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.initialized = false;
        this.rconClient = null;
    }

    @Override
    public String getType() {
        return "real-server";
    }

    @Override
    public void initialize() throws Exception {
        if (!initialized) {
            System.out.println("Initializing Real Server backend...");
            System.out.println("  Host: " + host + ", Port: " + port);

            // Create and connect to RCON server
            rconClient = new RconClient(host, port, password);
            boolean connected = rconClient.connect();
            if (!connected) {
                throw new Exception("Failed to connect to RCON server at " + host + ":" + port);
            }

            initialized = true;
            System.out.println("Real Server backend initialized with RCON");
        }
    }

    @Override
    public void cleanup() throws Exception {
        if (initialized && rconClient != null) {
            System.out.println("Cleaning up Real Server backend...");
            rconClient.disconnect();
            initialized = false;
            System.out.println("Real Server backend cleaned up");
        }
    }

    @Override
    public void movePlayer(String playerName, String destinationType, String destination) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping movePlayer");
            return;
        }

        System.out.println("RealServer: Moving player " + playerName + " to " + destinationType + " " + destination);
        String command = String.format("tp %s %s", playerName, destination);
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public void equipItem(String playerName, String item, String slot) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping equipItem");
            return;
        }

        System.out.println("RealServer: Equipping " + item + " to " + slot + " for " + playerName);
        // Use replaceitem for precise slot control
        String slotName = convertSlotName(slot);
        String command = String.format("item replace entity %s %s with minecraft:%s 1", playerName, slotName, item);
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    /**
     * Convert simple slot names to Minecraft slot format
     */
    private String convertSlotName(String slot) {
        return switch (slot.toLowerCase()) {
            case "offhand" -> "weapon.offhand";
            case "mainhand" -> "weapon.mainhand";
            case "helmet" -> "armor.head";
            case "chestplate" -> "armor.chest";
            case "leggings" -> "armor.legs";
            case "boots" -> "armor.feet";
            default -> "weapon.mainhand";
        };
    }

    @Override
    public void useItem(String playerName, String item, String target) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping useItem");
            return;
        }

        System.out.println("RealServer: Using " + item + " by " + playerName + " on " + target);
        // This would typically be handled by the plugin itself
        String command = String.format("say %s used %s on %s", playerName, item, target);
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public void executePlayerCommand(String playerName, String command, List<String> arguments) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping executePlayerCommand");
            return;
        }

        String fullCommand = command + " " + String.join(" ", arguments);
        System.out.println("RealServer: " + playerName + " executes command: " + fullCommand);

        // Execute as the player
        String rconCommand = String.format("execute as %s run %s", playerName, fullCommand);
        String result = rconClient.executeCommand(rconCommand);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public void sendChat(String playerName, String message) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping sendChat");
            return;
        }

        System.out.println("RealServer: " + playerName + " sends chat: " + message);
        String command = String.format("say <%s> %s", playerName, message);
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public void spawnEntity(String name, String entityType, List<Double> location, Map<String, String> equipment) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping spawnEntity");
            return;
        }

        System.out.println("RealServer: Spawning entity " + name + " (" + entityType + ") at " + location);

        // Build NBT for custom name and tags
        String nbt = String.format("{CustomName:'\"test_%s\"',CustomNameVisible:1b,Tags:[\"test_entity\",\"pilaf_test\"]}", name);
        String command = String.format("summon minecraft:%s %.1f %.1f %.1f %s",
            entityType.toLowerCase(), location.get(0), location.get(1), location.get(2), nbt);
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);

        // Apply equipment if provided
        if (equipment != null && !equipment.isEmpty()) {
            for (Map.Entry<String, String> entry : equipment.entrySet()) {
                String slot = entry.getKey();
                String item = entry.getValue();
                String slotName = convertSlotName(slot);
                String equipCmd = String.format("item replace entity @e[name=\"test_%s\",limit=1] %s with minecraft:%s",
                    name, slotName, item);
                rconClient.executeCommand(equipCmd);
            }
        }
    }

    @Override
    public void giveItem(String playerName, String item, Integer count) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping giveItem");
            return;
        }

        System.out.println("RealServer: Giving " + count + " " + item + " to " + playerName);
        String command = String.format("give %s minecraft:%s %d", playerName, item, count);
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public void setEntityHealth(String entityName, Double health) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping setEntityHealth");
            return;
        }

        System.out.println("RealServer: Setting health of " + entityName + " to " + health);
        String command = String.format("data modify entity @e[name=\"test_%s\",limit=1] Health set value %df", entityName, health.floatValue());
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public void executeServerCommand(String command, List<String> arguments) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping executeServerCommand");
            return;
        }

        String fullCommand = command + " " + String.join(" ", arguments);
        System.out.println("RealServer: Server executes command: " + fullCommand);
        String result = rconClient.executeCommand(fullCommand);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public boolean entityExists(String entityName) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, returning false for entityExists");
            return false;
        }

        System.out.println("RealServer: Checking if entity " + entityName + " exists");
        // Use execute command to check for entity
        String command = String.format("execute if entity @e[name=\"test_%s\"]", entityName);
        String result = rconClient.executeCommand(command);
        boolean exists = result != null && !result.toLowerCase().contains("no entity");
        System.out.println("  Entity exists: " + exists);
        return exists;
    }

    @Override
    public double getEntityHealth(String entityName) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, returning 0.0 for getEntityHealth");
            return 0.0;
        }

        System.out.println("RealServer: Getting health of " + entityName);
        String command = String.format("data get entity @e[name=\"test_%s\",limit=1] Health", entityName);
        String result = rconClient.executeCommand(command);

        // Parse health from result
        double health = 20.0; // Default health
        if (result != null && result.contains(":")) {
            try {
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    String healthStr = parts[1].trim().replaceAll("[^0-9.]", "");
                    if (!healthStr.isEmpty()) {
                        health = Double.parseDouble(healthStr);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("  Warning: Failed to parse health from: " + result);
            }
        }

        System.out.println("  Entity health: " + health);
        return health;
    }

    @Override
    public boolean playerInventoryContains(String playerName, String item, String slot) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, returning false for playerInventoryContains");
            return false;
        }

        System.out.println("RealServer: Checking if " + playerName + " has " + item + " in " + slot);
        String slotName = convertSlotName(slot);
        String command = String.format("execute if entity @p[name=%s,nbt={Inventory:[{id:\"minecraft:%s\"}]}]", playerName, item);
        String result = rconClient.executeCommand(command);
        boolean contains = result != null && !result.toLowerCase().contains("no entity");
        System.out.println("  Inventory contains: " + contains);
        return contains;
    }

    @Override
    public boolean pluginReceivedCommand(String pluginName, String command, String playerName) {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, returning false for pluginReceivedCommand");
            return false;
        }

        System.out.println("RealServer: Checking if plugin " + pluginName + " received command from " + playerName);
        // Check if plugin is loaded
        String result = rconClient.executeCommand("plugins");
        boolean pluginLoaded = result != null && result.contains(pluginName);
        System.out.println("  Plugin " + pluginName + " loaded: " + pluginLoaded);
        return pluginLoaded;
    }

    @Override
    public void removeAllTestEntities() {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping removeAllTestEntities");
            return;
        }

        System.out.println("RealServer: Removing all test entities");
        String command = "kill @e[tag=pilaf_test]";
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    @Override
    public void removeAllTestPlayers() {
        if (!initialized) {
            System.out.println("Warning: RealServer not initialized, skipping removeAllTestPlayers");
            return;
        }

        System.out.println("RealServer: Removing all test players");
        // Can't really remove players via RCON, but we can kick them
        String command = "kick @a[tag=pilaf_test] Test completed";
        String result = rconClient.executeCommand(command);
        System.out.println("  RCON Response: " + result);
    }

    /**
     * Check if backend is initialized and connected
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Get the RCON client for advanced operations
     */
    public RconClient getRconClient() {
        return rconClient;
    }
}
