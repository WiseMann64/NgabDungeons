package com.github.wisemann64.ngabdungeons;

import com.github.wisemann64.ngabdungeons.items.ItemData;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemManager {

    private final Map<String, ItemData> items = new HashMap<>();

    public ItemManager() {
        File f = new File(NgabDungeons.getPlugin().getDataFolder() + "/data/items.json");
        try {
            JsonObject itemList = NgabDungeons.GSON.fromJson(new JsonReader(new FileReader(f)),JsonObject.class);
            itemList.entrySet().forEach(e -> items.put(e.getKey(),new ItemData(e.getKey(),e.getValue().getAsJsonObject())));
        } catch (IOException ex) {
            ex.printStackTrace();
            NgabDungeons.err("Failed to load items");
        }
        NgabDungeons.log("Loaded " + items.size() + " items");
        NgabDungeons.log(items.keySet());
    }

    public static ItemManager getInstance() {
        return NgabDungeons.getItemManager();
    }

    public ItemData getItem(String item) {
        return items.get(item);
    }

    public boolean hasId(String id) {
        return items.containsKey(id);
    }

    public Set<String> getItemIds() {
        return new HashSet<>(items.keySet());
    }
}
