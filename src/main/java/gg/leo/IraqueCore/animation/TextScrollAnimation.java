package gg.leo.IraqueCore.animation;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextScrollAnimation extends AbstractTextAnimation {

    private final int width;
    private final ChatColor color;

    public TextScrollAnimation(String text, int width, ChatColor color) {
        super(text);
        this.width = Math.max(width, 1);
        this.color = color;
    }

    @Override
    protected List<String> generateFrames() {
        List<String> result = new ArrayList<>();
        List<String> tokens = tokenize("   " + text + "   ");
        int totalLen = tokens.size();

        for (int start = 0; start < totalLen; start++) {
            StringBuilder sb = new StringBuilder();
            sb.append(color);

            for (int i = 0; i < width; i++) {
                int pos = (start + i) % totalLen;
                sb.append(tokens.get(pos));
            }
            result.add(sb.toString());
        }

        return result;
    }

    private static List<String> tokenize(String s) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '\u00A7' || c == '&') && i + 1 < s.length()) {
                // Código hex &#RRGGBB
                if (c == '&' && i + 1 < s.length() && s.charAt(i + 1) == '#'
                        && i + 7 < s.length()) {
                    tokens.add(s.substring(i, i + 8));
                    i += 7;
                } else {
                    tokens.add(s.substring(i, i + 2));
                    i++;
                }
            } else {
                tokens.add(String.valueOf(c));
            }
        }
        return tokens;
    }
}
