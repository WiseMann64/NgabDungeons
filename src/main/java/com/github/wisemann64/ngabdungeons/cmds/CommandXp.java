package com.github.wisemann64.ngabdungeons.cmds;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.players.EnumDungeonClass;
import com.github.wisemann64.ngabdungeons.players.EnumLevelType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandXp implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        DPlayer p = NgabDungeons.getPlayer(player);
        if (p == null) return false;
        if (args.length > 1) {
            EnumLevelType type = EnumLevelType.valueOf(args[0]);
            float val = Float.parseFloat(args[1]);
            if (type == EnumLevelType.COMBAT) {
                p.getCombat().addXp(val);
                return true;
            }
            p.addClassXp(EnumDungeonClass.valueOf(type.name()),val);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            EnumDungeonClass[] a = EnumDungeonClass.values();
            List<String> strs = new ArrayList<>();
            for (EnumDungeonClass e : a) strs.add(e.name());
            return strs;
        }
        return Collections.emptyList();
    }
}
