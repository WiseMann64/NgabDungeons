package com.github.wisemann64.ngabdungeons.mobs;

public class MobAttributes {

    public final AbstractDungeonMob owner;
    private long tickCreated = 0;

    private double maxHealth = 100d;
    private double health = 100d;
    private double attackPower = 0;
    private double defense = 0;
    private double penetration = 0;

    public MobAttributes(AbstractDungeonMob mob) {
        this.owner = mob;
    }


    public AbstractDungeonMob getOwner() {
        return owner;
    }

    public long getTickCreated() {
        return tickCreated;
    }

    protected void setTickCreated(long tickCreated) {
        this.tickCreated = tickCreated;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    protected void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getHealth() {
        return health;
    }

    protected void setHealth(double health) {
        this.health = health;
    }

    public double getAttackPower() {
        return attackPower;
    }

    protected void setAttackPower(double attackPower) {
        this.attackPower = attackPower;
    }

    public double getDefense() {
        return defense;
    }

    protected void setDefense(double defense) {
        this.defense = defense;
    }

    public double getPenetration() {
        return penetration;
    }

    public void setPenetration(double penetration) {
        this.penetration = penetration;
    }
}
