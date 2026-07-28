package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.tag.Tag;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.type.BorderedPaginatedMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;
import java.util.stream.Collectors;

public class TagMenu {

    private final IraqueCore plugin;

    public TagMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        var tagManager = plugin.getTagManager();
        int emojisCount = tagManager.getTagsByCategory("emojis", player).size();
        int textCount = tagManager.getTagsByCategory("text", player).size();
        int especialCount = tagManager.getTagsByCategory("especial", player).size();
        String currentTagText = tagManager.getPlayerTagDisplay(player);

        new Menu(player) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();

                buttons.put(11, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.NETHER_STAR; }
                    @Override public List<String> getDescription(Player p) {
                        return List.of(
                                "&7Tags con emojis y simbolos",
                                "&7Total: &f" + emojisCount + " tags",
                                "",
                                "&a\u25B8 Click para abrir"
                        );
                    }
                    @Override public String getDisplayName(Player p) { return "&e\u2B50 Emojis"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) { openCategoryGUI(p, "emojis", 0); }
                });
                buttons.put(13, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.PAPER; }
                    @Override public List<String> getDescription(Player p) {
                        return List.of(
                                "&7Tags con texto personalizado",
                                "&7Total: &f" + textCount + " tags",
                                "",
                                "&a\u25B8 Click para abrir"
                        );
                    }
                    @Override public String getDisplayName(Player p) { return "&bTexto"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) { openCategoryGUI(p, "text", 0); }
                });
                buttons.put(15, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.DRAGON_HEAD; }
                    @Override public List<String> getDescription(Player p) {
                        return List.of(
                                "&7Tags unicas y exclusivas",
                                "&7Total: &f" + especialCount + " tags",
                                "",
                                "&a\u25B8 Click para abrir"
                        );
                    }
                    @Override public String getDisplayName(Player p) { return "&d\u2728 Especial"; }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) { openCategoryGUI(p, "especial", 0); }
                });
                buttons.put(22, new Button() {
                    @Override public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override public List<String> getDescription(Player p) {
                        if (currentTagText.isEmpty()) {
                            return List.of(legacyMsg("tag.gui.no-tag-equipped"));
                        }
                        return List.of(
                                legacyMsg("tag.gui.current-tag", "{tag}", currentTagText),
                                "",
                                legacyMsg("tag.gui.click-remove")
                        );
                    }
                    @Override public String getDisplayName(Player p) { return "&c\u2716 " + legacyMsg("tag.gui.remove-title"); }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) {
                        if (!tagManager.getPlayerTagDisplay(p).isEmpty()) {
                            tagManager.setPlayerTag(p, null);
                            plugin.getRankManager().updatePlayerRankVisuals(p);
                            p.sendMessage(plugin.getConfigManager().getMessageComponent("tag.gui.remove-success"));
                            // Refrescar el menú actual, no crear uno nuevo
                            openMainMenu(p);
                        } else {
                            p.sendMessage(plugin.getConfigManager().getMessageComponent("tag.gui.no-tag-equipped-action"));
                        }
                    }
                });

                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return ChatColor.DARK_PURPLE + "\u00BB " + ChatColor.LIGHT_PURPLE + "Selecionar Categoria";
            }
        }.openMenu();
    }

    public void openCategoryGUI(Player player, String category, int page) {
        var tagManager = plugin.getTagManager();
        List<Tag> categoryTags = tagManager.getTagsByCategory(category, player);

        if (categoryTags.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessageComponent("tag.gui.category-empty"));
            return;
        }

        String categoryName = getCategoryDisplayName(category);
        String title = ChatColor.BLUE + categoryName;

        // Crear el menú y guardar referencia para refrescar
        TagPaginatedMenu menu = new TagPaginatedMenu(player, categoryTags, title, category, tagManager);
        menu.currentPage = Math.max(1, Math.min(page + 1, Math.max(1, (int) Math.ceil((double) categoryTags.size() / menu.getButtonsPerPage()))));
        menu.updateMenu();
    }

    private class TagPaginatedMenu extends BorderedPaginatedMenu {
        private final List<Tag> categoryTags;
        private final String menuTitle;
        private final String category;
        private final gg.leo.IraqueCore.tag.TagManager tagManager;

        TagPaginatedMenu(Player player, List<Tag> categoryTags, String title, String category,
                         gg.leo.IraqueCore.tag.TagManager tagManager) {
            super(player);
            this.categoryTags = categoryTags;
            this.menuTitle = title;
            this.category = category;
            this.tagManager = tagManager;
        }

        @Override
        public Map<Integer, Button> getPagesButtons(Player p) {
            Map<Integer, Button> buttons = new LinkedHashMap<>();
            int index = 0;
            for (Tag tag : categoryTags) {
                List<String> lore = new ArrayList<>(tag.getLore().stream()
                        .map(l -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', l)))
                        .map(l -> "&7" + l)
                        .toList());
                lore.add("");
                boolean equipped = tagManager.hasTagEquipped(p, tag.getId());
                if (equipped) {
                    lore.add("&a\u2713 Equipada");
                    lore.add("&7Click para remover");
                } else {
                    lore.add("&e\u25B8 Click para equipar");
                }
                List<String> fLore = lore;
                String tagId = tag.getId();
                buttons.put(index, new Button() {
                    @Override public Material getMaterial(Player p) { return tag.getMaterial(); }
                    @Override public List<String> getDescription(Player p) { return fLore; }
                    @Override public String getDisplayName(Player p) { return tag.getDisplayName(); }
                    @Override public int getData(Player p) { return 0; }
                    @Override public void onClick(Player p, int slot, ClickType type) {
                        handleTagClick(p, tag, tagId, category, currentPage - 1);
                    }
                });
                index++;
            }
            return buttons;
        }

        @Override
        public String getTitle(Player p) {
            return menuTitle;
        }

        @Override
        public Map<Integer, Button> getHeaderItems(Player p) {
            Map<Integer, Button> headers = super.getHeaderItems(p);
            headers.put(40, new Button() {
                @Override public Material getMaterial(Player p) { return Material.BARRIER; }
                @Override public List<String> getDescription(Player p) { return List.of("&7Volver al menu de categorias"); }
                @Override public String getDisplayName(Player p) { return "&c\u2190 Volver"; }
                @Override public int getData(Player p) { return 0; }
                @Override public void onClick(Player p, int slot, ClickType type) { openMainMenu(p); }
            });
            return headers;
        }
    }

    private void handleTagClick(Player player, Tag tag, String tagId, String category, int currentPageIndex) {
        var tagManager = plugin.getTagManager();

        if (!tag.getPermission().isEmpty() && !player.hasPermission(tag.getPermission())) {
            player.sendMessage(plugin.getConfigManager().getMessageComponent("tag.gui.no-permission"));
            return;
        }

        String display = tag.getDisplayName();
        if (tagManager.hasTagEquipped(player, tagId)) {
            tagManager.setPlayerTag(player, null);
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translateAndReplace(
                            plugin.getConfigManager().getMessage("tag.gui.removed", "&cTag removed"),
                            "{tag}", display)));
        } else {
            tagManager.setPlayerTag(player, tagId);
            player.sendMessage(plugin.getConfigManager().deserialize(
                    plugin.getConfigManager().translateAndReplace(
                            plugin.getConfigManager().getMessage("tag.gui.equipped", "&aTag equipped"),
                            "{tag}", display)));
        }

        plugin.getRankManager().updatePlayerRankVisuals(player);
        // Mantener la página actual
        openCategoryGUI(player, category, currentPageIndex);
    }

    private String getCategoryDisplayName(String category) {
        return switch (category.toLowerCase()) {
            case "emojis" -> "\u2B50 Emojis";
            case "text" -> "Texto";
            case "especial" -> "\u2728 Especial";
            default -> category;
        };
    }

    private String legacyMsg(String path) {
        return plugin.getConfigManager().toLegacyMessage(path);
    }

    private String legacyMsg(String path, String placeholder, String value) {
        return plugin.getConfigManager().toLegacyMessage(path, placeholder, value);
    }
}