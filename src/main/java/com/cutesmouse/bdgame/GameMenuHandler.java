package com.cutesmouse.bdgame;

import com.cutesmouse.bdgame.utils.ItemBank;
import com.cutesmouse.mgui.GUI;
import com.cutesmouse.mgui.GUIItem;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameMenuHandler {
    public static void open(Player p) {
        PlayerData data = PlayerDataManager.getPlayerData(p);
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 5, p);
        for (int i = 0; i < 45; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        int stage = Main.BDGAME.getStage();
        if (stage == 0) { //遊戲開始前
            gui.addItem(22, new GUIItem(Material.BARRIER, null, "§c遊戲準備中", GUI.blank()));
        } else if (stage == 1) { //出題環節
            if (data.currentRoom().data.origin == null) {
                gui.addItem(22, new GUIItem(Material.OAK_SIGN, new ArrayList<>(Arrays.asList("§b☉ 點擊設定題目")), "§f設定題目",
                        GUI.signText("", (h) -> checkInsert(p, combine(h.getLines()), data))));
            } else
                gui.addItem(22, new GUIItem(Material.EMERALD_BLOCK, Collections.singletonList("§b▶ " + data.currentRoom().data.origin),
                        "§f出題完成", GUI.blank()));
        } else if (stage % 2 == 0) { //建築環節
            if (data.isDone()) {
                gui.addItem(44, new GUIItem(Material.EMERALD_BLOCK, Arrays.asList("§b▶ 目前已標示為完成", "§e☉ 點擊可重新標示為未完成"), "§c重新標示為未完成", (e, i) -> {
                    e.setCancelled(true);
                    data.undone();
                    e.getWhoClicked().closeInventory();
                }));
            } else {
                gui.addItem(44, new GUIItem(Material.IRON_BLOCK, new ArrayList<>(Arrays.asList("§b▶ 標示後仍可繼續建築", "§b▶ 也可以重新標示為未完成", "§c☉ 所有人皆完成後會立即傳送")),
                        "§f標示完成", (e, i) -> {
                    e.setCancelled(true);
                    data.done();
                    e.getWhoClicked().closeInventory();
                }));
            }
            gui.addItem(19, new GUIItem(ItemBank.SELECT, (e, i) -> {
            }));
            gui.addItem(20, new GUIItem(ItemBank.SET, (e, i) -> {
            }));
            gui.addItem(21, new GUIItem(ItemBank.SETPLOT, (e, i) -> {
            }));
            gui.addItem(22, new GUIItem(ItemBank.UNDO, (e, i) -> {
            }));
            gui.addItem(23, new GUIItem(ItemBank.RESET, (e, i) -> {
            }));
            gui.addItem(24, new GUIItem(ItemBank.COPY, (e, i) -> {
            }));
            gui.addItem(25, new GUIItem(ItemBank.PASTE, (e, i) -> {
            }));
            gui.addItem(43, new GUIItem(Material.BARRIER, new ArrayList<>(Arrays.asList("§b▶ 丟出的物品會變成不可撿取的狀態", "§e☉ 點擊可清除所有掉落物")),
                    "§c清除所有掉落物品", (e, i) -> {
                e.setCancelled(true);
                Room room = PlayerDataManager.getPlayerData(e.getWhoClicked().getName()).currentRoom();
                e.getWhoClicked().getWorld().getEntities().stream()
                        .filter(item -> item.getType().equals(EntityType.ITEM) && room.isInside(item.getLocation()))
                        .forEach(Entity::remove);
                e.getWhoClicked().closeInventory();
            }));
        } else { //猜測環節
            if (data.currentRoom().data.guess == null) {
                gui.addItem(22, new GUIItem(Material.OAK_SIGN, new ArrayList<>(Arrays.asList("§b☉ 點擊進行猜測")), "§f進行猜測",
                        GUI.signText("", h -> checkGuess(p, combine(h.getLines()), data))));
            } else
                gui.addItem(22, new GUIItem(Material.EMERALD_BLOCK, Collections.singletonList("§b▶ " + data.currentRoom().data.guess),
                        "§f猜測完成", GUI.blank()));
        }
        p.openInventory(gui.getInv());
    }

    //確認出題
    private static void checkInsert(Player p, String output, PlayerData data) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 5, p);
        for (int i = 0; i < 45; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        gui.addItem(13, new GUIItem(Material.OAK_SIGN, new ArrayList<>(Arrays.asList("§b▶ " + output, "§c☉ 確認後無法進行更改")),
                "§f確認題目", GUI.blank()));
        gui.addItem(29, new GUIItem(Material.EMERALD_BLOCK, null, "§a⇨ 是", (e, i) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            data.currentRoom().data.origin = output;
            data.currentRoom().data.originProvider = p.getName();
            data.done();
            e.getWhoClicked().sendMessage("§a題目設定完成");
        }));
        gui.addItem(33, new GUIItem(Material.REDSTONE_BLOCK, null, "§c⇨ 否",
                GUI.signText("", h -> checkInsert(h.getPlayer(), combine(h.getLines()), data))));
        new BukkitRunnable() {
            @Override
            public void run() {
                p.openInventory(gui.getInv());
            }
        }.runTaskLater(Main.getPlugin(Main.class), 1L);
    }

    private static void checkGuess(Player p, String output, PlayerData data) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 5, p);
        for (int i = 0; i < 45; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        gui.addItem(13, new GUIItem(Material.OAK_SIGN, new ArrayList<>(Arrays.asList("§b▶ " + output, "§c☉ 確認後無法進行更改!")),
                "§f確認題目", GUI.blank()));
        gui.addItem(29, new GUIItem(Material.EMERALD_BLOCK, null, "§a⇨ 是", (e, i) -> {
            System.out.println("輸入Guess -> " + data.currentRoom().toString() + " / " + output + " / " + e.getWhoClicked().getName());
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            data.currentRoom().data.guess = output;
            data.currentRoom().data.guessProvider = p.getName();
            data.done();
            e.getWhoClicked().sendMessage("§a已輸入猜測結果");
        }));
        gui.addItem(33, new GUIItem(Material.REDSTONE_BLOCK, null, "§c⇨ 否", GUI.signText("", h -> checkGuess(h.getPlayer(), combine(h.getLines()), data))));
        new BukkitRunnable() {
            @Override
            public void run() {
                p.openInventory(gui.getInv());
            }
        }.runTaskLater(Main.getPlugin(Main.class), 1L);
    }

    private static String combine(String[] output) {
        assert output.length == 4;
        return output[0] + output[1] + output[2] + output[3];
    }
}
