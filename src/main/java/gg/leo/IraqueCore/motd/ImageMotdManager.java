package gg.leo.IraqueCore.motd;

import gg.leo.IraqueCore.IraqueCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.util.CachedServerIcon;

public class ImageMotdManager implements Listener {

    private static final Map<String, int[]> CHAT_COLORS = new LinkedHashMap<>();

    static {
        CHAT_COLORS.put("§0", new int[]{0, 0, 0});
        CHAT_COLORS.put("§1", new int[]{0, 0, 170});
        CHAT_COLORS.put("§2", new int[]{0, 170, 0});
        CHAT_COLORS.put("§3", new int[]{0, 170, 170});
        CHAT_COLORS.put("§4", new int[]{170, 0, 0});
        CHAT_COLORS.put("§5", new int[]{170, 0, 170});
        CHAT_COLORS.put("§6", new int[]{255, 170, 0});
        CHAT_COLORS.put("§7", new int[]{170, 170, 170});
        CHAT_COLORS.put("§8", new int[]{85, 85, 85});
        CHAT_COLORS.put("§9", new int[]{85, 85, 255});
        CHAT_COLORS.put("§a", new int[]{85, 255, 85});
        CHAT_COLORS.put("§b", new int[]{85, 255, 255});
        CHAT_COLORS.put("§c", new int[]{255, 85, 85});
        CHAT_COLORS.put("§d", new int[]{255, 85, 255});
        CHAT_COLORS.put("§e", new int[]{255, 255, 85});
        CHAT_COLORS.put("§f", new int[]{255, 255, 255});
    }

    private static final String FULL_BLOCK = "\u2588";

    private final IraqueCore plugin;
    private boolean enabled;
    private String imagePath;
    private int charWidth;
    private int charHeight;
    private boolean setFavicon;
    private CachedServerIcon cachedIcon;
    private List<String> cachedArt;
    private Component cachedMotd;

    public ImageMotdManager(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("motd-image.enabled", false);
        imagePath = config.getString("motd-image.file", "motd-image.png");
        charWidth = config.getInt("motd-image.char-width", 40);
        charHeight = config.getInt("motd-image.char-height", 10);
        setFavicon = config.getBoolean("motd-image.set-favicon", true);

        if (!enabled) return;

        File imagesDir = new File(plugin.getDataFolder(), "images");
        if (!imagesDir.exists()) imagesDir.mkdirs();
        File imageFile = new File(imagesDir, imagePath);
        if (!imageFile.exists()) {
            plugin.getPluginLogger().warn("MOTD image not found: {}", imageFile.getAbsolutePath());
            return;
        }

        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                plugin.getPluginLogger().error("Unsupported image format: {}", imageFile.getName());
                return;
            }
            cachedArt = imageToTextArt(image, charWidth, charHeight);
            cachedMotd = buildMotd(cachedArt);
            plugin.getPluginLogger().info("Loaded MOTD image ({}x{} chars)", charWidth, charHeight);

            if (setFavicon) {
                cachedIcon = loadFavicon(image);
            }
        } catch (IOException e) {
            plugin.getPluginLogger().error("Failed to load MOTD image", e);
        }
    }

    private List<String> imageToTextArt(BufferedImage image, int targetWidth, int targetHeight) {
        int imgW = image.getWidth();
        int imgH = image.getHeight();

        double scale = Math.min((double) targetWidth / imgW, (double) targetHeight * 2 / imgH);
        int scaledW = Math.max(1, (int) (imgW * scale));
        int scaledH = Math.max(1, (int) (imgH * scale));

        BufferedImage scaled = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, scaledW, scaledH, null);
        g.dispose();

        List<String> lines = new ArrayList<>();
        for (int y = 0; y < scaledH; y += 2) {
            StringBuilder line = new StringBuilder();
            String lastColor = "";
            for (int x = 0; x < scaledW; x++) {
                int rgb = scaled.getRGB(x, y);
                Color c = new Color(rgb);
                String nearest = nearestChatColor(c);
                if (!nearest.equals(lastColor)) {
                    line.append(nearest);
                    lastColor = nearest;
                }
                line.append(FULL_BLOCK);
            }
            lines.add(line.toString());
        }

        return lines;
    }

    private String nearestChatColor(Color color) {
        String best = "§f";
        double bestDist = Double.MAX_VALUE;

        for (Map.Entry<String, int[]> entry : CHAT_COLORS.entrySet()) {
            int[] rgb = entry.getValue();
            double dr = color.getRed() - rgb[0];
            double dg = color.getGreen() - rgb[1];
            double db = color.getBlue() - rgb[2];
            double dist = dr * dr + dg * dg + db * db;
            if (dist < bestDist) {
                bestDist = dist;
                best = entry.getKey();
            }
        }
        return best;
    }

    private Component buildMotd(List<String> art) {
        List<Component> lines = art.stream()
                .map(line -> plugin.getConfigManager().deserialize(
                        plugin.getConfigManager().translate(line)))
                .collect(Collectors.toList());

        return Component.join(JoinConfiguration.newlines(), lines);
    }

    private CachedServerIcon loadFavicon(BufferedImage image) {
        try {
            BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = icon.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, 64, 64, null);
            g.dispose();
            return Bukkit.loadServerIcon(icon);
        } catch (Exception e) {
            plugin.getPluginLogger().error("Failed to load server favicon", e);
            return null;
        }
    }

    public void reload() {
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Component getCachedMotd() {
        return cachedMotd;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (!enabled) return;
        if (cachedMotd != null) event.motd(cachedMotd);
        if (setFavicon && cachedIcon != null) event.setServerIcon(cachedIcon);
    }
}
