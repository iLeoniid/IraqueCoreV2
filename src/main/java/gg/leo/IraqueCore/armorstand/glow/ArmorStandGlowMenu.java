package gg.leo.IraqueCore.armorstand.glow;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.armorstand.ArmorStandEditor;
import gg.leo.IraqueCore.armorstand.menu.ArmorStandMenu;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class ArmorStandGlowMenu extends Menu {

    private final IraqueCore plugin;
    private final ArmorStand stand;
    private final ArmorStandEditor editor;

    public ArmorStandGlowMenu(IraqueCore plugin, Player player, ArmorStand stand, ArmorStandEditor editor) {
        super(player);
        this.plugin = plugin;
        this.stand = stand;
        this.editor = editor;
        this.staticSize = 36;
        this.placeholder = true;
    }

    @Override
    public Map<Integer, Button> getButtons(Player p) {
        Map<Integer, Button> buttons = new LinkedHashMap<>();

        buttons.put(4, infoButton());

        int slot = 9;
        for (GlowColor color : GlowColor.values()) {
            buttons.put(slot++, new GlowColorButton(color, isActive(color)));
        }

        buttons.put(31, noGlowButton());
        buttons.put(33, backButton());

        return buttons;
    }

    @Override
    public String getTitle(Player p) {
        return ChatColor.DARK_GRAY + "Armor Stand Glow";
    }

    private boolean isActive(GlowColor glowColor) {
        if (!stand.isGlowing()) return false;
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team t : board.getTeams()) {
            if (t.getName().startsWith("irqglow_") && t.hasEntry(stand.getUniqueId().toString())) {
                return t.getName().equals("irqglow_" + glowColor.name());
            }
        }
        return false;
    }

    private void setGlow(GlowColor glowColor) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Team t : board.getTeams()) {
            if (t.getName().startsWith("irqglow_") && t.hasEntry(stand.getUniqueId().toString())) {
                t.removeEntry(stand.getUniqueId().toString());
            }
        }

        if (glowColor == null) {
            stand.setGlowing(false);
            return;
        }

        stand.setGlowing(true);

        String teamName = "irqglow_" + glowColor.name();
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.color(glowColor.named());
        }
        team.addEntry(stand.getUniqueId().toString());
    }

    private Button infoButton() {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.GLOWSTONE_DUST; }
            @Override public List<String> getDescription(Player p) {
                GlowColor current = null;
                if (stand.isGlowing()) {
                    Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                    for (Team t : board.getTeams()) {
                        if (t.getName().startsWith("irqglow_") && t.hasEntry(stand.getUniqueId().toString())) {
                            String name = t.getName().replace("irqglow_", "");
                            try { current = GlowColor.valueOf(name); } catch (Exception ignored) {}
                            break;
                        }
                    }
                }
                String colorName = current != null ? current.display() : "&7None";
                return List.of(
                        "&7Select a glow color for",
                        "&7the armor stand.",
                        " ",
                        "&eCurrent: &r" + colorName
                );
            }
            @Override public String getDisplayName(Player p) { return "&bGlow Color"; }
            @Override public int getData(Player p) { return 0; }
            @Override public void onClick(Player p, int slot, ClickType type) {}
        };
    }

    private Button noGlowButton() {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.BARRIER; }
            @Override public List<String> getDescription(Player p) {
                return List.of("&7Disables the glow effect");
            }
            @Override public String getDisplayName(Player p) { return "&cNo Glow"; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                setGlow(null);
                // Refrescar el menú actual, no crear uno nuevo
                openMenu();
            }
        };
    }

    private Button backButton() {
        return new Button() {
            @Override public Material getMaterial(Player p) { return Material.ARROW; }
            @Override public List<String> getDescription(Player p) {
                return List.of("&7Return to Armor Stand Editor");
            }
            @Override public String getDisplayName(Player p) { return "&aBack"; }
            @Override public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {
                new ArmorStandMenu(plugin, p, stand, editor).openMenu();
            }
        };
    }

    public enum GlowColor {
        DARK_RED("&4Dark Red",    NamedTextColor.DARK_RED,    Material.RED_WOOL),
        RED("&cRed",              NamedTextColor.RED,         Material.RED_WOOL),
        GOLD("&6Gold",            NamedTextColor.GOLD,        Material.ORANGE_WOOL),
        YELLOW("&eYellow",        NamedTextColor.YELLOW,      Material.YELLOW_WOOL),
        DARK_GREEN("&2Dark Green",NamedTextColor.DARK_GREEN,  Material.GREEN_WOOL),
        GREEN("&aGreen",          NamedTextColor.GREEN,       Material.LIME_WOOL),
        AQUA("&bAqua",            NamedTextColor.AQUA,        Material.LIGHT_BLUE_WOOL),
        DARK_AQUA("&3Dark Aqua",  NamedTextColor.DARK_AQUA,   Material.CYAN_WOOL),
        DARK_BLUE("&1Dark Blue",  NamedTextColor.DARK_BLUE,   Material.BLUE_WOOL),
        BLUE("&9Blue",            NamedTextColor.BLUE,        Material.BLUE_WOOL),
        LIGHT_PURPLE("&dLight Purple", NamedTextColor.LIGHT_PURPLE, Material.MAGENTA_WOOL),
        DARK_PURPLE("&5Dark Purple",   NamedTextColor.DARK_PURPLE,  Material.PURPLE_WOOL),
        WHITE("&fWhite",          NamedTextColor.WHITE,       Material.WHITE_WOOL),
        GRAY("&7Gray",            NamedTextColor.GRAY,        Material.LIGHT_GRAY_WOOL),
        DARK_GRAY("&8Dark Gray",  NamedTextColor.DARK_GRAY,   Material.GRAY_WOOL),
        BLACK("&0Black",          NamedTextColor.BLACK,       Material.BLACK_WOOL);

        private final String display;
        private final NamedTextColor named;
        private final Material material;

        GlowColor(String display, NamedTextColor named, Material material) {
            this.display = display;
            this.named = named;
            this.material = material;
        }

        public String display() { return display; }
        public NamedTextColor named() { return named; }
        public Material material() { return material; }
    }

    private class GlowColorButton extends Button {

        private final GlowColor color;
        private final boolean active;

        GlowColorButton(GlowColor color, boolean active) {
            this.color = color;
            this.active = active;
        }

        @Override
        public Material getMaterial(Player p) {
            return color.material();
        }

        @Override
        public List<String> getDescription(Player p) {
            List<String> lore = new ArrayList<>();
            lore.add(" ");
            if (active) {
                lore.add("&a\u2713 Currently selected");
                lore.add("&7Click to deselect");
            } else {
                lore.add("&7Click to select this color");
            }
            return lore;
        }

        @Override
        public String getDisplayName(Player p) {
            return ChatColor.translateAlternateColorCodes('&', color.display());
        }

        @Override
        public int getData(Player p) {
            return 0;
        }

        @Override
        public void onClick(Player p, int slot, ClickType type) {
            if (active) {
                setGlow(null);
                if (!stand.isGlowing()) {
                    stand.setGlowing(true);
                }
            } else {
                setGlow(color);
            }
            // Refrescar el menú actual, no crear uno nuevo
            openMenu();
        }
    }
}