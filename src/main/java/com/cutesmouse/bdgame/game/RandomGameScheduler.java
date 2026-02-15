package com.cutesmouse.bdgame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomGameScheduler extends GameScheduler {

    private int[][] scheduleTable;
    private Random rand = new Random(); // 將 Random 提升為成員變數

    public RandomGameScheduler(int players, int max_stages) {
        super(players, max_stages);
        scheduleTable = new int[players][max_stages];
        generateRandomSchedule();
    }

    /**
     * 核心演算法：生成隨機拉丁方陣路徑，並包含邊界衝突檢查
     */
    protected void generateRandomSchedule() {
        int stagesProcessed = 0;

        // 用來記錄上一個區塊「最後一輪」每個人畫的房間
        // 初始化為 -1 (第一輪前面沒有其他輪)
        int[] lastRoundRooms = new int[players];
        for (int i = 0; i < players; i++) lastRoundRooms[i] = -1;

        while (stagesProcessed < max_stages) {
            int stagesInBlock = Math.min(players, max_stages - stagesProcessed);

            int[][] validBlock = null;

            // --- 衝突檢查與重骰迴圈 ---
            // 不斷嘗試生成區塊，直到找到一個與上一輪不衝突的
            while (validBlock == null) {
                // 1. 生成一個候選區塊
                int[][] candidate = generateCandidateBlock(stagesInBlock);

                // 2. 檢查邊界衝突
                // 如果是第一個區塊 (stagesProcessed == 0)，或者通過邊界檢查，則視為有效
                if (stagesProcessed == 0 || isValidBoundary(lastRoundRooms, candidate)) {
                    validBlock = candidate;
                }
                // 如果衝突 (isValidBoundary 回傳 false)，validBlock 仍為 null，while 會繼續重骰
            }

            // 3. 將合法的區塊填入總表 scheduleTable
            for (int s = 0; s < stagesInBlock; s++) {
                int currentStageIndex = stagesProcessed + s; // 0 ~ max_stages-1

                for (int p = 0; p < players; p++) {
                    int room = validBlock[p][s];
                    scheduleTable[p][currentStageIndex] = room;

                    // 如果這是這個區塊的最後一輪，更新 lastRoundRooms 供下一次檢查用
                    if (s == stagesInBlock - 1) {
                        lastRoundRooms[p] = room;
                    }
                }
            }
            stagesProcessed += stagesInBlock;
        }
    }

    /**
     * 輔助方法：生成單一個候選的隨機拉丁方陣區塊
     */
    private int[][] generateCandidateBlock(int stagesInBlock) {
        int[][] block = new int[players][stagesInBlock];

        // 1. 準備基礎序列 0 ~ players-1
        List<Integer> baseList = new ArrayList<>();
        for (int i = 0; i < players; i++) {
            baseList.add(i);
        }

        // 2. 產生三個獨立的隨機排列 (Triple Shuffle)
        List<Integer> pi_R = new ArrayList<>(baseList); // Row permutation
        Collections.shuffle(pi_R, rand);

        List<Integer> pi_C = new ArrayList<>(baseList); // Column shift permutation
        Collections.shuffle(pi_C, rand);

        List<Integer> pi_S = new ArrayList<>(baseList); // Symbol permutation
        Collections.shuffle(pi_S, rand);

        // 3. 計算區塊內容
        for (int s = 0; s < stagesInBlock; s++) {
            // 取得這一輪的隨機偏移量
            int shift = pi_C.get(s);

            for (int p = 0; p < players; p++) {
                // 取得該玩家分配到的隨機角色
                int role = pi_R.get(p);

                // 核心算式：(角色 + 偏移量) % players
                int logicRoomIndex = (role + shift) % players;

                // 將邏輯位置映射到實際的隨機房間 (轉為 1-based)
                int actualRoom = pi_S.get(logicRoomIndex) + 1;

                block[p][s] = actualRoom;
            }
        }
        return block;
    }

    /**
     * 輔助方法：檢查邊界是否衝突
     * @param lastRoundRooms 上一個區塊最後一輪的配置
     * @param candidateBlock 現在這個區塊的配置
     * @return true 表示合法(沒衝突), false 表示有衝突
     */
    private boolean isValidBoundary(int[] lastRoundRooms, int[][] candidateBlock) {
        // 如果只有 1 個玩家，必然重複，無法避免，直接回傳 true 以免無窮迴圈
        if (players < 2) return true;

        for (int p = 0; p < players; p++) {
            // 比較：[上個區塊該玩家的最後一間房] vs [新區塊該玩家的第一間房]
            int lastRoom = lastRoundRooms[p];
            int newFirstRoom = candidateBlock[p][0];

            if (lastRoom == newFirstRoom) {
                // 抓到了！某個玩家連續兩次進入同一間房，視為無效區塊
                return false;
            }
        }
        return true;
    }

    /**
     * 取得指定玩家在指定輪數的房間編號
     * @param player_id 玩家編號 (1 ~ players)
     * @param stage 目前輪數 (1 ~ max_stages)
     * @return room 編號 (1 ~ players)，若輸入無效回傳 -1
     */
    @Override
    public int get_row_by_player(int player_id, int stage) {
        // 基本邊界檢查
        if (player_id < 1 || player_id > players || stage < 1 || stage > max_stages) {
            return -1; // Error code
        }

        // 因為陣列是 0-indexed，所以輸入要減 1
        return scheduleTable[player_id - 1][stage - 1];
    }
}