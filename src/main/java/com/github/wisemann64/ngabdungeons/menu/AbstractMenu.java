package com.github.wisemann64.ngabdungeons.menu;

import com.github.wisemann64.ngabdungeons.players.DPlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMenu implements InventoryHolder {

    protected Inventory inventory;
    private final DPlayer owner;
    protected AbstractMenu parent;

    public AbstractMenu(DPlayer owner) {
        this.owner = owner;
        inventory = createInventory();
    }

    public AbstractMenu(DPlayer owner, AbstractMenu from) {
        this.owner = owner;
        inventory = createInventory();
        parent = from;
    }

    protected abstract Inventory createInventory();
    public abstract void onClick(InventoryClickEvent clickEvent);

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
    public void overrideInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public DPlayer getOwner() {
        return owner;
    }

    public void open() {
        getOwner().openMenu(this);
    }
    public void close() {
        getOwner().getHandle().closeInventory();
    }
    public void back() {
        if (parent != null) parent.open();
    }
}
