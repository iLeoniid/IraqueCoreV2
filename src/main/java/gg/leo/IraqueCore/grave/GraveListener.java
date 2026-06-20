package gg.leo.IraqueCore.grave;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraveListener implements Listener {

    private final IraqueCore plugin;
    private final Map<Location, ArmorStand> graves = new HashMap<>();

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

        ArmorStand hologram = (ArmorStand) loc.getWorld().spawnEntity(
                loc.getBlock().getLocation().add(0.5, 1.3, 0.5), EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setMarker(true);
        hologram.setCustomNameVisible(true);
        hologram.setCustomName(ItemBuilder.color(
                plugin.getConfigManager().getMessage("grave.hologram", "&6Cadaver de &e{player}")
                        .replace("{player}", player.getName())));

        graves.put(loc.getBlock().getLocation(), hologram);

        plugin.getServer().broadcast(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("grave.created",
                                "&6{player} &edied. Items are in a grave at &6{x}, {y}, {z}")
                                .replace("{player}", player.getName())
                                .replace("{x}", String.valueOf(loc.getBlockX()))
                                .replace("{y}", String.valueOf(loc.getBlockY()))
                                .replace("{z}", String.valueOf(loc.getBlockZ())))));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        checkEmptyAndRemove(event.getInventory());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        checkEmptyAndRemove(event.getInventory());
    }

    private void checkEmptyAndRemove(org.bukkit.inventory.Inventory inv) {
        if (inv.getLocation() == null) return;
        Location chestLoc = inv.getLocation().getBlock().getLocation();
        if (!graves.containsKey(chestLoc)) return;

        if (isEmpty(inv)) {
            removeGrave(chestLoc);
        }
    }

    private boolean isEmpty(org.bukkit.inventory.Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) return false;
        }
        return true;
    }

    private void removeGrave(Location loc) {
        Block block = loc.getBlock();
        if (block.getType() == Material.CHEST) {
            block.setType(Material.AIR);
        }
        ArmorStand holo = graves.remove(loc);
        if (holo != null) {
            holo.remove();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        ArmorStand holo = graves.remove(loc);
        if (holo != null) {
            holo.remove();
        }
    }
}
