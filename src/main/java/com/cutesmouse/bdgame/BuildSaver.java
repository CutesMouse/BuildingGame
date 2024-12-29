package com.cutesmouse.bdgame;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BuildSaver {
    private static String PREFIX;
    public static boolean isAvaliable() {
        return !(Main.getPlugin(Main.class).getServer().getPluginManager().getPlugin("WorldEdit") == null);
    }
    public static void save(Room r) {
        if (!isAvaliable()) return;
        CuboidRegion region = new CuboidRegion(new BukkitWorld(r.loc.getWorld()), new BlockVector3(r.getMinBuildX(),r.getMinBuildY(),
                r.getMinBuildZ()), new BlockVector3(r.getMaxBuildX(),r.getMaxBuildY(),
                r.getMaxBuildZ()));
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld());

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        forwardExtentCopy.setCopyingEntities(true);
        try {
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            e.printStackTrace();
            return;
        }
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC.getWriter(Files.newOutputStream(getFile(r).toPath()))) {
            writer.write(clipboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static File getFile(Room r) {
        if (PREFIX == null) PREFIX = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        File folder = new File(Main.getPlugin(Main.class).getDataFolder(), "buildings/"+PREFIX);
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, "b" + r.row + "_" + r.line + "_" + r.data.builder + ".schem");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}
