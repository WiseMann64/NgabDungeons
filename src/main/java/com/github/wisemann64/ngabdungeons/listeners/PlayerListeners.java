package com.github.wisemann64.ngabdungeons.listeners;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.PlayerManager;
import com.github.wisemann64.ngabdungeons.menu.AbstractMenu;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent v) {
        v.getPlayer().teleport(NgabDungeons.TEMP_SPAWN_LOCATION, PlayerTeleportEvent.TeleportCause.PLUGIN);
        v.setJoinMessage(null);

        Player p = v.getPlayer();
        p.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.4);
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40);
        p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
        p.setHealth(40);
        p.setFoodLevel(20);

        DPlayer player = new DPlayer(p);
    }

    @EventHandler
    public void quit(PlayerQuitEvent v) {
        DPlayer p = PlayerManager.instance().getPlayer(v.getPlayer());
        if (p != null) p.remove();
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent v) {
        DPlayer p = PlayerManager.instance().getPlayer(v.getPlayer());
        if (p == null) return;
        if (p.getHandle().getGameMode() != GameMode.CREATIVE) v.setCancelled(true);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent v) {
        DPlayer p = PlayerManager.instance().getPlayer(v.getPlayer());
        if (p == null) return;
        if (p.getHandle().getGameMode() != GameMode.CREATIVE) v.setCancelled(true);
    }

    @EventHandler
    public void cancelInteract(PlayerInteractEntityEvent v) {
        if (v.getRightClicked() instanceof Villager)v.setCancelled(true);
    }

    @EventHandler
    public void hunger(FoodLevelChangeEvent v) {
        v.setCancelled(true);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent v) {
        if (v.getInventory().getHolder() instanceof AbstractMenu menu) menu.onClick(v);
    }
}
