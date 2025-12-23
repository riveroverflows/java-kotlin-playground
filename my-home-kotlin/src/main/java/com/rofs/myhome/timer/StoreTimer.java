package com.rofs.myhome.timer;

import com.rofs.myhome.item.ItemStorage;
import com.rofs.myhome.item.StoreItem;

import java.util.List;

public class StoreTimer implements Runnable {

    private List<StoreItem> storeItems;

    public StoreTimer(ItemStorage itemStorage) {
        this.storeItems = itemStorage.getStoreItems();
    }

    @Override
    public void run() {
        // TODO: 상점 타임세일 진행하기
        rand();
        /*
        if (event[rand]) {
            System.out.println();
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t =====================================");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t" + storeTimer.storeItem.get(rand).entryName + " 30% 할인 이벤트! ");
            System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t =====================================");

            storeTimer.storeItem.get(rand).entryPrice *= 0.7;
        }
         */
    }

    private void rand() {
        /*
        event = new boolean[storeTimer.storeItem.size()];
        for (int i = 0; i < event.length; i++) {
            event[i] = new Random().nextBoolean();
        }
        rand = new Random().nextInt(storeTimer.storeItem.size());
         */
    }
}
