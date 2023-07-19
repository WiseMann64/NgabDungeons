package com.github.wisemann64.ngabdungeons.combat;

import org.bukkit.entity.Arrow;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public record ArrowStats(Arrow arrow, double damage,
                         ArrowStats.ArrowShooter shooter, UUID shooterUUID,
                         boolean critical,
                         double penetration,
                         EnumMap<ArrowFlags,Float> flags) {

    public enum ArrowShooter {
        PLAYER, MOB, GENERIC;
    }

    public enum ArrowFlags {
        IGNORE_DEFENSE,
        ANTI_GRAVITY,
        PENETRATE
    }
}
