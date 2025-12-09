package org.example.tools;

public class CategoryLeaf extends CategoryComponent {
    String name;
    double amount;

    public CategoryLeaf(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
