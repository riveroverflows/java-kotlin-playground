package com.rofs.myhome.harvestgame;

class Fish extends MiniGamePiece {
    public Fish(int x, int y) {
        super(x, y, 0);
    }

    @Override
    public boolean move(String[][] board) {
        return false;
    }

    @Override
    public String getShape() {
        return "∞";
    }
}