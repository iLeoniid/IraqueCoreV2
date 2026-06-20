package gg.leo.IraqueCore.scoreboard;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.animation.TextAnimation;
import gg.leo.IraqueCore.utils.ItemBuilder;
import net.kyori.adventure.text.Component;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardManager implements Listener {

    private final IraqueCore plugin;

    private final Map<UUID, Boolean> playerEnabled = new HashMap<>();

    private final Map<UUID, Integer> blocksBroken = new HashMap<>();
    private final Map<UUID, Integer> blocksPlaced = new HashMap<>();
    private final Map<UUID, Integer> deaths       = new HashMap<>();

    private long          updateInterval;
    private TextAnimation titleAnimation;
    private List<String>  lines;
    private boolean       globalEnabled;

    private File              statsFile;
    private FileConfiguration statsConfig;

    private final Set<UUID> dirtyPlayers = new HashSet<>();

    private final Map<UUID, Objective> playerObjectives = new HashMap<>();
    private final Map<UUID, String> lastTitles = new HashMap<>();
    private final Map<UUID, List<String>> lastLines = new HashMap<>();

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
        if (titleAnimation != null && titleAnimation.isAnimated()) {
            long animInterval = Math.max(titleAnimation.getTicks(), 5L);
            new BukkitRunnable() {
                @Override public void run() {
                    if (!globalEnabled || Bukkit.getOnlinePlayers().isEmpty()) return;
                    updateTitlesOnly();
                }
            }.runTaskTimer(plugin, 20L, animInterval);
        }

        new BukkitRunnable() {
            @Override public void run() {
                if (!globalEnabled || Bukkit.getOnlinePlayers().isEmpty()) return;
                updateAllLines();
            }
        }.runTaskTimer(plugin, 20L, updateInterval);

        new BukkitRunnable() {
            @Override public void run() {
                if (statsConfig != null) saveStats();
            }
        }.runTaskTimer(plugin, 6000L, 6000L);
    }

    public Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(text));
    }

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
        playerObjectives.remove(player.getUniqueId());
        lastTitles.remove(player.getUniqueId());
        lastLines.remove(player.getUniqueId());
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerEnabled(player)) updateScoreboard(player);
        }
    }

    private void updateTitlesOnly() {
        String currentTitle = titleAnimation.nextFrame();
        Component titleComponent = parse(currentTitle);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isPlayerEnabled(player)) continue;

            UUID id = player.getUniqueId();
            Objective obj = playerObjectives.get(id);

            if (obj == null) {
                updateScoreboard(player);
                continue;
            }

            String lastTitle = lastTitles.get(id);
            if (lastTitle != null && lastTitle.equals(currentTitle)) continue;

            obj.displayName(titleComponent);
            lastTitles.put(id, currentTitle);
        }
    }

    private void updateAllLines() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerEnabled(player)) updateScoreboardLines(player);
        }
    }

    public void updateScoreboard(Player player) {
        if (!globalEnabled || !isPlayerEnabled(player) || lines.isEmpty()) {
            clearScoreboard(player);
            return;
        }

        UUID id = player.getUniqueId();

        Scoreboard board = player.getScoreboard();
        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }

        Objective obj = board.getObjective("iraqueboard");
        if (obj == null) {
            Component titleComponent = parse(titleAnimation.getCurrentText());
            obj = board.registerNewObjective("iraqueboard", "dummy", titleComponent);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.numberFormat(NumberFormat.blank());
            lastTitles.put(id, titleAnimation.getCurrentText());
        }
        playerObjectives.put(id, obj);

        updateScoreboardLines(player);
    }

    private void updateScoreboardLines(Player player) {
        UUID id = player.getUniqueId();
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("iraqueboard");

        if (obj == null) {
            updateScoreboard(player);
            return;
        }

        // clear old entries from the previous update to avoid stale duplicates
        for (String entry : new HashSet<>(board.getEntries())) {
            board.resetScores(entry);
        }

        int online = Bukkit.getOnlinePlayers().size();
        int max    = Bukkit.getMaxPlayers();
        int score  = lines.size();
        List<String> currentLines = new ArrayList<>();

        for (String raw : lines) {
            String line = ItemBuilder.color(applyPlaceholders(raw, player, online, max));
            currentLines.add(line);
            obj.getScore(line + "§" + score).setScore(score--);
        }

        lastLines.put(id, currentLines);
    }

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
            plugin.getPluginLogger().info("Stats saved successfully.");
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

    public List<String> getTopBlocksBroken(int limit) {
        return blocksBroken.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(e -> getPlayerName(e.getKey()) + ": <yellow>" + e.getValue())
            .toList();
    }

    public List<String> getTopBlocksPlaced(int limit) {
        return blocksPlaced.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(e -> getPlayerName(e.getKey()) + ": <yellow>" + e.getValue())
            .toList();
    }

    public List<String> getTopDeaths(int limit) {
        return deaths.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(e -> getPlayerName(e.getKey()) + ": <red>" + e.getValue())
            .toList();
    }

    private long convertToTicks(int time, String unit) {
        return switch (unit) {
            case "seconds" -> time * 20L;
            case "minutes" -> time * 20L * 60L;
            case "hours"   -> time * 20L * 60L * 60L;
            default        -> 10 * 20L;
        };
    }

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
        playerObjectives.remove(id);
        lastTitles.remove(id);
        lastLines.remove(id);

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
