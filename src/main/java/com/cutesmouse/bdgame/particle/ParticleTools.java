package com.cutesmouse.bdgame.particle;

import com.cutesmouse.bdgame.Main;
import com.cutesmouse.bdgame.game.PlayerData;
import com.cutesmouse.bdgame.game.PlayerDataManager;
import com.cutesmouse.bdgame.game.Room;
import com.cutesmouse.bdgame.utils.ItemBank;
import com.cutesmouse.mgui.GUI;
import com.cutesmouse.mgui.GUIItem;
import com.saicone.rtag.RtagEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ParticleTools {

    private static HashMap<String, ParticleSettingProfile> profiles = new HashMap<>();

    private static ParticleSettingProfile getProfile(String name) {
        if (profiles.containsKey(name)) return profiles.get(name);
        profiles.put(name, new ParticleSettingProfile(Particle.HEART, 0.2, 5));
        return profiles.get(name);
    }

    public static void open(Player p) {
        open(p, getProfile(p.getName()));
    }

    public static void open(Player p, ParticleSettingProfile profile) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 1, p);
        for (int i = 0; i < 9; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        gui.addItem(3, new GUIItem(Material.FIREWORK_STAR, new ArrayList<>(Arrays.asList("§b▶ 設定粒子效果類型")),
                "§f更改種類", (e, i) -> {
            e.setCancelled(true);
            openParticleType(p, profile);
        }));
        gui.addItem(4, new GUIItem(Material.IRON_NUGGET, new ArrayList<>(Arrays.asList("§b▶ 設定粒子效果數量", "§e☉ 大小需界在1~32之間")),
                "§f更改數量", (e, i) -> {
            e.setCancelled(true);
            openAmount(p, profile);
        }));
        gui.addItem(5, new GUIItem(Material.WIND_CHARGE, new ArrayList<>(Arrays.asList("§b▶ 設定粒子效果分布半徑")),
                "§f更改範圍", (e, i) -> {
            e.setCancelled(true);
            openRadius(p, profile);
        }));
        if (profile instanceof ModifiableParticleSettingProfile modify) {
            gui.addItem(8, new GUIItem(Material.BARRIER, null,
                    "§c刪除粒子效果", (e, i) -> {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                modify.remove();
            }));
        }
        p.openInventory(gui.getInv());
    }

    private static void openAmount(Player player, ParticleSettingProfile profile) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 1, player);
        for (int i = 0; i < 9; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        ArrayList<Integer> ofs = new ArrayList<>(Arrays.asList(-10, -5, -1, 0, 0, 0, +1, +5, +10));
        ArrayList<Material> mats = new ArrayList<>(Arrays.asList(Material.IRON_BLOCK, Material.IRON_INGOT, Material.IRON_NUGGET, Material.GRAY_STAINED_GLASS_PANE,
                Material.FIREWORK_ROCKET, Material.GRAY_STAINED_GLASS_PANE,
                Material.GOLD_NUGGET, Material.GOLD_INGOT, Material.GOLD_BLOCK));
        ArrayList<String> names = new ArrayList<>(Arrays.asList("-10", "-5", "-1", "§r", "當前數量: " + profile.getNumber(), "§r", "+1", "+5", "+10"));

        for (int i = 0; i < ofs.size(); i++) {
            final int index = i;
            gui.addItem(i, new GUIItem(mats.get(i), null,
                    "§f" + names.get(i), (e, item) -> {
                e.setCancelled(true);
                if (ofs.get(index) != 0) {
                    profile.setNumber(Math.max(1, Math.min(profile.getNumber() + ofs.get(index), 32)));
                    openAmount(player, profile);
                }
            }));
        }
        player.openInventory(gui.getInv());
    }

    private static void openRadius(Player player, ParticleSettingProfile profile) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 1, player);
        for (int i = 0; i < 9; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));

        gui.addItem(3, new GUIItem(Material.COPPER_NUGGET, null,
                "§f窄", (e, item) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            profile.setRadius(0.1);
        }));
        gui.addItem(4, new GUIItem(Material.COPPER_INGOT, null,
                "§f中", (e, item) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            profile.setRadius(0.5);
        }));
        gui.addItem(5, new GUIItem(Material.COPPER_BLOCK, null,
                "§f廣", (e, item) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            profile.setRadius(1);
        }));

        player.openInventory(gui.getInv());
    }

    private static void openParticleType(Player player, ParticleSettingProfile profile) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 3, player);
        for (int i = 0; i < 27; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));

        ArrayList<Material> materials = new ArrayList<>(Arrays.asList(Material.VILLAGER_SPAWN_EGG, Material.WATER_BUCKET, Material.CAMPFIRE, Material.BONE_MEAL,
                Material.DIAMOND_SWORD, Material.CHARCOAL, Material.LINGERING_POTION, Material.TNT, Material.VILLAGER_SPAWN_EGG,
                Material.HEART_OF_THE_SEA, Material.LAVA_BUCKET, Material.FIRE_CHARGE, Material.NOTE_BLOCK, Material.WATER_BUCKET,
                Material.WAXED_COPPER_BLOCK, Material.COPPER_BLOCK, Material.WITCH_SPAWN_EGG, Material.SILVERFISH_SPAWN_EGG,
                Material.SNOWBALL, Material.ENCHANTING_TABLE, Material.TOTEM_OF_UNDYING, Material.INK_SAC
                ));
        ArrayList<String> titles = new ArrayList<>(Arrays.asList("村民生氣", "泡泡", "煙霧", "白色泡沫", "爆擊效果", "黑色愛心",
        "藥水滯留效果", "爆炸", "村民開心", "愛心", "岩漿火花", "火焰", "音符", "水花",
                "上蠟", "除蠟", "魔法", "灰塵", "雪花", "附魔", "不死圖騰", "墨汁"));
        ArrayList<Particle> particles = new ArrayList<>(Arrays.asList(Particle.ANGRY_VILLAGER, Particle.BUBBLE_COLUMN_UP,
                Particle.CAMPFIRE_SIGNAL_SMOKE, Particle.CLOUD, Particle.CRIT, Particle.DAMAGE_INDICATOR, Particle.EFFECT,
                Particle.EXPLOSION, Particle.HAPPY_VILLAGER, Particle.HEART, Particle.LAVA,
                Particle.FLAME, Particle.NOTE, Particle.SPLASH, Particle.WAX_ON, Particle.WAX_OFF, Particle.WITCH, Particle.INFESTED,
                Particle.SNOWFLAKE, Particle.ENCHANT, Particle.TOTEM_OF_UNDYING, Particle.SQUID_INK));

        for (int i = 0; i < titles.size(); i = i + 1) {
            final int k = i;
            gui.addItem(i, new GUIItem(materials.get(i), null,
                    "§f" + titles.get(i), (e, item) -> {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                profile.setType(particles.get(k));
            }));
        }

        player.openInventory(gui.getInv());
    }

    public static void spawnParticle(Player p, Location loc) {
        PlayerData data = PlayerDataManager.getPlayerData(p);
        if (!data.currentRoom().isInside(loc)) return;
        ParticleSettingProfile profile = getProfile(p.getName());
        ArmorStand entity = p.getLocation().getWorld().spawn(loc, ArmorStand.class);
        ParticleEntity particle = ParticleEntity.loadAsParticle(entity, profile);
        ParticleDisplayHandler.registerParticle(particle);
    }
}
