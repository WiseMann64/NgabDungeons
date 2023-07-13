package com.github.wisemann64.ngabdungeons.players;

public enum EnumDungeonClass {

    ARCHER("Archer","&a"),
    FIGHTER("Fighter","&c"),
    TANK("Tank","&e"),
    SUPPORT("Support","&b");

    private final String name;
    private final String color;

    EnumDungeonClass(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String display() {
        return color+name;
    }

    public String getColor() {
        return color;
    }
}
