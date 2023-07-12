package com.github.wisemann64.ngabdungeons.players;

import com.github.wisemann64.ngabdungeons.data.DatabaseDriver;
import com.google.gson.JsonObject;
import org.bukkit.Sound;

import java.util.function.Function;

public class PlayerLevel {

    private final DPlayer owner;
    private final EnumLevelType type;

    private float xp;
    private float totalXp;
    private int level;

    public PlayerLevel(DPlayer owner, EnumLevelType type, JsonObject in) {
        this.owner = owner;
        this.type = type;

        if (in != null) {
            xp = in.get("xp").getAsFloat();
            totalXp = in.get("cumulative_xp").getAsFloat();
            level = in.get("level").getAsInt();
        } else {
            xp = 0f;
            totalXp = 0f;
            level = 0;
        }

        initSyncLevel();
    }

    public EnumLevelType getType() {
        return type;
    }

    public float getXp() {
        return xp;
    }

    public float getTotalXp() {
        return totalXp;
    }

    public int getLevel() {
        return level;
    }

    public void addXp(float xp) {
        totalXp += xp;
        this.xp += xp;

        syncLevel();
    }

    private void initSyncLevel() {
        int targetLevel = type == EnumLevelType.COMBAT ? DatabaseDriver.getInstance().combatLevelFromXp(totalXp) : DatabaseDriver.getInstance().classLevelFromXp(totalXp);
        if (level != targetLevel) {
            owner.sendMessage("&aLevel Up! &5"+ type.getName() +" Level " + targetLevel);
            owner.selfSound(Sound.ENTITY_PLAYER_LEVELUP,1,1.5f);
        }
        level = targetLevel;
        xp = totalXp - (type == EnumLevelType.COMBAT ?
                DatabaseDriver.getInstance().getCombatTotalLevelUpXp(level) :
                DatabaseDriver.getInstance().getClassTotalLevelUpXp(level));
    }

    private void syncLevel() {
        int targetLevel = type == EnumLevelType.COMBAT ? DatabaseDriver.getInstance().combatLevelFromXp(totalXp) : DatabaseDriver.getInstance().classLevelFromXp(totalXp);
        if (level < targetLevel) {
            setLevelTo(targetLevel);
            xp = totalXp - (type == EnumLevelType.COMBAT ?
                    DatabaseDriver.getInstance().getCombatTotalLevelUpXp(level) :
                    DatabaseDriver.getInstance().getClassTotalLevelUpXp(level));
        }
    }

    private void setLevelTo(int to) {
        if (level == to) return;
        if (level < to) {
            owner.sendMessage("&aLevel Up! &5"+ type.getName() +" Level " + to);
            owner.selfSound(Sound.ENTITY_PLAYER_LEVELUP,1,1.5f);
        }
        level = to;
        if (type == EnumLevelType.COMBAT) owner.getAttributes().setBaseStats(to);
        else owner.updateClassStats(switch (type) {
            case TANK -> EnumDungeonClass.TANK;
            case SUPPORT -> EnumDungeonClass.SUPPORT;
            case ARCHER -> EnumDungeonClass.ARCHER;
            case FIGHTER -> EnumDungeonClass.FIGHTER;
            default -> null;
        });
    }

    public JsonObject asJson() {
        JsonObject ret = new JsonObject();
        ret.addProperty("xp",xp);
        ret.addProperty("cumulative_xp",totalXp);
        ret.addProperty("level",level);
        return ret;
    }

    public float getProgress() {
        if (type != EnumLevelType.COMBAT) return 0.0f;
        if (level == 60) return 1.0f;
        float t = xp / DatabaseDriver.getInstance().getCombatLevelUpXp(level+1);
        return Math.max(0,Math.min(1,t));
    }
}
