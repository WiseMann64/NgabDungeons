package com.github.wisemann64.ngabdungeons.items;

import com.google.gson.JsonObject;
import org.bukkit.Material;

import java.util.EnumMap;

public class ItemData {
    private final String id;
    private final String name;
    private final Material material;
    private final EnumItemRarity rarity;
    private final EnumItemType type;;
    private final EnumEquipmentSlot equip;
    private final boolean isRangedDamage;
    private final boolean unbreakable;
    private final boolean glow;
    private final EnumMap<EnumItemStats,Integer> baseStats = new EnumMap<>(EnumItemStats.class);

    public ItemData(String id, JsonObject obj) {
        this.id = id;
        name = obj.get("name").getAsString();
        material = Material.valueOf(obj.get("material").getAsString());
        rarity = obj.has("rarity") ? EnumItemRarity.ofId(obj.get("rarity").getAsInt()) : EnumItemRarity.COMMON;
        equip = obj.has("equip") ? EnumEquipmentSlot.valueOf(obj.get("equip").getAsString()) : EnumEquipmentSlot.NONE;
        unbreakable = obj.has("unbreakable") && obj.get("unbreakable").getAsBoolean();
        isRangedDamage = obj.has("ranged") && obj.get("ranged").getAsBoolean();
        glow = obj.has("glow") && obj.get("glow").getAsBoolean();
        JsonObject base = obj.getAsJsonObject("base");
        if (base != null) base.entrySet().forEach(e -> baseStats.put(EnumItemStats.valueOf(e.getKey()),e.getValue().getAsInt()));

        if (equip == EnumEquipmentSlot.HELMET) type = EnumItemType.HELMET;
        else if (equip == EnumEquipmentSlot.CHESTPLATE) type = EnumItemType.CHESTPLATE;
        else if (equip == EnumEquipmentSlot.LEGGINGS) type = EnumItemType.LEGGINGS;
        else if (equip == EnumEquipmentSlot.BOOTS) type = EnumItemType.BOOTS;
        else type = obj.has("type")? EnumItemType.valueOf(obj.get("type").getAsString()) : EnumItemType.NONE;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public EnumItemRarity getRarity() {
        return rarity;
    }

    public String toString() {
        return id;
    }

    public EnumMap<EnumItemStats, Integer> getBaseStats() {
        return new EnumMap<>(baseStats);
    }

    public int getBaseStat(EnumItemStats stat) {
        return baseStats.getOrDefault(stat,0);
    }

    public EnumEquipmentSlot getEquipSlot() {
        return equip;
    }

    public boolean isRangedDamage() {
        return isRangedDamage;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public boolean isGlow() {
        return glow;
    }

    public EnumItemType getType() {
        return type;
    }
}
