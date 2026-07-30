package gg.leo.IraqueCore.troll.effects;

import gg.leo.IraqueCore.troll.TrollEffect;
import gg.leo.IraqueCore.troll.TrollManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class InventoryEffects {

    private static final Random RANDOM = new Random();

    private static final List<String> FUNNY_NAMES = List.of(
        "&aItem Magico", "&eRama Seca", "&4Basura Espacial", "&dPolvo Magico",
        "&cItem de Admin", "&7Piedra Normal", "&6Item Legendario", "&bAgua Bendita",
        "&5Item Misterioso", "&2Hoja de Arbol", "&aTrol", "&cNada Importante",
        "&eItem #" + RANDOM.nextInt(9999), "&fItem Generico"
    );

    private InventoryEffects() {}

    public static void register(TrollManager manager) {
        manager.registerEffect(new ShuffleInventoryEffect());
        manager.registerEffect(new RenameItemsEffect());
        manager.registerEffect(new FakeInventoryFullEffect());
        manager.registerEffect(new VanishingItemEffect());
    }

    private static class ShuffleInventoryEffect extends TrollEffect {
        ShuffleInventoryEffect() {
            super("shuffle-inv", "&eReorganizar Inventario", Material.HOPPER,
                    List.of("&7Reorganiza el inventario", "&7aleatoriamente cada 5s.", "", "&e\u25B8 Click para aplicar"),
                    "inventory", "troll.effect.shuffle-inv", 18, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            doShuffle(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {}

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 100; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            doShuffle(target);
        }

        private void doShuffle(Player target) {
            ItemStack[] contents = target.getInventory().getContents();
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack item : contents) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item);
                }
            }
            for (int i = items.size() - 1; i > 0; i--) {
                int j = RANDOM.nextInt(i + 1);
                ItemStack temp = items.get(i);
                items.set(i, items.get(j));
                items.set(j, temp);
            }
            int index = 0;
            ItemStack[] newContents = new ItemStack[contents.length];
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR && index < items.size()) {
                    newContents[i] = items.get(index++);
                } else {
                    newContents[i] = null;
                }
            }
            target.getInventory().setContents(newContents);
            target.updateInventory();
        }
    }

    private static class RenameItemsEffect extends TrollEffect {
        private final Map<String, Map<Integer, String>> originalNames = new HashMap<>();

        RenameItemsEffect() {
            super("rename-items", "&bRenombrar Items", Material.NAME_TAG,
                    List.of("&7Cambia temporalmente los nombres", "&7de los items a cosas graciosas.", "", "&e\u25B8 Click para aplicar"),
                    "inventory", "troll.effect.rename-items", 25, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            String key = target.getUniqueId().toString();
            Map<Integer, String> stored = new HashMap<>();
            ItemStack[] contents = target.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    ItemMeta meta = contents[i].getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        stored.put(i, meta.getDisplayName());
                    } else {
                        stored.put(i, null);
                    }
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                            FUNNY_NAMES.get(RANDOM.nextInt(FUNNY_NAMES.size()))));
                    contents[i].setItemMeta(meta);
                }
            }
            originalNames.put(key, stored);
            target.getInventory().setContents(contents);
            target.updateInventory();
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            String key = target.getUniqueId().toString();
            Map<Integer, String> stored = originalNames.remove(key);
            if (stored == null) return;
            ItemStack[] contents = target.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR && stored.containsKey(i)) {
                    ItemMeta meta = contents[i].getItemMeta();
                    String originalName = stored.get(i);
                    if (originalName != null) {
                        meta.setDisplayName(originalName);
                    } else {
                        meta.setDisplayName(null);
                    }
                    contents[i].setItemMeta(meta);
                }
            }
            target.getInventory().setContents(contents);
            target.updateInventory();
        }
    }

    private static class FakeInventoryFullEffect extends TrollEffect {
        FakeInventoryFullEffect() {
            super("fake-inv-full", "&6Inventario Lleno Falso", Material.CHEST,
                    List.of("&7Muestra un mensaje falso", "&7de inventario lleno.", "", "&e\u25B8 Click para aplicar"),
                    "inventory", "troll.effect.fake-inv-full", 0, 10);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            target.sendMessage(ChatColor.RED + "Your inventory is full!");
            target.sendMessage(ChatColor.RED + "Tu inventario esta lleno!");
        }

        @Override
        public void revert(Player target, TrollManager manager) {}
    }

    private static class VanishingItemEffect extends TrollEffect {
        private final Map<String, Map<Integer, ItemStack>> hiddenItems = new HashMap<>();

        VanishingItemEffect() {
            super("vanishing-item", "&7Item Desaparece", Material.ITEM_FRAME,
                    List.of("&7Un item del inventario", "&7desaparece y reaparece.", "", "&e\u25B8 Click para aplicar"),
                    "inventory", "troll.effect.vanishing-item", 18, 60);
        }

        @Override
        public void apply(Player target, TrollManager manager) {
            hideRandomItem(target);
        }

        @Override
        public void revert(Player target, TrollManager manager) {
            String key = target.getUniqueId().toString();
            Map<Integer, ItemStack> restored = hiddenItems.remove(key);
            if (restored != null) {
                restoreItems(target, restored);
            }
        }

        @Override
        public boolean requiresTask() { return true; }

        @Override
        public int getInterval() { return 80; }

        @Override
        public void onTick(Player target, TrollManager manager) {
            if (!manager.hasActiveEffect(target, id)) return;
            String key = target.getUniqueId().toString();
            Map<Integer, ItemStack> hidden = hiddenItems.get(key);
            if (hidden != null && !hidden.isEmpty()) {
                restoreItems(target, hidden);
                hiddenItems.remove(key);
            } else {
                hideRandomItem(target);
            }
        }

        private void restoreItems(Player target, Map<Integer, ItemStack> items) {
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                ItemStack current = target.getInventory().getItem(slot);
                if (current == null || current.getType() == Material.AIR) {
                    target.getInventory().setItem(slot, item);
                } else {
                    target.getInventory().addItem(item).values().forEach(dropped ->
                            target.getWorld().dropItemNaturally(target.getLocation(), dropped));
                }
            }
            target.updateInventory();
        }

        private void hideRandomItem(Player target) {
            ItemStack[] contents = target.getInventory().getContents();
            List<Integer> nonEmpty = new ArrayList<>();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    nonEmpty.add(i);
                }
            }
            if (nonEmpty.isEmpty()) return;
            int slot = nonEmpty.get(RANDOM.nextInt(nonEmpty.size()));
            String key = target.getUniqueId().toString();
            Map<Integer, ItemStack> hidden = new HashMap<>();
            hidden.put(slot, contents[slot].clone());
            hiddenItems.put(key, hidden);
            target.getInventory().setItem(slot, null);
            target.updateInventory();
        }
    }
}
