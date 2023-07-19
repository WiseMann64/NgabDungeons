package com.github.wisemann64.ngabdungeons.menu;

import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.players.EnumDungeonClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuClassSelection extends AbstractMenu {

    public MenuClassSelection(DPlayer owner, MenuProfile parent) {
        super(owner,parent);
    }
    @Override
    protected Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(this,45,"Class Selection");
        for (int i = 0 ; i < 45 ; i++) inv.setItem(i,MenuTools.PANE);
        inv.setItem(0,selectSkills());
        inv.setItem(20,selectClass(EnumDungeonClass.TANK));
        inv.setItem(21,selectClass(EnumDungeonClass.SUPPORT));
        inv.setItem(23,selectClass(EnumDungeonClass.FIGHTER));
        inv.setItem(24,selectClass(EnumDungeonClass.ARCHER));
        inv.setItem(39,MenuTools.BACK);
        inv.setItem(40,MenuTools.CLOSE);
        return inv;
    }

    @Override
    public void onClick(InventoryClickEvent clickEvent) {
        clickEvent.setCancelled(true);
        if (!getInventory().equals(clickEvent.getClickedInventory())) return;
        switch (clickEvent.getSlot()) {
            case 0 -> {

            }
            case 39 -> back();
            case 40 -> close();
        }
    }

    private ItemStack selectSkills() {
        if (getOwner().getSelectedClassLevel() < 5)
            return MenuTools.generateItem(Material.RED_STAINED_GLASS_PANE,"&cClass Skills", List.of("&eReach class level 5","&eto unlock!"),1,false);
        else {
            return MenuTools.generateItem(Material.IRON_SWORD,"&cClass Skills",List.of());
        }
    }

    private ItemStack selectClass(EnumDungeonClass cls) {
        Material mat = switch (cls) {
            case ARCHER -> Material.BOW;
            case FIGHTER -> Material.DIAMOND_SWORD;
            case TANK -> Material.SHIELD;
            case SUPPORT -> Material.TOTEM_OF_UNDYING;
        };
        boolean eq = getOwner().getSelectedClass() == cls;
        String lvl = "&6[&eLevel " + getOwner().getClassLevel(cls).getLevel() + "&6] " + cls.display();
        return MenuTools.generateItem(mat,lvl,List.of(eq ? "&aSELECTED" : "&eClick to select"),1,eq);
    }
}
