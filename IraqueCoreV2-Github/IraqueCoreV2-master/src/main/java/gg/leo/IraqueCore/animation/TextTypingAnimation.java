package gg.leo.IraqueCore.animation;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextTypingAnimation {
    
    private final String text;
    private final ChatColor textColor;
    private final ChatColor cursorColor;
    private final List<String> frames;
    private int index;
    private final String cursor;

    public TextTypingAnimation(String text, ChatColor textColor, ChatColor cursorColor) {
        this(text, textColor, cursorColor, "▌");
    }

    public TextTypingAnimation(String text, ChatColor textColor, ChatColor cursorColor, String cursor) {
        this.text = text;
        this.textColor = textColor;
        this.cursorColor = cursorColor;
        this.cursor = cursor;
        this.frames = generateFrames();
        this.index = 0;
    }

    private List<String> generateFrames() {
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
        for (int blink = 0; blink < 4; blink++) {
            if (blink % 2 == 0) {
                result.add(textColor + text + cursorColor + cursor);
            } else {
                result.add(textColor + text);
            }
        }

        // Borrar (opcional - descomenta si quieres que se borre)
        /*
        for (int i = len; i >= 0; i--) {
            StringBuilder sb = new StringBuilder();
            sb.append(textColor);
            sb.append(text, 0, i);
            sb.append(cursorColor).append(cursor);
            result.add(sb.toString());
        }
        */

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