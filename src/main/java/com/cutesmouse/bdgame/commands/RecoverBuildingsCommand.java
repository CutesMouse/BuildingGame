package com.cutesmouse.bdgame.commands;

import com.cutesmouse.bdgame.saves.BuildFileSystem;
import com.cutesmouse.bdgame.saves.BuildRecovery;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.util.List;

public class RecoverBuildingsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() || !(sender instanceof Player)) {
            sender.sendMessage("§c無使用權限!");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("§6用法: §f/pb <建築時間> <建築名稱>");
            return true;
        }
        try {
            BuildRecovery.recovery(((Player) sender).getLocation(), args[0], args[1]);
        } catch (FileNotFoundException e) {
            sender.sendMessage("§c找不到指定的檔案!");
            return true;
        } catch (ClassNotFoundException e) {
            sender.sendMessage("§c未安裝WorldEdit!");
            return true;
        }
        sender.sendMessage("§a已成功貼上 buildings/" + args[0] + "/" + args[1]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) return null;
        if (args.length == 1) {
            return BuildFileSystem.listFileUnderFolder("", args[0]);
        }
        if (args.length == 2) {
            return BuildFileSystem.listFileUnderFolder(args[0] + "/", args[1]);
        }
        return null;
    }
}
