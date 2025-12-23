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
import com.rofs.myhome.etc.MyHomeConstants;
import com.rofs.myhome.etc.MyHomeUtils;
import com.rofs.myhome.etc.ProgressBar;
import com.rofs.myhome.item.ItemStorage;
import com.rofs.myhome.quest.AchieveTitleThread;
import com.rofs.myhome.quest.QuestThread;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Scanner;

import static com.rofs.myhome.etc.MyHomeConstants.FATIGUE_IS_TOO_HIGH;

public class MyHome {

    private final Farm farm;
    private final AnimalFarm animalFarm;
    private final Forest forest;
    private final CraftShop craftShop;
    private final Arcade arcade;
    private final Merchant merchant;

    SoundPlayerUsingClip soundPlayer = new SoundPlayerUsingClip();
    Scanner scanner = new Scanner(System.in);

    public MyHome() {
        arcade = new Arcade();
        ItemStorage itemStorage = new ItemStorage();
        farm = new Farm(itemStorage);
        animalFarm = new AnimalFarm(itemStorage);
        forest = new Forest(itemStorage);
        craftShop = new CraftShop(itemStorage);
        merchant = Merchant.createMerchant("로빈", itemStorage);
    }

    public void start() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        ProgressBar.loading();
        soundPlayer.play("intro.wav", 1);

        System.out.println(
                "\r                              -,*.~                                   \n" +
                        "                            ;,:=~;~,                                  \n" +
                        "                             :.:=.-;                                  \n" +
                        "                          -,-!=====;-.                                \n" +
                        "                        ,-!===!::;*===:,:      ,,,*                   \n" +
                        "             :-~.    ;.-=$===*~.-,:*====;,-,,~:==:.                   \n" +
                        "              !=*-  ..;*=====*~.~,;=======-~!=====-~                  \n" +
                        "           .,;===$,,;,,-!*$=*====**=$==!-:!!=====*~.,,,~              \n" +
                        "           .-:$==$~  . .-:*-   ,$: ;$~..,.=: :=====.;=!;*!*;.-        \n" +
                        "        -~;,:====$.....,,-*=-...:  ;$.,*;.~, ;$*  !*==:     ;*.~, ,-  \n" +
                        "       .:$==:~====$!,. .. !~.~=!$:.;:~-.:!* ,:=..;==$;. ;    =!!!;*;-.\n" +
                        "  -,---.-========$==...,-:~$:,,~$:.~$$-,,;~.;$=!*,      .*!*!      -;,\n" +
                        "~-;;~--:!=$$$$==$: :!:~~;;=$$$$$$$$$$==~.-!;*=;!-     ,;**:.:-,~- .;:-\n" +
                        ",!.            ,==     =$$!        -=$$;    ,*$$=-   .!$$$$=    ~$-.! \n" +
                        ",~!;            ,*     =*,           -$!.   ,*$$$:    ,;==!.    ;*~,. \n" +
                        " -~!    ~===:   .*     ==:   -*=*-    ~!.   ,==-~*;           .;:..:!,\n" +
                        " -~!    !$$$!.  .*       ~; ~$$$$$:   ,;.   ,!     ,;!~    ~!;.    .!-\n" +
                        " -~!    !$$$!.  .*     .-!~ ,$$$$$-   ,;.   ,==:                 .;*:-\n" +
                        "  -*.    -~~,   .*.    =:     .-.     :!.   ,=$$;;*!;~-----~:;!*~-*.  \n" +
                        "  .:=,..........,=.    $$;,. .... ..,;#*,   -=$$:     .......    ~*.  \n" +
                        "   , ;=!:::::::;=#,....,:##=:-----:*##$=-.....=#*.  .:######=    =:-  \n" +
                        "       .~=########$;~-~;$#$##########$$##=!!*$#$#!,....,--,....,=:.   \n" +
                        "         ,,!#######################################$:--,,,,-,:=!,     \n" +
                        "           ,.~::;~$==;;!:;;:*:=~:=;*;::*#!;##=$;:;:;~~:;;;;:~..;      \n" +
                        "              .;;!;-*~-!:;-,*:$:-=:*##*=*-!~=;~*;;-:                  \n" +
                        "                .~~~-~!-:;!~~!!*-!***~!**;,!!-~:,;                    \n" +
                        "                     .,,,,,,,,..,,,.,,.....,..                        \n");

        MyHomeUtils.delayAsMillis(1800);

        System.out.println("\n" +
                "                       __                       \n" +
                "   ____ ___  __  __   / /_  ____  ____ ___  ___ \n" +
                "  / __ `__ \\/ / / /  / __ \\/ __ \\/ __ `__ \\/ _ \\\n" +
                " / / / / / / /_/ /  / / / / /_/ / / / / / /  __/\n" +
                "/_/ /_/ /_/\\__, /  /_/ /_/\\____/_/ /_/ /_/\\___/ \n" +
                "          /____/                                \n");

        MyHomeUtils.delayAsMillis(1000);

        soundPlayer.play("mainMusic.wav", 0);
        MyHomeUtils.printLineAsCount(33);
        ProgressBar.loading();

        System.out.print("\r달님이 수호해주는 마을…");

        MyHomeUtils.printLineAsCount(1);
        MyHomeUtils.delayAsMillis(2000);
        MyHomeUtils.printLineAsCount(2);

        System.out.println("그 마을의 가장 유명한 공방에");
        System.out.println("스승님 아래서 열심히 일하던 아이가 있었어요.");

        MyHomeUtils.delayAsMillis(2000);
        MyHomeUtils.printLineAsCount(2);
        MyHomeUtils.delayAsMillis(1000);

        ProgressBar.loading();

        String playerName;

        do {
            System.out.println("당신의 이름을 알려주세요. (2 ~ 8 글자)");
            System.out.print("입력 >> ");              // 게임 플레이어 이름 입력
            playerName = scanner.nextLine();

            if (playerName == null || playerName.length() < 2) {
                System.out.println("┌──────────────────────────────────────────────────┐");
                System.out.println("            이름이 너무 짧아요. 다시 입력해주세요.        ");
            }
            if (playerName != null && playerName.length() > 8) {
                System.out.println("┌──────────────────────────────────────────────────┐");
                System.out.println("            이름이 너무 길어요. 다시 입력해주세요.        ");
            }
        } while (playerName == null || playerName.length() < 2 || playerName.length() > 8);

        MyHomeUtils.printLineAsCount(100);
        Player player = Player.createPlayer(playerName);
        System.out.println("[ " + playerName + " ] 반가워요");


        // =================== 로딩 시나리오 ========================

        MyHomeUtils.delayAsMillis(1000);
        MyHomeUtils.printLineAsCount(2);

        System.out.println("어느 날 스승님이 한 가지 제안을 하셨어요.");

        MyHomeUtils.printLineAsCount(1);
        MyHomeUtils.delayAsMillis(2000);

        System.out.println("숲 속 어딘가에 사용하지 않는 공방을");
        System.out.println("너만의 공방으로 만들지 않겠냐고 말이에요!");

        MyHomeUtils.delayAsMillis(3000);
        MyHomeUtils.printLineAsCount(2);

        System.out.println("하지만..");

        MyHomeUtils.delayAsMillis(1500);
        MyHomeUtils.printLineAsCount(1);

        System.out.println("공방의 주인으로 인정받기 위해서는");
        System.out.println("한가지 해야 할 일이 있었어요.");

        MyHomeUtils.delayAsMillis(3000);
        MyHomeUtils.printLineAsCount(1);

        System.out.println("마을의 가장 커다란 축제인 감사제를");
        System.out.println("마을 사람들을 도와 무사히 열릴 수 있게 해야 한대요!");

        MyHomeUtils.delayAsMillis(3000);
        MyHomeUtils.printLineAsCount(1);

        System.out.println("스승님 없이 혼자서 공방을 잘 운영해 나갈 수 있을까요?");

        MyHomeUtils.printLineAsCount(1);
        MyHomeUtils.delayAsMillis(3000);

        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("                공방 운영에 도움이 될만한                  ");
        System.out.println("             초기 지원 자금 " + MyHomeConstants.INITIAL_SUPPORT_GOLD + "골드를 드릴게요.      ");
        System.out.println("└──────────────────────────────────────────────────┘");

        System.out.println();
        System.out.println("    Enter를 누르면 Main Menu로 이동합니다. ");
        scanner.nextLine();
        //  ================== 로딩 시나리오 끝================

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
                    ProgressBar.loading();
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
                    ProgressBar.loading();
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
                    ProgressBar.loading();
                    merchant.buyAndSell(player, scanner);
                    break;
                case 7:     // 휴식 취하기
                    ProgressBar.loading();
                    player.willRest(scanner);
                    break;
                case 8:     // 미니게임
                    ProgressBar.loading();
                    arcade.showGames(player, scanner);
                    break;
                default:
                    MyHomeUtils.enterAgain(scanner);
            }
        }
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