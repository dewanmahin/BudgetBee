package org.example.model;

public class Expense {
    public String date;
    public String description;
    public String category;
    public int quantity;
    public double amount;

    public Expense(String date, String description, String category, int quantity, double amount) {
        this.date = date;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.amount = amount;
    }

    public double getTotal() {
        return quantity * amount;
    }
}