package gg.leo.IraqueCore.chatcolor.menu;

import gg.leo.IraqueCore.chatcolor.ChatColor;
import gg.leo.IraqueCore.chatcolor.ChatColorManager;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.buttons.SimpleActionButton;
import gg.leo.IraqueCore.utils.menu.type.BorderedPaginatedMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class ChatColorMenu {

    private final ChatColorManager manager;

    public ChatColorMenu(ChatColorManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        ChatColorPaginatedMenu menu = new ChatColorPaginatedMenu(player);
        menu.updateMenu();
    }

    private class ChatColorPaginatedMenu extends BorderedPaginatedMenu {

        ChatColorPaginatedMenu(Player player) {
            super(player);
        }

        @Override
        public Map<Integer, Button> getPagesButtons(Player p) {
            List<ChatColor> available = manager.getColors().values().stream()
                    .filter(c -> c.getPermission().isEmpty() || p.hasPermission(c.getPermission()))
                    .toList();

            Map<Integer, Button> buttons = new LinkedHashMap<>();
            int index = 0;
            for (ChatColor color : available) {
                boolean isActive = isColorActive(p, color);
                buttons.put(index++, new ColorButton(color, p, isActive, manager, this));
            }
            return buttons;
        }

        @Override
        public String getTitle(Player p) {
            return org.bukkit.ChatColor.BLUE + "Select a Chat Color";
        }

        @Override
        public Map<Integer, Button> getHeaderItems(Player p) {
            Map<Integer, Button> headers = super.getHeaderItems(p);
            ChatColor active = manager.getActiveColor(p.getUniqueId());
            String current;
            if (active != null) {
                current = active.getChatColor() + ChatColorManager.proper(active.getDisplayName());
            } else {
                current = org.bukkit.ChatColor.GRAY + "None";
            }

            List<String> lore = List.of(
                    " ",
                    "&7Click to reset your current",
                    "&7chat color.",
                    " ",
                    "&eCurrently: " + current + " &eequipped",
                    " "
            );

            headers.put(4, new SimpleActionButton(
                    Material.PAPER, lore, "&cReset ChatColor", 0
            ).onClick((pl, slot) -> {
                manager.setActiveColor(pl.getUniqueId(), null);
                pl.sendMessage(org.bukkit.ChatColor.GREEN + "You have reset your chat color to normal.");
                this.updateMenu();
            }));
            return headers;
        }
    }

    private boolean isColorActive(Player player, ChatColor color) {
        ChatColor active = manager.getActiveColor(player.getUniqueId());
        return active != null && active.getId().equals(color.getId());
    }

    private static class ColorButton extends Button {

        private final ChatColor chatColor;
        private final Player player;
        private final boolean active;
        private final ChatColorManager manager;
        private final BorderedPaginatedMenu menu;

        ColorButton(ChatColor chatColor, Player player, boolean active, ChatColorManager manager, BorderedPaginatedMenu menu) {
            this.chatColor = chatColor;
            this.player = player;
            this.active = active;
            this.manager = manager;
            this.menu = menu;
        }

        @Override
        public Material getMaterial(Player p) {
            return Material.WHITE_WOOL;
        }

        @Override
        public List<String> getDescription(Player p) {
            List<String> desc = new ArrayList<>();
            desc.add("&6&m------------------");
            desc.add("&eColor:");
            desc.add("&e\u2502 &r" + chatColor.getChatColor() + ChatColorManager.proper(chatColor.getDisplayName()));
            desc.add(" ");
            desc.add("&eExample:");
            desc.add("&e\u2502 &r" + chatColor.getChatColor() + "Hello!");
            desc.add(" ");
            if (p.hasPermission(chatColor.getPermission())) {
                if (active) {
                    desc.add("&a\u2713 Currently selected");
                    desc.add("&7Click to deselect");
                } else {
                    desc.add("&aClick to select this color");
                }
            } else {
                desc.add("&cYou do not own this color");
            }
            desc.add("&6&m------------------");
            return desc;
        }

        @Override
        public String getDisplayName(Player p) {
            return org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    chatColor.getChatColor() + ChatColorManager.proper(chatColor.getDisplayName()));
        }

        @Override
        public int getData(Player p) {
            return 0;
        }

        @Override
        public void onClick(Player p, int slot, ClickType type) {
            if (!p.hasPermission(chatColor.getPermission())) {
                p.sendMessage(org.bukkit.ChatColor.RED + "You do not have permission to use this color!");
                return;
            }
            if (active) {
                manager.setActiveColor(p.getUniqueId(), null);
                p.sendMessage(org.bukkit.ChatColor.GREEN + "You have reset your chat color to normal.");
            } else {
                manager.setActiveColor(p.getUniqueId(), chatColor.getId());
                p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        "&aUpdated your chat color to " + chatColor.getChatColor()
                                + ChatColorManager.proper(chatColor.getDisplayName()) + "&a!"));
            }
            menu.updateMenu();
        }
    }
}
