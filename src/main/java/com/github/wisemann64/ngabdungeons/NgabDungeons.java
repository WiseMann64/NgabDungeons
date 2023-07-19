package com.github.wisemann64.ngabdungeons;

import com.github.wisemann64.ngabdungeons.cmds.*;
import com.github.wisemann64.ngabdungeons.combat.CombatEntity;
import com.github.wisemann64.ngabdungeons.combat.CombatListeners;
import com.github.wisemann64.ngabdungeons.data.DatabaseDriver;
import com.github.wisemann64.ngabdungeons.listeners.PlayerListeners;
import com.github.wisemann64.ngabdungeons.mobs.AbstractDungeonMob;
import com.github.wisemann64.ngabdungeons.mobs.MobListeners;
import com.github.wisemann64.ngabdungeons.players.DPlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.opencsv.exceptions.CsvException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public final class NgabDungeons extends JavaPlugin {

    private static NgabDungeons plugin;
    public static Gson GSON;
    public static Random RANDOM;
    private static YamlConfiguration config;

    private static DatabaseDriver databaseDriver;
    private static PlayerManager playerManager;
    private static MobManager mobManager;
    private static ItemManager itemManager;

    public static Location TEMP_SPAWN_LOCATION;
    private static World mainWorld;

    private BukkitRunnable tickAction;

    @Override
    public void onEnable() {
        plugin = this;
        GSON = new GsonBuilder().setPrettyPrinting().create();
        RANDOM = new Random();

        prepareConfig();
        try {
            databaseDriver = new DatabaseDriver(this);
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            getLogger().severe("Couldn't load database driver");
            getServer().getPluginManager().disablePlugin(this);
        }

        playerManager = new PlayerManager();
        mobManager = new MobManager();
        itemManager = new ItemManager();

        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new CombatListeners(),this);
        getServer().getPluginManager().registerEvents(new MobListeners(),this);

        getCommand("test").setExecutor(new CommandTest());
        getCommand("basestats").setExecutor(new CommandBaseStats());
        getCommand("item").setExecutor(new CommandItem());
        getCommand("xp").setExecutor(new CommandXp());
        getCommand("switch").setExecutor(new CommandSwitch());

        mainWorld = Bukkit.getWorld("world");
        TEMP_SPAWN_LOCATION = new Location(mainWorld,155.0,64.0,-248.0,180f,0f);

        startTicker();
    }

    private void startTicker() {
        tickAction = new BukkitRunnable() {
            @Override
            public void run() {
                pluginTick();
            }
        };
        tickAction.runTaskTimer(this,1,1);
    }

    private void pluginTick() {
        playerManager.tick();
        mobManager.tick();
    }

    @Override
    public void onDisable() {
        try {
            tickAction.cancel();
        } catch (IllegalStateException ignored) {

        }
        playerManager.saveAll();
    }

    public static NgabDungeons getPlugin() {
        return plugin;
    }
    public static YamlConfiguration getNDConfig() {
        return config;
    }
    public static PlayerManager getPlayerManager() {
        return playerManager;
    }
    public static MobManager getMobManager() {
        return mobManager;
    }
    public static ItemManager getItemManager() {
        return itemManager;
    }
    public static DatabaseDriver getDatabaseDriver() {
        return databaseDriver;
    }
    public static NamespacedKey key(String s) {
        return new NamespacedKey(plugin,s);
    }
    public static World getWorld() {
        return mainWorld;
    }

    public static DPlayer getPlayer(Player p) {
        return playerManager.getPlayer(p);
    }
    public static AbstractDungeonMob getMob(UUID uuid) {
        return mobManager.getMob(uuid);
    }
    public static CombatEntity getCombatEntity(UUID uuid) {
        DPlayer a = playerManager.getPlayer(uuid);
        if (a != null) return a;
        return mobManager.getMob(uuid);
    }


    public void prepareConfig() {
        File f = new File(plugin.getDataFolder(), "config.yml");
        if (!f.exists()) saveResource("config.yml",true);
        config = YamlConfiguration.loadConfiguration(f);

        saveResource("data/xp_reqs.json",true);
        saveResource("data/items.json",true);
        saveResource("data/base_stats.csv",true);
        saveResource("data/class_bonus.csv",true);
        saveResource("data/class_skills.json",true);
    }

    public static void savePlayerData(DPlayer player) {
        JsonObject o = player.getConfig();
        File f = new File(plugin.getDataFolder().getPath() + "/player_data", player.getUniqueId() + ".json");
        try {
            if (!f.exists()) f.createNewFile();
            FileWriter w = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(w);
            GSON.toJson(o,bw);
            bw.close();
            w.close();
        } catch (IOException ex) {
            System.err.println("Couldn't save player data with id " + player.getUniqueId() + "!");
        }
    }

    public static JsonObject getPlayerData(Player player) {
        File f = new File(plugin.getDataFolder().getPath() + "/player_data", player.getUniqueId() + ".json");
        if (!f.exists()) {
            f = new File(plugin.getDataFolder().getPath() + "/player_data", "player_uuid.json");
            if (!f.exists()) plugin.saveResource("player_data/player_uuid.json",false);
        }
        try {
            return GSON.fromJson(new JsonReader(new FileReader(f)),JsonObject.class);
        } catch (IOException ex) {
            System.err.println("Couldn't read data from player with uuid " + player.getUniqueId() + "!");
            return new JsonObject();
        }
    }

    public static boolean isNewPlayer(Player player) {
        File f = new File(plugin.getDataFolder().getPath() + "/player_data", player.getUniqueId() + ".json");
        return !f.exists();
    }

    public static void log(Object msg) {
        plugin.getLogger().log(Level.INFO,String.valueOf(msg));
    }
    public static void warn(Object msg) {
        plugin.getLogger().log(Level.WARNING,String.valueOf(msg));
    }
    public static void err(Object msg) {
        plugin.getLogger().log(Level.SEVERE,String.valueOf(msg));
    }
}
