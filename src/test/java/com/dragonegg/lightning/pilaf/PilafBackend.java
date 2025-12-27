package com.dragonegg.lightning.pilaf;

import java.util.List;
import java.util.Map;

/**
 * Backend interface for Paper Integration Lightning Automation Framework (PILAF)
 *
 * Defines the contract for different testing backends:
 * - MockBukkitBackend: Fast, in-memory testing using MockBukkit
 * - RealServerBackend: Integration testing with actual Paper servers
 */
public interface PilafBackend {

    /**
     * Get the backend type name
     * @return Backend type (e.g., "mockbukkit", "real-server")
     */
    String getType();

    /**
     * Initialize the backend
     */
    void initialize() throws Exception;

    /**
     * Clean up backend resources
     */
    void cleanup() throws Exception;

    // Player Actions
    /**
     * Move player to destination
     */
    void movePlayer(String playerName, String destinationType, String destination);

    /**
     * Equip item to player
     */
    void equipItem(String playerName, String item, String slot);

    /**
     * Use item
     */
    void useItem(String playerName, String item, String target);

    /**
     * Execute player command
     */
    void executePlayerCommand(String playerName, String command, List<String> arguments);

    /**
     * Send chat message
     */
    void sendChat(String playerName, String message);

    // Server Actions
    /**
     * Spawn entity
     */
    void spawnEntity(String name, String entityType, List<Double> location, Map<String, String> equipment);

    /**
     * Give item to player
     */
    void giveItem(String playerName, String item, Integer count);

    /**
     * Set entity health
     */
    void setEntityHealth(String entityName, Double health);

    /**
     * Execute server command
     */
    void executeServerCommand(String command, List<String> arguments);

    // Assertions
    /**
     * Check if entity exists
     */
    boolean entityExists(String entityName);

    /**
     * Get entity health
     */
    double getEntityHealth(String entityName);

    /**
     * Check if player inventory contains item
     */
    boolean playerInventoryContains(String playerName, String item, String slot);

    /**
     * Check if plugin received command
     */
    boolean pluginReceivedCommand(String pluginName, String command, String playerName);

    // Cleanup
    /**
     * Remove all test entities
     */
    void removeAllTestEntities();

    /**
     * Remove all test players
     */
    void removeAllTestPlayers();
}
