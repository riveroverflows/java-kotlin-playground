package com.rofs.myhome.character;

public abstract class Character {
    private String name;

    public String getName() {
        return this.name;
    }

    protected Character(String name) {
        this.name = name;
    }
}