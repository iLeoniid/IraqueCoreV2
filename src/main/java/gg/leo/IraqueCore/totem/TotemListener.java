package gg.leo.IraqueCore.totem;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

import java.time.Duration;

public class TotemListener implements Listener {

    private final IraqueCore plugin;
    private boolean enabled;

    public TotemListener(IraqueCore plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("totem-notification.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTotem(EntityResurrectEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Player player)) return;

        String playerName = player.getName();

        String broadcastMsg = plugin.getConfigManager().getMessage("totem.broadcast",
                "&c&lTOTEM! &7{player} used a totem of undying!")
                .replace("{player}", playerName);

        Bukkit.broadcast(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(broadcastMsg)));

        player.showTitle(Title.title(
                plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate("&c&lTOTEM!")),
                plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate("&7You survived!")),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofSeconds(1))
        ));

        player.playSound(player, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.5f);
    }
}
