package com.cutesmouse.bdgame.commands;

import com.cutesmouse.bdgame.Main;
import com.cutesmouse.bdgame.game.PlayerData;
import com.cutesmouse.bdgame.game.PlayerDataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

public class BuildingGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§c無使用權限!");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§6用法: §f/bd <start | skip | notready | set>");
            return true;
        }
        switch (args[0]) {
            case "start":
                Main.BDGAME.start();
                return true;
            case "skip":
                if (Main.BDGAME.isPreparingStage()) {
                    sender.sendMessage("§c尚未開始遊戲!");
                    return true;
                }
                if (Main.BDGAME.isViewingStage()) {
                    sender.sendMessage("§c遊戲已結束!");
                    return true;
                }
                PlayerDataManager.getPlayers().forEach(p -> p.setDone(false));
                Main.BDGAME.nextStage();
                sender.sendMessage("§a已強制結束此回合");
                return true;
            case "notready":
                if (Main.BDGAME.isPreparingStage()) {
                    sender.sendMessage("§c尚未開始遊戲!");
                    return true;
                }
                if (Main.BDGAME.isViewingStage()) {
                    sender.sendMessage("§c遊戲已結束!");
                    return true;
                }
                if (PlayerDataManager.getPlayers().stream().allMatch(PlayerData::isDone)) {
                    sender.sendMessage("§a所有人都完成準備了!");
                    return true;
                }
                sender.sendMessage("§6尚未完成: §f"+PlayerDataManager.getPlayers().stream().filter(p -> p.isPlaying() && (!p.isDone())).map(p -> ", "+p.getPlayer().getName()).collect(Collectors.joining()).substring(2));
            case "set":
                if (!Main.BDGAME.isGuessingStage()) {
                    sender.sendMessage("§c目前不是設定主題的階段!");
                    return true;
                }
                if (args.length == 1) {
                    sender.sendMessage("§6用法: §f/bd set <輸入>");
                    return true;
                }
                String input = String.join(" ", args).substring(4);
                sender.sendMessage("§a已將您的輸入設定為 " + input);
                if (Main.BDGAME.getStage() == 1) PlayerDataManager.getPlayerData(sender.getName()).submitTitle(input);
                else PlayerDataManager.getPlayerData(sender.getName()).submitGuess(input);
        }
        return true;
    }
}
