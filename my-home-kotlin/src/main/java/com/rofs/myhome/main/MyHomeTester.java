package com.rofs.myhome.main;

import com.rofs.myhome.area.AnimalFarm;
import com.rofs.myhome.area.Arcade;
import com.rofs.myhome.area.CraftShop;
import com.rofs.myhome.area.Farm;
import com.rofs.myhome.area.Forest;
import com.rofs.myhome.soundplayer.SoundPlayerUsingClip;
import com.rofs.myhome.character.LevelUpThread;
import com.rofs.myhome.character.Merchant;
import com.rofs.myhome.character.Player;
import com.rofs.myhome.etc.MyHomeUtils;
import com.rofs.myhome.item.ItemStorage;
import com.rofs.myhome.quest.AchieveTitleThread;
import com.rofs.myhome.quest.QuestThread;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Scanner;

import static com.rofs.myhome.etc.MyHomeConstants.FATIGUE_IS_TOO_HIGH;

public class MyHomeTester {

    private final Farm farm;
    private final AnimalFarm animalFarm;
    private final Forest forest;
    private final CraftShop craftShop;
    private final Arcade arcade;
    private final Merchant merchant;

    SoundPlayerUsingClip soundPlayer = new SoundPlayerUsingClip();
    Scanner scanner = new Scanner(System.in);

    public MyHomeTester() {
        arcade = new Arcade();
        ItemStorage itemStorage = new ItemStorage();
        farm = new Farm(itemStorage);
        animalFarm = new AnimalFarm(itemStorage);
        forest = new Forest(itemStorage);
        craftShop = new CraftShop(itemStorage);
        merchant = Merchant.createMerchant("로빈", itemStorage);
    }

    public void start() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        soundPlayer.play("mainMusic.wav", 0);
        Player player = Player.createPlayer("river");
        player3LevelUp(player);

        LevelUpThread levelUpThread = new LevelUpThread(player);
        new Thread(levelUpThread).start();

        QuestThread questThread = new QuestThread(player);
        new Thread(questThread).start();

        AchieveTitleThread achieveTitleThread = new AchieveTitleThread(player);
        new Thread(achieveTitleThread).start();

        while (true) {
            showMenus(player.isResting());

            String inputValue = MyHomeUtils.input(scanner);
            if (!MyHomeUtils.isInteger(inputValue)) {
                MyHomeUtils.enterAgain(scanner);
                continue;
            }

            // TODO: inputValue enum으로 바꾸기. 각 case가 뭘 의미하는지 한눈에 알기 어려움
            switch (MyHomeUtils.stringToInt(inputValue)) {
                case 0:
                    System.exit(0);
                    break;
                case 1:     // 플레이어 정보 보기
                    player.showInfo(scanner);
                    break;
                case 2:     // 재료 수확하러 하기
                    if (player.isResting()) {
                        printToRest();
                        continue;
                    }
                    if (player.needToRest()) {
                        MyHomeUtils.printLineAsCount(100);
                        System.out.println("┌──────────────────────────────────────────────────┐");
                        System.out.println("      " + FATIGUE_IS_TOO_HIGH);
                        scanner.nextLine();
                        continue;
                    }
                    showAreas(player);
                    break;
                case 3:     // 아이템 제작
                    if (player.isResting()) {
                        printToRest();
                        continue;
                    }
                    if (player.needToRest()) {
                        MyHomeUtils.printLineAsCount(100);
                        System.out.println("┌──────────────────────────────────────────────────┐");
                        System.out.println("      " + FATIGUE_IS_TOO_HIGH);
                        scanner.nextLine();
                        continue;
                    }
                    craftShop.craft(player, scanner);
                    break;
                case 4:     // 퀘스트 리스트 확인
                    if (player.isResting()) {
                        printToRest();
                        continue;
                    }
                    if (player.needToRest()) {
                        MyHomeUtils.printLineAsCount(100);
                        System.out.println("┌──────────────────────────────────────────────────┐");
                        System.out.println("      " + FATIGUE_IS_TOO_HIGH);
                        scanner.nextLine();
                        continue;
                    }
                    player.showQuests(scanner);
                    break;
                case 5:     // 인벤토리 확인
                    player.showInventory(scanner);
                    break;
                case 6:     // 상점

                    merchant.buyAndSell(player, scanner);
                    break;
                case 7:     // 휴식 취하기
                    player.willRest(scanner);
                    break;
                case 8:     // 미니게임
                    arcade.showGames(player, scanner);
                    break;
                default:
                    MyHomeUtils.enterAgain(scanner);
            }
        }
    }

    private void player3LevelUp(Player player) {
        player.updateExp(200);
    }

    private static void showMenus(boolean isResting) {
        MyHomeUtils.printLineAsCount(100);
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("                     Main Menu");
        System.out.println("└──────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("               1. 👤 내 정보 보기");
        if (!isResting) {
            System.out.println("               2. 🍓 재료 수확하기");
            System.out.println("               3. 🔨 아이템 제작");
            System.out.println("               4. 📜 퀘스트 리스트 보기");
        } else {
            System.out.println("               2. 🍓 재료 수확하기 (휴식중)");
            System.out.println("               3. 🔨 아이템 제작 (휴식중)");
            System.out.println("               4. 📜 퀘스트 리스트 보기 (휴식중)");
        }
        System.out.println("               5. 📦 인벤토리 보기");
        System.out.println("               6. 💰 상점");
        if (!isResting) {
            System.out.println("               7. 😴 휴식 취하기");
        } else {
            System.out.println("               7. 🚫 휴식 끝내기");
        }
        System.out.println("               8. 🎮 미니 게임");
//                    System.out.println("               9. ⚙️ 설정");
        System.out.println();
        System.out.println("               0. 🔚 게임 종료               ");
        System.out.println();
        System.out.print("입력 >> ");
    }

    public void printToRest() {
        MyHomeUtils.printLineAsCount(100);
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("                지금은 휴식 중이에요.");
        System.out.println();
        scanner.nextLine();
    }

    private void showAreas(Player player) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        while (true) {
            MyHomeUtils.printLineAsCount(100);
            System.out.println("┌──────────────────────────────────────────────────┐");
            System.out.println("           재료를 수확하러 왔다. 어디로 갈까?");
            System.out.println();
            System.out.println("   1. 밭     2. 동물농장     3. 숲        0. 이전으로");
            System.out.println("└──────────────────────────────────────────────────┘");
            System.out.println();
            System.out.print("입력 >> ");

            String inputVal = MyHomeUtils.input(scanner);
            if (!MyHomeUtils.isInteger(inputVal)) {
                MyHomeUtils.enterAgain(scanner);
                continue;
            }

            // TODO: inputVal enum 으로 변경하기. 각 case가 뭘 의미하는지 한눈에 파악하기 어려움.
            switch (MyHomeUtils.stringToInt(inputVal)) {
                case 1:
                    farm.cultivate(player, scanner);
                    break;
                case 2:
                    animalFarm.breed(player, scanner);
                    break;
                case 3:
                    forest.growTrees(player, scanner);
                    break;
                default:
                    return;
            }
        }
    }
}