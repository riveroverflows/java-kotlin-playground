package com.rofs.myhome.quest;

import com.rofs.myhome.character.NPC;
import com.rofs.myhome.inventory.ItemEntry;

import java.util.List;

public class QuestInfo {
    private final String name;
    private final NPC npc;
    private final char[] script;
    private final char[] endingScript;
    private final int rewardGold;
    private final int rewardExp;
    private final List<ItemEntry> rewardItems;
    private final List<ItemEntry> requiredCraftItems;
    private final List<ItemEntry> requiredDeliveryItems;

    public QuestInfo(String name, NPC npc, String script, String endingScript, List<ItemEntry> rewardItems, int rewardGold, int rewardExp, List<ItemEntry> requiredCraftItems, List<ItemEntry> requiredDeliveryItems) {
        this.name = name;
        this.npc = npc;
        this.script = script.toCharArray();
        this.endingScript = endingScript.toCharArray();
        this.rewardItems = rewardItems;
        this.rewardGold = rewardGold;
        this.rewardExp = rewardExp;
        this.requiredCraftItems = requiredCraftItems;
        this.requiredDeliveryItems = requiredDeliveryItems;
    }

    public String getName() {
        return name;
    }

    public NPC getNpc() {
        return npc;
    }

    public String getNpcName() {
        return this.npc.getName();
    }

    public char[] getScript() {
        return script;
    }

    public char[] getEndingScript() {
        return endingScript;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public int getRewardExp() {
        return rewardExp;
    }

    public List<ItemEntry> getRewardItems() {
        return rewardItems;
    }

    public List<ItemEntry> getRequiredCraftItems() {
        return requiredCraftItems;
    }

    public List<ItemEntry> getRequiredDeliveryItems() {
        return requiredDeliveryItems;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QuestInfo)) {
            return false;
        }
        QuestInfo info = (QuestInfo) obj;
        return getName().equalsIgnoreCase(info.getName()) && getNpcName().equalsIgnoreCase(info.getNpcName());
    }
}