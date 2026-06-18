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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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

    // `volatile` guarantees visibility across threads
    private volatile boolean running   = false;
    private volatile boolean cancelled = false;

    public DiscordManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    //  Lifecycle ──

    /**
     * Starts the bot in an async thread to avoid blocking the server's main thread.
     * awaitReady() can take several seconds — should never be called on the main thread.
     */
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

                // Si shutdown() fue llamado mientras esperábamos, cancelamos limpiamente
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

                // Asignación atómica antes de poner running = true
                jda     = built;
                channel = ch;
                running = true;

                plugin.getPluginLogger().info("Discord bot connected successfully!");

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
    }

    //  Minecraft → Discord 

    public void sendMinecraftToDiscord(Player player, String message) {
        if (!running || channel == null) return;

        String format = plugin.getConfigManager().getMinecraftToDiscordFormat();

        // {rank} — empty string if the player has no assigned rank
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
            sendWebhookMessage(player.getName(), player.getUniqueId().toString(), message);
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

    //  Discord → Minecraft 

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!running) return;
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().equals(channel)) return;

        String content = event.getMessage().getContentDisplay();
        if (content.isBlank()) return;

        String author    = event.getAuthor().getName();
        String format    = plugin.getConfigManager().getDiscordToMinecraftFormat();
        String formatted = format
                .replace("{author}",  author)
                .replace("{message}", content);

        // Translate & codes (only valid ones, not URLs nor &amp; etc.)
        Component component = LegacyComponentSerializer.legacySection()
                .deserialize(translateLegacy(formatted));

        // Send to main thread — never modify Bukkit state from JDA thread
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component))
        );
    }

    //  Webhook 

    private void sendWebhookMessage(String name, String uuid, String message) {
        String webhookUrl = plugin.getConfigManager().getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) return;

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

    /**
     * Removes all color formats supported by the plugin:
     *  - MiniMessage tags  : &lt;gold&gt;, &lt;bold&gt;, &lt;#RRGGBB&gt;, etc.
     *  - Hex legacy        : &amp;#RRGGBB
     *  - Spigot hex        : &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B
     *  - Classic &amp; codes   : &amp;6, &amp;l, &amp;r, etc.
     */
    private String stripColor(String text) {
        if (text == null) return "";
        // MiniMessage tags (<gold>, <bold>, <#RRGGBB>, </gold>, etc.)
        text = text.replaceAll("<[^>]+>", "");
        // Hex &#RRGGBB
        text = text.replaceAll("&#[0-9a-fA-F]{6}", "");
        // Spigot hex &x&R&R&G&G&B&B
        text = text.replaceAll("&x(&[0-9a-fA-F]){6}", "");
        // Classic & codes (case-insensitive)
        text = text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        return text;
    }

    /**
     * Translates valid &amp; codes to § for LegacyComponentSerializer.
     * Only replaces &amp; followed by a valid code — does not touch URLs or &amp;amp;.
     */
    private String translateLegacy(String text) {
        if (text == null) return "";
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
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