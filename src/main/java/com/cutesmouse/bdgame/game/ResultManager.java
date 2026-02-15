package com.cutesmouse.bdgame.game;

import com.cutesmouse.bdgame.Main;
import com.cutesmouse.bdgame.saves.BuildFileSystem;
import com.cutesmouse.bdgame.saves.BuildSave;
import com.cutesmouse.bdgame.utils.SpecialEffects;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResultManager {
    public static Queue<Runnable> GenTaskQueue() {
        Queue<Runnable> tasks = new LinkedBlockingQueue<>();
        System.out.println("============================");
        for (int row = 1; row <= Main.BDGAME.ActivePlayers(); row++) {
            for (int stage = 1; stage <= (Main.BDGAME.getMaxStage() - 2) / 2; stage++) {
                Room room = Main.BDGAME.getMapManager().getRoom(row, stage);
                if (BuildFileSystem.isAvaliable()) BuildSave.save(room);
                tasks.add(concatRunnables(() -> sendEveryone("§f這座建築的主題為 §6§l" + room.data.origin + " §f由 §6§l" + room.data.originProvider + " §f設定!", room.getSpawnLocation()),
                        () -> Main.BDGAME.startRanking(room.data.builder)));
                tasks.add(() -> sendEveryone("§6§l" + room.data.builder + " §f根據主題建造了這座建築!"));
                tasks.add(() -> sendEveryone("§6§l" + room.data.guessProvider + " §f猜測此為 §6§l" + room.data.guess));
                tasks.add(concatRunnables(
                        () -> System.out.println(String.format("%s 建造 %s 時，在銳評中得到了 %1.1f 分", room.data.builder, room.data.origin, Main.BDGAME.getVoteResult())),
                        () -> sendEveryone(String.format("§f這棟建築在銳評中得到了 §6§l%1.1f §f分", Main.BDGAME.getVoteResult())),
                        () -> BuildSave.saveRoomRankInfo(room, Main.BDGAME.getVoteResult()),
                        () -> SpecialEffects.rankingEffect(Main.BDGAME.getVoteResult(), Bukkit.getPlayer(room.data.builder)),
                        () -> Main.BDGAME.endRanking()));
                if (stage == 1)
                    System.out.print(room.data.originProvider + "出題\"" + room.data.origin + "\" → " + room.data.builder + "蓋" + " → " + room.data.guessProvider + "猜測\"" + room.data.guess + "\"");
                else
                    System.out.print(" → " + room.data.builder + "蓋 → " + room.data.guessProvider + "猜測\"" + room.data.guess + "\"");
            }
            System.out.println();
        }
        System.out.println("============================");
        tasks.add(() -> sendEveryone("§f所有建築已參觀完成! 感謝各位遊玩!"));
        return tasks;
    }

    private static Runnable concatRunnables(Runnable... runnables) {
        return () -> {
            for (Runnable r : runnables) {
                r.run();
            }
        };
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
