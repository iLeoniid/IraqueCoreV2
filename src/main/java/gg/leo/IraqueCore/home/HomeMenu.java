package gg.leo.IraqueCore.home;

import gg.leo.IraqueCore.IraqueCore;
import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeMenu {

    private final IraqueCore plugin;
    private final HomeManager homeManager;

    public HomeMenu(IraqueCore plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    public void open(Player player) {
        boolean hasHome = homeManager.hasHome(player);
        Location home = homeManager.getHome(player);

        String world = home != null ? home.getWorld().getName() : "";
        int x = home != null ? home.getBlockX() : 0;
        int y = home != null ? home.getBlockY() : 0;
        int z = home != null ? home.getBlockZ() : 0;

        String fWorld = world;
        int fX = x;
        int fY = y;
        int fZ = z;
        Location fHome = home;
        boolean fHasHome = hasHome;

        new Menu(player) {
            {
                staticSize = 27;
                placeholder = true;
            }

            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();

                if (fHome != null) {
                    buttons.put(4, new Button() {
                        @Override
                        public Material getMaterial(Player p) { return Material.RED_BED; }
                        @Override
                        public List<String> getDescription(Player p) {
                            return List.of(
                                    "&7Mundo: &f" + fWorld,
                                    "&7X: &f" + fX,
                                    "&7Y: &f" + fY,
                                    "&7Z: &f" + fZ,
                                    "",
                                    legacy("home.menu-teleport")
                            );
                        }
                        @Override
                        public String getDisplayName(Player p) { return "&aMinha Home"; }
                        @Override
                        public int getData(Player p) { return 0; }
                        @Override
                        public void onClick(Player p, int slot, ClickType type) { teleport(p, fHome); }
                    });

                    buttons.put(11, new Button() {
                        @Override
                        public Material getMaterial(Player p) { return Material.ENDER_PEARL; }
                        @Override
                        public List<String> getDescription(Player p) { return List.of(legacy("home.menu-teleport")); }
                        @Override
                        public String getDisplayName(Player p) { return "&bTeleportar"; }
                        @Override
                        public int getData(Player p) { return 0; }
                        @Override
                        public void onClick(Player p, int slot, ClickType type) { teleport(p, fHome); }
                    });

                    buttons.put(15, new Button() {
                        @Override
                        public Material getMaterial(Player p) { return Material.BARRIER; }
                        @Override
                        public List<String> getDescription(Player p) { return List.of(legacy("home.menu-delete")); }
                        @Override
                        public String getDisplayName(Player p) { return "&cDeletar Home"; }
                        @Override
                        public int getData(Player p) { return 0; }
                        @Override
                        public void onClick(Player p, int slot, ClickType type) {
                            homeManager.deleteHome(p);
                            p.sendMessage(plugin.getConfigManager().getMessageComponent("home.deleted"));
                            open(p);
                        }
                    });
                } else {
                    String info = legacy(fHasHome ? "home.invalid" : "home.not-set");
                    buttons.put(4, new Button() {
                        @Override
                        public Material getMaterial(Player p) { return Material.RED_BED; }
                        @Override
                        public List<String> getDescription(Player p) {
                            return List.of(info, "", legacy("home.menu-set"));
                        }
                        @Override
                        public String getDisplayName(Player p) { return "&cSem Home definida"; }
                        @Override
                        public int getData(Player p) { return 0; }
                        @Override
                        public void onClick(Player p, int slot, ClickType type) { setHome(p); }
                    });

                    buttons.put(13, new Button() {
                        @Override
                        public Material getMaterial(Player p) { return Material.EMERALD; }
                        @Override
                        public List<String> getDescription(Player p) { return List.of(legacy("home.menu-set")); }
                        @Override
                        public String getDisplayName(Player p) { return "&aDefinir Home"; }
                        @Override
                        public int getData(Player p) { return 0; }
                        @Override
                        public void onClick(Player p, int slot, ClickType type) { setHome(p); }
                    });
                }

                buttons.put(22, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.IRON_DOOR; }
                    @Override
                    public List<String> getDescription(Player p) { return List.of(legacy("home.menu-close")); }
                    @Override
                    public String getDisplayName(Player p) { return "&cFechar"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) { p.closeInventory(); }
                });

                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return plugin.getConfigManager().getMessage("home.menu-title", "&8\u00BB &6Minhas Homes");
            }
        }.openMenu();
    }

    private void teleport(Player p, Location loc) {
        p.closeInventory();
        p.teleport(loc);
        p.sendMessage(plugin.getConfigManager().getMessageComponent("home.teleport"));
    }

    private void setHome(Player p) {
        homeManager.setHome(p);
        p.sendMessage(plugin.getConfigManager().getMessageComponent("home.set"));
        open(p);
    }

    private String legacy(String path) {
        return plugin.getConfigManager().toLegacyMessage(path);
    }
}
