package com.github.wisemann64.ngabdungeons.skills;

import java.util.UUID;

public abstract class AbstractSkill {

    public abstract void tick();
    public abstract void remove();

    public abstract UUID getUniqueId();

}
