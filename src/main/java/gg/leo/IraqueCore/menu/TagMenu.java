package gg.leo.IraqueCore.menu;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.tag.Tag;
import gg.leo.IraqueCore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class TagMenu {

    private final IraqueCore plugin;
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, String> playerCategory = new HashMap<>();

    private static final int ITEMS_PER_PAGE = 45;
    private static final int PREV_BUTTON_SLOT = 48;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int NEXT_BUTTON_SLOT = 50;

    public TagMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.DARK_PURPLE + "\u00BB " + ChatColor.LIGHT_PURPLE + "Selecionar Categoria");

        var tagManager = plugin.getTagManager();

        ItemStack emojisItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta emojisMeta = emojisItem.getItemMeta();
        emojisMeta.setDisplayName(ChatColor.YELLOW + "\u2B50 Emojis");
        int emojisCount = tagManager.getTagsByCategory("emojis", player).size();
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
        int textCount = tagManager.getTagsByCategory("text", player).size();
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
        int especialCount = tagManager.getTagsByCategory("especial", player).size();
        especialMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Tags unicas y exclusivas",
                ChatColor.GRAY + "Total: " + ChatColor.WHITE + especialCount + " tags",
                "",
                ChatColor.GREEN + "\u25B8 Click para abrir"
        ));
        especialItem.setItemMeta(especialMeta);
        gui.setItem(15, especialItem);

        String currentTagText = tagManager.getPlayerTagDisplay(player);
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
        var tagManager = plugin.getTagManager();
        List<Tag> categoryTags = tagManager.getTagsByCategory(category, player);

        if (categoryTags.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessageComponent("tag.gui.category-empty"));
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

    private ItemStack createTagItem(Tag tag, Player player) {
        ItemStack item = new ItemStack(tag.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tag.getDisplayName());

        List<String> lore = new ArrayList<>(tag.getLore());
        lore.add("");
        if (plugin.getTagManager().hasTagEquipped(player, tag.getId())) {
            lore.add(ChatColor.GREEN + "\u2713 Equipada");
            lore.add(ChatColor.GRAY + "Click para remover");
        } else {
            lore.add(ChatColor.YELLOW + "\u25B8 Click para equipar");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
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

    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    public String getPlayerCategory(Player player) {
        return playerCategory.getOrDefault(player.getUniqueId(), "emojis");
    }
}
