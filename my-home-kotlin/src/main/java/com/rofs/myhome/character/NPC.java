package com.rofs.myhome.character;

public class NPC extends Character {
    protected NPC(String name) {
        super(name);
    }

    public static NPC createNPC(String name) {
        return new NPC(name);
    }
}