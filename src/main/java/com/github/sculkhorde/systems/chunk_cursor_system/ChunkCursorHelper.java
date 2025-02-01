package com.github.sculkhorde.systems.chunk_cursor_system;

import com.github.sculkhorde.common.block.InfestationEntries.BlockInfestationTable;
import com.github.sculkhorde.common.blockentity.SculkBeeNestBlockEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.IPlantable;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ChunkCursorHelper {

    public static String getTimeSince(long start) {
        long time = System.currentTimeMillis() - start;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes);
        long milliseconds = (time - TimeUnit.SECONDS.toMillis(seconds)) - TimeUnit.MINUTES.toMillis(minutes);

        return String.format("%d min: %d sec: %d ms", minutes, seconds, milliseconds);
    }

    public static String textTime(long time) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes);
        long milliseconds = (time - TimeUnit.SECONDS.toMillis(seconds)) - TimeUnit.MINUTES.toMillis(minutes);

        return String.format("%d min: %d sec: %d ms", minutes, seconds, milliseconds);
    }

    public static boolean tryToCureBlock(ServerLevel world, BlockPos targetPos, Boolean noGrass)
    {
        boolean wasAbleToCureBlock = false;
        BlockState getNormalVariant = null;

        if(!BlockInfestationSystem.isCurable(world, targetPos)) { return false; }

        for(BlockInfestationTable table : BlockInfestationSystem.INFESTATION_TABLES)
        {
            getNormalVariant = table.getNormalVariant(world, targetPos);

            if(getNormalVariant == null) { continue; }

            wasAbleToCureBlock = true;

            break;
        }

        // If we did not successfully cure the block, return
        if(!wasAbleToCureBlock)
        {
            return false;
        }

        // Convert Block
        world.setBlockAndUpdate(targetPos, getNormalVariant);

        if(BlockInfestationSystem.shouldBeRemovedFromAboveBlock.test(world.getBlockState(targetPos.above())))
        {
            world.setBlockAndUpdate(targetPos.above(), Blocks.AIR.defaultBlockState());
        }

        if (!noGrass) {
            Random rand = new Random();
            boolean canCuredBlockSustainPlant = world.getBlockState(targetPos).canSustainPlant(world, targetPos, Direction.UP, (IPlantable) Blocks.POPPY);

            if(rand.nextBoolean() && canCuredBlockSustainPlant && world.getBlockState(targetPos.above()).isAir()) {
                world.setBlockAndUpdate(targetPos.above(), Blocks.GRASS.defaultBlockState());
            }
        }

        return true;
    }

    public static void tryToInfestBlock(ServerLevel world, BlockPos targetPos, Boolean noSpawners) {
        if(!ModConfig.SERVER.block_infestation_enabled.get()) {return;}

        BlockState victimBlockState = world.getBlockState(targetPos);
        boolean wasAbleToInfestBlock = false;

        if(BlockInfestationSystem.isExplicitlyNotInfectable(victimBlockState)) {return;}

        for(BlockInfestationTable table : BlockInfestationSystem.INFESTATION_TABLES) {
            if(table.canBeInfectedByThisTable(world, targetPos)) {
                wasAbleToInfestBlock = table.infectBlock(world, targetPos);
                break;
            }
        }

        // If we did not successfully infect the block, return
        if(!wasAbleToInfestBlock) {return;}

        if (r.nextInt(100) < 25) {
            world.sendParticles(ParticleTypes.SCULK_CHARGE_POP, targetPos.getX() + 0.5D, targetPos.getY() + 1.15D, targetPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
            world.playSound(null, targetPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
        }

        // world.sendParticles(ParticleTypes.SCULK_CHARGE_POP, targetPos.getX() + 0.5D, targetPos.getY() + 1.15D, targetPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        // world.playSound(null, targetPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);

        BlockInfestationSystem.removeNearbyVein(world, targetPos);
        BlockInfestationSystem.tryPlaceDiseasedKelp(world, targetPos.above());

        placeSculkFlora(world, targetPos.above(), noSpawners);
        tryPlaceSculkBeeHive(world, targetPos.above());
    }

    public static boolean canSpawnBeehive(ServerLevel world, BlockPos targetPos) {
        SculkHorde.LOGGER.info("System Test: ABOVE_TEST | Origin:" + targetPos + ".above().above() = " + targetPos.above().above() + " | .above(2) = " + targetPos.above(2));
        return (BlockInfestationSystem.blockIsAirOrSnow(world.getBlockState(targetPos)) &&
                BlockInfestationSystem.blockIsAirOrSnow(world.getBlockState(targetPos.above())) &&
                BlockInfestationSystem.blockIsAirOrSnow(world.getBlockState(targetPos.above().above()))
        );
    }

    /**
     * Will only place Sculk Bee Hives
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */

    private static final Random r = new Random();

    public static void tryPlaceSculkBeeHive(ServerLevel world, BlockPos targetPos) {
        //Given random chance and the target location can see the sky, create a sculk hive
        if(r.nextInt(50000) <= 1 && canSpawnBeehive(world, targetPos)) {
            SculkHorde.LOGGER.info("Spawning Beehive at: " + targetPos);
            world.setBlockAndUpdate(targetPos, ModBlocks.SCULK_BEE_NEST_BLOCK.get().defaultBlockState());
            SculkBeeNestBlockEntity nest = (SculkBeeNestBlockEntity) world.getBlockEntity(targetPos);

            //Add bees
            nest.addFreshInfectorOccupant();
            nest.addFreshInfectorOccupant();
            nest.addFreshHarvesterOccupant();
            nest.addFreshHarvesterOccupant();
        }

    }

    /**
     * A Jank solution to spawning flora. Given a random chance, spawn flora.
     * @param targetPos The BlockPos to spawn it at
     * @param world The world to spawn it in.
     */
    public static void placeSculkFlora(ServerLevel world, BlockPos targetPos, Boolean noSpawners)
    {
        if (world.random.nextInt(4) <= 0) {
            BlockState blockState = SculkHorde.randomSculkFlora.getRandomEntry().defaultBlockState();
            if (blockState.getBlock().equals(ModBlocks.SCULK_SUMMONER_BLOCK.get()) && noSpawners) return;

            BlockPos blockBelow = targetPos.below();
            BlockState belowBlockState = world.getBlockState(blockBelow);

            if (world.getBlockState(targetPos.below()).is(ModBlocks.DISEASED_KELP_BLOCK.get())) return;

            boolean canBlockBeWaterLogged = blockState.hasProperty(BlockStateProperties.WATERLOGGED);
            FluidState fluidStateAtTargetPos = world.getFluidState(targetPos);

            // If block is water loggable and in water and can survive, then place
            if (canBlockBeWaterLogged && fluidStateAtTargetPos.getType() == Fluids.WATER && blockState.canSurvive(world, targetPos)) {
                blockState.setValue(BlockStateProperties.WATERLOGGED, true); // This line doesn't work to set water logged
                world.setBlockAndUpdate(targetPos, blockState);
                world.setBlockAndUpdate(targetPos, world.getBlockState(targetPos).setValue(BlockStateProperties.WATERLOGGED, true)); // This one does though
            }

            //If block below target is valid and the target can be replaced by water and target is not waterloggable
            else if (belowBlockState.isFaceSturdy(world, blockBelow, Direction.UP) && blockState.canSurvive(world, targetPos) && (world.getBlockState(targetPos).isAir() || world.getBlockState(targetPos).is(Blocks.SNOW))) {
                world.setBlockAndUpdate(targetPos, blockState);
            }
        }

    }

}
