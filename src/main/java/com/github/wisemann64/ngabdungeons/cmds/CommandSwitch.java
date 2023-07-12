package com.github.wisemann64.ngabdungeons.cmds;

import com.github.wisemann64.ngabdungeons.ItemManager;
import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.items.ItemGenerator;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.github.wisemann64.ngabdungeons.players.EnumDungeonClass;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandSwitch implements TabExecutor {

    private final String[] clss = {"ARCHER","FIGHTER","TANK","SUPPORT"};
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        DPlayer p = NgabDungeons.getPlayer(player);
        if (p == null) return false;
        if (args.length > 0) {
            EnumDungeonClass cls = EnumDungeonClass.valueOf(args[0]);
            p.switchClass(cls);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String t = args[0];
            return new ArrayList<>(Arrays.stream(clss).filter(s -> s.toLowerCase().contains(t.toLowerCase())).toList());
        }
        return Collections.emptyList();
    }
}
