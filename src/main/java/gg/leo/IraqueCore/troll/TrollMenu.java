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
                List.of("&7Efeitos visuais e de cliente", "&7que desorientam o jogador.")));
        CATEGORIES.put("movement", new CategoryInfo("&aMovimento", Material.FEATHER,
                List.of("&7Efeitos que alteram o", "&7movimento do jogador.")));
        CATEGORIES.put("inventory", new CategoryInfo("&6Inventario", Material.CHEST,
                List.of("&7Efeitos que mexem com", "&7o inventario do jogador.")));
        CATEGORIES.put("soundchat", new CategoryInfo("&dSom / Chat", Material.JUKEBOX,
                List.of("&7Efeitos de som e", "&7mensagens no chat.")));
        CATEGORIES.put("combatworld", new CategoryInfo("&cCombate / Mundo", Material.DIAMOND_SWORD,
                List.of("&7Efeitos que afetam o", "&7mundo e combate.")));
        CATEGORIES.put("interface", new CategoryInfo("&5Interface", Material.BOOK,
                List.of("&7Efeitos que alteram a", "&7interface do usuario.")));
        CATEGORIES.put("classic", new CategoryInfo("&4Classicos", Material.DIAMOND,
                List.of("&7Efeitos classicos e", "&7divertidos para trolar.")));
        CATEGORIES.put("explosion", new CategoryInfo("&6Explosao", Material.TNT,
                List.of("&7Explosoes, galinhas e", "&7mais caos explosivo.")));
        CATEGORIES.put("beds", new CategoryInfo("&cCamas", Material.RED_BED,
                List.of("&7Troleos relacionados", "&7com camas e dormir.")));
        CATEGORIES.put("chat2", new CategoryInfo("&dChat", Material.PAPER,
                List.of("&7Efeitos que alteram", "&7o chat do jogador.")));
        CATEGORIES.put("random", new CategoryInfo("&aAleatorio", Material.COMMAND_BLOCK,
                List.of("&7Efeitos aleatorios", "&7e variados.")));
        CATEGORIES.put("event", new CategoryInfo("&eEventos", Material.OBSERVER,
                List.of("&7Efeitos baseados em", "&7eventos do jogador.")));
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
                    desc.add("&7Efeitos: &f" + effectCount);
                    desc.add("");
                    desc.add("&e\u25B8 Clique para abrir");
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
                        return List.of("&7Fecha o menu de trolagem",
                                "",
                                "&c\u2716 Clique para fechar");
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
                return ChatColor.DARK_RED + "\u00BB " + ChatColor.RED + "Trolar " + target.getName();
            }
        }.openMenu();
    }

    private void openCategoryMenu(Player player, Player target, String category) {
        List<TrollEffect> effects = manager.getEffectsByCategory(category);
        CategoryInfo info = CATEGORIES.get(category);

        if (effects.isEmpty()) {
            player.sendMessage("§cNenhum efeito encontrado nessa categoria.");
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
                        lore.add("&c\u25CF Em cooldown: &e" + cooldown + "s");
                    } else if (manager.hasActiveEffect(target, effect.getId())) {
                        lore.add("");
                        lore.add("&a\u25CF Ativo agora");
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
                        return List.of("&7Voltar ao menu principal");
                    }
                    @Override
                    public String getDisplayName(Player p) { return "&c\u2190 Voltar"; }
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
                        return List.of("&7Fecha o menu de trolagem");
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
