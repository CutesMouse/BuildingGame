package com.cutesmouse.bdgame.tools;

import org.bukkit.Material;
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
}
