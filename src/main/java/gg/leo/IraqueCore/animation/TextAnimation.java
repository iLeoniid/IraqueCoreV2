package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class TextAnimation {

    private final String staticText;
    private final List<String> frames;
    private final boolean animated;
    private final long ticks;
    private int index;
    private TextFadeAnimation fadeAnimation;

    public TextAnimation(Plugin plugin, FileConfiguration config, String path) {
        this.staticText = config.getString(path + ".text", "null");
        this.animated = config.getBoolean(path + ".animated", false);
        this.ticks = Math.max(config.getLong(path + ".ticks", 10L), 1L);

        boolean fade = config.getBoolean(path + ".fade", false);
        if (fade) {
            List<String> rawColors = config.getStringList(path + ".fade-colors");
            if (!rawColors.isEmpty()) {
                ChatColor[] colors = rawColors.stream()
                        .map(s -> {
                            ChatColor c = ChatColor.getByChar(s.replace("&", ""));
                            return c != null ? c : ChatColor.WHITE;
                        })
                        .toArray(ChatColor[]::new);
                this.fadeAnimation = new TextFadeAnimation(staticText, colors);
                this.frames = new ArrayList<>(fadeAnimation.getFrames());
            } else {
                this.frames = config.getStringList(path + ".animation");
            }
        } else {
            this.frames = config.getStringList(path + ".animation");
        }

        this.index = 0;
    }

    public String getCurrentText() {
        if (fadeAnimation != null) return fadeAnimation.getCurrentText();
        if (!animated || frames.isEmpty()) return staticText;
        return frames.get(index);
    }

    public String nextFrame() {
        if (fadeAnimation != null) return fadeAnimation.nextFrame();
        if (!animated || frames.isEmpty()) return staticText;
        String frame = frames.get(index);
        index = (index + 1) % frames.size();
        return frame;
    }

    public String getText() {
        return getCurrentText();
    }

    public boolean isAnimated() {
        return animated || fadeAnimation != null;
    }

    public List<String> getAnimation() {
        return frames;
    }

    public long getTicks() {
        return ticks;
    }

    public boolean isFade() {
        return fadeAnimation != null;
    }
}
