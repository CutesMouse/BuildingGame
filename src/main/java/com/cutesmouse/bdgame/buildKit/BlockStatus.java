package com.cutesmouse.bdgame.buildKit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockStatus {
    public final Material material;
    public final BlockData data;
    public int dx;
    public int dy;
    public int dz;
    public BlockStatus(Location ori, Block b) {
        material = b.getType();
        data = b.getBlockData();
        dx = b.getLocation().getBlockX() - ori.getBlockX();
        dy = b.getLocation().getBlockY() - ori.getBlockY();
        dz = b.getLocation().getBlockZ() - ori.getBlockZ();
    }
    public Location putPlace(Location ori) {
        return new Location(ori.getWorld(),ori.getBlockX() + dx,
                ori.getBlockY() + dy, ori.getBlockZ() + dz);
    }
}
