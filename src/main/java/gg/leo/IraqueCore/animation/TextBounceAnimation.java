package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextBounceAnimation {
    
    private final String text;
    private final ChatColor[] colors;
    private final List<String> frames;
    private int index;

    public TextBounceAnimation(String text, ChatColor... colors) {
        this.text = text;
        this.colors = colors;
        this.frames = generateFrames();
        this.index = 0;
    }

    private List<String> generateFrames() {
        List<String> result = new ArrayList<>();
        int len = text.length();
        int colorCount = colors.length;
        if (colorCount == 0) {
            result.add(text);
            return result;
        }

        // La "ola" rebota de izquierda a derecha
        for (int wavePos = 0; wavePos < len * 2 - 2; wavePos++) {
            StringBuilder sb = new StringBuilder();
            int actualPos = wavePos < len ? wavePos : len * 2 - 2 - wavePos;
            
            for (int i = 0; i < len; i++) {
                char c = text.charAt(i);
                if (c == '\u00A7' && i + 1 < text.length()) {
                    sb.append(c).append(text.charAt(i + 1));
                    i++;
                    continue;
                }
                
                // La letra en la posición de la ola está resaltada
                if (i == actualPos) {
                    sb.append(ChatColor.BOLD);
                    int colorIndex = i % colorCount;
                    sb.append(colors[colorIndex]).append(c).append(ChatColor.RESET);
                } else if (Math.abs(i - actualPos) == 1) {
                    // Letras adyacentes con color diferente
                    int colorIndex = (i + 1) % colorCount;
                    sb.append(colors[colorIndex]).append(c);
                } else {
                    sb.append(ChatColor.GRAY).append(c);
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