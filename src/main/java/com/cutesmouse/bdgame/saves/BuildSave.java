package com.cutesmouse.bdgame.saves;

import com.cutesmouse.bdgame.game.Room;
import com.cutesmouse.bdgame.game.RoomData;
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
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;


public class BuildSave {

    public static void save(Room r) {
        if (!BuildFileSystem.isAvaliable()) return;
        // block information
        saveRoomBlocks(r);
        // guess information
        saveRoomInfo(r);
    }

    private static void saveRoomBlocks(Room r) {
        CuboidRegion region = new CuboidRegion(new BukkitWorld(r.loc.getWorld()), new BlockVector3(r.getMinBuildX(), r.getMinBuildY(),
                r.getMinBuildZ()), new BlockVector3(r.getMaxBuildX(), r.getMaxBuildY(),
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
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V2_SCHEMATIC
                .getWriter(Files.newOutputStream(BuildFileSystem.getFile(r, BuildFileSystem.FileType.SCHEMATIC).toPath()))) {
            writer.write(clipboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveRoomInfo(Room r) {
        File file = BuildFileSystem.getFile(r, BuildFileSystem.FileType.INFO);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        RoomData data = r.data;
        try {
            for (Field field : RoomData.class.getFields()) {
                config.set(field.getName(), field.get(data));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveRoomRankInfo(Room r, double rank) {
        File file = BuildFileSystem.getFile(r, BuildFileSystem.FileType.INFO);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("rank", rank);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
