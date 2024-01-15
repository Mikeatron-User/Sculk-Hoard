package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.infection.SculkNodeInfectionHandler;
import com.github.sculkhorde.common.structures.procedural.SculkNodeProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkNodeBlockEntity extends BlockEntity
{
    private long tickedAt = System.nanoTime();

    private SculkNodeProceduralStructure nodeProceduralStructure;

    //Repair routine will restart after an hour
    private final long repairIntervalInMinutes = 60;
    //Keep track of last time since repair, so we know when to restart
    private long lastTimeSinceRepair = -1;

    public static final int tickIntervalSeconds = 1;

    private SculkNodeInfectionHandler infectionHandler;

    public SculkNodeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.SCULK_NODE_BLOCK_ENTITY.get(), blockPos, blockState);

    }

    private final long heartBeatDelayMillis = TimeUnit.SECONDS.toMillis(10);
    private long lastHeartBeat = System.currentTimeMillis();
    private long lastPopulationUpdate = 0;
    private final long populationUpdateIntervalMillis = TickUnits.convertMinutesToTicks(3);
    public final int MAX_POPULATION = 64;
    Collection<ISculkSmartEntity> sculkEntitiesBelongingToThisNode = new ArrayList<>();

    /** Accessors **/

    public boolean isActive()
    {
        return this.getBlockState().getValue(SculkNodeBlock.ACTIVE);
    }

    public void setActive(boolean active)
    {
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(SculkNodeBlock.ACTIVE, active), 3);
    }

    public void updateListOfSculkEntitiesBelongingToThisNode()
    {
        assert level != null;
        if(level.getGameTime() - lastPopulationUpdate < populationUpdateIntervalMillis)
        {
            return;
        }

        lastPopulationUpdate = level.getGameTime();
        sculkEntitiesBelongingToThisNode.clear();
        ServerLevel serverLevel = (ServerLevel)level;
        Iterable<Entity> listOfEntities = serverLevel.getEntities().getAll();

        for(Entity entity : listOfEntities)
        {
            if(! (entity instanceof LivingEntity))
            {
                continue;
            }

            if(!EntityAlgorithms.isSculkLivingEntity.test((LivingEntity) entity))
            {
                continue;
            }

            if(entity instanceof SculkBeeHarvesterEntity)
            {
                continue;
            }

            BlockPos thisNodePosition = this.getBlockPos();
            BlockPos theClosestNodetoEntity = ((ISculkSmartEntity)entity).getClosestNodePosition();
            boolean isClosestNodeThisNode = theClosestNodetoEntity.equals(thisNodePosition);

            if(isClosestNodeThisNode && entity.isAlive())
            {
                sculkEntitiesBelongingToThisNode.add((ISculkSmartEntity) entity);
                if(SculkHorde.isDebugMode() && isPopulationAtMax()) { SculkHorde.LOGGER.info("Sculk Node has reached maximum population."); }
            }
        }
    }

    public boolean isPopulationAtMax()
    {
        tryCalculateSculkEntityPopulationForThisNode();
        // Population will be 200 / number of nodes

        return sculkEntitiesBelongingToThisNode.size() > (MAX_POPULATION);
    }

    public void tryCalculateSculkEntityPopulationForThisNode()
    {
        assert level != null;

        if(level.getGameTime() - lastPopulationUpdate < populationUpdateIntervalMillis)
        {
            return;
        }

        lastPopulationUpdate = level.getGameTime();
        updateListOfSculkEntitiesBelongingToThisNode();
    }

    /** Modifiers **/


    /** Events **/

    private static void addDarknessEffectToNearbyPlayers(Level level, BlockPos blockPos, int distance)
    {
        level.players().forEach((player) -> {
            if(player.blockPosition().closerThan(blockPos, distance) && !player.isCreative() && !player.isInvulnerable() && !player.isSpectator())
            {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, TickUnits.convertMinutesToTicks(1), 1));
            }
        });
    }

    private void initializeInfectionHandler()
    {
        if(infectionHandler == null)
        {
            infectionHandler = new SculkNodeInfectionHandler(this, getBlockPos());
            infectionHandler.spawnOnSurface = false;
        }
    }
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkNodeBlockEntity blockEntity)
    {
        if(level.isClientSide)
        {
            if(System.currentTimeMillis() - blockEntity.lastHeartBeat > blockEntity.heartBeatDelayMillis)
            {
                blockEntity.lastHeartBeat = System.currentTimeMillis();
                level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 5.0F, 1.0F, false);
            }
            return;
        }

        // Initialize the infection handler
        if(blockEntity.infectionHandler == null)
        {
            blockEntity.initializeInfectionHandler();
        }

        if(blockEntity.infectionHandler.canBeActivated() && blockEntity.isActive())
        {
            blockEntity.infectionHandler.activate();
        }

        if(!blockEntity.isActive())
        {
            blockEntity.infectionHandler.deactivate();
        }

        blockEntity.infectionHandler.tick();

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);

        // If the time elapsed is less than the tick interval, return
        if(timeElapsed < tickIntervalSeconds) { return; }

        // Update the tickedAt time
        blockEntity.tickedAt = System.nanoTime();

        addDarknessEffectToNearbyPlayers(level, blockPos, 50);

        /** Chunkloading **/

        if(blockEntity.isActive())
        {
            //TODO Uncomment
            //BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare((ServerLevel) level, blockPos, ModConfig.SERVER.sculk_node_chunkload_radius.get(), 1, TickUnits.convertMinutesToTicks(30));
        }
        else
        {
            //TODO Uncomment
            //BlockEntityChunkLoaderHelper.getChunkLoaderHelper().removeRequestsWithOwner(blockPos, (ServerLevel) level);
        }


        /** Building Shell Process **/
        long repairTimeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - blockEntity.lastTimeSinceRepair, TimeUnit.NANOSECONDS);

        //If the structure has not been initialized yet, do it
        if(blockEntity.nodeProceduralStructure == null)
        {
            //Create Structure
            blockEntity.nodeProceduralStructure = new SculkNodeProceduralStructure((ServerLevel) level, blockPos);
            blockEntity.nodeProceduralStructure.generatePlan();
        }

        //If currently building, call build tick.
        if(blockEntity.nodeProceduralStructure.isCurrentlyBuilding())
        {
            blockEntity.nodeProceduralStructure.buildTick();
            blockEntity.lastTimeSinceRepair = System.nanoTime();
        }
        //If enough time has passed, or we havent built yet, and we can build, start build
        else if((repairTimeElapsed >= blockEntity.repairIntervalInMinutes || blockEntity.lastTimeSinceRepair == -1) && blockEntity.nodeProceduralStructure.canStartToBuild())
        {
            blockEntity.nodeProceduralStructure.startBuildProcedure();
        }
    }
}
