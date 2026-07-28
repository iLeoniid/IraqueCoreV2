package gg.leo.IraqueCore.discord;

import gg.leo.IraqueCore.IraqueCore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class DiscordManager extends ListenerAdapter {

    private final IraqueCore plugin;
    private JDA         jda;
    private TextChannel channel;
    private TextChannel whitelistChannel;

    private volatile boolean running   = false;
    private volatile boolean cancelled = false;

    public DiscordManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    //  Lifecycle

    public void start() {
        String token = plugin.getConfigManager().getDiscordToken();
        if (token.isEmpty() || "YOUR_BOT_TOKEN_HERE".equals(token)) {
            plugin.getPluginLogger().warn("Discord token not configured — bot disabled.");
            return;
        }

        String channelId = plugin.getConfigManager().getDiscordChannelId();
        if (channelId.isEmpty() || "YOUR_CHANNEL_ID_HERE".equals(channelId)) {
            plugin.getPluginLogger().warn("Discord channel ID not configured — bot disabled.");
            return;
        }

        cancelled = false;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                JDA built = JDABuilder.createLight(token, EnumSet.of(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                ))
                        .setMemberCachePolicy(MemberCachePolicy.NONE)
                        .addEventListeners(this)
                        .build()
                        .awaitReady();

                if (cancelled) {
                    built.shutdown();
                    return;
                }

                TextChannel ch = built.getTextChannelById(channelId);
                if (ch == null) {
                    plugin.getPluginLogger().error("Discord channel not found: " + channelId);
                    built.shutdown();
                    return;
                }

                jda     = built;
                channel = ch;
                running = true;

                String wlChannelId = plugin.getConfigManager().getDiscordWhitelistChannelId();
                if (!wlChannelId.isBlank()) {
                    TextChannel wlCh = built.getTextChannelById(wlChannelId);
                    if (wlCh == null) {
                        plugin.getPluginLogger().warn("Whitelist Discord channel not found: " + wlChannelId + " — falling back to main channel");
                    }
                    whitelistChannel = wlCh;
                }

                plugin.getPluginLogger().info("Discord bot connected successfully!");

                sendWhitelistPrompt();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                plugin.getPluginLogger().warn("Discord startup interrupted.");
            } catch (Exception e) {
                plugin.getPluginLogger().error("Failed to start Discord bot", e);
            }
        });
    }

    public void shutdown() {
        cancelled = true;
        running   = false;

        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
        channel = null;
        whitelistChannel = null;
    }

    //  Minecraft  Discord

    public void sendMinecraftToDiscord(Player player, String message) {
        if (!running || channel == null) return;

        String format = plugin.getConfigManager().getMinecraftToDiscordFormat();

        String rankName = plugin.getRankManager()
                .getPlayerRank(player.getUniqueId())
                .map(r -> r.name())
                .orElse("");

        String formatted = format
                .replace("{player}",  player.getName())
                .replace("{message}", message)
                .replace("{world}",   player.getWorld().getName())
                .replace("{rank}",    rankName);

        formatted = stripColor(formatted);

        if (plugin.getConfigManager().isUseWebhooks()) {
            String webhookUrl = plugin.getConfigManager().getDiscordWebhook("chat");
            if (webhookUrl == null || webhookUrl.isBlank() || !webhookUrl.startsWith("http")) {
                final String finalFormatted = formatted;
                channel.sendMessage(finalFormatted).queue(
                        null,
                        err -> plugin.getPluginLogger().warn("Failed to send message to Discord: " + err.getMessage())
                );
            } else {
                sendWebhookMessage("chat", player.getName(), player.getUniqueId().toString(), message);
            }
        } else {
            final String finalFormatted = formatted;
            channel.sendMessage(finalFormatted).queue(
                    null,
                    err -> plugin.getPluginLogger().warn("Failed to send message to Discord: " + err.getMessage())
            );
        }
    }

    public void sendRawMessage(String message) {
        if (!running || channel == null) return;
        channel.sendMessage(stripColor(message)).queue(
                null,
                err -> plugin.getPluginLogger().warn("Failed to send raw message to Discord: " + err.getMessage())
        );
    }

    public void sendWebhookEvent(String type, String name, String uuid, String message) {
        if (!running) return;
        sendWebhookMessage(type, name, uuid, message);
    }

    //  Discord  Minecraft

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!running) return;
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().equals(channel)) return;

        String content = event.getMessage().getContentDisplay();
        if (content.isBlank()) return;

        handleWhitelist(content, event);
        handleChatBridge(content, event);
    }

    private void handleWhitelist(String content, MessageReceivedEvent event) {
        if (!plugin.getConfigManager().isWhitelistEnabled()) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!Bukkit.hasWhitelist()) return;

            String name = content.strip();
            if (!isValidPlayerName(name)) {
                sendWhitelistMessage(plugin.getConfigManager().getWhitelistInvalid());
                return;
            }

            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            if (offline.isWhitelisted()) {
                sendWhitelistMessage(plugin.getConfigManager().getWhitelistAlready()
                        .replace("{player}", name));
                return;
            }

            offline.setWhitelisted(true);
            sendWhitelistMessage(plugin.getConfigManager().getWhitelistAdded()
                    .replace("{player}", name));

            plugin.getPluginLogger().info("Whitelisted {} via Discord (user: {})", name, event.getAuthor().getName());
        });
    }

    private boolean isValidPlayerName(String name) {
        return name.length() >= 3 && name.length() <= 16 && name.matches("[a-zA-Z0-9_]+");
    }

    private void handleChatBridge(String content, MessageReceivedEvent event) {
        String author    = event.getAuthor().getName();
        String format    = plugin.getConfigManager().getDiscordToMinecraftFormat();
        String formatted = format
                .replace("{author}",  author)
                .replace("{message}", content);

        Component component = plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(formatted));

        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component))
        );
    }

    //  Whitelist prompt

    public void sendWhitelistPrompt() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!running || channel == null) return;
            if (!Bukkit.hasWhitelist()) return;
            if (!plugin.getConfigManager().isWhitelistEnabled()) return;

            String prompt = plugin.getConfigManager().getWhitelistPrompt();
            if (!prompt.isBlank()) {
                sendWhitelistMessage(prompt);
            }
        });
    }

    private void sendDiscordMessage(String message) {
        sendToChannel(channel, message);
    }

    private void sendWhitelistMessage(String message) {
        TextChannel target = (whitelistChannel != null) ? whitelistChannel : channel;
        sendToChannel(target, message);
    }

    private void sendToChannel(TextChannel target, String message) {
        if (!running || target == null) return;
        String stripped = stripColor(message);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                target.sendMessage(stripped).queue(
                        null,
                        err -> plugin.getPluginLogger().warn("Failed to send Discord message: " + err.getMessage())
                )
        );
    }

    //  Webhook

    private void sendWebhookMessage(String type, String name, String uuid, String message) {
        String webhookUrl = plugin.getConfigManager().getDiscordWebhook(type);
        if (webhookUrl == null || webhookUrl.isBlank() || !webhookUrl.startsWith("http")) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String payload = String.format(
                        "{\"username\":\"%s\",\"content\":\"%s\",\"avatar_url\":\"https://crafthead.net/avatar/%s\"}",
                        escapeJson(name),
                        escapeJson(message),
                        uuid
                );

                URL               url  = URI.create(webhookUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    plugin.getPluginLogger().warn("Webhook returned HTTP " + responseCode);
                }
                conn.disconnect();

            } catch (IOException e) {
                plugin.getPluginLogger().error("Failed to send Discord webhook", e);
            }
        });
    }

    //  Helpers

    private String stripColor(String text) {
        if (text == null) return "";
        text = text.replaceAll("<[^>]+>", "");
        text = text.replaceAll("&#[0-9a-fA-F]{6}", "");
        text = text.replaceAll("&x(&[0-9a-fA-F]){6}", "");
        text = text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        return text;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public boolean isRunning() {
        return running;
    }
}
