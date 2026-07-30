package gg.leo.IraqueCore.alerts;

public class Alert {

    private final String id;
    private final String title;
    private final String description;
    private final SoundConfig sound;

    public Alert(String id, String title, String description, SoundConfig sound) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sound = sound;
    }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public SoundConfig sound() { return sound; }

    public static class SoundConfig {
        private final String id;
        private final float volume;
        private final float pitch;
        private final String source;
        private final boolean enabled;

        public SoundConfig(String id, float volume, float pitch, String source, boolean enabled) {
            this.id = id;
            this.volume = volume;
            this.pitch = pitch;
            this.source = source;
            this.enabled = enabled;
        }

        public String id() { return id; }
        public float volume() { return volume; }
        public float pitch() { return pitch; }
        public String source() { return source; }
        public boolean enabled() { return enabled; }
    }
}
