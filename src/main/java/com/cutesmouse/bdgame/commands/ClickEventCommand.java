package com.cutesmouse.bdgame.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class ClickEventCommand implements CommandExecutor {

    private static HashMap<UUID, CommandTask> tasks = new HashMap<>();

    public static String registerNewTask(Consumer<CommandSender> run, long validateTimeInMills) {
        CommandTask task = new CommandTask(System.currentTimeMillis() + validateTimeInMills, run);
        UUID represent = UUID.randomUUID();
        tasks.put(represent, task);
        return "/request " + represent;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 1) return true;
        try {
            UUID id = UUID.fromString(args[0]);
            if (!tasks.containsKey(id)) return true;
            CommandTask task = tasks.get(id);
            if (System.currentTimeMillis() > task.expireTime) {
                tasks.remove(id);
                return true;
            }
            task.task().accept(sender);
            return true;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private record CommandTask(long expireTime, Consumer<CommandSender> task) {

    }
}
