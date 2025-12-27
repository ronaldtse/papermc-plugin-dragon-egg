package com.dragonegg.lightning.pilaf;

import java.util.*;

/**
 * Test context for DragonEggLightning Integration Test Framework (DITF)
 *
 * Manages test state including:
 * - Test entities and players
 * - World state
 * - Plugin interactions
 * - Test results and assertions
 */
public class PilafTestContext {

    private final Map<String, PilafTestEntity> entities;
    private final Map<String, PilafTestPlayer> players;
    private final List<String> pluginCommands;
    private final List<String> testLogs;
    private final Map<String, Object> worldState;

    public PilafTestContext() {
        this.entities = new HashMap<>();
        this.players = new HashMap<>();
        this.pluginCommands = new ArrayList<>();
        this.testLogs = new ArrayList<>();
        this.worldState = new HashMap<>();
    }

    /**
     * Add a test entity
     */
    public void addEntity(String name, PilafTestEntity entity) {
        entities.put(name, entity);
    }

    /**
     * Get a test entity
     */
    public PilafTestEntity getEntity(String name) {
        return entities.get(name);
    }

    /**
     * Remove a test entity
     */
    public void removeEntity(String name) {
        entities.remove(name);
    }

    /**
     * Check if entity exists
     */
    public boolean hasEntity(String name) {
        return entities.containsKey(name);
    }

    /**
     * Add a test player
     */
    public void addPlayer(String name, PilafTestPlayer player) {
        players.put(name, player);
    }

    /**
     * Get a test player
     */
    public PilafTestPlayer getPlayer(String name) {
        return players.get(name);
    }

    /**
     * Remove a test player
     */
    public void removePlayer(String name) {
        players.remove(name);
    }

    /**
     * Check if player exists
     */
    public boolean hasPlayer(String name) {
        return players.containsKey(name);
    }

    /**
     * Record a plugin command
     */
    public void recordPluginCommand(String command, String player) {
        pluginCommands.add(player + ": " + command);
    }

    /**
     * Check if plugin received command
     */
    public boolean hasPluginCommand(String command, String player) {
        String searchTerm = player + ": " + command;
        return pluginCommands.stream().anyMatch(cmd -> cmd.contains(searchTerm));
    }

    /**
     * Add a test log entry
     */
    public void addLog(String message) {
        testLogs.add(message);
    }

    /**
     * Get all test logs
     */
    public List<String> getLogs() {
        return new ArrayList<>(testLogs);
    }

    /**
     * Set world state value
     */
    public void setWorldState(String key, Object value) {
        worldState.put(key, value);
    }

    /**
     * Get world state value
     */
    public Object getWorldState(String key) {
        return worldState.get(key);
    }

    /**
     * Get all entities
     */
    public Map<String, PilafTestEntity> getEntities() {
        return new HashMap<>(entities);
    }

    /**
     * Get all players
     */
    public Map<String, PilafTestPlayer> getPlayers() {
        return new HashMap<>(players);
    }

    /**
     * Get all plugin commands
     */
    public List<String> getPluginCommands() {
        return new ArrayList<>(pluginCommands);
    }

    /**
     * Clear all test data
     */
    public void cleanup() {
        entities.clear();
        players.clear();
        pluginCommands.clear();
        testLogs.clear();
        worldState.clear();
    }

    /**
     * Get summary of test context state
     */
    public String getSummary() {
        return String.format("DITF Test Context - Entities: %d, Players: %d, Commands: %d, Logs: %d",
            entities.size(), players.size(), pluginCommands.size(), testLogs.size());
    }
}
