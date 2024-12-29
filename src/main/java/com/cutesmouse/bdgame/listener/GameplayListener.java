package com.cutesmouse.bdgame.listener;

import com.cutesmouse.bdgame.*;
import com.cutesmouse.bdgame.buildKit.BlockStatus;
import com.cutesmouse.bdgame.buildKit.ClipBoardData;
import com.cutesmouse.bdgame.buildKit.EntityEditting;
import com.cutesmouse.bdgame.buildKit.SelectedArea;
import com.cutesmouse.bdgame.tools.ItemBank;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Creature;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
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

    private static HashMap<String, Long> COOLDOWN = new HashMap<>();
    private static HashMap<String, UndoQueue> UNDO = new HashMap<>();

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (BANNED_ITEM.contains(e.getMaterial())) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                e.setCancelled(true);
            return;
        }
        if (Main.BDGAME.getStage() > 0 && Main.BDGAME.getStage() % 2 == 0 && Main.BDGAME.getStage() < Main.BDGAME.getMaxStage()) {
            e.setCancelled(true);
            // 選取工具
            if (e.getItem().isSimilar(ItemBank.SELECT)) {
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
                e.getPlayer().sendMessage("§d已設定第" + (e.getAction().equals(Action.LEFT_CLICK_BLOCK) ? "一" : "二") + "個選擇點" +
                        (area.ready() ? " (" + area.size() + ")" : ""));
                return;
            }
            // 填滿工具
            if (e.getItem().isSimilar(ItemBank.SET)) {
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
                return;
            }
            // 複製工具
            if (e.getItem().isSimilar(ItemBank.COPY)) {
                if (COOLDOWN.containsKey(e.getPlayer().getName())) {
                    long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
                    if (time < 10000) {
                        e.getPlayer().sendMessage("§c此物品冷卻中! 請等待 §e" + (10 - (time / 1000)) + " §c秒!");
                        return;
                    }
                }
                PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
                SelectedArea area = data.getSelectedArea();
                Location ori = (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? e.getClickedBlock().getLocation() : e.getPlayer().getLocation());
                if (!area.ready()) {
                    e.getPlayer().sendMessage("§c尚未設定選取點! 請使用選取工具設定!");
                    return;
                }
                if (!data.currentRoom().isInside(area.firstPoint) || !data.currentRoom().isInside(area.secondPoint)) {
                    area.clear();
                    e.getPlayer().sendMessage("§c選取點錯誤! 請重新選擇!");
                    return;
                }
                ClipBoardData clip = new ClipBoardData();
                int copied = area.forEach(loc -> {
                    clip.add(new BlockStatus(ori, loc.getBlock()));
                    return true;
                });
                data.getSelectedArea().Clipboard = clip;
                e.getPlayer().sendMessage("§a已複製 " + copied + " 個方塊!");
                COOLDOWN.put(e.getPlayer().getName(), System.currentTimeMillis());
                return;
            }
            if (e.getItem().isSimilar(ItemBank.PASTE)) {
                if (COOLDOWN.containsKey(e.getPlayer().getName())) {
                    long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
                    if (time < 1000) {
                        e.getPlayer().sendMessage("§c請間隔一秒後再進行操作!");
                        return;
                    }
                }
                PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
                SelectedArea area = data.getSelectedArea();
                Location ori = (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) ? e.getClickedBlock().getLocation() : e.getPlayer().getLocation());
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
                return;
            }
            if (e.getItem().isSimilar(ItemBank.SETPLOT)) {
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
            if (e.getItem().isSimilar(ItemBank.RESET)) {
                if (COOLDOWN.containsKey(e.getPlayer().getName())) {
                    long time = System.currentTimeMillis() - COOLDOWN.get(e.getPlayer().getName());
                    if (time < 10000) {
                        e.getPlayer().sendMessage("§c請間隔十秒後再進行操作!");
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
                            if (y == 100 && b.getType().equals(Material.LIGHT_BLUE_TERRACOTTA)) continue;
                            if (b.getType().equals(Material.AIR)) continue;
                            final Material oldType = b.getType();
                            final BlockData oldData = b.getBlockData();
                            undo.add(() -> {
                                b.setType(oldType);
                                b.setBlockData(oldData, true);
                            });
                            b.setType(y != 100 ? Material.AIR : Material.LIGHT_BLUE_TERRACOTTA);
                        }
                    }
                }
                UNDO.put(e.getPlayer().getName(), new UndoQueue(undo));
                COOLDOWN.put(e.getPlayer().getName(), System.currentTimeMillis());
                return;
            }
            if (e.getItem().isSimilar(ItemBank.UNDO)) {
                if (!UNDO.containsKey(e.getPlayer().getName())) {
                    e.getPlayer().sendMessage("§c你近期沒有做任何動作的紀錄!");
                    return;
                }
                UndoQueue undo = UNDO.remove(e.getPlayer().getName());
                if (undo.STAGE != Main.BDGAME.getStage()) {
                    e.getPlayer().sendMessage("§c你近期沒有做任何動作的紀錄!");
                    return;
                }
                for (Runnable r : undo.LIST) {
                    r.run();
                }
                e.getPlayer().sendMessage("§a復原完成! 總計 " + undo.LIST.size() + " 個動作!");
                return;
            }
            e.setCancelled(false);
        }
        if (e.getItem().isSimilar(ItemBank.MENU)) {
            GameMenuHandler.open(e.getPlayer());
            e.setCancelled(true);
            return;
        }
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
    public void onPistonExtend(BlockPistonExtendEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            return;
        }
        if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlaceMob(CreatureSpawnEvent e) {
        if (e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) {
            e.getEntity().setAI(false);
            e.getEntity().setGravity(false);
            e.getEntity().setSilent(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent e) {
        if (Main.BDGAME.getStage() == 0) return;
        if (Main.BDGAME.getStage() % 2 != 0) e.setCancelled(true);
        if (e.getPlayer().isOp()) return;
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        if (!data.isPlaying()) return;
        if (Main.BDGAME.getMapManager().canBuild(data.currentRoom().loc, e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreakPlace(BlockPlaceEvent e) {
        if (Main.BDGAME.getStage() == 0) return;
        if (Main.BDGAME.getStage() % 2 != 0) e.setCancelled(true);
        if (e.getPlayer().isOp()) return;
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        if (!data.isPlaying()) return;
        if (Main.BDGAME.getMapManager().canBuild(data.currentRoom().loc, e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (Main.BDGAME.getStage() == 0) return;
        if (Main.BDGAME.getStage() == Main.BDGAME.getMaxStage()) return;
        if (e.getTo() == null) return;
        if (e.getTo().getY() > 135 || e.getTo().getY() < 100) {
            PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
            if (!data.isPlaying()) return;
            e.setTo(data.currentRoom().loc);
        }
    }

    @EventHandler
    public void onExp(EntityExplodeEvent e) {
        e.blockList().clear();
    }

    @EventHandler
    public void onTel(PlayerTeleportEvent e) {
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) {
            if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickEntity(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player) return;
        if (Main.BDGAME.getStage() == 0) return;
        e.setCancelled(true);
        if (Main.BDGAME.getStage() % 2 != 0) return;
        if (e.getRightClicked() instanceof ItemFrame) {
            if (e.getPlayer().isSneaking())
                EntityEditting.openItemFrame(e.getPlayer(), ((ItemFrame) e.getRightClicked()));
            else e.setCancelled(false);
            return;
        }
        if (!(e.getRightClicked() instanceof LivingEntity)) return;

        EntityEditting.open(e.getPlayer(), ((LivingEntity) e.getRightClicked()));
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        EntityEditting.reset(e.getPlayer());
    }

    // prevent zombies, skeletons, etc.. from burning
    @EventHandler
    public void onCombust(EntityCombustEvent e) {
        if (e.getEntity().getFireTicks() > (Integer.MAX_VALUE >> 1)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (Main.BDGAME.getStage() == 0) return;
        if (Main.BDGAME.getStage() % 2 != 0) return;
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        Room current = data.currentRoom();
        if (!current.isInside(e.getItemDrop().getLocation())) {
            e.setCancelled(true);
            return;
        }
        e.getItemDrop().setPickupDelay(Integer.MAX_VALUE);
        e.getItemDrop().setGravity(false);
        e.getItemDrop().setUnlimitedLifetime(true);
        e.getItemDrop().setPersistent(true);
        e.getItemDrop().setVelocity(new Vector());
        e.getItemDrop().teleport(e.getItemDrop().getLocation().subtract(0, 1.25, 0));
    }
}
