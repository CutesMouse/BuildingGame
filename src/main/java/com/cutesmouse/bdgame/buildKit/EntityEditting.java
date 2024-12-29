package com.cutesmouse.bdgame.buildKit;

import com.cutesmouse.bdgame.Main;
import com.cutesmouse.mgui.ChatQueueData;
import com.cutesmouse.mgui.GUI;
import com.cutesmouse.mgui.GUIItem;
import com.cutesmouse.mgui.ListenerHandler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EntityEditting {
    private static class Task {
        // 0, 1 正、副手
        // 2, 3, 4, 5 帽子、胸甲、褲子、鞋子
        private int position;
        private LivingEntity entity;
        private GUI gui;
        private int gui_slot;

        private Task(int position, LivingEntity entity, GUI gui, int gui_slot) {
            this.position = position;
            this.entity = entity;
            this.gui = gui;
            this.gui_slot = gui_slot;
        }
    }

    public static HashMap<String, Task> tasks = new HashMap<>();

    public static void reset(HumanEntity player) {
        tasks.remove(player.getName());
    }

    private static void equipEntity(LivingEntity entity, ItemStack item, int position) {
        if (entity.getEquipment() == null) return;
        switch (position) {
            case 0:
                entity.getEquipment().setItemInMainHand(item);
                break;
            case 1:
                entity.getEquipment().setItemInOffHand(item);
                break;
            case 2:
                entity.getEquipment().setHelmet(item);
                break;
            case 3:
                entity.getEquipment().setChestplate(item);
                break;
            case 4:
                entity.getEquipment().setLeggings(item);
                break;
            case 5:
                entity.getEquipment().setBoots(item);
                break;
            default:
                break;
        }
    }

    private static boolean putOnEquipment(Player player, int slot) {
        if (!tasks.containsKey(player.getName())) return false;
        Task task = tasks.get(player.getName());
        tasks.remove(player.getName());
        ItemStack item = task.gui.getInv().getItem(task.gui_slot);
        item.setType(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
        task.gui.getInv().setItem(task.gui_slot, item);
        LivingEntity entity = task.entity;
        if (entity == null || entity.getEquipment() == null) {
            player.sendMessage("§c此生物無法設定裝備!");
            return true;
        }
        equipEntity(entity, player.getInventory().getItem(slot), task.position);
        return true;
    }

    public static void open(Player player, LivingEntity entity) {
        GUI gui = new GUI("§e● 2025 跨年建築大賽", 5, player);
        for (int i = 0; i < 45; i++) {
            final int slot = i;
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", (e, item) -> {
                e.setCancelled(true);
                putOnEquipment(player, slot);
            }));
        }
        gui.addItem(10, new GUIItem(Material.COMPASS, new ArrayList<>(Arrays.asList("§b▶ 讓生物的正臉面向自己")),
                "§f面向自己", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 10)) return;
            e.getWhoClicked().closeInventory();
            entity.teleport(entity.getLocation().setDirection(player.getLocation().subtract(entity.getLocation()).toVector()));
        }));
        gui.addItem(12, new GUIItem(Material.BLAZE_POWDER, new ArrayList<>(Arrays.asList("§b▶ 切換生物著火/非著火狀態", "§b▶ 著火狀態下，生物會顯示火焰特效")),
                "§f著火", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 12)) return;
            e.getWhoClicked().closeInventory();
            entity.setVisualFire(!entity.isVisualFire());
        }));
        gui.addItem(13, new GUIItem(Material.ICE, new ArrayList<>(Arrays.asList("§b▶ 切換生物冰凍/非冰凍狀態", "§b▶ 冰凍狀態下，生物會不斷左右晃動")),
                "§f冰凍", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 13)) return;
            e.getWhoClicked().closeInventory();
            if (entity.isFrozen()) entity.setFreezeTicks(0);
            else entity.setFreezeTicks(Integer.MAX_VALUE);
        }));
        gui.addItem(14, new GUIItem(Material.BELL, new ArrayList<>(Arrays.asList("§b▶ 切換生物是否能發出聲音", "§e☉ 預設為無聲音")),
                "§f聲音", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 14)) return;
            e.getWhoClicked().closeInventory();
            entity.setSilent(!entity.isSilent());
        }));
        gui.addItem(15, new GUIItem(Material.DIAMOND_BOOTS, new ArrayList<>(Arrays.asList("§b▶ 切換生物是否會往下墜落", "§e☉ 預設為無重力")),
                "§f重力", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 15)) return;
            e.getWhoClicked().closeInventory();
            entity.setGravity(!entity.hasGravity());
        }));
        gui.addItem(16, new GUIItem(Material.BOOK, new ArrayList<>(Arrays.asList("§b▶ 切換生物是否有AI", "§b▶ 如果開啟，生物能夠自由移動、攻擊其他生物等", "§e☉ 預設為無AI")),
                "§fAI", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 16)) return;
            e.getWhoClicked().closeInventory();
            entity.setAI(!entity.hasAI());
        }));
        addArmorItem(player, entity, gui, 28, 0, "正手");
        addArmorItem(player, entity, gui, 29, 1, "副手");
        addArmorItem(player, entity, gui, 31, 2, "頭盔");
        addArmorItem(player, entity, gui, 32, 3, "胸甲");
        addArmorItem(player, entity, gui, 33, 4, "護腿");
        addArmorItem(player, entity, gui, 34, 5, "鞋子");
        gui.addItem(44, new GUIItem(Material.BARRIER, null,
                "§f移除實體", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 44)) return;
            e.getWhoClicked().closeInventory();
            entity.remove();
        }));
        player.openInventory(gui.getInv());
    }

    private static void addArmorItem(Player player, LivingEntity entity, GUI gui, int gui_slot, int position, String slot_name) {
        GUIItem armor = new GUIItem(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, new ArrayList<>(Arrays.asList("§b▶ 更換生物" + slot_name + "持有的物品", "§e☉ 先點一下這個道具，再點要裝備的物品", "§e☉ 按右鍵可以取消裝備")),
                "§f" + slot_name + "裝備", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, gui_slot)) return;
            if (e.getClick().isRightClick()) {
                equipEntity(entity, null, position);
                return;
            }
            ItemStack item = gui.getInv().getItem(gui_slot);
            item.setType(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
            gui.getInv().setItem(gui_slot, item);
            tasks.put(player.getName(), new Task(position, entity, gui, gui_slot));
        }).hideEverything();
        gui.addItem(gui_slot, armor);
    }

    public static void openItemFrame(Player player, ItemFrame frame) {
        GUI gui = new GUI("§e● 2025 跨年建築大賽", 3, player);
        for (int i = 0; i < 27; i++) {
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        }
        gui.addItem(13, new GUIItem(Material.GLASS, new ArrayList<>(Arrays.asList("§b▶ 切換物品展示框顯示狀態")),
                "§f隱藏物品展示框", (e, i) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            frame.setVisible(!frame.isVisible());
        }));
        player.openInventory(gui.getInv());
    }
}
