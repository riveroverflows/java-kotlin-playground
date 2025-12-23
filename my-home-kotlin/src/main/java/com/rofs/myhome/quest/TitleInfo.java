package com.rofs.myhome.quest;

public class TitleInfo {
    private String name;
    private String condition;
    private boolean achieved;

    private TitleInfo(String name, String condition) {
        this.name = name;
        this.condition = condition;
        this.achieved = false;
    }

    public static TitleInfo of(String name, String condition) {
        return new TitleInfo(name, condition);
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void achieved() {
        this.achieved = true;
    }
}