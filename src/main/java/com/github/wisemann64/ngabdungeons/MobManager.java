package com.github.wisemann64.ngabdungeons;

import com.github.wisemann64.ngabdungeons.combat.ArrowStats;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import org.bukkit.entity.Arrow;

import java.util.*;

public class MobManager {

    private final Map<String, AbstractDungeonMob> trackedMobs = new HashMap<>();
    private final Map<UUID, ArrowStats> flyingArrows = new HashMap<>();

    public Set<UUID> getTrackedMobsUUIDs() {
        Set<UUID> ret = new HashSet<>();
        trackedMobs.values().forEach(v -> ret.add(v.getHandle().getUniqueId()));
        return ret;
    }

    public String randomId(Map<String,?> target) {
        StringBuilder var;
        do {
            var = new StringBuilder();
            Random r = new Random();
            for (int i = 0 ; i < 8 ; i++) {
                char c = (char) (r.nextInt(26)+97);
                var.append(c);
            }
        } while (target.containsKey(var.toString()));
        return var.toString();
    }

    public String putMobToRegistry(AbstractDungeonMob mob) {
        String index = randomId(trackedMobs);
        this.trackedMobs.put(index,mob);
        return index;
    }

    public String putMobToRegistry(AbstractDungeonMob mob, String id) {
        if (id == null) return putMobToRegistry(mob);
        if (trackedMobs.containsKey(id)) throw new IllegalArgumentException("Mob with id " + id + " already exists!");
        this.trackedMobs.put(id,mob);
        return id;
    }

//    public void empty() {
//        new ArrayList<>(trackedMobs.values()).forEach(SLMob::remove);
//    }

    public AbstractDungeonMob getMob(UUID u) {
        for (AbstractDungeonMob ma : trackedMobs.values()) if (ma.getHandle().getUniqueId().equals(u)) return ma;
        return null;
    }

    public AbstractDungeonMob getMobId(String id) {
        return trackedMobs.getOrDefault(id,null);
    }

    public void removeMobFromRegistry(String key) {
        AbstractDungeonMob mob = this.trackedMobs.remove(key);
        if (mob != null) NgabDungeons.log("Removed mob " + key + " from registry");
    }
    public void tick() {
        Set<String> rem = new HashSet<>();
        trackedMobs.forEach((id,mob) -> {
            if (mob.getHandle().isDead()) rem.add(id);
        });
        rem.forEach(this::removeMobFromRegistry);

        Set<UUID> rem1 = new HashSet<>();
        flyingArrows.forEach((id,arr) -> {
            if (arr.arrow().isInBlock() || arr.arrow().isDead()) rem1.add(id);
        });
        rem1.forEach(this::removeArrow);

        trackedMobs.values().forEach(AbstractDungeonMob::tick);
    }

    public void addArrow(Arrow arrow, double damage, ArrowStats.ArrowShooter shooter, UUID shooterUUID, boolean crit,double penetration) {
        ArrowStats as = new ArrowStats(arrow, damage, shooter, shooterUUID, crit,penetration);
        flyingArrows.put(arrow.getUniqueId(),as);
    }

    public ArrowStats removeArrow(UUID uuid) {
        return flyingArrows.remove(uuid);
    }
}
