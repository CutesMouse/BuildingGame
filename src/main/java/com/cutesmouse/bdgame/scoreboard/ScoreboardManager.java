package com.cutesmouse.bdgame.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager implements Listener {
    public static ScoreboardManager INSTANCE;
    static {
        INSTANCE = new ScoreboardManager();
    }
    private ObjectiveData Objective_Data;
    private String Objective_Name;
    private String Objective_DisplayName;
    public ScoreboardManager() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
            p.setScoreboard(sb);
        }
    }
    public void setObjective_DisplayName(String name) {
        this.Objective_DisplayName = name;
    }
    public void setObjective_Data(ObjectiveData data) {
        this.Objective_Data = data;
    }
    public void setObjective_Name(String name) {
        this.Objective_Name = name;
    }
    public void updateSidebarData() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = p.getScoreboard();
            if (sb.equals(Bukkit.getScoreboardManager().getMainScoreboard())) return;
            Objective obj;
            if (sb.getObjective(Objective_Name) == null) {
                 obj = sb.registerNewObjective(Objective_Name, "dummy",Objective_DisplayName);
                 obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else obj = sb.getObjective(Objective_Name);
            Objective_Data.apply(obj,p);
        }
    }
    public void reloadSidebarData() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
            p.setScoreboard(sb);
        }
        updateSidebarData();
    }
    @EventHandler
    public void join(PlayerJoinEvent e) {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        e.getPlayer().setScoreboard(sb);
    }
}
