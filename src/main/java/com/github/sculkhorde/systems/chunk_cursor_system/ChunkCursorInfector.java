package com.github.sculkhorde.systems.chunk_cursor_system;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ChunkCursorInfector extends ChunkCursorBase<ChunkCursorInfector> {

    public ChunkCursorInfector () {
        super();
    }

    public static ChunkCursorInfector of() {
        return new ChunkCursorInfector();
    }

    // Initialisations -------------------------------------------------------------------------------------------------
    @Override
    protected void initDefaults() {
        super.initDefaults();
        this.blocksPerTick(256)
                .doNotPlaceFeatures()
                .spawnSurfaceCursorsAtEnd()
                .enableAdjacentBlocks();

        this.fullDebug.enabled = false;
    }

    // Check Blocks ----------------------------------------------------------------------------------------------------
    @Override
    protected boolean isObstructed(ServerLevel serverLevel, BlockPos pos) {
        return !BlockAlgorithms.isExposedToAir(serverLevel, pos) || BlockAlgorithms.isExposedToInfestationWardBlock(serverLevel, pos);
    }

    @Override
    protected boolean canChange(ServerLevel serverLevel, BlockPos pos) {
        return BlockInfestationSystem.isInfectable(serverLevel, pos);
    }

    @Override
    protected boolean canConsume(ServerLevel serverLevel, BlockPos pos) {
        return serverLevel.getBlockState(pos).is(BlockTags.LEAVES);
    }


    // Change Blocks ---------------------------------------------------------------------------------------------------
    @Override
    protected void changeBlock(ServerLevel serverLevel, BlockPos pos) {
        ChunkCursorHelper.tryToInfestBlock(serverLevel, pos, !shouldPlaceFeatures());
    }

    @Override
    protected void entityCheck(ServerLevel serverLevel, Entity entity) {
        super.entityCheck(serverLevel, entity);

        if (entity instanceof Player player) {
            int y1 = ChunkCursorHelper.pokeHeightMap(getServerLevel(), getPos1()).getY();
            int y2 = ChunkCursorHelper.pokeHeightMap(getServerLevel(), getPos2()).getY();

            int y = Math.min(y1, y2);

            y = y - 64;
            y = Math.max(y, 0);
            y = Math.min(y, 319);

            MobEffectInstance darkness = new MobEffectInstance(MobEffects.DARKNESS, TickUnits.convertSecondsToTicks(10), y, false, false);
            player.addEffect(darkness);

            MobEffectInstance fog = new MobEffectInstance(ModMobEffects.SCULK_FOG.get(), TickUnits.convertSecondsToTicks(10), y, false, false);
            player.addEffect(fog);
        }
    }

    @Override
    protected void consumeItem(ItemEntity item) {
        int massToAdd = item.getItem().getCount();
        SculkHorde.savedData.addSculkAccumulatedMass(massToAdd);
        SculkHorde.statisticsData.addTotalMassFromInfestedCursorItemEating(massToAdd);

        super.consumeItem(item);
    }

}
