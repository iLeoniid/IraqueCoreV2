package gg.leo.IraqueCore.animation;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextBounceAnimation extends AbstractTextAnimation {

    private final ChatColor[] colors;

    public TextBounceAnimation(String text, ChatColor... colors) {
        super(text);
        this.colors = colors;
    }

    @Override
    protected List<String> generateFrames() {
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
}
