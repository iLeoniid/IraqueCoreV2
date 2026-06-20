package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class BaseMenu {

    protected final Player player;
    protected final int size;
    protected final String title;
    protected final MenuHolder holder;
    protected Inventory inventory;

    public BaseMenu(Player player, int size, String title, String type) {
        this.player = player;
        this.size = size;
        this.title = title;
        this.holder = new MenuHolder(type);
        this.inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);
    }

    public BaseMenu(Player player, int size, String title, String type, String category, int page) {
        this.player = player;
        this.size = size;
        this.title = title;
        this.holder = new MenuHolder(type, category, page);
        this.inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);
    }

    public abstract void build();

    public abstract void handleClick(int slot);

    public void open() {
        build();
        player.openInventory(inventory);
    }

    public void close() {
        player.closeInventory();
    }

    protected void fill(int... slots) {
        ItemStack bg = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int slot : slots) {
            inventory.setItem(slot, bg);
        }
    }

    protected void fillBorders() {
        ItemStack bg = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                    inventory.setItem(i, bg);
                }
            }
        }
    }

    protected void fillBackground() {
        ItemStack bg = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, bg);
            }
        }
    }

    protected void setItem(int slot, Material material, String name, String... lore) {
        inventory.setItem(slot, ItemBuilder.of(material).name(name).lore(lore).build());
    }

    protected void setSkull(int slot, String skullOwner, String name, String... lore) {
        inventory.setItem(slot, ItemBuilder.of(Material.PLAYER_HEAD, 1)
                .skullOwner(skullOwner).name(name).lore(lore).build());
    }

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    public MenuHolder getHolder() {
        return holder;
    }

    public String getType() {
        return holder.getType();
    }

    public String getCategory() {
        return holder.getCategory();
    }

    public int getPage() {
        return holder.getPage();
    }
}
