package com.github.wisemann64.ngabdungeons.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.codehaus.plexus.util.Base64;

import java.lang.reflect.Field;
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

}
