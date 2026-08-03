package gg.leo.IraqueCore.animation;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextBlinkAnimation extends AbstractTextAnimation {

    private final ChatColor[] colors;
    private final boolean bold;

    public TextBlinkAnimation(String text, boolean bold, ChatColor... colors) {
        super(text);
        this.bold = bold;
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
}
