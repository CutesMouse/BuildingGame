package com.cutesmouse.bdgame.listener;

import com.cutesmouse.bdgame.*;
import com.cutesmouse.bdgame.buildKit.*;
import com.cutesmouse.bdgame.tools.ItemBank;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class GameplayListener implements Listener {
    private static Main plugin;

    public GameplayListener(Main plugin) {
        GameplayListener.plugin = plugin;
    }

    // 用於補充玩家的遊戲選單
    @EventHandler
    public void onPlayerMovedInventory(InventoryClickEvent e) {
        ItemStack item = ItemBank.MENU;
        if (Main.BDGAME.getStage() == Main.BDGAME.getMaxStage()) {
            if (!e.getWhoClicked().isOp()) return;
            item = ItemBank.NEXT_ITEM;
        }
        if (!e.getWhoClicked().getInventory().containsAtLeast(item, 1)) {
            e.getWhoClicked().getInventory().setItem(8, item);
        }
    }

    private static Queue<Runnable> TASKS;
    private long last;
    private static final ArrayList<Material> BANNED_ITEM;

    static {
        BANNED_ITEM = new ArrayList<>();
        BANNED_ITEM.add(Material.EGG);
        BANNED_ITEM.add(Material.SNOWBALL);
        BANNED_ITEM.add(Material.BOW);
        BANNED_ITEM.add(Material.TRIDENT);
        BANNED_ITEM.add(Material.ENDER_PEARL);
        BANNED_ITEM.add(Material.CROSSBOW);
        BANNED_ITEM.add(Material.ENDER_EYE);
        BANNED_ITEM.add(Material.CHORUS_FRUIT);
    }

    @EventHandler
    public void onPlayerRiding(EntityMountEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (BANNED_ITEM.contains(e.getMaterial())) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                e.setCancelled(true);
            return;
        }

        // 建築工具
        if (Main.BDGAME.getStage() > 0 && Main.BDGAME.getStage() % 2 == 0 &&
                Main.BDGAME.getStage() < Main.BDGAME.getMaxStage()) {
            e.setCancelled(true);
            if (e.getItem().isSimilar(ItemBank.SELECT)) BuildTools.invokeSelectionTool(e);
            else if (e.getItem().isSimilar(ItemBank.SET)) BuildTools.invokeSetTool(e);
            else if (e.getItem().isSimilar(ItemBank.COPY)) BuildTools.invokeCopyTool(e);
            else if (e.getItem().isSimilar(ItemBank.PASTE)) BuildTools.invokePasteTool(e);
            else if (e.getItem().isSimilar(ItemBank.SETPLOT)) BuildTools.invokePlotTool(e);
            else if (e.getItem().isSimilar(ItemBank.RESET)) BuildTools.invokeResetTool(e);
            else if (e.getItem().isSimilar(ItemBank.UNDO)) BuildTools.invokeUndoTool(e);
            else e.setCancelled(false);
        }

        // 遊戲選單
        if (e.getItem().isSimilar(ItemBank.MENU)) {
            GameMenuHandler.open(e.getPlayer());
            e.setCancelled(true);
            return;
        }

        // 下一頁
        if (e.getItem().isSimilar(ItemBank.NEXT_ITEM)) {
            if (Main.BDGAME.getStage() != Main.BDGAME.getMaxStage()) {
                e.getPlayer().sendMessage("§c遊戲尚未結束! 還無法使用!");
                return;
            }
            if (TASKS == null) {
                TASKS = ResultManager.GenTaskQueue();
            }
            if (System.currentTimeMillis() - last < 1000) return;
            if (TASKS.size() > 0) {
                TASKS.poll().run();
                e.setCancelled(true);
                last = System.currentTimeMillis();
            }
        }
    }

    @EventHandler
    public void onClickEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof Player) return;
        if (Main.BDGAME.getStage() == 0) return;
        e.setCancelled(true);
        if (Main.BDGAME.getStage() % 2 != 0) return;
        if (e.getRightClicked() instanceof ItemFrame) {
            if (e.getPlayer().isSneaking())
                EntityEditting.openItemFrame(e.getPlayer(), ((ItemFrame) e.getRightClicked()));
            else e.setCancelled(true);
            return;
        }
        if (!(e.getRightClicked() instanceof LivingEntity)) return;

        Material hand_item = e.getPlayer().getInventory().getItemInMainHand().getType();
        if (hand_item.equals(Material.LEAD) || hand_item.equals(Material.NAME_TAG))
            e.setCancelled(false);
        else EntityEditting.open(e.getPlayer(), ((LivingEntity) e.getRightClicked()));
    }

    @EventHandler // 生物移動功能
    public void onPlayerMove(PlayerMoveEvent e) {
        EntityEditting.playerMove(e);
    }

    @EventHandler // 生物移動功能
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        if (e.isSneaking()) EntityEditting.resetMovingTask(e.getPlayer());
    }

    @EventHandler // 裝備編輯功能
    public void onCloseInventory(InventoryCloseEvent e) {
        EntityEditting.resetArmorTask(e.getPlayer());
    }

    @EventHandler // 生物移動功能、填充複製貼上功能外框顯示
    public void onSwapItem(PlayerItemHeldEvent e) {
        EntityEditting.mouseScroll(e);
        if (e.isCancelled()) return;
        if (Main.BDGAME.getStage() == 0) return;
        if (Main.BDGAME.getStage() % 2 != 0) return;
        BuildTools.showHoverOutline(e.getPlayer(), plugin, e.getNewSlot());
    }
}
