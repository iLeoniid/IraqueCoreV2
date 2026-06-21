package gg.leo.IraqueCore.tag;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.menu.TagMenu;
import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TagManager {

    private final IraqueCore plugin;
    private final Map<String, Tag> tags = new LinkedHashMap<>();
    private final Map<UUID, String> playerTags = new HashMap<>();
    private final TagMenu menu;
    private YamlConfiguration tagsConfig;
    private File tagsFile;



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
        String equipped = playerTags.get(player.getUniqueId());
        return equipped != null && equipped.equalsIgnoreCase(tagId);
    }

    public Tag getTag(String id) {
        return tags.get(id);
    }

    public Map<String, Tag> getTags() {
        return Collections.unmodifiableMap(tags);
    }

}
