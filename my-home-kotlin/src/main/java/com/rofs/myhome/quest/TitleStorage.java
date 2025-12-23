package com.rofs.myhome.quest;

import java.util.ArrayList;
import java.util.List;

public class TitleStorage {

    public static final List<TitleInfo> TITLE_INFOS = new ArrayList<>();

    static {
        TITLE_INFOS.add(TitleInfo.of("공방의 새 주인", "퀘스트 3회 완료 시"));
        TITLE_INFOS.add(TitleInfo.of("원목 공예 장인", "아이템 제작 10회 이상"));
    }
}
