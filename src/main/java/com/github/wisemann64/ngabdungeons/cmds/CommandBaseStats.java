package com.github.wisemann64.ngabdungeons.cmds;

import com.github.wisemann64.ngabdungeons.PlayerManager;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.players.EnumStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class CommandBaseStats implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return false;
        DPlayer pl = PlayerManager.instance().getPlayer(p);
        if (pl == null) return false;
        EnumMap<EnumStats, Double> a = pl.getAttributes().getBaseStats();
        if (args.length < 2) return false;
        EnumStats stats = EnumStats.valueOf(args[0]);
        int val = Integer.parseInt(args[1]);
        a.put(stats, (double) val);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> res = new ArrayList<>();
            Arrays.stream(EnumStats.values()).forEach(es -> res.add(es.toString()));
            return res;
        }
        return null;
    }
}
