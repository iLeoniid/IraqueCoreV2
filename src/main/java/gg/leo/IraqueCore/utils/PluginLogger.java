package gg.leo.IraqueCore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logger de consola con estilo (MiniMessage / Adventure) para plugins de Bukkit/Paper.
 *
 * <p>Características principales:</p>
 * <ul>
 *     <li>Thread-safe: puede usarse desde tareas async sin problema.</li>
 *     <li>Sin inyección de MiniMessage: el texto dinámico (detail) nunca se interpreta
 *         como markup, se renderiza como texto plano literal.</li>
 *     <li>Timers de features con soporte para anidamiento y try-with-resources.</li>
 *     <li>Filtro por nivel mínimo (para no imprimir DEBUG en producción, por ejemplo).</li>
 *     <li>Logging opcional a archivo (texto plano, sin tags de color).</li>
 *     <li>Resumen final con desglose de features más lentas.</li>
 * </ul>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>{@code
 * PluginLogger log = PluginLogger.builder(this)
 *         .debug(config.getBoolean("debug"))
 *         .minLevel(PluginLogger.Level.INFO)
 *         .logToFile(true)
 *         .build();
 *
 * try (var timer = log.timeFeature("database-connect")) {
 *     connectToDatabase();
 *     log.success("database-connect", "Conectado correctamente");
 * } catch (Exception e) {
 *     log.error("database-connect", "No se pudo conectar", e);
 * }
 *
 * log.printSummary();
 * }</pre>
 */
public final class PluginLogger {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public enum Level {
        DEBUG(0, "🐛", "<#888888>"),
        INFO(1, "ℹ️", "<gradient:#40c4ff:#0091ea>"),
        SUCCESS(2, "✅", "<gradient:#00e676:#00c853>"),
        WARNING(3, "⚠️", "<gradient:#ffd600:#ffab00>"),
        ERROR(4, "❌", "<gradient:#ff1744:#d50000>");

        final int severity;
        public final String emoji;
        public final String color;

        Level(int severity, String emoji, String color) {
            this.severity = severity;
            this.emoji = emoji;
            this.color = color;
        }
    }

    private record LogEntry(Level level, String feature, String detail, long elapsedMs, LocalDateTime at) {}

    private final String pluginName;
    private final String pluginVersion;
    private final ComponentLogger fallback;
    private final boolean debugMode;
    private final Level minLevel;
    private final boolean logToFile;
    private final Path logFile;

    private final long startTime = System.currentTimeMillis();
    private final Map<String, Long> featureStarts = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<LogEntry> entries = new ConcurrentLinkedQueue<>();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger warningCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();
    private final AtomicInteger infoCount = new AtomicInteger();

    private PluginLogger(Builder b) {
        this.pluginName = b.pluginName;
        this.pluginVersion = b.pluginVersion;
        this.fallback = b.fallback;
        this.debugMode = b.debugMode;
        this.minLevel = b.minLevel;
        this.logToFile = b.logToFile;
        this.logFile = b.logFile;

        if (logToFile && logFile != null) {
            try {
                Files.createDirectories(logFile.getParent());
            } catch (IOException e) {
                fallback.warn("No se pudo crear el directorio de logs: {}", e.getMessage());
            }
        }
    }

    // Builder

    public static Builder builder(@NotNull Plugin plugin) {
        return new Builder(plugin);
    }

    /** @deprecated usar {@link #builder(Plugin)} para configuración completa. */
    @Deprecated
    public PluginLogger(@NotNull String pluginName, @NotNull ComponentLogger fallback) {
        this(pluginName, fallback, false);
    }

    /** @deprecated usar {@link #builder(Plugin)} para configuración completa. */
    @Deprecated
    public PluginLogger(@NotNull String pluginName, @NotNull ComponentLogger fallback, boolean debugMode) {
        this.pluginName = pluginName;
        this.pluginVersion = "";
        this.fallback = fallback;
        this.debugMode = debugMode;
        this.minLevel = debugMode ? Level.DEBUG : Level.INFO;
        this.logToFile = false;
        this.logFile = null;
    }

    public static final class Builder {
        private final String pluginName;
        private final String pluginVersion;
        private final ComponentLogger fallback;
        private boolean debugMode = false;
        private Level minLevel = Level.INFO;
        private boolean logToFile = false;
        private Path logFile;

        private Builder(Plugin plugin) {
            this.pluginName = plugin.getPluginMeta().getName();
            this.pluginVersion = plugin.getPluginMeta().getVersion();
            this.fallback = plugin.getComponentLogger();
            this.logFile = plugin.getDataFolder().toPath()
                    .resolve("logs")
                    .resolve(pluginName + "-" + LocalDateTime.now().format(FILE_FMT) + ".log");
        }

        public Builder debug(boolean debug) {
            this.debugMode = debug;
            if (debug) this.minLevel = Level.DEBUG;
            return this;
        }

        public Builder minLevel(Level level) {
            this.minLevel = level;
            return this;
        }

        public Builder logToFile(boolean enabled) {
            this.logToFile = enabled;
            return this;
        }

        public Builder logFilePath(Path path) {
            this.logFile = path;
            return this;
        }

        public PluginLogger build() {
            return new PluginLogger(this);
        }
    }

    // API pública de logging

    public boolean isDebug() {
        return debugMode;
    }

    /** Marca el inicio de una feature para medir su duración en el próximo log asociado. */
    public void startFeature(String feature) {
        featureStarts.put(feature, System.currentTimeMillis());
    }

    /**
     * Inicia un timer que se cierra automáticamente con try-with-resources,
     * ideal para medir bloques de código sin olvidarse de parar el timer.
     *
     * <pre>{@code
     * try (var timer = log.timeFeature("world-load")) {
     *     loadWorld();
     * }
     * }</pre>
     */
    public FeatureTimer timeFeature(String feature) {
        startFeature(feature);
        return () -> featureStarts.remove(feature);
    }

    public interface FeatureTimer extends AutoCloseable {
        @Override
        void close();
    }

    public void success(String feature, String detail) {
        log(Level.SUCCESS, feature, detail, null);
    }

    public void warning(String feature, String detail) {
        log(Level.WARNING, feature, detail, null);
    }

    public void error(String feature, String detail) {
        log(Level.ERROR, feature, detail, null);
    }

    public void error(String feature, String detail, @Nullable Throwable cause) {
        log(Level.ERROR, feature, detail, cause);
    }

    public void info(String feature, String detail) {
        log(Level.INFO, feature, detail, null);
    }

    public void debug(String feature, String detail) {
        if (debugMode) log(Level.DEBUG, feature, detail, null);
    }

    // Núcleo

    private void log(Level level, String feature, String detail, @Nullable Throwable cause) {
        if (level.severity < minLevel.severity) return;

        long elapsed = 0;
        if (feature != null) {
            Long start = featureStarts.remove(feature);
            if (start != null) elapsed = System.currentTimeMillis() - start;
        }

        LocalDateTime now = LocalDateTime.now();
        entries.add(new LogEntry(level, feature, detail, elapsed, now));
        countFor(level).incrementAndGet();

        Component prefix = MINI.deserialize(
                level.emoji + " <gradient:#ff6b35:#ff9a44>" + pluginName + "</gradient>:" +
                        (feature != null ? " <gray>[" + escapeTags(feature) + "]</gray>" : "") +
                        " " + level.color + level.name() + "</color> - "
        );

        // El detalle se agrega como texto plano, NUNCA se interpreta como MiniMessage.
        // Esto evita que un texto dinámico (nombre de jugador, mensaje de excepción, etc.)
        // inyecte tags de color/gradiente o rompa el formato del log.
        Component detailComponent = Component.text(stripLegacyColorCodes(detail == null ? "" : detail));

        Component full = prefix.append(detailComponent);
        if (elapsed > 0) {
            full = full.append(MINI.deserialize(" <dark_gray>(" + elapsed + "ms)</dark_gray>"));
        }

        Bukkit.getConsoleSender().sendMessage(full);

        if (cause != null && level == Level.ERROR) {
            Bukkit.getConsoleSender().sendMessage(MINI.deserialize(
                    "  <red>Cause: " + escapeTags(cause.getClass().getSimpleName()) + " - " +
                            escapeTags(cause.getMessage() != null ? cause.getMessage() : "No message") + "</red>"
            ));
            cause.printStackTrace();
        }

        if (logToFile) {
            writeToFile(level, feature, detail, elapsed, now, cause);
        }
    }

    private AtomicInteger countFor(Level level) {
        return switch (level) {
            case SUCCESS -> successCount;
            case WARNING -> warningCount;
            case ERROR -> errorCount;
            case INFO -> infoCount;
            case DEBUG -> infoCount; // se cuenta junto a info para el resumen
        };
    }

    private static String stripLegacyColorCodes(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    /** Evita que valores dinámicos usados como marcado literal rompan la sintaxis de MiniMessage. */
    private static String escapeTags(String text) {
        return text.replace("<", "\\<");
    }

    private void writeToFile(Level level, String feature, String detail, long elapsed,
                              LocalDateTime at, @Nullable Throwable cause) {
        if (logFile == null) return;
        StringBuilder line = new StringBuilder();
        line.append('[').append(at.format(TIME_FMT)).append("] ");
        line.append('[').append(level.name()).append("] ");
        if (feature != null) line.append('[').append(feature).append("] ");
        line.append(detail == null ? "" : stripLegacyColorCodes(detail));
        if (elapsed > 0) line.append(" (").append(elapsed).append("ms)");
        if (cause != null) {
            line.append(" | Cause: ").append(cause.getClass().getSimpleName())
                    .append(" - ").append(cause.getMessage());
        }
        line.append(System.lineSeparator());

        try {
            Files.writeString(logFile, line.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir el archivo de log: " + logFile, e);
        }
    }

    // Resumen

    /** Imprime un resumen general con conteos, tiempo total y las features más lentas. */
    public void printSummary() {
        long totalTime = System.currentTimeMillis() - startTime;
        int success = successCount.get();
        int warnings = warningCount.get();
        int errors = errorCount.get();
        int infos = infoCount.get();

        StringBuilder sb = new StringBuilder();
        sb.append("\n<gradient:#ff9a44:#ff6b35>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        sb.append("\n  <gradient:#ff6b35:#ff9a44>").append(pluginName);
        if (!pluginVersion.isEmpty()) sb.append(" v").append(pluginVersion);
        sb.append(errors > 0 ? " <red>✘</red>" : " <green>✔</green>");

        sb.append("\n  <green>✅ ").append(success).append(" loaded</green>");
        if (warnings > 0) sb.append("\n  <gold>⚠️ ").append(warnings).append(" warnings</gold>");
        if (errors > 0) sb.append("\n  <red>❌ ").append(errors).append(" errors</red>");
        if (infos > 0) sb.append("\n  <aqua>ℹ️ ").append(infos).append(" infos</aqua>");
        sb.append("\n  <dark_gray>⏱ ").append(totalTime).append("ms total</dark_gray>");

        List<LogEntry> slowest = entries.stream()
                .filter(e -> e.elapsedMs() > 0)
                .sorted(Comparator.comparingLong(LogEntry::elapsedMs).reversed())
                .limit(3)
                .toList();

        if (!slowest.isEmpty()) {
            sb.append("\n  <dark_gray>Slowest:</dark_gray>");
            for (LogEntry e : slowest) {
                sb.append("\n    <gray>- ").append(e.feature()).append(": ")
                        .append(e.elapsedMs()).append("ms</gray>");
            }
        }

        sb.append("\n<gradient:#ff9a44:#ff6b35>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>");
        Bukkit.getConsoleSender().sendMessage(MINI.deserialize(sb.toString()));
    }

    /** Devuelve todas las entradas registradas, útil para exportar o testear. */
    public List<String> getPlainEntries() {
        List<String> out = new ArrayList<>();
        for (LogEntry e : entries) {
            out.add(String.format(Locale.ROOT, "[%s] [%s] %s: %s (%dms)",
                    e.at().format(TIME_FMT), e.level(), e.feature(), e.detail(), e.elapsedMs()));
        }
        return out;
    }

    // Compatibilidad SLF4J-style (delegan al ComponentLogger de la plataforma)

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
