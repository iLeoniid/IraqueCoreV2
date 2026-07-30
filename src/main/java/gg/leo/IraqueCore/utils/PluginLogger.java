package gg.leo.IraqueCore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PluginLogger {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final String pluginName;
    private final ComponentLogger fallback;
    private final boolean debugMode;
    private final long startTime;
    private final Map<String, Long> featureStarts = new LinkedHashMap<>();
    private final List<LogEntry> entries = new ArrayList<>();

    private record LogEntry(Level level, String feature, String detail, long timeMs) {}

    public enum Level {
        SUCCESS("✅", "<gradient:#00e676:#00c853>"),
        WARNING("⚠️", "<gradient:#ffd600:#ffab00>"),
        ERROR("❌", "<gradient:#ff1744:#d50000>"),
        INFO("ℹ️", "<gradient:#40c4ff:#0091ea>"),
        DEBUG("🐛", "<#888888>");

        public final String emoji;
        public final String color;

        Level(String emoji, String color) { this.emoji = emoji; this.color = color; }
    }

    public PluginLogger(@NotNull String pluginName, @NotNull ComponentLogger fallback) {
        this(pluginName, fallback, false);
    }

    public PluginLogger(@NotNull String pluginName, @NotNull ComponentLogger fallback, boolean debugMode) {
        this.pluginName = pluginName;
        this.fallback = fallback;
        this.debugMode = debugMode;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isDebug() { return debugMode; }

    public void startFeature(String feature) {
        featureStarts.put(feature, System.currentTimeMillis());
    }

    public void success(String feature, String detail) {
        log(Level.SUCCESS, feature, detail, null);
    }

    public void warning(String feature, String detail) {
        log(Level.WARNING, feature, detail, null);
    }

    public void error(String feature, String detail, Throwable cause) {
        log(Level.ERROR, feature, detail, cause);
    }

    public void info(String feature, String detail) {
        log(Level.INFO, feature, detail, null);
    }

    public void debug(String feature, String detail) {
        if (debugMode) log(Level.DEBUG, feature, detail, null);
    }

    private void log(Level level, String feature, String detail, Throwable cause) {
        long time = 0;
        if (feature != null) {
            Long start = featureStarts.get(feature);
            if (start != null) time = System.currentTimeMillis() - start;
        }

        entries.add(new LogEntry(level, feature, detail, time));

        String timeStr = time > 0 ? " <dark_gray>(" + time + "ms)</dark_gray>" : "";
        String featureStr = feature != null ? " <gray>[" + feature + "]</gray>" : "";

        String safeDetail = detail != null
                ? detail.replaceAll("§[0-9a-fklmnor]", "")
                : "";

        String msg = level.emoji + " " +
                "<gradient:#ff6b35:#ff9a44>" + pluginName + "</gradient>:" +
                featureStr +
                " " + level.color + level.name() + "</color>" +
                " - " + safeDetail + timeStr;

        Bukkit.getConsoleSender().sendMessage(MINI.deserialize(msg));

        if (cause != null) {
            if (level == Level.ERROR) {
                Bukkit.getConsoleSender().sendMessage(MINI.deserialize(
                        "  <red>Cause: " + cause.getClass().getSimpleName() + " - " +
                                (cause.getMessage() != null ? cause.getMessage() : "No message") + "</red>"
                ));
                cause.printStackTrace();
            }
        }
    }

    public void printSummary() {
        long totalTime = System.currentTimeMillis() - startTime;
        int success = (int) entries.stream().filter(e -> e.level == Level.SUCCESS).count();
        int warnings = (int) entries.stream().filter(e -> e.level == Level.WARNING).count();
        int errors = (int) entries.stream().filter(e -> e.level == Level.ERROR).count();
        int infos = (int) entries.stream().filter(e -> e.level == Level.INFO).count();

        StringBuilder sb = new StringBuilder();
        sb.append("\n<gradient:#ff9a44:#ff6b35>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        sb.append("\n  <gradient:#ff6b35:#ff9a44>").append(pluginName)
                .append(" v").append(Bukkit.getPluginManager().getPlugin(pluginName).getPluginMeta().getVersion())
                .append("</gradient>");

        String status = errors > 0 ? " <red>✘</red>" : " <green>✔</green>";
        sb.append(status);

        sb.append("\n  <green>✅ ").append(success).append(" loaded</green>");
        if (warnings > 0) sb.append("\n  <gold>⚠️ ").append(warnings).append(" warnings</gold>");
        if (errors > 0) sb.append("\n  <red>❌ ").append(errors).append(" errors</red>");
        if (infos > 0) sb.append("\n  <aqua>ℹ️ ").append(infos).append(" infos</aqua>");
        sb.append("\n  <dark_gray>⏱ ").append(totalTime).append("ms</dark_gray>");
        sb.append("\n<gradient:#ff9a44:#ff6b35>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");

        Bukkit.getConsoleSender().sendMessage(MINI.deserialize(sb.toString()));
    }

    public void warn(String message, Object... args) {
        fallback.warn(message, args);
    }

    public void error(String message, Object... args) {
        fallback.error(message, args);
    }

    public void info(String message, Object... args) {
        fallback.info(message, args);
    }
}
