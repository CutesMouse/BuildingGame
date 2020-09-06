package com.cutesmouse.bdgame.command;

import com.cutesmouse.bdgame.Main;
import com.cutesmouse.bdgame.PlayerData;
import com.cutesmouse.bdgame.PlayerDataManager;
import com.cutesmouse.bdgame.Room;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R1.ChatComponentText;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Debug implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.isOp()) return true;
        if (args.length != 2) return true;
        switch (args[0]) {
            case "room":
                Room r = Main.BDGAME.getMapManager().getRoom(Integer.parseInt(args[1].split("/")[0]),Integer.parseInt(args[1].split("/")[1]));
                sender.sendMessage("§6====RoomData -> "+args[1]+"====");
                sender.sendMessage("§6位置: §f"+r.loc.getX()+", "+r.loc.getZ());
                sender.sendMessage("§6原始題目: §f"+r.data.origin);
                sender.sendMessage("§6原始出題者: §f"+r.data.originProvider);
                sender.sendMessage("§6猜測: §f"+r.data.guess);
                sender.sendMessage("§6猜測者: §f"+r.data.guessProvider);
                sender.sendMessage("§6建築者: §f"+r.data.builder);
                sender.sendMessage("§6====RoomData -> "+args[1]+"====");
                break;
            case "player":
                PlayerData data = PlayerDataManager.getPlayerData(args[1]);
                sender.sendMessage("§6====PlayerData -> "+args[1]+"====");
                sender.sendMessage("§6isPlaying: §r"+data.isPlaying());
                sender.sendMessage("§6CurrentRoom: §r"+data.currentRoom().row+"/"+data.currentRoom().line);
                sender.sendMessage("§6isDone: §r"+data.isDone());
                sender.sendMessage("§6GuessRoom: §r"+data.getGuessRoom());
                sender.sendMessage("§6ID: §r"+data.getId());
                for (int i = 1 ; i < Main.BDGAME.getMaxStage(); i++) {
                    Room room = data.nextRoom(i);
                    TextComponent tex = new TextComponent("§6階段"+i+(i == 1 ? "(出題)" : (i % 2 == 0 ? "(建築)" : "(猜測)"))+"房間: §r"+ room + (i % 2 == 0 && i > 2 ?
                            "(題目: "+data.getGuessRoom(i)+")" : ""));
                    tex.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new TextComponent[]{new TextComponent("§f顯示更多資訊")}));
                    tex.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/debug room "+room.toString()));
                    sender.spigot().sendMessage(tex);
                }
                break;
        }
        return true;
    }
}
