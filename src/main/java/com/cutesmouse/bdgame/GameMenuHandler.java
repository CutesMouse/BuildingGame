package com.cutesmouse.bdgame;

import com.cutesmouse.bdgame.tools.ItemBank;
import com.cutesmouse.mgui.ChatQueueData;
import com.cutesmouse.mgui.GUI;
import com.cutesmouse.mgui.GUIItem;
import com.cutesmouse.mgui.ListenerHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GameMenuHandler {
    public static void open(Player p) {
        PlayerData data = PlayerDataManager.getPlayerData(p);
        GUI gui = new GUI("§d◤§b§l建築大賽 Building Game§d◢ §1遊戲選單",5,p);
        for (int i = 0 ; i < 45 ; i++) gui.addItem(i,new GUIItem(Material.GRAY_STAINED_GLASS_PANE,null,"§r",GUI.blank()));
        int stage = Main.BDGAME.getStage();
        if (stage == 0) {
            gui.addItem(22,new GUIItem(Material.BARRIER,null,"§c遊戲尚未開始!",GUI.blank()));
        } else if (stage == 1) {
            /*if (data.currentRoom().data.origin == null) gui.addItem(22,new GUIItem(Material.OAK_SIGN,new ArrayList<>(Arrays.asList("§a出一個題目!")),"§e設定題目",
                    (e,i) -> {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().sendMessage("§6請輸入題目:");
                ListenerHandler.ChattedQueue.add(new ChatQueueData(((Player) e.getWhoClicked()),(pa,id) -> checkInsert(p,id,data)));
                    }));*/
            if (data.currentRoom().data.origin == null) {
                gui.addItem(22,new GUIItem(Material.OAK_SIGN,new ArrayList<>(Arrays.asList("§a出一個題目!")),"§e設定題目",
                        GUI.signText("",(h) -> checkInsert(p,combine(h.getLines()),data))));
                gui.addItem(44,new GUIItem(Material.BARRIER,null,"§d出現問題嗎? 點我使用原方法設定",(e,i) -> {
                    e.setCancelled(true);
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage("§6請輸入題目:");
                    ListenerHandler.ChattedQueue.add(new ChatQueueData(((Player) e.getWhoClicked()),(pa,id) -> checkInsert(p,id,data)));
                }));
            }
            else gui.addItem(22,new GUIItem(Material.LIGHT_BLUE_WOOL, Collections.singletonList("§a您的題目為 §e"+data.currentRoom().data.origin),
                    "§e出題完成",GUI.blank()));
        } else if (stage % 2 == 0) {
            if (data.isDone()) {
                gui.addItem(44,new GUIItem(Material.LIGHT_BLUE_WOOL,Collections.singletonList("§a您已完成建築!"),"§e建築完成",GUI.blank()));
            } else {
                gui.addItem(44,new GUIItem(Material.GOLD_BLOCK,new ArrayList<>(Arrays.asList("§a標示後您仍可繼續建築","§a但會在其他人完成後立即傳送")),
                        "§e標示為已完成",(e,i) -> {
                    e.setCancelled(true);
                    checkOK(p,data);
                }));
            }
            gui.addItem(19,new GUIItem(ItemBank.SELECT,(e,i) -> {}));
            gui.addItem(20,new GUIItem(ItemBank.SET,(e,i) -> {}));
            gui.addItem(21,new GUIItem(ItemBank.SETPLOT,(e,i) -> {}));
            gui.addItem(22,new GUIItem(ItemBank.UNDO,(e,i) -> {}));
            gui.addItem(23,new GUIItem(ItemBank.RESET,(e,i) -> {}));
            gui.addItem(24,new GUIItem(ItemBank.COPY,(e,i) -> {}));
            gui.addItem(25,new GUIItem(ItemBank.PASTE,(e,i) -> {}));
        } else {
            if (data.currentRoom().data.guess == null) {
                gui.addItem(22,new GUIItem(Material.OAK_SIGN,new ArrayList<>(Arrays.asList("§a你覺得這東西像什麼...")),"§e進行猜測",
                        GUI.signText("",h -> checkGuess(p,combine(h.getLines()),data))));
                gui.addItem(44,new GUIItem(Material.BARRIER,null,"§d出現問題嗎? 點我使用原方法設定",(e,i) -> {
                    e.setCancelled(true);
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage("§6請輸入題目:");
                    ListenerHandler.ChattedQueue.add(new ChatQueueData(((Player) e.getWhoClicked()),(pa,id) -> checkGuess(p,id,data)));
                }));
            }
            else gui.addItem(22,new GUIItem(Material.LIGHT_BLUE_WOOL, Collections.singletonList("§a您的猜測為 §e"+data.currentRoom().data.guess),
                    "§e猜測完成",GUI.blank()));
        }
        p.openInventory(gui.getInv());
    }
    private static void checkInsert(Player p, String output, PlayerData data) {
        GUI gui = new GUI("§d◤§b§l建築大賽 Building Game§d◢ §1遊戲選單",5,p);
        for (int i = 0 ; i < 45 ; i++) gui.addItem(i,new GUIItem(Material.GRAY_STAINED_GLASS_PANE,null,"§r",GUI.blank()));
        gui.addItem(13,new GUIItem(Material.OAK_SIGN,new ArrayList<>(Arrays.asList("§a您目前設定: §6"+output,"§a確認後將無法進行更改!")),
                "§e確認題目",GUI.blank()));
        gui.addItem(29,new GUIItem(Material.GREEN_WOOL,null,"§a是",(e,i) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            data.currentRoom().data.origin = output;
            data.currentRoom().data.originProvider = p.getName();
            data.done();
            e.getWhoClicked().sendMessage("§a您的題目已經設定完成!");
        }));
        gui.addItem(33,new GUIItem(Material.RED_WOOL,null,"§c否",GUI.signText("",h -> checkInsert(h.getPlayer(),combine(h.getLines()),data))));
        gui.addItem(44,new GUIItem(Material.BARRIER,null,"§d出現問題嗎? 點我使用原方法設定",(e,i) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            e.getWhoClicked().sendMessage("§6請輸入題目:");
            ListenerHandler.ChattedQueue.add(new ChatQueueData(((Player) e.getWhoClicked()),(pa,id) -> checkInsert(p,id,data)));
        }));
        new BukkitRunnable() {
            @Override
            public void run() {
                p.openInventory(gui.getInv());
            }
        }.runTaskLater(Main.getPlugin(Main.class),1L);
    }
    private static void checkGuess(Player p, String output, PlayerData data) {
        GUI gui = new GUI("§d◤§b§l建築大賽 Building Game§d◢ §1遊戲選單",5,p);
        for (int i = 0 ; i < 45 ; i++) gui.addItem(i,new GUIItem(Material.GRAY_STAINED_GLASS_PANE,null,"§r",GUI.blank()));
        gui.addItem(13,new GUIItem(Material.OAK_SIGN,new ArrayList<>(Arrays.asList("§a您目前設定: §6"+output,"§a確認後將無法進行更改!")),
                "§e確認猜測",GUI.blank()));
        gui.addItem(29,new GUIItem(Material.GREEN_WOOL,null,"§a是",(e,i) -> {
            System.out.println("輸入Guess -> "+ data.currentRoom().toString()+" / "+ output+ " / " + e.getWhoClicked().getName());
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            data.currentRoom().data.guess = output;
            data.currentRoom().data.guessProvider = p.getName();
            data.done();
            e.getWhoClicked().sendMessage("§a已輸入您的猜測結果!");
        }));
        gui.addItem(33,new GUIItem(Material.RED_WOOL,null,"§c否",GUI.signText("",h -> checkGuess(h.getPlayer(),combine(h.getLines()),data))));
        gui.addItem(44,new GUIItem(Material.BARRIER,null,"§d出現問題嗎? 點我使用原方法設定",(e,i) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            e.getWhoClicked().sendMessage("§6你覺得這東西像什麼?");
            ListenerHandler.ChattedQueue.add(new ChatQueueData(((Player) e.getWhoClicked()),(pa,id) -> checkGuess(p,id,data)));
        }));
        new BukkitRunnable() {
            @Override
            public void run() {
                p.openInventory(gui.getInv());
            }
        }.runTaskLater(Main.getPlugin(Main.class),1L);
    }
    private static String combine(String[] output) {
        assert output.length == 4;
        return output[0] + output[1] + output[2] + output[3];
    }
    private static void checkOK(Player p, PlayerData data) {
        GUI gui = new GUI("§d◤§b§l建築大賽 Building Game§d◢ §1遊戲選單",5,p);
        for (int i = 0 ; i < 45 ; i++) gui.addItem(i,new GUIItem(Material.GRAY_STAINED_GLASS_PANE,null,"§r",GUI.blank()));
        gui.addItem(13,new GUIItem(Material.OAK_SIGN,new ArrayList<>(Arrays.asList("§a確認後將無法進行更改!")),
                "§e準備確認",GUI.blank()));
        gui.addItem(29,new GUIItem(Material.GREEN_WOOL,null,"§a是",(e,i) -> {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            data.done();
            e.getWhoClicked().sendMessage("§a您已完成建築!");
        }));
        gui.addItem(33,new GUIItem(Material.RED_WOOL,null,"§c否",(e,i) -> {
            e.setCancelled(true);
            open(p);
        }));
        p.openInventory(gui.getInv());
    }
}
