package com.cutesmouse.bdgame.buildKit;

import com.cutesmouse.bdgame.*;
import com.cutesmouse.bdgame.tools.BlockOutlineParticle;
import com.cutesmouse.bdgame.tools.ItemBank;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class BuildTools {

    private static HashMap<String, Long> COOLDOWN = new HashMap<>();
    private static HashMap<String, UndoQueue> UNDO = new HashMap<>();

    // 選取工具
    public static void invokeSelectionTool(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Location target = e.getClickedBlock().getLocation();
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        Room current = data.currentRoom();
        if (!current.isInside(target)) {
            e.getPlayer().sendMessage("§c超出可選取範圍!");
            return;
        }
        SelectedArea area = data.getSelectedArea();
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) area.firstPoint = target;
        else area.secondPoint = target;
        e.getPlayer().sendMessage("§d已設定第" + (e.getAction().equals(Action.LEFT_CLICK_BLOCK) ? "一" : "二") + "個選擇點" + (area.ready() ? " (" + area.size() + ")" : ""));
    }

    // 填滿工具
    public static void invokeSetTool(PlayerInteractEvent e) {
        if (COOLDOWN.containsKey(e.getPlayer().getName())) {
            long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
            if (time < 1000) {
                e.getPlayer().sendMessage("§c請間隔一秒後再進行操作!");
                return;
            }
        }
        if (e.getClickedBlock() == null) return;
        Material target = e.getClickedBlock().getType();
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        Room current = data.currentRoom();
        SelectedArea area = data.getSelectedArea();
        if (!area.ready()) {
            e.getPlayer().sendMessage("§c尚未設定選取點! 請使用選取工具設定!");
            return;
        }
        if (!current.isInside(area.firstPoint) || !current.isInside(area.secondPoint)) {
            area.clear();
            e.getPlayer().sendMessage("§c選取點錯誤! 請重新選擇!");
            return;
        }
        ArrayList<Runnable> undo = new ArrayList<>();
        int times = area.forEach(loc -> {
            if (loc.getBlock().getType().equals(target)) return false;
            final Material oldType = loc.getBlock().getType();
            final BlockData oldData = loc.getBlock().getBlockData();
            undo.add(() -> {
                loc.getBlock().setType(oldType);
                loc.getBlock().setBlockData(oldData);
            });
            loc.getBlock().setType(target);
            return true;
        });
        if (times == 0) {
            e.getPlayer().sendMessage("§c未做任何變更!");
            return;
        }
        e.getPlayer().sendMessage("§a已完成設定! 共更新 " + times + " 個方塊");
        COOLDOWN.put(e.getPlayer().getName(), System.currentTimeMillis());
        UNDO.put(e.getPlayer().getName(), new UndoQueue(undo));
    }

    // 複製工具
    public static void invokeCopyTool(PlayerInteractEvent e) {
        if (COOLDOWN.containsKey(e.getPlayer().getName())) {
            long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
            if (time < 3000) {
                e.getPlayer().sendMessage("§c此物品冷卻中! 請等待 §e" + (3 - (time / 1000)) + " §c秒!");
                return;
            }
        }
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        SelectedArea area = data.getSelectedArea();
        Location ori = e.getPlayer().getLocation();
        if (!area.ready()) {
            e.getPlayer().sendMessage("§c尚未設定選取點! 請使用選取工具設定!");
            return;
        }
        if (!data.currentRoom().isInside(area.firstPoint) || !data.currentRoom().isInside(area.secondPoint)) {
            area.clear();
            e.getPlayer().sendMessage("§c選取點錯誤! 請重新選擇!");
            return;
        }
        ClipBoardData clip = new ClipBoardData(area.getSizeVector(),
                area.getMinVector().subtract(new Vector(ori.getBlockX(), ori.getBlockY(), ori.getBlockZ())));
        int copied = area.forEach(loc -> {
            clip.add(new BlockStatus(ori, loc.getBlock()));
            return true;
        });
        data.getSelectedArea().Clipboard = clip;
        e.getPlayer().sendMessage("§a已複製 " + copied + " 個方塊!");
        COOLDOWN.put(e.getPlayer().getName(), System.currentTimeMillis());
    }

    // 黏貼工具
    public static void invokePasteTool(PlayerInteractEvent e) {
        if (COOLDOWN.containsKey(e.getPlayer().getName())) {
            long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
            if (time < 1000) {
                e.getPlayer().sendMessage("§c請間隔一秒後再進行操作!");
                return;
            }
        }
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        SelectedArea area = data.getSelectedArea();
        Location ori = e.getPlayer().getLocation();
        if (area.Clipboard == null) {
            e.getPlayer().sendMessage("§c尚未複製! 請使用複製工具設定!");
            return;
        }
        Room room = data.currentRoom();
        int ignore = 0;
        int success = 0;
        ArrayList<Runnable> undo = new ArrayList<>();
        for (BlockStatus status : area.Clipboard.DATA) {
            Location place = status.putPlace(ori);
            if (room.isInside(place)) {
                final Material oldMaterial = place.getBlock().getType();
                if (oldMaterial.equals(status.material)) continue;
                final BlockData oldData = place.getBlock().getBlockData();
                undo.add(() -> {
                    place.getBlock().setType(oldMaterial);
                    place.getBlock().setBlockData(oldData);
                });
                place.getBlock().setType(status.material);
                place.getBlock().setBlockData(status.data);
                success++;
            } else {
                ignore++;
            }
        }
        e.getPlayer().sendMessage("§a已貼上 " + success + " 個方塊!");
        if (ignore > 0) e.getPlayer().sendMessage("§c因為牴觸邊界 有 " + ignore + " 個方塊被自動忽略!");
        UNDO.put(e.getPlayer().getName(), new UndoQueue(undo));
        COOLDOWN.put(e.getPlayer().getName(), System.currentTimeMillis());
    }

    // 場景工具
    public static void invokePlotTool(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (COOLDOWN.containsKey(e.getPlayer().getName())) {
            long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
            if (time < 1000) {
                e.getPlayer().sendMessage("§c請間隔一秒後再進行操作!");
                return;
            }
        }
        ArrayList<Runnable> undo = new ArrayList<>();
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        if (!data.isPlaying()) return;
        Room current = data.currentRoom();
        World w = current.loc.getWorld();
        if (w == null) return;
        Material type = e.getClickedBlock().getType();
        for (int x = current.getMinBuildX(); x <= current.getMaxBuildX(); x++) {
            for (int z = current.getMinBuildZ(); z <= current.getMaxBuildZ(); z++) {
                Block b = w.getBlockAt(x, 100, z);
                final Material oldType = b.getType();
                if (!b.getType().equals(Material.AIR) && !b.getType().equals(type)) {
                    undo.add(() -> b.setType(oldType));
                    b.setType(type);
                }
            }
        }
        COOLDOWN.put(e.getPlayer().getName(), System.currentTimeMillis());
        UNDO.put(e.getPlayer().getName(), new UndoQueue(undo));
        return;
    }

    // 重置工具
    public static void invokeResetTool(PlayerInteractEvent e) {
        if (COOLDOWN.containsKey(e.getPlayer().getName())) {
            long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
            if (time < 3000) {
                e.getPlayer().sendMessage("§c此物品冷卻中! 請等待 §e" + (3 - (time / 1000)) + " §c秒!");
                return;
            }
        }
        Room current = PlayerDataManager.getPlayerData(e.getPlayer()).currentRoom();
        World w = current.loc.getWorld();
        if (w == null) return;
        ArrayList<Runnable> undo = new ArrayList<>();
        for (int x = current.getMinBuildX(); x <= current.getMaxBuildX(); x++) {
            for (int z = current.getMinBuildZ(); z <= current.getMaxBuildZ(); z++) {
                for (int y = 100; y <= current.getMaxBuildY(); y++) {
                    Block b = w.getBlockAt(x, y, z);
                    if (y == 100 && b.getType().equals(Material.WHITE_CONCRETE)) continue;
                    if (b.getType().equals(Material.AIR)) continue;
                    final Material oldType = b.getType();
                    final BlockData oldData = b.getBlockData();
                    undo.add(() -> {
                        b.setType(oldType);
                        b.setBlockData(oldData, true);
                    });
                    b.setType(y != 100 ? Material.AIR : Material.WHITE_CONCRETE);
                }
            }
        }
        UNDO.put(e.getPlayer().getName(), new UndoQueue(undo));
        COOLDOWN.put(e.getPlayer().getName(), System.currentTimeMillis());
        e.getPlayer().sendMessage("§a已將地圖復原完畢");
    }

    // 復原工具
    public static void invokeUndoTool(PlayerInteractEvent e) {
        if (!UNDO.containsKey(e.getPlayer().getName())) {
            e.getPlayer().sendMessage("§c你近期沒有做任何動作的紀錄");
            return;
        }
        UndoQueue undo = UNDO.remove(e.getPlayer().getName());
        if (undo.STAGE != Main.BDGAME.getStage()) {
            e.getPlayer().sendMessage("§c你近期沒有做任何動作的紀錄");
            return;
        }
        for (Runnable r : undo.LIST) {
            r.run();
        }
        e.getPlayer().sendMessage("§a復原完成! 總計 " + undo.LIST.size() + " 個動作!");
    }

    private static HashMap<String, BukkitRunnable> showing = new HashMap<>();

    public static void showHoverOutline(Player p, Plugin plugin, int slot) {
        ItemStack hand = p.getInventory().getItem(slot);
        if (hand == null) return;
        if (hand.isSimilar(ItemBank.SELECT) || hand.isSimilar(ItemBank.COPY) ||
                hand.isSimilar(ItemBank.PASTE) || hand.isSimilar(ItemBank.SET) ||
                hand.isSimilar(ItemBank.RESET) || hand.isSimilar(ItemBank.SETPLOT)) {
            if (showing.containsKey(p.getName())) return;
            else addShowingTask(p, plugin);
        } else {
            BukkitRunnable remove = showing.remove(p.getName());
            if (remove != null) remove.cancel();
        }
    }

    private static void addShowingTask(Player p, Plugin plugin) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack hand = p.getInventory().getItemInMainHand();
                Color c = Color.black;
                if (hand.isSimilar(ItemBank.SELECT)) c = new Color(0, 255, 10);
                else if (hand.isSimilar(ItemBank.COPY)) c = new Color(236, 255, 25);
                else if (hand.isSimilar(ItemBank.SET)) c = new Color(0, 14, 178);
                else if (hand.isSimilar(ItemBank.PASTE)) c = new Color(0, 149, 255);
                else if (hand.isSimilar(ItemBank.SETPLOT)) c = new Color(255, 255, 255);
                else if (hand.isSimilar(ItemBank.RESET)) c = new Color(255, 0, 0);
                if (hand.isSimilar(ItemBank.SELECT) || hand.isSimilar(ItemBank.COPY) ||
                        hand.isSimilar(ItemBank.SET)) { // 為選取、複製、貼上工具時，顯示選取位置
                    PlayerData data = PlayerDataManager.getPlayerData(p);
                    SelectedArea area = data.getSelectedArea();
                    if (!area.ready()) return;
                    if (!data.currentRoom().isInside(area.firstPoint) || !data.currentRoom().isInside(area.secondPoint))
                        return;
                    BlockOutlineParticle.playOutlineParticle(p,
                            area.getMinLocation(),
                            area.getMaxLocation().add(1, 1, 1),
                            c);
                } else if (hand.isSimilar(ItemBank.PASTE)) {
                    PlayerData data = PlayerDataManager.getPlayerData(p);
                    SelectedArea area = data.getSelectedArea();
                    if (area.Clipboard == null) return;
                    Location ori = new Location(p.getWorld(), p.getLocation().getBlockX(),
                            p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                    BlockOutlineParticle.playOutlineParticle(p,
                            ori.clone().add(area.Clipboard.getOffset()),
                            ori.clone().add(area.Clipboard.getOffset()).add(area.Clipboard.getSize()).add(1, 1, 1),
                            c);
                } else if (hand.isSimilar(ItemBank.SETPLOT)) {
                    Room current = PlayerDataManager.getPlayerData(p).currentRoom();
                    BlockOutlineParticle.playOutlineParticle(p,
                            new Location(p.getWorld(), current.getMinBuildX(), current.getMinBuildY(), current.getMinBuildZ()),
                            new Location(p.getWorld(), current.getMaxBuildX(), current.getMinBuildY(), current.getMaxBuildZ()).add(1, 1, 1),
                            c);
                } else if (hand.isSimilar(ItemBank.RESET)) {
                    Room current = PlayerDataManager.getPlayerData(p).currentRoom();
                    BlockOutlineParticle.playOutlineParticle(p,
                            new Location(p.getWorld(), current.getMinBuildX(), current.getMinBuildY(), current.getMinBuildZ()),
                            new Location(p.getWorld(), current.getMaxBuildX(), current.getMaxBuildY(), current.getMaxBuildZ()).add(1, 1, 1),
                            c);
                }
            }
        };
        task.runTaskTimer(plugin, 1L, 5L);
        showing.put(p.getName(), task);
    }
}
