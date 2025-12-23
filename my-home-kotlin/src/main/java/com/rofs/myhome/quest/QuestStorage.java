package com.rofs.myhome.quest;

import com.rofs.myhome.character.NPC;
import com.rofs.myhome.inventory.ItemEntry;
import com.rofs.myhome.item.ItemStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestStorage {

    public List<QuestInfo> questInfos;

    public QuestStorage() {
        questInfos = new ArrayList<>();
        NPC mimi = NPC.createNPC("미미");
        NPC tomson = NPC.createNPC("톰슨");

        questInfos.add(new QuestInfo(
                "밀짚을 이용한 끈!",
                mimi,
                "수확한 밀로 끈을 만들어봐요!",
                "생각보다 튼튼해 보여요. 처음이라 걱정했는데 잘하고 계시는데요?\n앞으로도 이렇게만 해주세요!",
                Collections.singletonList(ItemEntry.of(ItemStorage.MILK, 1)),
                1000,
                40,
                Collections.singletonList(ItemEntry.of(ItemStorage.STRAW_ROPE, 4)),
                new ArrayList<>()
        ));

        questInfos.add(new QuestInfo(
                "밀짚끈으로 소파를?",
                mimi,
                "만들어 놓은 밀짚끈으로 소파를 만들어볼까요?",
                "우와! 정말 좋아 보여요! 저도 나중에 부탁해도 될까요?",
                new ArrayList<>(),
                980,
                45,
                Collections.singletonList(ItemEntry.of(ItemStorage.NATURE_ORGANIC_SOFA,
                        1)),
                new ArrayList<>()
        ));

        questInfos.add(new QuestInfo("톰슨 할아버지의 첫 주문!",
                tomson,
                "자네가 만든 의자 괜찮더군! 작업실에 의자가 필요한데.. 괜찮으면 만들어주지 않겠나?",
                "흠, 제법 하는구먼.. 밀짚도 튼튼하게 엮여있고..\n그렇다곤 해도 아직 초보자니 마음을 놓지 말게나!",
                new ArrayList<>(),
                2000,
                45,
                new ArrayList<>(),
                Collections.singletonList(ItemEntry.of(ItemStorage.NATURE_ORGANIC_SOFA,
                        1))));
    }

    public List<QuestInfo> getQuestInfos() {
        return questInfos;
    }

    public int getQuestQuantity() {
        return questInfos.size();
    }
}
