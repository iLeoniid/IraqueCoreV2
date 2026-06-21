package gg.leo.IraqueCore.chatcolor;

import gg.leo.IraqueCore.chatcolor.menu.ChatColorMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatColorCommand implements CommandExecutor {

    private final ChatColorManager manager;

    public ChatColorCommand(ChatColorManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        ChatColorMenu menu = new ChatColorMenu(manager);
        menu.open(player);
        return true;
    }
}
