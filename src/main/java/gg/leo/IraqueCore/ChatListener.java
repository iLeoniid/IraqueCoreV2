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
    private final LegacyComponentSerializer legacy;

    public ChatListener(IraqueCore plugin) {
        this.plugin = plugin;
        this.legacy = LegacyComponentSerializer.legacySection();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        String format = plugin.getConfigManager().getChatFormat();
        String prefix = "";
        String suffix = "";
        String color = "&7";
        boolean useRanks = plugin.getConfigManager().isUseRanks();

        if (useRanks) {
            Optional<Rank> rankOpt = plugin.getRankManager().getPlayerRank(player.getUniqueId());
            if (rankOpt.isPresent()) {
                Rank rank = rankOpt.get();
                prefix = rank.prefix();
                suffix = rank.suffix();
                color = rank.color();
            }
        }

        String tagStr = "";
        boolean useTags = plugin.getConfigManager().isUseTags();
        if (useTags) {
            String display = plugin.getTagManager().getPlayerTagDisplay(player);
            if (!display.isEmpty()) {
                tagStr = display + " ";
            }
        }

        String formatted = format
                .replace("{prefix}", prefix)
                .replace("{suffix}", suffix)
                .replace("{player}", color + player.getName())
                .replace("{displayname}", color + player.getDisplayName())
                .replace("{tag}", tagStr)
                .replace("{world}", player.getWorld().getName())
                .replace("{message}", message);

        Component rendered = legacy.deserialize(formatted.replace("&", "\u00a7"));

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
                discord.sendRawMessage(stripColor(msg
                        .replace("{player}", player.getName())));
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
                discord.sendRawMessage(stripColor(msg
                        .replace("{player}", player.getName())));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        DiscordManager discord = plugin.getDiscordManager();
        if (discord != null) {
            String msg = plugin.getConfigManager().getDeathMessage();
            if (!msg.isEmpty()) {
                String deathMsg = event.getDeathMessage();
                if (deathMsg != null) {
                    discord.sendRawMessage(stripColor(msg
                            .replace("{player}", event.getPlayer().getName())
                            .replace("{message}", deathMsg)));
                }
            }
        }
    }

    private String stripColor(String text) {
        return text.replaceAll("&[0-9a-fk-or]", "");
    }
}
