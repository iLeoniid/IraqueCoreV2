package gg.leo.IraqueCore;

import gg.leo.IraqueCore.afk.AfkManager;
import gg.leo.IraqueCore.alerts.AlertCommand;
import gg.leo.IraqueCore.alerts.AlertManager;
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
import gg.leo.IraqueCore.chatcolor.ChatColorCommand;
import gg.leo.IraqueCore.chatcolor.ChatColorManager;
import gg.leo.IraqueCore.grant.GrantListener;
import gg.leo.IraqueCore.grant.GrantManager;
import gg.leo.IraqueCore.grant.GrantsCommand;
import gg.leo.IraqueCore.grant.RevokeCommand;
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
import gg.leo.IraqueCore.punishment.PunishmentCommand;
import gg.leo.IraqueCore.punishment.PunishmentManager;
import gg.leo.IraqueCore.rank.RankCommand;
import gg.leo.IraqueCore.rank.RankManager;
import gg.leo.IraqueCore.scoreboard.ScoreboardCommand;
import gg.leo.IraqueCore.scoreboard.ScoreboardManager;
import gg.leo.IraqueCore.troll.PanicCommand;
import gg.leo.IraqueCore.troll.TrollCommand;
import gg.leo.IraqueCore.troll.TrollEventListener;
import gg.leo.IraqueCore.troll.TrollManager;
import gg.leo.IraqueCore.troll.TrollMenu;
import gg.leo.IraqueCore.troll.TrollfCommand;
import gg.leo.IraqueCore.troll.UntrollCommand;
import gg.leo.IraqueCore.utils.PluginLogger;
import gg.leo.IraqueCore.utils.menu.listener.MenuListener;
import gg.leo.IraqueCore.sleep.SleepManager;
import gg.leo.IraqueCore.tag.TagCommand;
import gg.leo.IraqueCore.tag.TagManager;
import gg.leo.IraqueCore.totem.TotemListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import org.bukkit.Bukkit;
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
    private ChatColorManager   chatColorManager;
    private AlertManager       alertManager;
    private StatsCommand       statsCommand;
    private TPAManager         tpaManager;
    private HomeManager        homeManager;
    private PunishmentManager  punishmentManager;
    private TrollManager       trollManager;
    private PluginLogger       pluginLogger;

    @Override
    public void onEnable() {
        instance          = this;
        this.pluginLogger = new PluginLogger(getName(), getComponentLogger());

        pluginLogger.startFeature("Core");
        saveDefaultConfig();

        //  Managers 
        pluginLogger.startFeature("Config");
        this.configManager = new ConfigManager(this);
        configManager.load();
        pluginLogger.success("Config", "Configuration loaded");

        pluginLogger.startFeature("AFK");
        this.afkManager = new AfkManager(this);
        afkManager.load();
        afkManager.startTask();
        pluginLogger.success("AFK", "AFK manager started");

        pluginLogger.startFeature("Sleep");
        this.sleepManager = new SleepManager(this);
        sleepManager.load();
        pluginLogger.success("Sleep", "Sleep voting loaded");

        this.playtimeManager = new PlaytimeManager(this);
        playtimeManager.load();
        playtimeManager.startTask();
        pluginLogger.success("Playtime", "Playtime tracker started");

        this.rankManager = new RankManager(this);
        rankManager.loadRanks();
        pluginLogger.success("Rank", "Rank system loaded");

        this.tagManager = new TagManager(this);
        tagManager.load();
        pluginLogger.success("Tag", "Tag system loaded");

        this.alertManager = new AlertManager(this);
        alertManager.load();
        getServer().getPluginManager().registerEvents(alertManager, this);
        pluginLogger.success("Alerts", "Alert system loaded");

        this.msgManager = new MsgManager();
        pluginLogger.success("Msg", "Private messaging loaded");

        this.permissionManager = new PermissionManager(this);
        permissionManager.load();
        pluginLogger.success("Permission", "Permission manager loaded");

        this.grantManager = new GrantManager(this);
        grantManager.load();
        grantManager.startTask();

        this.grantListener = new GrantListener(this);
        getServer().getPluginManager().registerEvents(grantListener, this);
        pluginLogger.success("Grant", "Grant system loaded");

        this.chatColorManager = new ChatColorManager(this);
        chatColorManager.load();
        pluginLogger.success("ChatColor", "Chat color system loaded");

        //  Scoreboard 
        this.scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.load();
        scoreboardManager.startTasks();
        pluginLogger.success("Scoreboard", "Scoreboard system loaded");

        //  Events ─
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(scoreboardManager, this);
        getServer().getPluginManager().registerEvents(afkManager, this);
        getServer().getPluginManager().registerEvents(new ArmorStandEditor(this), this);
        getServer().getPluginManager().registerEvents(new AnvilColorListener(this), this);
        getServer().getPluginManager().registerEvents(sleepManager, this);
        getServer().getPluginManager().registerEvents(playtimeManager, this);
        getServer().getPluginManager().registerEvents(new AdvancementListener(this), this);
        getServer().getPluginManager().registerEvents(new DurabilityListener(this), this);
        getServer().getPluginManager().registerEvents(new TotemListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        pluginLogger.info("Events", "Event listeners registered");

        this.statsCommand = new StatsCommand(this);
        pluginLogger.success("Stats", "Stats command loaded");

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
        pluginLogger.success("Leaderboard", "Leaderboard system loaded");

        this.motdManager = new MotdManager(this);
        motdManager.load();
        pluginLogger.success("MOTD", "MOTD system loaded");

        this.imageMotdManager = new ImageMotdManager(this);
        imageMotdManager.load();
        getServer().getPluginManager().registerEvents(imageMotdManager, this);
        pluginLogger.success("ImageMOTD", "Image MOTD loaded");

        this.tpaManager = new TPAManager(this);
        pluginLogger.success("TPA", "Teleport request system loaded");

        this.homeManager = new HomeManager(this);
        homeManager.load();
        pluginLogger.success("Home", "Home system loaded");

        this.punishmentManager = new PunishmentManager(this);
        punishmentManager.load();
        getServer().getPluginManager().registerEvents(punishmentManager, this);
        pluginLogger.success("Punishment", "Punishment system loaded");

        //  Troll plugin 
        this.trollManager = new TrollManager(this);
        trollManager.load();
        getServer().getPluginManager().registerEvents(trollManager.getEventListener(), this);
        pluginLogger.success("Troll", "Troll system loaded");

        //  Commands 
        registerCommands();
        pluginLogger.success("Commands", "All commands registered");

        //  Discord (async — doesn't block startup) ─
        if (configManager.isDiscordEnabled()) {
            pluginLogger.startFeature("Discord");
            try {
                this.discordManager = new DiscordManager(this);
                discordManager.start();
                pluginLogger.success("Discord", "Discord bot connected");
            } catch (Exception e) {
                pluginLogger.error("Discord", "Failed to start Discord", e);
            }
        }

        pluginLogger.success("Core", "Plugin fully initialized");
        pluginLogger.printSummary();
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
        if (punishmentManager != null) {
            punishmentManager.saveMutes();
        }
        pluginLogger.info("IraqueCore", "Plugin disabled.");
        instance = null;
    }

    public String reload() {
        if (playtimeManager != null) {
            playtimeManager.savePlaytime();
        }
        if (scoreboardManager != null) {
            scoreboardManager.saveStats();
        }
        reloadConfig();
        configManager.load();
        configManager.reloadDiscordFile();
        configManager.reloadMessages();
        ArrayList<String> files = new ArrayList<>();
        files.add("config.yml");
        if (tagManager != null) {
            tagManager.reload();
            files.add("tags.yml");
        }
        if (motdManager != null) {
            motdManager.reload();
            files.add("motd.yml");
        }
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
            playtimeManager.load();
        }
        if (imageMotdManager != null) {
            imageMotdManager.reload();
        }
        if (alertManager != null) {
            alertManager.reload();
        }
        if (discordManager != null) {
            discordManager.shutdown();
            discordManager = null;
        }
        if (configManager.isDiscordEnabled()) {
            discordManager = new DiscordManager(this);
            discordManager.start();
        }
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
        gamemodeCommand.startTask();

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

        var chatColorCommand = new ChatColorCommand(chatColorManager);
        register("chatcolor", chatColorCommand, null);

        var alertCommand = new AlertCommand(this, alertManager);
        register("alert", alertCommand, alertCommand);

        var infoCommand = new IraqueCoreCommand(this);
        register("iraquecore", infoCommand, null);

        var tpaCommand = new TPACommand(this, tpaManager);
        register("tpa", tpaCommand, tpaCommand);

        var homeCommand = new HomeCommand(this, homeManager);
        register("home", homeCommand, homeCommand);
        register("sethome", homeCommand, homeCommand);
        register("delhome", homeCommand, homeCommand);

        var punishmentCommand = new PunishmentCommand(this, punishmentManager);
        register("ban", punishmentCommand, punishmentCommand);
        register("unban", punishmentCommand, punishmentCommand);
        register("mute", punishmentCommand, punishmentCommand);
        register("unmute", punishmentCommand, punishmentCommand);
        register("kick", punishmentCommand, punishmentCommand);

        var healCommand = new gg.leo.IraqueCore.commands.HealCommand(this);
        register("heal", healCommand, healCommand);
        var feedCommand = new gg.leo.IraqueCore.commands.FeedCommand(this);
        register("feed", feedCommand, feedCommand);
        var trashCommand = new gg.leo.IraqueCore.commands.TrashCommand(this);
        register("trash", trashCommand, null);
        getServer().getPluginManager().registerEvents(trashCommand, this);

        var removeArmorStandCommand = new gg.leo.IraqueCore.commands.RemoveArmorStandCommand(this);
        register("removearmorstand", removeArmorStandCommand, null);

        var trollMenu = new TrollMenu(trollManager);
        var trollCommand = new TrollCommand(trollManager, trollMenu);
        register("troll", trollCommand, trollCommand);
        register("trollf", new TrollfCommand(trollManager), new TrollfCommand(trollManager));
        register("untroll", new UntrollCommand(trollManager), new UntrollCommand(trollManager));
        register("panicstoptroll", new PanicCommand(trollManager), null);
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
            pluginLogger.warn("Command '{}' not found in plugin.yml — skipping.", name);
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
    public ChatColorManager   getChatColorManager()     { return chatColorManager; }
    public AlertManager       getAlertManager()          { return alertManager; }
    public TPAManager         getTpaManager()            { return tpaManager; }
    public HomeManager        getHomeManager()           { return homeManager; }
    public PunishmentManager  getPunishmentManager()     { return punishmentManager; }
    public TrollManager       getTrollManager()          { return trollManager; }
    public TrollEventListener getTrollEventListener()     { return trollManager.getEventListener(); }

    public PluginLogger getPluginLogger() { return pluginLogger; }
}