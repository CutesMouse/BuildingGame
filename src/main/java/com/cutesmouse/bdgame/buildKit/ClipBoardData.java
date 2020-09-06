package com.cutesmouse.bdgame.buildKit;

import com.cutesmouse.bdgame.Main;

import java.util.ArrayList;

public class ClipBoardData {
    public ArrayList<BlockStatus> DATA;
    public int stage;
    public ClipBoardData() {
        stage = Main.BDGAME.getStage();
        DATA = new ArrayList<>();
    }
    public void add(BlockStatus block) {
        DATA.add(block);
    }
}
