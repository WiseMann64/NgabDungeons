package com.github.wisemann64.ngabdungeons;

import com.github.wisemann64.ngabdungeons.combat.ArrowStats;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.github.wisemann64.ngabdungeons.skills.AbstractSkill;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;

import java.util.*;

public class MobManager {

    private final Map<String, AbstractDungeonMob> trackedMobs = new HashMap<>();
    private final Map<UUID, ArrowStats> flyingArrows = new HashMap<>();
    private final Map<UUID, AbstractSkill> trackedSkills = new HashMap<>();
    private final Set<UUID> skillsToRemove = new HashSet<>();
    private final Set<AbstractSkill> skillsToAdd = new HashSet<>();

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
            if (arr.arrow().isInBlock()) arr.arrow().remove();

        });
        rem1.forEach(this::removeArrow);

        trackedMobs.values().forEach(AbstractDungeonMob::tick);
        flyingArrows.values().forEach(as -> {
            if (as.flags().containsKey(ArrowStats.ArrowFlags.ANTI_GRAVITY))
                as.arrow().getWorld().spawnParticle(Particle.FIREWORKS_SPARK,as.arrow().getLocation(),1,0.01,0.01,0.01,0,null,true);
            if (as.flags().containsKey(ArrowStats.ArrowFlags.PENETRATE))
                as.arrow().getWorld().spawnParticle(Particle.REDSTONE,as.arrow().getLocation(),1,0.01,0.01,0.01,0,new Particle.DustOptions(Color.RED,1),true);
        });

        trackedSkills.values().forEach(AbstractSkill::tick);

        skillsToRemove.forEach(trackedSkills::remove);
        skillsToRemove.clear();

        skillsToAdd.forEach(s -> trackedSkills.put(s.getUniqueId(),s));
        skillsToAdd.clear();
    }

    public void addArrow(Arrow arrow, double damage, ArrowStats.ArrowShooter shooter, UUID shooterUUID, boolean crit,double penetration) {
        ArrowStats as = new ArrowStats(arrow, damage, shooter, shooterUUID, crit,penetration,new EnumMap<>(ArrowStats.ArrowFlags.class));
        flyingArrows.put(arrow.getUniqueId(),as);
    }

    public void addArrow(Arrow arrow, double damage, ArrowStats.ArrowShooter shooter,
                         UUID shooterUUID, boolean crit, double penetration,
                         EnumMap<ArrowStats.ArrowFlags,Float> flags) {
        ArrowStats as = new ArrowStats(arrow, damage, shooter, shooterUUID, crit,penetration,flags);
        flyingArrows.put(arrow.getUniqueId(),as);
    }

    public ArrowStats removeArrow(UUID uuid) {
        return flyingArrows.remove(uuid);
    }

    public void addSkill(AbstractSkill skill) {
        skillsToAdd.add(skill);
    }

    public void removeSkill(UUID skill) {
        skillsToRemove.add(skill);
    }
}
