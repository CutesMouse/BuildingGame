package com.cutesmouse.bdgame.tools;

import com.cutesmouse.mgui.GUI;
import com.cutesmouse.mgui.GUIItem;
import com.saicone.rtag.RtagEntity;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EntityEditting {
    private static class ArmorTask {
        // 0, 1 正、副手
        // 2, 3, 4, 5 帽子、胸甲、褲子、鞋子
        private int position;
        private LivingEntity entity;
        private GUI gui;
        private int gui_slot;

        private ArmorTask(int position, LivingEntity entity, GUI gui, int gui_slot) {
            this.position = position;
            this.entity = entity;
            this.gui = gui;
            this.gui_slot = gui_slot;
        }
    }

    private static class MovingTask {
        private MoveType type;
        private LivingEntity entity;
        private ArmorStand as;
        private Location base;
        private EulerAngle base_angle;
        private static final double ADJUST_PARAMETER = 0.06;
        private double y_offset;

        private MovingTask(LivingEntity entity) {
            this.entity = entity;
            this.type = MoveType.TELEPORT;
            y_offset = 0;
        }

        private MovingTask(ArmorStand as, MoveType type, Location base) {
            this.as = as;
            this.type = type;
            this.base = base;
            savePose();
        }

        private void savePose() {
            switch (type) {
                case TELEPORT:
                    break;
                case HEAD:
                    base_angle = as.getHeadPose();
                    break;
                case BODY:
                    base_angle = as.getBodyPose();
                    break;
                case LEFT_HAND:
                    base_angle = as.getLeftArmPose();
                    break;
                case RIGHT_HAND:
                    base_angle = as.getRightArmPose();
                    break;
                case LEFT_FEET:
                    base_angle = as.getLeftLegPose();
                    break;
                case RIGHT_FEET:
                    base_angle = as.getRightLegPose();
                    break;
            }
        }

        private void setPoseOffset(double x, double y, double z) {
            EulerAngle angle = new EulerAngle(x + base_angle.getX(), y + base_angle.getY(), z + base_angle.getZ());
            switch (type) {
                case TELEPORT:
                    break;
                case HEAD:
                    as.setHeadPose(angle);
                    break;
                case BODY:
                    as.setBodyPose(angle);
                    break;
                case LEFT_HAND:
                    as.setLeftArmPose(angle);
                    break;
                case RIGHT_HAND:
                    as.setRightArmPose(angle);
                    break;
                case LEFT_FEET:
                    as.setLeftLegPose(angle);
                    break;
                case RIGHT_FEET:
                    as.setRightLegPose(angle);
                    break;
            }
        }

        private void moveEntity(Player player) {
            // assert now is stage 2
            // check if player's pointing at a location
            // which is inside the room
            if (type.equals(MoveType.TELEPORT)) {
                //Room room = PlayerDataManager.getPlayerData(player).currentRoom();
                RayTraceResult trace = player.rayTraceBlocks(30.0);
                if (trace == null || trace.getHitBlock() == null) return;
                Vector vec = trace.getHitPosition();
                Location loc = new Location(entity.getWorld(), vec.getX(), vec.getY() + y_offset, vec.getZ(),
                        entity.getLocation().getYaw(), entity.getLocation().getPitch());
                //if (!room.isInside(loc)) return;
                entity.teleport(loc);
            } else {
                setPoseOffset(ADJUST_PARAMETER * (base.getPitch() - player.getLocation().getPitch()), 0,
                        ADJUST_PARAMETER * (base.getYaw() - player.getLocation().getYaw()));
            }
        }

        private void adjustHeight(Player player, int old_slot, int new_slot) {
            int direction = 1; // +: Up Slot-, -: Down Slot+
            if ((old_slot == 8 && new_slot == 0) || new_slot != 8 && (new_slot > old_slot)) {  // DOWN
                direction = -1;
            }
            y_offset += direction * ADJUST_PARAMETER;
            moveEntity(player);
        }

        private enum MoveType {
            TELEPORT, HEAD, BODY, LEFT_HAND, RIGHT_HAND, LEFT_FEET, RIGHT_FEET
        }
    }

    private static HashMap<String, ArmorTask> armorTasks = new HashMap<>();
    private static HashMap<String, MovingTask> movingTasks = new HashMap<>();

    public static void resetArmorTask(HumanEntity player) {
        armorTasks.remove(player.getName());
    }

    public static boolean resetMovingTask(Player player) {
        if (movingTasks.remove(player.getName()) != null) {
            player.sendTitle("", "§a放置完成", 0, 10, 5);
            return true;
        }
        return false;
    }

    public static void mouseScroll(PlayerItemHeldEvent e) {
        if (!movingTasks.containsKey(e.getPlayer().getName())) return;
        MovingTask task = movingTasks.get(e.getPlayer().getName());
        if (!task.type.equals(MovingTask.MoveType.TELEPORT)) return;
        task.adjustHeight(e.getPlayer(), e.getPreviousSlot(), e.getNewSlot());
        e.setCancelled(true);
    }

    public static void playerMove(PlayerMoveEvent e) {
        if (!movingTasks.containsKey(e.getPlayer().getName())) return;
        MovingTask task = movingTasks.get(e.getPlayer().getName());
        task.moveEntity(e.getPlayer());
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
        if (!armorTasks.containsKey(player.getName())) return false;
        ArmorTask armorTask = armorTasks.get(player.getName());
        armorTasks.remove(player.getName());
        ItemStack item = armorTask.gui.getInv().getItem(armorTask.gui_slot);
        item.setType(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
        armorTask.gui.getInv().setItem(armorTask.gui_slot, item);
        LivingEntity entity = armorTask.entity;
        if (entity == null || entity.getEquipment() == null) {
            player.sendMessage("§c此生物無法設定裝備!");
            return true;
        }
        equipEntity(entity, player.getInventory().getItem(slot), armorTask.position);
        return true;
    }

    public static void open(Player player, LivingEntity entity) {
        EntityType type = entity.getType();
        boolean special = type.equals(EntityType.ARMOR_STAND) || type.equals(EntityType.SHEEP) || type.equals(EntityType.VILLAGER);
        GUI gui = new GUI("§e● 2026 跨年建築大賽", special ? 6 : 5, player);
        for (int i = 0; i < (9 * (special ? 6 : 5)); i++) {
            final int slot = i;
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", (e, item) -> {
                e.setCancelled(true);
                putOnEquipment(player, slot);
            }));
        }
        // General Options
        gui.addItem(10, new GUIItem(Material.COMPASS, new ArrayList<>(Arrays.asList("§b▶ 讓生物的正臉面向自己")),
                "§f面向自己", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, 10)) return;
            e.getWhoClicked().closeInventory();
            entity.teleport(entity.getLocation().setDirection(player.getLocation().subtract(entity.getLocation()).toVector()));
        }));
        if (entity instanceof Ageable || entity instanceof ArmorStand) {
            gui.addItem(11, new GUIItem(Material.CLOCK, new ArrayList<>(Arrays.asList("§b▶ 切換生物是否為嬰兒狀態", "§e☉ 只有存在嬰兒狀態的生物才會出現這個選項")),
                    "§f嬰兒狀態", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 11)) return;
                e.getWhoClicked().closeInventory();
                if (entity instanceof Ageable) {
                    Ageable age = (Ageable) entity;
                    if (age.isAdult()) age.setBaby();
                    else age.setAdult();
                } else if (entity instanceof ArmorStand) {
                    ArmorStand as = ((ArmorStand) entity);
                    as.setSmall(!as.isSmall());
                }
            }));
        }
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
        // Villager Options - Profession, Type
        if (entity instanceof Villager) {
            gui.addItem(21, new GUIItem(Material.VILLAGER_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 更換村民的職業")),
                    "§f更改職業", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 21)) return;
                openVillagerProfession(player, ((Villager) entity));
            }));
            gui.addItem(23, new GUIItem(Material.VILLAGER_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 更換村民的外觀")),
                    "§f更改外觀", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 23)) return;
                openVillagerType(player, ((Villager) entity));
            }));
        }
        // Armor Stand Options - Pose, Plate, Show armor
        if (entity instanceof ArmorStand) {
            ArmorStand as = ((ArmorStand) entity);
            gui.addItem(21, new GUIItem(Material.SMOOTH_STONE_SLAB, new ArrayList<>(Arrays.asList("§b▶ 把盔甲架的底盤隱藏起來", "§e☉ 預設為隱藏")),
                    "§f隱藏底盤", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 21)) return;
                e.getWhoClicked().closeInventory();
                as.setBasePlate(!as.hasBasePlate());
            }));
            gui.addItem(22, new GUIItem(Material.ARMOR_STAND, new ArrayList<>(Arrays.asList("§b▶ 調整盔甲架姿勢")),
                    "§f設定姿勢", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 22)) return;
                e.getWhoClicked().closeInventory();
                openArmorStandPose(player, as);
            }));
            gui.addItem(23, new GUIItem(Material.WOODEN_SWORD, new ArrayList<>(Arrays.asList("§b▶ 顯示盔甲架的手臂", "§e☉ 預設為顯示")),
                    "§f顯示手臂", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 23)) return;
                e.getWhoClicked().closeInventory();
                as.setArms(!as.hasArms());
            }));
        }
        // Sheep Options - Color sheep
        if (entity instanceof Sheep) {
            gui.addItem(22, new GUIItem(Material.SHEEP_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定彩虹羊毛", "§e☉ 與顛倒生物互斥")),
                    "§f彩色羊毛", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 22)) return;
                e.getWhoClicked().closeInventory();
                if (entity.getCustomName() != null && entity.getCustomName().equals("jeb_")) entity.setCustomName(null);
                else entity.setCustomName("jeb_");
                entity.setCustomNameVisible(false);
            }));
        }

        // Slime Options - Slime size
        if (entity instanceof Slime) {
            gui.addItem(22, new GUIItem(entity instanceof MagmaCube ? Material.MAGMA_CUBE_SPAWN_EGG : Material.SLIME_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定史萊姆尺寸", "§e☉ 大小需界在1~16之間")),
                    "§f更改史萊姆尺寸", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 22)) return;
                e.getWhoClicked().closeInventory();
                openSlimeSize(player, ((Slime) entity));
            }));
        }

        // Shulker Options - Shulker Peek, Shulker color
        if (entity instanceof Shulker) {
            gui.addItem(21, new GUIItem(Material.SHULKER_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定界伏蚌開啟程度", "§e☉ 大小需界在0~1之間")),
                    "§f更改界伏蚌開啟程度", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 21)) return;
                e.getWhoClicked().closeInventory();
                openShulkerPeek(player, ((Shulker) entity));
            }));
            gui.addItem(23, new GUIItem(Material.SHULKER_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定界伏蚌顏色")),
                    "§f更改界伏蚌顏色", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 23)) return;
                e.getWhoClicked().closeInventory();
                openShulkerColor(player, ((Shulker) entity));
            }));
        }

        // Wolf Options - Sit, Variant, Angry
        if (entity instanceof Wolf) {
            Wolf wolf = ((Wolf) entity);
            gui.addItem(21, new GUIItem(Material.BONE, new ArrayList<>(Arrays.asList("§b▶ 讓狼坐下")),
                    "§f切換坐下狀態", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 21)) return;
                e.getWhoClicked().closeInventory();
                wolf.setSitting(!wolf.isSitting());
            }));
            gui.addItem(22, new GUIItem(Material.WOLF_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定狼的變種")),
                    "§f更改外觀", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 22)) return;
                openWolfType(player, wolf);
            }));
            gui.addItem(23, new GUIItem(Material.COOKED_BEEF, new ArrayList<>(Arrays.asList("§b▶ 讓狼生氣", "§e☉ 狗不可為馴服狀態")),
                    "§f切換生氣狀態", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 23)) return;
                e.getWhoClicked().closeInventory();
                wolf.setAngry(!wolf.isAngry());
            }));
        }

        // Chicken, Cow, Pig Options - Temperature Variants
        if (entity instanceof Chicken) {
            gui.addItem(22, new GUIItem(Material.CHICKEN_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定雞的溫度變種")),
                    "§f更改外觀", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 22)) return;
                openTemperatureVariant(player, ((Animals) entity));
            }));
        }
        if (entity instanceof Cow) {
            gui.addItem(22, new GUIItem(Material.COW_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定牛的溫度變種")),
                    "§f更改外觀", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 22)) return;
                openTemperatureVariant(player, ((Animals) entity));
            }));
        }
        if (entity instanceof Pig) {
            gui.addItem(22, new GUIItem(Material.PIG_SPAWN_EGG, new ArrayList<>(Arrays.asList("§b▶ 設定豬的溫度變種")),
                    "§f更改外觀", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, 22)) return;
                openTemperatureVariant(player, ((Animals) entity));
            }));
        }

        // Armor Options
        addArmorItem(player, entity, gui, 28 + (special ? 9 : 0), 0, "正手");
        addArmorItem(player, entity, gui, 29 + (special ? 9 : 0), 1, "副手");
        addArmorItem(player, entity, gui, 31 + (special ? 9 : 0), 2, "頭盔");
        addArmorItem(player, entity, gui, 32 + (special ? 9 : 0), 3, "胸甲");
        addArmorItem(player, entity, gui, 33 + (special ? 9 : 0), 4, "護腿");
        addArmorItem(player, entity, gui, 34 + (special ? 9 : 0), 5, "鞋子");
        int last_row_base = 9 * (special ? 5 : 4);
        if (!(entity instanceof ArmorStand)) {
            gui.addItem(last_row_base + 5, new GUIItem(Material.MAGENTA_GLAZED_TERRACOTTA, Arrays.asList("§b▶ 使實體上下顛倒", "§e☉ 並非所有生物都適用"),
                    "§f上下顛倒", (e, i) -> {
                e.setCancelled(true);
                if (putOnEquipment(player, last_row_base + 5)) return;
                e.getWhoClicked().closeInventory();
                if (entity.getCustomName() != null && entity.getCustomName().equals("Grumm"))
                    entity.setCustomName(null);
                else entity.setCustomName("Grumm");
                entity.setCustomNameVisible(false);
            }));
        }
        gui.addItem(last_row_base + 6, new GUIItem(Material.SPAWNER, null,
                "§f複製", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, last_row_base + 6)) return;
            e.getWhoClicked().closeInventory();
            ((Player) e.getWhoClicked()).sendTitle("", "§6滑鼠滾輪調整高度/Shift以確認放置位置", 0, Integer.MAX_VALUE, 0);
            LivingEntity new_entity = ((LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType()));
            cloneNBT(entity, new_entity);
            movingTasks.put(e.getWhoClicked().getName(), new MovingTask(new_entity));
        }));
        gui.addItem(last_row_base + 7, new GUIItem(Material.LEAD, null,
                "§f移動實體", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, last_row_base + 7)) return;
            e.getWhoClicked().closeInventory();
            ((Player) e.getWhoClicked()).sendTitle("", "§6滑鼠滾輪調整高度/Shift以確認放置位置", 0, Integer.MAX_VALUE, 0);
            movingTasks.put(e.getWhoClicked().getName(), new MovingTask(entity));
        }));
        gui.addItem(last_row_base + 8, new GUIItem(Material.BARRIER, null,
                "§c刪除實體", (e, i) -> {
            e.setCancelled(true);
            if (putOnEquipment(player, last_row_base + 8)) return;
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
            armorTasks.put(player.getName(), new ArmorTask(position, entity, gui, gui_slot));
        }).hideEverything();
        gui.addItem(gui_slot, armor);
    }

    public static void openItemFrame(Player player, ItemFrame frame) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 3, player);
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

    private static void openVillagerProfession(Player player, Villager villager) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 3, player);
        for (int i = 0; i < 27; i++) {
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        }
        ArrayList<Villager.Profession> profs = new ArrayList<>(Arrays.asList(Villager.Profession.NONE, Villager.Profession.NITWIT, Villager.Profession.ARMORER,
                Villager.Profession.BUTCHER, Villager.Profession.CARTOGRAPHER, Villager.Profession.CLERIC, Villager.Profession.FARMER, Villager.Profession.FISHERMAN,
                Villager.Profession.FLETCHER, Villager.Profession.LEATHERWORKER, Villager.Profession.LIBRARIAN, Villager.Profession.MASON, Villager.Profession.SHEPHERD,
                Villager.Profession.TOOLSMITH, Villager.Profession.WEAPONSMITH));
        ArrayList<String> names = new ArrayList<>(Arrays.asList("預設", "傻子", "武器匠", "屠夫", "製圖師", "神職人員", "農夫",
                "漁夫", "製箭師", "皮匠", "圖書管理員", "石匠", "牧羊人", "工具匠", "武器匠"));
        ArrayList<Material> mats = new ArrayList<>(Arrays.asList(Material.VILLAGER_SPAWN_EGG, Material.BARRIER, Material.BLAST_FURNACE,
                Material.SMOKER, Material.CARTOGRAPHY_TABLE, Material.BREWING_STAND, Material.COMPOSTER, Material.BARREL, Material.FLETCHING_TABLE,
                Material.CAULDRON, Material.LECTERN, Material.STONECUTTER, Material.LOOM, Material.SMITHING_TABLE, Material.GRINDSTONE));

        for (int i = 0; i < profs.size(); i++) {
            final int index = i;
            gui.addItem(i, new GUIItem(mats.get(i), null,
                    "§f" + names.get(i), (e, item) -> {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                villager.setProfession(profs.get(index));
            }));
        }
        player.openInventory(gui.getInv());
    }

    private static void openVillagerType(Player player, Villager villager) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 3, player);
        for (int i = 0; i < 27; i++) {
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        }
        ArrayList<Villager.Type> types = new ArrayList<>(Arrays.asList(Villager.Type.PLAINS,
                Villager.Type.SAVANNA, Villager.Type.DESERT, Villager.Type.TAIGA,
                Villager.Type.JUNGLE, Villager.Type.SNOW, Villager.Type.SWAMP));
        ArrayList<String> names = new ArrayList<>(Arrays.asList("平原", "莽原", "沙漠", "針葉林", "叢林", "雪原", "沼澤"));
        ArrayList<Material> mats = new ArrayList<>(Arrays.asList(Material.GRASS_BLOCK, Material.ACACIA_LOG, Material.SAND,
                Material.SPRUCE_LOG, Material.JUNGLE_LOG, Material.SNOW_BLOCK, Material.MUD));

        for (int i = 0; i < types.size(); i++) {
            final int index = i;
            gui.addItem(i, new GUIItem(mats.get(i), null,
                    "§f" + names.get(i), (e, item) -> {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                villager.setVillagerType(types.get(index));
            }));
        }
        player.openInventory(gui.getInv());
    }

    private static void openShulkerColor(Player player, Shulker shulker) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 3, player);
        for (int i = 0; i < 27; i++) {
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        }
        ArrayList<DyeColor> colors = new ArrayList<>(Arrays.asList(null,
                DyeColor.WHITE, DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE,
                DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.GRAY,
                DyeColor.LIGHT_GRAY, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE,
                DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED, DyeColor.BLACK));
        ArrayList<String> names = new ArrayList<>(Arrays.asList("預設",
                "白色", "橘色", "洋紅色", "淺藍色",
                "黃色", "淺綠色", "粉紅色", "灰色",
                "淺灰色", "青色", "紫色", "藍色",
                "棕色", "綠色", "紅色", "黑色"));
        ArrayList<Material> mats = new ArrayList<>(Arrays.asList(Material.SHULKER_SPAWN_EGG,
                Material.WHITE_DYE, Material.ORANGE_DYE, Material.MAGENTA_DYE, Material.LIGHT_BLUE_DYE,
                Material.YELLOW_DYE, Material.LIME_DYE, Material.PINK_DYE, Material.GRAY_DYE,
                Material.LIGHT_GRAY_DYE, Material.CYAN_DYE, Material.PURPLE_DYE, Material.BLUE_DYE,
                Material.BROWN_DYE, Material.GREEN_DYE, Material.RED_DYE, Material.BLACK_DYE));

        for (int i = 0; i < colors.size(); i++) {
            final int index = i;
            gui.addItem(i, new GUIItem(mats.get(i), null,
                    "§f" + names.get(i), (e, item) -> {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                shulker.setColor(colors.get(index));
            }));
        }
        player.openInventory(gui.getInv());
    }

    private static void openSlimeSize(Player player, Slime slime) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 1, player);
        ArrayList<Integer> ofs = new ArrayList<>(Arrays.asList(-10, -5, -1, 0, 0, 0, +1, +5, +10));
        ArrayList<Material> mats = new ArrayList<>(Arrays.asList(Material.IRON_BLOCK, Material.IRON_INGOT, Material.IRON_NUGGET, Material.GRAY_STAINED_GLASS_PANE,
                slime instanceof MagmaCube ? Material.MAGMA_CUBE_SPAWN_EGG : Material.SLIME_SPAWN_EGG, Material.GRAY_STAINED_GLASS_PANE,
                Material.GOLD_NUGGET, Material.GOLD_INGOT, Material.GOLD_BLOCK));
        ArrayList<String> names = new ArrayList<>(Arrays.asList("-10", "-5", "-1", "§r", "當前大小: " + slime.getSize(), "§r", "+1", "+5", "+10"));

        for (int i = 0; i < ofs.size(); i++) {
            final int index = i;
            gui.addItem(i, new GUIItem(mats.get(i), null,
                    "§f" + names.get(i), (e, item) -> {
                e.setCancelled(true);
                if (ofs.get(index) != 0) {
                    slime.setSize(Math.max(1, Math.min(slime.getSize() + ofs.get(index), 16)));
                    openSlimeSize(player, slime);
                }
            }));
        }
        player.openInventory(gui.getInv());
    }

    private static void openShulkerPeek(Player player, Shulker shulker) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 1, player);
        ArrayList<Float> ofs = new ArrayList<>(Arrays.asList(-0.5F, -0.1F, -0.05F, 0F, 0F, 0F, +0.05F, +0.1F, +0.5F));
        ArrayList<Material> mats = new ArrayList<>(Arrays.asList(Material.IRON_BLOCK, Material.IRON_INGOT, Material.IRON_NUGGET, Material.GRAY_STAINED_GLASS_PANE,
                Material.SHULKER_SPAWN_EGG, Material.GRAY_STAINED_GLASS_PANE,
                Material.GOLD_NUGGET, Material.GOLD_INGOT, Material.GOLD_BLOCK));
        ArrayList<String> names = new ArrayList<>(Arrays.asList("-0.5", "-0.1", "-0.05", "§r", String.format("當前開啟程度: %1.2f", shulker.getPeek()), "§r", "+0.05", "+0.1", "+0.5"));
        for (int i = 0; i < ofs.size(); i++) {
            final int index = i;
            gui.addItem(i, new GUIItem(mats.get(i), null,
                    "§f" + names.get(i), (e, item) -> {
                e.setCancelled(true);
                if (ofs.get(index) != 0) {
                    shulker.setPeek(Math.max(0, Math.min(shulker.getPeek() + ofs.get(index), 1)));
                    openShulkerPeek(player, shulker);
                }
            }));
        }
        player.openInventory(gui.getInv());
    }

    private static void openWolfType(Player player, Wolf wolf) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 1, player);
        ArrayList<Wolf.Variant> types = new ArrayList<>(Arrays.asList(Wolf.Variant.PALE,
                Wolf.Variant.ASHEN, Wolf.Variant.BLACK, Wolf.Variant.CHESTNUT, Wolf.Variant.RUSTY,
                Wolf.Variant.SNOWY, Wolf.Variant.SPOTTED, Wolf.Variant.STRIPED, Wolf.Variant.WOODS));
        ArrayList<String> names = new ArrayList<>(Arrays.asList("蒼狼(預設)", "灰狼", "黑狼", "栗色狼", "赭紅狼", "雪狼", "斑點狼", "條紋狼", "森林狼"));
        ArrayList<Material> mats = new ArrayList<>(Arrays.asList(Material.SPRUCE_LOG,
                Material.SNOW, Material.MOSSY_COBBLESTONE, Material.PODZOL, Material.JUNGLE_LOG,
                Material.SNOW_BLOCK, Material.ACACIA_LOG, Material.TERRACOTTA, Material.BIRCH_LOG));

        for (int i = 0; i < types.size(); i++) {
            final int index = i;
            gui.addItem(i, new GUIItem(mats.get(i), null,
                    "§f" + names.get(i), (e, item) -> {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                wolf.setVariant(types.get(index));
            }));
        }
        player.openInventory(gui.getInv());
    }

    // for chickens, cows, and pigs
    private static void openTemperatureVariant(Player player, Animals animal) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 1, player);
        for (int i = 0; i < 9; i++) {
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        }
        gui.addItem(3, new GUIItem(Material.RED_SAND, null,
                "§f熱帶氣候", (e, item) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            if (animal instanceof Chicken) ((Chicken) animal).setVariant(Chicken.Variant.WARM);
            else if (animal instanceof Cow) ((Cow) animal).setVariant(Cow.Variant.WARM);
            else if (animal instanceof Pig) ((Pig) animal).setVariant(Pig.Variant.WARM);
        }));
        gui.addItem(4, new GUIItem(Material.GRASS_BLOCK, null,
                "§f預設", (e, item) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            if (animal instanceof Chicken) ((Chicken) animal).setVariant(Chicken.Variant.TEMPERATE);
            else if (animal instanceof Cow) ((Cow) animal).setVariant(Cow.Variant.TEMPERATE);
            else if (animal instanceof Pig) ((Pig) animal).setVariant(Pig.Variant.TEMPERATE);
        }));
        gui.addItem(5, new GUIItem(Material.ICE, null,
                "§f寒帶氣候", (e, item) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            if (animal instanceof Chicken) ((Chicken) animal).setVariant(Chicken.Variant.COLD);
            else if (animal instanceof Cow) ((Cow) animal).setVariant(Cow.Variant.COLD);
            else if (animal instanceof Pig) ((Pig) animal).setVariant(Pig.Variant.COLD);
        }));
        player.openInventory(gui.getInv());
    }

    private static void openArmorStandPose(Player player, ArmorStand as) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 5, player);
        for (int i = 0; i < 45; i++) {
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        }
        gui.addItem(13, new GUIItem(Material.PLAYER_HEAD, new ArrayList<>(Arrays.asList("§b▶ 左鍵以調整角度", "§b▶ 右鍵重置姿勢")),
                "§f更改頭部動作", (e, item) -> {
            e.setCancelled(true);
            if (e.getClick().isLeftClick()) {
                e.getWhoClicked().closeInventory();
                ((Player) e.getWhoClicked()).sendTitle("", "§6擺動視角調整角度/Shift確認放置位置", 0, Integer.MAX_VALUE, 0);
                movingTasks.put(player.getName(), new MovingTask(as, MovingTask.MoveType.HEAD, player.getLocation()));
            } else if (e.getClick().isRightClick()) {
                as.setHeadPose(new EulerAngle(0, 0, 0));
            }
        }));
        gui.addItem(21, new GUIItem(Material.DIAMOND_SWORD, new ArrayList<>(Arrays.asList("§b▶ 左鍵以調整角度", "§b▶ 右鍵重置姿勢", "§c☉ 盔甲架的右手相當於玩家正對時的左方")),
                "§f更改右手動作", (e, item) -> {
            e.setCancelled(true);
            if (e.getClick().isLeftClick()) {
                e.getWhoClicked().closeInventory();
                ((Player) e.getWhoClicked()).sendTitle("", "§6擺動視角調整角度/Shift確認放置位置", 0, Integer.MAX_VALUE, 0);
                movingTasks.put(player.getName(), new MovingTask(as, MovingTask.MoveType.RIGHT_HAND, player.getLocation()));
            } else if (e.getClick().isRightClick()) {
                as.setRightArmPose(new EulerAngle(0, 0, 0));
            }
        }));
        gui.addItem(22, new GUIItem(Material.DIAMOND_CHESTPLATE, new ArrayList<>(Arrays.asList("§b▶ 左鍵以調整角度", "§b▶ 右鍵重置姿勢")),
                "§f更改身體動作", (e, item) -> {
            e.setCancelled(true);
            if (e.getClick().isLeftClick()) {
                e.getWhoClicked().closeInventory();
                ((Player) e.getWhoClicked()).sendTitle("", "§6擺動視角調整角度/Shift確認放置位置", 0, Integer.MAX_VALUE, 0);
                movingTasks.put(player.getName(), new MovingTask(as, MovingTask.MoveType.BODY, player.getLocation()));
            } else if (e.getClick().isRightClick()) {
                as.setBodyPose(new EulerAngle(0, 0, 0));
            }
        }));
        gui.addItem(23, new GUIItem(Material.SHIELD, new ArrayList<>(Arrays.asList("§b▶ 左鍵以調整角度", "§b▶ 右鍵重置姿勢", "§c☉ 盔甲架的左手相當於玩家正對時的右方")),
                "§f更改左手動作", (e, item) -> {
            e.setCancelled(true);
            if (e.getClick().isLeftClick()) {
                e.getWhoClicked().closeInventory();
                ((Player) e.getWhoClicked()).sendTitle("", "§6擺動視角調整角度/Shift確認放置位置", 0, Integer.MAX_VALUE, 0);
                movingTasks.put(player.getName(), new MovingTask(as, MovingTask.MoveType.LEFT_HAND, player.getLocation()));
            } else if (e.getClick().isRightClick()) {
                as.setLeftArmPose(new EulerAngle(0, 0, 0));
            }
        }));
        gui.addItem(30, new GUIItem(Material.DIAMOND_BOOTS, new ArrayList<>(Arrays.asList("§b▶ 左鍵以調整角度", "§b▶ 右鍵重置姿勢", "§c☉ 盔甲架的右腳相當於玩家正對時的左方")),
                "§f更改右腳動作", (e, item) -> {
            e.setCancelled(true);
            if (e.getClick().isLeftClick()) {
                e.getWhoClicked().closeInventory();
                ((Player) e.getWhoClicked()).sendTitle("", "§6擺動視角調整角度/Shift確認放置位置", 0, Integer.MAX_VALUE, 0);
                movingTasks.put(player.getName(), new MovingTask(as, MovingTask.MoveType.RIGHT_FEET, player.getLocation()));
            } else if (e.getClick().isRightClick()) {
                as.setRightLegPose(new EulerAngle(0, 0, 0));
            }
        }));
        gui.addItem(32, new GUIItem(Material.DIAMOND_BOOTS, new ArrayList<>(Arrays.asList("§b▶ 左鍵以調整角度", "§b▶ 右鍵重置姿勢", "§c☉ 盔甲架的左腳相當於玩家正對時的右方")),
                "§f更改左腳動作", (e, item) -> {
            e.setCancelled(true);
            if (e.getClick().isLeftClick()) {
                e.getWhoClicked().closeInventory();
                ((Player) e.getWhoClicked()).sendTitle("", "§6擺動視角調整角度/Shift確認放置位置", 0, Integer.MAX_VALUE, 0);
                movingTasks.put(player.getName(), new MovingTask(as, MovingTask.MoveType.LEFT_FEET, player.getLocation()));
            } else if (e.getClick().isRightClick()) {
                as.setLeftLegPose(new EulerAngle(0, 0, 0));
            }
        }));
        player.openInventory(gui.getInv());
    }

    private static void cloneNBT(Entity old_entity, Entity new_entity) {
        RtagEntity tag_old = new RtagEntity(old_entity);
        RtagEntity tag_new = new RtagEntity(new_entity);
        tag_new.merge(tag_old.get(), true);
        tag_new.load();
    }
}
