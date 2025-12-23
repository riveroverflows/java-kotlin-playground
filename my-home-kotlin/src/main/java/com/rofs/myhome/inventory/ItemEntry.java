package com.rofs.myhome.inventory;

import com.rofs.myhome.item.Item;
import com.rofs.myhome.item.ItemType;

public class ItemEntry {

    // 아이템 객체
    private Item item;

    // 아이템 객체 하나당 갖고 있는 개수
    private int quantity;

    private ItemEntry(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public static ItemEntry of(Item item, int quantity) {
        return new ItemEntry(item, quantity);
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public String getItemName() {
        return this.item.getName();
    }

    public int getItemSalePrice() {
        return (int) (this.item.getPrice() * 0.6);
    }

    public void updateQuantity(int quantity) {
        if (quantity < 0) {
            this.quantity = 0;
            return;
        }
        this.quantity = quantity;
    }

    public ItemType getItemType() {
        return this.item.getType();
    }
}