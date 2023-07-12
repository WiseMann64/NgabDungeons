package com.github.wisemann64.ngabdungeons.items;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;

import static org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack.asNMSCopy;
import static org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack.asBukkitCopy;

public class ItemNBTImpl {

    public static ItemStack setDungeonTag(ItemStack stack, CompoundTag dungeonTag) {
        net.minecraft.world.item.ItemStack is = asNMSCopy(stack);
        CompoundTag tag = is.getOrCreateTag();
        tag.put("dungeon",dungeonTag);
        is.setTag(tag);
        return asBukkitCopy(is);
    }

    public static CompoundTag getDungeonTag(ItemStack stack) {
        net.minecraft.world.item.ItemStack item = asNMSCopy(stack);
        if (item.getTag() == null || !item.getTag().contains("dungeon")) return new CompoundTag();
        return item.getTag().getCompound("dungeon");
    }

    public static CompoundTag enchantment(EnumMap<EnumItemEnchantments,Integer> in) {
        CompoundTag tag = new CompoundTag();
        in.forEach((e,i) -> tag.putInt(e.name(),i));
        return tag;
    }

    public static EnumMap<EnumItemEnchantments,Integer> enchantment(CompoundTag in) {
        EnumMap<EnumItemEnchantments,Integer> map = new EnumMap<>(EnumItemEnchantments.class);
        in.getAllKeys().forEach(k -> map.put(EnumItemEnchantments.valueOf(k),in.getInt(k)));
        return map;
    }
}
