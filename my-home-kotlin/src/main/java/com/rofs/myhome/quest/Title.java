package com.rofs.myhome.quest;

public class Title {
    private final TitleInfo info;
    private boolean isAchieved;

    private Title(TitleInfo info) {
        this.info = info;
        this.isAchieved = false;
    }

    public static Title of(TitleInfo info) {
        return new Title(info);
    }

    public TitleInfo getInfo() {
        return info;
    }

    public boolean isAchieved() {
        return isAchieved;
    }

    public void achieved() {
        this.isAchieved = true;
    }
}
