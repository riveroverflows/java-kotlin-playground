package com.rofs.myhome.area;

import com.rofs.myhome.character.Player;
import com.rofs.myhome.etc.MyHomeUtils;
import com.rofs.myhome.inventory.ItemEntry;
import com.rofs.myhome.item.CraftItem;
import com.rofs.myhome.item.ItemStorage;

import java.util.List;
import java.util.Scanner;

public class CraftShop extends Area {
    private final List<CraftItem> items;

    public CraftShop(ItemStorage itemStorage) {
        super("공방");
        items = itemStorage.getCraftItems();
    }

    public void craft(Player player, Scanner scanner) {
        MyHomeUtils.printLineAsCount(100);
        if (!player.hasWoodenWorkbench()) {
            System.out.println("┌──────────────────────────────────────────────────┐");
            System.out.println("          감사제를 준비하려면 원목 작업대가 필요해요.");
            System.out.println("             상점에서 원목 작업대를 구입하세요.");
            System.out.println();
            scanner.nextLine();
            return;
        }
        int playerLevel = player.getLevel();
        while (true) {
            MyHomeUtils.printLineAsCount(100);
            showCraftMenus(playerLevel);
            System.out.println();
            System.out.print("입력 >> ");
            String inputValue = MyHomeUtils.input(scanner);
            if (!MyHomeUtils.isInteger(inputValue)) {
                MyHomeUtils.enterAgain(scanner);
                continue;
            }
            int input = MyHomeUtils.stringToInt(inputValue);
            if (input < 0 || input > items.size()) {
                MyHomeUtils.enterAgain(scanner);
                continue;
            }
            if (input == 0) {
                return;
            }

            CraftItem item = items.get(input - 1);
            if (!item.isCraftable(playerLevel)) {
                MyHomeUtils.printLineAsCount(100);
                System.out.println("┌──────────────────────────────────────────────────┐");
                System.out.println("      플레이어의 레벨이 충족되지 않아 제작 할 수 없습니다.");
                System.out.println("                이전 단계로 돌아갑니다.");
                scanner.nextLine();
                continue;
            }

            while (true) {
                MyHomeUtils.printLineAsCount(100);
                String itemName = item.getName();
                System.out.println("┌──────────────────────────────────────────────────┐");
                System.out.println("                 [ " + itemName + " ] 제작");
                System.out.println();
                System.out.println("  필요한 재료 아이템\t\t\t   보유 개수 / 필요 개수");
                System.out.println();

                ItemEntry[] requiredItems = item.getRequiredItems();
                int craftableQuantity = 999_999_999;
                for (int i = 0; i < requiredItems.length; i++) {
                    ItemEntry requiredItem = requiredItems[i];
                    String requiredItemName = requiredItem.getItemName();
                    int requiredItemQuantity = requiredItem.getQuantity();

                    ItemEntry playerItem = player.getItem(requiredItem);
                    int playerItemQuantity = 0;
                    if (playerItem != null) {
                        playerItemQuantity = playerItem.getQuantity();
                    }

                    System.out.print(i + 1 + ". " + requiredItemName);
                    System.out.print("\t\t\t\t\t\t   " + playerItemQuantity + " / " + requiredItemQuantity);
                    craftableQuantity = Math.min(craftableQuantity, playerItemQuantity / requiredItemQuantity);
                }

                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println("1. 제작하기         0. 이전으로");
                System.out.print("입력 >> ");
                inputValue = MyHomeUtils.input(scanner);
                if (!MyHomeUtils.isInteger(inputValue)) {
                    MyHomeUtils.enterAgain(scanner);
                    continue;
                }
                input = MyHomeUtils.stringToInt(inputValue);
                if (input > 1 || input < 0) {
                    MyHomeUtils.enterAgain(scanner);
                    continue;
                }
                if (input == 0) {
                    break;
                }

                while (true) {
                    MyHomeUtils.printLineAsCount(100);
                    if (craftableQuantity <= 0) {
                        System.out.println("┌──────────────────────────────────────────────────┐");
                        System.out.println("             아이템을 제작할 재료가 부족해요.");
                        scanner.nextLine();
                        break;
                    }
                    System.out.println("┌──────────────────────────────────────────────────┐");
                    System.out.println("                 몇 개 제작하시겠어요? ");
                    System.out.println(" (최대 제작 가능 개수: " + craftableQuantity + ")         0. 이전으로");
                    System.out.println();
                    System.out.println("숫자만 입력해주세요");
                    System.out.print("입력 >> ");
                    inputValue = MyHomeUtils.input(scanner);
                    if (!MyHomeUtils.isInteger(inputValue)) {
                        MyHomeUtils.enterAgain(scanner);
                        continue;
                    }
                    int craftCount = MyHomeUtils.stringToInt(inputValue);
                    if (craftCount < 0) {
                        MyHomeUtils.enterAgain(scanner);
                        continue;
                    }
                    if (craftCount == 0) {
                        break;
                    }
                    if (craftCount > craftableQuantity) {
                        MyHomeUtils.printLineAsCount(100);
                        System.out.println("┌──────────────────────────────────────────────────┐");
                        System.out.println("         최대 제작 가능 개수는 [ " + craftableQuantity + " ]개 입니다.");
                        System.out.println("                   다시 입력해주세요.");
                        scanner.nextLine();
                        continue;
                    }
                    player.craft(item, craftCount);
                    scanner.nextLine();
                    break;
                }
            }
        }
    }

    private void showCraftMenus(int playerLevel) {
        MyHomeUtils.printLineAsCount(100);
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("         어떤 아이템을 제작할까요? (0. 메인 메뉴로)");
        System.out.println();
        for (int i = 0; i < items.size(); i++) {
            CraftItem item = items.get(i);
            String itemName = item.getName();
            System.out.printf("%-10s", (i + 1) + ". " + itemName);
            if (!item.isCraftable(playerLevel)) {
                System.out.print("\t\t [ 🔒 ] LV." + item.getLevel() + " 이상");
            }
            System.out.println();
        }
    }
}