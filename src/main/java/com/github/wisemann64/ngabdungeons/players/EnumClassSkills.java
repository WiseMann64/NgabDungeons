package com.github.wisemann64.ngabdungeons.players;

public enum EnumClassSkills {

    MULTI_ARROW(0,false, "Multi Arrow", false),
    ANTI_GRAVITY(0,false,"Anti-Gravity Arrow", true),
    PENETRATE(0,false,"Penetrate", true),
    POWER(0,false,"Power", false),
    BONE_BREAKER(0,true,"Bone Breaker", true),
    ARROW_STORM(0,true,"Arrow Storm", true),

    CLEAVE(1,false,"Cleave", true),
    HATRED(1,false,"Internal Hatred", false),
    BLOODLUST(1,false,"Bloodlust", true),
    FRENZY(1,false,"Frenzy", false),
    THRUST(1,false,"Thrust", true),
    BERSERK(1,true,"Berserk", false),
    HUNTER(1,true,"Hunting Mode", true),

    FORTRESS(2,false,"Fortress", false),
    PROVOKE(2,false,"Provocation", true),
    REINFORCED(2,false,"Reinforced", true),
    BLAST(2,false,"Blast" ,true),
    SHIELD(2,true,"Shield",false),
    PERFECT(2,true,"Perfect Defense",true),

    BRAVE(3,false,"Brave Aura",false),
    SMALL_HEAL(3,false,"Small Heal",true),
    SUPERIOR(3,false,"Superior",false),
    RESURRECTION(3,false,"Resurrection",false),
    BIG_HEAL(3,false,"Big Heal",true),
    TEAM_BUFF(3,false,"Hail to the Demon",true),

    ;
    private final int classId;
    private final boolean ultimate;
    private final String name;
    private final boolean active;

    EnumClassSkills(int classId, boolean ultimate, String name, boolean active) {
        this.classId = classId;
        this.ultimate = ultimate;
        this.name = name;
        this.active = active;
    }

    public int getClassId() {
        return classId;
    }

    public boolean isUltimate() {
        return ultimate;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
