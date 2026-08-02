package gg.leo.IraqueCore.scoreboard;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.animation.TextAnimation;
import gg.leo.IraqueCore.utils.ItemBuilder;
import gg.leo.IraqueCore.utils.SchedulerUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager
implements Listener {
    private final IraqueCore plugin;
    private final Map<UUID, Boolean> playerEnabled = new HashMap<>();
    private final Map<UUID, Integer> blocksBroken = new HashMap<>();
    private final Map<UUID, Integer> blocksPlaced = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private long updateInterval;
    private TextAnimation titleAnimation;
    private List<String> lines;
    private boolean globalEnabled;
    private File statsFile;
    private FileConfiguration statsConfig;
    private final Set<UUID> dirtyPlayers = new HashSet<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();
    private final Map<UUID, String> lastTitles = new HashMap<>();
    private final Map<UUID, List<String>> lastLines = new HashMap<>();
    private final List<BukkitRunnable> runningTasks = new ArrayList<>();

    public ScoreboardManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    private void queueUpdate(Player player) {
        if (this.dirtyPlayers.add(player.getUniqueId())) {
            SchedulerUtil.runSync(this.plugin, this::flushDirty);
        }
    }

    private void flushDirty() {
        for (UUID id : this.dirtyPlayers) {
            Player player = Bukkit.getPlayer(id);
            if (player == null || !this.isPlayerEnabled(player)) continue;
            this.updateScoreboard(player);
        }
        this.dirtyPlayers.clear();
    }

    public void load() {
        this.statsFile = new File(this.plugin.getDataFolder(), "stats.yml");
        if (!this.statsFile.exists()) {
            try {
                this.statsFile.getParentFile().mkdirs();
                this.statsFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getPluginLogger().error("Could not create stats.yml", e);
            }
        }
        this.statsConfig = YamlConfiguration.loadConfiguration(this.statsFile);
        this.loadStats();
        this.loadConfig();
    }

    public void loadConfig() {
        FileConfiguration cfg = this.plugin.getConfig();
        this.globalEnabled = cfg.getBoolean("scoreboard.enabled", true);
        this.titleAnimation = new TextAnimation(this.plugin, cfg, "scoreboard.title");
        int time = cfg.getInt("scoreboard.update.amount", 2);
        String unit = cfg.getString("scoreboard.update.unit", "seconds").toLowerCase();
        this.updateInterval = this.convertToTicks(time, unit);
        this.lines = cfg.getStringList("scoreboard.lines");
    }

    public void reload() {
        this.loadConfig();
        this.startTasks();
        this.updateAll();
    }

    public void startTasks() {
        this.stopTasks();
        if (this.titleAnimation != null && this.titleAnimation.isAnimated()) {
            long animInterval = Math.max(this.titleAnimation.getTicks(), 5L);
            ScoreboardManager self = this;
            BukkitRunnable animTask = new BukkitRunnable() {
                public void run() {
                    if (!self.globalEnabled || Bukkit.getOnlinePlayers().isEmpty()) {
                        return;
                    }
                    self.updateTitlesOnly();
                }
            };
            animTask.runTaskTimer(this.plugin, 20L, animInterval);
            this.runningTasks.add(animTask);
        }
        ScoreboardManager self = this;
        BukkitRunnable updateTask = new BukkitRunnable() {
            public void run() {
                if (!self.globalEnabled || Bukkit.getOnlinePlayers().isEmpty()) {
                    return;
                }
                self.updateAllLines();
            }
        };
        updateTask.runTaskTimer(this.plugin, 20L, this.updateInterval);
        this.runningTasks.add(updateTask);
        BukkitRunnable saveTask = new BukkitRunnable() {
            public void run() {
                if (self.statsConfig != null) {
                    self.saveStats();
                }
            }
        };
        saveTask.runTaskTimer(this.plugin, 6000L, 6000L);
        this.runningTasks.add(saveTask);
    }

    public void stopTasks() {
        for (BukkitRunnable task : this.runningTasks) {
            try {
                task.cancel();
            }
            catch (Exception exception) {}
        }
        this.runningTasks.clear();
    }

    public Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(text));
    }

    public void setPlayerEnabled(Player player, boolean enabled) {
        this.playerEnabled.put(player.getUniqueId(), enabled);
        if (enabled) {
            this.updateScoreboard(player);
        } else {
            this.clearScoreboard(player);
        }
    }

    public boolean isPlayerEnabled(Player player) {
        return this.playerEnabled.getOrDefault(player.getUniqueId(), this.globalEnabled);
    }

    private void clearScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null) {
            return;
        }
        Objective old = board.getObjective("iraqueboard");
        if (old != null) {
            old.unregister();
        }
        this.playerObjectives.remove(player.getUniqueId());
        this.lastTitles.remove(player.getUniqueId());
        this.lastLines.remove(player.getUniqueId());
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.isPlayerEnabled(player)) continue;
            this.updateScoreboard(player);
        }
    }

    private void updateTitlesOnly() {
        String currentTitle = this.titleAnimation.nextFrame();
        Component titleComponent = this.parse(currentTitle);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.isPlayerEnabled(player)) continue;
            UUID id = player.getUniqueId();
            Objective obj = this.playerObjectives.get(id);
            if (obj == null) {
                this.updateScoreboard(player);
                continue;
            }
            String lastTitle = this.lastTitles.get(id);
            if (lastTitle != null && lastTitle.equals(currentTitle)) continue;
            obj.displayName(titleComponent);
            this.lastTitles.put(id, currentTitle);
        }
    }

    private void updateAllLines() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.isPlayerEnabled(player)) continue;
            this.updateScoreboardLines(player);
        }
    }

    public void updateScoreboard(Player player) {
        Objective obj;
        if (!this.globalEnabled || !this.isPlayerEnabled(player) || this.lines.isEmpty()) {
            this.clearScoreboard(player);
            return;
        }
        UUID id = player.getUniqueId();
        Scoreboard board = player.getScoreboard();
        boolean newBoard = false;
        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
            newBoard = true;
        }
        if ((obj = board.getObjective("iraqueboard")) == null) {
            Component titleComponent = this.parse(this.titleAnimation.getCurrentText());
            obj = board.registerNewObjective("iraqueboard", "dummy", titleComponent);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            this.lastTitles.put(id, this.titleAnimation.getCurrentText());
        }
        this.playerObjectives.put(id, obj);
        this.updateScoreboardLines(player);
        if (newBoard && this.plugin.getRankManager() != null) {
            this.plugin.getRankManager().initVisuals(player);
        }
    }

    private void updateScoreboardLines(Player player) {
        UUID id = player.getUniqueId();
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("iraqueboard");
        if (obj == null) {
            this.updateScoreboard(player);
            return;
        }
        for (String entry : new HashSet<>(board.getEntries())) {
            board.resetScores(entry);
        }
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        int score = this.lines.size();
        ArrayList<String> currentLines = new ArrayList<>();
        String[] colorCodes = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "k", "l", "m", "n", "o", "r"};
        for (int i = 0; i < this.lines.size(); ++i) {
            String raw = this.lines.get(i);
            String line = ItemBuilder.color(this.applyPlaceholders(raw, player, online, max));
            currentLines.add(line);
            String colorPrefix = "\u00a7" + colorCodes[i % colorCodes.length];
            String key = line.trim().isEmpty() ? colorPrefix + " \u00a7r" : colorPrefix + line + "\u00a7r";
            obj.getScore(key).setScore(score--);
        }
        this.lastLines.put(id, currentLines);
    }

    private String applyPlaceholders(String raw, Player player, int online, int max) {
        return raw.replace("{online}", String.valueOf(online)).replace("{max}", String.valueOf(max)).replace("{player}", player.getName()).replace("{displayname}", player.getDisplayName()).replace("{world}", player.getWorld().getName()).replace("{ping}", String.valueOf(player.getPing())).replace("{blocks_broken}", String.valueOf(this.blocksBroken.getOrDefault(player.getUniqueId(), 0))).replace("{blocks_placed}", String.valueOf(this.blocksPlaced.getOrDefault(player.getUniqueId(), 0))).replace("{deaths}", String.valueOf(this.deaths.getOrDefault(player.getUniqueId(), 0))).replace("{players}", Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.joining(", ")));
    }

    private void loadStats() {
        if (this.statsConfig == null || !this.statsConfig.contains("players")) {
            return;
        }
        for (String uuidStr : this.statsConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String path = "players." + uuidStr + ".";
                this.blocksBroken.put(uuid, this.statsConfig.getInt(path + "blocks_broken", 0));
                this.blocksPlaced.put(uuid, this.statsConfig.getInt(path + "blocks_placed", 0));
                this.deaths.put(uuid, this.statsConfig.getInt(path + "deaths", 0));
            }
            catch (IllegalArgumentException illegalArgumentException) {}
        }
    }

    public void saveStats() {
        if (this.statsConfig == null) {
            return;
        }
        this.statsConfig.set("players", null);
        for (UUID uuid : this.blocksBroken.keySet()) {
            String path = "players." + uuid + ".";
            this.statsConfig.set(path + "name", this.getPlayerName(uuid));
            this.statsConfig.set(path + "blocks_broken", this.blocksBroken.get(uuid));
            this.statsConfig.set(path + "blocks_placed", this.blocksPlaced.getOrDefault(uuid, 0));
            this.statsConfig.set(path + "deaths", this.deaths.getOrDefault(uuid, 0));
        }
        try {
            this.statsConfig.save(this.statsFile);
            this.plugin.getPluginLogger().info("Stats saved successfully.");
        }
        catch (IOException e) {
            this.plugin.getPluginLogger().error("Failed to save stats.yml", e);
        }
    }

    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            return online.getName();
        }
        if (this.statsConfig != null && this.statsConfig.contains("players." + uuid + ".name")) {
            return this.statsConfig.getString("players." + uuid + ".name");
        }
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unknown";
    }

    public Map<UUID, Integer> getBlocksBroken() {
        return Collections.unmodifiableMap(this.blocksBroken);
    }

    public Map<UUID, Integer> getBlocksPlaced() {
        return Collections.unmodifiableMap(this.blocksPlaced);
    }

    public Map<UUID, Integer> getDeaths() {
        return Collections.unmodifiableMap(this.deaths);
    }

    public List<String> getTopBlocksBroken(int limit) {
        return this.blocksBroken.entrySet().stream().sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed()).limit(limit).map(e -> this.getPlayerName(e.getKey()) + ": <yellow>" + e.getValue()).toList();
    }

    public List<String> getTopBlocksPlaced(int limit) {
        return this.blocksPlaced.entrySet().stream().sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed()).limit(limit).map(e -> this.getPlayerName(e.getKey()) + ": <yellow>" + e.getValue()).toList();
    }

    public List<String> getTopDeaths(int limit) {
        return this.deaths.entrySet().stream().sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed()).limit(limit).map(e -> this.getPlayerName(e.getKey()) + ": <red>" + e.getValue()).toList();
    }

    private long convertToTicks(int time, String unit) {
        return switch (unit) {
            case "seconds" -> (long)time * 20L;
            case "minutes" -> (long)time * 20L * 60L;
            case "hours" -> (long)time * 20L * 60L * 60L;
            default -> 200L;
        };
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        this.blocksBroken.put(id, this.blocksBroken.getOrDefault(id, 0) + 1);
        if (this.isPlayerEnabled(player)) {
            this.queueUpdate(player);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        this.blocksPlaced.put(id, this.blocksPlaced.getOrDefault(id, 0) + 1);
        if (this.isPlayerEnabled(player)) {
            this.queueUpdate(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID id = player.getUniqueId();
        this.deaths.put(id, this.deaths.getOrDefault(id, 0) + 1);
        if (this.isPlayerEnabled(player)) {
            this.queueUpdate(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        this.blocksBroken.putIfAbsent(id, 0);
        this.blocksPlaced.putIfAbsent(id, 0);
        this.deaths.putIfAbsent(id, 0);
        if (this.statsConfig != null) {
            this.statsConfig.set("players." + id + ".name", player.getName());
        }
        SchedulerUtil.runLater(this.plugin, () -> {
            if (this.isPlayerEnabled(player)) {
                this.updateScoreboard(player);
            }
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.playerObjectives.remove(id);
        this.lastTitles.remove(id);
        this.lastLines.remove(id);
        if (this.statsConfig != null) {
            String path = "players." + id + ".";
            this.statsConfig.set(path + "name", event.getPlayer().getName());
            this.statsConfig.set(path + "blocks_broken", this.blocksBroken.getOrDefault(id, 0));
            this.statsConfig.set(path + "blocks_placed", this.blocksPlaced.getOrDefault(id, 0));
            this.statsConfig.set(path + "deaths", this.deaths.getOrDefault(id, 0));
            try {
                this.statsConfig.save(this.statsFile);
            }
            catch (IOException e) {
                this.plugin.getPluginLogger().error("Failed to save stats.yml on quit", e);
            }
        }
    }
}
