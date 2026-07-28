package gg.leo.IraqueCore.home;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class HomeManager {

    private final IraqueCore plugin;
    private File homeFile;
    private FileConfiguration homeConfig;

    public HomeManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        homeFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homeFile.exists()) {
            try {
                homeFile.getParentFile().mkdirs();
                homeFile.createNewFile();
            } catch (IOException e) {
                plugin.getPluginLogger().error("Could not create homes.yml", e);
            }
        }
        homeConfig = YamlConfiguration.loadConfiguration(homeFile);
    }

    public void setHome(Player player) {
        UUID id = player.getUniqueId();
        Location loc = player.getLocation();

        String path = "homes." + id;
        homeConfig.set(path + ".world", loc.getWorld().getName());
        homeConfig.set(path + ".x", loc.getX());
        homeConfig.set(path + ".y", loc.getY());
        homeConfig.set(path + ".z", loc.getZ());
        homeConfig.set(path + ".yaw", (double) loc.getYaw());
        homeConfig.set(path + ".pitch", (double) loc.getPitch());

        save();
    }

    public Location getHome(Player player) {
        UUID id = player.getUniqueId();
        String path = "homes." + id;

        if (!homeConfig.contains(path)) return null;

        String worldName = homeConfig.getString(path + ".world");
        if (worldName == null) return null;

        var world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = homeConfig.getDouble(path + ".x");
        double y = homeConfig.getDouble(path + ".y");
        double z = homeConfig.getDouble(path + ".z");
        float yaw = (float) homeConfig.getDouble(path + ".yaw");
        float pitch = (float) homeConfig.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean hasHome(Player player) {
        return homeConfig != null && homeConfig.contains("homes." + player.getUniqueId());
    }

    public void deleteHome(Player player) {
        homeConfig.set("homes." + player.getUniqueId(), null);
        save();
    }

    private void save() {
        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save homes.yml", e);
        }
    }
}
