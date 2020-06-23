package com.dunzo.coffeemachine.model;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Item {

    private final String name;

    private volatile AtomicInteger qty;

    private Map<String,Integer> requiredIngredientQtyMap;

    private Item(String name, Map<String, Integer> requiredIngredientQtyMap) {
        this.name=name;
        this.requiredIngredientQtyMap=requiredIngredientQtyMap;
    }

    private Item(String name,int qty){
        this.name = name;
        this.qty = new AtomicInteger(qty);
    }

    public static Item createInventoryItem(String name,int qty){
        return new Item(name,qty);
    }

    public static Item createBeveragesItem(String name,Map<String, Integer> requiredIngredientQtyMap){
        return new Item(name,requiredIngredientQtyMap);
    }

    public Map<String, Integer> getRequiredIngredientQtyMap() {
        return requiredIngredientQtyMap;
    }

    public String getName() {
        return name;
    }

    public AtomicInteger getQty() {
        return qty;
    }

    public void setQty(AtomicInteger qty) {
        this.qty = qty;
    }
}
