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
    private TextWaveAnimation waveAnimation;
    private TextBlinkAnimation blinkAnimation;
    private TextGlitchAnimation glitchAnimation;
    private TextTypingAnimation typingAnimation;
    private TextBounceAnimation bounceAnimation;
    private TextScrollAnimation scrollAnimation;

    // Enum para tipos de animación
    public enum AnimationType {
        NONE, FADE, WAVE, BLINK, GLITCH, TYPING, BOUNCE, SCROLL
    }

    public TextAnimation(Plugin plugin, FileConfiguration config, String path) {
        this.staticText = config.getString(path + ".text", "null");
        this.animated = config.getBoolean(path + ".animated", false);
        this.ticks = Math.max(config.getLong(path + ".ticks", 10L), 1L);

        String typeStr = config.getString(path + ".type", "fade").toLowerCase();
        AnimationType type = AnimationType.valueOf(typeStr.toUpperCase());

        switch (type) {
            case FADE -> {
                boolean fade = config.getBoolean(path + ".fade", false);
                if (fade) {
                    List<String> rawColors = config.getStringList(path + ".fade-colors");
                    if (!rawColors.isEmpty()) {
                        ChatColor[] colors = parseColors(rawColors);
                        this.fadeAnimation = new TextFadeAnimation(staticText, colors);
                        this.frames = new ArrayList<>(fadeAnimation.getFrames());
                    } else {
                        this.frames = config.getStringList(path + ".animation");
                    }
                } else {
                    this.frames = config.getStringList(path + ".animation");
                }
            }
            case WAVE -> {
                List<String> rawColors = config.getStringList(path + ".wave-colors");
                ChatColor[] colors = parseColors(rawColors);
                this.waveAnimation = new TextWaveAnimation(staticText, colors);
                this.frames = new ArrayList<>(waveAnimation.getFrames());
            }
            case BLINK -> {
                boolean bold = config.getBoolean(path + ".blink-bold", false);
                List<String> rawColors = config.getStringList(path + ".blink-colors");
                ChatColor[] colors = parseColors(rawColors);
                this.blinkAnimation = new TextBlinkAnimation(staticText, bold, colors);
                this.frames = new ArrayList<>(blinkAnimation.getFrames());
            }
            case GLITCH -> {
                ChatColor normal = parseColor(config.getString(path + ".glitch-normal", "&f"));
                ChatColor glitch = parseColor(config.getString(path + ".glitch-color", "&c"));
                this.glitchAnimation = new TextGlitchAnimation(staticText, normal, glitch);
                this.frames = new ArrayList<>(glitchAnimation.getFrames());
            }
            case TYPING -> {
                ChatColor textColor = parseColor(config.getString(path + ".typing-color", "&f"));
                ChatColor cursorColor = parseColor(config.getString(path + ".typing-cursor-color", "&7"));
                String cursor = config.getString(path + ".typing-cursor", "▌");
                this.typingAnimation = new TextTypingAnimation(staticText, textColor, cursorColor, cursor);
                this.frames = new ArrayList<>(typingAnimation.getFrames());
            }
            case BOUNCE -> {
                List<String> rawColors = config.getStringList(path + ".bounce-colors");
                ChatColor[] colors = parseColors(rawColors);
                this.bounceAnimation = new TextBounceAnimation(staticText, colors);
                this.frames = new ArrayList<>(bounceAnimation.getFrames());
            }
            case SCROLL -> {
                int width = config.getInt(path + ".scroll-width", 16);
                ChatColor color = parseColor(config.getString(path + ".scroll-color", "&f"));
                this.scrollAnimation = new TextScrollAnimation(staticText, width, color);
                this.frames = new ArrayList<>(scrollAnimation.getFrames());
            }
            default -> this.frames = config.getStringList(path + ".animation");
        }

        this.index = 0;
    }

    private ChatColor[] parseColors(List<String> rawColors) {
        if (rawColors == null || rawColors.isEmpty()) {
            return new ChatColor[]{ChatColor.WHITE};
        }
        return rawColors.stream()
                .map(this::parseColor)
                .toArray(ChatColor[]::new);
    }

    private ChatColor parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) return ChatColor.WHITE;
        ChatColor c = ChatColor.getByChar(colorStr.replace("&", "").replace("§", "").charAt(0));
        return c != null ? c : ChatColor.WHITE;
    }

    public String getCurrentText() {
        if (fadeAnimation != null) return fadeAnimation.getCurrentText();
        if (waveAnimation != null) return waveAnimation.getCurrentText();
        if (blinkAnimation != null) return blinkAnimation.getCurrentText();
        if (glitchAnimation != null) return glitchAnimation.getCurrentText();
        if (typingAnimation != null) return typingAnimation.getCurrentText();
        if (bounceAnimation != null) return bounceAnimation.getCurrentText();
        if (scrollAnimation != null) return scrollAnimation.getCurrentText();
        if (!animated || frames.isEmpty()) return staticText;
        return frames.get(index);
    }

    public String nextFrame() {
        if (fadeAnimation != null) return fadeAnimation.nextFrame();
        if (waveAnimation != null) return waveAnimation.nextFrame();
        if (blinkAnimation != null) return blinkAnimation.nextFrame();
        if (glitchAnimation != null) return glitchAnimation.nextFrame();
        if (typingAnimation != null) return typingAnimation.nextFrame();
        if (bounceAnimation != null) return bounceAnimation.nextFrame();
        if (scrollAnimation != null) return scrollAnimation.nextFrame();
        if (!animated || frames.isEmpty()) return staticText;
        String frame = frames.get(index);
        index = (index + 1) % frames.size();
        return frame;
    }

    public String getText() {
        return getCurrentText();
    }

    public boolean isAnimated() {
        return animated || fadeAnimation != null || waveAnimation != null 
            || blinkAnimation != null || glitchAnimation != null 
            || typingAnimation != null || bounceAnimation != null 
            || scrollAnimation != null;
    }

    public long getTicks() {
        return ticks;
    }
}
