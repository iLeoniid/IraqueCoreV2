package gg.leo.IraqueCore;

import gg.leo.IraqueCore.commands.GameModeCommand;
import gg.leo.IraqueCore.commands.ReloadCommand;
import gg.leo.IraqueCore.commands.SpawnCommand;
import gg.leo.IraqueCore.config.ConfigManager;
import gg.leo.IraqueCore.discord.DiscordManager;
import gg.leo.IraqueCore.leaderboard.LeaderboardCommand;
import gg.leo.IraqueCore.leaderboard.LeaderboardManager;
import gg.leo.IraqueCore.motd.MotdManager;
import gg.leo.IraqueCore.msg.MsgCommand;
import gg.leo.IraqueCore.msg.MsgManager;
import gg.leo.IraqueCore.msg.ReplyCommand;
import gg.leo.IraqueCore.rank.RankCommand;
import gg.leo.IraqueCore.rank.RankManager;
import gg.leo.IraqueCore.scoreboard.ScoreboardCommand;
import gg.leo.IraqueCore.scoreboard.ScoreboardManager;
import gg.leo.IraqueCore.tag.TagCommand;
import gg.leo.IraqueCore.tag.TagManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class IraqueCore extends JavaPlugin {

    private static IraqueCore instance;

    private ConfigManager     configManager;
    private RankManager       rankManager;
    private TagManager        tagManager;
    private ScoreboardManager scoreboardManager;
    private DiscordManager    discordManager;
    private MsgManager        msgManager;
    private LeaderboardManager leaderboardManager;
    private MotdManager       motdManager;

    // Paper 1.20.6+ provides native ComponentLogger — much better than raw SLF4J
    private ComponentLogger componentLogger;

    @Override
    public void onEnable() {
        instance            = this;
        this.componentLogger = getComponentLogger();

        saveDefaultConfig();

        //  Managers 
        this.configManager = new ConfigManager(this);
        configManager.load();

        this.rankManager = new RankManager(this);
        rankManager.loadRanks();

        this.tagManager = new TagManager(this);
        tagManager.load();

        this.msgManager = new MsgManager();

        //  Scoreboard 
        this.scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.load();
        scoreboardManager.startTasks();

        //  Events ─
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(tagManager, this);
        getServer().getPluginManager().registerEvents(scoreboardManager, this);

        this.leaderboardManager = new LeaderboardManager(this);
        getServer().getPluginManager().registerEvents(leaderboardManager, this);

        this.motdManager = new MotdManager(this);
        motdManager.load();

        //  Commands 
        registerCommands();

        //  Discord (async — doesn't block startup) ─
        if (configManager.isDiscordEnabled()) {
            this.discordManager = new DiscordManager(this);
            discordManager.start();
        }

        componentLogger.info("IraqueCore v{} enabled!", getPluginMeta().getVersion());
    }

    @Override
    public void onDisable() {
        if (discordManager != null) {
            discordManager.shutdown();
            discordManager = null;
        }
        if (rankManager != null) {
            rankManager.saveAll();
        }
        if (tagManager != null) {
            tagManager.savePlayerTags();
        }
        if (scoreboardManager != null) {
            scoreboardManager.saveStats();
        }
        componentLogger.info("IraqueCore disabled.");
        instance = null;
    }

    /**
     * Reloads all plugin configuration.
     * Also restarts Discord if the token/channel changed.
     */
    public void reload() {
        reloadConfig();
        configManager.load();
        rankManager.loadRanks();
        rankManager.updateAllVisuals();
        tagManager.reload();

        if (scoreboardManager != null) {
            scoreboardManager.loadConfig();
        }

        if (motdManager != null) {
            motdManager.reload();
        }

        // Discord: always restart to apply possible token/channel changes
        if (discordManager != null) {
            discordManager.shutdown();
            discordManager = null;
        }
        if (configManager.isDiscordEnabled()) {
            this.discordManager = new DiscordManager(this);
            discordManager.start();
        }
    }

    //  Command registration 

    private void registerCommands() {
        var rankCommand      = new RankCommand(this);
        var tagCommand       = new TagCommand(this);
        var scoreboardCmd    = new ScoreboardCommand(scoreboardManager, this);
        var msgCommand       = new MsgCommand(this, msgManager);
        var replyCommand     = new ReplyCommand(this, msgManager);
        var reloadCommand    = new ReloadCommand(this);
        var spawnCommand     = new SpawnCommand(this);
        var gamemodeCommand  = new GameModeCommand(this);

        register("rank",       rankCommand,   rankCommand);
        register("tags",       tagCommand,    tagCommand);
        register("scoreboard", scoreboardCmd, scoreboardCmd);
        register("msg",        msgCommand,    msgCommand);
        register("r",          replyCommand,  null);
        register("reload",     reloadCommand, null);
        register("spawn",      spawnCommand,  null);
        register("setspawn",   spawnCommand,  null);
        register("gm",         gamemodeCommand, gamemodeCommand);

        var leaderboardCommand = new LeaderboardCommand(this);
        register("leaderboards", leaderboardCommand, leaderboardCommand);

        var motdCommand = new gg.leo.IraqueCore.motd.MotdCommand(this);
        register("motd", motdCommand, motdCommand);
    }

    /**
     * Helper to register executor + tab completer in a single line.
     * tabCompleter can be null if the command has no tab completion.
     */
    private void register(String name,
                          org.bukkit.command.CommandExecutor executor,
                          org.bukkit.command.TabCompleter tabCompleter) {
        var cmd = getCommand(name);
        if (cmd == null) {
            componentLogger.warn("Command '{}' not found in plugin.yml — skipping.", name);
            return;
        }
        cmd.setExecutor(executor);
        if (tabCompleter != null) cmd.setTabCompleter(tabCompleter);
    }

    //  Getters 

    public static IraqueCore getInstance()           { return instance; }
    public ConfigManager     getConfigManager()       { return configManager; }
    public RankManager       getRankManager()         { return rankManager; }
    public TagManager        getTagManager()          { return tagManager; }
    public DiscordManager    getDiscordManager()      { return discordManager; }
    public ScoreboardManager getScoreboardManager()   { return scoreboardManager; }
    public MsgManager        getMsgManager()          { return msgManager; }
    public LeaderboardManager getLeaderboardManager()  { return leaderboardManager; }
    public MotdManager        getMotdManager()         { return motdManager; }

    /**
     * Native Paper logger with Adventure Components support.
     * Use it instead of raw SLF4J for colored console messages.
     */
    public ComponentLogger getPluginLogger()          { return componentLogger; }
}