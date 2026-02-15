package com.cutesmouse.bdgame.game;

public class ShiftGameScheduler extends GameScheduler {
    public ShiftGameScheduler(int players, int max_stages) {
        super(players, max_stages);
    }

    @Override
    public int get_row_by_player(int player_id, int stage) {
        return ((player_id + stage - 1) % players) + 1;
    }
}
