package gg.leo.IraqueCore.animation;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;

public class TextAnimation {

    // Enum para tipos de animación
    public enum AnimationType {
        NONE, FADE, WAVE, BLINK, GLITCH, TYPING, BOUNCE, SCROLL
    }

    private final String staticText;
    private final AbstractTextAnimation animation;
    private final List<String> manualFrames;
    private final boolean animated;
    private final long ticks;
    private int index;

    public TextAnimation(Plugin plugin, FileConfiguration config, String path) {
        this.staticText = config.getString(path + ".text", "");
        this.animated = config.getBoolean(path + ".animated", false);
        this.ticks = Math.max(config.getLong(path + ".ticks", 10L), 1L);

        String typeStr = config.getString(path + ".type", "fade");
        AnimationType type;
        try {
            type = AnimationType.valueOf(typeStr.toUpperCase(Locale.ROOT).trim());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tipo de animación inválido en " + path + ": " + typeStr + ". Usando NONE.");
            type = AnimationType.NONE;
        }

        AbstractTextAnimation built = null;
        List<String> manual = null;
        switch (type) {
            case FADE -> {
                boolean fade = config.getBoolean(path + ".fade", false);
                List<String> rawColors = config.getStringList(path + ".fade-colors");
                if (fade && !rawColors.isEmpty()) {
                    built = new TextFadeAnimation(staticText, parseColors(rawColors));
                } else {
                    manual = config.getStringList(path + ".animation");
                }
            }
            case WAVE -> built = new TextWaveAnimation(staticText,
                    parseColors(config.getStringList(path + ".wave-colors")));
            case BLINK -> built = new TextBlinkAnimation(staticText,
                    config.getBoolean(path + ".blink-bold", false),
                    parseColors(config.getStringList(path + ".blink-colors")));
            case GLITCH -> built = new TextGlitchAnimation(staticText,
                    parseColor(config.getString(path + ".glitch-normal", "&f")),
                    parseColor(config.getString(path + ".glitch-color", "&c")));
            case TYPING -> built = new TextTypingAnimation(staticText,
                    parseColor(config.getString(path + ".typing-color", "&f")),
                    parseColor(config.getString(path + ".typing-cursor-color", "&7")),
                    config.getString(path + ".typing-cursor", "▌"),
                    config.getBoolean(path + ".typing-erase", false));
            case BOUNCE -> built = new TextBounceAnimation(staticText,
                    parseColors(config.getStringList(path + ".bounce-colors")));
            case SCROLL -> built = new TextScrollAnimation(staticText,
                    config.getInt(path + ".scroll-width", 16),
                    parseColor(config.getString(path + ".scroll-color", "&f")));
            default -> manual = config.getStringList(path + ".animation");
        }

        this.animation = built;
        this.manualFrames = manual == null ? List.of() : List.copyOf(manual);
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
        String s = colorStr.trim();
        // Soporte de colores hex (#RRGGBB o &#RRGGBB)
        if (s.startsWith("&#") && s.length() == 8) {
            try {
                return ChatColor.of(s.substring(1));
            } catch (IllegalArgumentException ignored) { /* fallback */ }
        }
        if (s.startsWith("#") && s.length() == 7) {
            try {
                return ChatColor.of(s);
            } catch (IllegalArgumentException ignored) { /* fallback */ }
        }
        ChatColor c = ChatColor.getByChar(s.replace("&", "").replace("§", "").charAt(0));
        return c != null ? c : ChatColor.WHITE;
    }

    public String getCurrentText() {
        if (animation != null) return animation.getCurrentText();
        if (!animated || manualFrames.isEmpty()) return staticText;
        return manualFrames.get(index);
    }

    public String nextFrame() {
        if (animation != null) return animation.nextFrame();
        if (!animated || manualFrames.isEmpty()) return staticText;
        String frame = manualFrames.get(index);
        index = (index + 1) % manualFrames.size();
        return frame;
    }

    public boolean isAnimated() {
        return animated || animation != null;
    }

    public long getTicks() {
        return ticks;
    }
}
