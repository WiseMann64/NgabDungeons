package com.github.wisemann64.ngabdungeons.players;

public enum EnumStats {
    MAX_HEALTH(0,"Max Health"),
    HEALTH(1, "Health"),
    REGEN(2, "Regen"),
    DEFENSE(3,"Defense"),
    STRENGTH(4,"Strength"),
    CRIT_CHANCE(5,"Crit Chance"),
    CRIT_DAMAGE(6,"Crit Damage"),
    ATTACK_SPEED(7,"Attack Speed"),
    PENETRATION(8,"Penetration"),
    ;
    private final int id;
    private final String display;

    EnumStats(int id, String display) {
        this.id = id;
        this.display = display;
    }

    public EnumStats ofId(int id) {
        return switch (id) {
            case 0 -> MAX_HEALTH;
            case 1 -> HEALTH;
            case 2 -> REGEN;
            case 3 -> DEFENSE;
            case 4 -> STRENGTH;
            case 5 -> CRIT_CHANCE;
            case 6 -> CRIT_DAMAGE;
            case 7 -> ATTACK_SPEED;
            case 8 -> PENETRATION;
            default -> null;
        };
    }

    public int getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }
}
