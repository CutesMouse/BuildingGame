package com.cutesmouse.bdgame;

import com.cutesmouse.bdgame.command.Debug;
import com.cutesmouse.bdgame.command.BDGameCMD;
import com.cutesmouse.bdgame.listener.GameplayListener;
import com.cutesmouse.bdgame.scoreboard.ObjectiveData;
import com.cutesmouse.bdgame.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends JavaPlugin {
    public static BuildingGame BDGAME;
    @Override
    public void onEnable() {
        BDGAME = new BuildingGame();
        BDGAME.setMapManager(MapManager.getInstance());
        getCommand("buildinggame").setExecutor(new BDGameCMD());
        getCommand("query").setExecutor(new Debug());
        getServer().getPluginManager().registerEvents(new GameplayListener(this),this);
        loadScoreboard();
    }
    private void loadScoreboard() {
        getServer().getPluginManager().registerEvents(ScoreboardManager.INSTANCE,this);
        ObjectiveData data = new ObjectiveData();
        data.set(10,s -> "§6歡迎 §e" + s.getName());
        data.set(9,s -> "§6參加本屆");
        data.set(8,s -> "§e§l「建築大賽」");
        data.set(7, s -> "§6伺服器人數: §e"+ Bukkit.getOnlinePlayers().size());
        data.set(6, s -> "§a");
        data.set(5, s -> "§6階段: §e準備中...");
        data.set(4, s -> "§c");
        data.set(3,s -> "§7"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        ScoreboardManager.INSTANCE.setObjective_Data(data);
        ScoreboardManager.INSTANCE.setObjective_DisplayName("§d◤§b§l建築大賽 Building Game§d◢");
        ScoreboardManager.INSTANCE.setObjective_Name("list");
        new BukkitRunnable() {
            @Override
            public void run() {
                ScoreboardManager.INSTANCE.updateSidebarData();
            }
        }.runTaskTimer(this,0L,10L);
    }
}
