package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.raid_system.RaidHandler;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

public interface ISculkSmartEntity {

    default boolean canParticipatingInRaid() {
        return RaidHandler.raidData.isRaidActive() && isParticipatingInRaid();
    }

    default ModSavedData.NodeEntry getClosestNode() {
        return SculkHorde.savedData.getClosestNodeEntry((ServerLevel) ((Mob) this).level(), ((Mob) this).blockPosition());
    }

    default BlockPos getClosestNodePosition() {
        return getClosestNode().getPosition();
    }

    SquadHandler getSquad();

    default Optional<SculkNodeBlockEntity> getClosestNodeBlockEntity() {
        return ((Mob)this).level().getBlockEntity(getClosestNodePosition(), ModBlockEntities.SCULK_NODE_BLOCK_ENTITY.get());
    }

    boolean isParticipatingInRaid();

    void setParticipatingInRaid(boolean isParticipatingInRaidIn);

    TargetParameters getTargetParameters();

    boolean isIdle();
}

