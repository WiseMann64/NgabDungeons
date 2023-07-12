package com.github.wisemann64.ngabdungeons.cmds;

import com.github.wisemann64.ngabdungeons.ItemManager;
import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.items.ItemGenerator;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandItem implements TabExecutor {

    private final String[] params = {"get"};
    private final List<String> items = new ArrayList<>(ItemManager.getInstance().getItemIds());

    public CommandItem() {
        items.sort(Comparator.naturalOrder());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        DPlayer p = NgabDungeons.getPlayer(player);
        if (p == null) return false;
        if (args.length > 0) {
            if (args[0].equals("get")) {
                if (args.length > 1) {
                    String id = args[1];
                    int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                    if (!items.contains(id)) {
                        p.sendMessage("&cUnknown item id '"+id+"'");
                        return false;
                    }
                    p.giveItem(ItemGenerator.rawItem(id,amount));
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String str, @NotNull String[] args) {
        if (args.length == 2 && args[0].equals("get")) {
            String t = args[1];
            return new ArrayList<>(items.stream().filter(s -> s.toLowerCase().contains(t.toLowerCase())).toList());
        }
        if (args.length == 1) {
            String t = args[0];
            return new ArrayList<>(Arrays.stream(params).filter(s -> s.toLowerCase().contains(t.toLowerCase())).toList());
        }
        return Collections.emptyList();
    }

}
