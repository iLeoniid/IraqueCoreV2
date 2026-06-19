package gg.leo.IraqueCore.animation;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class TextAnimation {

    private final String staticText;
    private final List<String> frames;
    private final boolean animated;
    private final long ticks;
    private int index;

    public TextAnimation(Plugin plugin, FileConfiguration config, String path) {
        this.staticText = config.getString(path + ".text", "null");
        this.animated = config.getBoolean(path + ".animated", false);
        this.ticks = config.getLong(path + ".ticks", 10L);
        this.frames = config.getStringList(path + ".animation");
        this.index = 0;
    }

    public String getCurrentText() {
        if (!animated || frames.isEmpty()) return staticText;
        return frames.get(index);
    }

    public String nextFrame() {
        if (!animated || frames.isEmpty()) return staticText;
        String frame = frames.get(index);
        index = (index + 1) % frames.size();
        return frame;
    }

    public String getText() {
        return getCurrentText();
    }

    public boolean isAnimated() {
        return animated;
    }

    public List<String> getAnimation() {
        return frames;
    }

    public long getTicks() {
        return ticks;
    }
}
