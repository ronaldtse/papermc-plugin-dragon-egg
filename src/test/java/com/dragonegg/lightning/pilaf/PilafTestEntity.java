package com.dragonegg.lightning.pilaf;

import java.util.*;

/**
 * Test entity for DITF framework
 *
 * Represents a Minecraft entity in tests with configurable properties
 */
public class PilafTestEntity {

    private final String name;
    private final String type;
    private final List<Double> location;
    private final Map<String, String> equipment;
    private double health;
    private final Map<String, Object> metadata;

    public PilafTestEntity(String name, String type, List<Double> location) {
        this.name = name;
        this.type = type;
        this.location = location != null ? new ArrayList<>(location) : new ArrayList<>();
        this.equipment = new HashMap<>();
        this.health = 20.0; // Default health
        this.metadata = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<Double> getLocation() {
        return new ArrayList<>(location);
    }

    public void setLocation(List<Double> location) {
        this.location.clear();
        this.location.addAll(location);
    }

    public Map<String, String> getEquipment() {
        return new HashMap<>(equipment);
    }

    public void setEquipment(Map<String, String> equipment) {
        this.equipment.clear();
        this.equipment.putAll(equipment);
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public Map<String, Object> getAllMetadata() {
        return new HashMap<>(metadata);
    }

    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public String toString() {
        return String.format("PilafTestEntity{name='%s', type='%s', location=%s, health=%.1f}",
            name, type, location, health);
    }
}
