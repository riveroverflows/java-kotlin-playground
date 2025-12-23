package com.rofs.myhome.timer;

import com.rofs.myhome.item.GrowthItem;

public class CultivateTimer implements Runnable {
    private final GrowthItem item;

    public CultivateTimer(GrowthItem item) {
        this.item = item;
    }

    public void run() {
        int growingPeriod = item.getGrowingPeriod();
        int countNumber = growingPeriod;
        for (int i = 0; i < growingPeriod; i++) {
            try {
                Thread.sleep(1000);
                countNumber--;
            } catch (InterruptedException ignore) {
            }
        }
        if (countNumber <= 0) {
            System.out.println();
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t =====================================");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + item.getName() + " 재배 완료!");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t =====================================");
        }
        item.harvestable();
    }
}