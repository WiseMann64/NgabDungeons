package com.github.wisemann64.ngabdungeons.mobs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobListeners implements Listener {
    @EventHandler
    public void event(EntityDeathEvent v) {
        v.setDroppedExp(0);
        v.getDrops().clear();
    }
}
