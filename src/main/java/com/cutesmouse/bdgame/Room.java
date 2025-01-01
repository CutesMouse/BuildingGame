package com.cutesmouse.bdgame;

import org.bukkit.Location;

import java.util.HashMap;

public class Room {
    private static HashMap<Integer,HashMap<Integer,Room>> ROOMS = new HashMap<>();
    public static Room genRoom(Location loc, int row, int line) {
        if (ROOMS.containsKey(row)) {
            if (ROOMS.get(row).containsKey(line)) {
                return ROOMS.get(row).get(line);
            } else {
                Room r = new Room(loc,row,line);
                ROOMS.get(row).put(line,r);
                return r;
            }
        } else {
            HashMap<Integer,Room> LINE = new HashMap<>();
            Room r = new Room(loc,row,line);
            LINE.put(line,r);
            ROOMS.put(row,LINE);
            return r;
        }
    }

    public RoomData data;
    public Location loc;
    public int row;
    public int line;
    public Room(Location loc, int row, int line) {
        this.loc = loc;
        this.row = row;
        this.line = line;
        this.data = new RoomData();
    }
    public boolean isInside(Location loc) {
        return (loc.getBlockX() >= getMinBuildX() && loc.getBlockX() <= getMaxBuildX()) &&
                (loc.getBlockY() >= getMinBuildY() && loc.getBlockY() <= getMaxBuildY()) &&
                (loc.getBlockZ() >= getMinBuildZ() && loc.getBlockZ() <= getMaxBuildZ());
    }
    public int getMinBuildX() {
        return loc.getBlockX() - 15;
    }
    public int getMaxBuildX() {
        return loc.getBlockX() + 15;
    }
    public int getMinBuildZ() {
        return loc.getBlockZ() + 4;
    }
    public int getMaxBuildZ() {
        return loc.getBlockZ() + 33;
    }
    public int getMinBuildY() {
        return 100;
    }
    public int getMaxBuildY() {
        return 130;
    }

    @Override
    public String toString() {
        return row+"/"+line;
    }
}
