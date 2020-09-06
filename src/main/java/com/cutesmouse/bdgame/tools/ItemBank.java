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
        meta.setDisplayName("§b建築大賽遊戲選單");
        meta.setLore(new ArrayList<>(Arrays.asList("§a左鍵.. 還是右鍵?","§a都能開唷! <3")));
        MENU.setItemMeta(meta);
    }
    public static final ItemStack NEXT_ITEM;
    static {
        NEXT_ITEM = new ItemStack(Material.PAPER);
        ItemMeta meta = NEXT_ITEM.getItemMeta();
        meta.setDisplayName("§b下一頁");
        NEXT_ITEM.setItemMeta(meta);
    }
    public static final ItemStack SETPLOT;
    static {
        SETPLOT = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = SETPLOT.getItemMeta();
        meta.setDisplayName("§b場景工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§a手持此物品對著要設定為地板的方塊點右鍵!","§cDEV")));
        SETPLOT.setItemMeta(meta);
    }
    public static final ItemStack RESET;
    static {
        RESET = new ItemStack(Material.RED_DYE);
        ItemMeta meta = RESET.getItemMeta();
        meta.setDisplayName("§b重製工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§a直接清除整張地圖","§cDEV")));
        RESET.setItemMeta(meta);
    }
    public static final ItemStack UNDO;
    static {
        UNDO = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta meta = UNDO.getItemMeta();
        meta.setDisplayName("§b復原工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§a回朔上個執行的動作","§cDEV")));
        UNDO.setItemMeta(meta);
    }
    public static final ItemStack SELECT;
    static {
        SELECT = new ItemStack(Material.STICK);
        ItemMeta meta = SELECT.getItemMeta();
        meta.setDisplayName("§b選取工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§a使用左右鍵點擊該方塊來選取區域","§cDEV")));
        SELECT.setItemMeta(meta);
    }
    public static final ItemStack SET;
    static {
        SET = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = SET.getItemMeta();
        meta.setDisplayName("§b填滿工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§a點擊一個方塊來將選取區域內的方塊填滿","§e須配合 §b選取工具","§cDEV")));
        SET.setItemMeta(meta);
    }
    public static final ItemStack COPY;
    static {
        COPY = new ItemStack(Material.BUCKET);
        ItemMeta meta = COPY.getItemMeta();
        meta.setDisplayName("§b複製工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§a站在某個位置來複製選取區內的方塊","§a左鍵點選將會以玩家位置為複製參考點",
                "§a右鍵方塊將以該方塊位置為參考點","§e須配合 §b選取工具","§cDEV")));
        COPY.setItemMeta(meta);
    }
    public static final ItemStack PASTE;
    static {
        PASTE = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = PASTE.getItemMeta();
        meta.setDisplayName("§b黏貼工具");
        meta.setLore(new ArrayList<>(Arrays.asList("§a複製黏貼版的內容","§a左鍵點選將會以玩家位置為黏貼參考點",
                "§a右鍵方塊將以該方塊位置為參考點","§e須配合 §b複製工具","§cDEV")));
        PASTE.setItemMeta(meta);
    }
}
