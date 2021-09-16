package fr.nilowk.jumpmanager.listener;

import fr.nilowk.jumpmanager.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class GameManager implements Listener {

    private Main instance;
    private String worldId;

    public GameManager(Main instance) {

        this.instance = instance;
        this.worldId = instance.getConfig().getString("game.world");

    }

    @EventHandler
    public void onTouchPlate(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (!player.getWorld().getName().equalsIgnoreCase(worldId)) return;
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        if (action == Action.PHYSICAL && block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {

            if (isStart(block.getLocation())) {

                instance.setCheckPoint(player, player.getLocation());

                if (instance.getInJump().containsKey(player)) {

                    if (instance.getInJump().get(player).equalsIgnoreCase(getJumpId(block.getLocation()))) {

                        return;

                    } else {

                        instance.removeInJump(player);

                    }

                }
                instance.addInJump(player, getJumpId(block.getLocation()));
                String name = instance.getInJump().get(player);
                player.sendMessage(instance.getConfig().getString("message.start") + name);

            } else {

                instance.setCheckPoint(player, player.getLocation());
                player.sendMessage(instance.getConfig().getString("message.checkpoint"));

            }

        } else if (action == Action.PHYSICAL && block.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {

            if (instance.getInJump().containsKey(player)) {

                player.teleport(instance.getSpawn());
                player.sendMessage(instance.getConfig().getString("message.finish") + instance.getInJump().get(player));

                instance.addInJump(player, instance.getInJump().get(player));
                instance.setCheckPoint(player, instance.getSpawn());
                instance.removeInJump(player);

            }

        }

    }

    private String getJumpId(Location loc) {

        for (String string : instance.getConfig().getConfigurationSection("teleport-point").getKeys(false)) {

            ConfigurationSection key = instance.getConfig().getConfigurationSection("teleport-point." + string);
            World world = instance.getServer().getWorld(key.getString("world"));
            double x = key.getDouble("x");
            double y = key.getDouble("y");
            double z = key.getDouble("z");

            Location loc2 = instance.getServer().getWorld(worldId).getBlockAt(new Location(world, x, y, z)).getLocation();

            if (loc.distance(loc2) < 1) {

                return key.getString("id");

            }

        }
        return "";

    }
    private boolean isStart(Location loc) {

        for (String string : instance.getConfig().getConfigurationSection("teleport-point").getKeys(false)) {

            ConfigurationSection key = instance.getConfig().getConfigurationSection("teleport-point." + string);
            World world = instance.getServer().getWorld(key.getString("world"));
            double x = key.getDouble("x");
            double y = key.getDouble("y");
            double z = key.getDouble("z");

            Location loc2 = instance.getServer().getWorld(worldId).getBlockAt(new Location(world, x, y, z)).getLocation();

            if (loc.distance(loc2) < 1) {

                return true;

            }

        }

        return false;

    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent event) {

        if (event.getEntityType() == EntityType.PLAYER) {

            Player player = (Player) event.getEntity();
            if (!player.getWorld().getName().equalsIgnoreCase(worldId)) return;
            event.setCancelled(true);

        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        if (!player.getWorld().getName().equalsIgnoreCase(worldId)) return;
        if (player.getLocation().getBlockY() < instance.getConfig().getInt("game.co-tp")) {

            instance.teleportToCheckPoint(player);

        }

    }

}
