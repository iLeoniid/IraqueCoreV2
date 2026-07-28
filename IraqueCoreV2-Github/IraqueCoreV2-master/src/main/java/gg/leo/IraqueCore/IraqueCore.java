package gg.leo.IraqueCore;

import gg.leo.IraqueCore.afk.AfkManager;
import gg.leo.IraqueCore.anvil.AnvilColorListener;
import gg.leo.IraqueCore.armorstand.ArmorStandEditor;
import gg.leo.IraqueCore.commands.GameModeCommand;
import gg.leo.IraqueCore.commands.IraqueCoreCommand;
import gg.leo.IraqueCore.commands.ReloadCommand;
import gg.leo.IraqueCore.commands.SettingsCommand;
import gg.leo.IraqueCore.commands.SpawnCommand;
import gg.leo.IraqueCore.commands.WhitelistCommand;
import gg.leo.IraqueCore.config.ConfigManager;
import gg.leo.IraqueCore.discord.AdvancementListener;
import gg.leo.IraqueCore.discord.DiscordManager;
import gg.leo.IraqueCore.durability.DurabilityListener;
import gg.leo.IraqueCore.grant.GrantCommand;
import gg.leo.IraqueCore.chatcolor.ChatColorCommand;
import gg.leo.IraqueCore.chatcolor.ChatColorManager;
import gg.leo.IraqueCore.grant.GrantListener;
import gg.leo.IraqueCore.grant.GrantManager;
import gg.leo.IraqueCore.grant.GrantsCommand;
import gg.leo.IraqueCore.grant.RevokeCommand;
import gg.leo.IraqueCore.grave.GraveListener;
import gg.leo.IraqueCore.home.HomeCommand;
import gg.leo.IraqueCore.home.HomeManager;
import gg.leo.IraqueCore.stats.StatsCommand;
import gg.leo.IraqueCore.tpa.TPACommand;
import gg.leo.IraqueCore.tpa.TPAManager;
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
import gg.leo.IraqueCore.utils.menu.listener.MenuListener;
import gg.leo.IraqueCore.sleep.SleepManager;
import gg.leo.IraqueCore.tag.TagCommand;
import gg.leo.IraqueCore.tag.TagManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
    private ChatColorManager   chatColorManager;
    private NametagDisplayManager nametagDisplayManager;
    private StatsCommand       statsCommand;
    private TPAManager         tpaManager;
    private HomeManager        homeManager;
    private GraveListener      graveListener;
    private DurabilityListener durabilityListener;
    private ArmorStandEditor   armorStandEditor;
    private AnvilColorListener anvilColorListener;
    private NametagDisplayManager nametagDisplayManager;

    // Paper 1.20.6+ provides native ComponentLogger — much better than raw SLF4J
    private ComponentLogger componentLogger;

    // Startup stats
    private int featuresLoaded = 0;
    private int featuresFailed = 0;

    private static final String PREFIX = "<#2b2b2b><strikethrough>                                  </strikethrough></#2b2b2b> ";
    private static final String PREFIX_LAST = "<#2b2b2b><strikethrough>                                  </strikethrough></#2b2b2b> ";

    @Override
    public void onEnable() {
        instance            = this;
        this.componentLogger = getComponentLogger();
        long startTime = System.currentTimeMillis();

        saveDefaultConfig();

        //  Managers 
        this.configManager = new ConfigManager(this);
        configManager.load();

        logHeader();
        logFeature("Config");

        this.afkManager = new AfkManager(this);
        afkManager.load();
        afkManager.startTask();
        logFeature("AFK System");

        this.sleepManager = new SleepManager(this);
        sleepManager.load();
        logFeature("Sleep System");

        this.playtimeManager = new PlaytimeManager(this);
        playtimeManager.load();
        playtimeManager.startTask();
        logFeature("Playtime Tracker");

        this.rankManager = new RankManager(this);
        rankManager.loadRanks();
        logFeature("Ranks");

        this.nametagDisplayManager = new NametagDisplayManager(this);
        getServer().getPluginManager().registerEvents(new NametagListener(this), this);
        logFeature("Nametag Display");

        this.tagManager = new TagManager(this);
        tagManager.load();
        logFeature("Tags");

        this.msgManager = new MsgManager();
        logFeature("Private Messages");

        this.permissionManager = new PermissionManager(this);
        permissionManager.load();
        logFeature("Permissions");

        this.grantManager = new GrantManager(this);
        grantManager.load();
        grantManager.startTask();

        this.grantListener = new GrantListener(this);
        getServer().getPluginManager().registerEvents(grantListener, this);
        logFeature("Grant System");

        this.chatColorManager = new ChatColorManager(this);
        chatColorManager.load();
        logFeature("Chat Colors");

        //  Scoreboard 
        this.scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.load();
        scoreboardManager.startTasks();
        logFeature("Scoreboard");

        //  Events ─
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(scoreboardManager, this);
        getServer().getPluginManager().registerEvents(afkManager, this);
        this.armorStandEditor = new ArmorStandEditor(this);
        getServer().getPluginManager().registerEvents(armorStandEditor, this);
        this.anvilColorListener = new AnvilColorListener(this);
        getServer().getPluginManager().registerEvents(anvilColorListener, this);
        getServer().getPluginManager().registerEvents(sleepManager, this);
        getServer().getPluginManager().registerEvents(playtimeManager, this);
        this.graveListener = new GraveListener(this);
        getServer().getPluginManager().registerEvents(graveListener, this);
        getServer().getPluginManager().registerEvents(new AdvancementListener(this), this);
        this.durabilityListener = new DurabilityListener(this);
        getServer().getPluginManager().registerEvents(durabilityListener, this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        logFeature("Event Listeners");

        this.statsCommand = new StatsCommand(this);
        logFeature("Statistics");

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
        logFeature("Leaderboards");

        this.motdManager = new MotdManager(this);
        motdManager.load();
        logFeature("MOTD");

        this.imageMotdManager = new ImageMotdManager(this);
        imageMotdManager.load();
        getServer().getPluginManager().registerEvents(imageMotdManager, this);
        logFeature("Image MOTD");

        this.tpaManager = new TPAManager(this);
        logFeature("TPA System");

        this.homeManager = new HomeManager(this);
        homeManager.load();
        logFeature("Home System");

        //  Commands 
        registerCommands();
        logFeature("Commands");

        //  Discord (async — doesn't block startup) ─
        if (configManager.isDiscordEnabled()) {
            try {
                this.discordManager = new DiscordManager(this);
                discordManager.start();
                logFeature("Discord Bridge");
            } catch (Exception e) {
                logFeatureFailed("Discord Bridge", e.getMessage());
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        logFooter(elapsed);
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
        if (chatColorManager != null) {
            chatColorManager.saveAll();
        }
        if (scoreboardManager != null) {
            scoreboardManager.saveStats();
            scoreboardManager.stopTasks();
        }
        if (playtimeManager != null) {
            playtimeManager.savePlaytime();
        }
        componentLogger.info("IraqueCore disabled.");
        instance = null;
    }

    /**
     * Reloads every YAML from disk and re-applies settings to all features.
     * @return human-readable summary of what was reloaded
     */
    public String reload() {
        // Persist runtime data before re-reading YAMLs from disk
        if (playtimeManager != null) {
            playtimeManager.savePlaytime();
        }
        if (scoreboardManager != null) {
            scoreboardManager.saveStats();
        }

        List<String> files = new ArrayList<>(configManager.reloadAllYaml());

        // tags.yml / motd.yml
        if (tagManager != null) {
            tagManager.reload();
            files.add("tags.yml");
        }
        if (motdManager != null) {
            motdManager.reload();
            files.add("motd.yml");
        }

        // Feature data YAMLs (safe re-read)
        if (homeManager != null) {
            homeManager.load();
            files.add("homes.yml");
        }
        if (chatColorManager != null) {
            chatColorManager.load();
        }
        if (permissionManager != null) {
            permissionManager.load();
        }
        if (grantManager != null) {
            grantManager.load();
        }

        // Apply config.yml feature settings
        rankManager.loadRanks();
        rankManager.updateAllVisuals();

        if (scoreboardManager != null) {
            scoreboardManager.reload();
        }
        if (afkManager != null) {
            afkManager.load();
        }
        if (sleepManager != null) {
            sleepManager.load();
        }
        if (playtimeManager != null) {
            playtimeManager.reload();
        }
        if (imageMotdManager != null) {
            imageMotdManager.reload();
        }
        if (graveListener != null) {
            graveListener.reload();
        }
        if (durabilityListener != null) {
            durabilityListener.reload();
        }

        // Discord: restart to apply token/channel changes from discord.yml
        if (discordManager != null) {
            discordManager.shutdown();
            discordManager = null;
        }
        if (configManager.isDiscordEnabled()) {
            this.discordManager = new DiscordManager(this);
            discordManager.start();
        }

        // Deduplicate while keeping order
        LinkedHashSet<String> unique = new LinkedHashSet<>(files);
        return String.join(", ", unique);
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

        var chatColorCommand = new ChatColorCommand(chatColorManager);
        register("chatcolor", chatColorCommand, null);

        var infoCommand = new IraqueCoreCommand(this);
        register("iraquecore", infoCommand, infoCommand);

        var settingsCommand = new SettingsCommand(this);
        register("settings", settingsCommand, null);

        var tpaCommand = new TPACommand(this, tpaManager);
        register("tpa", tpaCommand, tpaCommand);

        var homeCommand = new HomeCommand(this, homeManager);
        register("home", homeCommand, homeCommand);
        register("sethome", homeCommand, homeCommand);
        register("delhome", homeCommand, homeCommand);
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
    public NametagDisplayManager getNametagDisplayManager() { return nametagDisplayManager; }
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
    public ChatColorManager   getChatColorManager()     { return chatColorManager; }
    public TPAManager         getTpaManager()           { return tpaManager; }
    public HomeManager        getHomeManager()          { return homeManager; }

    /**
     * Native Paper logger with Adventure Components support.
     * Use it instead of raw SLF4J for colored console messages.
     */
    public ComponentLogger getPluginLogger()          { return componentLogger; }

    //  Console startup logging 

    private void logHeader() {
        String version = getPluginMeta().getVersion();
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a><strikethrough>                                                    </strikethrough></#1a1a1a>"
        ));
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a>    </#1a1a1a><#e63946>\u25C6</#e63946> <#f1faee><bold>IraqueCore</bold></#f1faee> <#a8dadc>v" + version + "</#a8dadc>"
        ));
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a>    </#1a1a1a><#457b9d>\u2502</#457b9d> <#a8dadc>Loading features...</#a8dadc>"
        ));
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a><strikethrough>                                                    </strikethrough></#1a1a1a>"
        ));
    }

    private void logFeature(String feature) {
        featuresLoaded++;
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a>    </#1a1a1a><#2d6a4f>\u2714</#2d6a4f> <#a8dadc>" + feature + "</#a8dadc>"
        ));
    }

    private void logFeatureFailed(String feature, String reason) {
        featuresFailed++;
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a>    </#1a1a1a><#e63946>\u2718</#e63946> <#a8dadc>" + feature + "</#a8dadc> <#6c757d>- " + reason + "</#6c757d>"
        ));
    }

    private void logFooter(long elapsedMs) {
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a><strikethrough>                                                    </strikethrough></#1a1a1a>"
        ));
        String status = featuresFailed == 0
            ? "<#2d6a4f>Operational</#2d6a4f>"
            : "<#e63946>" + featuresFailed + " failed</#e63946>";
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a>    </#1a1a1a><#457b9d>\u2502</#457b9d> <#6c757d>Status:</#6c757d> " + status + " <#6c757d>|</#6c757d> <#6c757d>Features:</#6c757d> <#f1faee>" + featuresLoaded + "</#f1faee> <#6c757d>|</#6c757d> <#6c757d>Time:</#6c757d> <#f1faee>" + elapsedMs + "ms</#f1faee>"
        ));
        Bukkit.getConsoleSender().sendMessage(configManager.deserialize(
            "<#1a1a1a><strikethrough>                                                    </strikethrough></#1a1a1a>"
        ));
    }
}