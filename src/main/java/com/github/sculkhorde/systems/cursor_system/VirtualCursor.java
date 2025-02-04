package com.github.sculkhorde.systems.cursor_system;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Predicate;

public class VirtualCursor implements ICursor{

    // Physical Properties ---------------------------------------------------------------------------------------------
    protected Level level;
    protected BlockPos pos;
    protected boolean toBeDeleted = false;

    UUID uuid = createUUID();

    // Transformation Properties ---------------------------------------------------------------------------------------
    protected int MAX_TRANSFORMATIONS = 100;
    protected int currentTransformations = 0;
    protected int MAX_RANGE = 20;
    protected long MAX_LIFETIME_TICKS = TickUnits.convertMinutesToTicks(5);
    protected long creationTickTime = 0;
    protected long lastTickTime = 0;

    // Tick Properties -------------------------------------------------------------------------------------------------
    public enum State
    {
        IDLE,
        SEARCHING,
        EXPLORING,
        FINISHED
    }

    protected State state = State.IDLE;

    public enum CursorType
    {
        INFESTOR,
        PURIFIER
    }

    protected CursorType cursorType = CursorType.INFESTOR;
    protected int searchIterationsPerTick = 20;
    protected long tickIntervalTicks = TickUnits.convertSecondsToTicks(1);

    // Search Properties -----------------------------------------------------------------------------------------------
    protected BlockPos origin = BlockPos.ZERO;
    protected BlockPos target = BlockPos.ZERO;
    protected HashMap<Long, Boolean> positionsSearched = new HashMap<>();
    Queue<BlockPos> searchQueue = new LinkedList<>();
    public boolean isSuccessful = false;

    protected HashMap<Long, Boolean> visitedPositions = new HashMap<>();

    // Particle Properties ---------------------------------------------------------------------------------------------
    protected int PARTICLE_SPAWN_COOLDOWN = TickUnits.convertSecondsToTicks(2);
    protected int ticksSinceLastParticleSpawn = PARTICLE_SPAWN_COOLDOWN;


    public VirtualCursor(Level level) {
        this.level = level;
        creationTickTime = level.getGameTime();
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public boolean isSuccessfullyFinished()
    {
        return state == State.FINISHED;
    }

    @Override
    public void setToBeDeleted() {
        toBeDeleted = true;
    }

    @Override
    public boolean isSetToBeDeleted() {
        return toBeDeleted;
    }

    @Override
    public void moveTo(double x, double y, double z) {
        this.pos = new BlockPos((int) x, (int) y, (int) z);
    }
    public Level getLevel()
    {
        return level;
    }

    public BlockPos getBlockPosition()
    {
        return pos;
    }

    public void setMaxTransformations(int MAX_INFECTIONS) {
        this.MAX_TRANSFORMATIONS = MAX_INFECTIONS;
    }

    public void setMaxRange(int MAX_RANGE) {
        this.MAX_RANGE = MAX_RANGE;
    }

    public void setMaxLifeTimeTicks(long ticks) {
        this.MAX_LIFETIME_TICKS = ticks;
    }

    public void setSearchIterationsPerTick(int iterations) {
        this.searchIterationsPerTick = iterations;
    }

    public void setTickIntervalTicks(long ticks) {
        this.tickIntervalTicks = ticks;
    }

    public void setState(State state)
    {
        this.state = state;
    }


    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(SculkHorde.savedData.getSculkAccumulatedMass() <= 0)
        {
            return false;
        }

        if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }
        else if(state.isAir())
        {
            return true;
        }
        // This is to prevent the entity from getting stuck in a loop
        else if(visitedPositions.containsKey(pos.asLong()))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the block is considered a target.
     * @param pos the block position
     * @return true if the block is considered a target
     */
    protected boolean isTarget(BlockPos pos)
    {
        return !getLevel().getBlockState(pos).is(Blocks.DIAMOND_BLOCK);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    protected void transformBlock(BlockPos pos)
    {
        getLevel().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
    }

    protected void spawnParticleEffects()
    {
        //this.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.1D, 0.0D);
    }

    protected void resetSearchTick()
    {
        searchQueue.clear();
        positionsSearched.clear();
    }

    protected void addPositionToQueueIfValid(BlockPos pos)
    {
        boolean isPositionNotVisited = !positionsSearched.containsKey(pos.asLong());
        BlockState neighborBlockState = getLevel().getBlockState(pos);
        boolean isPositionNotObstructed = !isObstructed(neighborBlockState, pos);

        // If not visited and is a valid block to navigate
        if (isPositionNotVisited && isPositionNotObstructed) {
            searchQueue.add(pos);
            positionsSearched.put(pos.asLong(), true);
        }
    }

    /**
     * Use Breadth-First Search to find the nearest infectable block within a certain maximum distance.
     * @return true if complete. false if not complete.
     */
    protected boolean searchTick() {
        // Initialize the visited positions map and the queue
        // Complete 20 times.
        for (int i = 0; i < Math.max(searchIterationsPerTick, 1); i++)
        {
            // Breadth-First Search

            if (searchQueue.isEmpty()) {
                isSuccessful = false;
                target = BlockPos.ZERO;
                return true;
            }

            BlockPos currentBlock = searchQueue.poll();

            // If the current block is a target, return it
            if (isTarget(currentBlock)) {
                isSuccessful = true;
                target = currentBlock;
                return true;
            }

            // Get all possible directions
            ArrayList<BlockPos> possibleBlocksToVisit = BlockAlgorithms.getNeighborsCube(currentBlock, false);
            Collections.shuffle(possibleBlocksToVisit);

            // Add all neighbors to the queue
            for (BlockPos neighbor : possibleBlocksToVisit) {
                addPositionToQueueIfValid(neighbor);
            }
        }

        return false;
    }

    public void exploreTick()
    {
        // Get Neighbors of Each Block
        ArrayList<BlockPos> neighbors = BlockAlgorithms.getNeighborsCube(this.getBlockPosition(), false);
        // Create a new list to store unobstructed neighbors
        ArrayList<BlockPos> unobstructedNeighbors = new ArrayList<>();
        // Check each neighbor for obstructions and add unobstructed neighbors to the new list
        for (BlockPos neighbor : neighbors)
        {
            if (!isObstructed(getLevel().getBlockState(neighbor), neighbor)) {
                unobstructedNeighbors.add(neighbor);
            }
        }

        // If there are no non-obstructed neighbors, return
        if (neighbors.size() == 0) {
            return;
        }

        // Find the block that is closest to target in neighbors
        BlockPos closest = neighbors.get(0);
        for (BlockPos pos : neighbors)
        {
            if (BlockAlgorithms.getBlockDistance(pos, target) < BlockAlgorithms.getBlockDistance(closest, target)) {
                closest = pos;
            }
        }


        // Move to the closest block
        this.moveTo(closest.getX(), closest.getY(), closest.getZ());

        // If we've reached the target block, find a new target
        if (getBlockPosition().equals(target))
        {
            target = BlockPos.ZERO;
            BlockState stateOfCurrentBlock = getLevel().getBlockState(getBlockPosition());

            boolean isTarget = isTarget(getBlockPosition());
            boolean isNotObstructed = !isObstructed(stateOfCurrentBlock, getBlockPosition());
            // If the block is not obstructed, infect it
            if(isTarget && isNotObstructed)
            {
                // Infect the block and increase the infection count
                transformBlock(getBlockPosition());
                currentTransformations++;
            }

            setState(State.SEARCHING);
            resetSearchTick();
            searchQueue.add(getBlockPosition());
        }

        // Mark position as visited
        visitedPositions.put(closest.asLong(), true);
    }
    private final Predicate<Entity> IS_DROPPED_ITEM = (entity) ->
    {
        return entity instanceof ItemEntity;
    };

    public void cursorTick()
    {
        float timeElapsedTicks = getLevel().getGameTime() - lastTickTime;
        double tickIntervalAfterMultiplier;

        if(cursorType == CursorType.INFESTOR)
        {
            tickIntervalAfterMultiplier = tickIntervalTicks / ModConfig.SERVER.infection_speed_multiplier.get();
        }
        else
        {
            tickIntervalAfterMultiplier = tickIntervalTicks / ModConfig.SERVER.purification_speed_multiplier.get();
        }

        if (timeElapsedTicks < Math.max(tickIntervalAfterMultiplier, 1)) {
            return;
        }

        lastTickTime = getLevel().getGameTime();

        // Keep track of the origin
        if (origin == BlockPos.ZERO)
        {
            origin = getBlockPosition();
        }

        if(getLevel().random.nextFloat() <= 0.1 && cursorType == CursorType.INFESTOR)
        {
            AABB boundingBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(getBlockPosition().getCenter(), 20);
            List<Entity> entities = EntityAlgorithms.getEntitiesInBoundingBox((ServerLevel) getLevel(), boundingBox, IS_DROPPED_ITEM);
            for(Entity entity : entities)
            {
                if(!ModConfig.SERVER.isItemEdibleToCursors((ItemEntity) entity))
                {
                    continue;
                }
                entity.discard();
                int massToAdd = ((ItemEntity)entity).getItem().getCount();
                SculkHorde.savedData.addSculkAccumulatedMass(massToAdd);
                SculkHorde.statisticsData.addTotalMassFromInfestedCursorItemEating(massToAdd);
            }
        }

        long currentLifeTimeTicks = getLevel().getGameTime() - creationTickTime;

        // Convert to seconds
        // If entity has lived too long, remove it
        if (currentLifeTimeTicks >= MAX_LIFETIME_TICKS)
        {
            setState(State.FINISHED);
        }
        else if (currentTransformations >= MAX_TRANSFORMATIONS)
        {
            setState(State.FINISHED);
        }

        if(state == State.IDLE)
        {
            searchQueue.add(getBlockPosition());
            setState(State.SEARCHING);
        }
        else if (state == State.SEARCHING)
        {

            // IF not complete, just return;
            if(!searchTick())
            {
                return;
            }

            // If we can't find a target, finish
            if (target.equals(BlockPos.ZERO)) {
                setState(state = State.FINISHED);
            }
            else // If we find target, start infecting
            {
                setState(state = State.EXPLORING);
                visitedPositions.clear();
            }
        }
        else if (state == State.EXPLORING)
        {
            exploreTick();
        }
        else if (state == State.FINISHED)
        {
            setToBeDeleted();
        }
    }

    @Override
    public void tick() {

        // Play Particles on Client
        if (getLevel() != null && getLevel().isClientSide)
        {
            ticksSinceLastParticleSpawn += 1;
            if(ticksSinceLastParticleSpawn >= PARTICLE_SPAWN_COOLDOWN)
            {
                spawnParticleEffects();
            }
            return;
        }

        cursorTick();

        /*
        if(canBeManuallyTicked())
        {
            ticksRemainingBeforeCheckingIfInCursorList--;

            if(ticksRemainingBeforeCheckingIfInCursorList <= 0)
            {
                SculkHorde.cursorSystem.computeIfAbsentVirtualCursor(this);
                ticksRemainingBeforeCheckingIfInCursorList = CHECK_DELAY_TICKS;
            }
        }
        boolean canBeManuallyTickedAndManualControlIsNotOn = (canBeManuallyTicked() && !SculkHorde.cursorSystem.isManualControlOfTickingEnabled());
        boolean cannotBeManuallyTicked = !canBeManuallyTicked();

        boolean shouldTick = canBeManuallyTickedAndManualControlIsNotOn || cannotBeManuallyTicked;

        if(shouldTick) {
            cursorTick();
        }

         */

    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }

    public void chanceToThanosSnapThisCursor()
    {
        if(getLevel().isClientSide()) { return; }

        if(SculkHorde.autoPerformanceSystem.isThanosSnappingCursors())
        {
            ServerLevel serverLevel = (ServerLevel) getLevel();
            MinecraftServer server = serverLevel.getServer();
            if(serverLevel.random.nextBoolean())
            {
                //server.tell(new net.minecraft.server.TickTask(getLevel().getServer().getTickCount() + 1, this::discard));
            }
        }
    }
}

