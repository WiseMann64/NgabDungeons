package com.github.wisemann64.ngabdungeons.items;

import com.github.wisemann64.ngabdungeons.ItemManager;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;

public class ItemReader {

    private final String id;
    private final int amount;
    private final EnumItemRarity rarity;
    private final EnumEquipmentSlot slot;
    private final ItemData data;
    private String uuid = null;
    private EnumMap<EnumItemEnchantments,Integer> enchantments = null;

    private ItemReader(ItemStack item) {
        amount = item.getAmount();
        CompoundTag dungeon = ItemNBTImpl.getDungeonTag(item);
        id = dungeon.getString("id");
        rarity = EnumItemRarity.ofId(dungeon.getByte("rarity"));
        slot = EnumEquipmentSlot.valueOf(dungeon.getString("slot"));
        data = ItemManager.getInstance().getItem(id);
        if (dungeon.contains("enchantments")) enchantments = ItemNBTImpl.enchantment(dungeon.getCompound("enchantments"));
        if (dungeon.contains("uuid")) uuid = dungeon.getString("uuid");
    }

    public static ItemReader ofItemStack(ItemStack item) {
        if (item == null) return null;
        CompoundTag dungeon = ItemNBTImpl.getDungeonTag(item);
        if (!dungeon.contains("id") || !dungeon.contains("rarity")) return null;
        return new ItemReader(item);
    }

    public static ItemReader ofItemStack(ItemStack item, EnumEquipmentSlot slot) {
        if (item == null) return null;
        CompoundTag dungeon = ItemNBTImpl.getDungeonTag(item);
        if (!dungeon.contains("id") || !dungeon.contains("rarity") || !dungeon.contains("slot")) return null;
        if (!slot.name().equals(dungeon.getString("slot"))) return null;
        return new ItemReader(item);
    }

    public static boolean hasEnchant(ItemStack item, EnumItemEnchantments en) {
        ItemReader reader = ofItemStack(item);
        if (reader == null) return false;
        return reader.hasEnchant(en);
    }

    public String getId() {
        return id;
    }

    public EnumItemRarity getRarity() {
        return rarity;
    }

    public EnumEquipmentSlot getSlot() {
        return slot;
    }

    public EnumMap<EnumItemEnchantments, Integer> getEnchantments() {
        return enchantments;
    }

    public boolean hasEnchant(EnumItemEnchantments enchantment) {
        return enchantments != null && enchantments.containsKey(enchantment);
    }

    public int getEnchant(EnumItemEnchantments enchantment) {
        return hasEnchant(enchantment) ? enchantments.get(enchantment) : 0;
    }

    public String getUUID() {
        return uuid;
    }

    public int getAmount() {
        return amount;
    }

    public JsonObject asJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("id",id);
        obj.addProperty("amount",amount);
        if (uuid != null) obj.addProperty("uuid",uuid);
        if (enchantments != null) {
            JsonObject o = new JsonObject();
            enchantments.forEach((e,i) -> o.addProperty(String.valueOf(e),i));
            obj.add("enchantments",o);
        }

        return obj;
    }

    public ItemData getData() {
        return data;
    }

    public float getDamage() {
//        TODO DAMAGE ADDERS
        return data.isRangedDamage() ? 0 : data.getBaseStat(EnumItemStats.DAMAGE);
    }

    public float getRangedDamage() {
//        TODO DAMAGE ADDERS
        return data.isRangedDamage() ? data.getBaseStat(EnumItemStats.DAMAGE) : 0;
    }

//    TODO STATS GETTER (getCritChance, getMaxHealth, getStrength, etc...)

}
