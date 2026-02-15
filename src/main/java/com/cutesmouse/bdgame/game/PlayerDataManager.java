package com.cutesmouse.bdgame.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PlayerDataManager {
    private static final HashMap<String,PlayerData> PLD = new HashMap<>();
    public static PlayerData getPlayerData(Player player) {
        PlayerData data = null;
        if (player == null) return null;
        if (PLD.containsKey(player.getName())) {
            data = PLD.get(player.getName());
        } else {
            data = new PlayerData(player);
            PLD.put(player.getName(),data);
        }
        return data;
    }
    public static PlayerData getPlayerData(String name) {
        return getPlayerData(Bukkit.getPlayer(name));
    }
    public static Set<PlayerData> getPlayers() {
        return new HashSet<>(PLD.values());
    }
}
