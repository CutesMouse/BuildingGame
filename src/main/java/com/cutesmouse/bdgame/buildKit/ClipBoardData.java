package com.cutesmouse.bdgame.buildKit;

import com.cutesmouse.bdgame.Main;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class ClipBoardData {
    public ArrayList<BlockStatus> DATA;
    public int stage;
    private Vector size;
    private Vector offset;
    public ClipBoardData(Vector size, Vector offset) {
        stage = Main.BDGAME.getStage();
        DATA = new ArrayList<>();
        this.size = size;
        this.offset = offset;
    }
    public void add(BlockStatus block) {
        DATA.add(block);
    }

    public Vector getSize() {
        return size;
    }

    public Vector getOffset() {
        return offset;
    }
}
