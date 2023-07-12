package com.github.wisemann64.ngabdungeons;

import com.github.wisemann64.ngabdungeons.players.DPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerManager {

    private final Map<UUID, DPlayer> players = new HashMap<>();

    public static PlayerManager instance() {
        return NgabDungeons.getPlayerManager();
    }

    public void tick() {
        players.values().forEach(DPlayer::tick);
    }

    public void addPlayer(DPlayer player) {
        players.put(player.getUniqueId(),player);
    }

    public static boolean hasInstance(Player p) {
        return NgabDungeons.getPlayerManager().players.containsKey(p.getUniqueId());
    }

    public DPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public DPlayer getPlayer(Player p) {
        return players.get(p.getUniqueId());
    }

    public void removePlayer(DPlayer p) {
        players.remove(p.getUniqueId());
    }

    public void saveAll() {
        Set<DPlayer> playerSet = new HashSet<>(players.values());
        playerSet.forEach(DPlayer::remove);
    }

    public Map<UUID,DPlayer> getPlayers() {
        return new HashMap<>(players);
    }

}
