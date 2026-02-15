package com.cutesmouse.bdgame.listeners;

import com.cutesmouse.bdgame.Main;
import com.cutesmouse.bdgame.game.PlayerData;
import com.cutesmouse.bdgame.game.PlayerDataManager;
import com.cutesmouse.bdgame.game.Room;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlockRulesListener implements Listener {
    @EventHandler // 禁止使用活塞
    public void onPistonExtend(BlockPistonExtendEvent e) {
        e.setCancelled(true);
    }

    @EventHandler // 實體不可受到傷害
    public void onEntityDamaged(EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            return;
        }
        if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler // 生怪蛋召喚怪物的預設屬性
    public void onPlaceMob(CreatureSpawnEvent e) {
        if (e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)) {
            e.getEntity().setAI(false);
            e.getEntity().setGravity(false);
            e.getEntity().setSilent(true);
            e.getEntity().setPersistent(true);
            e.getEntity().setInvulnerable(true);
            e.getEntity().setRemoveWhenFarAway(false);
            if (e.getEntity() instanceof Breedable aged) aged.setAgeLock(true);
        }
    }

    @EventHandler // 盔甲架的基本屬性
    public void onArmorStand(EntitySpawnEvent e) {
        if (e.getEntity() instanceof ArmorStand) {
            ArmorStand as = ((ArmorStand) e.getEntity());
            as.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            as.addEquipmentLock(EquipmentSlot.BODY, ArmorStand.LockType.ADDING_OR_CHANGING);
            as.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            as.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            as.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            as.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            as.setGravity(false);
            as.setArms(true);
            as.setBasePlate(false);
        }
    }

    @EventHandler // 方塊破壞
    public void onBreakBlock(BlockBreakEvent e) {
        if (Main.BDGAME.isPreparingStage()) return;
        if (!Main.BDGAME.isBuildingStage()) e.setCancelled(true);
        PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
        if (!data.isPlaying()) return;
        if (data.currentRoom().isInside(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler // 防止移動
    public void onMove(PlayerMoveEvent e) {
        if (Main.BDGAME.isPreparingStage()) return;
        if (Main.BDGAME.isViewingStage()) return;
        if (e.getTo() == null) return;
        if (e.getTo().getY() > 135 || e.getTo().getY() < 100) {
            PlayerData data = PlayerDataManager.getPlayerData(e.getPlayer());
            if (!data.isPlaying()) return;
            e.setTo(data.currentRoom().getSpawnLocation());
        }
    }

    @EventHandler // 防止爆炸
    public void onExp(EntityExplodeEvent e) {
        e.blockList().clear();
    }

    @EventHandler // 防止傳送
    public void onTel(PlayerTeleportEvent e) {
        if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) {
            if (!e.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) e.setCancelled(true);
        }
    }

    @EventHandler // 防止開啟村民交易介面、盔甲架被變動、命名牌
    public void onVillagerMenu(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Villager) e.setCancelled(true);
        else if (e.getRightClicked() instanceof ArmorStand) e.setCancelled(true);

        if (e.getRightClicked() instanceof ItemFrame) {
            if (e.getPlayer().isSneaking()) {
                e.setCancelled(true);
                return;
            }
            ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
            if (hand.getItemMeta() == null || !hand.getItemMeta().hasDisplayName()) return;
            e.getPlayer().sendMessage("§c為了最佳化遊戲體驗，建築大賽中禁止將命名過的物品放入物品展示框");
            e.setCancelled(true);
            return;
        }

        if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG)) {
            e.getPlayer().sendMessage("§c為了最佳化遊戲體驗，建築大賽中禁止使用命名牌");
            e.setCancelled(true);
        }
    }

    @EventHandler // 防止生物被燃燒
    public void onCombust(EntityCombustEvent e) {
        if (e.getEntity().getFireTicks() > (Integer.MAX_VALUE >> 1)) return;
        e.setCancelled(true);
    }

    @EventHandler // 防止掉落物消失
    public void onItemDrop(PlayerDropItemEvent e) {
        if (Main.BDGAME.isPreparingStage()) return;
        if (!Main.BDGAME.isBuildingStage()) return;
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

    @EventHandler // 防止物品展示框消失
    public void onItemFrameBreak(HangingBreakEvent e) {
        if (!e.getCause().equals(HangingBreakEvent.RemoveCause.ENTITY)) e.setCancelled(true);
    }

}
