package com.rofs.myhome.quest;

import com.rofs.myhome.character.Player;
import com.rofs.myhome.etc.MyHomeUtils;

import java.util.List;

public class QuestThread implements Runnable {
    private QuestStorage storage;
    private Player player;

    public QuestThread(Player player) {
        this.player = player;
        storage = new QuestStorage();
    }

    @Override
    public void run() {
        int questQuantity = storage.getQuestQuantity();
        List<QuestInfo> quests = storage.getQuestInfos();
        while (player.getQuests().size() <= questQuantity) {
            MyHomeUtils.delayAsMillis(1000);
            if (player.getLevel() <= 3) {
                continue;
            }
            int level = (int) (Math.random() * 10);
            if (player.getLevel() != level) {
                continue;
            }

            MyHomeUtils.delayAsMillis((int) (Math.random() * 100_000) + 10_000);
            if (player.getLevel() != level) {
                continue;
            }

            int randomNumber = (int) (Math.random() * questQuantity);
            QuestInfo questInfo = quests.get(randomNumber);
            if (player.hasQuest(questInfo)) {
                continue;
            }
            System.out.println("\n\t\t\t\t\t\t\t\t\t\t\t\t =====================================");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + questInfo.getNpc().getName() + " 퀘스트 도착!!");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t   📜 퀘스트 리스트를 확인하세요");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t =====================================");
            player.addQuest(Quest.create(questInfo));
        }
    }
}