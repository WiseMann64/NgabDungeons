package com.github.wisemann64.ngabdungeons.menu;

import com.github.wisemann64.ngabdungeons.utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static com.github.wisemann64.ngabdungeons.utils.Utils.c;

public class MenuTools {

    public final static ItemStack PANE = createPane();
    public final static ItemStack BACK = createBack();
    public final static ItemStack CLOSE = createClose();
    public final static ItemStack COMING_SOON = createComingSoon();
    public final static ItemStack NOT_YET_UNLOCKED = createNotYetUnlocked();

    private static ItemStack createPane() {
        ItemStack is = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(c("&e"));
        is.setItemMeta(meta);
        return is;
    }
    private static ItemStack createBack() {
        ItemStack pane = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(c("&eGo Back"));
        pane.setItemMeta(meta);
        return pane;
    }
    private static ItemStack createClose() {
        ItemStack pane = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(c("&cClose"));
        pane.setItemMeta(meta);
        return pane;
    }
    private static ItemStack createComingSoon() {
        ItemStack pane = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(c("&eComing Soon!"));
        pane.setItemMeta(meta);
        return pane;
    }

    private static ItemStack createNotYetUnlocked() {
        ItemStack pane = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(c("&cNot Yet Unlocked"));
        pane.setItemMeta(meta);
        return pane;
    }

    public static ItemStack generateItem(Material mat, String name, List<String> lore) {
        return generateItem(mat, name, lore,1,false);
    }

    public static ItemStack generateItem(Material mat, String name, List<String> lore, int amount) {
        return generateItem(mat,name,lore,amount,false);
    }

    public static ItemStack generateItem(Material mat, String name, List<String> lore, int amount, boolean glow) {
        if (amount < 1) throw new IllegalArgumentException("Item amount must be a positive number");
        ItemStack ret = new ItemStack(mat);
        ItemMeta meta = ret.getItemMeta();
        meta.setDisplayName(Utils.c(name));
        lore = lore.stream().map(Utils::c).toList();
        meta.setLore(lore);
        meta.addItemFlags(
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_DYE,
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_POTION_EFFECTS,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_PLACED_ON
        );
        if (glow) meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1,false);
        ret.setAmount(amount);
        ret.setItemMeta(meta);
        return ret;
    }
}
