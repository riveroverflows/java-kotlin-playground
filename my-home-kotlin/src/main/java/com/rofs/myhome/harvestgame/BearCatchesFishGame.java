package com.rofs.myhome.harvestgame;

import com.rofs.myhome.etc.MyHomeUtils;
import com.rofs.myhome.item.Item;

import java.util.Arrays;
import java.util.Random;

public class BearCatchesFishGame {
    private final String[][] board;
    private Bear bear;
    private Fish fish;
    private boolean haveWon;

    public BearCatchesFishGame() {
        board = new String[10][15];
        this.bear = new Bear(0, 0);
        this.fish = getPositionedFish();
        this.haveWon = false;
    }

    public boolean haveWon() {
        return haveWon;
    }

    public Fish getPositionedFish() {
        int x = 0, y = 0;
        while (x == 0 && y == 0) {
            x = new Random().nextInt(10);
            y = new Random().nextInt(15);
        }
        return new Fish(x, y);
    }


    public void set() {
        for (String[] strings : board) {
            Arrays.fill(strings, "_");
        }
        board[bear.getX()][bear.getY()] = bear.getShape();
        board[fish.getX()][fish.getY()] = fish.getShape();
    }

    private void showArray() {
        for (String[] strings : board) {
            for (String string : strings) {
                System.out.print(string);
            }
            System.out.println();
        }
    }

    private void showWonBoard() { // 이겼을 경우 보여줄 배열(플레이어가 아이템에 닿으면 아이템 자리에 플레이어 모양 출력)
        board[bear.getX()][bear.getY()] = bear.getShape();
        for (String[] strings : board) {
            for (String string : strings) {
                System.out.print(string);
            }
            System.out.println();
        }
    }

    public void start(Item item) {
        set();
        while (true) {
            MyHomeUtils.printLineAsCount(100);
            System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = =");
            System.out.println("            " + item.getName() + " 수확중 ...");
            System.out.println();
            if (bear.collide(fish)) {
                showWonBoard();
                win();
                break;
            }
            showArray();
            boolean moved = bear.move(board);
            if (!moved) {
                break;
            }
        }
    }

    private void win() {
        this.haveWon = true;
    }
}