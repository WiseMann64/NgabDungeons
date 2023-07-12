package com.github.wisemann64.ngabdungeons.players;

public enum EnumLevelType {
    COMBAT("Combat"), TANK("Tank"), SUPPORT("Support"), ARCHER("Archer"), FIGHTER("Fighter");

    private final String name;

    EnumLevelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
