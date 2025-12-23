package com.rofs.myhome.item;

public enum ItemType {
    CRAFTING("제작"), // 공예품
    CULTIVATE("생산"), // 기르는 아이템. 동식물..
    CONSUMPTION("소비"), // 소비아이템
    STORE("상점"); // 상점아이템

    private final String name;

    ItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
