package gg.leo.IraqueCore.discord;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListener implements Listener {

    private final IraqueCore plugin;

    public AdvancementListener(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        DiscordManager discord = plugin.getDiscordManager();
        if (discord == null || !discord.isRunning()) return;

        var advancement = event.getAdvancement();
        var display = advancement.getDisplay();
        if (display == null) return;
        if (!display.doesAnnounceToChat()) return;

        String title = PlainTextComponentSerializer.plainText().serialize(display.title());
        String description = PlainTextComponentSerializer.plainText().serialize(display.description());
        String playerName = event.getPlayer().getName();

        String msg = plugin.getConfigManager().getMessage("discord.advancement",
                "\uD83C\uDFC6 **{player}** has made the advancement **{title}**\n*{description}*");
        msg = msg.replace("{player}", event.getPlayer().getName())
                .replace("{title}", title)
                .replace("{description}", description);

        discord.sendRawMessage(msg);
    }
}
