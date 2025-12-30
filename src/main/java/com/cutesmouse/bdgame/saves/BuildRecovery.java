package com.cutesmouse.bdgame.saves;

import com.cutesmouse.bdgame.MapManager;
import com.cutesmouse.bdgame.Room;
import com.saicone.rtag.RtagEntity;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class BuildRecovery {
    public static void recovery(Location loc, String prefix, String suffix) throws FileNotFoundException, ClassNotFoundException {
        // check worldedit installed
        if (!BuildFileSystem.isAvaliable()) throw new ClassNotFoundException();
        // find a target room
        Room room = MapManager.getInstance().getNearestVirtualRoom(loc);
        // get schem file
        File schem = BuildFileSystem.locateFile(BuildFileSystem.FileType.SCHEMATIC, prefix, suffix, false);
        if (schem == null) throw new FileNotFoundException();
        // paste schem file
        pasteSchematic(room.getMinLocaction(), schem);
    }

    private static void pasteSchematic(Location location, File schematicFile) {
        Clipboard clipboard = null;
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);

        if (format != null) {
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Unknown schematic format for file: " + schematicFile.getPath());
            return;
        }

        World bukkitWorld = location.getWorld();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);

        // Target position for pasting (WorldEdit uses BlockVector3 for location)
        BlockVector3 pastePosition = BlockVector3.at(location.getX(), location.getY(), location.getZ());

        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(pastePosition)
                    .ignoreAirBlocks(false) // Set to true to ignore air blocks in the schematic
                    .build();
            Operations.complete(operation);
            // editSession is flushed automatically by the try-with-resources statement
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
}
