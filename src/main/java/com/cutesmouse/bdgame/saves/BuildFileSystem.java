package com.cutesmouse.bdgame.saves;

import com.cutesmouse.bdgame.Main;
import com.cutesmouse.bdgame.game.Room;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BuildFileSystem {
    private static String PREFIX;


    public static boolean isAvaliable() {
        return !(Main.getPlugin(Main.class).getServer().getPluginManager().getPlugin("WorldEdit") == null);
    }

    public static List<String> listFileUnderFolder(String path, String prefix) {
        File folder = new File(Main.getPlugin(Main.class).getDataFolder(), "buildings/" + path);
        if (!folder.exists() || !folder.isDirectory()) return null;
        return Arrays.stream(folder.listFiles()).map(f -> f.getName().split("\\.")[0]).distinct().filter(name -> name.startsWith(prefix)).toList();
    }

    public static File getFile(Room r, FileType type) {
        if (PREFIX == null) PREFIX = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        return locateFile(type, PREFIX, "b" + r.row + "_" + r.line + "_" + r.data.builder, true);
    }

    public static File locateFile(FileType type, String prefix, String suffix, boolean createFile) {
        File folder = new File(Main.getPlugin(Main.class).getDataFolder(), "buildings/" + prefix);
        if (!folder.exists()) {
            if (createFile) folder.mkdirs();
            else return null;
        }
        File file = new File(folder, suffix + "." + type.ident);
        if (!file.exists()) {
            if (createFile) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else return null;
        }
        return file;
    }

    public enum FileType {
        SCHEMATIC("schem"), INFO("bdinfo");
        public String ident;

        FileType(String s) {
            this.ident = s;
        }
    }
}
