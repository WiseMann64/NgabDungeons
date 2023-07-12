package com.github.wisemann64.ngabdungeons.mobs;

import net.minecraft.world.level.Level;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;

import java.util.List;

public class DungeonZombie extends AbstractDungeonMob {

    public DungeonZombie(World w, String name) {
        super(w, name, 0);
    }

    @Override
    public HandleZombie getMobHandle() {
        return (HandleZombie) handle;
    }

    @Override
    public void createHandle(World world) {
        Level ws = ((CraftWorld)world).getHandle();
        this.handle = new HandleZombie(ws,this);
    }

    @Override
    public void initAttribute() {
        MobAttributes a = getAttributes();
        a.setMaxHealth(50000);
        a.setHealth(50000);
        a.setAttackPower(20);
        a.setDefense(500);
    }

    @Override
    public List<String> drops() {
        return List.of("1:BOW:1","0.75:PEDANG_LOREM_IPSUM:1~5");
    }
    @Override
    public int getXpYield() {
        return 25;
    }

}
