package com.github.wisemann64.ngabdungeons.combat;

public class Damage {

    private final double oldValue;
    private double newValue;
    private final boolean ignoreDefense;
    private final boolean crit;
    private final double penetration;
    private CombatEntity damager = null;

    public Damage(double value, boolean ignoreDefense, boolean crit, double penetration) {
        this.oldValue = value;
        this.ignoreDefense = ignoreDefense;
        this.crit = crit;
        this.penetration = penetration;
        newValue = oldValue;
    }

    public static Damage multiply(Damage from, double multiplier) {
        return new Damage(from.oldValue*multiplier,from.ignoreDefense,from.crit,from.penetration);
    }

    public double getOldValue() {
        return oldValue;
    }

    public boolean isCrit() {
        return crit;
    }

    public boolean isIgnoreDefense() {
        return ignoreDefense;
    }

    public double getNewValue() {
        return newValue;
    }

    public void setNewValue(double newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "{oldValue=" + oldValue + ", newValue=" + newValue + ", crit=" + crit + "}";
    }

    public CombatEntity getDamager() {
        return damager;
    }

    public void setDamager(CombatEntity damager) {
        this.damager = damager;
    }

    public double getPenetration() {
        return penetration;
    }
}
