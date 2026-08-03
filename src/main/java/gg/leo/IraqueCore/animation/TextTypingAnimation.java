package gg.leo.IraqueCore.animation;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextTypingAnimation extends AbstractTextAnimation {

    private static final int CURSOR_BLINKS = 4;

    private final ChatColor textColor;
    private final ChatColor cursorColor;
    private final String cursor;
    private final boolean erase;

    public TextTypingAnimation(String text, ChatColor textColor, ChatColor cursorColor) {
        this(text, textColor, cursorColor, "▌", false);
    }

    public TextTypingAnimation(String text, ChatColor textColor, ChatColor cursorColor, String cursor) {
        this(text, textColor, cursorColor, cursor, false);
    }

    public TextTypingAnimation(String text, ChatColor textColor, ChatColor cursorColor, String cursor, boolean erase) {
        super(text);
        this.textColor = textColor;
        this.cursorColor = cursorColor;
        this.cursor = cursor;
        this.erase = erase;
    }

    @Override
    protected List<String> generateFrames() {
        List<String> result = new ArrayList<>();
        int len = text.length();

        // Escribir letra por letra
        for (int i = 0; i <= len; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(textColor);
            sb.append(text, 0, i);
            sb.append(cursorColor).append(cursor);
            result.add(sb.toString());
        }

        // Cursor parpadeando al final
        for (int blink = 0; blink < CURSOR_BLINKS; blink++) {
            if (blink % 2 == 0) {
                result.add(textColor + text + cursorColor + cursor);
            } else {
                result.add(textColor + text);
            }
        }

        // Borrar el texto letra por letra (opcional)
        if (erase) {
            for (int i = len; i >= 0; i--) {
                StringBuilder sb = new StringBuilder();
                sb.append(textColor);
                sb.append(text, 0, i);
                sb.append(cursorColor).append(cursor);
                result.add(sb.toString());
            }
        }

        return result;
    }
}
