package com.github.wisemann64.ngabdungeons.data;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.players.EnumDungeonClass;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DatabaseDriver {

    private final NgabDungeons plugin;

    private final Map<Integer,LevelBaseStats> levelBaseStatsMap = new HashMap<>();
    private final EnumMap<EnumDungeonClass,Map<Integer,LevelClassBonus>> classBonus = new EnumMap<>(EnumDungeonClass.class);

    public DatabaseDriver(NgabDungeons plugin) throws IOException,CsvException {
        this.plugin = plugin;
        init();
    }

    public static DatabaseDriver getInstance() {
        return NgabDungeons.getDatabaseDriver();
    }

    public void init() throws IOException,CsvException {
        File f = new File(plugin.getDataFolder(),"data");
        f = new File(f,"xp_reqs.json");
        JsonObject level = new Gson().fromJson(new FileReader(f),JsonObject.class);
        int sum = 0;
        for (int i = 0; i < 60; i++) {
            combatLevelup[i] = level.getAsJsonArray("combat_skills_req").get(i).getAsInt();
            sum += combatLevelup[i];
            combatLevelupTotal[i] = sum;
        }

        sum = 0;
        for (int i = 0; i < 60; i++) {
            classLevelup[i] = level.getAsJsonArray("class_skills_req").get(i).getAsInt();
            sum += classLevelup[i];
            classLevelupTotal[i] = sum;
        }

        prepareBaseStats();
        prepareClassBonusStats();
    }

    private final int[] combatLevelup = new int[60];
    private final int[] combatLevelupTotal = new int[60];
    private final int[] classLevelup = new int[60];
    private final int[] classLevelupTotal = new int[60];

    public int combatLevelFromXp(double xp) {
        int i;
        for (i = 0 ; i < 60 ; i++) if (xp < combatLevelupTotal[i]) break;
        return i;
    }

    public int classLevelFromXp(double xp) {
        int i;
        for (i = 0 ; i < 60 ; i++) if (xp < classLevelupTotal[i]) break;
        return i;
    }

    public int getCombatLevelUpXp(int level) {
        if (level < 1 || level > 60) return 0;
        return combatLevelup[level-1];
    }

    public int getCombatTotalLevelUpXp(int level) {
        if (level < 1 || level > 60) return 0;
        return combatLevelupTotal[level-1];
    }

    public int getClassLevelUpXp(int level) {
        if (level < 1 || level > 60) return 0;
        return classLevelup[level-1];
    }

    public int getClassTotalLevelUpXp(int level) {
        if (level < 1 || level > 60) return 0;
        return classLevelupTotal[level-1];
    }

    private void prepareBaseStats() throws IOException, CsvException {
        File f = new File(plugin.getDataFolder(),"data/base_stats.csv");
        FileReader fr = new FileReader(f);
        CSVReader csvReader = new CSVReaderBuilder(fr).withSkipLines(1).build();
        csvReader.readAll().forEach(st -> {
            int level = Integer.parseInt(st[0]);
            double health = Double.parseDouble(st[1]);
            double regen = Double.parseDouble(st[2]);
            double defense = Double.parseDouble(st[3]);
            double str = Double.parseDouble(st[4]);
            double cc = Double.parseDouble(st[5]);
            double cd = Double.parseDouble(st[6]);
            double pen = Double.parseDouble(st[8]);
            levelBaseStatsMap.put(level,new LevelBaseStats(health,regen,defense,str,cc,cd,pen));
        });
    }

    public LevelBaseStats getBaseStats(int level) {
        return levelBaseStatsMap.get(level);
    }

    private void prepareClassBonusStats() throws IOException, CsvException {
        Arrays.stream(EnumDungeonClass.values()).forEach(c -> classBonus.put(c,new HashMap<>()));

        File f = new File(plugin.getDataFolder(),"data/class_bonus.csv");
        FileReader fr = new FileReader(f);
        CSVReader csvReader = new CSVReaderBuilder(fr).withSkipLines(1).build();
        csvReader.readAll().forEach(st -> {
            int level = Integer.parseInt(st[0]);
            classBonus.get(EnumDungeonClass.ARCHER).put(level, new LevelClassBonus(level,Double.parseDouble(st[1]),Double.parseDouble(st[2])));
            classBonus.get(EnumDungeonClass.FIGHTER).put(level, new LevelClassBonus(level,Double.parseDouble(st[3]),Double.parseDouble(st[4])));
            classBonus.get(EnumDungeonClass.TANK).put(level, new LevelClassBonus(level,Double.parseDouble(st[5]),Double.parseDouble(st[6])));
            classBonus.get(EnumDungeonClass.SUPPORT).put(level, new LevelClassBonus(level,Double.parseDouble(st[7]),Double.parseDouble(st[8])));
        });
    }

    public LevelClassBonus getClassBonus(EnumDungeonClass c, int level) {
        return classBonus.get(c).get(level);
    }

}