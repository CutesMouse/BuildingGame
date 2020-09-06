package com.cutesmouse.bdgame;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResultManager {
    public static Queue<Runnable> GenTaskQueue() {
        Queue<Runnable> tasks = new LinkedBlockingQueue<>();
        System.out.println("============================");
        for (int row = 1; row <= Main.BDGAME.ActivePlayers(); row++) {
            for (int stage = 1; stage <= (Main.BDGAME.getMaxStage()-2) / 2; stage++) {
                Room room = Main.BDGAME.getMapManager().getRoom(row, stage);
                if (BuildSaver.isAvaliable()) BuildSaver.save(room);
                tasks.add(() -> sendEveryone("§f這座建築的主題為 §6§l" + room.data.origin + " §f由 §6§l" + room.data.originProvider + " §f設定!", room.loc));
                tasks.add(() -> sendEveryone("§6§l" + room.data.builder + " §f根據主題建造了這座建築!"));
                tasks.add(() -> sendEveryone("§6§l" + room.data.guessProvider + " §f猜測此為 §6§l" + room.data.guess));
                if (stage == 1) System.out.print(room.data.originProvider+"出題\""+room.data.origin+"\" → "+room.data.builder+"蓋"+" → "+room.data.guessProvider+"猜測\""+room.data.guess+"\"");
                else System.out.print(" → "+room.data.builder+"蓋 → " + room.data.guessProvider+"猜測\""+room.data.guess+"\"");
            }
            System.out.println();
        }
        System.out.println("============================");
        tasks.add(() -> sendEveryone("§f所有建築已參觀完成! 感謝各位遊玩!"));
        return tasks;
    }
    private static void sendEveryone(String s) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(s));
    }
    private static void sendEveryone(String s, Location loc) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendMessage(s);
            p.teleport(loc);
        });
    }
}
