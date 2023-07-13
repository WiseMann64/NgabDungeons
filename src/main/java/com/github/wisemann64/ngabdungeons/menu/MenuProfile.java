package com.github.wisemann64.ngabdungeons.menu;

import com.github.wisemann64.ngabdungeons.data.DatabaseDriver;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.players.EnumDungeonClass;
import com.github.wisemann64.ngabdungeons.players.PlayerAttributes;
import com.github.wisemann64.ngabdungeons.players.PlayerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.wisemann64.ngabdungeons.utils.Utils.c;

public class MenuProfile extends AbstractMenu{
    public MenuProfile(DPlayer owner) {
        super(owner);
    }

    @Override
    protected Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(this,54,"Your Profile");
        for (int i = 0 ; i < 54 ; i++) inv.setItem(i,MenuTools.PANE);
        inv.setItem(0,createStats());
        inv.setItem(1, selectedClass());
        inv.setItem(13,combatLevel());
        inv.setItem(20, classLevel(EnumDungeonClass.TANK));
        inv.setItem(21, classLevel(EnumDungeonClass.SUPPORT));
        inv.setItem(23, classLevel(EnumDungeonClass.FIGHTER));
        inv.setItem(24, classLevel(EnumDungeonClass.ARCHER));
        return inv;
    }

    @Override
    public void onClick(InventoryClickEvent clickEvent) {
        clickEvent.setCancelled(true);
        if (!getInventory().equals(clickEvent.getClickedInventory())) return;

    }

    private ItemStack createStats() {
        List<String> lore = new ArrayList<>();

        PlayerAttributes attr = getOwner().getAttributes();

        Function<Double,String> convert = d -> {
            String v = String.format("%.2f",d);
            return v.endsWith(".00") ? v.substring(0,v.length()-3) : v;
        };

        lore.add(c(" &cHealth&3: &e" + convert.apply(attr.getMaxHealth())));
        lore.add(c(" &aDefense&3: &e" + convert.apply(attr.getDefense())));
        lore.add(c(" &2Regen&3: &e" + convert.apply(attr.getRegen())));
        lore.add("");
        lore.add(c(" &cStrength&3: &e" + convert.apply(attr.getStrength())));
        double cc = attr.getCritChance();
        lore.add(c(" &9Crit Chance&3: &e" + (cc > 100 ? ("100% &c(+"+convert .apply(cc-100)+"%)") : (convert.apply(cc) + "%"))));
        lore.add(c(" &9Crit Damage&3: &e" + convert.apply(attr.getCritDamage()) + "%"));
        lore.add(c(" &6Attack Speed&3: &e" + convert.apply(attr.getAttackSpeed())));
        lore.add(c(" &4Penetration&3: &e" + convert.apply(attr.getPenetration())));

        ItemStack ret = MenuTools.generateItem(Material.PLAYER_HEAD,c("&dYour Stats"),lore);
        SkullMeta meta = (SkullMeta) ret.getItemMeta();
        meta.setOwningPlayer(getOwner().getHandle());
        ret.setItemMeta(meta);
        return ret;
    }

    private ItemStack selectedClass() {
        List<String> lore = new ArrayList<>();
        lore.add(c(getOwner().getSelectedClass().display() + " &6[" + "&eLevel " + getOwner().getSelectedClassLevel() + "&6]"));
        lore.add(c("&eClick to change"));


        Material mat = switch (getOwner().getSelectedClass()) {
            case ARCHER -> Material.BOW;
            case FIGHTER -> Material.DIAMOND_SWORD;
            case TANK -> Material.SHIELD;
            case SUPPORT -> Material.TOTEM_OF_UNDYING;
        };

        return MenuTools.generateItem(mat,"&dSelected Class",lore);
    }

    private ItemStack combatLevel() {
        PlayerLevel combat = getOwner().getCombat();
        int level = combat.getLevel();
        int totalxp = (int) combat.getTotalXp();
        int levelxp = (int) combat.getXp();

        List<String> lore = new ArrayList<>();
        lore.add(c("&7Kill mobs and complete dungeons"));
        lore.add(c("&7to gain Combat Experience!"));
        lore.add(c(""));
        lore.add(c("&7Total XP Gained: &e" + String.format("%,d",totalxp)));
        if (level == 60) {
            lore.add(c("&8Maximum Level Reached!"));
            lore.add(c("&e" + "-".repeat(20) + " &6" + String.format("%,d",levelxp)+" XP"));
        } else {
            int nextXp = DatabaseDriver.getInstance().getCombatLevelUpXp(level+1);
            float frac = (float) levelxp/nextXp;
            float percent = Math.round(1000F*frac)/10F;
            int progressBar = Math.round(20*frac);
            lore.add(c("&7Progress to Level " + (level+1) + ": &e" + percent + "%"));
            lore.add(c("&2-".repeat(progressBar) + "&f-".repeat(20-progressBar) + "&e " + String.format("%,d",levelxp) + "&6/&e"+String.format("%,d",nextXp)));
        }
        lore.add("");
        lore.add(c("&7Each Combat Level grants additional base stats, that is"));
        lore.add(c("&cHealth&7, &aDefense&7, &2Regen&7, &cStrength&7, &9Crit Chance&7,"));
        lore.add(c("&9Crit Damage&7, &6Attack Speed&7, and &4Penetration&7."));

        return MenuTools.generateItem(Material.IRON_SWORD,"&8Combat &7Level " + level,lore,1,level == 60);
    }

    private ItemStack classLevel(EnumDungeonClass dungeonClass) {
        PlayerLevel pl = getOwner().getClassLevel(dungeonClass);

        int level = pl.getLevel();
        int totalxp = (int) pl.getTotalXp();
        int levelxp = (int) pl.getXp();

        Material mat = switch (dungeonClass) {
            case ARCHER -> Material.BOW;
            case FIGHTER -> Material.DIAMOND_SWORD;
            case TANK -> Material.SHIELD;
            case SUPPORT -> Material.TOTEM_OF_UNDYING;
        };

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(c("&7Total XP Gained: &e" + String.format("%,d",totalxp)));
        if (level == 60) {
            lore.add(c("&8Maximum Level Reached!"));
            lore.add(c("&e" + "-".repeat(20) + " &6" + String.format("%,d",levelxp)+" XP"));
        } else {
            int nextXp = DatabaseDriver.getInstance().getClassLevelUpXp(level+1);
            float frac = (float) levelxp/nextXp;
            float percent = Math.round(1000F*frac)/10F;
            int progressBar = Math.round(20*frac);
            lore.add(c("&7Progress to Level " + (level+1) + ": &e" + percent + "%"));
            lore.add(c("&2-".repeat(progressBar) + "&f-".repeat(20-progressBar) + "&e " + String.format("%,d",levelxp) + "&6/&e"+String.format("%,d",nextXp)));
        }
        lore.add("");
        lore.add(c("&7Use this class in dungeons to level it up"));
        lore.add(c("&7and gain more stats!"));
        lore.add("");
        lore.add(c("&eClick to view!"));

        return MenuTools.generateItem(mat,dungeonClass.display() + "&7 Level " + level,lore,1,level == 60);
    }
}
