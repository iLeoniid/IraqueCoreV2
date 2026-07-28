package gg.leo.IraqueCore.home;

import gg.leo.IraqueCore.IraqueCore;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class HomeManager {
    private final IraqueCore plugin;
    private File homeFile;
    private FileConfiguration homeConfig;

    public HomeManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.homeFile = new File(this.plugin.getDataFolder(), "homes.yml");
        if (!this.homeFile.exists()) {
            try {
                this.homeFile.getParentFile().mkdirs();
                this.homeFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getPluginLogger().error("Could not create homes.yml", e);
            }
        }
        this.homeConfig = YamlConfiguration.loadConfiguration(this.homeFile);
    }

    public void setHome(Player player) {
        UUID id = player.getUniqueId();
        Location loc = player.getLocation();
        String path = "homes." + id;
        this.homeConfig.set(path + ".world", loc.getWorld().getName());
        this.homeConfig.set(path + ".x", loc.getX());
        this.homeConfig.set(path + ".y", loc.getY());
        this.homeConfig.set(path + ".z", loc.getZ());
        this.homeConfig.set(path + ".yaw", loc.getYaw());
        this.homeConfig.set(path + ".pitch", loc.getPitch());
        this.save();
    }

    public Location getHome(Player player) {
        UUID id = player.getUniqueId();
        String path = "homes." + id;
        if (!this.homeConfig.contains(path)) {
            return null;
        }
        String worldName = this.homeConfig.getString(path + ".world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        double x = this.homeConfig.getDouble(path + ".x");
        double y = this.homeConfig.getDouble(path + ".y");
        double z = this.homeConfig.getDouble(path + ".z");
        float yaw = (float)this.homeConfig.getDouble(path + ".yaw");
        float pitch = (float)this.homeConfig.getDouble(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean hasHome(Player player) {
        return this.homeConfig != null && this.homeConfig.contains("homes." + player.getUniqueId());
    }

    public void deleteHome(Player player) {
        this.homeConfig.set("homes." + player.getUniqueId(), null);
        this.save();
    }

    private void save() {
        try {
            this.homeConfig.save(this.homeFile);
        }
        catch (IOException e) {
            this.plugin.getPluginLogger().error("Failed to save homes.yml", e);
        }
    }
}
