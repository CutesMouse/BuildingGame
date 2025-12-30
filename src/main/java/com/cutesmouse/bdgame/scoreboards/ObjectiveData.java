package com.cutesmouse.bdgame.scoreboards;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.HashMap;

public class ObjectiveData {
    private HashMap<Integer,ScoreData> DATAS;
    private static HashMap<Player, SentData> LastDatas;
    static {
        LastDatas = new HashMap<>();
    }
    public ObjectiveData() {
        DATAS = new HashMap<>();
    }
    public void set(int score, ScoreData data) {
        DATAS.put(score,data);
    }
    public void apply(Objective obj, Player p) {
        SentData sd = new SentData();
        DATAS.forEach((score,data) -> {
            String tobePut = data.apply(p);
            if (LastDatas.containsKey(p)) {
                SentData sentData = LastDatas.get(p);
                sentData.map.forEach((s,i) -> {
                    if (s.equals(score) && !i.equals(tobePut)) obj.getScoreboard().resetScores(i);
                });
            }
            Score scored = obj.getScore(data.apply(p));
            scored.setScore(score);
            sd.append(score,tobePut);
        });
        LastDatas.put(p,sd);
    }
    private static class SentData {
        private HashMap<Integer,String> map;
        private SentData() {
            map = new HashMap<>();
        }
        private void append(int score, String item) {
            map.put(score,item);
        }
    }
}
