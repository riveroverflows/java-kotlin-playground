package com.rofs.myhome.area;

public enum AreaType {
    FOREST("숲"),
    FARM("농장"),
    ANIMAL_FARM("동물농장"),
    STORE("상점"),
    CRAFT_SHOP("공방"),
    ARCADE("미니게임");

    private final String name;

    AreaType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

