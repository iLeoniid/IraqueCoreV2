package gg.leo.IraqueCore.chatcolor;

import gg.leo.IraqueCore.IraqueCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChatColorManager {

    private final IraqueCore plugin;
    private final Map<String, ChatColor> colors = new LinkedHashMap<>();
    private final Map<UUID, String> playerColors = new HashMap<>();
    private YamlConfiguration colorConfig;
    private File colorFile;

    public ChatColorManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        colorFile = new File(plugin.getDataFolder(), "chatcolors.yml");
        if (!colorFile.exists()) {
            try {
                colorFile.createNewFile();
            } catch (IOException e) {
                plugin.getPluginLogger().error("Failed to create chatcolors.yml", e);
            }
        }
        colorConfig = YamlConfiguration.loadConfiguration(colorFile);
        loadColors();
        loadPlayerColors();
    }

    private void loadColors() {
        colors.clear();

        for (org.bukkit.ChatColor bukkitColor : org.bukkit.ChatColor.values()) {
            if (!bukkitColor.isColor()) continue;

            String id = bukkitColor.name().toLowerCase();
            String display = proper(bukkitColor.name());
            String colorStr = bukkitColor.toString().replace("§", "&");
            String permission = "iraquecore.chatcolor." + id;

            colors.put(id, new ChatColor(id, display, colorStr, permission));
        }

        plugin.getPluginLogger().info("Loaded {} chat colors", colors.size());
    }

    private void loadPlayerColors() {
        playerColors.clear();
        if (!colorConfig.contains("players")) return;

        for (String uuidStr : colorConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String colorId = colorConfig.getString("players." + uuidStr + ".color");
                if (colorId != null && !colorId.equals("null") && colors.containsKey(colorId)) {
                    playerColors.put(uuid, colorId);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAll() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(colorFile);
        config.set("players", null);
        for (Map.Entry<UUID, String> entry : playerColors.entrySet()) {
            config.set("players." + entry.getKey().toString() + ".color", entry.getValue());
        }
        try {
            config.save(colorFile);
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to save chatcolors.yml", e);
        }
    }

    public void setActiveColor(UUID playerId, String colorId) {
        if (colorId == null) {
            playerColors.remove(playerId);
        } else if (colors.containsKey(colorId)) {
            playerColors.put(playerId, colorId);
        }
        saveAll();
    }

    public String getActiveColorCode(UUID playerId) {
        String colorId = playerColors.get(playerId);
        if (colorId != null) {
            ChatColor color = colors.get(colorId);
            if (color != null) return color.getChatColor();
        }
        return null;
    }

    public ChatColor getActiveColor(UUID playerId) {
        String colorId = playerColors.get(playerId);
        if (colorId != null) {
            return colors.get(colorId);
        }
        return null;
    }

    public ChatColor getColor(String id) {
        return colors.get(id);
    }

    public Map<String, ChatColor> getColors() {
        return Collections.unmodifiableMap(colors);
    }

    public static String proper(String name) {
        if (name.contains("_")) {
            String[] split = name.split("_");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                String part = split[i].toLowerCase();
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1));
                if (i < split.length - 1) sb.append(" ");
            }
            return sb.toString();
        }
        String lower = name.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
