package com.github.wisemann64.ngabdungeons.players;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class SkillHolder {

    private final DPlayer owner;
    private final EnumDungeonClass dungeonClass;

    private EnumClassSkills primary;
    private EnumClassSkills secondary;
    private EnumClassSkills ultimate;

    public SkillHolder(DPlayer owner, EnumDungeonClass dungeonClass) {
        this.owner = owner;
        this.dungeonClass = dungeonClass;
    }

    public void ofJson(JsonElement ele) {
        if (!ele.isJsonArray()) return;
        JsonArray arr = ele.getAsJsonArray();
        if (arr.size() < 3) return;
        primary = arr.get(0).isJsonNull() ? null : EnumClassSkills.valueOf(arr.get(0).getAsString());
        secondary = arr.get(1).isJsonNull() ? null : EnumClassSkills.valueOf(arr.get(1).getAsString());
        ultimate = arr.get(2).isJsonNull() ? null : EnumClassSkills.valueOf(arr.get(2).getAsString());
    }

    public DPlayer getOwner() {
        return owner;
    }

    public EnumDungeonClass getDungeonClass() {
        return dungeonClass;
    }

    public EnumClassSkills getPrimary() {
        return primary;
    }

    public EnumClassSkills getSecondary() {
        return secondary;
    }

    public EnumClassSkills getUltimate() {
        return ultimate;
    }

    public void setPrimary(EnumClassSkills primary) {
        if (primary.getClassId() != dungeonClass.getId()) throw new IllegalArgumentException("Class id doesn't match");
        if (primary.isUltimate()) throw new IllegalArgumentException("Skill in 1st slot cannot be an ultimate skill");
        this.primary = primary;
    }

    public void setSecondary(EnumClassSkills secondary) {
        if (secondary.getClassId() != dungeonClass.getId()) throw new IllegalArgumentException("Class id doesn't match");
        if (secondary.isUltimate()) throw new IllegalArgumentException("Skill in 2nd slot cannot be an ultimate skill");
        this.secondary = secondary;
    }

    public void setUltimate(EnumClassSkills ultimate) {
        if (ultimate.getClassId() != dungeonClass.getId()) throw new IllegalArgumentException("Class id doesn't match");
        if (!ultimate.isUltimate()) throw new IllegalArgumentException("Skill in 3rd slot must be an ultimate skill");
        this.ultimate = ultimate;
    }

    public JsonArray asJson() {
        JsonArray ret = new JsonArray();
        if (primary == null) ret.add(JsonNull.INSTANCE); else ret.add(primary.name());
        if (secondary == null) ret.add(JsonNull.INSTANCE); else ret.add(secondary.name());
        if (ultimate == null) ret.add(JsonNull.INSTANCE); else ret.add(ultimate.name());
        return ret;
    }
}
