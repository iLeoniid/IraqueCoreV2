package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextBlinkAnimation {
    
    private final String text;
    private final ChatColor[] colors;
    private final List<String> frames;
    private int index;
    private final boolean bold;

    public TextBlinkAnimation(String text, boolean bold, ChatColor... colors) {
        this.text = text;
        this.colors = colors;
        this.bold = bold;
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

        // Cada color se muestra como frame completo (parpadeo)
        for (ChatColor color : colors) {
            StringBuilder sb = new StringBuilder();
            sb.append(color);
            if (bold) sb.append(ChatColor.BOLD);
            sb.append(text);
            result.add(sb.toString());
        }

        // Frame con texto invisible (parpadeo completo)
        result.add(ChatColor.RESET + ChatColor.BLACK.toString() + text);

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