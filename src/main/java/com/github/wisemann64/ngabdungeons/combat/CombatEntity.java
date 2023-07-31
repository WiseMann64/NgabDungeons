package com.github.wisemann64.ngabdungeons.combat;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.EnumMap;
import java.util.Random;

public interface CombatEntity {

    boolean isInvis();
    double getHealth();
    double getMaxHealth();
    float getHealthFraction();
    double getAttackPower();
    double getRangedAttackPower();
    double getDefense();
    int getLevel();
    Location getLocation();
    Location getEyeLocation();
    void setHealth(double health);
    void heal(double amount);
    double dealDamage(double finalDamage, CombatEntity damager);
    Entity getHandle();
    void sendMessage(Object msg);
    EnumMap<EntityDamageEvent.DamageCause, Integer> getEnvDamageCooldown();
    default void dealDamage(Damage damage) {
        double pen = Math.max(0,damage.getPenetration());
        double def = damage.isIgnoreDefense() ? 0 : Math.max(getDefense()-pen,-500);
        double mul = 1 - (def < 0 ? def/120d : def/(def+120));
        damage.setNewValue(dealDamage(damage.getOldValue()*mul,damage.getDamager()));
        showIndicator(damage);
    }
    default void dealDamageIgnoreDefense(Damage damage, float percent) {
        double pen = Math.max(0,damage.getPenetration());
        double def = damage.isIgnoreDefense() ? 0 : Math.max(getDefense()-pen,-500);
        if (def > 0) def *= (100-percent)/100;
        double mul = 1 - (def < 0 ? def/120d : def/(def+120));
        damage.setNewValue(dealDamage(damage.getOldValue()*mul,damage.getDamager()));
        showIndicator(damage);
    }
    default void showIndicator(Damage damage) {
        Location loc = getEyeLocation().clone();
        Random random = new Random();
        loc.add(1.4*random.nextDouble()-0.7,0.6*random.nextDouble()-0.8,1.4*random.nextDouble()-0.7);
        StringBuilder value = new StringBuilder(damage.isCrit() ?  "&4✧" : "&c");
        value.append(String.format("%.2f",damage.getNewValue()));
        ArmorStand label = NgabDungeons.getWorld().spawn(loc,ArmorStand.class, e -> {
            e.setVisible(false);
            e.setBasePlate(false);
            e.setCustomNameVisible(true);
            e.setGravity(false);
            e.setMarker(true);
            e.setInvulnerable(true);
            e.setCustomName(Utils.c(value.toString()));
        });
        new BukkitRunnable(){
            @Override
            public void run() {
                label.remove();
            }
        }.runTaskLater(NgabDungeons.getPlugin(),60L);
    }
    default void addKnockback(Vector val) {
        getHandle().setVelocity(getHandle().getVelocity().add(val));
    }

}
