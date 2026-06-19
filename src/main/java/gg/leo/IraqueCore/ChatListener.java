package gg.leo.IraqueCore;

import gg.leo.IraqueCore.discord.DiscordManager;
import gg.leo.IraqueCore.rank.Rank;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
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

                if (plugin.getAfkManager() != null && plugin.getAfkManager().isAfk(player.getUniqueId())) {
                    prefix = plugin.getAfkManager().getAfkPrefix();
                } else {
                    prefix = rank.prefix();
                }

                suffix = rank.suffix();
                color  = rank.color();
            }
        }

        String tagStr = "";
        if (plugin.getConfigManager().isUseTags()) {
            String display = plugin.getTagManager().getPlayerTagDisplay(player);
            if (!display.isEmpty()) tagStr = display + " ";
        }

        String displayName = color + player.getName();
        try {
            displayName = color + net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(player.displayName());
        } catch (Exception ignored) {
        }

        String formatted = format
                .replace("{prefix}",      prefix)
                .replace("{suffix}",      suffix)
                .replace("{player}",      color + player.getName())
                .replace("{displayname}", displayName)
                .replace("{tag}",         tagStr)
                .replace("{world}",       player.getWorld().getName())
                .replace("{message}",     message);

        Component rendered = plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(formatted));
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
        plugin.getRankManager().applyPermissions(player);

        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getRankManager().initVisuals(player);
            }
        }, 12L);

        if (!player.hasPlayedBefore()) {
            event.joinMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("join.first", "&6Welcome {player}!")
                                    .replace("{player}", player.getName()))));
        } else {
            event.joinMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translate(
                            plugin.getConfigManager().getMessage("join.normal", "&8[&a+&8] &7{player} &ejoined")
                                    .replace("{player}", player.getName()))));
        }

        DiscordManager discord = plugin.getDiscordManager();
        if (discord != null) {
            String msg = plugin.getConfigManager().getJoinMessage();
            if (!msg.isEmpty()) {
                String formatted = stripColor(msg.replace("{player}", player.getName()));
                discord.sendRawMessage(formatted);
                if (plugin.getConfigManager().isUseWebhooks()) {
                    discord.sendWebhookEvent("join", player.getName(), player.getUniqueId().toString(), formatted);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getRankManager().removePermissions(player);
        plugin.getRankManager().removePlayerFromTeams(player);
        plugin.getRankManager().savePlayer(player.getUniqueId());

        event.quitMessage(plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translate(
                        plugin.getConfigManager().getMessage("quit.normal", "&8[&c-&8] &7{player} &eleft")
                                .replace("{player}", player.getName()))));

        DiscordManager discord = plugin.getDiscordManager();
        if (discord != null) {
            String msg = plugin.getConfigManager().getLeaveMessage();
            if (!msg.isEmpty()) {
                String formatted = stripColor(msg.replace("{player}", player.getName()));
                discord.sendRawMessage(formatted);
                if (plugin.getConfigManager().isUseWebhooks()) {
                    discord.sendWebhookEvent("leave", player.getName(), player.getUniqueId().toString(), formatted);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        DiscordManager discord = plugin.getDiscordManager();
        if (discord == null) return;

        String msg = plugin.getConfigManager().getDeathMessage();
        if (msg.isEmpty()) return;

        Component deathComponent = event.deathMessage();
        if (deathComponent == null) return;

        Player deadPlayer = event.getPlayer();
        String deathMsg = PlainTextComponentSerializer.plainText().serialize(deathComponent);
        String formatted = stripColor(msg
                .replace("{player}",  deadPlayer.getName())
                .replace("{message}", deathMsg));
        discord.sendRawMessage(formatted);
        if (plugin.getConfigManager().isUseWebhooks()) {
            discord.sendWebhookEvent("death", deadPlayer.getName(), deadPlayer.getUniqueId().toString(), formatted);
        }
    }

    private String stripColor(String text) {
        if (text == null) return "";
        text = text.replaceAll("<[^>]+>", "");
        text = text.replaceAll("&#[0-9a-fA-F]{6}", "");
        text = text.replaceAll("&x(&[0-9a-fA-F]){6}", "");
        text = text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        return text;
    }

}
