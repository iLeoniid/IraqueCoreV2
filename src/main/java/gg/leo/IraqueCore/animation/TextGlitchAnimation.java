package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextGlitchAnimation {
    
    private final String text;
    private final ChatColor glitchColor;
    private final ChatColor normalColor;
    private final List<String> frames;
    private int index;
    private final Random random = new Random();

    // Caracteres de glitch
    private static final String GLITCH_CHARS = "▓▒░█▄▀■□▪▫▬►◄▲▼◊○●◐◑☺☻♥♦♣♠•◘○";

    public TextGlitchAnimation(String text, ChatColor normalColor, ChatColor glitchColor) {
        this.text = text;
        this.normalColor = normalColor;
        this.glitchColor = glitchColor;
        this.frames = generateFrames();
        this.index = 0;
    }

    private List<String> generateFrames() {
        List<String> result = new ArrayList<>();
        int len = text.length();

        // Frame normal
        result.add(normalColor + text);

        // Frames con glitch progresivo
        for (int glitchCount = 1; glitchCount <= len; glitchCount++) {
            StringBuilder sb = new StringBuilder();
            boolean[] glitched = new boolean[len];
            
            // Elegir posiciones aleatorias para glitch
            for (int g = 0; g < glitchCount && g < len; g++) {
                int pos;
                do { pos = random.nextInt(len); } while (glitched[pos]);
                glitched[pos] = true;
            }

            for (int i = 0; i < len; i++) {
                if (glitched[i]) {
                    sb.append(glitchColor);
                    sb.append(GLITCH_CHARS.charAt(random.nextInt(GLITCH_CHARS.length())));
                } else {
                    sb.append(normalColor).append(text.charAt(i));
                }
            }
            result.add(sb.toString());
        }

        // Frame de recuperación (parcialmente normal)
        for (int i = len - 1; i >= 0; i--) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                if (j >= i) {
                    sb.append(normalColor).append(text.charAt(j));
                } else {
                    sb.append(glitchColor);
                    sb.append(GLITCH_CHARS.charAt(random.nextInt(GLITCH_CHARS.length())));
                }
            }
            result.add(sb.toString());
        }

        return result;
    }

    public String getCurrentText() {
        return frames.isEmpty() ? text : frames.get(index);
    }

    public String nextFrame() {
        if (frames.isEmpty()) return text;
        String frame = frames.get(index);
        index = (index + 1) % frames.size();
        return frame;
    }

    public List<String> getFrames() {
        return List.copyOf(frames);
    }
}