package com.github.wisemann64.ngabdungeons.utils;

import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.codehaus.plexus.util.Base64;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

public class Utils {

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&',s);
    }

    public static ItemStack createHead(String link) {
        ItemStack ret = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) ret.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", link).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception ignored) {
        }
        ret.setItemMeta(meta);
        return ret;
    }

    public static double xzDistanceSquared(Location loc1, Location loc2) {
        return loc1.toVector().setY(0).distanceSquared(loc2.toVector().setY(0));
    }

    public static void hunterSlashParticle(AbstractDungeonMob mob) {
        Location l = mob.getLocation();
        World w = l.getWorld();

        double r = 0.5;
        double length = Math.sqrt(1.06);

        Random rn = new Random();
        double theta = rn.nextDouble()*2*Math.PI;

        double x = r*Math.cos(theta)+l.getX();
        double y = l.getY()+0.1;
        double z = r*Math.sin(theta)+l.getZ();

        double[] dist = {l.getX()-x, 0.9, l.getZ()-z};
        double mult = 0.15/length;
        double[] dP = {dist[0]*mult,dist[1]*mult,dist[2]*mult};

        Particle.DustOptions dust = new Particle.DustOptions(Color.RED, 1.2F);

        for (int i = 0; i < 14 ; i++) {
            w.spawnParticle(Particle.REDSTONE, x,y,z, 1, 0, 0, 0, 0, dust);
            x += dP[0];
            y += dP[1];
            z += dP[2];
        }
    }

}
