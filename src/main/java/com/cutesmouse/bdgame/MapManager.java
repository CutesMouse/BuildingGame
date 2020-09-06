package com.cutesmouse.bdgame;import org.bukkit.Location;import org.bukkit.World;public class MapManager {    private static MapManager INSTANCE;    public static MapManager getInstance() {        if (INSTANCE == null) INSTANCE = new MapManager();        return INSTANCE;    }    // 全域靜態常數 >> 基礎資訊    public static final int FIRST_X = 1013;    public static final int FIRST_Z = 1003;    public static final int FIRST_Y_BLOCK = 100;    public static final int FIRST_Y = 101;    // 橫的叫row 直的叫line    // max(row) = 20    // max(line) = 7    public static final int PER_ROW = 30;    public static final int PER_LINE = 40;    // 全域動態變數 >> 基礎資訊    private World w;    private MapManager() {        this.w = Main.BDGAME.getWorld();    }    public Location getRoomBlock(int row, int line) {        return new Location(w,FIRST_X+ PER_ROW * (row - 1), FIRST_Y_BLOCK, FIRST_Z + PER_LINE* (line -1));    }    public Room getRoom(int row, int line) {        return Room.genRoom(new Location(w,FIRST_X+ PER_ROW * (row - 1), FIRST_Y, FIRST_Z + PER_LINE* (line -1)),row,line);    }    // x -> -9 ~ +9 >> z -> +4 ~ +29    public boolean canBuild(Location ownedRoom, Location buildLocation) {        int by = buildLocation.getBlockY();        if (by > 130 || by < 100) return false;        int bx = buildLocation.getBlockX();        int bz = buildLocation.getBlockZ();        int rx = ownedRoom.getBlockX();        int maxZ = ownedRoom.getBlockZ() + 29;        int minZ = ownedRoom.getBlockZ() + 4;        if (Math.abs(bx - rx) > 9) return false;        if (bz < minZ || bz > maxZ) return false;        return true;    }}