package com.github.sculkhorde.systems.cursor_system;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface ICursor {

    void moveTo(double x, double y, double z);
    void setToBeDeleted();
    boolean isSetToBeDeleted();

    BlockPos getBlockPosition();

    Level getLevel();

    void tick();

    UUID getUUID();

    default UUID createUUID()
    {
        return UUID.randomUUID();
    }

}
