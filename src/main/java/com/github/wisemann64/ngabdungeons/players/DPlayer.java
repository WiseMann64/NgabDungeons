package com.github.wisemann64.ngabdungeons.players;

import com.github.wisemann64.ngabdungeons.NgabDungeons;
import com.github.wisemann64.ngabdungeons.PlayerManager;
import com.github.wisemann64.ngabdungeons.combat.CombatEntity;
import com.github.wisemann64.ngabdungeons.combat.Damage;
import com.github.wisemann64.ngabdungeons.items.EnumEquipmentSlot;
import com.github.wisemann64.ngabdungeons.items.ItemReader;
import com.github.wisemann64.ngabdungeons.menu.AbstractMenu;
import com.github.wisemann64.ngabdungeons.utils.Utils;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class DPlayer implements CombatEntity {

    private final Player player;
    private final UUID uuid;
    private final String name;
    private final JsonObject config;

    private PlayerLevel combat;
    private final EnumMap<EnumDungeonClass,PlayerLevel> classLevels = new EnumMap<>(EnumDungeonClass.class);
    private final PlayerAttributes attr;

    private boolean readyTick;

    private EnumDungeonClass selectedClass = EnumDungeonClass.TANK;
    private final EnumMap<EnumDungeonClass,SkillHolder> skills = new EnumMap<>(EnumDungeonClass.class);
    private final EnumMap<EnumClassSkills,Integer> skillsLevels = new EnumMap<>(EnumClassSkills.class);
    private final Map<String,Integer> activeTriggers = new HashMap<>();
    private final Map<String,Integer> additionalTriggers = new HashMap<>();
    private final Map<String,Integer> counter = new HashMap<>();

    private final SkillHandler skillHandler = new SkillHandler(this);


    public DPlayer(@NotNull Player p) {
        player = p;
        uuid = p.getUniqueId();
        name = p.getName();

        config = NgabDungeons.getPlayerData(p);
        prepareSkills();

        if (NgabDungeons.isNewPlayer(p)) createData();
        else readData();

        attr = new PlayerAttributes(this);

        PlayerManager.instance().addPlayer(this);
        readyTick = true;
    }

    private void newClassLevel() {
        for (EnumLevelType e : EnumLevelType.values()) {
            if (e == EnumLevelType.COMBAT) continue;
            classLevels.put(switch(e) {
                case TANK -> EnumDungeonClass.TANK;
                case SUPPORT -> EnumDungeonClass.SUPPORT;
                case ARCHER -> EnumDungeonClass.ARCHER;
                case FIGHTER -> EnumDungeonClass.FIGHTER;
                default -> null;
            },new PlayerLevel(this,e,null));
        }
    }

    private void prepareSkills() {
        for (EnumDungeonClass e: EnumDungeonClass.values()) skills.put(e,new SkillHolder(this,e));
        for (EnumClassSkills e: EnumClassSkills.values()) skillsLevels.put(e,0);
    }

    private void createData() {
        combat = new PlayerLevel(this,EnumLevelType.COMBAT,null);
        newClassLevel();
        newPlayerEvent();
    }

    private void readData() {
        combat = new PlayerLevel(this,EnumLevelType.COMBAT,config.getAsJsonObject("combat"));

        selectedClass = config.has("class") ? EnumDungeonClass.valueOf(config.get("class").getAsString()) : EnumDungeonClass.TANK;

        JsonObject classLevels = config.getAsJsonObject("class_levels");
        if (classLevels == null) newClassLevel();
        else classLevels.asMap().forEach((c,e) -> this.classLevels.put(EnumDungeonClass.valueOf(c),new PlayerLevel(this,EnumLevelType.valueOf(c),e.getAsJsonObject())));

        JsonObject skills = config.getAsJsonObject("skills");
        if (skills != null) for (EnumDungeonClass e: EnumDungeonClass.values()) {
            if (!skills.has(e.name())) continue;
            this.skills.get(e).ofJson(skills.get(e.name()));
        }

        JsonObject skillLevels = config.getAsJsonObject("skill_levels");
        if (skillLevels != null) skillLevels.entrySet().forEach(e -> skillsLevels.put(EnumClassSkills.valueOf(e.getKey()),e.getValue().getAsInt()));
    }

    private void saveData() {
        config.addProperty("uuid",getUniqueId().toString());
        config.addProperty("name",name);

        config.add("combat",combat.asJson());

        config.addProperty("class",selectedClass.name());

        JsonObject classLevels = new JsonObject();
        classLevels.add("TANK",this.classLevels.get(EnumDungeonClass.TANK).asJson());
        classLevels.add("FIGHTER",this.classLevels.get(EnumDungeonClass.FIGHTER).asJson());
        classLevels.add("ARCHER",this.classLevels.get(EnumDungeonClass.ARCHER).asJson());
        classLevels.add("SUPPORT",this.classLevels.get(EnumDungeonClass.SUPPORT).asJson());

        config.add("class_levels",classLevels);

        JsonObject skills = new JsonObject();
        for (EnumDungeonClass e: EnumDungeonClass.values()) skills.add(e.name(),this.skills.get(e).asJson());
        config.add("skills",skills);

        JsonObject levels = new JsonObject();
        skillsLevels.forEach((e,i) -> levels.addProperty(e.name(),i));
        config.add("skill_levels",levels);
    }

    private void newPlayerEvent() {
        player.sendMessage(Utils.c("&4Anak Anjing Baru cuy !!!"));
    }

    public Player getHandle() {
        return player;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PlayerAttributes getAttributes() {
        return attr;
    }

    public void tick() {
        if (!readyTick) return;
        attr.tick();
        actionBarMessage();
        syncHealth();
        cooldownTick();
        regenTick();
        xpTick();
        skillHandler.tick();
        triggerTick();
    }

    private void syncHealth() {
        if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
        player.setHealth(Math.max(1,Math.floor(getHealthFraction()*40)));
    }

    private int attackCooldown = 0;

    private void cooldownTick() {
        player.setMaximumNoDamageTicks(0);
        player.setNoDamageTicks(0);
        for (EntityDamageEvent.DamageCause v : envDamage.keySet()) {
            int cd = envDamage.get(v)-1;
            if (cd == 0) envDamage.remove(v);
            else envDamage.put(v,cd);
        }
        attackCooldown = attackCooldown > 0 ? attackCooldown-1 : 0;
    }

    private int regenCooldown = 0;
    private void regenTick() {
        if (getHealthFraction() < 1) {
            if (regenCooldown == 100) heal(attr.getRegen());
            regenCooldown = regenCooldown >= 100 ? 0 : regenCooldown+1;
        } else regenCooldown = 0;
    }

    private void xpTick() {
        player.setLevel(getLevel());
        player.setExp(getLevel() == 60 ? 1.0f : combat.getProgress());
    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public int getAttackRecover() {
        return attr.getAttackDelay();
    }

    public void setAttackCooldown() {
        this.attackCooldown = getAttackRecover();
    }

    private int messageTick = 0;
    private String varMessage = null;

    private void actionBarMessage() {
        if (messageTick == 0) varMessage = null;
        int a = (int) getDefense();
        String strDef = a > 0 ? "&a   "+a+" &2[&a♠&2]" : "";
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.c("&c"+(int)attr.getHealth()+"/"+(int)attr.getMaxHealth()+
                " &4[&c❤&4]" + (varMessage == null ? "" : "   ") +variableActionBarMessage() + strDef)));
        if (messageTick > 0) messageTick--;
    }

    private String variableActionBarMessage() {
        if (messageTick > 0 && varMessage != null) return varMessage;
        return "";
    }

    private void sendActionbarMessage(String msg, int duration) {
        varMessage = msg;
        messageTick = duration;
    }

    public void remove() {
        readyTick = false;
        saveData();
        NgabDungeons.savePlayerData(this);
        NgabDungeons.getPlayerManager().removePlayer(this);
    }


    public JsonObject getConfig() {
        return config;
    }

    private final EnumMap<EntityDamageEvent.DamageCause, Integer> envDamage = new EnumMap<>(EntityDamageEvent.DamageCause.class);
    @Override
    public EnumMap<EntityDamageEvent.DamageCause, Integer> getEnvDamageCooldown() {
        return envDamage;
    }

    public void sendMessage(Object obj) {
        player.sendMessage(Utils.c(String.valueOf(obj)));
    }

    public void selfSound(Sound sound, float i, float v) {
        player.playSound(player.getLocation(),sound,i,v);
    }

    public void worldSound(Sound sound, float i, float v) {
        player.getWorld().playSound(player.getLocation(),sound,i,v);
    }

    public PlayerLevel getCombat() {
        return combat;
    }

    public void giveItem(ItemStack item) {
        player.getInventory().addItem(item).values().forEach(i -> player.getWorld().dropItemNaturally(getLocation(),i,it -> {
            it.setOwner(getUniqueId());
            it.setPickupDelay(10);
        }));
    }

    private void die() {
//        sendMessage("&4DIE!");
        System.out.println(getMaxHealth());
        setHealth(getMaxHealth());
    }

//    COMBAT ENTITY SECTION
    @Override
    public boolean isInvis() {
        return false;
    }

    @Override
    public double getHealth() {
        return attr.getHealth();
    }

    @Override
    public double getMaxHealth() {
        return attr.getMaxHealth();
    }

    @Override
    public float getHealthFraction() {
        return Math.min((float) (getHealth()/getMaxHealth()),1.0F);
    }

    @Override
    public double getDefense() {
        return Math.max(-500,attr.getDefense());
    }

    public int getLevel() {
        return combat.getLevel();
    }

    @Override
    public Location getLocation() {
        return player.getLocation();
    }

    public void teleport(Location loc) {
        player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public World getWorld() {
        return player.getWorld();
    }

    @Override
    public Location getEyeLocation() {
        return player.getEyeLocation();
    }

    @Override
    public void setHealth(double amount) {
        if (amount <= 0) die();
        else attr.setHealth(Math.min(amount,getMaxHealth()));
    }

    @Override
    public void heal(double amount) {
        setHealth(getHealth() + amount);
    }

    @Override
    public double dealDamage(double finalDamage, CombatEntity damager) {
        setHealth(getHealth()-finalDamage);
        return finalDamage;
    }

    @Override
    public double getAttackPower() {
        ItemReader main = getMainHand();
        return 5 + (main == null ? 0 : main.getDamage());
    }

    @Override
    public double getRangedAttackPower() {
        ItemReader main = getMainHand();
        return 5 + (main == null ? 0 : main.getRangedDamage());
    }

    public Damage basicAttack() {
        boolean crit = drawCrit();
        return basicAttackFixedCrit(crit);
    }
    public Damage basicAttackFixedCrit(boolean crit) {
        double cd = crit ? 1 + attr.getCritDamage()/100 : 1;
        double dmg = getAttackPower() * (1 + 0.01*attr.getStrength())*cd*attr.getMeleeDamageMultiplier()*attr.getDamageMultiplier();
        return new Damage(dmg, false,crit,attr.getPenetration());
    }
    public Damage simulateBasicAttackFixedCrit(ItemReader item, boolean crit) {
        double cd = crit ? 1 + attr.getCritDamage()/100 : 1;
        double dmg = (5 + (item == null ? 0 : item.getDamage())) * (1 + 0.01*attr.getStrength())*cd*attr.getMeleeDamageMultiplier()*attr.getDamageMultiplier();
        return new Damage(dmg, false,crit,attr.getPenetration());
    }
    public Damage arrowAttack(boolean arrowCrit) {
        boolean crit = drawCrit() && arrowCrit;
        double cd = crit ? 1 + attr.getCritDamage()/100 : 1;
        double dmg = getRangedAttackPower() * (1 + 0.01*attr.getStrength())*cd*attr.getArrowDamageMultiplier()*attr.getDamageMultiplier();
        return new Damage(dmg, false,crit,attr.getPenetration());
    }

    public Damage simulateArrowAttackFixedCrit(ItemReader item, boolean crit) {
        double cd = crit ? 1 + attr.getCritDamage()/100 : 1;
        double dmg = (5 + (item == null ? 0 : item.getRangedDamage())) * (1 + 0.01*attr.getStrength())*cd*attr.getArrowDamageMultiplier()*attr.getDamageMultiplier();
        return new Damage(dmg, false,crit,attr.getPenetration());
    }

    public boolean drawCrit() {
        Random r = new Random();
        return r.nextDouble() < attr.getCritChance()/100D;
    }

    public ItemReader getMainHand() {
        return ItemReader.ofItemStack(player.getInventory().getItemInMainHand(), EnumEquipmentSlot.MAINHAND);
    }

    public ItemReader getOffHand() {
        return ItemReader.ofItemStack(player.getInventory().getItemInOffHand(),EnumEquipmentSlot.OFFHAND);
    }

    public ItemReader getHelmet() {
        return ItemReader.ofItemStack(player.getInventory().getHelmet(),EnumEquipmentSlot.HELMET);
    }

    public ItemReader getChestplate() {
        return ItemReader.ofItemStack(player.getInventory().getChestplate(),EnumEquipmentSlot.CHESTPLATE);
    }

    public ItemReader getLeggings() {
        return ItemReader.ofItemStack(player.getInventory().getLeggings(),EnumEquipmentSlot.LEGGINGS);
    }

    public ItemReader getBoots() {
        return ItemReader.ofItemStack(player.getInventory().getBoots(),EnumEquipmentSlot.BOOTS);
    }

    public Map<EnumEquipmentSlot, ItemReader> getEquipmentSet() {
        EnumMap<EnumEquipmentSlot,ItemReader> inv = new EnumMap<>(EnumEquipmentSlot.class);
        BiConsumer<EnumEquipmentSlot,ItemReader> add = (e, i) -> {
            if (i != null) inv.put(e,i);
        };
        add.accept(EnumEquipmentSlot.HELMET,getHelmet());
        add.accept(EnumEquipmentSlot.CHESTPLATE,getChestplate());
        add.accept(EnumEquipmentSlot.LEGGINGS,getLeggings());
        add.accept(EnumEquipmentSlot.BOOTS,getBoots());
        add.accept(EnumEquipmentSlot.MAINHAND,getMainHand());
        add.accept(EnumEquipmentSlot.OFFHAND,getOffHand());
        return inv;
    }

    public void updateClassStats(EnumDungeonClass levelup) {
        if (levelup == null || levelup != selectedClass) return;
        attr.setClassStats(classLevels.get(levelup).getLevel(),levelup);
    }

    public EnumDungeonClass getSelectedClass() {
        return selectedClass;
    }

    public PlayerLevel getClassLevel(EnumDungeonClass cls) {
        return classLevels.get(cls);
    }

    public int getSelectedClassLevel() {
        return classLevels.get(selectedClass).getLevel();
    }

    public void addClassXp(EnumDungeonClass c, double xp) {
        classLevels.get(c).addXp((float) xp);
    }

    public void switchClass(EnumDungeonClass cls) {
        if (selectedClass == cls) return;
        selectedClass = cls;
        attr.setClassStats(classLevels.get(cls).getLevel(),cls);
    }


    public void openMenu(AbstractMenu menu) {
        player.closeInventory();
        player.openInventory(menu.getInventory());
    }

    public void castPrimary() {
        skillHandler.castPrimary();
    }

    public void castSecondary() {
        skillHandler.castSecondary();
    }

    public void castUltimate() {
        skillHandler.castUltimate();
    }

    public EnumClassSkills getPrimarySkill() {
        return skills.get(selectedClass).getPrimary();
    }

    public EnumClassSkills getSecondarySkill() {
        return skills.get(selectedClass).getSecondary();
    }

    public EnumClassSkills getUltimateSkill() {
        return skills.get(selectedClass).getUltimate();
    }

    public int getSkillLevel(EnumClassSkills skill) {
        return skillsLevels.getOrDefault(skill,0);
    }

    public List<EnumClassSkills> getPassiveSkills() {
        SkillHolder sh = skills.get(getSelectedClass());
        List<EnumClassSkills> ret = new ArrayList<>();
        if (sh.getPrimary() != null && !sh.getPrimary().isActive()) ret.add(sh.getPrimary());
        if (sh.getSecondary() != null && !sh.getSecondary().isActive()) ret.add(sh.getSecondary());
        if (sh.getUltimate() != null && !sh.getUltimate().isActive()) ret.add(sh.getUltimate());
        return ret;
    }

    private void triggerTick() {
        Set<String> trigs = new HashSet<>(activeTriggers.keySet());
        trigs.forEach(t -> {
            int i = activeTriggers.getOrDefault(t,0);
            if (i == 0) {
                activeTriggers.remove(t);
                additionalTriggers.remove(t);
            }
            else activeTriggers.put(t,i-1);
        });
    }

    public void addTrigger(String trigger, int duration) {
        activeTriggers.put(trigger,duration);
    }

    public void addAdditionalTrigger(String trigger, int value) {
        additionalTriggers.put(trigger,value);
    }

    public boolean hasTrigger(String trigger) {
        return activeTriggers.containsKey(trigger);
    }
    public int getAdditionalTrigger(String trigger) {
        return additionalTriggers.getOrDefault(trigger,0);
    }

    public void removeTrigger(String trigger) {
        activeTriggers.remove(trigger);
        additionalTriggers.remove(trigger);
    }

    public int getCounter(String key) {
        return counter.getOrDefault(key,0);
    }

    public void setCounter(String key, int value) {
        counter.put(key,value);
    }

    public void removeCounter(String key) {
        counter.remove(key);
    }

    public SkillHandler getSkillHandler() {
        return skillHandler;
    }

    public boolean ultimateReady() {
        return skillHandler.ultimateReady();
    }
}
