package com.dragonegg.lightning.pilaf.entities;

public class Item {
    private String type;
    private int amount;

    public Item(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() { return type; }
    public int getAmount() { return amount; }

    public void setType(String type) { this.type = type; }
    public void setAmount(int amount) { this.amount = amount; }

    @Override
    public String toString() {
        return amount + " " + type + (amount > 1 ? "s" : "");
    }
}
