package com.cutesmouse.bdgame.listeners;

import com.cutesmouse.bdgame.*;
import com.cutesmouse.bdgame.tools.*;
import com.cutesmouse.bdgame.utils.ItemBank;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
        refill(((Player) e.getWhoClicked()), item, 8);
        // 銳評道具
        if (Main.BDGAME.isRanking()) {
            refill(((Player) e.getWhoClicked()), ItemBank.RANK_LEVEL_0, 0);
            refill(((Player) e.getWhoClicked()), ItemBank.RANK_LEVEL_1, 1);
            refill(((Player) e.getWhoClicked()), ItemBank.RANK_LEVEL_2, 2);
            refill(((Player) e.getWhoClicked()), ItemBank.RANK_LEVEL_3, 3);
            refill(((Player) e.getWhoClicked()), ItemBank.RANK_LEVEL_4, 4);
        }
    }

    private void refill(Player p, ItemStack item, int slot) {
        if (!p.getInventory().containsAtLeast(item, 1)) {
            p.getInventory().setItem(slot, item);
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
            if (!TASKS.isEmpty()) {
                TASKS.poll().run();
                e.setCancelled(true);
                last = System.currentTimeMillis();
            }
        }

        // 銳評
        if (e.getItem().isSimilar(ItemBank.RANK_LEVEL_0)) {
            if (!Main.BDGAME.isRanking()) {
                e.getPlayer().sendMessage("§c銳評系統尚未開啟!");
                return;
            }
            e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_CAT_AMBIENT, 100, 0);
            Main.BDGAME.vote(e.getPlayer().getName(), 1);
        }
        if (e.getItem().isSimilar(ItemBank.RANK_LEVEL_1)) {
            if (!Main.BDGAME.isRanking()) {
                e.getPlayer().sendMessage("§c銳評系統尚未開啟!");
                return;
            }
            e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_CAT_AMBIENT, 100F, 0.5F);
            Main.BDGAME.vote(e.getPlayer().getName(), 2);
        }
        if (e.getItem().isSimilar(ItemBank.RANK_LEVEL_2)) {
            if (!Main.BDGAME.isRanking()) {
                e.getPlayer().sendMessage("§c銳評系統尚未開啟!");
                return;
            }
            e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_CAT_AMBIENT, 100F, 1F);
            Main.BDGAME.vote(e.getPlayer().getName(), 3);
        }
        if (e.getItem().isSimilar(ItemBank.RANK_LEVEL_3)) {
            if (!Main.BDGAME.isRanking()) {
                e.getPlayer().sendMessage("§c銳評系統尚未開啟!");
                return;
            }
            e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_CAT_AMBIENT, 100F, 1.5F);
            Main.BDGAME.vote(e.getPlayer().getName(), 4);
        }
        if (e.getItem().isSimilar(ItemBank.RANK_LEVEL_4)) {
            if (!Main.BDGAME.isRanking()) {
                e.getPlayer().sendMessage("§c銳評系統尚未開啟!");
                return;
            }
            e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_CAT_AMBIENT, 100F, 2F);
            Main.BDGAME.vote(e.getPlayer().getName(), 5);
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
