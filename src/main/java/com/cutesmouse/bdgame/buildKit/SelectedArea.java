package com.cutesmouse.bdgame.buildKit;

import org.bukkit.Location;

import java.util.function.Function;

public class SelectedArea {
    public Location firstPoint;
    public Location secondPoint;
    public ClipBoardData Clipboard;
    public SelectedArea() {

    }
    public void clear() {
        firstPoint = null;
        secondPoint = null;
        Clipboard = null;
    }
    public int size() {
        return (Math.abs(firstPoint.getBlockX() - secondPoint.getBlockX())+1)
                *(Math.abs(firstPoint.getBlockY() - secondPoint.getBlockY())+1)
                *(Math.abs(firstPoint.getBlockZ() - secondPoint.getBlockZ())+1);
    }
    public boolean ready() {
        return (firstPoint != null && secondPoint != null);
    }
    public int getMaxX() {
        return Math.max(firstPoint.getBlockX(),secondPoint.getBlockX());
    }
    public int getMaxY() {
        return Math.max(firstPoint.getBlockY(),secondPoint.getBlockY());
    }
    public int getMaxZ() {
        return Math.max(firstPoint.getBlockZ(),secondPoint.getBlockZ());
    }
    public int getMinX() {
        return Math.min(firstPoint.getBlockX(),secondPoint.getBlockX());
    }
    public int getMinY() {
        return Math.min(firstPoint.getBlockY(),secondPoint.getBlockY());
    }
    public int getMinZ() {
        return Math.min(firstPoint.getBlockZ(),secondPoint.getBlockZ());
    }
    public int forEach(Function<Location,Boolean> function) {
        int times = 0;
        for (int x = getMinX(); x <= getMaxX(); x++) {
            for (int y = getMinY(); y <= getMaxY(); y++) {
                for (int z = getMinZ(); z <= getMaxZ(); z++) {
                    if (function.apply(new Location(firstPoint.getWorld(),x,y,z))) times++;
                }
            }
        }
        return times;
    }
}
