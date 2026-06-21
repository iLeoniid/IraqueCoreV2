package gg.leo.IraqueCore.utils.menu.listener;

import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.MenuController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuListener implements Listener {

    private final Map<UUID, Long> timestamps = new HashMap<>();

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Menu menu = MenuController.menus.get(player.getUniqueId());

        if (menu != null && event.getClickedInventory() != player.getInventory()) {
            event.setCancelled(!menu.stealable);

            if (event.getClick() == ClickType.CREATIVE || event.getClick() == ClickType.MIDDLE) {
                event.setCancelled(true);
                return;
            }

            Long time = timestamps.get(player.getUniqueId());
            if (time != null && System.currentTimeMillis() - time < 300L) {
                event.setCancelled(true);
                timestamps.remove(player.getUniqueId());
                return;
            }
            timestamps.put(player.getUniqueId(), System.currentTimeMillis());

            Button button = menu.getButtons(player).get(event.getSlot());
            if (button != null) {
                button.onClick(player, event.getSlot(), event.getClick());
            }
        }
    }

    @EventHandler
    public void onPaginatedMenuClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        var paginated = MenuController.paginatedMenus.get(player.getUniqueId());

        if (paginated != null && event.getClickedInventory() != player.getInventory()) {
            event.setCancelled(true);

            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                event.setCancelled(true);
                return;
            }

            Long time = timestamps.get(player.getUniqueId());
            if (time != null && System.currentTimeMillis() - time < 300L) {
                event.setCancelled(true);
                timestamps.remove(player.getUniqueId());
                return;
            }
            timestamps.put(player.getUniqueId(), System.currentTimeMillis());

            var buttons = paginated.getButtonsInRange(player);
            Button button = buttons.get(event.getSlot());
            if (button != null) {
                button.onClick(player, event.getSlot(), event.getClick());
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        Menu menu = MenuController.menus.get(player.getUniqueId());
        if (menu != null) {
            event.setCancelled(!menu.stealable);
            return;
        }

        if (MenuController.paginatedMenus.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        MenuController.menus.remove(event.getPlayer().getUniqueId());
        MenuController.paginatedMenus.remove(event.getPlayer().getUniqueId());
    }
}
