package com.github.sculkhorde.systems.cursor_system;

import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.PlayerProfileHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class VirtualOreMinerCursor extends VirtualCursor{

    protected Block blockToTarget;
    protected UUID owner;
    protected ItemStack pickaxe;

    public VirtualOreMinerCursor(Level level, Block blockToTarget, UUID owner, ItemStack pickaxe)
    {
        super(level);
        cursorType = CursorType.MISC;
        this.blockToTarget = blockToTarget;
        this.owner = owner;
        this.pickaxe = pickaxe;
    }

    protected Optional<Player> getOwner()
    {
        return PlayerProfileHandler.getOrCreatePlayerProfile(owner).getPlayer();
    }


    /**
     * Returns true if the block is considered a target.
     * @param pos the block position
     * @return true if the block is considered a target
     */
    @Override
    protected boolean isTarget(BlockPos pos)
    {
        return level.getBlockState(pos).is(blockToTarget);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    @Override
    protected void transformBlock(BlockPos pos)
    {
        if(getOwner().isEmpty())
        {
            setMaxTransformations(0);
            return;
        }


        LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)getLevel()))
                .withParameter(LootContextParams.ORIGIN, getBlockPosition().getCenter())
                .withParameter(LootContextParams.TOOL, pickaxe)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, getOwner().get());

        BlockState blockToBreak = level.getBlockState(pos);

        for(ItemStack itemstack1 : blockToBreak.getDrops(lootparams$builder)) {
            this.spawnDropAtLocation(itemstack1);
        }
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        BlockInfestationSystem.placeSculkVein((ServerLevel) getLevel(), pos);
    }

    @Nullable
    public ItemEntity spawnDropAtLocation(ItemStack itemStack) {
        if (itemStack.isEmpty())
        {
            return null;
        }

        Vec3 playerPos = getOwner().get().position();

        ItemEntity itementity = new ItemEntity(getLevel(), playerPos.x, playerPos.y + 0.5F, playerPos.z, itemStack);
        itementity.setDefaultPickUpDelay();
        getLevel().addFreshEntity(itementity);
        return itementity;

    }

    @Override
    public void moveTo(double x, double y, double z) {

        BlockInfestationSystem.placeSculkVein((ServerLevel) getLevel(), getBlockPosition());

        super.moveTo(x, y, z);
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(state.isAir())
        {
            return true;
        }
        // If we detect fluid
        else if(!state.getFluidState().isEmpty())
        {
            // If its water, its only obstructed if its the water source block or flowing water block
            if(state.getFluidState().is(Fluids.WATER) && state.is(Blocks.WATER))
            {
                return true;
            }

            if(!state.getFluidState().is(Fluids.WATER))
            {
                return true;
            }
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositions.containsKey(pos.asLong()))
        {
            return true;
        }

        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir((ServerLevel) getLevel(), pos);

        if(isBlockNotExposedToAir)
        {
            return true;
        }

        return false;
    }

    @Override
    protected void spawnParticleEffects()
    {
        Random random = new Random();
        float maxOffset = 2;

        float randomXOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomYOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomZOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        Vector3f spawnPos = new Vector3f(getBlockPosition().getX() + randomXOffset, getBlockPosition().getY() + randomYOffset, getBlockPosition().getZ() + randomZOffset);
        Vector3f velocity = new Vector3f(randomXOffset * 0.1F, randomYOffset * 0.1F, randomZOffset * 0.1F);
        ClientLevel clientLevel = (ClientLevel) getLevel();
        ParticleUtil.spawnBlockParticleOnClient(getLevel().getBlockState(getBlockPosition()), clientLevel, spawnPos, velocity);

    }

}

