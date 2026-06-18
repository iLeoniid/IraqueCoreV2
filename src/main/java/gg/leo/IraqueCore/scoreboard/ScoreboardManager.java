package gg.leo.IraqueCore.scoreboard;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.animation.TextAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.criteria.Criteria;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardManager implements Listener {

    private final IraqueCore plugin;

    // Per-player toggle
    private final Map<UUID, Boolean> playerEnabled = new HashMap<>();

    // Stats
    private final Map<UUID, Integer> blocksBroken = new HashMap<>();
    private final Map<UUID, Integer> blocksPlaced = new HashMap<>();
    private final Map<UUID, Integer> deaths       = new HashMap<>();

    // Config values
    private long          updateInterval;
    private TextAnimation titleAnimation;
    private List<String>  lines;
    private boolean       globalEnabled;

    // Persistence
    private File              statsFile;
    private FileConfiguration statsConfig;

    // Adventure serializers
    private static final MiniMessage              MINI   = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()          // supports &#rrggbb and #rrggbb via &x legacy OR MiniMessage hex
                    .useUnusualXRepeatedCharacterHexFormat() // &#RRGGBB -> &x&R&R&G&G&B&B
                    .build();

    public ScoreboardManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    //  Lifecycle 

    public void load() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getPluginLogger().error("Could not create stats.yml", e);
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        loadStats();
        loadConfig();
    }

    public void loadConfig() {
        var cfg = plugin.getConfig();
        globalEnabled = cfg.getBoolean("scoreboard.enabled", true);

        titleAnimation = new TextAnimation(plugin, cfg, "scoreboard.title");

        int    time = cfg.getInt("scoreboard.update.amount", 2);
        String unit = cfg.getString("scoreboard.update.unit", "seconds").toLowerCase();
        updateInterval = convertToTicks(time, unit);

        lines = cfg.getStringList("scoreboard.lines");
    }

    public void startTasks() {
        // Scoreboard update task
        new BukkitRunnable() {
            @Override public void run() {
                if (!globalEnabled || Bukkit.getOnlinePlayers().isEmpty()) return;
                updateAll();
            }
        }.runTaskTimer(plugin, 20L, updateInterval);

        // Auto-save stats every 5 minutes
        new BukkitRunnable() {
            @Override public void run() {
                if (statsConfig != null) saveStats();
            }
        }.runTaskTimer(plugin, 6000L, 6000L);
    }

    //  Color parsing 

    /**
     * Parses a string that may contain:
     *  - Legacy & codes        : &6&lText
     *  - Hex & codes           : &#RRGGBBText  or  &x&R&R&G&G&B&B...
     *  - MiniMessage tags      : <gold><bold>Text</bold></gold>
     *  - Raw hex (MiniMessage) : <#RRGGBB>Text</#RRGGBB>
     *
     * Strategy: run MiniMessage first (it is strict and won't eat & codes),
     * then fall back to legacy for anything MiniMessage doesn't touch.
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // If it contains MiniMessage tags, parse with MiniMessage
        if (text.contains("<") && text.contains(">")) {
            // MiniMessage also supports inserting legacy inside via <legacy> — but for
            // simplicity we pre-convert & codes to MiniMessage before parsing.
            String converted = legacyToMiniMessage(text);
            return MINI.deserialize(converted);
        }

        // Pure legacy / hex-legacy string
        return LEGACY.deserialize(text);
    }

    /**
     * Converts & color codes (including &#RRGGBB) inside a string to
     * MiniMessage equivalents so MiniMessage can handle the full string.
     */
    private static String legacyToMiniMessage(String input) {
        // &#RRGGBB -> <#RRGGBB>
        input = input.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");
        // &x&R&R&G&G&B&B (Spigot hex) -> <#RRGGBB>
        input = input.replaceAll(
                "&x&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])",
                "<#$1$2$3$4$5$6>"
        );
        // Classic & codes
        input = input
                .replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&A", "<green>").replace("&B", "<aqua>")
                .replace("&C", "<red>").replace("&D", "<light_purple>")
                .replace("&E", "<yellow>").replace("&F", "<white>")
                .replace("&l", "<bold>").replace("&o", "<italic>")
                .replace("&n", "<underlined>").replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>").replace("&r", "<reset>");
        return input;
    }

    //  Scoreboard logic 

    public void setPlayerEnabled(Player player, boolean enabled) {
        playerEnabled.put(player.getUniqueId(), enabled);
        if (enabled) {
            updateScoreboard(player);
        } else {
            clearScoreboard(player);
        }
    }

    public boolean isPlayerEnabled(Player player) {
        return playerEnabled.getOrDefault(player.getUniqueId(), globalEnabled);
    }

    private void clearScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null) return;
        Objective old = board.getObjective("iraqueboard");
        if (old != null) old.unregister();
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerEnabled(player)) updateScoreboard(player);
        }
    }

    public void updateScoreboard(Player player) {
        if (!globalEnabled || !isPlayerEnabled(player) || lines.isEmpty()) {
            clearScoreboard(player);
            return;
        }

        // Ensure the player has a custom scoreboard (not the main one)
        Scoreboard board = player.getScoreboard();
        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }

        // Unregister old objective
        Objective old = board.getObjective("iraqueboard");
        if (old != null) old.unregister();

        // Title — from animation, parsed as Component
        Component titleComponent = parse(titleAnimation.getText());

        // Register new objective using Adventure Component API (Paper 1.20.4+)
        Objective obj = board.registerNewObjective(
                "iraqueboard",
                Criteria.DUMMY,
                titleComponent
        );
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Build lines
        int online = Bukkit.getOnlinePlayers().size();
        int max    = Bukkit.getMaxPlayers();
        int score  = lines.size();

        for (String raw : lines) {
            String line = applyPlaceholders(raw, player, online, max);
            // Score entry name must be unique — pad with invisible chars if duplicate
            String entryName = ensureUnique(board, line, score);
            obj.getScore(entryName).setScore(score--);
        }
    }

    /**
     * Replaces all supported placeholders in a scoreboard line.
     */
    private String applyPlaceholders(String raw, Player player, int online, int max) {
        return raw
                .replace("{online}",       String.valueOf(online))
                .replace("{max}",          String.valueOf(max))
                .replace("{player}",       player.getName())
                .replace("{displayname}",  player.getDisplayName())
                .replace("{world}",        player.getWorld().getName())
                .replace("{ping}",         String.valueOf(player.getPing()))
                .replace("{blocks_broken}", String.valueOf(blocksBroken.getOrDefault(player.getUniqueId(), 0)))
                .replace("{blocks_placed}", String.valueOf(blocksPlaced.getOrDefault(player.getUniqueId(), 0)))
                .replace("{deaths}",       String.valueOf(deaths.getOrDefault(player.getUniqueId(), 0)))
                .replace("{players}",      Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName).collect(Collectors.joining(", ")));
    }

    /**
     * Scoreboard entries must be unique strings. If two lines resolve to the
     * same text (e.g. multiple blank lines) we append invisible § characters.
     */
    private String ensureUnique(Scoreboard board, String line, int score) {
        String candidate = line;
        // Use section symbols (invisible in-game when not followed by a valid code)
        // to differentiate duplicate entries
        while (board.getEntries().contains(candidate)) {
            candidate = candidate + "§";
        }
        return candidate;
    }

    //  Stats persistence 

    private void loadStats() {
        if (statsConfig == null || !statsConfig.contains("players")) return;

        for (String uuidStr : statsConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID   uuid = UUID.fromString(uuidStr);
                String path = "players." + uuidStr + ".";
                blocksBroken.put(uuid, statsConfig.getInt(path + "blocks_broken", 0));
                blocksPlaced.put(uuid, statsConfig.getInt(path + "blocks_placed", 0));
                deaths.put(uuid,       statsConfig.getInt(path + "deaths", 0));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveStats() {
        if (statsConfig == null) return;
        statsConfig.set("players", null);

        for (UUID uuid : blocksBroken.keySet()) {
            String path = "players." + uuid + ".";
            statsConfig.set(path + "name",          getPlayerName(uuid));
            statsConfig.set(path + "blocks_broken", blocksBroken.get(uuid));
            statsConfig.set(path + "blocks_placed", blocksPlaced.getOrDefault(uuid, 0));
            statsConfig.set(path + "deaths",        deaths.getOrDefault(uuid, 0));
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save stats.yml", e);
        }
    }

    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        if (statsConfig != null && statsConfig.contains("players." + uuid + ".name")) {
            return statsConfig.getString("players." + uuid + ".name");
        }
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unknown";
    }

    //  Helpers 

    private long convertToTicks(int time, String unit) {
        return switch (unit) {
            case "seconds" -> time * 20L;
            case "minutes" -> time * 20L * 60L;
            case "hours"   -> time * 20L * 60L * 60L;
            default        -> 10 * 20L;
        };
    }

    //  Events 

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        blocksBroken.put(id, blocksBroken.getOrDefault(id, 0) + 1);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        blocksPlaced.put(id, blocksPlaced.getOrDefault(id, 0) + 1);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        deaths.put(id, deaths.getOrDefault(id, 0) + 1);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID   id     = player.getUniqueId();

        blocksBroken.putIfAbsent(id, 0);
        blocksPlaced.putIfAbsent(id, 0);
        deaths.putIfAbsent(id, 0);

        if (statsConfig != null) {
            statsConfig.set("players." + id + ".name", player.getName());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (isPlayerEnabled(player)) updateScoreboard(player);
        }, 10L);
    }
}