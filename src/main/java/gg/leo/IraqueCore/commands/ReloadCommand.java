package gg.leo.IraqueCore.commands;

import gg.leo.IraqueCore.IraqueCore;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand
implements TabExecutor {
    private final IraqueCore plugin;

    public ReloadCommand(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("iraquecore.reload")) {
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate(this.plugin.getConfigManager().getMessage("general.no-permission", "&cYou don't have permission."))));
            return true;
        }
        try {
            String reloaded = this.plugin.reload();
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().getPrefixedMessage("reload.success")));
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().translate("&7Recargado: &f" + reloaded)));
        }
        catch (Exception e) {
            sender.sendMessage(this.plugin.getConfigManager().deserialize(this.plugin.getConfigManager().getPrefixedMessage("reload.failed")));
            this.plugin.getPluginLogger().error("Error reloading plugin", e);
        }
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
