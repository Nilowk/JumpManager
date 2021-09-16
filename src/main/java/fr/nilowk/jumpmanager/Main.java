package fr.nilowk.jumpmanager;

import fr.nilowk.jumpmanager.listener.GameManager;
import fr.nilowk.jumpmanager.listener.Navigation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new Navigation(this), this);
        getServer().getPluginManager().registerEvents(new GameManager(this), this);

    }

    public Location getSpawn() {

        World world = getServer().getWorld(this.getConfig().getString("teleport-point.spawn.world"));
        double x = this.getConfig().getDouble("teleport-point.spawn.x");
        double y = this.getConfig().getDouble("teleport-point.spawn.y");
        double z = this.getConfig().getDouble("teleport-point.spawn.z");
        float yaw = (float) this.getConfig().getDouble("teleport-point.spawn.yaw");
        float pitch = (float) this.getConfig().getDouble("teleport-point.spawn.pitch");

        Location spawn = (new Location(world, x, y, z, yaw, pitch));
        return spawn;

    }

    public void teleportToCheckPoint(Player player) {

        for (String string : getConfig().getConfigurationSection("players").getKeys(false)) {

            ConfigurationSection k = getConfig().getConfigurationSection("players." + string);

            if (string.equalsIgnoreCase(player.getUniqueId().toString())) {

                World world = getServer().getWorld(k.getString("checkpoint.world"));
                double x = k.getDouble("checkpoint.x");
                double y = k.getDouble("checkpoint.y");
                double z = k.getDouble("checkpoint.z");
                float yaw = (float) k.getDouble("checkpoint.yaw");
                float pitch = (float) k.getDouble("checkpoint.pitch");

                Location loc = (new Location(world, x, y, z, yaw, pitch));

                player.teleport(loc);

            }

        }

    }

    public void setCheckPoint(Player player, Location loc) {

        ConfigurationSection key = getConfig().getConfigurationSection("players." + player.getUniqueId());

        key.set("checkpoint.x", loc.getX());
        key.set("checkpoint.y", loc.getY());
        key.set("checkpoint.z", loc.getZ());
        key.set("checkpoint.yaw", loc.getYaw());
        key.set("checkpoint.pitch", loc.getPitch());

        saveConfig();

    }

    public HashMap<Player, String> getInJump() {

        HashMap<Player, String> inJump = new HashMap<>();
        if (getConfig().getConfigurationSection("in-jump") != null) {
            for (String sting : getConfig().getConfigurationSection("in-jump").getKeys(false)) {

                inJump.put(Bukkit.getPlayer(sting), getConfig().getString("in-jump." + sting));

            }
        } else {
            inJump.put(null, null);
        }
        return inJump;

    }

    public void addInJump(Player player, String id) {

        if (!getConfig().getConfigurationSection("in-jump").contains(player.getUniqueId().toString())) {

            getConfig().getConfigurationSection("in-jump").set(player.getUniqueId().toString(), id);

        } else {

            if (getConfig().getConfigurationSection("in-jump").getString(player.getUniqueId().toString()) == null) {

                getConfig().getConfigurationSection("in-jump").set(player.getUniqueId().toString(), id);

            }

        }

        saveConfig();

    }

    public void addStats(Player player, String id) {

        ConfigurationSection key = getConfig().getConfigurationSection("players." + player.getUniqueId() + "stats");
        key.set(id, id);

    }

    public void removeInJump(Player player) {

        getConfig().getConfigurationSection("in-jump").set(player.getUniqueId().toString(), null);

        saveConfig();

    }

}
