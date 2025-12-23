package com.rofs.myhome.etc;

import java.util.ArrayList;
import java.util.Random;

public class ProgressBar {
    private static final ArrayList<String> TIP_LIST = new ArrayList<>();

    static {
        TIP_LIST.add("플레이어 레벨 5까지는 레벨이 오를 때마다 3000골드를 드려요!");
        TIP_LIST.add("아이템 제작을 시작하려면 원목 작업대가 꼭 필요해요.");
        TIP_LIST.add("내 정보 보기에서 업적 정보를 확인할 수 있어요.");
        TIP_LIST.add("피로도가 100이 되면 어떤 작업도 할 수 없어요. 피로도를 잘 관리해 주세요.");
        TIP_LIST.add("상점에서 피로도 회복 물약을 구입할 수 있어요.");
        TIP_LIST.add("게임 속 미니 게임을 즐겨보세요! 피로도를 회복할 수 있습니다.");
        TIP_LIST.add("휴식 모드로 진입하면 피로도가 회복됩니다.");
    }

    public static void loading() {
        int rand = new Random().nextInt(TIP_LIST.size());
        MyHomeUtils.printLineAsCount(100);
        System.out.println("\t\t⚠️ TIP ! " + TIP_LIST.get(rand));
        System.out.println();
        System.out.print("\t\t\t\t\tLoading........... ");
        int count = 1;
        for (int j = 1; j < 30; j++) {
            System.out.print("\r");
            if (count == 1) {
                System.out.print("\t\t\t\t\tLoading........... 🌑");
                count++;
            } else if (count == 2) {
                System.out.print("\t\t\t\t\tLoading........... 🌒");
                count++;
            } else if (count == 3) {
                System.out.print("\t\t\t\t\tLoading........... 🌓");
                count++;
            } else if (count == 4) {
                System.out.print("\t\t\t\t\tLoading........... 🌔");
                count++;
            } else if (count == 5) {
                System.out.print("\t\t\t\t\tLoading........... 🌕");
                count++;
            } else if (count == 6) {
                System.out.print("\t\t\t\t\tLoading........... 🌖");
                count++;
            } else if (count == 7) {
                System.out.print("\t\t\t\t\tLoading........... 🌗");
                count++;
            } else if (count == 8) {
                System.out.print("\t\t\t\t\tLoading........... 🌘");
                count++;
            } else {
                count = 1;
            }

            MyHomeUtils.delayAsMillis(70);
        }
        MyHomeUtils.printLineAsCount(100);
    }
}
