package com.cutesmouse.bdgame;

import com.cutesmouse.bdgame.tools.SelectedArea;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerData {
    private String player;
    private boolean playing;
    private boolean done;
    private int id; // 從1開始
    private SelectedArea SELECTED;

    public PlayerData(Player player) {
        this.player = player.getName();
        this.playing = false;
        this.done = false;
        SELECTED = new SelectedArea();
    }
    /*
    @stage
        1 -> 出題
        2(偶數) -> 建築
        3(1以外的奇數) -> 猜測
     */

    public SelectedArea getSelectedArea() {
        return SELECTED;
    }

    public Room nextRoom(int next_stage) {
        if (next_stage == 1) return Main.BDGAME.getMapManager().getRoom(id, 1);
        int row = addRow(id, (next_stage - 1));
        int line = (int) Math.round((next_stage - 1) / 2.0);
        return Main.BDGAME.getMapManager().getRoom(row, line);
    }

    private static int addRow(int i, int b) {
        int c = i + b;
        while (c > Main.BDGAME.ActivePlayers()) c -= Main.BDGAME.ActivePlayers();
        return c;
    }

    private static int minusRow(int i, int b) {
        int c = i - b; // 1 - 1
        while (c < 1) c += Main.BDGAME.ActivePlayers();
        return c;
    }

    public void done() {
        done = true;
        Main.BDGAME.checkIfNextStage();
    }

    public void undone() {
        done = false;
    }

    public boolean isDone() {
        return done;
    }

    public Room getGuessRoom() {
        //至少階段四
        int stage = Main.BDGAME.getStage();
        if (stage == 1) return null;
        int row = addRow(id, stage - 1);
        return Main.BDGAME.getMapManager().getRoom(row, (int) Math.round((stage - 1) / 2.0) - 1);
    }

    public Room getGuessRoom(int stage) {
        //至少階段四
        if (stage == 1) return null;
        int row = addRow(id, stage - 1);
        return Main.BDGAME.getMapManager().getRoom(row, (int) Math.round((stage - 1) / 2.0) - 1);
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Room currentRoom() {
        int stage = Main.BDGAME.getStage();
        if (stage == 1) return Main.BDGAME.getMapManager().getRoom(id, 1);
        return Main.BDGAME.getMapManager().getRoom(addRow(id, stage - 1), (int) Math.round((stage - 1) / 2.0));
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
