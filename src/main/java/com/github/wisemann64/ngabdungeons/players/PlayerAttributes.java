package com.github.wisemann64.ngabdungeons.players;

import com.github.wisemann64.ngabdungeons.data.DatabaseDriver;
import com.github.wisemann64.ngabdungeons.data.LevelBaseStats;
import com.github.wisemann64.ngabdungeons.data.LevelClassBonus;
import com.github.wisemann64.ngabdungeons.items.EnumEquipmentSlot;
import com.github.wisemann64.ngabdungeons.items.EnumItemStats;
import com.github.wisemann64.ngabdungeons.items.ItemReader;
import com.github.wisemann64.ngabdungeons.utils.EnumDoubleMap;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class PlayerAttributes {

    private final DPlayer owner;
    private final EnumMap<EnumStats,Double> baseStats = new EnumMap<>(EnumStats.class);
    private final EnumMap<EnumStats,Double> statsMap = new EnumMap<>(EnumStats.class);
    private final EnumMap<EnumAdditionalStats,Double> additional = new EnumMap<>(EnumAdditionalStats.class);

    private LevelClassBonus classBonus;
    private EnumDungeonClass selectedClass;

    protected PlayerAttributes(DPlayer owner) {
        this.owner = owner;
        initialize();
    }

    private void initialize() {
        setBaseStats(owner.getLevel());
        setClassStats(owner.getLevel(),owner.getSelectedClass());

        statsMap.putAll(baseStats);
        statsMap.put(EnumStats.HEALTH, baseStats.get(EnumStats.MAX_HEALTH));

        for (EnumAdditionalStats e : EnumAdditionalStats.values()) additional.put(e,100D);
    }



    public void tick() {

//      TODO Multipliers
        EnumDoubleMap<EnumStats> multipliers = new EnumDoubleMap<>(EnumStats.class);
        for (EnumStats es : EnumStats.values()) multipliers.put(es,1);
        if (selectedClass == EnumDungeonClass.TANK) {
            multipliers.add(EnumStats.DEFENSE,classBonus.bonusA()/100);
        } else if (selectedClass == EnumDungeonClass.SUPPORT) {
            multipliers.add(EnumStats.REGEN,classBonus.bonusA()/100);
        }

        if (owner.getUltimateSkill() == EnumClassSkills.HUNTER && owner.hasTrigger("HUNTER")) {
            Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.HUNTER,owner.getSkillLevel(EnumClassSkills.HUNTER));
            multipliers.add(EnumStats.ATTACK_SPEED,data.getOrDefault("aspd",0F));
        }

        if (owner.getUltimateSkill() == EnumClassSkills.BERSERK && owner.hasTrigger("BERSERK")) {
            Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.BERSERK,owner.getSkillLevel(EnumClassSkills.BERSERK));
            multipliers.add(EnumStats.DEFENSE,data.getOrDefault("def",0F)/100);
        }

//        more to go...

//        TODO Flat bonus/buff
        EnumDoubleMap<EnumStats> flatBonus = new EnumDoubleMap<>(EnumStats.class);
        if (selectedClass == EnumDungeonClass.ARCHER) {
            flatBonus.add(EnumStats.CRIT_DAMAGE, classBonus.bonusB());
        } else if (selectedClass == EnumDungeonClass.FIGHTER) {
            flatBonus.add(EnumStats.ATTACK_SPEED, classBonus.bonusB());
        }

        if (owner.getPassiveSkills().contains(EnumClassSkills.POWER)) {
            Map<String, Float> data = SkillHandler.dataGetter(owner, EnumClassSkills.POWER);
            flatBonus.add(EnumStats.CRIT_CHANCE,data.get("cc"));
            flatBonus.add(EnumStats.CRIT_DAMAGE,data.get("cd"));
        }

        if (owner.hasTrigger("BLOODLUST")) {
            Map<String, Float> data = SkillHandler.dataGetter(owner, EnumClassSkills.BLOODLUST);
            flatBonus.add(EnumStats.ATTACK_SPEED,data.get("aspd"));
        }

        if (owner.getPassiveSkills().contains(EnumClassSkills.HATRED)) {
            Map<String, Float> data = SkillHandler.dataGetter(owner, EnumClassSkills.HATRED);
            float missingHealth = 1-owner.getHealthFraction();
            double bonus = data.getOrDefault("strength",0F)*missingHealth;
            flatBonus.add(EnumStats.STRENGTH,bonus);
        }

        if (owner.getPassiveSkills().contains(EnumClassSkills.FRENZY)) {
            Map<String, Float> data = SkillHandler.dataGetter(owner, EnumClassSkills.FRENZY);
            int count = owner.getAdditionalTrigger("FRENZY");
            if (count > 0) flatBonus.add(EnumStats.ATTACK_SPEED,count*data.getOrDefault("aspd",0F));
        }

//        more to go...

        Map<EnumEquipmentSlot, ItemReader> inv = owner.getEquipmentSet();
        Function<EnumItemStats,Float> getBase = i -> {
            AtomicDouble f = new AtomicDouble(0F);
            inv.forEach((e,r) -> f.addAndGet(r.getData().getBaseStat(i)));
            return (float) f.get();
        };

        Function<EnumStats,Double> calculate = stats -> {
            double base = baseStats.getOrDefault(stats,0D);
            double flat = flatBonus.get(stats);
            float multiplier = multipliers.getOrDefault(stats,1D).floatValue();
            double item = switch (stats) {
                case MAX_HEALTH -> getBase.apply(EnumItemStats.HEALTH);
                case DEFENSE -> getBase.apply(EnumItemStats.DEFENSE);
                case REGEN -> getBase.apply(EnumItemStats.REGEN);
                case STRENGTH -> getBase.apply(EnumItemStats.STRENGTH);
                case CRIT_CHANCE -> getBase.apply(EnumItemStats.CRIT_CHANCE);
                case CRIT_DAMAGE -> getBase.apply(EnumItemStats.CRIT_DAMAGE);
                case ATTACK_SPEED -> getBase.apply(EnumItemStats.ATTACK_SPEED);
                case PENETRATION -> getBase.apply(EnumItemStats.PENETRATION);
                default -> 0D;
            };
            return multiplier*(base + flat + item);
        };

        for (EnumStats e : EnumStats.values()) {
            if (e == EnumStats.HEALTH) continue;
            set(e,calculate.apply(e));
        }
    }

    public DPlayer getOwner() {
        return owner;
    }

    public double getHealth() {
        return statsMap.get(EnumStats.HEALTH);
    }
    public double getDefense() {
        return statsMap.get(EnumStats.DEFENSE);
    }
    public double getMaxHealth() {
        return statsMap.get(EnumStats.MAX_HEALTH);
    }
    public double getRegen() {
        return statsMap.get(EnumStats.REGEN);
    }
    public double getStrength() {
        return statsMap.get(EnumStats.STRENGTH);
    }
    public double getCritChance() {
        return statsMap.get(EnumStats.CRIT_CHANCE);
    }
    public double getCritDamage() {
        return statsMap.get(EnumStats.CRIT_DAMAGE);
    }
    public double getAttackSpeed() {
        return statsMap.get(EnumStats.ATTACK_SPEED);
    }
    public double getPenetration() {
        return statsMap.get(EnumStats.PENETRATION);
    }

    public double getMeleeDamageMultiplier() {
        double base = 100;
//        TODO Items/buff
        if (owner.getPassiveSkills().contains(EnumClassSkills.HATRED)) {
            Map<String, Float> data = SkillHandler.dataGetter(owner, EnumClassSkills.HATRED);
            float missingHealth = 1-owner.getHealthFraction();
            double bonus = data.getOrDefault("melee",0F)*missingHealth;
            base += bonus;
        }
        if (owner.getPassiveSkills().contains(EnumClassSkills.FRENZY)) {
            Map<String, Float> data = SkillHandler.dataGetter(owner, EnumClassSkills.FRENZY);
            int count = owner.getAdditionalTrigger("FRENZY");
            if (count > 0) {
                base += data.getOrDefault("melee",0F)*count;
            }
        }

        if (owner.getUltimateSkill() == EnumClassSkills.BERSERK && owner.hasTrigger("BERSERK")) {
            Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.BERSERK,owner.getSkillLevel(EnumClassSkills.BERSERK));
            base += data.getOrDefault("melee",0F);
        }

        if (selectedClass == EnumDungeonClass.FIGHTER) {
            base += classBonus.bonusA();
        }

        if (selectedClass == EnumDungeonClass.ARCHER) {
            base *= 0.5;
        }

        return base/100D;
    }

    public double getArrowDamageMultiplier() {
        double base = 100;
//        TODO Items/buff
        if (owner.getPassiveSkills().contains(EnumClassSkills.POWER)) {
            Map<String, Float> data = SkillHandler.dataGetter(owner, EnumClassSkills.POWER);
            base += data.getOrDefault("ranged",0F);
        }

        if (selectedClass == EnumDungeonClass.ARCHER) {
            base += classBonus.bonusA();
        }
        if (selectedClass == EnumDungeonClass.FIGHTER) {
            base *= 0.75;
        }

        return base/100D;
    }

    public double getDamageMultiplier() {
//        TODO Item/Buff
        double base = 100;

        if (selectedClass == EnumDungeonClass.TANK) {
            base *= 0.4;
        }
        if (selectedClass == EnumDungeonClass.SUPPORT) {
            base *= 0.6;
        }
        return base/100D;
    }

    public void setHealth(double amount) {
        statsMap.put(EnumStats.HEALTH,amount);
    }

    public EnumMap<EnumStats, Double> getBaseStats() {
        return baseStats;
    }

    public void describe() {
        owner.sendMessage("Max Health: " + get(EnumStats.MAX_HEALTH));
        owner.sendMessage("Defense: " + get(EnumStats.DEFENSE));
        owner.sendMessage("Regen: " + get(EnumStats.REGEN));
        owner.sendMessage("Strength: " + get(EnumStats.STRENGTH));
        owner.sendMessage("Crit Chance: " + get(EnumStats.CRIT_CHANCE));
        owner.sendMessage("Crit Damage: " + get(EnumStats.CRIT_DAMAGE));
        owner.sendMessage("Attack Speed: " + get(EnumStats.ATTACK_SPEED));
        owner.sendMessage("Penetration: " + get(EnumStats.PENETRATION));
        owner.sendMessage("Melee Damage: " + getMeleeDamageMultiplier());
        owner.sendMessage("Arrow Damage: " + getArrowDamageMultiplier());
        owner.sendMessage("Damage Multiplier: " + getDamageMultiplier());
    }

    private double get(EnumStats stats) {
        return statsMap.getOrDefault(stats,0d);
    }

    private void set(EnumStats stats, double val) {
        statsMap.put(stats,val);
    }

    public void setBaseStats(int level) {
        LevelBaseStats lbs = DatabaseDriver.getInstance().getBaseStats(level);

        baseStats.put(EnumStats.MAX_HEALTH, lbs.health());
        baseStats.put(EnumStats.REGEN, lbs.regen());
        baseStats.put(EnumStats.DEFENSE,lbs.defense());
        baseStats.put(EnumStats.STRENGTH, lbs.strength());
        baseStats.put(EnumStats.CRIT_CHANCE, lbs.critChance());
        baseStats.put(EnumStats.CRIT_DAMAGE, lbs.critDamage());
        baseStats.put(EnumStats.ATTACK_SPEED, 0D);
        baseStats.put(EnumStats.PENETRATION, lbs.penetration());
    }

    public void setClassStats(int level, EnumDungeonClass selectedClass) {
        classBonus = DatabaseDriver.getInstance().getClassBonus(selectedClass,level);
        this.selectedClass = selectedClass;
    }

    public int getAttackDelay() {
        int aspd = Math.min(800,(int) get(EnumStats.ATTACK_SPEED));
        return 10 - aspd/100;
    }
}
