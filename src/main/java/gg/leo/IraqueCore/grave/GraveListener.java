package gg.leo.IraqueCore.grave;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraveListener implements Listener {

    private final IraqueCore plugin;
    private final Set<Location> graves = new HashSet<>();

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

        Block above = loc.getBlock().getRelative(BlockFace.UP);
        above.setType(Material.OAK_WALL_SIGN);
        if (above.getState() instanceof Sign sign) {
            if (sign.getBlockData() instanceof Directional dir) {
                dir.setFacing(getPlayerFacing(player));
                sign.setBlockData(dir);
            }
            sign.setLine(0, "§6Cadaver de");
            sign.setLine(1, "§e" + player.getName());
            sign.setWaxed(true);
            sign.update(true, false);
        }

        graves.add(loc.getBlock().getLocation());

        plugin.getServer().broadcast(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("grave.created",
                                "&6{player} &edied. Items are in a grave at &6{x}, {y}, {z}")
                                .replace("{player}", player.getName())
                                .replace("{x}", String.valueOf(loc.getBlockX()))
                                .replace("{y}", String.valueOf(loc.getBlockY()))
                                .replace("{z}", String.valueOf(loc.getBlockZ())))));
    }

    private BlockFace getPlayerFacing(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;
        if (yaw >= 315 || yaw < 45) return BlockFace.SOUTH;
        if (yaw >= 45 && yaw < 135) return BlockFace.WEST;
        if (yaw >= 135 && yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
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
        if (!graves.contains(chestLoc)) return;

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
        Block above = loc.getBlock().getRelative(BlockFace.UP);
        if (above.getState() instanceof Sign) {
            above.setType(Material.AIR);
        }
        graves.remove(loc);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (graves.contains(loc)) {
            Block above = loc.getBlock().getRelative(BlockFace.UP);
            if (above.getState() instanceof Sign) {
                above.setType(Material.AIR);
            }
            graves.remove(loc);
        }
    }
}
