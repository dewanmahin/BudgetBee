package org.example.tools;

import org.example.model.Expense;

public interface ExpenseIterator {
    boolean hasNext();
    Expense next();
}