package com.github.wisemann64.ngabdungeons.utils;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.PlayerManager;
import com.github.wisemann64.ngabdungeons.mobs.DungeonZombie;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.players.EnumDungeonClass;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Test {

    public static void command(String arg, CommandSender sender) {
        switch (arg) {
            case "level" -> {
                if (!(sender instanceof Player p)) return;
                DPlayer pl = PlayerManager.instance().getPlayer(p);
                if (pl == null) return;
                pl.sendMessage("&3Combat Level: &6" + pl.getCombat().getLevel());
                pl.sendMessage("&3Total Combat XP: &6" + pl.getCombat().getTotalXp());
                for (EnumDungeonClass cl : EnumDungeonClass.values()) {
                    pl.sendMessage("&3" + cl.name() + " Level: &6" + pl.getClassLevel(cl).getLevel());
                    pl.sendMessage("&3" + cl.name() + " XP: &6" + pl.getClassLevel(cl).getTotalXp());
                }
            }
            case "zombie" -> {
                if (!(sender instanceof Player p)) return;
                Location l = p.getLocation();
                new DungeonZombie(l.getWorld(),"Basic Zombie").spawn(l);
            }
            case "describe" -> {
                if (!(sender instanceof Player p)) return;
                DPlayer pl = PlayerManager.instance().getPlayer(p);
                if (pl == null) return;
                pl.getAttributes().describe();
            }
        }

    }

}
