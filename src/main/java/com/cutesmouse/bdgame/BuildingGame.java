package com.cutesmouse.bdgame;

import com.cutesmouse.bdgame.scoreboard.ObjectiveData;
import com.cutesmouse.bdgame.scoreboard.ScoreboardManager;
import com.cutesmouse.bdgame.tools.ItemBank;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class BuildingGame {
    private World world;
    private MapManager manager;
    private int stage;
    private int max_stage;
    private long total_time;
    private long current_time;
    private int players;
    /*
    @stage
        1 -> 出題
        2(偶數) -> 建築
        3(1以外的奇數) -> 猜測
     */
    /*
    @max_stage
        必須為偶數(大於3)
     */
    public BuildingGame() {
        // Init
        this.world = Bukkit.getWorld("world");
        this.stage = 0;
        this.max_stage = 8;
    }

    public int getMaxStage() {
        return max_stage;
    }

    public void setMapManager(MapManager manager) {
        this.manager = manager;
    }

    public int getTotalPlayers() {
        return players;
    }

    public int getStage() {
        return stage;
    }

    public void nextStage() {
        current_time = System.currentTimeMillis();
        PlayerDataManager.getPlayers().forEach(p -> p.getSelectedArea().clear());
        stage++;
        if (stage == getMaxStage()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§a所有回合已經輪番結束! 現在開始欣賞結果!");
                if (p.isOp()) p.getInventory().setItem(8,ItemBank.NEXT_ITEM);
            }
            return;
        }
        if (stage == 1) {
            for (PlayerData p : PlayerDataManager.getPlayers()) {
                Room room = p.nextRoom(stage);
                p.getPlayer().teleport(room.loc);
                p.getPlayer().sendMessage("§6現在是出題時間! 請開啟選單來命題!");
                p.getPlayer().sendTitle("§a出題時間","§e第1階段",20,60,20);
            }
            return;
        }
        if (stage == 2) {
            for (PlayerData p : PlayerDataManager.getPlayers()) {
                Room room = p.nextRoom(stage);
                p.getPlayer().teleport(room.loc);
                p.getPlayer().sendMessage("§6您分配到的題目是 §e"+ room.data.origin);
                p.getPlayer().sendTitle("§a建築時間","§e第2階段",20,60,20);
                room.data.builder = p.getPlayer().getName();
            }
            return;
        }
        if (stage % 2 == 1) {
            for (PlayerData p : PlayerDataManager.getPlayers()) {
                Room room = p.nextRoom(stage);
                p.getPlayer().teleport(room.loc);
                p.getPlayer().sendMessage("§6請依照建築內容進行猜測");
                p.getPlayer().sendTitle("§a猜測時間","§e第"+stage+"階段",20,60,20);
            }
            return;
        }
        for (PlayerData p : PlayerDataManager.getPlayers()) {
            Room room = p.nextRoom(stage);
            Room guess = p.getGuessRoom();
            room.data.origin = guess.data.guess;
            room.data.originProvider = guess.data.guessProvider;
            p.getPlayer().teleport(room.loc);
            p.getPlayer().sendMessage("§6您分配到的題目是 §e"+room.data.origin);
            p.getPlayer().sendTitle("§a建築時間","§e第"+stage+"階段",20,60,20);
            room.data.builder = p.getPlayer().getName();
        }
    }

    public void start() {
        this.total_time = System.currentTimeMillis();
        ArrayList<Player> ps = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(ps);
        int id = 0;
        for (Player p : ps) {
            id++;
            PlayerData data = PlayerDataManager.getPlayerData(p);
            p.getInventory().setItem(8, ItemBank.MENU);
            data.setId(id);
            data.setPlaying(true);
        }
        this.players = id;
        nextStage();
        loadScoreboard();
    }

    public MapManager getMapManager() {
        return manager;
    }

    private String getStageText() {
        if (stage == max_stage) return "參觀中";
        if (stage == 0) return "準備中";
        if (stage == 1) return "出題中";
        if (stage % 2 == 0) return "建築中";
        return "猜測中";
    }
    public int DonePlayers() {
        return (int) PlayerDataManager.getPlayers().stream().filter(p -> p.isPlaying() && p.isDone()).count();
    }
    public int ActivePlayers() {
        return (int) PlayerDataManager.getPlayers().stream().filter(PlayerData::isPlaying).count();
    }
    public void checkIfNextStage() {
        if (ActivePlayers() > DonePlayers()) return;
        PlayerDataManager.getPlayers().forEach(p -> p.setDone(false));
        nextStage();
    }
    private String getTimeText(long t) {
        t = System.currentTimeMillis() - t;
        int hour = (int) (t /1000 / 60 /60);
        int mins = (int) ((t - hour * 1000 * 60 * 60) / 1000 / 60);
        int sec = (int) ((t - (mins * 1000 * 60) - (hour * 1000 * 60 * 60)) / 1000);
        return (hour == 0 ? String.format("%02d:%02d",mins,sec) : String.format("%02d:%02d:%02d",hour,mins,sec));
    }
    public String getTheme(Player p) {
        return stage % 2 == 0 ? (PlayerDataManager.getPlayerData(p).currentRoom().data.origin) : null;
    }
    public void loadScoreboard() {
        ObjectiveData data = new ObjectiveData();
        data.set(11,s -> "§l");
        data.set(10,s -> String.format("§f▶ %s", getStageText()));
        data.set(9,s -> "§a");
        data.set(8,s -> String.format("§f▶ 階段 §b%s", getTimeText(current_time)));
        data.set(7,s -> String.format("§f▶ 完成 §b%s", (stage == max_stage ? "100%" : (DonePlayers()+"/"+PlayerDataManager.getPlayers().size()))));
        data.set(6,s -> "§k");
        data.set(5, s -> String.format("§f▶ 進行 §b%s", getTimeText(total_time)));
        data.set(4,s -> String.format("§r%s", (stage < max_stage ? "§f▶ 回合 §b"+stage+"/"+(max_stage-1) : "")));
        data.set(3, s -> "§f"+(getStage() == getMaxStage() ? "▶ 遊戲已結束" : (getTheme(s) == null ? "▶ 使用選單設定題目/猜測" : "▶ 主題 §e"+getTheme(s))));
        data.set(2,s -> "§9");
        data.set(1,s -> "§7"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        ScoreboardManager.INSTANCE.setObjective_Data(data);
        ScoreboardManager.INSTANCE.setObjective_DisplayName("§e● 2025 跨年建築大賽");
        ScoreboardManager.INSTANCE.setObjective_Name("list");
        ScoreboardManager.INSTANCE.reloadSidebarData();
    }
    public World getWorld() {
        return world;
    }
}
