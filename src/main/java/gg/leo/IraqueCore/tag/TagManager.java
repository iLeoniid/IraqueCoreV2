package gg.leo.IraqueCore.tag;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TagManager implements Listener {

    private final IraqueCore plugin;
    private final Map<String, Tag> tags = new LinkedHashMap<>();
    private final Map<UUID, String> playerTags = new HashMap<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, String> playerCategory = new HashMap<>();
    private YamlConfiguration tagsConfig;
    private File tagsFile;

    private static final int ITEMS_PER_PAGE = 45;
    private static final int PREV_BUTTON_SLOT = 48;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int NEXT_BUTTON_SLOT = 50;

    public TagManager(IraqueCore plugin) {
        this.plugin = plugin;
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
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_PURPLE + "\u00BB " + ChatColor.LIGHT_PURPLE + "Selecionar Categoria");

        ItemStack emojisItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta emojisMeta = emojisItem.getItemMeta();
        emojisMeta.setDisplayName(ChatColor.YELLOW + "\u2B50 Emojis");
        int emojisCount = getTagsByCategory("emojis", player).size();
        emojisMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Tags con emojis y simbolos",
                ChatColor.GRAY + "Total: " + ChatColor.WHITE + emojisCount + " tags",
                "",
                ChatColor.GREEN + "\u25B8 Click para abrir"
        ));
        emojisItem.setItemMeta(emojisMeta);
        gui.setItem(11, emojisItem);

        ItemStack textItem = new ItemStack(Material.PAPER);
        ItemMeta textMeta = textItem.getItemMeta();
        textMeta.setDisplayName(ChatColor.AQUA + "Texto");
        int textCount = getTagsByCategory("text", player).size();
        textMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Tags con texto personalizado",
                ChatColor.GRAY + "Total: " + ChatColor.WHITE + textCount + " tags",
                "",
                ChatColor.GREEN + "\u25B8 Click para abrir"
        ));
        textItem.setItemMeta(textMeta);
        gui.setItem(13, textItem);

        ItemStack especialItem = new ItemStack(Material.DRAGON_HEAD);
        ItemMeta especialMeta = especialItem.getItemMeta();
        especialMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "\u2728 Especial");
        int especialCount = getTagsByCategory("especial", player).size();
        especialMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Tags unicas y exclusivas",
                ChatColor.GRAY + "Total: " + ChatColor.WHITE + especialCount + " tags",
                "",
                ChatColor.GREEN + "\u25B8 Click para abrir"
        ));
        especialItem.setItemMeta(especialMeta);
        gui.setItem(15, especialItem);

        String currentTagText = getPlayerTagDisplay(player);
        ItemStack removeItem = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeItem.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "\u2716 " + legacyMsg("tag.gui.remove-title"));
        if (currentTagText.isEmpty()) {
            removeMeta.setLore(Arrays.asList(
                    legacyMsg("tag.gui.no-tag-equipped")
            ));
        } else {
            removeMeta.setLore(Arrays.asList(
                    legacyMsg("tag.gui.current-tag", "{tag}", currentTagText),
                    "",
                    legacyMsg("tag.gui.click-remove")
            ));
        }
        removeItem.setItemMeta(removeMeta);
        gui.setItem(22, removeItem);

        player.openInventory(gui);
    }

    public void openCategoryGUI(Player player, String category, int page) {
        List<Tag> categoryTags = getTagsByCategory(category, player);

        if (categoryTags.isEmpty()) {
            player.sendMessage(txt("tag.gui.category-empty"));
            return;
        }

        int totalPages = (int) Math.ceil((double) categoryTags.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        playerPages.put(player.getUniqueId(), page);
        playerCategory.put(player.getUniqueId(), category);

        String categoryName = getCategoryDisplayName(category);
        String title = ChatColor.BLUE + categoryName + " " + ChatColor.GRAY + "(" + (page + 1) + "/" + totalPages + ")";
        Inventory gui = Bukkit.createInventory(null, 54, title);

        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, categoryTags.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            gui.setItem(slot, createTagItem(categoryTags.get(i), player));
            slot++;
        }

        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "\u2190 Pagina Anterior");
            prevMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Ir para a pagina " + page
            ));
            prevButton.setItemMeta(prevMeta);
            gui.setItem(PREV_BUTTON_SLOT, prevButton);
        }

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "\u2190 Volver");
        backMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Volver al menu de categorias"
        ));
        backButton.setItemMeta(backMeta);
        gui.setItem(BACK_BUTTON_SLOT, backButton);

        if (page < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Pagina Siguiente \u2192");
            nextMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Ir para a pagina " + (page + 2)
            ));
            nextButton.setItemMeta(nextMeta);
            gui.setItem(NEXT_BUTTON_SLOT, nextButton);
        }

        player.openInventory(gui);
    }

    private List<Tag> getTagsByCategory(String category, Player player) {
        return tags.values().stream()
                .filter(tag -> tag.getCategory().equalsIgnoreCase(category))
                .filter(tag -> tag.getPermission().isEmpty() || player.hasPermission(tag.getPermission()))
                .collect(Collectors.toList());
    }

    private String getCategoryDisplayName(String category) {
        switch (category.toLowerCase()) {
            case "emojis": return "\u2B50 Emojis";
            case "text": return "Texto";
            case "especial": return "\u2728 Especial";
            default: return category;
        }
    }

    private ItemStack createTagItem(Tag tag, Player player) {
        ItemStack item = new ItemStack(tag.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tag.getDisplayName());

        List<String> lore = new ArrayList<>(tag.getLore());
        lore.add("");
        if (hasTagEquipped(player, tag.getId())) {
            lore.add(ChatColor.GREEN + "\u2713 Equipada");
            lore.add(ChatColor.GRAY + "Click para remover");
        } else {
            lore.add(ChatColor.YELLOW + "\u25B8 Click para equipar");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
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
                int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                String category = playerCategory.getOrDefault(player.getUniqueId(), "emojis");
                openCategoryGUI(player, category, currentPage - 1);
                return;
            }

            if (slot == BACK_BUTTON_SLOT && clicked.getType() == Material.BARRIER) {
                openMainMenu(player);
                return;
            }

            if (slot == NEXT_BUTTON_SLOT && clicked.getType() == Material.ARROW) {
                int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                String category = playerCategory.getOrDefault(player.getUniqueId(), "emojis");
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

                String category = playerCategory.getOrDefault(player.getUniqueId(), "emojis");
                int page = playerPages.getOrDefault(player.getUniqueId(), 0);
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
