package gg.leo.IraqueCore.animation;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextWaveAnimation extends AbstractTextAnimation {

    private final ChatColor[] colors;

    public TextWaveAnimation(String text, ChatColor... colors) {
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
}
