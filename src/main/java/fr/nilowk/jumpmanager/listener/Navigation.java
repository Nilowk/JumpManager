package fr.nilowk.jumpmanager.listener;

import fr.nilowk.jumpmanager.Main;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Navigation implements Listener {

    private Main instance;
    private String worldId;

    public Navigation(Main instance) {

        this.instance = instance;
        this.worldId = instance.getConfig().getString("game.world");

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        if (!player.getWorld().getName().equalsIgnoreCase(worldId)) return;

        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();

        if (!instance.getInJump().containsKey(player)) {

            player.teleport(instance.getSpawn());

        }

        if (instance.getConfig().contains("players." + player.getUniqueId().toString())) {

            instance.getConfig().set("players." + player.getUniqueId() + ".name", player.getName());

        } else {

            String key = "players." + player.getUniqueId();

            instance.getConfig().set(key + ".name", player.getName());

            instance.getConfig().set(key + ".checkpoint.world", instance.getSpawn().getWorld().getName());
            instance.getConfig().set(key + ".checkpoint.x", instance.getSpawn().getX());
            instance.getConfig().set(key + ".checkpoint.y", instance.getSpawn().getY());
            instance.getConfig().set(key + ".checkpoint.z", instance.getSpawn().getZ());
            instance.getConfig().set(key + ".checkpoint.yaw", instance.getSpawn().getYaw());
            instance.getConfig().set(key + ".checkpoint.pitch", instance.getSpawn().getPitch());

        }

        instance.saveConfig();

        getDefaultItem(player);

    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (!player.getWorld().getName().equalsIgnoreCase(worldId)) return;
        ItemStack it = event.getItem();
        Action action = event.getAction();

        if (it != null && action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {

            String sec = "default-item";
            getAction(it, player, sec);

        }

    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        if (!player.getWorld().getName().equalsIgnoreCase(worldId)) return;
        ItemStack it = event.getCurrentItem();

        if (it != null) {

            String sec = "";

            for (String string: instance.getConfig().getConfigurationSection("menu").getKeys(false)) {

                ConfigurationSection key = instance.getConfig().getConfigurationSection("menu." + string);

                if (key.getString("id").equalsIgnoreCase(event.getView().getTitle())) {

                    sec = "menu." + string + ".item";

                }

            }

            if (event.getInventory().getType() == InventoryType.CHEST) {

                if (!event.getView().getTitle().equalsIgnoreCase(InventoryType.CHEST.getDefaultTitle())) {

                    getAction(it, player, sec);
                    event.setCancelled(true);

                }

            } else {

                if (!player.isOp()) {

                    event.setCancelled(true);

                }

            }

        }

    }

    private void getAction(ItemStack item, Player player, String sec) {

        if (instance.getConfig().getConfigurationSection(sec) != null) {

            for (String string : instance.getConfig().getConfigurationSection(sec).getKeys(false)) {

                ConfigurationSection key = instance.getConfig().getConfigurationSection(sec + "." + string);
                String id = key.getString("id");

                if (id.equalsIgnoreCase(item.getItemMeta().getDisplayName())) {

                    if (key.getString("func").equalsIgnoreCase("open-menu")) {

                        openMenu(player, key);

                    } else if (key.getString("func").equalsIgnoreCase("teleport")) {

                        if (key.getString("teleport-point").equalsIgnoreCase("spawn")) {

                            instance.setCheckPoint(player, instance.getSpawn());

                        }
                        if (instance.getInJump().containsKey(player)) {

                            instance.removeInJump(player);

                        }
                        teleport(player, key);


                    } else if (key.getString("func").equalsIgnoreCase("checkpoint")) {

                        instance.teleportToCheckPoint(player);

                    } else if (key.getString("func").equalsIgnoreCase("stats")) {

                        getStats(player);

                    }

                }

            }

        }

    }

    private void openMenu(Player player, ConfigurationSection key) {

        for (String string : instance.getConfig().getConfigurationSection("menu").getKeys(false)) {

            ConfigurationSection k = instance.getConfig().getConfigurationSection("menu." + string);
            String id = k.getString("id");

            if (id.equalsIgnoreCase(key.getString("menu"))) {

                Inventory inv = Bukkit.createInventory(null, k.getInt("size"), id);

                if (k.getBoolean("glass_pane.activated")) {

                    if (k.getIntegerList("glass_pane.slot") != null) {

                        List<Integer> slots = k.getIntegerList("glass_pane.slot");

                        for (int slot : slots) {

                            ItemStack glass = new ItemStack(Material.getMaterial(k.getString("glass_pane.color") + "_GLASS_PANE"), 1);
                            ItemMeta glassMeta = glass.getItemMeta();

                            glassMeta.setDisplayName(k.getString("glass_pane.name"));
                            if (k.getStringList("glass_pane.description") != null) {

                                glassMeta.setLore(k.getStringList("glass_pane.description"));

                            }

                            glass.setItemMeta(glassMeta);
                            inv.setItem(slot, glass);

                        }

                    }

                }

                if (k.getConfigurationSection("item") != null) {

                    for (String str : k.getConfigurationSection("item").getKeys(false)) {

                        ConfigurationSection it = k.getConfigurationSection("item." + str);

                        ItemStack itemStack = new ItemStack(Material.getMaterial(it.getString("item")), it.getInt("num"));
                        ItemMeta itemMeta = itemStack.getItemMeta();

                        itemMeta.setDisplayName(it.getString("id"));
                        if (it.getStringList("description") != null) {

                            List<String> description = it.getStringList("description");
                            itemMeta.setLore(description);

                        }

                        itemStack.setItemMeta(itemMeta);
                        inv.setItem(it.getInt("slot"), itemStack);

                    }

                }

                player.openInventory(inv);

            }

        }

    }

    private void teleport(Player player, ConfigurationSection key) {

        for (String string : instance.getConfig().getConfigurationSection("teleport-point").getKeys(false)) {

            ConfigurationSection k = instance.getConfig().getConfigurationSection("teleport-point." + string);
            String id = k.getString("id");

            if (id.equalsIgnoreCase(key.getString("teleport-point"))) {

                World world = instance.getServer().getWorld(k.getString("world"));
                double x = k.getDouble("x");
                double y = k.getDouble("y");
                double z = k.getDouble("z");
                float yaw = (float) k.getDouble("yaw");
                float pitch = (float) k.getDouble("pitch");

                Location loc = (new Location(world, x, y, z, yaw, pitch));

                player.teleport(loc);

            }

        }

    }

    private void getStats(Player player) {

        if (instance.getConfig().getConfigurationSection("players." + player.getUniqueId() + ".stats") != null) {

            List<String> stats = new ArrayList<>();
            Inventory inv = Bukkit.createInventory(null, 54, "Stats");
            for (String string : instance.getConfig().getConfigurationSection("players." + player.getUniqueId() + ".stats").getKeys(false)) {

                ConfigurationSection key = instance.getConfig().getConfigurationSection("players." + player.getUniqueId() + ".stats");
                stats.add(key.getString(string));

            }

            for (String id : stats) {

                ItemStack it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
                ItemMeta im = it.getItemMeta();

                im.setDisplayName(id);
                List<String> list = new ArrayList<>();
                list.add("validated");
                im.setLore(list);

                it.setItemMeta(im);
                inv.addItem(it);

            }

            player.openInventory(inv);

        } else {

            player.closeInventory();
            player.sendMessage(instance.getConfig().getString("message.no-stats"));

        }

    }

    private void getDefaultItem(Player player) {

        for (String string : instance.getConfig().getConfigurationSection("default-item").getKeys(false)) {

            ConfigurationSection key = instance.getConfig().getConfigurationSection("default-item." + string);

            Material mat = Material.getMaterial(key.getString("item"));

            ItemStack itemStack = new ItemStack(mat, key.getInt("num"));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(key.getString("id"));

            if (key.getStringList("description") != null) {

                List<String> description = key.getStringList("description");
                itemMeta.setLore(description);

            }

            itemStack.setItemMeta(itemMeta);
            player.getInventory().setItem(key.getInt("slot"), itemStack);

        }

    }

}
