package com.dragonegg.lightning.pilaf.entities;

public class Entity {
    private String name;
    private EntityType type;
    private Position position;

    public Entity() {}

    public String getName() { return name; }
    public EntityType getType() { return type; }
    public Position getPosition() { return position; }

    public void setName(String name) { this.name = name; }
    public void setType(EntityType type) { this.type = type; }
    public void setPosition(Position position) { this.position = position; }

    @Override
    public String toString() {
        return String.format("Entity{name='%s', type=%s, position=%s}", name, type, position);
    }
}
