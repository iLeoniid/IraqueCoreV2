package gg.leo.IraqueCore;

import gg.leo.IraqueCore.commands.ReloadCommand;
import gg.leo.IraqueCore.commands.SpawnCommand;
import gg.leo.IraqueCore.config.ConfigManager;
import gg.leo.IraqueCore.discord.DiscordManager;
import gg.leo.IraqueCore.msg.MsgCommand;
import gg.leo.IraqueCore.msg.MsgManager;
import gg.leo.IraqueCore.msg.ReplyCommand;
import gg.leo.IraqueCore.rank.RankCommand;
import gg.leo.IraqueCore.rank.RankManager;
import gg.leo.IraqueCore.scoreboard.ScoreboardCommand;
import gg.leo.IraqueCore.scoreboard.ScoreboardManager;
import gg.leo.IraqueCore.tag.TagCommand;
import gg.leo.IraqueCore.tag.TagManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IraqueCore extends JavaPlugin {

    private static IraqueCore instance;
    private ConfigManager configManager;
    private RankManager rankManager;
    private TagManager tagManager;
    private ScoreboardManager scoreboardManager;
    private DiscordManager discordManager;
    private MsgManager msgManager;
    private Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        this.logger = LoggerFactory.getLogger("IraqueCore");

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        configManager.load();

        this.rankManager = new RankManager(this);
        rankManager.loadRanks();

        this.tagManager = new TagManager(this);
        tagManager.load();

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(tagManager, this);

        this.scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.load();
        getServer().getPluginManager().registerEvents(scoreboardManager, this);
        scoreboardManager.startTasks();

        this.msgManager = new MsgManager();

        var rankCommand = new RankCommand(this);
        var tagCommand = new TagCommand(this);
        var scoreboardCommand = new ScoreboardCommand(scoreboardManager);
        var msgCommand = new MsgCommand(this, msgManager);
        var replyCommand = new ReplyCommand(this, msgManager);

        var rankCmd = getCommand("rank");
        if (rankCmd != null) {
            rankCmd.setExecutor(rankCommand);
            rankCmd.setTabCompleter(rankCommand);
        }
        var tagCmd = getCommand("tags");
        if (tagCmd != null) {
            tagCmd.setExecutor(tagCommand);
            tagCmd.setTabCompleter(tagCommand);
        }
        var msgCmd = getCommand("msg");
        if (msgCmd != null) {
            msgCmd.setExecutor(msgCommand);
            msgCmd.setTabCompleter(msgCommand);
        }
        var replyCmd = getCommand("r");
        if (replyCmd != null) {
            replyCmd.setExecutor(replyCommand);
        }
        var sbCmd = getCommand("scoreboard");
        if (sbCmd != null) {
            sbCmd.setExecutor(scoreboardCommand);
            sbCmd.setTabCompleter(scoreboardCommand);
        }
        var reloadCmd = getCommand("reload");
        if (reloadCmd != null) {
            reloadCmd.setExecutor(new ReloadCommand(this));
        }
        var spawnCmd = getCommand("spawn");
        if (spawnCmd != null) {
            spawnCmd.setExecutor(new SpawnCommand(this));
        }
        var setspawnCmd = getCommand("setspawn");
        if (setspawnCmd != null) {
            setspawnCmd.setExecutor(new SpawnCommand(this));
        }

        if (configManager.isDiscordEnabled()) {
            this.discordManager = new DiscordManager(this);
            discordManager.start();
        }

        logger.info("IraqueCore v{} enabled!", getPluginMeta().getVersion());
    }

    @Override
    public void onDisable() {
        if (discordManager != null) {
            discordManager.shutdown();
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
        logger.info("IraqueCore disabled.");
        instance = null;
    }

    public void reload() {
        reloadConfig();
        configManager.load();
        rankManager.loadRanks();
        tagManager.reload();

        if (scoreboardManager != null) {
            scoreboardManager.loadConfig();
        }

        if (configManager.isDiscordEnabled() && discordManager == null) {
            this.discordManager = new DiscordManager(this);
            discordManager.start();
        } else if (!configManager.isDiscordEnabled() && discordManager != null) {
            discordManager.shutdown();
            discordManager = null;
        }
    }

    public static IraqueCore getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public Logger getPluginLogger() {
        return logger;
    }
}
