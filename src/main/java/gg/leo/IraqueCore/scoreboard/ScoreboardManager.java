package gg.leo.IraqueCore.scoreboard;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.animation.TextAnimation;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

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

    private final Set<UUID> dirtyPlayers = new HashSet<>();

    public ScoreboardManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    private void queueUpdate(Player player) {
        if (dirtyPlayers.add(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, this::flushDirty);
        }
    }

    private void flushDirty() {
        for (UUID id : dirtyPlayers) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && isPlayerEnabled(player)) {
                updateScoreboard(player);
            }
        }
        dirtyPlayers.clear();
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
        }.runTaskTimer(plugin, 1200L, 1200L);
    }

    //  Color parsing 

    /**
     * Parses a string that supports all color formats:
     *  - & codes        : &6&lText
     *  - MiniMessage     : <gold><bold>Text</bold></gold>
     *  - Hex            : <#RRGGBB> or #RRGGBB or &#RRGGBB
     */
    public Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(text));
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

        // Paper 26.2: registerNewObjective con criterio como String + Component como título
        Objective obj = board.registerNewObjective(
                "iraqueboard",
                "dummy",
                titleComponent
        );
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Build lines
        int online = Bukkit.getOnlinePlayers().size();
        int max    = Bukkit.getMaxPlayers();
        int score  = lines.size();

        for (String raw : lines) {
            String line = ChatColor.translateAlternateColorCodes('&', applyPlaceholders(raw, player, online, max));
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

    public Map<UUID, Integer> getBlocksBroken() { return Collections.unmodifiableMap(blocksBroken); }
    public Map<UUID, Integer> getBlocksPlaced() { return Collections.unmodifiableMap(blocksPlaced); }
    public Map<UUID, Integer> getDeaths() { return Collections.unmodifiableMap(deaths); }

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
        Player player = event.getPlayer();
        UUID   id     = player.getUniqueId();
        blocksBroken.put(id, blocksBroken.getOrDefault(id, 0) + 1);
        if (isPlayerEnabled(player)) queueUpdate(player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID   id     = player.getUniqueId();
        blocksPlaced.put(id, blocksPlaced.getOrDefault(id, 0) + 1);
        if (isPlayerEnabled(player)) queueUpdate(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID   id     = player.getUniqueId();
        deaths.put(id, deaths.getOrDefault(id, 0) + 1);
        if (isPlayerEnabled(player)) queueUpdate(player);
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (statsConfig != null) {
            String path = "players." + id + ".";
            statsConfig.set(path + "name",          event.getPlayer().getName());
            statsConfig.set(path + "blocks_broken", blocksBroken.getOrDefault(id, 0));
            statsConfig.set(path + "blocks_placed", blocksPlaced.getOrDefault(id, 0));
            statsConfig.set(path + "deaths",        deaths.getOrDefault(id, 0));
            try {
                statsConfig.save(statsFile);
            } catch (IOException e) {
                plugin.getPluginLogger().error("Failed to save stats.yml on quit", e);
            }
        }
    }
}