package com.cutesmouse.bdgame.game;

import com.cutesmouse.bdgame.Main;
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

    public Room currentRoom() {
        int stage = Main.BDGAME.getStage();
        return getRoomForStage(stage);
    }

    // stage must be a "building" stage and >4
    public Room getThemeRoom(int stage) {
        GameScheduler scheduler = Main.BDGAME.getScheduler();
        int col = Math.max(1, (stage - 1) / 2);
        int row = scheduler.get_row_by_player(id, stage); // 猜測房間和現在同個row，只是col-1
        return Main.BDGAME.getMapManager().getRoom(row, col);
    }
    
    public Room getRoomForStage(int stage) {
        int col = Math.max(1, stage / 2);
        int row = Main.BDGAME.getScheduler().get_row_by_player(id, stage);
        return Main.BDGAME.getMapManager().getRoom(row, col);
    }

    public void submitTitle(String output) {
        Room room = currentRoom();
        room.data.origin = output;
        room.data.originProvider = player;
        done();
    }

    public void submitGuess(String output) { // 必須確保當前能夠猜測，才能執行
        Room room = currentRoom();
        room.data.guess = output;
        room.data.guessProvider = player;
        done();
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

    public void setDone(boolean done) {
        this.done = done;
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
