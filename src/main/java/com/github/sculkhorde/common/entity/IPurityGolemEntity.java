package com.github.sculkhorde.common.entity;

import net.minecraft.core.BlockPos;

import java.util.Optional;

public interface IPurityGolemEntity<LivingEntity> {

    Optional<BlockPos> getBoundBlockPos();

    int getMaxDistanceFromBoundBlockBeforeDeath();

    int getMaxTravelDistanceFromBoundBlock();

}
