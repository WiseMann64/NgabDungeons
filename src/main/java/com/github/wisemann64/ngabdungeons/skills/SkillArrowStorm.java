package com.github.wisemann64.ngabdungeons.skills;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.combat.ArrowStats;
import com.github.wisemann64.ngabdungeons.combat.Damage;
import com.github.wisemann64.ngabdungeons.items.ItemReader;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SkillArrowStorm extends AbstractSkill {

    private final DPlayer shooter;
    private final double critChance;
    private final Damage noCritDamage;
    private final Damage critDamage;
    private final float damageMultiplier;
    private final int groupCount;

    private final UUID uuid = UUID.randomUUID();

    private final World world;
    private final Vector initDirection;

    private final ArmorStand handle;
    private final ArmorStand head;
    private final Predicate<Entity> filter;

    private Location hitLocation;
    private int timer = 0;
    private int phase = 1;
    private int shoots = 0;

    public SkillArrowStorm(DPlayer owner, float damageMultiplier, int groupCount, ItemReader castItem) {
        this.shooter = owner;
        this.damageMultiplier = damageMultiplier;
        this.groupCount = groupCount;
        critChance = owner.getAttributes().getCritChance();

        critDamage = owner.simulateArrowAttackFixedCrit(castItem,true);
        noCritDamage = owner.simulateArrowAttackFixedCrit(castItem,false);

        world = shooter.getHandle().getWorld();
        initDirection = shooter.getLocation().getDirection().multiply(0.75);

        handle = world.spawn(shooter.getEyeLocation(), ArmorStand.class,as -> {
            as.setVisible(false);
            as.setCustomNameVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setVelocity(this.initDirection);
            as.setMarker(true);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                as.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
                as.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
            }
        });
        head = world.spawn(shooter.getLocation(), ArmorStand.class, as -> {
            as.setMarker(true);
            as.setVisible(false);
            as.setCustomNameVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setMarker(true);
            ItemStack head = Utils.createHead("http://textures.minecraft.net/texture/7e509f06b8dc384358ff2472ab62ce6fdc2f646e338efdc3c9fb05ddc431f64");
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                as.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
                as.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
            }
            as.getEquipment().setHelmet(head);
        });
        filter = ent -> {
            UUID u = ent.getUniqueId();
            if (u.equals(shooter.getUniqueId()) || u.equals(handle.getUniqueId()) || u.equals(head.getUniqueId())) return false;
            return NgabDungeons.getMobManager().getMob(u) != null;
        };

        NgabDungeons.getMobManager().addSkill(this);
    }

    private void traceHitLocation(Location start) {
        RayTraceResult res = world.rayTraceBlocks(start,new Vector(0,-1,0),25,FluidCollisionMode.ALWAYS,true);
        hitLocation = res == null ? start.clone().add(0,-25,0) : res.getHitPosition().toLocation(world);
    }

    @Override
    public void tick() {
        if (phase == 1) {
            if (timer > 60) {
                traceHitLocation(handle.getLocation());
                phase = 2;
                timer = 0;
                return;
            }
            RayTraceResult res = handle.getWorld()
                    .rayTrace(handle.getLocation(),initDirection,initDirection.length()*1.05, FluidCollisionMode.NEVER,true,0.3,filter);
            if (res != null) {
                phase = 2;
                timer = 0;
                Entity fe = res.getHitEntity();
                handle.teleport(res.getHitBlock() == null ? res.getHitPosition().toLocation(world) : handle.getLocation());
                head.teleport(handle.getLocation().clone().add(0,-1.44,0));
                if (fe != null) {
                    AbstractDungeonMob mob = NgabDungeons.getMob(fe.getUniqueId());
                    hitLocation = res.getHitPosition().toLocation(world);
                    Consumer<AbstractDungeonMob> hit = e -> {
                        if (e == null) return;
                        boolean crit = new Random().nextDouble() < critChance / 100;
                        Damage dmg = crit ? critDamage : noCritDamage;
                        e.dealDamage(dmg);
                        e.fakeDamage();
                        Vector vel = e.getHandle().getVelocity();
                        vel.add(initDirection.clone().multiply(0.7).setY(0.4));
                        e.getHandle().setVelocity(vel);
                    };
                    hit.accept(mob);
                    fe.getNearbyEntities(1.75, 1.75, 1.75).stream().filter(filter).map(e -> NgabDungeons.getMob(e.getUniqueId())).forEach(hit);
                } else traceHitLocation(handle.getLocation());
            } else {
                handle.teleport(handle.getLocation().add(initDirection));
                head.teleport(handle.getLocation().clone().add(0,-1.44,0));
            }
            world.spawnParticle(Particle.REDSTONE,handle.getLocation(),1,0,0,0,0,new Particle.DustOptions(Color.AQUA,1F),true);
            world.spawnParticle(Particle.REDSTONE,handle.getLocation(),4,0.2,0.2,0.2,0,new Particle.DustOptions(Color.WHITE,0.7F),true);
            timer++;
        } else if (phase == 2) {
            if (timer == 8) {
                timer = 0;
                phase = 3;
                return;
            }
            RayTraceResult res = handle.getWorld().rayTraceBlocks(handle.getLocation(),new Vector(0,1,0),1,FluidCollisionMode.NEVER,true);
            if (res != null) {
                timer = 0;
                phase = 3;
                return;
            }
            handle.teleport(handle.getLocation().add(0,1,0));
            float yaw = head.getLocation().getYaw() + 18;
            head.teleport(handle.getLocation().clone().add(0,-1.44,0));
            Location loc = head.getLocation();
            loc.setYaw(yaw);
            head.teleport(loc);
            world.spawnParticle(Particle.FIREWORKS_SPARK,handle.getLocation(),1,0,0,0,0,null,true);
            world.spawnParticle(Particle.REDSTONE,handle.getLocation(),4,0.2,0.2,0.2,0,new Particle.DustOptions(Color.WHITE,0.7F),true);
            timer++;
        } else if (phase == 3) {
            Location loc = head.getLocation();
            loc.setYaw(loc.getYaw()+18);
            head.teleport(loc);
            if (timer % 3 == 0 && timer > 9) {
                shoots++;
                Random rand = new Random();
                for (int i = 0 ; i < 5 ; i++) {
                    double r = rand.nextDouble()*7;
                    double t = rand.nextDouble()*6.28318530718;

                    Vector direction = hitLocation.toVector().
                            add(new Vector(r*Math.cos(t),0,r*Math.sin(t))).subtract(handle.getLocation().toVector()).
                            normalize();

                    boolean crit = rand.nextDouble() < critChance/100;

                    Arrow arr = world.spawnArrow(handle.getLocation(),direction,0,0);
                    arr.setVelocity(direction);
                    arr.setShooter(shooter.getHandle());
                    Damage dmg = crit ? critDamage : noCritDamage;
                    arr.setCritical(false);
                    NgabDungeons.getMobManager().
                            addArrow(arr,dmg.getOldValue()*damageMultiplier/100, ArrowStats.ArrowShooter.PLAYER,shooter.getUniqueId(),crit,dmg.getPenetration());
                }
            }
            if (shoots == groupCount){
                remove();
                return;
            }
            timer++;
            world.spawnParticle(Particle.FIREWORKS_SPARK,handle.getLocation(),3,0.1,0.1,0.3,0.3,null,true);
            world.spawnParticle(Particle.REDSTONE,handle.getLocation(),10,1,0.6,1,0.1,new Particle.DustOptions(Color.GRAY,0.7F),true);
        }
    }

    @Override
    public void remove() {
        handle.remove();
        head.remove();
        NgabDungeons.getMobManager().removeSkill(uuid);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }
}
