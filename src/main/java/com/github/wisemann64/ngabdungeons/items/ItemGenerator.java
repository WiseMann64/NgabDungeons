package com.github.wisemann64.ngabdungeons.items;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.utils.Utils;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public class ItemGenerator {

    private final ItemData data;
    private final String id;
    private final Material mat;
    private String name;
    private EnumItemRarity rarity = EnumItemRarity.COMMON;
    private EnumEquipmentSlot slot = EnumEquipmentSlot.NONE;
    private int amount = 1;
    private String uuid = null;
    private boolean unbreakable = false;
    private boolean glow = false;
    private final EnumMap<EnumItemEnchantments,Integer> enchantments = new EnumMap<>(EnumItemEnchantments.class);

    public ItemGenerator(ItemData data) {
        this.data = data;
        id = data.getId();
        mat = data.getMaterial();
    }

    public ItemStack create() {
        ItemStack stack = new ItemStack(mat);

        stack.setAmount(amount);
        ItemMeta meta = hideAllFlags(Objects.requireNonNull(stack.getItemMeta()));

        meta.setDisplayName(constructName());
        meta.setLore(constructLore());

        if (unbreakable) meta.setUnbreakable(true);

        if (glow || !enchantments.isEmpty()) {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
//            enchantments.forEach((e,l) -> {
//                if (elementaryEnchantments.contains(e)) meta.addEnchant(ItemEnchantment.getFromElementary(e), l, true);
//            });
        }


        stack.setItemMeta(meta);


        CompoundTag tag = new CompoundTag();
        tag.putString("id",id);
        tag.putByte("rarity", (byte) rarity.getId());
        tag.putString("slot",slot.name());
        if (!enchantments.isEmpty()) tag.put("enchantments",ItemNBTImpl.enchantment(enchantments));
        if (uuid != null) tag.putString("uuid",uuid);
        return ItemNBTImpl.setDungeonTag(stack,tag);
    }

    public static ItemStack rawItem(String id) {
        return rawItem(id,1);
    }

    public static ItemStack rawItem(String id, int amount) {
        return ofId(id).setAmount(amount).create();
    }



    public static ItemGenerator ofData(ItemData data) {
        return new ItemGenerator(data).setName(data.getName()).setRarity(data.getRarity()).setSlot(data.getEquipSlot()).setUnbreakable(true)
                .setGlow(data.isGlow());
    }

    public static ItemGenerator ofId(String id) {
        return ofData(NgabDungeons.getItemManager().getItem(id));
    }

    public static ItemGenerator ofReader(ItemReader reader) {
        return new ItemGenerator(reader.getData()).setName(reader.getData().getName()).
                setRarity(reader.getRarity()).setSlot(reader.getSlot()).setAmount(reader.getAmount()).setUUID(reader.getUUID()).
                setUnbreakable(reader.getData().isUnbreakable()).setEnchantments(reader.getEnchantments());
    }

    private String constructName() {
        return Utils.c(rarity.getColor() + name);
    }

    private List<String> constructLore() {
        List<String> lore = new ArrayList<>();
        if (!data.getBaseStats().isEmpty()) {
            EnumItemStats[] stats = EnumItemStats.orderedArray();
            Consumer<EnumItemStats> addStat = i -> {
                int s = data.getBaseStat(i);
                if (s != 0) lore.add(Utils.c("&7"+i.getDisplay()+": "+i.getColor()+"+"+s+(i.isPercentage()?"%":"")));
            };
            for (var i: stats) addStat.accept(i);
            lore.add("");
        }

        if (!enchantments.isEmpty()) {
            List<String> ench = new ArrayList<>();
            enchantments.keySet().forEach(e -> ench.add(e.name()));
            ench.sort(Comparator.naturalOrder());
            StringBuilder sb = new StringBuilder("&9");
            int row = 0;
            String en;
            for (Iterator<String> it = ench.iterator(); it.hasNext() ; sb.append(enchantParser(en))) {
                en = it.next();
                if (sb.length() != 2) sb.append(", ");
                if (row == 3) {
                    lore.add(Utils.c(sb.toString()));
                    row = 0;
                    sb = new StringBuilder("&9");
                }
                row++;
            }
            if (!sb.toString().equals("&9")) lore.add(Utils.c(sb.toString()));
            lore.add(" ");
        }


        lore.add(Utils.c(rarity.getColor()+"&l"+rarity.name() + (data.getType() == EnumItemType.NONE ? "" : " " + data.getType().name())));
        return lore;
    }

    private String enchantParser(String enc) {
        EnumItemEnchantments e = EnumItemEnchantments.valueOf(enc);
        if (enchantments.containsKey(e)) return e.getDisplay() + (e.isShowLvl() ? " " + enchantments.get(e) : "");
        else return "";
    }

    public ItemGenerator setName(String name) {
        this.name = name;
        return this;
    }

    public ItemGenerator setRarity(EnumItemRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemGenerator setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ItemGenerator randomUUID() {
        uuid = UUID.randomUUID().toString();
        return this;
    }

    public ItemGenerator addEnchant(EnumItemEnchantments enc, int level) {
        enchantments.put(enc,level);
        return this;
    }

    public ItemGenerator addEnchant(EnumItemEnchantments enc) {
        return addEnchant(enc,1);
    }

    public ItemGenerator setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemGenerator setUnbreakable(boolean var) {
        unbreakable = var;
        return this;
    }

    public ItemGenerator setGlow(boolean var) {
        glow = var;
        return this;
    }

    public ItemData getData() {
        return data;
    }

    public ItemGenerator setSlot(EnumEquipmentSlot slot) {
        this.slot = slot;
        return this;
    }

    public ItemGenerator setEnchantments(Map<EnumItemEnchantments,Integer> enchantments) {
        if (enchantments != null) this.enchantments.putAll(enchantments);
        return this;
    }

    private ItemMeta hideAllFlags(ItemMeta meta) {
        meta.addItemFlags(
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_DYE,
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_POTION_EFFECTS,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_PLACED_ON
        );
        return meta;
    }

}
