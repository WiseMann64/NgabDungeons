package com.github.wisemann64.ngabdungeons.combat;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.data.DatabaseDriver;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.players.EnumClassSkills;
import com.github.wisemann64.ngabdungeons.players.SkillHandler;
import com.github.wisemann64.ngabdungeons.utils.Utils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
            case CONTACT, DRYOUT, CRAMMING, HOT_FLOOR, FLY_INTO_WALL, DRAGON_BREATH, THORNS, FALLING_BLOCK,
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
            if (p.isInvis()) return;

            p.dealDamage(d);

            if (p.getUltimateSkill() == EnumClassSkills.BERSERK && p.getHealthFraction() < 0.25 && p.ultimateReady()) {
                Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.BERSERK,p.getSkillLevel(EnumClassSkills.BERSERK));
                int cd = (int) (data.getOrDefault("cooldown",0F)*20);
                int duration = (int) (data.getOrDefault("duration",0F)*20);
                p.getSkillHandler().setUltimateCooldown(cd);
                p.sendMessage("&aYou casted ultimate skill &6" + EnumClassSkills.BERSERK.getName() +  "&a.");
                p.addTrigger("BERSERK",duration);
                double bdmg = p.basicAttackFixedCrit(false).getOldValue()*data.getOrDefault("damage",1F);
                Damage bDmg = new Damage(bdmg,false,false,p.getAttributes().getPenetration());
                p.getHandle().getNearbyEntities(3,2,3).stream().map(e -> NgabDungeons.getMob(e.getUniqueId()))
                        .filter(Objects::nonNull).filter(mob -> Utils.xzDistanceSquared(mob.getLocation(),p.getLocation()) < 9).forEach(mob -> {
                            mob.dealDamage(bDmg);
                            mob.setLastDamager(p);
                            mob.fakeDamage();
                            double kbStrength = 0.25*(4-p.getLocation().distance(mob.getLocation()));
                            Vector dir = mob.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0);
                            if (!dir.isZero()) mob.addKnockback(dir.normalize().multiply(kbStrength).setY(0.25));
                        });
                Location loc = p.getLocation();
                double pi = Math.PI;
                Particle.DustOptions dust = new Particle.DustOptions(Color.RED,1F);
                for (int i = 0; i < 60; i++) loc.getWorld().spawnParticle(Particle.REDSTONE,loc.getX()+3*Math.cos(pi*i/30),loc.getY(),
                            loc.getZ()+3*Math.sin(pi*i/30),1,0,0,0,0,dust,true);
                loc.add(0,1,0);
                loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL,loc,320,2.5,1,2.5,0.25,null,true);
                loc.getWorld().playSound(loc,Sound.ENTITY_GENERIC_EXPLODE,2,1);
            }

            return;
        }
        // PLAYER TO MOB
        if (dam1 instanceof DPlayer p && rec instanceof AbstractDungeonMob rec1) {
            if (p.getAttackCooldown() != 0 || rec1.getDamageCooldown().getOrDefault(p.getUniqueId(),0) != 0) {
                v.setCancelled(true);
                return;
            }
            if (rec1.isInvis()) {
                v.setCancelled(true);
                return;
            }
            Damage d = p.basicAttack();

//            TODO SKILLS
            boolean lifeSteal = p.hasTrigger("BLOODLUST");
            Map<String, Float> bloodLust = lifeSteal ? SkillHandler.dataGetter(p, EnumClassSkills.BLOODLUST) : new HashMap<>();
            if (p.hasTrigger("CLEAVE") && p.getAdditionalTrigger("CLEAVE") != 0) {
                float damage = SkillHandler.dataGetter(p,EnumClassSkills.CLEAVE).getOrDefault("damage",0F);

                int hitLeft = p.getAdditionalTrigger("CLEAVE")-1;

                for (int i = 0 ; i < 60 ; i++) {
                    double t = 2*Math.PI*i/60;
                    p.getWorld().spawnParticle(
                            Particle.REDSTONE,p.getLocation().add(0,1.3,0).add(4*Math.cos(t),0,4*Math.sin(t))
                            ,1,0,0,0,0,new Particle.DustOptions(Color.RED,1F),true);
                }
                p.worldSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR,1,0.75F);
                Damage cleave = p.basicAttack();
                Damage cleave1 = new Damage(cleave.getOldValue()*damage,false,cleave.isCrit(),cleave.getPenetration());
                Predicate<Entity> inRadius = e -> Utils.xzDistanceSquared(e.getLocation(),p.getLocation()) < 25;
                Function<Entity,AbstractDungeonMob> getter = e -> NgabDungeons.getMob(e.getUniqueId());
                Consumer<AbstractDungeonMob> hit = mob -> {
                    if (mob == null) return;
                    if (mob == rec1) return;
                    mob.fakeDamage();
                    mob.dealDamage(cleave1);

                    if (lifeSteal) {
                        double amount = cleave1.getNewValue()*bloodLust.getOrDefault("life_steal",0F)*0.01;
                        double cap = p.getMaxHealth()*bloodLust.getOrDefault("cap",0F)*0.01;
                        p.heal(Math.min(amount,cap));
                        p.getWorld().spawnParticle(Particle.HEART,p.getLocation(),4,0.5,1,0.5,0,null,true);
                    }
                };
                p.getHandle().getNearbyEntities(4,1,4).stream().filter(inRadius).map(getter).forEach(hit);

                if (hitLeft == 0) p.removeTrigger("CLEAVE");
                else p.addAdditionalTrigger("CLEAVE",hitLeft);


            } else p.removeTrigger("CLEAVE");
            if (p.getPassiveSkills().contains(EnumClassSkills.FRENZY)) {
                p.addTrigger("FRENZY",80);
                int frenzy = Math.min(p.getAdditionalTrigger("FRENZY")+1,4);
                p.addAdditionalTrigger("FRENZY",frenzy);
            }
            if (p.hasTrigger("HUNTER") && p.getUltimateSkill() == EnumClassSkills.HUNTER) {
                v.setCancelled(true);
                rec1.fakeDamage();
                PotionEffect eff = new PotionEffect(PotionEffectType.SLOW,4,4,false,false,false);
                rec1.addPotionEffect(eff);
                int hits = p.getCounter("HUNTER")+1;

                if (hits >= 3) {
                    Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.HUNTER,p.getSkillLevel(EnumClassSkills.HUNTER));
                    p.worldSound(Sound.BLOCK_PISTON_EXTEND,1.25F,2F);
                    Damage slash = Damage.multiply(p.basicAttackFixedCrit(false),data.getOrDefault("damage",0F));
                    rec1.dealDamage(slash);
                    Utils.hunterSlashParticle(rec1);

                    p.removeCounter("HUNTER");
                } else p.setCounter("HUNTER", hits);
            }

            d.setDamager(p);
            rec1.dealDamage(d);
            rec1.getDamageCooldown().put(p.getUniqueId(),p.getAttackRecover());
            p.setAttackCooldown();

            if (lifeSteal) {
                double amount = d.getNewValue()*bloodLust.getOrDefault("life_steal",0F)*0.01;
                double cap = p.getMaxHealth()*bloodLust.getOrDefault("cap",0F)*0.01;
                p.heal(Math.min(amount,cap));
                p.getWorld().spawnParticle(Particle.HEART,p.getLocation(),4,0.5,1,0.5,0,null,true);
            }


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
        EnumMap<ArrowStats.ArrowFlags,Float> flags = new EnumMap<>(ArrowStats.ArrowFlags.class);
        if (c instanceof DPlayer p) {
            Damage dmg = p.arrowAttack(crit);
            damage = dmg.getOldValue();
            shooter = ArrowStats.ArrowShooter.PLAYER;
            crit = dmg.isCrit();
            penetration = dmg.getPenetration();
//            TODO Skill Buffs

            boolean multi = p.getPassiveSkills().contains(EnumClassSkills.MULTI_ARROW)
                    && new Random().nextDouble() < 0.01*SkillHandler.dataGetter(p,EnumClassSkills.MULTI_ARROW).getOrDefault("chance",0F);

            Arrow second = multi ? a.getWorld().spawnArrow(a.getLocation(),a.getVelocity(),0,0) : null;
            Damage secondDmg = multi ? p.arrowAttack(a.isCritical()) : null;
            double secondDamage = multi ? secondDmg.getOldValue() : 0;
            double secondPen = multi ? secondDmg.getPenetration() : 0;
            boolean secondCrit = multi && secondDmg.isCrit();
            if (multi) {
                Random r = new Random();
                Vector vel = a.getVelocity().normalize();
                vel.rotateAroundX(2*0.0436332*r.nextDouble()-0.0436332).
                        rotateAroundY(2*0.0436332*r.nextDouble()-0.0436332).
                        rotateAroundZ(2*0.0436332*r.nextDouble()-0.0436332);
                second.setVelocity(vel.normalize().multiply(a.getVelocity().length()));
                second.setCritical(a.isCritical());
            }

            if (p.hasTrigger("ANTI_GRAVITY")) {
                dmg = p.arrowAttack(true);
                damage = dmg.getOldValue();
                crit = dmg.isCrit();
                penetration = dmg.getPenetration();

                Map<String, Float> data = SkillHandler.dataGetter(p, EnumClassSkills.ANTI_GRAVITY);
                damage *= data.getOrDefault("damage",1F);
                a.setGravity(false);
                p.removeTrigger("ANTI_GRAVITY");
                a.setVelocity(a.getVelocity().normalize().multiply(2.5));
                flags.put(ArrowStats.ArrowFlags.ANTI_GRAVITY,1F);
                a.setCritical(false);

                if (multi) {
                    Random r = new Random();
                    Vector vel = a.getVelocity().normalize();
                    vel.rotateAroundX(2*0.0174533*r.nextDouble()-0.0174533).
                            rotateAroundY(2*0.0174533*r.nextDouble()-0.0174533).
                            rotateAroundZ(2*0.0174533*r.nextDouble()-0.0174533);
                    second.setGravity(false);
                    second.setVelocity(vel.normalize().multiply(2.5));
                    second.setCritical(false);

                    secondDmg = p.arrowAttack(true);
                    secondDamage *= data.getOrDefault("damage",1F);
                    secondCrit = secondDmg.isCrit();
                }
            }

            if (p.hasTrigger("PENETRATE")) {
                Map<String, Float> data = SkillHandler.dataGetter(p, EnumClassSkills.PENETRATE);
                penetration += data.getOrDefault("pen",0F);
                a.setCritical(false);
                flags.put(ArrowStats.ArrowFlags.IGNORE_DEFENSE,data.getOrDefault("ignore",0F));
                flags.put(ArrowStats.ArrowFlags.PENETRATE,1F);
                p.removeTrigger("PENETRATE");
                p.worldSound(Sound.ENTITY_ITEM_BREAK,1,1);

                if (multi) {
                    second.setCritical(false);
                    secondPen += data.getOrDefault("pen",0F);
                }
            }

            if (multi) NgabDungeons.getMobManager().addArrow(second,secondDamage,shooter,e.getUniqueId(),secondCrit, secondPen,flags);

        } else if (c instanceof AbstractDungeonMob m) {
            damage = m.getRangedAttackPower();
            shooter = ArrowStats.ArrowShooter.MOB;
            penetration = m.getPenetration();
        } else {
            damage = a.getDamage();
            shooter = ArrowStats.ArrowShooter.GENERIC;
        }
        NgabDungeons.getMobManager().addArrow(a,damage,shooter,e.getUniqueId(),crit,penetration,flags);
    }

    private void damageByArrow(CombatEntity rec, Arrow a, EntityDamageByEntityEvent v) {
        if (rec.isInvis()) {
            v.setCancelled(true);
            return;
        }

        ArrowStats arrow = NgabDungeons.getMobManager().removeArrow(a.getUniqueId());

        if (arrow == null) arrow = new ArrowStats(a,0, ArrowStats.ArrowShooter.GENERIC,null,a.isCritical(),0,null);

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

        if (arrow.flags() != null && arrow.flags().containsKey(ArrowStats.ArrowFlags.IGNORE_DEFENSE))
            rec.dealDamageIgnoreDefense(dmg,arrow.flags().get(ArrowStats.ArrowFlags.IGNORE_DEFENSE));
        else rec.dealDamage(dmg);
    }
}
