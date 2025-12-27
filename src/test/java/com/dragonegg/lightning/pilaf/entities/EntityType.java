package com.dragonegg.lightning.pilaf.entities;

public enum EntityType {
    ZOMBIE("zombie"),
    SKELETON("skeleton"),
    SPIDER("spider"),
    CREEPER("creeper"),
    ENDERMAN("enderman"),
    WITCH("witch"),
    VILLAGER("villager"),
    COW("cow"),
    PIG("pig"),
    SHEEP("sheep"),
    CHICKEN("chicken"),
    HORSE("horse"),
    CAT("cat"),
    WOLF("wolf"),
    BEE("bee"),
    FOX("fox"),
    PANDA("panda"),
    DOLPHIN("dolphin"),
    TURTLE("turtle"),
    COD("cod"),
    SALMON("salmon"),
    PUFFERFISH("pufferfish"),
    TROPICAL_FISH("tropical_fish"),
    SQUID("squid"),
    GLOW_SQUID("glow_squid"),
    GOAT("goat"),
    AXOLOTL("axolotl"),
    ALLAY("allay");

    private final String name;

    EntityType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static EntityType fromString(String name) {
        for (EntityType type : EntityType.values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown entity type: " + name);
    }
}
