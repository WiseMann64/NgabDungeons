package com.github.wisemann64.ngabdungeons.combat;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class CombatListeners implements Listener {

    @EventHandler
    public void envDamage(EntityDamageEvent v) {
        if (v.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            v.setCancelled(true);
            return;
        }
        CombatEntity c = NgabDungeons.getCombatEntity(v.getEntity().getUniqueId());
        if (c == null) return;
        double initDamage = v.getDamage();
        v.setDamage(0);
        switch (v.getCause()) {
            case CONTACT, DRYOUT, CRAMMING, HOT_FLOOR, FLY_INTO_WALL, CUSTOM, DRAGON_BREATH, THORNS, FALLING_BLOCK,
                    POISON, MAGIC, FIRE, FIRE_TICK, WITHER, MELTING, FREEZE, SONIC_BOOM -> {
                if (c.getEnvDamageCooldown().getOrDefault(v.getCause(),0) != 0) {
                    v.setCancelled(true);
                    return;
                }
                Damage dam = new Damage(initDamage*5, true, false,0);
                c.dealDamage(dam);
                c.getEnvDamageCooldown().put(v.getCause(),10);
            }
            case ENTITY_ATTACK, PROJECTILE, ENTITY_EXPLOSION -> {
                v.setDamage(initDamage);
                if (v instanceof EntityDamageByEntityEvent v1) damageByEntity(v1);
            }
            case FALL -> {
                float fallDistance = c.getHandle().getFallDistance()-3;
                float frac = fallDistance/25;
                Damage dam = new Damage(frac*c.getMaxHealth(),true,false,0);
                c.dealDamage(dam);
            }
            case SUFFOCATION, DROWNING, STARVATION -> {
                if (c.getEnvDamageCooldown().getOrDefault(v.getCause(),0) != 0) {
                    v.setCancelled(true);
                    return;
                }
                Damage dam = new Damage(0.05*c.getMaxHealth(),true,false,0);
                c.dealDamage(dam);
                c.getEnvDamageCooldown().put(v.getCause(),10);
            }
            case LAVA -> {
                if (c.getEnvDamageCooldown().getOrDefault(v.getCause(),0) != 0) {
                    v.setCancelled(true);
                    return;
                }
                Damage dam = new Damage(0.1*c.getMaxHealth(),true,false,0);
                c.dealDamage(dam);
                c.getEnvDamageCooldown().put(v.getCause(),10);
            }
            case VOID, SUICIDE -> {
                Damage dam = new Damage(c.getMaxHealth(), true, true,0);
                c.dealDamage(dam);
            }
            case LIGHTNING, BLOCK_EXPLOSION -> v.setCancelled(true);
        }
    }

    private void damageByEntity(EntityDamageByEntityEvent v) {
        CombatEntity rec = NgabDungeons.getCombatEntity(v.getEntity().getUniqueId());
        if (rec == null) return;
        double dmg = v.getDamage();
        v.setDamage(0);
        Entity dam0 = v.getDamager();
        if (dam0 instanceof Arrow a) {
            damageByArrow(rec,a,v);
            return;
        }
        CombatEntity dam1 = NgabDungeons.getCombatEntity(dam0.getUniqueId());
        if (dam1 == null) {
            Damage damage = new Damage(dmg*5,false,false,0);
            rec.dealDamage(damage);
            return;
        }
        // PLAYER TO PLAYER
        if (rec instanceof DPlayer && dam1 instanceof DPlayer) {
            v.setCancelled(true);
            return;
        }
        // MOB TO PLAYER
        if (rec instanceof DPlayer p && dam1 instanceof AbstractDungeonMob dam2) {
            double damage = v.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ? 0 : dam2.getAttackPower();
            Damage d = new Damage(damage,false,false,dam2.getPenetration());
            if (p.isInvis()) d.setNewValue(0);
            p.dealDamage(d);
            return;
        }
        // PLAYER TO MOB
        if (dam1 instanceof DPlayer p && rec instanceof AbstractDungeonMob rec1) {
            if (p.getAttackCooldown() != 0 || rec1.getDamageCooldown().getOrDefault(p.getUniqueId(),0) != 0) {
                v.setCancelled(true);
                return;
            }
            Damage d = p.basicAttack();
            d.setDamager(p);
            if (rec1.isInvis()) {
                v.setCancelled(true);
                return;
            }
            rec1.dealDamage(d);
            rec1.getDamageCooldown().put(p.getUniqueId(),p.getAttackRecover());
            p.setAttackCooldown();
            return;
        }
        // MOB TO MOB
        if (dam1 instanceof AbstractDungeonMob dam2 && rec instanceof AbstractDungeonMob rec1) {
            double damage = v.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ? 0 : dam2.getAttackPower();
            Damage d = new Damage(damage,false,false,dam2.getPenetration());
            d.setDamager(dam2);
            if (rec1.isInvis()) {
                v.setCancelled(true);
                return;
            }
            rec1.dealDamage(d);
        }
    }

    @EventHandler
    public void event(ProjectileLaunchEvent v) {
        if (!(v.getEntity() instanceof Arrow a)) return;
        if (!(a.getShooter() instanceof Entity e)) return;
        CombatEntity c = NgabDungeons.getCombatEntity(e.getUniqueId());
        boolean crit = a.isCritical();
        double damage;
        double penetration=0;
        ArrowStats.ArrowShooter shooter;
        if (c instanceof DPlayer p) {
            Damage dmg = p.arrowAttack(crit);
            damage = dmg.getOldValue();
            shooter = ArrowStats.ArrowShooter.PLAYER;
            crit = dmg.isCrit();
            penetration = dmg.getPenetration();
        } else if (c instanceof AbstractDungeonMob m) {
            damage = m.getRangedAttackPower();
            shooter = ArrowStats.ArrowShooter.MOB;
            penetration = m.getPenetration();
        } else {
            damage = a.getDamage();
            shooter = ArrowStats.ArrowShooter.GENERIC;
        }
        NgabDungeons.getMobManager().addArrow(a,damage,shooter,e.getUniqueId(),crit,penetration);
    }

    private void damageByArrow(CombatEntity rec, Arrow a, EntityDamageByEntityEvent v) {
        if (rec.isInvis()) {
            v.setCancelled(true);
            return;
        }

        ArrowStats arrow = NgabDungeons.getMobManager().removeArrow(a.getUniqueId());

        if (arrow == null) {
            arrow = new ArrowStats(a,0, ArrowStats.ArrowShooter.GENERIC,null,a.isCritical(),0);
        }

        double damage = arrow.damage();
        ArrowStats.ArrowShooter shooter = arrow.shooter();
        boolean crit = arrow.critical();
        CombatEntity damager = NgabDungeons.getCombatEntity(arrow.shooterUUID());
        if (rec instanceof DPlayer && shooter == ArrowStats.ArrowShooter.PLAYER) {
            v.setCancelled(true);
            return;
        }
        Damage dmg = new Damage(damage,false,crit,arrow.penetration());
        dmg.setDamager(damager);
        rec.dealDamage(dmg);
    }
}
