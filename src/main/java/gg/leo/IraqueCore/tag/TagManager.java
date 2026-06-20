package gg.leo.IraqueCore.tag;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.menu.TagMenu;
import gg.leo.IraqueCore.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TagManager implements Listener {

    private final IraqueCore plugin;
    private final Map<String, Tag> tags = new LinkedHashMap<>();
    private final Map<UUID, String> playerTags = new HashMap<>();
    private final TagMenu menu;
    private YamlConfiguration tagsConfig;
    private File tagsFile;

    private static final int PREV_BUTTON_SLOT = 48;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int NEXT_BUTTON_SLOT = 50;

    public TagManager(IraqueCore plugin) {
        this.plugin = plugin;
        this.menu = new TagMenu(plugin);
    }

    public void load() {
        tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        if (!tagsFile.exists()) {
            plugin.saveResource("tags.yml", false);
        }
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
        loadTags();
        loadPlayerTags();
    }

    public void reload() {
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
        loadTags();
        loadPlayerTags();
    }

    private void loadTags() {
        tags.clear();
        if (!tagsConfig.contains("tags")) return;

        for (String key : tagsConfig.getConfigurationSection("tags").getKeys(false)) {
            String path = "tags." + key;
            String displayName = ItemBuilder.color(
                    tagsConfig.getString(path + ".display-name", key));
            String tagText = ItemBuilder.color(
                    tagsConfig.getString(path + ".tag", ""));
            String permission = tagsConfig.getString(path + ".permission", "");
            String category = tagsConfig.getString(path + ".category", "emojis");
            String materialName = tagsConfig.getString(path + ".material", "NAME_TAG");
            Material material = Material.getMaterial(materialName);
            if (material == null) material = Material.NAME_TAG;

            List<String> lore = tagsConfig.getStringList(path + ".lore");
            List<String> coloredLore = lore.stream()
                    .map(ItemBuilder::color)
                    .collect(Collectors.toList());

            Tag tag = new Tag(key, displayName, tagText, permission, material, coloredLore);
            tag.setCategory(category);
            tags.put(key, tag);
        }
        plugin.getPluginLogger().info("Loaded {} tags", tags.size());
    }

    private void loadPlayerTags() {
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
        playerTags.clear();
        if (!tagsConfig.contains("players")) return;

        for (String uuidStr : tagsConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String tagId = tagsConfig.getString("players." + uuidStr + ".tag");
                if (tagId != null && !tagId.equals("null") && tags.containsKey(tagId)) {
                    playerTags.put(uuid, tagId);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void savePlayerTags() {
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
        tagsConfig.set("players", null);
        for (Map.Entry<UUID, String> entry : playerTags.entrySet()) {
            tagsConfig.set("players." + entry.getKey().toString() + ".tag", entry.getValue());
        }
        try {
            tagsConfig.save(tagsFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save tags.yml", e);
        }
    }

    // ─── GUI ───

    public void openMainMenu(Player player) {
        menu.openMainMenu(player);
    }

    public void openCategoryGUI(Player player, String category, int page) {
        menu.openCategoryGUI(player, category, page);
    }

    public List<Tag> getTagsByCategory(String category, Player player) {
        return tags.values().stream()
                .filter(tag -> tag.getCategory().equalsIgnoreCase(category))
                .filter(tag -> tag.getPermission().isEmpty() || player.hasPermission(tag.getPermission()))
                .collect(Collectors.toList());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        if (title.contains("Selecionar Categoria")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getSlot();
            if (slot == 11 && clicked.getType() == Material.NETHER_STAR) {
                openCategoryGUI(player, "emojis", 0);
            } else if (slot == 13 && clicked.getType() == Material.PAPER) {
                openCategoryGUI(player, "text", 0);
            } else if (slot == 15 && clicked.getType() == Material.DRAGON_HEAD) {
                openCategoryGUI(player, "especial", 0);
            } else if (slot == 22 && clicked.getType() == Material.BARRIER) {
                if (!getPlayerTagDisplay(player).isEmpty()) {
                    setPlayerTag(player, null);
                    plugin.getRankManager().updatePlayerRankVisuals(player);
                    player.sendMessage(txt("tag.gui.remove-success"));
                    openMainMenu(player);
                } else {
                    player.sendMessage(txt("tag.gui.no-tag-equipped-action"));
                }
            }
            return;
        }

        if (title.contains("Emojis") || title.contains("Texto") || title.contains("Especial")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getSlot();

            if (slot == PREV_BUTTON_SLOT && clicked.getType() == Material.ARROW) {
                int currentPage = menu.getPlayerPage(player);
                String category = menu.getPlayerCategory(player);
                openCategoryGUI(player, category, currentPage - 1);
                return;
            }

            if (slot == BACK_BUTTON_SLOT && clicked.getType() == Material.BARRIER) {
                openMainMenu(player);
                return;
            }

            if (slot == NEXT_BUTTON_SLOT && clicked.getType() == Material.ARROW) {
                int currentPage = menu.getPlayerPage(player);
                String category = menu.getPlayerCategory(player);
                openCategoryGUI(player, category, currentPage + 1);
                return;
            }

            handleTagClick(clicked, player);
        }
    }

    private void handleTagClick(ItemStack clicked, Player player) {
        for (Tag tag : tags.values()) {
            if (clicked.getType() == tag.getMaterial()
                    && clicked.hasItemMeta()
                    && clicked.getItemMeta().hasDisplayName()
                    && clicked.getItemMeta().getDisplayName().equals(tag.getDisplayName())) {

                if (!tag.getPermission().isEmpty() && !player.hasPermission(tag.getPermission())) {
                    player.sendMessage(txt("tag.gui.no-permission"));
                    return;
                }

                if (hasTagEquipped(player, tag.getId())) {
                    setPlayerTag(player, null);
                    player.sendMessage(txt("tag.gui.removed", "{tag}", tag.getDisplayName()));
                } else {
                    setPlayerTag(player, tag.getId());
                    player.sendMessage(txt("tag.gui.equipped", "{tag}", tag.getDisplayName()));
                }

                plugin.getRankManager().updatePlayerRankVisuals(player);

                String category = menu.getPlayerCategory(player);
                int page = menu.getPlayerPage(player);
                openCategoryGUI(player, category, page);
                return;
            }
        }
    }

    public void setPlayerTag(Player player, String tagId) {
        if (tagId == null) {
            playerTags.remove(player.getUniqueId());
        } else if (tags.containsKey(tagId)) {
            playerTags.put(player.getUniqueId(), tagId);
        }
        savePlayerTags();
    }

    public String getPlayerTagDisplay(Player player) {
        String tagId = playerTags.get(player.getUniqueId());
        if (tagId != null) {
            Tag tag = tags.get(tagId);
            if (tag != null) return tag.getTag();
        }
        return "";
    }

    public boolean hasTagEquipped(Player player, String tagId) {
        return tagId.equals(playerTags.get(player.getUniqueId()));
    }

    public Tag getTag(String id) {
        return tags.get(id);
    }

    public Map<String, Tag> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    private Component txt(String path) {
        return plugin.getConfigManager().getMessageComponent(path);
    }

    private Component txt(String path, String placeholder, String value) {
        return plugin.getConfigManager().deserialize(
                plugin.getConfigManager().translateAndReplace(
                        plugin.getConfigManager().getMessage(path, "&c" + path),
                        placeholder, value));
    }

    private String legacyMsg(String path) {
        return plugin.getConfigManager().toLegacyMessage(path);
    }

    private String legacyMsg(String path, String placeholder, String value) {
        return plugin.getConfigManager().toLegacyMessage(path, placeholder, value);
    }
}
