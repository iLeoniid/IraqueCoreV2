package gg.leo.IraqueCore.utils.menu.listener;

import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.MenuController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuListener implements Listener {

    private final Map<UUID, Long> timestamps = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Menu menu = MenuController.menus.get(player.getUniqueId());
        var paginated = MenuController.paginatedMenus.get(player.getUniqueId());

        if (menu == null && paginated == null) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;
        if (event.getSlot() < 0) return;

        ClickType click = event.getClick();
        if (click == ClickType.CREATIVE ||
            click == ClickType.MIDDLE ||
            click == ClickType.SHIFT_LEFT ||
            click == ClickType.SHIFT_RIGHT ||
            click == ClickType.DOUBLE_CLICK ||
            click == ClickType.NUMBER_KEY ||
            click == ClickType.DROP ||
            click == ClickType.CONTROL_DROP ||
            click == ClickType.WINDOW_BORDER_LEFT ||
            click == ClickType.WINDOW_BORDER_RIGHT ||
            click == ClickType.UNKNOWN) {
            return;
        }

        long now = System.currentTimeMillis();
        Long last = timestamps.get(player.getUniqueId());
        if (last != null && now - last < 300L) return;
        timestamps.put(player.getUniqueId(), now);

        Button btn = null;
        if (paginated != null) {
            btn = paginated.getButtonsInRange(player).get(event.getSlot());
        } else if (menu != null) {
            if (menu.isStealable() && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                event.setCancelled(false);
                return;
            }
            btn = menu.getButtons(player).get(event.getSlot());
        }

        if (btn != null) {
            final Button fbtn = btn;
            org.bukkit.Bukkit.getScheduler().runTask(
                gg.leo.IraqueCore.IraqueCore.getInstance(),
                () -> fbtn.onClick(player, event.getSlot(), event.getClick())
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (MenuController.menus.containsKey(player.getUniqueId()) ||
            MenuController.paginatedMenus.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        MenuController.menus.remove(event.getPlayer().getUniqueId());
        MenuController.paginatedMenus.remove(event.getPlayer().getUniqueId());
    }
}
