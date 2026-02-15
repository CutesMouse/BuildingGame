package com.cutesmouse.bdgame.game;

public abstract class GameScheduler {

    protected int players;
    protected int max_stages;

    // stage from 1~max_stage
    public GameScheduler(int players, int max_stages) {
        this.players = players;
        this.max_stages = max_stages;
    }

    // stage from 1-max_stage
    // player_id from 1-players
    // return new row of a player
    public abstract int get_row_by_player(int player_id, int stage);

}
