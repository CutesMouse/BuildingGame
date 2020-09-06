package com.cutesmouse.bdgame;

import java.util.ArrayList;

public class UndoQueue {
    public ArrayList<Runnable> LIST;
    public Long TIME;
    public int STAGE;
    public UndoQueue(ArrayList<Runnable> list) {
        LIST = list;
        TIME = System.currentTimeMillis();
        STAGE = Main.BDGAME.getStage();
    }
}
