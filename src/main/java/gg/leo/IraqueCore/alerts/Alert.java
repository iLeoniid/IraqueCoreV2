package gg.leo.IraqueCore.alerts;

import java.util.List;

public class Alert {

    private final String id;
    private final String title;
    private final String description;
    private final String message;
    private final SoundConfig sound;
    private final SendType sendType;
    private final boolean saveForOffline;
    private final boolean removeAfterSend;
    private final int joinDelay;

    public enum SendType {
        CHAT, ACTION_BAR, TITLE, SUBTITLE, COMBINED
    }

    public Alert(String id, String title, String description, String message,
                 SoundConfig sound, SendType sendType, boolean saveForOffline,
                 boolean removeAfterSend, int joinDelay) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.message = message;
        this.sound = sound;
        this.sendType = sendType;
        this.saveForOffline = saveForOffline;
        this.removeAfterSend = removeAfterSend;
        this.joinDelay = joinDelay;
    }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public String message() { return message; }
    public SoundConfig sound() { return sound; }
    public SendType sendType() { return sendType; }
    public boolean saveForOffline() { return saveForOffline; }
    public boolean removeAfterSend() { return removeAfterSend; }
    public int joinDelay() { return joinDelay; }

    public static class SoundConfig {
        private final String id;
        private final String category;
        private final float volume;
        private final float pitch;
        private final boolean enabled;

        public SoundConfig(String id, String category, float volume, float pitch, boolean enabled) {
            this.id = id;
            this.category = category;
            this.volume = volume;
            this.pitch = pitch;
            this.enabled = enabled;
        }

        public String id() { return id; }
        public String category() { return category; }
        public float volume() { return volume; }
        public float pitch() { return pitch; }
        public boolean enabled() { return enabled; }
    }
}
