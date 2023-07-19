package com.github.wisemann64.ngabdungeons.skills;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.combat.Damage;
import com.github.wisemann64.ngabdungeons.items.ItemReader;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.github.wisemann64.ngabdungeons.mobs.EnumMobFlags;
import com.github.wisemann64.ngabdungeons.mobs.MobFlag;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public class SkillBoneBreaker extends AbstractSkill {

    private final DPlayer shooter;
    private final Damage crit;
    private final Damage nonCrit;
    private final double mainDamageMultiplier;
    private final double fragmentDamageMultiplier;
    private final double defenseReduction;
    private final double critChance;
    private final UUID uuid = UUID.randomUUID();
    private final UUID groupId;

    private final World world;
    private final Vector direction;

    private final ArmorStand handle;
    private final ArmorStand head;
    private final Predicate<Entity> filter;

    private AbstractDungeonMob trigger;

    private Location hitLocation;
    private int timer = 0;

    public SkillBoneBreaker(SkillBoneBreaker parent, Vector direction) {
        groupId = parent.groupId;

        this.shooter = parent.shooter;
        mainDamageMultiplier = parent.fragmentDamageMultiplier;
        fragmentDamageMultiplier = 0;
        defenseReduction = parent.defenseReduction;
        critChance = parent.critChance;
        crit = parent.crit;
        nonCrit = parent.nonCrit;
        trigger = parent.trigger;

        world = shooter.getHandle().getWorld();
        this.direction = direction;
        Location spawnLoc = parent.hitLocation.clone();
        spawnLoc.setDirection(direction);

        handle = world.spawn(spawnLoc,ArmorStand.class,as -> {
            as.setVisible(false);
            as.setCustomNameVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                as.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
                as.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
            }
        });

        head = spawnLoc.getWorld().spawn(spawnLoc.add(0,-1.44,0),ArmorStand.class, as -> {
            as.setMarker(true);
            as.setVisible(false);
            as.setCustomNameVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            ItemStack head = Utils.createHead("http://textures.minecraft.net/texture/89e70162143c7caa20e303eea314a9aed5db9cc68435e783b3c59af43bf43635");
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                as.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
                as.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
            }
            as.getEquipment().setHelmet(head);
        });

        filter = ent -> {
            UUID u = ent.getUniqueId();
            UUID trigger = this.trigger == null ? null : this.trigger.getHandle().getUniqueId();
            if (u.equals(shooter.getUniqueId()) || u.equals(handle.getUniqueId()) || u.equals(head.getUniqueId()) || u.equals(trigger)) return false;
            AbstractDungeonMob mob = NgabDungeons.getMob(u);
            if (mob == null) return false;
            return !mob.hasFlag(EnumMobFlags.BONE_BREAKER_HIT,groupId);
        };

        NgabDungeons.getMobManager().addSkill(this);
    }
    public SkillBoneBreaker(DPlayer shooter, double base, double frag, double defReduction, double critChance, ItemReader castItem) {
        groupId = UUID.randomUUID();

        this.shooter = shooter;
        mainDamageMultiplier = base;
        fragmentDamageMultiplier = frag;
        defenseReduction = defReduction;
        this.critChance = critChance;

        world = shooter.getHandle().getWorld();
        this.direction = shooter.getLocation().getDirection();

        handle = world.spawn(shooter.getEyeLocation(), ArmorStand.class,as -> {
            as.setVisible(false);
            as.setCustomNameVisible(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setVelocity(this.direction);
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
            ItemStack head = Utils.createHead("http://textures.minecraft.net/texture/86ce9b18ca238ead6515b0173534a7f34d28bc51c0188e2db06845c17e62f10a");
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                as.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
                as.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
            }
            as.getEquipment().setHelmet(head);
        });

        filter = ent -> {
            UUID u = ent.getUniqueId();
            UUID trigger = this.trigger == null ? null : this.trigger.getHandle().getUniqueId();
            if (u.equals(shooter.getUniqueId()) || u.equals(handle.getUniqueId()) || u.equals(head.getUniqueId()) || u.equals(trigger)) return false;
            AbstractDungeonMob mob = NgabDungeons.getMob(u);
            if (mob == null) return false;
            return !mob.hasFlag(EnumMobFlags.BONE_BREAKER_HIT,groupId);
        };

        crit = shooter.simulateArrowAttackFixedCrit(castItem,true);
        nonCrit = shooter.simulateArrowAttackFixedCrit(castItem,false);

        NgabDungeons.getMobManager().addSkill(this);
    }

    private void breakParticle() {
        if (hitLocation == null) return;
        Particle.DustOptions dust = new Particle.DustOptions(Color.WHITE,1.2F);
        BlockData block = Bukkit.createBlockData(Material.BONE_BLOCK);
        world.spawnParticle(Particle.REDSTONE,hitLocation,30,0.2,0.2,0.2,0.1,dust,true);
        world.spawnParticle(Particle.BLOCK_CRACK,hitLocation,30,0.2,0.2,0.2,0.1,block,true);
    }

    public void hit() {
        remove();
//        TODO damage and effects
        boolean crit = new Random().nextDouble() < critChance/100;
        Damage res = crit ? this.crit : nonCrit;
        Damage newDamage = new Damage(res.getOldValue()*mainDamageMultiplier*0.01,false,crit,res.getPenetration());
        trigger.dealDamage(newDamage);
        trigger.fakeDamage();
        Vector vel = trigger.getHandle().getVelocity();
        vel.add(direction.clone().multiply(1.5).setY(0.1));
        trigger.getHandle().setVelocity(vel);

        trigger.addFlag(EnumMobFlags.BONE_BREAKER_HIT,groupId);
        trigger.addTimedFlag(EnumMobFlags.REDUCE_DEFENSE,defenseReduction,200);

        world.playSound(hitLocation,Sound.ENTITY_ITEM_BREAK,1,1);
    }

    public void spawnAnother() {
        Vector dir = direction.clone();
        Location loc = new Location(world,0,0,0);
        loc.setDirection(dir);
        loc.setYaw(loc.getYaw()-15F);
        dir = loc.getDirection();
        for (int i = 0 ; i < 5 ; i++) {
            new SkillBoneBreaker(this,dir.clone().multiply(1.6));
            loc.setYaw(loc.getYaw()+7.5F);
            dir = loc.getDirection();
        }
    }

    public void tick() {
        handle.teleport(handle.getLocation().add(direction));
        head.teleport(handle.getLocation().clone().add(0,-1.44,0));
        RayTraceResult res = handle.getWorld().rayTrace(handle.getLocation(),direction,direction.length()*1.05, FluidCollisionMode.NEVER,true,0.3,filter);
        if (res != null) {
            hitLocation = res.getHitPosition().toLocation(handle.getWorld());
            breakParticle();
            Entity fe = res.getHitEntity();
            if (fe != null) {
                boolean spawnAnother = trigger == null;
                trigger = NgabDungeons.getMob(fe.getUniqueId());
                hit();
                if (spawnAnother) spawnAnother();
                return;
            }
        }
        Particle.DustOptions dust = new Particle.DustOptions(Color.WHITE,1F);
        world.spawnParticle(Particle.REDSTONE,handle.getLocation(),1,0,0,0,0,dust,true);
        timer++;
        if (timer == 100) remove();
    }

    public void remove() {
        handle.remove();
        head.remove();
        NgabDungeons.getMobManager().removeSkill(uuid);
    }

    public UUID getUniqueId() {
        return uuid;
    }

}
