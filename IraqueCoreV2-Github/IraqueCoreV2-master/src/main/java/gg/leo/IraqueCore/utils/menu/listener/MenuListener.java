package gg.leo.IraqueCore.utils.menu.listener;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.MenuController;
import gg.leo.IraqueCore.utils.menu.pagination.PaginatedMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuListener implements Listener {

    private static final long CLICK_COOLDOWN_MS = 200L;
    private final Map<UUID, Long> timestamps = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        Menu menu = MenuController.getMenu(uuid);
        PaginatedMenu paginated = MenuController.getPaginated(uuid);

        if (menu == null && paginated == null) return;

        boolean stealable = menu != null && menu.isStealable();
        if (!stealable) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (!stealable) event.setCancelled(true);
            return;
        }
        if (event.getSlot() < 0) return;

        // Always cancel top-inventory interactions for non-stealable menus
        event.setCancelled(true);

        ClickType click = event.getClick();
        if (isBlockedClick(click)) return;

        long now = System.currentTimeMillis();
        Long last = timestamps.get(uuid);
        if (last != null && now - last < CLICK_COOLDOWN_MS) return;
        timestamps.put(uuid, now);

        Button btn = resolveButton(menu, paginated, player, event.getSlot());
        if (btn == null) return;

        final Button clicked = btn;
        final int slot = event.getSlot();
        final ClickType type = event.getClick();

        Bukkit.getScheduler().runTask(IraqueCore.getInstance(), () -> {
            if (!player.isOnline()) return;
            clicked.onClick(player, slot, type);
        });
    }

    private Button resolveButton(Menu menu, PaginatedMenu paginated, Player player, int slot) {
        if (paginated != null) {
            Button cached = paginated.getButton(slot);
            if (cached != null) return cached;
            return paginated.getButtonsInRange(player).get(slot);
        }
        if (menu != null) {
            Button cached = menu.getButton(slot);
            if (cached != null) return cached;
            return menu.getButtons(player).get(slot);
        }
        return null;
    }

    private boolean isBlockedClick(ClickType click) {
        return switch (click) {
            case CREATIVE, MIDDLE, DOUBLE_CLICK, NUMBER_KEY,
                 SHIFT_LEFT, SHIFT_RIGHT,
                 DROP, CONTROL_DROP,
                 WINDOW_BORDER_LEFT, WINDOW_BORDER_RIGHT,
                 UNKNOWN -> true;
            default -> false;
        };
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (MenuController.hasOpenMenu(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (MenuController.isUpdating(uuid)) return;
        MenuController.unregister(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        timestamps.remove(uuid);
        MenuController.clear(uuid);
    }
}
