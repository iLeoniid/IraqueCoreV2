package gg.leo.IraqueCore.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    private final String type;
    private final String category;
    private final int page;
    private Inventory inventory;

    public MenuHolder(String type) {
        this(type, null, 0);
    }

    public MenuHolder(String type, String category, int page) {
        this.type = type;
        this.category = category;
        this.page = page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inv) {
        this.inventory = inv;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public int getPage() {
        return page;
    }
}
