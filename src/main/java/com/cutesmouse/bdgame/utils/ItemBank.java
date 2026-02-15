package com.cutesmouse.bdgame.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemBank {
    public static final ItemStack MENU;
    static {
        MENU = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = MENU.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§f遊戲選單");
        MENU.setItemMeta(meta);
    }
    public static final ItemStack NEXT_ITEM;
    static {
        NEXT_ITEM = new ItemStack(Material.PAPER);
        ItemMeta meta = NEXT_ITEM.getItemMeta();
        meta.setDisplayName("§f下一頁");
        NEXT_ITEM.setItemMeta(meta);
    }
    public static final ItemStack RANK_LEVEL_4;
    static {
        RANK_LEVEL_4 = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = RANK_LEVEL_4.getItemMeta();
        meta.setDisplayName("§f夯");
        RANK_LEVEL_4.setItemMeta(meta);
    }
    public static final ItemStack RANK_LEVEL_3;
    static {
        RANK_LEVEL_3 = new ItemStack(Material.ORANGE_WOOL);
        ItemMeta meta = RANK_LEVEL_3.getItemMeta();
        meta.setDisplayName("§f頂級");
        RANK_LEVEL_3.setItemMeta(meta);
    }
    public static final ItemStack RANK_LEVEL_2;
    static {
        RANK_LEVEL_2 = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta meta = RANK_LEVEL_2.getItemMeta();
        meta.setDisplayName("§f人上人");
        RANK_LEVEL_2.setItemMeta(meta);
    }
    public static final ItemStack RANK_LEVEL_1;
    static {
        RANK_LEVEL_1 = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta meta = RANK_LEVEL_1.getItemMeta();
        meta.setDisplayName("§fNPC");
        RANK_LEVEL_1.setItemMeta(meta);
    }
    public static final ItemStack RANK_LEVEL_0;
    static {
        RANK_LEVEL_0 = new ItemStack(Material.WHITE_WOOL);
        ItemMeta meta = RANK_LEVEL_0.getItemMeta();
        meta.setDisplayName("§f拉完了");
        RANK_LEVEL_0.setItemMeta(meta);
    }
    public static final ItemStack SETPLOT;
    static {
        SETPLOT = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = SETPLOT.getItemMeta();
        meta.setDisplayName("§f場景工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 手持此物品對著要設定為地板的方塊點右鍵!")));
        SETPLOT.setItemMeta(meta);
    }
    public static final ItemStack RESET;
    static {
        RESET = new ItemStack(Material.RED_DYE);
        ItemMeta meta = RESET.getItemMeta();
        meta.setDisplayName("§f重製工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 直接清除整張地圖")));
        RESET.setItemMeta(meta);
    }
    public static final ItemStack UNDO;
    static {
        UNDO = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta meta = UNDO.getItemMeta();
        meta.setDisplayName("§f復原工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 回朔上個執行的動作")));
        UNDO.setItemMeta(meta);
    }
    public static final ItemStack SELECT;
    static {
        SELECT = new ItemStack(Material.STICK);
        ItemMeta meta = SELECT.getItemMeta();
        meta.setDisplayName("§f選取工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 使用左右鍵點擊該方塊來選取區域")));
        SELECT.setItemMeta(meta);
    }
    public static final ItemStack SET;
    static {
        SET = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = SET.getItemMeta();
        meta.setDisplayName("§f填滿工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 點擊一個方塊來將選取區域內的方塊填滿","§c☉ 須配合 §f選取工具")));
        SET.setItemMeta(meta);
    }
    public static final ItemStack COPY;
    static {
        COPY = new ItemStack(Material.BUCKET);
        ItemMeta meta = COPY.getItemMeta();
        meta.setDisplayName("§f複製工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 站在某個位置來複製選取區內的方塊","§b▶ 以玩家位置為複製參考點", "§c☉ 須配合 §f選取工具")));
        COPY.setItemMeta(meta);
    }
    public static final ItemStack PASTE;
    static {
        PASTE = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = PASTE.getItemMeta();
        meta.setDisplayName("§f黏貼工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 複製黏貼版的內容","§b▶ 以玩家位置為黏貼參考點", "§c☉ 須配合 §f複製工具")));
        PASTE.setItemMeta(meta);
    }
    public static final ItemStack PARTICLE;
    static {
        PARTICLE = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta meta = PARTICLE.getItemMeta();
        meta.setDisplayName("§f粒子效果工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§b▶ 在建築中放置喜歡的粒子效果", "§b▶ 左鍵進入設定介面", "§b▶ 右鍵放置粒子效果")));
        PARTICLE.setItemMeta(meta);
    }
}
