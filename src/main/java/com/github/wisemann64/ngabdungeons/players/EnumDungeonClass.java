package com.github.wisemann64.ngabdungeons.players;

import org.bukkit.Material;

public enum EnumDungeonClass {

    ARCHER("Archer","&a",0),
    FIGHTER("Fighter","&c",1),
    TANK("Tank","&e",2),
    SUPPORT("Support","&b",3);

    private final String name;
    private final String color;
    private final int id;

    EnumDungeonClass(String name, String color, int id) {
        this.name = name;
        this.color = color;
        this.id = id;
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

    public int getId() {
        return id;
    }

    public EnumDungeonClass ofId(int id) {
        return switch (id) {
          default -> null;
          case 0 -> ARCHER;
          case 1 -> FIGHTER;
          case 3 -> TANK;
          case 4 -> SUPPORT;
        };
    }
}
