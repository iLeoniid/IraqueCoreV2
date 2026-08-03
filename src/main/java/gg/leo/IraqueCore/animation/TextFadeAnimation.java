package gg.leo.IraqueCore.animation;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextFadeAnimation extends AbstractTextAnimation {

    private final ChatColor[] colors;

    public TextFadeAnimation(String text, ChatColor... colors) {
        super(text);
        this.colors = colors;
    }

    @Override
    protected List<String> generateFrames() {
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
}
