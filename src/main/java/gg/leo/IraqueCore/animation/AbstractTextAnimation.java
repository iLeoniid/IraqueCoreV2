package gg.leo.IraqueCore.animation;

import java.util.List;

public abstract class AbstractTextAnimation {

    protected final String text;
    private List<String> frames;
    private int index;

    protected AbstractTextAnimation(String text) {
        this.text = text;
    }

    protected abstract List<String> generateFrames();

    private List<String> frames() {
        if (frames == null) {
            frames = List.copyOf(generateFrames());
        }
        return frames;
    }

    public String getCurrentText() {
        List<String> f = frames();
        return f.isEmpty() ? text : f.get(index);
    }

    public String nextFrame() {
        List<String> f = frames();
        if (f.isEmpty()) return text;
        String frame = f.get(index);
        index = (index + 1) % f.size();
        return frame;
    }

    public List<String> getFrames() {
        return frames();
    }
}
