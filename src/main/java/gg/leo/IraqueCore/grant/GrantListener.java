package gg.leo.IraqueCore.grant;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GrantListener implements Listener {

    private final IraqueCore plugin;
    private final GrantMenu grantMenu;

    public GrantListener(IraqueCore plugin) {
        this.plugin = plugin;
        this.grantMenu = new GrantMenu(plugin);
    }

    public GrantMenu getGrantMenu() {
        return grantMenu;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (grantMenu.handleChatInput(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
        }
    }

    public void cleanupPlayer(org.bukkit.entity.Player player) {
        grantMenu.cleanupPlayer(player);
    }
}
