package com.cutesmouse.bdgame.saves;

import com.cutesmouse.bdgame.Room;
import com.saicone.rtag.RtagEntity;
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
import org.bukkit.entity.Entity;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


public class BuildSave {

    public static void save(Room r) {
        if (!BuildFileSystem.isAvaliable()) return;
        // block information
        saveRoomBlocks(r);
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
}
