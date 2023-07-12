package com.github.wisemann64.ngabdungeons.items;

public enum EnumItemEnchantments {
    TELEKINESIS("Telekinesis",1,false),;

    private final String display;
    private final int maxLvl;
    private final boolean showLvl;

    EnumItemEnchantments(String display, int maxLvl, boolean showLvl) {
        this.display = display;
        this.maxLvl = maxLvl;
        this.showLvl = showLvl;
    }

    public String getDisplay() {
        return display;
    }

    public int getMaxLvl() {
        return maxLvl;
    }

    public boolean isShowLvl() {
        return showLvl;
    }
}
