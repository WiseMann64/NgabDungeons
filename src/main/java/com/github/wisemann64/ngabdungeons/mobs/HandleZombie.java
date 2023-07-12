package com.github.wisemann64.ngabdungeons.mobs;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class HandleZombie extends Zombie {

    private final AbstractDungeonMob handler;
    public HandleZombie(Level world, DungeonZombie handler) {
        super(EntityType.ZOMBIE, world);
        this.handler = handler;
    }

    public DungeonZombie getHandler() {
        return (DungeonZombie) handler;
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        this.invulnerableTime = 0;
        return super.hurt(damagesource, f);
    }
}
