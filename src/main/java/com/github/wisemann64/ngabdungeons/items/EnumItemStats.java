package com.github.wisemann64.ngabdungeons.items;

public enum EnumItemStats {

    DAMAGE("&c","Damage",false),
    STRENGTH("&c","Strength",false),
    CRIT_CHANCE("&c","Critical Chance",true),
    CRIT_DAMAGE("&c","Critical Damage",true),
    ATTACK_SPEED("&c","Attack Speed",false),
    PENETRATION("&c","Penetration",false),

    HEALTH("&a","Health",false),
    DEFENSE("&a","Defense",false),
    REGEN("&a","Regeneration",false);

    private final CharSequence color;
    private final String display;
    private final boolean percentage;

    EnumItemStats(CharSequence color, String display, boolean percentage) {
        this.color = color;
        this.display = display;
        this.percentage = percentage;
    }

    public CharSequence getColor() {
        return color;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isPercentage() {
        return percentage;
    }

    public static EnumItemStats[] orderedArray() {
        return new EnumItemStats[] {DAMAGE,STRENGTH,CRIT_CHANCE,CRIT_DAMAGE,PENETRATION,ATTACK_SPEED,HEALTH,DEFENSE,REGEN};
    }

}
