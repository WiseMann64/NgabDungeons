package com.github.wisemann64.ngabdungeons.skills;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.combat.Damage;
import com.github.wisemann64.ngabdungeons.items.ItemReader;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.github.wisemann64.ngabdungeons.mobs.EnumMobFlags;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.mojang.datafixers.util.Pair;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class SkillThrust extends AbstractSkill {

    private final DPlayer owner;
    private final Function<Entity, AbstractDungeonMob> convert = e -> NgabDungeons.getMob(e.getUniqueId());
    private Predicate<AbstractDungeonMob> inPosition;
    private final double theta;
    private final double ownerX;
    private final double ownerY;
    private final double ownerZ;

    private double distance;
    private Location hitLocation;

    private int timer = 0;
    private final UUID uuid = UUID.randomUUID();

    private final Damage crit;
    private final Damage nonCrit;
    private final double critChance;

    private final float defReduction;
    private final float ignoreDefense;

    private Pair<Double,Double> rot(Pair<Double,Double> zx) {
        double z = zx.getFirst();
        double x = zx.getSecond();
        return new Pair<>(z*cos(theta)+x*sin(theta),-z*sin(theta)+x*cos(theta));
    }

    private Pair<Double,Double> inverseRot(Pair<Double,Double> uv) {
        double u = uv.getFirst();
        double v = uv.getSecond();
        return new Pair<>(u*cos(theta)-v*sin(theta),u*sin(theta)+v*cos(theta));
    }

    public SkillThrust(DPlayer owner, float damage, float ignoreDefense, float defenseReduction, ItemReader castItem) {
        this.owner = owner;
        ownerX = owner.getLocation().getX();
        ownerY = owner.getLocation().getY();
        ownerZ = owner.getLocation().getZ();
        theta = -owner.getLocation().getYaw()*Math.PI/180;
        prepareDistance();
        prepareFilter();
        particle();

        defReduction = defenseReduction;
        this.ignoreDefense = ignoreDefense;
        Damage dmg = owner.simulateBasicAttackFixedCrit(castItem,true);
        crit = new Damage(dmg.getOldValue()*damage,false,true,dmg.getPenetration());
        dmg = owner.simulateBasicAttackFixedCrit(castItem,false);
        nonCrit = new Damage(dmg.getOldValue()*damage,false,false,dmg.getPenetration());

        critChance = owner.getAttributes().getCritChance();

        NgabDungeons.getMobManager().addSkill(this);
    }

    private void prepareDistance() {
        Vector dir = new Vector(sin(theta),0,cos(theta));
        RayTraceResult res = owner.getWorld().rayTraceBlocks(owner.getLocation().add(0,0.1,0),
                dir,12, FluidCollisionMode.NEVER,true);
        distance = res == null ? 12.0 : res.getHitPosition().distance(owner.getLocation().add(0,0.1,0).toVector())-0.5;
        hitLocation = owner.getLocation().clone().add(dir.multiply(distance));
        hitLocation.setYaw(owner.getLocation().getYaw());
        hitLocation.setPitch(0F);
    }

    private void prepareFilter() {
        inPosition = mob -> {
            Location l = mob.getLocation();
            if (l.getY() < ownerY - 0.5 || l.getY() > ownerY + 1.5) return false;
            double z = l.getZ()-ownerZ;
            double x = l.getX()-ownerX;
            Pair<Double,Double> uv = rot(new Pair<>(z,x));
            double u = uv.getFirst();
            double v = uv.getSecond();
            return 0 <= u && u <= distance && -1 <= v && v <= 1;
        };
    }

    private void cast() {
        List<Entity> ne = owner.getHandle().getNearbyEntities(distance+1,2,distance+1);
        ne.stream().map(convert).filter(Objects::nonNull).filter(inPosition).forEach(a -> {
            boolean crit = new Random().nextDouble() < critChance/100;
            Damage res = crit ? this.crit : nonCrit;
            a.fakeDamage();
            a.dealDamageIgnoreDefense(res,ignoreDefense);
            a.getHandle().setVelocity(a.getHandle().getVelocity().clone().add(new Vector(sin(theta)*0.7,0.1,cos(theta)*0.7)));

            a.addTimedFlag(EnumMobFlags.REDUCE_DEFENSE,(double) defReduction,150);
        });
    }

    private void particle() {
        Particle.DustOptions dust = new Particle.DustOptions(Color.RED,1.2F);

        double u = 0;
        int[] vs = {-1,1};
        double[] us = {0,distance};
        while (u < distance) {
            for (double v : vs) {
                Pair<Double, Double> zx = inverseRot(new Pair<>(u,v));
                double x = zx.getSecond();
                double z = zx.getFirst();
                owner.getWorld().spawnParticle(Particle.REDSTONE,ownerX+x,ownerY,ownerZ+z,1,0,0,0,0,dust,true);
            }
            u+=0.2;
        }
        for (int i = 0; i < 11; i++) {
            for (double u2 : us) {
                Pair<Double, Double> zx = inverseRot(new Pair<>(u2,-1+0.2*i));
                double x = zx.getSecond();
                double z = zx.getFirst();
                owner.getWorld().spawnParticle(Particle.REDSTONE,ownerX+x,ownerY,ownerZ+z,1,0,0,0,0,dust,true);
            }
        }
    }

    @Override
    public void tick() {
        if (timer == 3) {
            cast();
            owner.teleport(hitLocation);
            owner.selfSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR,1,2);
            remove();
        }
        timer++;
    }

    @Override
    public void remove() {
        NgabDungeons.getMobManager().removeSkill(uuid);
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }
}
