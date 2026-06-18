package gg.leo.IraqueCore;

import gg.leo.IraqueCore.discord.DiscordManager;
import gg.leo.IraqueCore.rank.Rank;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class ChatListener implements Listener {

    private final IraqueCore plugin;
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    public ChatListener(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player  = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        String format    = plugin.getConfigManager().getChatFormat();
        String prefix    = "";
        String suffix    = "";
        String color     = "&7";

        if (plugin.getConfigManager().isUseRanks()) {
            Optional<Rank> rankOpt = plugin.getRankManager().getPlayerRank(player.getUniqueId());
            if (rankOpt.isPresent()) {
                Rank rank = rankOpt.get();
                prefix = rank.prefix();
                suffix = rank.suffix();
                color  = rank.color();
            }
        }

        String tagStr = "";
        if (plugin.getConfigManager().isUseTags()) {
            String display = plugin.getTagManager().getPlayerTagDisplay(player);
            if (!display.isEmpty()) tagStr = display + " ";
        }

        // getDisplayName() on Paper 1.21+ returns String (legacy already translated)
        // If your API returns Component, serialize it: PlainTextComponentSerializer or LEGACY
        String displayName = color + player.getName(); // safe fallback
        try {
            // Paper 26: displayName() returns Component
            displayName = color + LEGACY.serialize(player.displayName());
        } catch (Exception ignored) {
            // In case the API changes between builds
        }

        String formatted = format
                .replace("{prefix}",      prefix)
                .replace("{suffix}",      suffix)
                .replace("{player}",      color + player.getName())
                .replace("{displayname}", displayName)
                .replace("{tag}",         tagStr)
                .replace("{world}",       player.getWorld().getName())
                .replace("{message}",     message);

        // Translate only valid & codes, not URLs nor &amp;
        Component rendered = LEGACY.deserialize(translateLegacy(formatted));
        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, msg) -> rendered));

        DiscordManager discord = plugin.getDiscordManager();
        if (discord != null) {
            discord.sendMinecraftToDiscord(player, message);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getRankManager().loadPlayer(player.getUniqueId());

        DiscordManager discord = plugin.getDiscordManager();
        if (discord != null) {
            String msg = plugin.getConfigManager().getJoinMessage();
            if (!msg.isEmpty()) {
                discord.sendRawMessage(stripColor(msg.replace("{player}", player.getName())));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRankManager().savePlayer(player.getUniqueId());

        DiscordManager discord = plugin.getDiscordManager();
        if (discord != null) {
            String msg = plugin.getConfigManager().getLeaveMessage();
            if (!msg.isEmpty()) {
                discord.sendRawMessage(stripColor(msg.replace("{player}", player.getName())));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        DiscordManager discord = plugin.getDiscordManager();
        if (discord == null) return;

        String msg = plugin.getConfigManager().getDeathMessage();
        if (msg.isEmpty()) return;

        // Modern Paper: deathMessage() returns Component (may be null)
        Component deathComponent = event.deathMessage();
        if (deathComponent == null) return;

        String deathMsg = PlainTextComponentSerializer.plainText().serialize(deathComponent);
        discord.sendRawMessage(stripColor(msg
                .replace("{player}",  event.getPlayer().getName())
                .replace("{message}", deathMsg)));
    }

    //  Helpers 

    /**
     * Removes all color formats:
     * MiniMessage tags, hex &#RRGGBB, Spigot hex, classic & codes.
     */
    private String stripColor(String text) {
        if (text == null) return "";
        text = text.replaceAll("<[^>]+>", "");
        text = text.replaceAll("&#[0-9a-fA-F]{6}", "");
        text = text.replaceAll("&x(&[0-9a-fA-F]){6}", "");
        text = text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        return text;
    }

    /**
     * Translates only valid & codes → § without touching URLs or &amp;.
     */
    private String translateLegacy(String text) {
        if (text == null) return "";
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }
}