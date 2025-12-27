package com.dragonegg.lightning.pilaf;

import java.util.*;

/**
 * Test player for DITF framework
 *
 * Represents a Minecraft player in tests with configurable properties
 */
public class PilafTestPlayer {

    private final String name;
    private final List<Double> location;
    private final Map<String, Integer> inventory;
    private final Map<String, String> equipment;
    private boolean op;
    private final Set<String> permissions;
    private final Map<String, Object> metadata;

    public PilafTestPlayer(String name, List<Double> location) {
        this.name = name;
        this.location = location != null ? new ArrayList<>(location) : new ArrayList<>();
        this.inventory = new HashMap<>();
        this.equipment = new HashMap<>();
        this.op = false;
        this.permissions = new HashSet<>();
        this.metadata = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public List<Double> getLocation() {
        return new ArrayList<>(location);
    }

    public void setLocation(List<Double> location) {
        this.location.clear();
        this.location.addAll(location);
    }

    public Map<String, Integer> getInventory() {
        return new HashMap<>(inventory);
    }

    public void addItem(String item, Integer count) {
        inventory.put(item, inventory.getOrDefault(item, 0) + count);
    }

    public void removeItem(String item, Integer count) {
        inventory.put(item, Math.max(0, inventory.getOrDefault(item, 0) - count));
    }

    public boolean hasItem(String item) {
        return inventory.containsKey(item) && inventory.get(item) > 0;
    }

    public Integer getItemCount(String item) {
        return inventory.getOrDefault(item, 0);
    }

    public Map<String, String> getEquipment() {
        return new HashMap<>(equipment);
    }

    public void setEquipment(Map<String, String> equipment) {
        this.equipment.clear();
        this.equipment.putAll(equipment);
    }

    public void equipItem(String slot, String item) {
        equipment.put(slot, item);
    }

    public String getEquippedItem(String slot) {
        return equipment.get(slot);
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    public Set<String> getPermissions() {
        return new HashSet<>(permissions);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission) || op;
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

    @Override
    public String toString() {
        return String.format("PilafTestPlayer{name='%s', location=%s, inventory=%s, op=%s}",
            name, location, inventory, op);
    }
}
