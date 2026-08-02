package gg.leo.IraqueCore.profile;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.menu.StatsMenu;
import gg.leo.IraqueCore.playtime.PlaytimeManager;
import gg.leo.IraqueCore.rank.Rank;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ProfileMenu {

    private static final String CROWN_TEXTURE =
            "http://textures.minecraft.net/texture/45587da7fe7336e8ab9f791ea5e2cfc8a827ca959567eb9d53a647babf948d5";
    private static final String CALENDAR_TEXTURE =
            "http://textures.minecraft.net/texture/d4b11b1d2fdd7dd8b89f6c9f732d5b7aa456a49bd156687bc8fe892b6dbfb20a";
    private static final String EMERALD_SKULL_TEXTURE =
            "http://textures.minecraft.net/texture/da5d33ac3d4062e9c7b4a4337cbea3fa50d543ae2e1cd81b6d07718aad709571";

    private final IraqueCore plugin;

    public ProfileMenu(IraqueCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, OfflinePlayer target) {
        var cm = plugin.getConfigManager();
        var rankManager = plugin.getRankManager();
        var tagManager = plugin.getTagManager();

        String name = target.getName();
        if (name == null) return;

        Optional<Rank> rankOpt = rankManager.getPlayerRank(target.getUniqueId());
        Rank rank = rankOpt.orElseGet(rankManager::getDefaultRank);

        String rankPrefix = rank != null ? rank.prefix() : "";
        String rankColor = rank != null ? rank.color() : "&7";
        String rankName = rank != null ? rank.name() : cm.getDefaultRankName();
        String rankDisplay = rankPrefix + rankName;

        String tagId = tagManager.getPlayerTagId(target.getUniqueId());
        String tagText = tagManager.getPlayerTagDisplay(target.getUniqueId());

        String tagDisplay;
        if (tagId != null) {
            var tag = tagManager.getTag(tagId);
            if (tag != null) {
                tagDisplay = tagText.isEmpty() ? tag.getDisplayName() : tagText + " &7(&f" + tag.getDisplayName() + "&7)";
            } else {
                tagDisplay = tagText;
            }
        } else {
            tagDisplay = cm.getMessage("profile.tag-none", "&7Nenhuma tag equipada");
        }

        boolean online = target.isOnline();
        String playtime = PlaytimeManager.formatTime(plugin.getPlaytimeManager().getPlaytime(target.getUniqueId()));

        long firstJoin = plugin.getPlaytimeManager().getFirstJoin(target.getUniqueId());
        if (firstJoin <= 0) firstJoin = target.getFirstPlayed();
        String joinDate = cm.getMessage("profile.date-unknown", "&7N/A");
        if (firstJoin > 0) {
            joinDate = new SimpleDateFormat(cm.getDateFormat()).format(new Date(firstJoin));
        }

        String status = online
                ? cm.getMessage("profile.status-online", "&aOnline")
                : cm.getMessage("profile.status-offline", "&cOffline");

        String achievement = pickAchievement();

        String display = rankPrefix + (tagText.isEmpty() ? "" : tagText + " ")
                + rankColor + name;

        String fRankDisplay = rankDisplay;
        String fTagDisplay = tagDisplay;
        String fPlaytime = playtime;
        String fJoinDate = joinDate;
        String fStatus = status;
        String fAchievement = achievement;
        String fDisplay = display;
        boolean fOnline = online;
        UUID targetId = target.getUniqueId();
        String fName = name;

        new Menu(viewer) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();

                buttons.put(4, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.PLAYER_HEAD; }
                    @Override
                    public List<String> getDescription(Player p) {
                        return List.of(
                                legacy("profile.rank", "{rank}", fRankDisplay),
                                legacy("profile.status", "{status}", fStatus),
                                "",
                                "&e\u25B8 Clique para ver stats"
                        );
                    }
                    @Override
                    public String getDisplayName(Player p) { return "&6" + fName; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {
                        new StatsMenu(plugin).open(p, targetId, fName);
                    }
                    @Override
                    public ItemStack getButtonItem(Player p) {
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) head.getItemMeta();
                        meta.setDisplayName(cm.toLegacyString("&6" + fName));
                        meta.setOwningPlayer(Bukkit.getOfflinePlayer(targetId));
                        meta.setLore(getDescription(p).stream().map(cm::toLegacyString).toList());
                        head.setItemMeta(meta);
                        return head;
                    }
                });

                buttons.put(10, customHeadButton(CROWN_TEXTURE,
                        "&6&lRango",
                        List.of(legacy("profile.rank", "{rank}", fRankDisplay))));

                buttons.put(11, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.NAME_TAG; }
                    @Override
                    public List<String> getDescription(Player p) { return List.of(legacy("profile.tag", "{tag}", fTagDisplay)); }
                    @Override
                    public String getDisplayName(Player p) { return "&b&lTag"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {}
                });

                buttons.put(12, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.CLOCK; }
                    @Override
                    public List<String> getDescription(Player p) { return List.of(legacy("profile.playtime", "{playtime}", fPlaytime)); }
                    @Override
                    public String getDisplayName(Player p) { return "&e&lTempo de jogo"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {}
                });

                buttons.put(14, customHeadButton(CALENDAR_TEXTURE,
                        "&a&lEntrou em",
                        List.of(legacy("profile.first-join", "{date}", fJoinDate))));

                buttons.put(15, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.SKELETON_SKULL; }
                    @Override
                    public List<String> getDescription(Player p) { return List.of(legacy("profile.status", "{status}", fStatus)); }
                    @Override
                    public String getDisplayName(Player p) { return fOnline ? "&a&lStatus" : "&c&lStatus"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {}
                    @Override
                    public ItemStack getButtonItem(Player p) {
                        if (!fOnline) return null;
                        return customHead(EMERALD_SKULL_TEXTURE, "&a&lStatus",
                                List.of(legacy("profile.status", "{status}", fStatus)));
                    }
                });

                buttons.put(16, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.NETHER_STAR; }
                    @Override
                    public List<String> getDescription(Player p) { return List.of(legacy("profile.achievement", "{achievement}", fAchievement)); }
                    @Override
                    public String getDisplayName(Player p) { return "&d&lLogro destacado"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {}
                });

                buttons.put(22, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override
                    public List<String> getDescription(Player p) { return List.of("&7Clique para fechar"); }
                    @Override
                    public String getDisplayName(Player p) { return "&c&lFechar"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) { p.closeInventory(); }
                });

                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return plugin.getConfigManager().toLegacyMessage("profile.title", "{display}", fDisplay);
            }
        }.openMenu();
    }

    private Button customHeadButton(String textureUrl, String name, List<String> lore) {
        return new Button() {
            @Override
            public Material getMaterial(Player p) { return Material.PLAYER_HEAD; }
            @Override
            public List<String> getDescription(Player p) { return lore; }
            @Override
            public String getDisplayName(Player p) { return name; }
            @Override
            public int getData(Player p) { return 0; }
            @Override
            public void onClick(Player p, int slot, ClickType type) {}
            @Override
            public ItemStack getButtonItem(Player p) { return customHead(textureUrl, name, lore); }
        };
    }

    private ItemStack customHead(String textureUrl, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(textureUrl));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (MalformedURLException ignored) {}
        meta.setDisplayName(plugin.getConfigManager().toLegacyString(name));
        if (lore != null) {
            meta.setLore(lore.stream().map(plugin.getConfigManager()::toLegacyString).toList());
        }
        head.setItemMeta(meta);
        return head;
    }

    private String legacy(String path, String placeholder, String value) {
        return plugin.getConfigManager().toLegacyMessage(path, placeholder, value);
    }

    private String pickAchievement() {
        List<String> list = plugin.getConfigManager().getMessageList("profile.achievements");
        if (list.isEmpty()) {
            return plugin.getConfigManager().getMessage("profile.achievement-fallback", "&7Conseguiu o logro 'Online'");
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
