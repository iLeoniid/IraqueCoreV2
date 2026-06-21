package gg.leo.IraqueCore;

import gg.leo.IraqueCore.afk.AfkManager;
import gg.leo.IraqueCore.anvil.AnvilColorListener;
import gg.leo.IraqueCore.armorstand.ArmorStandEditor;
import gg.leo.IraqueCore.commands.GameModeCommand;
import gg.leo.IraqueCore.commands.IraqueCoreCommand;
import gg.leo.IraqueCore.commands.ReloadCommand;
import gg.leo.IraqueCore.commands.SpawnCommand;
import gg.leo.IraqueCore.commands.WhitelistCommand;
import gg.leo.IraqueCore.config.ConfigManager;
import gg.leo.IraqueCore.discord.AdvancementListener;
import gg.leo.IraqueCore.discord.DiscordManager;
import gg.leo.IraqueCore.durability.DurabilityListener;
import gg.leo.IraqueCore.grant.GrantCommand;
import gg.leo.IraqueCore.grant.GrantListener;
import gg.leo.IraqueCore.grant.GrantManager;
import gg.leo.IraqueCore.grant.GrantsCommand;
import gg.leo.IraqueCore.grant.RevokeCommand;
import gg.leo.IraqueCore.grave.GraveListener;
import gg.leo.IraqueCore.stats.StatsCommand;
import gg.leo.IraqueCore.leaderboard.LeaderboardCommand;
import gg.leo.IraqueCore.leaderboard.LeaderboardManager;
import gg.leo.IraqueCore.permission.PermissionManager;
import gg.leo.IraqueCore.permission.PermissionsCommand;
import gg.leo.IraqueCore.motd.ImageMotdManager;
import gg.leo.IraqueCore.motd.MotdManager;
import gg.leo.IraqueCore.msg.MsgCommand;
import gg.leo.IraqueCore.msg.MsgManager;
import gg.leo.IraqueCore.msg.ReplyCommand;
import gg.leo.IraqueCore.playtime.PlaytimeCommand;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.rank.RankCommand;
import gg.leo.IraqueCore.rank.RankManager;
import gg.leo.IraqueCore.scoreboard.ScoreboardCommand;
import gg.leo.IraqueCore.scoreboard.ScoreboardManager;
import gg.leo.IraqueCore.sleep.SleepManager;
import gg.leo.IraqueCore.tag.TagCommand;
import gg.leo.IraqueCore.tag.TagManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
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
    private ImageMotdManager  imageMotdManager;
    private AfkManager        afkManager;
    private SleepManager      sleepManager;
    private PlaytimeManager    playtimeManager;
    private PermissionManager  permissionManager;
    private GrantManager       grantManager;
    private GrantListener      grantListener;
    private StatsCommand       statsCommand;

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

        this.afkManager = new AfkManager(this);
        afkManager.load();
        afkManager.startTask();

        this.sleepManager = new SleepManager(this);
        sleepManager.load();

        this.playtimeManager = new PlaytimeManager(this);
        playtimeManager.load();
        playtimeManager.startTask();

        this.rankManager = new RankManager(this);
        rankManager.loadRanks();

        this.tagManager = new TagManager(this);
        tagManager.load();

        this.msgManager = new MsgManager();

        this.permissionManager = new PermissionManager(this);
        permissionManager.load();

        this.grantManager = new GrantManager(this);
        grantManager.load();
        grantManager.startTask();

        this.grantListener = new GrantListener(this);

        //  Scoreboard 
        this.scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.load();
        scoreboardManager.startTasks();

        //  Events ─
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(tagManager, this);
        getServer().getPluginManager().registerEvents(scoreboardManager, this);
        getServer().getPluginManager().registerEvents(afkManager, this);
        getServer().getPluginManager().registerEvents(new ArmorStandEditor(this), this);
        getServer().getPluginManager().registerEvents(new AnvilColorListener(this), this);
        getServer().getPluginManager().registerEvents(sleepManager, this);
        getServer().getPluginManager().registerEvents(playtimeManager, this);
        getServer().getPluginManager().registerEvents(new GraveListener(this), this);
        getServer().getPluginManager().registerEvents(new AdvancementListener(this), this);
        getServer().getPluginManager().registerEvents(new DurabilityListener(this), this);
        getServer().getPluginManager().registerEvents(grantListener, this);

        this.statsCommand = new StatsCommand(this);
        getServer().getPluginManager().registerEvents(statsCommand, this);

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onRespawn(PlayerRespawnEvent event) {
                Location spawn = configManager.getSpawnLocation();
                if (spawn != null && spawn.getWorld() != null) {
                    event.setRespawnLocation(spawn);
                }
            }
        }, this);

        this.leaderboardManager = new LeaderboardManager(this);
        getServer().getPluginManager().registerEvents(leaderboardManager, this);

        this.motdManager = new MotdManager(this);
        motdManager.load();

        this.imageMotdManager = new ImageMotdManager(this);
        imageMotdManager.load();
        getServer().getPluginManager().registerEvents(imageMotdManager, this);

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
        if (permissionManager != null) {
            permissionManager.saveAll();
        }
        if (grantManager != null) {
            grantManager.saveAll();
        }
        if (scoreboardManager != null) {
            scoreboardManager.saveStats();
        }
        if (playtimeManager != null) {
            playtimeManager.savePlaytime();
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
        configManager.reloadDiscordFile();
        rankManager.loadRanks();
        rankManager.updateAllVisuals();
        tagManager.reload();

        if (scoreboardManager != null) {
            scoreboardManager.loadConfig();
        }

        if (afkManager != null) {
            afkManager.load();
        }

        if (sleepManager != null) {
            sleepManager.load();
        }

        if (playtimeManager != null) {
            playtimeManager.load();
        }

        if (permissionManager != null) {
            permissionManager.load();
        }
        if (grantManager != null) {
            grantManager.load();
        }

        if (motdManager != null) {
            motdManager.reload();
        }
        if (imageMotdManager != null) {
            imageMotdManager.reload();
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
        var permissionsCommand = new PermissionsCommand(this);
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

        var whitelistCommand = new WhitelistCommand(this);
        register("whitelist", whitelistCommand, whitelistCommand);

        var playtimeCommand = new PlaytimeCommand(this);
        register("playtime", playtimeCommand, playtimeCommand);

        register("stats", statsCommand, statsCommand);

        register("perm", permissionsCommand, permissionsCommand);

        register("grant", new GrantCommand(this), new GrantCommand(this));
        register("grants", new GrantsCommand(this), new GrantsCommand(this));
        register("revoke", new RevokeCommand(this), new RevokeCommand(this));

        var infoCommand = new IraqueCoreCommand(this);
        register("iraquecore", infoCommand, null);
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
    public AfkManager         getAfkManager()          { return afkManager; }
    public SleepManager       getSleepManager()        { return sleepManager; }
    public PlaytimeManager    getPlaytimeManager()      { return playtimeManager; }
    public PermissionManager  getPermissionManager()    { return permissionManager; }
    public GrantManager       getGrantManager()         { return grantManager; }
    public GrantListener      getGrantListener()        { return grantListener; }

    /**
     * Native Paper logger with Adventure Components support.
     * Use it instead of raw SLF4J for colored console messages.
     */
    public ComponentLogger getPluginLogger()          { return componentLogger; }
}