package com.cs407.spendsmart;

import java.util.Date;

public class Transaction {
    private final Double amount;
    private final String category;
    private final Date date;

    public Transaction(double amount, String category, Date date) {
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }
}
