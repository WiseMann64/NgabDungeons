package com.github.wisemann64.ngabdungeons.players;

import com.github.wisemann64.ngabdungeons.data.DatabaseDriver;
import com.github.wisemann64.ngabdungeons.items.ItemReader;
import com.github.wisemann64.ngabdungeons.skills.SkillArrowStorm;
import com.github.wisemann64.ngabdungeons.skills.SkillBoneBreaker;
import com.github.wisemann64.ngabdungeons.skills.SkillThrust;
import org.bukkit.Sound;

import java.util.Map;

public class SkillHandler {

    private final DPlayer owner;

    private int cdPrimary = 0;
    private int cdSecondary = 0;
    private int cdUltimate = 0;

    private ItemReader castItem;

    protected SkillHandler(DPlayer owner) {
        this.owner = owner;
    }



    public void tick() {
        if (cdPrimary == 1) {
            cdPrimary = 0;
            owner.sendMessage("&aYour primary skill &6" + owner.getPrimarySkill().getName() + "&a is now ready.");
        } else if (cdPrimary > 1) cdPrimary--;

        if (cdSecondary == 1) {
            cdSecondary = 0;
            owner.sendMessage("&aYour secondary skill &6" + owner.getSecondarySkill().getName() + "&a is now ready.");
        } else if (cdSecondary > 1) cdSecondary--;

        if (cdUltimate == 1) {
            cdUltimate = 0;
            owner.sendMessage("&aYour ultimate skill &6" + owner.getUltimateSkill().getName() + "&a is now ready.");
        } else if (cdUltimate > 1) cdUltimate--;
    }

    public DPlayer getOwner() {
        return owner;
    }

    public void castPrimary() {
        EnumClassSkills skill = owner.getPrimarySkill();
        if (skill == null || !skill.isActive() || owner.getSkillLevel(skill) < 1) {
            castItem = null;
            return;
        }
        if (cdPrimary != 0) {
            int cdSecs = cdPrimary/20 + 1;
            owner.sendMessage("&cThis skill is on cooldown for " + cdSecs + " second" + (cdSecs > 2 ? "s" : "") + ".");
            cdSound();
            castItem = null;
            return;
        }
        castSkill(skill,1);
        castItem = null;
    }

    public void castSecondary() {
        EnumClassSkills skill = owner.getSecondarySkill();
        if (skill == null) return;
        if (!skill.isActive()) return;
        if (owner.getSkillLevel(skill) < 1) return;
        if (cdSecondary != 0) {
            int cdSecs = cdSecondary/20 + 1;
            owner.sendMessage("&cThis skill is on cooldown for " + cdSecs + " second" + (cdSecs > 2 ? "s" : "") + ".");
            cdSound();
            return;
        }
        castSkill(skill,2);
    }

    public void castUltimate() {
        EnumClassSkills skill = owner.getUltimateSkill();
        if (skill == null) return;
        if (skill == EnumClassSkills.BERSERK) {
            castBerserk();
            return;
        }
        if (!skill.isActive()) return;
        if (owner.getSkillLevel(skill) < 1) return;
        if (cdUltimate != 0) {
            int cdSecs = cdUltimate/20 + 1;
            owner.sendMessage("&cThis skill is on cooldown for " + cdSecs + " second" + (cdSecs > 2 ? "s" : "") + ".");
            cdSound();
            return;
        }
        castSkill(skill,3);
    }

    private void giveCd(int slot, int duration) {
        switch (slot) {
            case 1 -> cdPrimary = duration;
            case 2 -> cdSecondary = duration;
            case 3 -> cdUltimate = duration;
        }
    }

    private void castSkill(EnumClassSkills skill, int skillSlot) {
        switch (skill) {
            case ANTI_GRAVITY -> castAntiGravity(skillSlot);
            case PENETRATE -> castPenetrate(skillSlot);
            case BONE_BREAKER -> castBoneBreaker();
            case ARROW_STORM -> castArrowStorm();

            case CLEAVE -> castCleave(skillSlot);
            case BLOODLUST -> castBloodlust(skillSlot);
            case THRUST -> castThrust(skillSlot);
            case HUNTER -> castHuntingMode();
        }
    }

    private void castPenetrate(int skillSlot) {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.PENETRATE,owner.getSkillLevel(EnumClassSkills.PENETRATE));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        giveCd(skillSlot,cd);
        owner.addTrigger("PENETRATE",cd);
        owner.sendMessage("&aYou casted skill &6Penetrate&a.");
    }
    private void castAntiGravity(int skillSlot) {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.ANTI_GRAVITY,owner.getSkillLevel(EnumClassSkills.ANTI_GRAVITY));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        giveCd(skillSlot,cd);
        owner.addTrigger("ANTI_GRAVITY",cd);
        owner.sendMessage("&aYou casted skill &6" + EnumClassSkills.ANTI_GRAVITY.getName() +  "&a.");
    }
    private void castBoneBreaker() {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.BONE_BREAKER,owner.getSkillLevel(EnumClassSkills.BONE_BREAKER));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        giveCd(3,cd);
        owner.sendMessage("&aYou casted ultimate skill &6" + EnumClassSkills.BONE_BREAKER.getName() +  "&a.");
        double base = data.getOrDefault("base_dmg",0F);
        double frag = data.getOrDefault("frag_dmg",0F);
        double def = data.getOrDefault("def_red",0F);
        double crit = data.getOrDefault("crit",0F);
        new SkillBoneBreaker(owner,base,frag,def,crit,castItem);
        owner.worldSound(Sound.ENTITY_SKELETON_AMBIENT,1,1.25F);
    }
    private void castArrowStorm() {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.ARROW_STORM,owner.getSkillLevel(EnumClassSkills.ARROW_STORM));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        giveCd(3,cd);
        owner.sendMessage("&aYou casted ultimate skill &6" + EnumClassSkills.ARROW_STORM.getName() +  "&a.");
        int groups = data.getOrDefault("groups",0F).intValue();
        float damage = data.getOrDefault("damage",0F);
        new SkillArrowStorm(owner,damage,groups,castItem);
    }

    private void castCleave(int skillSlot) {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.CLEAVE,owner.getSkillLevel(EnumClassSkills.CLEAVE));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        giveCd(skillSlot,cd);
        owner.addTrigger("CLEAVE",cd);
        owner.addAdditionalTrigger("CLEAVE",3);
        owner.sendMessage("&aYou casted skill &6Cleave&a.");
    }
    private void castBloodlust(int skillSlot) {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.BLOODLUST,owner.getSkillLevel(EnumClassSkills.BLOODLUST));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        giveCd(skillSlot,cd);
        int duration = (int) (data.getOrDefault("duration",0F)*20);
        owner.addTrigger("BLOODLUST",duration);
        owner.sendMessage("&aYou casted skill &6Bloodlust&a.");
    }
    private void castThrust(int skillSlot) {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.THRUST,owner.getSkillLevel(EnumClassSkills.THRUST));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        giveCd(skillSlot,cd);
        owner.sendMessage("&aYou casted skill &6" + EnumClassSkills.THRUST.getName() +  "&a.");
        float damage = data.getOrDefault("damage",0F);
        float defReduction = data.getOrDefault("def_red",0F);
        float ignoreDefense = data.getOrDefault("ignore",0F);
        new SkillThrust(owner,damage,ignoreDefense,defReduction,castItem);
    }
    private void castBerserk() {
        if (cdUltimate != 0) {
            int cdSecs = cdUltimate/20 + 1;
            owner.sendMessage("&cThis skill is on cooldown for " + cdSecs + " second" + (cdSecs > 2 ? "s" : "") + ".");
            cdSound();
        } else {
            owner.sendMessage("&aThis skill is available and will automatically cast when your &cHealth &ais below &625%&a.");
            cdSound();
        }
    }
    private void castHuntingMode() {
        Map<String, Float> data = DatabaseDriver.getInstance().getSkillData(EnumClassSkills.HUNTER,owner.getSkillLevel(EnumClassSkills.HUNTER));
        int cd = (int) (data.getOrDefault("cooldown",0F)*20);
        int duration = (int) (data.getOrDefault("duration",0F)*20);
        giveCd(3,cd);
        owner.sendMessage("&aYou casted ultimate skill &6" + EnumClassSkills.HUNTER.getName() +  "&a.");
        owner.addTrigger("HUNTER",duration);
        owner.removeCounter("HUNTER");
    }


    public static Map<String,Float> dataGetter(DPlayer player, EnumClassSkills skill) {
        return DatabaseDriver.getInstance().getSkillData(skill,player.getSkillLevel(skill));
    }

    private void cdSound() {
        owner.selfSound(Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
    }

    public void setCastItem(ItemReader itemReader) {
        castItem = itemReader;
    }

    public void setUltimateCooldown(int cd) {
        cdUltimate = Math.max(cdUltimate,cd);
    }

    public boolean ultimateReady() {
        return cdUltimate == 0;
    }
}
