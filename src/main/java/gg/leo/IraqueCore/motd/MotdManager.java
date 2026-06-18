package gg.leo.IraqueCore.motd;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.animation.TextAnimation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MotdManager {

    private final IraqueCore plugin;
    private TextAnimation animation;
    private String staticText;
    private boolean animated;
    private File motdFile;
    private FileConfiguration motdConfig;

    public MotdManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        motdFile = new File(plugin.getDataFolder(), "motd.yml");
        if (!motdFile.exists()) {
            plugin.saveResource("motd.yml", false);
        }
        motdConfig = YamlConfiguration.loadConfiguration(motdFile);

        staticText = motdConfig.getString("motd.text", "&6Welcome!");
        animated = motdConfig.getBoolean("motd.animated", false);
        long ticks = motdConfig.getLong("motd.ticks", 40L);

        if (animated) {
            animation = new TextAnimation(plugin, motdConfig, "motd");
        }

        setMotd(getCurrentText());

        if (animated) {
            startMotdTask(ticks);
        }
    }

    public void reload() {
        if (motdFile != null) {
            motdConfig = YamlConfiguration.loadConfiguration(motdFile);
            staticText = motdConfig.getString("motd.text", "&6Welcome!");
            animated = motdConfig.getBoolean("motd.animated", false);
            long ticks = motdConfig.getLong("motd.ticks", 40L);

            if (animated) {
                animation = new TextAnimation(plugin, motdConfig, "motd");
            } else {
                animation = null;
            }

            setMotd(getCurrentText());
        }
    }

    private String getCurrentText() {
        if (animated && animation != null) {
            return animation.getText();
        }
        return staticText;
    }

    private void setMotd(String text) {
        Component component = plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(text));
        Bukkit.getServer().motd(component);
    }

    private void startMotdTask(long ticks) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (animation != null) {
                setMotd(animation.getText());
            }
        }, ticks, ticks);
    }
}
