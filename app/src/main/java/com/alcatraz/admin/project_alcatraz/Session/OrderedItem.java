package com.alcatraz.admin.project_alcatraz.Session;

import android.util.Log;

import java.util.List;

public class OrderedItem {
    private MenuItem item;
    private int quantity;
    private int remainingSeconds;
    private int typeIndex;
    private List<ItemCustomizationField> selectedFields;

    OrderedItem(MenuItem menuItem, int quantity, int type) {
        this.item = menuItem;
        this.quantity = quantity;
        this.typeIndex = type;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public double getPrice() {
        double price = 0;
        price = item.getTypeCost().get(typeIndex);
        return price * quantity;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public int getCount() {
        return quantity;
    }

    public MenuItem getItem() {
        return item;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OrderedItem && ((OrderedItem) obj).getItem().getId() == this.getItem().getId();
    }
}
