package com.github.wisemann64.ngabdungeons.cmds;

import com.github.wisemann64.ngabdungeons.utils.Test;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandTest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        if (args.length == 0) Test.command(null,sender);
        else Test.command(args[0],sender);
        return false;
    }
}
