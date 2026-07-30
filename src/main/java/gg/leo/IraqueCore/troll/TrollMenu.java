package gg.leo.IraqueCore.troll;

import gg.leo.IraqueCore.utils.menu.Button;
import gg.leo.IraqueCore.utils.menu.Menu;
import gg.leo.IraqueCore.utils.menu.type.BorderedPaginatedMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrollMenu {

    private static final Map<String, CategoryInfo> CATEGORIES = new HashMap<>();

    static {
        CATEGORIES.put("visual", new CategoryInfo("&bVisuais / Cliente", Material.ENDER_EYE,
                List.of("&7Efectos visuales y de cliente", "&7que desorientan al jugador.")));
        CATEGORIES.put("movement", new CategoryInfo("&aMovimiento", Material.FEATHER,
                List.of("&7Efectos que alteran el", "&7movimiento del jugador.")));
        CATEGORIES.put("inventory", new CategoryInfo("&6Inventario", Material.CHEST,
                List.of("&7Efectos que juegan con", "&7el inventario del jugador.")));
        CATEGORIES.put("soundchat", new CategoryInfo("&dSonido / Chat", Material.JUKEBOX,
                List.of("&7Efectos de sonido y", "&7mensajes en el chat.")));
        CATEGORIES.put("combatworld", new CategoryInfo("&cCombate / Mundo", Material.DIAMOND_SWORD,
                List.of("&7Efectos que afectan el", "&7mundo y combate.")));
        CATEGORIES.put("interface", new CategoryInfo("&5Interfaz", Material.BOOK,
                List.of("&7Efectos que alteran la", "&7interfaz de usuario.")));
        CATEGORIES.put("classic", new CategoryInfo("&4Clasicos", Material.DIAMOND,
                List.of("&7Efectos clasicos y", "&7divertidos para trollear.")));
        CATEGORIES.put("explosion", new CategoryInfo("&6Explosion", Material.TNT,
                List.of("&7Explosiones, pollos y", "&7mas caos explosivo.")));
        CATEGORIES.put("beds", new CategoryInfo("&cCamas", Material.RED_BED,
                List.of("&7Trolleos relacionados", "&7con camas y dormir.")));
        CATEGORIES.put("chat2", new CategoryInfo("&dChat", Material.PAPER,
                List.of("&7Efectos que alteran", "&7el chat del jugador.")));
        CATEGORIES.put("random", new CategoryInfo("&aRandom", Material.COMMAND_BLOCK,
                List.of("&7Efectos aleatorios", "&7y variados.")));
    }

    private final TrollManager manager;

    public TrollMenu(TrollManager manager) {
        this.manager = manager;
    }

    public void openMainMenu(Player player, Player target) {
        new Menu(player) {
            {
                staticSize = 54;
                placeholder = true;
            }
            @Override
            public Map<Integer, Button> getButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();

                int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};
                int i = 0;
                for (Map.Entry<String, CategoryInfo> entry : CATEGORIES.entrySet()) {
                    String catId = entry.getKey();
                    CategoryInfo info = entry.getValue();
                    List<TrollEffect> effects = manager.getEffectsByCategory(catId);
                    int effectCount = effects.size();

                    buttons.put(slots[i], new Button() {
                        @Override
                        public Material getMaterial(Player p) { return info.icon; }
                        @Override
                        public List<String> getDescription(Player p) {
                            List<String> desc = new ArrayList<>(info.description);
                            desc.add("");
                            desc.add("&7Efectos: &f" + effectCount);
                            desc.add("");
                            desc.add("&e\u25B8 Click para abrir");
                            return desc;
                        }
                        @Override
                        public String getDisplayName(Player p) { return info.displayName; }
                        @Override
                        public int getData(Player p) { return 0; }
                        @Override
                        public void onClick(Player p, int slot, ClickType type) {
                            openCategoryMenu(p, target, catId);
                        }
                    });
                    i++;
                }

                buttons.put(49, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override
                    public List<String> getDescription(Player p) {
                        return List.of("&7Cierra el menu de trolleo",
                                "",
                                "&c\u2716 Click para cerrar");
                    }
                    @Override
                    public String getDisplayName(Player p) { return "&cCerrar"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {
                        p.closeInventory();
                    }
                });

                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return ChatColor.DARK_RED + "\u00BB " + ChatColor.RED + "Trollear a " + target.getName();
            }
        }.openMenu();
    }

    private void openCategoryMenu(Player player, Player target, String category) {
        List<TrollEffect> effects = manager.getEffectsByCategory(category);
        CategoryInfo info = CATEGORIES.get(category);

        if (effects.isEmpty()) {
            player.sendMessage("§cNo effects found in this category.");
            return;
        }

        new BorderedPaginatedMenu(player) {
            @Override
            public Map<Integer, Button> getPagesButtons(Player p) {
                Map<Integer, Button> buttons = new HashMap<>();
                int index = 0;

                for (TrollEffect effect : effects) {
                    List<String> lore = new ArrayList<>(effect.getDescription());

                    if (manager.isOnCooldown(target.getUniqueId(), effect.getId())) {
                        long cooldown = manager.getCooldownRemaining(target.getUniqueId(), effect.getId());
                        lore.add("");
                        lore.add("&c\u25CF En cooldown: &e" + cooldown + "s");
                    } else if (manager.hasActiveEffect(target, effect.getId())) {
                        lore.add("");
                        lore.add("&a\u25CF Activo ahora");
                    }

                    String effectId = effect.getId();
                    buttons.put(index, new Button() {
                        @Override
                        public Material getMaterial(Player p) {
                            if (manager.hasActiveEffect(target, effectId)) {
                                return Material.LIME_DYE;
                            }
                            if (manager.isOnCooldown(target.getUniqueId(), effectId)) {
                                return Material.GRAY_DYE;
                            }
                            return effect.getIcon();
                        }
                        @Override
                        public List<String> getDescription(Player p) { return lore; }
                        @Override
                        public String getDisplayName(Player p) {
                            String prefix = "";
                            if (manager.hasActiveEffect(target, effectId)) prefix = "&a\u2713 ";
                            if (manager.isOnCooldown(target.getUniqueId(), effectId)) prefix = "&c\u29B8 ";
                            return prefix + effect.getName();
                        }
                        @Override
                        public int getData(Player p) { return 0; }
                        @Override
                        public void onClick(Player p, int slot, ClickType type) {
                            if (p != player) return;
                            manager.applyEffect(target, effectId, player);
                            openCategoryMenu(player, target, category);
                        }
                    });
                    index++;
                }

                return buttons;
            }

            @Override
            public String getTitle(Player p) {
                return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                        info.displayName)) + " - " + target.getName();
            }

            @Override
            public Map<Integer, Button> getHeaderItems(Player p) {
                Map<Integer, Button> headers = super.getHeaderItems(p);
                headers.put(40, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override
                    public List<String> getDescription(Player p) {
                        return List.of("&7Volver al menu principal");
                    }
                    @Override
                    public String getDisplayName(Player p) { return "&c\u2190 Volver"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {
                        openMainMenu(p, target);
                    }
                });
                headers.put(36, new Button() {
                    @Override
                    public Material getMaterial(Player p) { return Material.BARRIER; }
                    @Override
                    public List<String> getDescription(Player p) {
                        return List.of("&7Cierra el menu de trolleo");
                    }
                    @Override
                    public String getDisplayName(Player p) { return "&cCerrar"; }
                    @Override
                    public int getData(Player p) { return 0; }
                    @Override
                    public void onClick(Player p, int slot, ClickType type) {
                        p.closeInventory();
                    }
                });
                return headers;
            }
        }.updateMenu();
    }

    private record CategoryInfo(String displayName, Material icon, List<String> description) {}
}
