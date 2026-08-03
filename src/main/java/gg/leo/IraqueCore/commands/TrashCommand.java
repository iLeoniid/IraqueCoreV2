package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrashCommand implements Listener {

    private final IraqueCore plugin;
    private final Set<UUID> trashViewers = new HashSet<>();

    public TrashCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void openTrash(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54,
                plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(
                                plugin.getConfigManager().getMessage("trash.title", "&cTrash"))));
        trashViewers.add(player.getUniqueId());
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player
                && trashViewers.remove(player.getUniqueId())) {
            event.getInventory().clear();
        }
    }
}
