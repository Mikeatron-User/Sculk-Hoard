package com.github.sculkhorde.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;

public interface ISpecialStructurePlacementConditionsBlock {

    void executeSpecialCondition(ServerLevelAccessor level, BlockPos pos);
}
