package com.cutesmouse.bdgame.saves;

import com.cutesmouse.bdgame.commands.ClickEventCommand;
import com.cutesmouse.bdgame.game.MapManager;
import com.cutesmouse.bdgame.game.Room;
import com.cutesmouse.bdgame.game.RoomData;
import com.cutesmouse.bdgame.particle.ParticleDisplayHandler;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;

public class BuildRecovery {
    public static void recovery(Location loc, String prefix, String suffix) throws FileNotFoundException, ClassNotFoundException {
        // check worldedit installed
        if (!BuildFileSystem.isAvaliable()) throw new ClassNotFoundException();
        // find a target room
        Room room = MapManager.getInstance().getNearestVirtualRoom(loc);
        // clear entities inside that room
        room.clearEntities();
        // get schem file
        File schem = BuildFileSystem.locateFile(BuildFileSystem.FileType.SCHEMATIC, prefix, suffix, false);
        File info = BuildFileSystem.locateFile(BuildFileSystem.FileType.INFO, prefix, suffix, false);
        if (schem == null) throw new FileNotFoundException();
        // paste schem file
        pasteSchematic(room.getMinLocaction(), schem);
        // reload particle entities
        ParticleDisplayHandler.resetParticleEntities();
        // announce paste
        announcePaste(info, room);
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

    private static void announcePaste(File info, Room room) {
        ComponentBuilder builder = new ComponentBuilder("§f▶ " + "已貼上了一棟新的建築 ");
        if (info != null) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(info);
            RoomData data = new RoomData();
            try {
                for (Field field : RoomData.class.getFields()) {
                    field.set(data, config.getString(field.getName()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            Double rank = null;
            if (config.contains("rank")) rank = config.getDouble("rank");
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(data.toHoverText(rank))));
        }
        ComponentBuilder clickEvent = new ComponentBuilder("§e[點擊前往]")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ClickEventCommand.registerNewTask(sender -> ((Player) sender).teleport(room.getSpawnLocation()), 20000L)));
        builder.append(clickEvent.build());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(builder.build());
        }
    }
}
