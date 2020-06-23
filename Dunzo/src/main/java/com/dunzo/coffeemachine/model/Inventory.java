package com.dunzo.coffeemachine.model;

import java.util.List;
import java.util.Set;

public interface Inventory {

    void addItem(Item item);

    void addItemWithMinimumThreshold(Item item, int minimumQty);

    Item getItem(String name);

    Item removeItem(String name);

    public void refill(String itemName, int qty);

    public List<Item> getLowRunningIngredients();

    Set<String> getAllAvailableItems();

    boolean itemAvailable(String required_item);
}
