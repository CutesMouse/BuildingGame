package com.cutesmouse.bdgame.game;

import java.util.ArrayList;

public class RoomData {
    public String builder;
    public String originProvider;
    public String guessProvider;
    public String origin;
    public String guess;
    public RoomData() {

    }

    public String toHoverText(Double rank) {
        ArrayList<String> result = new ArrayList<>();
        if (builder != null) result.add("§f建造者: §6" + builder);
        if (origin != null) result.add("§f題目: §6" + origin);
        if (originProvider != null) result.add("§f出題者: §6" + originProvider);
        if (guess != null) result.add("§f猜測: §6" + guess);
        if (guessProvider != null) result.add("§f猜測者: §6" + guessProvider);
        if (rank != null) result.add("§f銳評: §6" + Math.round(rank * 10) / 10.0);
        return String.join("\n", result);
    }
}
