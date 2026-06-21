package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextScrollAnimation {
    
    private final String text;
    private final int width;
    private final ChatColor color;
    private final List<String> frames;
    private int index;

    public TextScrollAnimation(String text, int width, ChatColor color) {
        this.text = text;
        this.width = width;
        this.color = color;
        this.frames = generateFrames();
        this.index = 0;
    }

    private List<String> generateFrames() {
        List<String> result = new ArrayList<>();
        String padded = "   " + text + "   ";
        int totalLen = padded.length();

        for (int start = 0; start < totalLen; start++) {
            StringBuilder sb = new StringBuilder();
            sb.append(color);
            
            for (int i = 0; i < width; i++) {
                int pos = (start + i) % totalLen;
                sb.append(padded.charAt(pos));
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