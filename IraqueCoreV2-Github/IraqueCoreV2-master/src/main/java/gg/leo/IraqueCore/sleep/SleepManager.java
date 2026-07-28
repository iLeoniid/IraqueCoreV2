package gg.leo.IraqueCore.sleep;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SleepManager implements Listener {

    private final IraqueCore plugin;
    private final Set<UUID> sleeping = new HashSet<>();
    private boolean enabled;
    private double percentage;

    public SleepManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        var cfg = plugin.getConfig();
        enabled = cfg.getBoolean("sleep.enabled", true);
        percentage = cfg.getDouble("sleep.percentage", 50.0);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!enabled) return;
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;

        Player player = event.getPlayer();
        World world = player.getWorld();
        if (world.isDayTime() || !world.isClearWeather()) return;

        sleeping.add(player.getUniqueId());
        checkSleep(world, player);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        sleeping.remove(event.getPlayer().getUniqueId());
    }

    private void checkSleep(World world, Player sleeper) {
        int online = 0;
        int asleep = 0;
        for (Player p : world.getPlayers()) {
            if (p.isSleeping() || p.isSleepingIgnored() || sleeping.contains(p.getUniqueId())) {
                asleep++;
            }
            if (!p.isSleepingIgnored()) {
                online++;
            }
        }

        int needed = Math.max(1, (int) Math.ceil(online * percentage / 100.0));

        String msg = plugin.getConfigManager().getMessage("sleep.vote",
                "&e{count}/{needed} players are sleeping");
        msg = msg.replace("{count}", String.valueOf(asleep))
                .replace("{needed}", String.valueOf(needed));
        plugin.getServer().broadcast(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(msg)));

        if (asleep >= needed) {
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);
            sleeping.clear();

            String skipMsg = plugin.getConfigManager().getMessage("sleep.skipped",
                    "&6{player} &eslept and the night was skipped!");
            skipMsg = skipMsg.replace("{player}", sleeper.getName());
            plugin.getServer().broadcast(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(skipMsg)));
        }
    }
}
