package com.cutesmouse.bdgame.tools;

import com.cutesmouse.mgui.GUI;
import com.cutesmouse.mgui.GUIItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SpecialItemBox {
    public static void open(Player player) {
        GUI gui = new GUI("§e● 2026 跨年建築大賽", 3, player);
        for (int i = 0; i < 27; i++)
            gui.addItem(i, new GUIItem(Material.GRAY_STAINED_GLASS_PANE, null, "§r", GUI.blank()));
        gui.addItem(0, new GUIItem(Material.BARRIER, null, null, (e, i) -> {
        }));
        gui.addItem(1, new GUIItem(Material.COMMAND_BLOCK, null, null, (e, i) -> {
        }));
        gui.addItem(2, new GUIItem(Material.COMMAND_BLOCK_MINECART, null, null, (e, i) -> {
        }));
        gui.addItem(3, new GUIItem(Material.CHAIN_COMMAND_BLOCK, null, null, (e, i) -> {
        }));
        gui.addItem(4, new GUIItem(Material.REPEATING_COMMAND_BLOCK, null, null, (e, i) -> {
        }));
        gui.addItem(5, new GUIItem(Material.STRUCTURE_BLOCK, null, null, (e, i) -> {
        }));
        gui.addItem(6, new GUIItem(Material.DEBUG_STICK, null, null, (e, i) -> {
        }));
        player.openInventory(gui.getInv());
    }
}
