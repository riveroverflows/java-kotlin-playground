package com.rofs.myhome.quest;

import com.rofs.myhome.character.NPC;
import com.rofs.myhome.character.Player;
import com.rofs.myhome.inventory.Inventory;
import com.rofs.myhome.inventory.ItemEntry;

import java.util.List;

public class Quest {
    private QuestInfo info;
    private boolean isCompleted;

    private Quest(QuestInfo info) {
        this.info = info;
        this.isCompleted = false;
    }

    public static Quest create(QuestInfo info) {
        return new Quest(info);
    }

    public QuestInfo getInfo() {
        return this.info;
    }

    public String getQuestName() {
        return this.info.getName();
    }

    public NPC getNpc() {
        return this.info.getNpc();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean complete(Player player) {
        Inventory inventory = player.getInventory();
        for (ItemEntry deliveryItem : getInfo().getRequiredDeliveryItems()) {
            ItemEntry inventoryItem = inventory.find(deliveryItem);
            inventoryItem.updateQuantity(inventoryItem.getQuantity() - deliveryItem.getQuantity());
            if (inventoryItem.getQuantity() <= 0) {
                inventory.remove(inventoryItem);
            }
        }

        int playerExp = player.getExp();
        int playerGold = player.getGold();
        Inventory playerInventory = new Inventory(inventory);
        try {
            if (getInfo().getRewardExp() > 0) {
                player.updateExp(playerExp + getInfo().getRewardExp());
            }
            if (getInfo().getRewardGold() > 0) {
                player.updateGold(playerGold + getInfo().getRewardGold());
            }
            List<ItemEntry> rewardItems = getInfo().getRewardItems();
            rewardItems.forEach(inventory::add);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            player.updateExp(playerExp);
            player.updateGold(playerGold);
            player.replaceInventory(playerInventory);
            return false;
        }
        this.isCompleted = true;
        return true;
    }
}
