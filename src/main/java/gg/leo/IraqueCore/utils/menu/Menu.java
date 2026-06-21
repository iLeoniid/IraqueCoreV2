package gg.leo.IraqueCore.utils.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.menu.buttons.PlaceholderButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

public abstract class Menu {

    protected final Player player;
    protected Integer staticSize;
    protected boolean placeholder;
    protected boolean stealable;

    public Menu(Player player) {
        this.player = player;
    }

    public abstract Map<Integer, Button> getButtons(Player player);
    public abstract String getTitle(Player player);

    public int size(Map<Integer, Button> buttons) {
        int highest = 0;
        for (int key : buttons.keySet()) {
            if (key > highest) highest = key;
        }
        return (int) (Math.ceil((highest + 1) / 9.0) * 9.0);
    }

    public boolean isStealable() {
        return stealable;
    }

    public void openMenu() {
        MenuController.paginatedMenus.remove(player.getUniqueId());
        MenuController.menus.put(player.getUniqueId(), this);

        Map<Integer, Button> buttons = getButtons(player);
        int finalSize = staticSize != null ? staticSize : size(buttons);

        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv != null && inv.getHolder() == null && inv.getSize() == finalSize) {
            inv.clear();
            if (placeholder && staticSize != null) {
                PlaceholderButton bg = new PlaceholderButton(
                        Material.GRAY_STAINED_GLASS_PANE, List.of(), " ", 0);
                for (int i = 0; i < staticSize; i++) {
                    if (!buttons.containsKey(i)) {
                        inv.setItem(i, bg.constructItemStack(player));
                    }
                }
            }
            for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue().constructItemStack(player));
            }
            player.updateInventory();
        } else {
            Bukkit.getScheduler().runTask(IraqueCore.getInstance(), () -> {
                if (player.getOpenInventory().getTopInventory() != null) {
                    player.closeInventory();
                }
                Inventory newInv = Bukkit.createInventory(null, finalSize, ChatColor.translateAlternateColorCodes('&', getTitle(player)));
                if (placeholder && staticSize != null) {
                    PlaceholderButton bg = new PlaceholderButton(
                            Material.GRAY_STAINED_GLASS_PANE, List.of(), " ", 0);
                    for (int i = 0; i < staticSize; i++) {
                        if (!buttons.containsKey(i)) {
                            newInv.setItem(i, bg.constructItemStack(player));
                        }
                    }
                }
                for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
                    newInv.setItem(entry.getKey(), entry.getValue().constructItemStack(player));
                }
                player.openInventory(newInv);
                player.updateInventory();
            });
        }
    }
}
