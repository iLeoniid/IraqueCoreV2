package gg.leo.IraqueCore.config;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {

    private final IraqueCore plugin;
    private FileConfiguration config;

    private String chatFormat;
    private boolean useRanks;
    private boolean useTags;

    private boolean discordEnabled;
    private String discordToken;
    private String discordChannelId;
    private boolean useWebhooks;
    private String webhookUrl;
    private String minecraftToDiscord;
    private String discordToMinecraft;
    private String joinMessage;
    private String leaveMessage;
    private String deathMessage;

    private String storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private int mysqlPoolSize;

    private File messagesFile;
    private FileConfiguration messagesConfig;

    public ConfigManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        loadMessages();
        loadGeneral();
        loadChat();
        loadDiscord();
        loadStorage();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessages() {
        if (messagesFile != null) {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        }
    }

    public String getMessage(String path) {
        return getMessage(path, "&cMessage not found: " + path);
    }

    public String getMessage(String path, String fallback) {
        if (messagesConfig == null) return fallback;
        return messagesConfig.getString(path, fallback);
    }

    public String getPrefixedMessage(String path) {
        String prefix = getMessage("prefix", "&8[&6IraqueCore&8] &7");
        return translate(prefix + getMessage(path, "&cMessage not found: " + path));
    }

    public String translate(String s) {
        return s.replace('&', '\u00A7');
    }

    public Location getSpawnLocation() {
        if (!config.contains("spawn.world")) return null;
        World world = plugin.getServer().getWorld(config.getString("spawn.world"));
        if (world == null) return null;
        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setSpawnLocation(Location loc) {
        config.set("spawn.world", loc.getWorld().getName());
        config.set("spawn.x", loc.getX());
        config.set("spawn.y", loc.getY());
        config.set("spawn.z", loc.getZ());
        config.set("spawn.yaw", (double) loc.getYaw());
        config.set("spawn.pitch", (double) loc.getPitch());
        plugin.saveConfig();
    }

    private void loadGeneral() {
    }

    private void loadChat() {
        ConfigurationSection section = config.getConfigurationSection("chat");
        if (section == null) return;

        this.chatFormat = section.getString("format", "{prefix}{tag}{player}{suffix}: {message}");
        this.useRanks = section.getBoolean("use-ranks", true);
        this.useTags = section.getBoolean("use-tags", true);
    }

    private void loadDiscord() {
        ConfigurationSection section = config.getConfigurationSection("discord");
        if (section == null) return;

        this.discordEnabled = section.getBoolean("enabled", false);
        this.discordToken = section.getString("token", "");
        this.discordChannelId = section.getString("channel-id", "");
        this.useWebhooks = section.getBoolean("use-webhooks", true);
        this.webhookUrl = section.getString("webhook-url", "");
        this.minecraftToDiscord = section.getString("minecraft-to-discord", "**{player}:** {message}");
        this.discordToMinecraft = section.getString("discord-to-minecraft", "&9[Discord] &b{author}&7: {message}");
        this.joinMessage = section.getString("join-message", "");
        this.leaveMessage = section.getString("leave-message", "");
        this.deathMessage = section.getString("death-message", "");
    }

    private void loadStorage() {
        ConfigurationSection section = config.getConfigurationSection("storage");
        if (section == null) return;

        this.storageType = section.getString("type", "yaml");

        ConfigurationSection mysql = section.getConfigurationSection("mysql");
        if (mysql != null) {
            this.mysqlHost = mysql.getString("host", "localhost");
            this.mysqlPort = mysql.getInt("port", 3306);
            this.mysqlDatabase = mysql.getString("database", "iraquecore");
            this.mysqlUsername = mysql.getString("username", "root");
            this.mysqlPassword = mysql.getString("password", "");
            this.mysqlPoolSize = mysql.getInt("pool-size", 10);
        }
    }

    public List<String> getRankNames() {
        ConfigurationSection section = config.getConfigurationSection("ranks.ranks");
        if (section == null) return List.of();
        return section.getKeys(false).stream().toList();
    }

    public ConfigurationSection getRankConfig(String name) {
        return config.getConfigurationSection("ranks.ranks." + name);
    }

    public String getDefaultRankName() {
        return config.getString("ranks.default-rank", "Member");
    }

    public String getChatFormat() { return chatFormat; }
    public boolean isUseRanks() { return useRanks; }
    public boolean isUseTags() { return useTags; }

    public boolean isDiscordEnabled() { return discordEnabled; }
    public String getDiscordToken() { return discordToken; }
    public String getDiscordChannelId() { return discordChannelId; }
    public boolean isUseWebhooks() { return useWebhooks; }
    public String getWebhookUrl() { return webhookUrl; }
    public String getMinecraftToDiscordFormat() { return minecraftToDiscord; }
    public String getDiscordToMinecraftFormat() { return discordToMinecraft; }
    public String getJoinMessage() { return joinMessage; }
    public String getLeaveMessage() { return leaveMessage; }
    public String getDeathMessage() { return deathMessage; }

    public String getStorageType() { return storageType; }
    public String getMysqlHost() { return mysqlHost; }
    public int getMysqlPort() { return mysqlPort; }
    public String getMysqlDatabase() { return mysqlDatabase; }
    public String getMysqlUsername() { return mysqlUsername; }
    public String getMysqlPassword() { return mysqlPassword; }
    public int getMysqlPoolSize() { return mysqlPoolSize; }
}
