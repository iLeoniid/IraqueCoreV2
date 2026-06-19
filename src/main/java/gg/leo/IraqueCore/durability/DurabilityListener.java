package gg.leo.IraqueCore.durability;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DurabilityListener implements Listener {

    private final IraqueCore plugin;
    private final Map<UUID, Long> lastWarning = new HashMap<>();

    private boolean enabled;
    private double threshold;

    public DurabilityListener(IraqueCore plugin) {
        this.plugin = plugin;
        var cfg = plugin.getConfig();
        enabled = cfg.getBoolean("durability-warning.enabled", true);
        threshold = cfg.getDouble("durability-warning.threshold", 0.1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        if (!(item.getItemMeta() instanceof Damageable damageable)) return;

        int maxDamage = item.getType().getMaxDurability();
        if (maxDamage <= 0) return;

        int currentDamage = damageable.getDamage();
        int remaining = maxDamage - currentDamage;

        if (remaining <= 0) return;

        double ratio = (double) remaining / maxDamage;

        if (ratio <= threshold) {
            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long last = lastWarning.get(id);
            if (last != null && (now - last) < 5000) return;
            lastWarning.put(id, now);

            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("durability.warning",
                                    "&cYour {item} is about to break! (&7{remaining}/{max}&c)")
                                    .replace("{item}", item.getType().name().toLowerCase().replace("_", " "))
                                    .replace("{remaining}", String.valueOf(remaining))
                                    .replace("{max}", String.valueOf(maxDamage)))));

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 1.2f);
        }
    }
}
