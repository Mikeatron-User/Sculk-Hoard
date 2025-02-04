package com.github.sculkhorde.systems.cursor_system;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface ICursor {
    void tick();
    UUID getUUID();
    default UUID createUUID()
    {
        return UUID.randomUUID();
    }
    boolean isSuccessfullyFinished();
    void setToBeDeleted();
    boolean isSetToBeDeleted();
    void moveTo(double x, double y, double z);
    Level getLevel();
    BlockPos getBlockPosition();
    void setMaxTransformations(int MAX_INFECTIONS);
    void setMaxRange(int MAX_RANGE);
    void setMaxLifeTimeTicks(long ticks);
    void setSearchIterationsPerTick(int iterations);
    void setTickIntervalTicks(long ticks);
    void setState(VirtualCursor.State state);


}
