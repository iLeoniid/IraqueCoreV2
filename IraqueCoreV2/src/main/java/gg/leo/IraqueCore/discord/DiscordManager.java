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
    private JDA jda;
    private TextChannel channel;
    private boolean running = false;

    public DiscordManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        String token = plugin.getConfigManager().getDiscordToken();
        if (token.isEmpty() || "YOUR_BOT_TOKEN_HERE".equals(token)) {
            plugin.getPluginLogger().warn("Discord token not configured!");
            return;
        }

        try {
            jda = JDABuilder.createLight(token, EnumSet.of(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT
            ))
                    .setMemberCachePolicy(MemberCachePolicy.NONE)
                    .addEventListeners(this)
                    .build();

            jda.awaitReady();

            String channelId = plugin.getConfigManager().getDiscordChannelId();
            if (channelId.isEmpty() || "YOUR_CHANNEL_ID_HERE".equals(channelId)) {
                plugin.getPluginLogger().warn("Discord channel ID not configured!");
                return;
            }

            channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                plugin.getPluginLogger().error("Discord channel not found: " + channelId);
                return;
            }

            running = true;
            plugin.getPluginLogger().info("Discord bot connected successfully!");
        } catch (Exception e) {
            plugin.getPluginLogger().error("Failed to start Discord bot", e);
        }
    }

    public void shutdown() {
        running = false;
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
        channel = null;
    }

    private String stripColor(String text) {
        return text.replaceAll("&[0-9a-fk-or]", "");
    }

    public void sendMinecraftToDiscord(Player player, String message) {
        if (!running || channel == null) return;

        String format = plugin.getConfigManager().getMinecraftToDiscordFormat();
        String formatted = format
                .replace("{player}", player.getName())
                .replace("{message}", message)
                .replace("{world}", player.getWorld().getName());

        var rankOpt = plugin.getRankManager().getPlayerRank(player.getUniqueId());
        if (rankOpt.isPresent()) {
            formatted = formatted.replace("{rank}", rankOpt.get().name());
        }

        formatted = stripColor(formatted);

        boolean useWebhooks = plugin.getConfigManager().isUseWebhooks();
        if (useWebhooks) {
            sendWebhookMessage(player.getName(), player.getUniqueId().toString(), message);
        } else {
            channel.sendMessage(formatted).queue();
        }
    }

    public void sendRawMessage(String message) {
        if (!running || channel == null) return;
        channel.sendMessage(stripColor(message)).queue();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!running) return;
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().equals(channel)) return;

        String content = event.getMessage().getContentDisplay();
        if (content.isEmpty()) return;

        String author = event.getAuthor().getName();
        String format = plugin.getConfigManager().getDiscordToMinecraftFormat();
        String formatted = format
                .replace("{author}", author)
                .replace("{message}", content);

        Component component = LegacyComponentSerializer.legacySection().deserialize(translateLegacy(formatted));
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(component));
    }

    private void sendWebhookMessage(String name, String uuid, String message) {
        String webhookUrl = plugin.getConfigManager().getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String payload = String.format(
                        "{\"username\":\"%s\",\"content\":\"%s\",\"avatar_url\":\"https://crafthead.net/avatar/%s\"}",
                        escapeJson(name),
                        escapeJson(message),
                        uuid
                );

                URL url = URI.create(webhookUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                conn.getResponseCode();
                conn.disconnect();
            } catch (IOException e) {
                plugin.getPluginLogger().error("Failed to send Discord webhook", e);
            }
        });
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String translateLegacy(String text) {
        return text.replace("&", "§");
    }

    public boolean isRunning() {
        return running;
    }
}
