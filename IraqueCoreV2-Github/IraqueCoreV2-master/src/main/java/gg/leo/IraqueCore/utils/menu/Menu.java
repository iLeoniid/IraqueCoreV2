package gg.leo.IraqueCore.utils.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.menu.buttons.PlaceholderButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Menu {

    protected final Player player;
    protected Integer staticSize;
    protected boolean placeholder;
    protected boolean stealable;
    protected Material placeholderMaterial = Material.GRAY_STAINED_GLASS_PANE;

    private Map<Integer, Button> cachedButtons = Collections.emptyMap();
    private Inventory openInventory;

    public Menu(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract Map<Integer, Button> getButtons(Player player);

    public abstract String getTitle(Player player);

    public int size(Map<Integer, Button> buttons) {
        int highest = 0;
        for (int key : buttons.keySet()) {
            if (key > highest) highest = key;
        }
        int rows = (int) Math.ceil((highest + 1) / 9.0);
        return Math.max(9, Math.min(54, rows * 9));
    }

    public boolean isStealable() {
        return stealable;
    }

    public Map<Integer, Button> getCachedButtons() {
        return cachedButtons;
    }

    public Button getButton(int slot) {
        return cachedButtons.get(slot);
    }

    public void openMenu() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(IraqueCore.getInstance(), this::openMenu);
            return;
        }
        if (!player.isOnline()) return;

        Map<Integer, Button> buttons = getButtons(player);
        int finalSize = staticSize != null ? staticSize : size(buttons);
        String title = ItemBuilder.color(getTitle(player));

        // Refresh in-place when possible (same size, already viewing this menu)
        Inventory current = player.getOpenInventory().getTopInventory();
        if (openInventory != null && current == openInventory && current.getSize() == finalSize) {
            render(current, buttons, finalSize);
            return;
        }

        safeSwap(() -> {
            Inventory inv = Bukkit.createInventory(null, finalSize, title != null ? title : "Menu");
            render(inv, buttons, finalSize);
            openInventory = inv;
            MenuController.register(this);
            player.openInventory(inv);
        });
    }

    /** Rebuild all slots without closing the inventory when possible. */
    public void updateMenu() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(IraqueCore.getInstance(), this::updateMenu);
            return;
        }
        if (!player.isOnline()) return;

        Inventory top = player.getOpenInventory().getTopInventory();
        if (openInventory != null && top == openInventory) {
            Map<Integer, Button> buttons = getButtons(player);
            render(top, buttons, top.getSize());
            return;
        }
        openMenu();
    }

    /** Refresh a single slot using the cached button definition. */
    public void updateSlot(int slot) {
        if (!player.isOnline()) return;
        Inventory top = player.getOpenInventory().getTopInventory();
        if (openInventory == null || top != openInventory) return;

        Button button = cachedButtons.get(slot);
        if (button == null) {
            // Re-fetch buttons so dynamic toggles stay in sync
            Map<Integer, Button> buttons = getButtons(player);
            cachedButtons = Map.copyOf(buttons);
            button = cachedButtons.get(slot);
        }
        if (button != null) {
            top.setItem(slot, button.constructItemStack(player));
        }
    }

    private void render(Inventory inv, Map<Integer, Button> buttons, int size) {
        cachedButtons = Map.copyOf(buttons);
        inv.clear();

        if (placeholder) {
            PlaceholderButton bg = new PlaceholderButton(placeholderMaterial, List.of(), " ", 0);
            for (int i = 0; i < size; i++) {
                if (!buttons.containsKey(i)) {
                    inv.setItem(i, bg.constructItemStack(player));
                }
            }
        }

        for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= size) continue;
            inv.setItem(slot, entry.getValue().constructItemStack(player));
        }
    }

    /**
     * Prevents InventoryCloseEvent from wiping the menu registration while we swap inventories.
     */
    private void safeSwap(Runnable action) {
        var uuid = player.getUniqueId();
        MenuController.beginUpdate(uuid);
        try {
            action.run();
        } finally {
            MenuController.endUpdate(uuid);
        }
    }

    protected static Map<Integer, Button> fillBorder(int size, Button filler) {
        Map<Integer, Button> map = new HashMap<>();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                map.put(i, filler);
            }
        }
        return map;
    }
}
