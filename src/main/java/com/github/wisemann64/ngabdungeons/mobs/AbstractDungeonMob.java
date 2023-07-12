package com.github.wisemann64.ngabdungeons.mobs;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.combat.CombatEntity;
import com.github.wisemann64.ngabdungeons.utils.Utils;
import net.minecraft.world.entity.Mob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

public abstract class AbstractDungeonMob implements CombatEntity {

    protected final CraftServer server = ((CraftServer) Bukkit.getServer());
    protected Mob handle;
    protected final String mobId;
    protected Entity lastDamager = null;
    protected final MobAttributes attributes = new MobAttributes(this);
    private final EnumMap<EntityDamageEvent.DamageCause,Integer> envDamageCooldown = new EnumMap<>(EntityDamageEvent.DamageCause.class);
    private final Map<UUID,Integer> damageCooldown = new HashMap<>();
    private final int level;
    private final String mobName;
    private final String tag = "&8[&7Lv %level&8] &3%name %health&c❤";
    private boolean isInvul;
    private boolean readyTick = false;

    public AbstractDungeonMob(World w, String name, int level) {
        this(w,name,level,null);
        readyTick = true;
    }

    public AbstractDungeonMob(World w, String name, int level, String id) {
        this.level = level;
        mobId = NgabDungeons.getMobManager().putMobToRegistry(this,id);
        mobName = name;
        createHandle(w);
        initAttribute();
        readyTick = true;
    }

    AbstractDungeonMob(World w, String name, int level, boolean delayCreateHandle, String id) {
        this.level = level;
        mobId = NgabDungeons.getMobManager().putMobToRegistry(this,id);
        mobName = name;
        if (!delayCreateHandle) {
            createHandle(w);
            initAttribute();
            readyTick = true;
        }
    }

    public void tick() {
        if (!readyTick) return;

        handle.getBukkitEntity().setCustomNameVisible(true);
        handle.getBukkitEntity().setCustomName(parseTag(tag));

        for (EntityDamageEvent.DamageCause v : envDamageCooldown.keySet()) {
            int cd = envDamageCooldown.get(v)-1;
            if (cd == 0) envDamageCooldown.remove(v);
            else envDamageCooldown.put(v,cd);
        }

        for (UUID v : damageCooldown.keySet()) {
            int cd = damageCooldown.get(v) - 1;
            if (cd == 0) damageCooldown.remove(v);
            else damageCooldown.put(v,cd);
        }
    }

    private String parseTag(String s) {
        if (s.contains("%health")) {
            String color = getHealthFraction() > 0.5 ? "&a" : getHealthFraction() > 0.2 ? "&e" : "&4";
            s = s.replace("%health", color + Math.round(attributes.getHealth()));
        }
        if (s.contains("%name")) s = s.replace("%name",mobName);
        if (s.contains("%level")) s = s.replace("%level",String.valueOf(level));
        return Utils.c(s);
    }

    public Entity getHandle() {
        return handle.getBukkitEntity();
    }
    public abstract Mob getMobHandle();
    public MobAttributes getAttributes() {
        return attributes;
    }

    public void spawn(Location l) {
        if (handle == null) return;
        setLocation(handle,l);
        ((CraftWorld)l.getWorld()).getHandle().addFreshEntity(handle);
    }

    public void remove() {
//        if (lastDamager != null) {
//            DPlayer p = NgabDungeons.playerManager().getPlayer(lastDamager.getUniqueId());
//            if (p != null) p.addXp(getXpYield());
//        }
        if (getHandle() instanceof LivingEntity g) g.setHealth(0);
        else getHandle().remove();
        NgabDungeons.getMobManager().removeMobFromRegistry(mobId);
    }

    public void quickRemove() {
        lastDamager = null;
        getHandle().remove();
        NgabDungeons.getMobManager().removeMobFromRegistry(mobId);
    }

    public static void setLocation(net.minecraft.world.entity.Entity handle, Location loc) {
        handle.teleportTo(loc.getX(),loc.getY(),loc.getZ());
        handle.setYRot(loc.getYaw());
        handle.setXRot(loc.getPitch());
    }

    public double dealDamage(double amount, CombatEntity damager) {
        if (damager != null) lastDamager = damager.getHandle();
        setHealth(getHealth()-amount);
        return amount;
    }

    public void die() {
        readyTick = false;
        remove();
//        if (trigger != null) trigger.mobDie();
    }

    public void setNoAI(boolean val) {
        handle.setNoAi(val);
    }
    public Location getLocation() {
        return this.getHandle().getLocation();
    }
    public abstract void createHandle(World world);
    public abstract void initAttribute();
    public void setInvul(boolean var) {
        isInvul = var;
    }
    public void fakeDamage() {
        handle.hurt(handle.damageSources().generic(),0F);
    }
    public CraftWorld getWorld() {
        return handle.getLevel().getWorld();
    }

    public boolean isInvis() {
        return isInvul;
    }

    public double getHealth() {
        return attributes.getHealth();
    }
    public double getMaxHealth() {
        return attributes.getMaxHealth();
    }
    public float getHealthFraction() {
        return (float) (attributes.getHealth()/ attributes.getMaxHealth());
    }
    public double getAttackPower() {
        return attributes.getAttackPower();
    }
    public double getRangedAttackPower() {
        return getAttackPower();
    }
    public double getDefense() {
        return attributes.getDefense();
    }
    public double getPenetration() {
        return attributes.getPenetration();
    }
    public int getLevel() {
        return level;
    }
    public abstract List<String> drops();

    public Location getEyeLocation() {
        if (handle.getBukkitEntity() instanceof LivingEntity li) return li.getEyeLocation();
        return getLocation();
    }
    public void setHealth(double health) {
        attributes.setHealth(health);
        if (health <= 0) {
            attributes.setHealth(0);
            handle.getBukkitEntity().setCustomName(parseTag(tag));
            die();
        }
    }
    public void heal(double amount) {
        attributes.setHealth(attributes.getHealth()+amount);
    }
    public void sendMessage(Object msg) {

    }
    public EnumMap<EntityDamageEvent.DamageCause, Integer> getEnvDamageCooldown() {
        return envDamageCooldown;
    }

    public Map<UUID, Integer> getDamageCooldown() {
        return damageCooldown;
    }

    public String getMobName() {
        return mobName;
    }

    public int getXpYield() {
        return 0;
    }

    public void setLastDamager(CombatEntity lastDamager) {
        this.lastDamager = lastDamager.getHandle();
    }
}
