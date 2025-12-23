package com.rofs.myhome.harvestgame;

abstract class MiniGamePiece {
    private int distance; // 한 번 이동 거리
    private int x, y; // 현재 위치(화면 맵 상의 위치)

    public MiniGamePiece(int x, int y, int distance) { // 초기 위치와 이동 거리 설정
        this.x = x;
        this.y = y;
        this.distance = distance;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDistance() {
        return distance;
    }

    public boolean collide(MiniGamePiece p) { // 이 객체가 객체 p와 충돌했으면 true 리턴
        return this.x == p.getX() && this.y == p.getY();
    }

    public abstract boolean move(String[][] board); // 이동한 후의 새로운 위치로 x, y 변경

    public abstract String getShape(); // 객체의 모양을 나타내는 문자 리턴
}