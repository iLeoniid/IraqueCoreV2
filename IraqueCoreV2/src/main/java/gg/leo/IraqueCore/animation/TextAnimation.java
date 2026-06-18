package gg.leo.IraqueCore.animation;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class TextAnimation {

    private String text;
    private final Plugin plugin;
    private final List<String> animation;
    private final boolean animated;
    private final long ticks;
    private int index;

    public TextAnimation(Plugin plugin, FileConfiguration config, String path) {
        this.plugin = plugin;
        this.text = config.getString(path + ".text", "null");
        this.animated = config.getBoolean(path + ".animated", false);
        this.ticks = config.getLong(path + ".ticks", 10L);
        this.animation = config.getStringList(path + ".animation");
        this.index = 0;

        if (this.animated && !this.animation.isEmpty()) {
            runAnimation();
        }
    }

    private void runAnimation() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            this.text = this.animation.get(this.index);
            this.index++;
            if (this.index == this.animation.size()) this.index = 0;
        }, this.ticks, this.ticks);
    }

    public String getText() {
        return text;
    }

    public boolean isAnimated() {
        return animated;
    }

    public List<String> getAnimation() {
        return animation;
    }

    public long getTicks() {
        return ticks;
    }
}
