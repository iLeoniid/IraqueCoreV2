package gg.leo.IraqueCore.grave;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GraveListener implements Listener {

    private final IraqueCore plugin;

    public GraveListener(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (Boolean.TRUE.equals(player.getWorld().getGameRuleValue(org.bukkit.GameRule.KEEP_INVENTORY))) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        if (drops.isEmpty()) return;

        Location loc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        loc.getBlock().setType(Material.CHEST);

        if (loc.getBlock().getState() instanceof Chest chest) {
            var inv = chest.getBlockInventory();
            int slot = 0;
            for (ItemStack item : drops) {
                if (slot >= inv.getSize()) break;
                inv.setItem(slot++, item);
            }
        }

        event.getDrops().clear();

        plugin.getServer().broadcast(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("grave.created",
                                "&6{player} &edied. Items are in a grave at &6{x}, {y}, {z}")
                                .replace("{player}", player.getName())
                                .replace("{x}", String.valueOf(loc.getBlockX()))
                                .replace("{y}", String.valueOf(loc.getBlockY()))
                                .replace("{z}", String.valueOf(loc.getBlockZ())))));
    }
}
