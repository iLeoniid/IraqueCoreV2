package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextWaveAnimation {
    
    private final String text;
    private final ChatColor[] colors;
    private final List<String> frames;
    private int index;

    public TextWaveAnimation(String text, ChatColor... colors) {
        this.text = text;
        this.colors = colors;
        this.frames = generateFrames();
        this.index = 0;
    }

    private List<String> generateFrames() {
        List<String> result = new ArrayList<>();
        int colorCount = colors.length;
        if (colorCount == 0) {
            result.add(text);
            return result;
        }

        // Onda que se mueve de izquierda a derecha
        for (int offset = 0; offset < colorCount * 2; offset++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                // Saltar códigos de color existentes
                if (c == '\u00A7' && i + 1 < text.length()) {
                    sb.append(c).append(text.charAt(i + 1));
                    i++;
                    continue;
                }
                // Solo colorear caracteres no espaciales
                if (c != ' ') {
                    int colorIndex = (offset + i) % colorCount;
                    sb.append(colors[colorIndex]).append(c);
                } else {
                    sb.append(c);
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