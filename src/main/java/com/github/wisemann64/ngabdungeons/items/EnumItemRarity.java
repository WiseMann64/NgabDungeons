package com.github.wisemann64.ngabdungeons.items;

public enum EnumItemRarity {

    COMMON(0,"&f"),
    UNCOMMON(1,"&a"),
    RARE(2,"&9"),
    EPIC(3,"&5"),
    LEGENDARY(4,"&6");

    private final int id;
    private final CharSequence color;

    EnumItemRarity(int id, CharSequence color) {
        this.id = id;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public CharSequence getColor() {
        return color;
    }

    public static EnumItemRarity ofId(int id) {
        return switch (id) {
            case 0 -> COMMON;
            case 1 -> UNCOMMON;
            case 2 -> RARE;
            case 3 -> EPIC;
            case 4 -> LEGENDARY;
            default -> null;
        };
    }

}
