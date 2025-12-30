package com.cutesmouse.bdgame.utils;

import com.cutesmouse.bdgame.Main;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;


public class SpecialEffects {
    private static final int FIREWORK_NUMS = 20;
    private static final long MAX_DELAY_TICKS = 2 * 20L;
    private static final double SHOOT_RADIUS = 1;
    private static final int MAX_COLOR = 3;

    public static void playFireworks(Location loc) {
        Random rd = new Random();
        for (int i = 0; i < FIREWORK_NUMS; i = i + 1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location loc_offset = randomOffset(loc, SHOOT_RADIUS, rd);
                    Firework fw_entity = loc.getWorld().spawn(loc_offset, Firework.class);
                    FireworkMeta meta = fw_entity.getFireworkMeta();
                    FireworkEffect.Builder builder = FireworkEffect.builder();
                    builder.flicker(rd.nextBoolean()).trail(rd.nextBoolean()).with(randomItem(FireworkEffect.Type.values(), rd));
                    ArrayList<Color> colors = new ArrayList<>();
                    ArrayList<Color> fade_colors = new ArrayList<>();
                    for (int j = 0; j < MAX_COLOR; j++) colors.add(randomColor(rd));
                    for (int j = 0; j < MAX_COLOR; j++) fade_colors.add(randomColor(rd));
                    builder.withColor(colors.toArray(new Color[0]));
                    builder.withFade(fade_colors.toArray(new Color[0]));
                    meta.addEffect(builder.build());
                    fw_entity.setShotAtAngle(rd.nextBoolean());
                    fw_entity.setLife(rd.nextInt(fw_entity.getMaxLife()));
                    fw_entity.setFireworkMeta(meta);
                }
            }.runTaskLater(Main.getProvidingPlugin(Main.class), rd.nextLong(MAX_DELAY_TICKS));
        }
    }

    public static void playLightning(Location center) {
        Random rd = new Random();
        for (int i = 0; i < FIREWORK_NUMS; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    center.getWorld().strikeLightning(randomOffset(center, SHOOT_RADIUS, rd));
                }
            }.runTaskLater(Main.getProvidingPlugin(Main.class), rd.nextLong(MAX_DELAY_TICKS));
        }
    }

    public static void rankingEffect(double avg_point, Player p) {
        int rank = 0;
        String main = "";
        String sub = String.format("§f這棟建築獲得平均 %1.1f 的評分", avg_point);
        if (avg_point > 4.5) {
            rank = 5; // 4.5 - 5
            main = "§4夯";
            if (p != null) playFireworks(p.getLocation());
        } else if (avg_point > 3.5) {
            rank = 4; // 3.5 - 4.5
            main = "§c頂級";
        } else if (avg_point > 2.5) {
            rank = 3; // 2.5 - 3.5
            main = "§e人上人";
        } else if (avg_point > 1.5) {
            rank = 2; // 1.5 - 2.5
            main = "§7NPC";
        } else if (avg_point >= 1) {
            rank = 1; // 1 - 1.5
            main = "§8拉完了";
            if (p != null) playLightning(p.getLocation());
        } else {
            rank = 0; // no ranking
            sub = "§f沒有人為這棟建築評分";
        }
        for (Player k : Bukkit.getOnlinePlayers()) showTitle(k, main, sub);
    }

    private static <T> T randomItem(T[] ary, Random rd) {
        return ary[rd.nextInt(ary.length)];
    }

    private static Color randomColor(Random rd) {
        return Color.fromBGR(rd.nextInt(256), rd.nextInt(256), rd.nextInt(256));
    }

    private static Location randomOffset(Location loc, double radius, Random rd) {
        return loc.clone().add(rd.nextDouble() * 2 * radius - radius,
                rd.nextDouble() * radius, rd.nextDouble() * 2 * radius - radius);
    }

    private static void showTitle(Player player, String main, String sub) {
        player.sendTitle(main, sub, 10, 30, 10);
    }
}
