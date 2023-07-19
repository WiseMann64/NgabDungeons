package com.github.wisemann64.ngabdungeons.players;

import com.github.wisemann64.ngabdungeons.data.DatabaseDriver;
import com.github.wisemann64.ngabdungeons.items.ItemReader;
import com.github.wisemann64.ngabdungeons.skills.SkillBoneBreaker;
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

    public static Map<String,Float> dataGetter(DPlayer player, EnumClassSkills skill) {
        return DatabaseDriver.getInstance().getSkillData(skill,player.getSkillLevel(skill));
    }

    private void cdSound() {
        owner.selfSound(Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
    }

    public void setCastItem(ItemReader itemReader) {
        castItem = itemReader;
    }
}
