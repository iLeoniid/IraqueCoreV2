package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextFadeAnimation {

    private final String text;
    private final ChatColor[] colors;
    private final List<String> frames;
    private int index;

    public TextFadeAnimation(String text, ChatColor... colors) {
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

        for (int offset = 0; offset < colorCount; offset++) {
            StringBuilder sb = new StringBuilder();
            int colorIndex = offset;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\u00A7' && i + 1 < text.length()) {
                    sb.append(c).append(text.charAt(i + 1));
                    i++;
                    continue;
                }
                sb.append(colors[colorIndex % colorCount]).append(c);
                colorIndex++;
            }
            result.add(sb.toString());
        }

        for (int offset = 1; offset < colorCount - 1; offset++) {
            StringBuilder sb = new StringBuilder();
            int colorIndex = offset;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\u00A7' && i + 1 < text.length()) {
                    sb.append(c).append(text.charAt(i + 1));
                    i++;
                    continue;
                }
                sb.append(colors[(colorCount - 1) - (colorIndex % colorCount)]).append(c);
                colorIndex++;
            }
            result.add(sb.toString());
        }

        return result;
    }

    public String getCurrentText() {
        if (frames.isEmpty()) return text;
        return frames.get(index);
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
