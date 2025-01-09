package com.github.sculkhorde.common.entity;

import net.minecraft.core.BlockPos;

import java.util.Optional;

public interface IPurityGolemEntity {

    boolean belongsToBoundBlock();
    boolean isBoundBlockPresent();

    Optional<BlockPos> getBoundBlockPos();

    void setBoundBlockPos(BlockPos pos);

    int getMaxDistanceFromBoundBlockBeforeDeath();

    int getMaxTravelDistanceFromBoundBlock();

}
