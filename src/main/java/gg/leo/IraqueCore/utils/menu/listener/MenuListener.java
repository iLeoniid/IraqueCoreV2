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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MenuListener implements Listener {

    private final Map<UUID, Long> timestamps = new HashMap<>();
    // Jugadores que están en medio de un update de menú (no borrar su registro en onClose)
    private final Set<UUID> updating = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Menu menu = MenuController.menus.get(player.getUniqueId());
        var paginated = MenuController.paginatedMenus.get(player.getUniqueId());

        if (menu == null && paginated == null) return;

        // CANCELAR INMEDIATAMENTE
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

        // Anti-spam (300ms)
        long now = System.currentTimeMillis();
        Long last = timestamps.get(player.getUniqueId());
        if (last != null && now - last < 300L) return;
        timestamps.put(player.getUniqueId(), now);

        // Marcar que estamos en medio de un update
        updating.add(player.getUniqueId());

        Button btn = null;
        if (paginated != null) {
            btn = paginated.getButtonsInRange(player).get(event.getSlot());
        } else if (menu != null) {
            btn = menu.getButtons(player).get(event.getSlot());
        }

        if (btn != null) {
            final Button fbtn = btn;
            org.bukkit.Bukkit.getScheduler().runTask(
                gg.leo.IraqueCore.IraqueCore.getInstance(),
                () -> {
                    fbtn.onClick(player, event.getSlot(), event.getClick());
                    // Después de ejecutar el click, remover el flag de update
                    // (el updateMenu() ya debería haber terminado)
                    org.bukkit.Bukkit.getScheduler().runTaskLater(
                        gg.leo.IraqueCore.IraqueCore.getInstance(),
                        () -> updating.remove(player.getUniqueId()),
                        2L
                    );
                }
            );
        } else {
            updating.remove(player.getUniqueId());
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
        UUID uuid = event.getPlayer().getUniqueId();
        
        // Si el jugador está en medio de un update de menú, NO borrar el registro
        // porque el nuevo menú se va a abrir inmediatamente después
        if (updating.contains(uuid)) {
            return;
        }

        MenuController.menus.remove(uuid);
        MenuController.paginatedMenus.remove(uuid);
    }
}