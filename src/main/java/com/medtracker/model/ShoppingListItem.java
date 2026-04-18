package com.medtracker.model;

import java.util.List;

public class ShoppingList {

    private Long id;
    private List<ShoppingListItem> items;

    public Long getId() {
        return id;
    }

    public List<ShoppingListItem> getItems() {
        return items;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setItems(List<ShoppingListItem> items) {
        this.items = items;
    }
}