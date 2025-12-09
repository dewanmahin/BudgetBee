package org.example.tools;

import java.util.ArrayList;
import java.util.List;

public class CategoryGroup extends CategoryComponent {
    List<CategoryComponent> components = new ArrayList<>();
    String name;

    public CategoryGroup(String name) {
        this.name = name;
    }

    @Override
    public void add(CategoryComponent component) {
        components.add(component);
    }

    @Override
    public double getAmount() {
        double total = 0;
        for (CategoryComponent component : components) {
            total += component.getAmount();
        }
        return total;
    }
}
