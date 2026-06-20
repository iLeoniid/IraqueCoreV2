package gg.leo.IraqueCore.anvil;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilColorListener implements Listener {

    private final IraqueCore plugin;

    public AnvilColorListener(IraqueCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        if (!player.hasPermission("iraquecore.anvilcolors")) return;

        String renameText = event.getView().getRenameText();
        if (renameText == null || renameText.isEmpty()) return;

        ItemStack result = event.getResult();
        if (result == null || result.getType().isAir()) return;

        String colored = ItemBuilder.color("&r" + renameText.replace("\u00A7", "&"));

        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        meta.displayName(LegacyComponentSerializer.legacySection().deserialize(colored));
        result.setItemMeta(meta);
        event.setResult(result);
    }
}
