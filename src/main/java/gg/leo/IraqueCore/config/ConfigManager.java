package gg.leo.IraqueCore.config;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final IraqueCore plugin;
    private FileConfiguration config;

    private String chatFormat;
    private boolean useRanks;
    private boolean useTags;

    private boolean discordEnabled;
    private String discordToken;
    private String discordChannelId;
    private String discordWhitelistChannelId;
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

    private File discordFile;
    private FileConfiguration discordConfig;

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static final Map<Character, String> LEGACY_TO_MINI = new HashMap<>();
    static {
        LEGACY_TO_MINI.put('0', "<black>");
        LEGACY_TO_MINI.put('1', "<dark_blue>");
        LEGACY_TO_MINI.put('2', "<dark_green>");
        LEGACY_TO_MINI.put('3', "<dark_aqua>");
        LEGACY_TO_MINI.put('4', "<dark_red>");
        LEGACY_TO_MINI.put('5', "<dark_purple>");
        LEGACY_TO_MINI.put('6', "<gold>");
        LEGACY_TO_MINI.put('7', "<gray>");
        LEGACY_TO_MINI.put('8', "<dark_gray>");
        LEGACY_TO_MINI.put('9', "<blue>");
        LEGACY_TO_MINI.put('a', "<green>");
        LEGACY_TO_MINI.put('b', "<aqua>");
        LEGACY_TO_MINI.put('c', "<red>");
        LEGACY_TO_MINI.put('d', "<light_purple>");
        LEGACY_TO_MINI.put('e', "<yellow>");
        LEGACY_TO_MINI.put('f', "<white>");
        LEGACY_TO_MINI.put('k', "<obfuscated>");
        LEGACY_TO_MINI.put('l', "<bold>");
        LEGACY_TO_MINI.put('m', "<strikethrough>");
        LEGACY_TO_MINI.put('n', "<underlined>");
        LEGACY_TO_MINI.put('o', "<italic>");
        LEGACY_TO_MINI.put('r', "<reset>");
    }

    public ConfigManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        loadMessages();
        loadDiscordFile();
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

    private void loadDiscordFile() {
        discordFile = new File(plugin.getDataFolder(), "discord.yml");
        if (!discordFile.exists()) {
            plugin.saveResource("discord.yml", false);
        }
        discordConfig = YamlConfiguration.loadConfiguration(discordFile);
    }

    public void reloadDiscordFile() {
        if (discordFile != null) {
            discordConfig = YamlConfiguration.loadConfiguration(discordFile);
        }
    }

    public String getDiscordWebhook(String type) {
        if (discordConfig == null) return "";
        String url = discordConfig.getString("webhooks." + type, "");
        if (url == null || url.isBlank()) {
            return webhookUrl;
        }
        return url;
    }

    public boolean isWhitelistEnabled() {
        if (discordConfig == null) return false;
        return discordConfig.getBoolean("whitelist.enabled", true);
    }

    public String getWhitelistPrompt() {
        if (discordConfig == null) return "";
        return discordConfig.getString("whitelist.prompt", "");
    }

    public String getWhitelistAdded() {
        if (discordConfig == null) return "";
        return discordConfig.getString("whitelist.added", "");
    }

    public String getWhitelistAlready() {
        if (discordConfig == null) return "";
        return discordConfig.getString("whitelist.already", "");
    }

    public String getWhitelistInvalid() {
        if (discordConfig == null) return "";
        return discordConfig.getString("whitelist.invalid", "");
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
        if (s == null) return "";
        // &#RRGGBB → <#RRGGBB>
        s = s.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");
        // standalone #RRGGBB → <#RRGGBB> (not preceded by <)
        s = s.replaceAll("(?<!<)#([0-9a-fA-F]{6})", "<#$1>");
        // & codes → MiniMessage tags
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '&' || c == '\u00A7') && i + 1 < s.length()) {
                String tag = LEGACY_TO_MINI.get(Character.toLowerCase(s.charAt(i + 1)));
                if (tag != null) {
                    sb.append(tag);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public Component deserialize(String miniString) {
        return MINI.deserialize(miniString);
    }

    public String toLegacy(String s) {
        if (s == null) return "";
        return LegacyComponentSerializer.legacySection().serialize(MINI.deserialize(s));
    }

    public String toLegacyMessage(String path) {
        return toLegacy(translate(getMessage(path)));
    }

    public String toLegacyMessage(String path, String placeholder, String value) {
        return toLegacy(translate(getMessage(path).replace(placeholder, value)));
    }

    public String translateAndReplace(String message, String placeholder, String value) {
        return translate(message.replace(placeholder, value));
    }

    public Component getMessageComponent(String path) {
        return getMessageComponent(path, "&cMessage not found: " + path);
    }

    public Component getMessageComponent(String path, String fallback) {
        return MINI.deserialize(translate(getMessage(path, fallback)));
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
        if (discordConfig == null) return;

        this.discordEnabled = discordConfig.getBoolean("enabled", false);
        this.discordToken = discordConfig.getString("token", "");
        this.discordChannelId = discordConfig.getString("channel-id", "");
        this.discordWhitelistChannelId = discordConfig.getString("whitelist-channel-id", "");
        this.useWebhooks = discordConfig.getBoolean("use-webhooks", true);
        this.webhookUrl = discordConfig.getString("webhook-url", "");
        this.minecraftToDiscord = discordConfig.getString("minecraft-to-discord", "**{player}:** {message}");
        this.discordToMinecraft = discordConfig.getString("discord-to-minecraft", "&9[Discord] &b{author}&7: {message}");
        this.joinMessage = discordConfig.getString("join-message", "");
        this.leaveMessage = discordConfig.getString("leave-message", "");
        this.deathMessage = discordConfig.getString("death-message", "");
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
    public String getDiscordWhitelistChannelId() { return discordWhitelistChannelId; }
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
