package com.github.wisemann64.ngabdungeons.combat;

import org.bukkit.entity.Arrow;

import java.util.UUID;

public record ArrowStats(Arrow arrow, double damage,
                         ArrowStats.ArrowShooter shooter, UUID shooterUUID,
                         boolean critical,
                         double penetration) {

    public enum ArrowShooter {
        PLAYER, MOB, GENERIC;
    }

}
