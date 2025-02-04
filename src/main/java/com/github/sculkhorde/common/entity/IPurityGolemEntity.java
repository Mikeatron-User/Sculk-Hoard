package com.github.sculkhorde.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface IPurityGolemEntity {

    BlockState getDepletedBoundBlockState();
    void convertBoundBlockToDepleted();
    boolean belongsToBoundBlock();
    boolean isBoundBlockPresent();

    Optional<BlockPos> getBoundBlockPos();

    void setBoundBlockPos(BlockPos pos);

    int getMaxDistanceFromBoundBlockBeforeDeath();

    int getMaxTravelDistanceFromBoundBlock();

}
