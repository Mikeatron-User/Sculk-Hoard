
package com.github.sculkhorde.systems.chunk_cursor_system;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChunkCursorBase<T extends ChunkCursorBase<T>> {

    protected final Log debug = new Log(this.toString().replaceAll("com.github.sculkhorde.systems.chunk_cursor_system.", ""));
    protected final Log fullDebug = new Log(this.toString().replaceAll("com.github.sculkhorde.systems.chunk_cursor_system.", ""));
    //private TaskHandler Tasks;

    private ServerLevel serverLevel;
    private Level level;
    private AABB boundingBox;
    private int lowestY;

    // Position
    private BlockPos center;
    private BlockPos pos1;
    private BlockPos pos2;

    // Settings
    private int blocksPerTick;                  // Speed essentially. Eventually this will get updated on each batch by checking the performance controller
    private int fadeDistance;                   // Anything within x blocks in from the edge of the chunk will have a chance to not be added
    private int maxAdjacentBlocks;              // When set to -1 the maximum will be set based on area size. Disables when set to 0
    private boolean caveMode;                   // Will find the highest block above center.y() instead of pos at 320              (fillMode must be false)
    private boolean fillMode;                   // Will only return blocks within the defined area that are not obstructed         (caveMode must be false)
    private boolean solidFill;                  // Will get all blocks within defined area, even if they're considered obstructed
    private boolean disableObstruction;         // Will ignore the isObstructed functions output
    private boolean doNotPlaceFeatures;         // Disables Spawners for Infectors, Grass for Purifiers
    private boolean spawnSurfaceCursorsAtEnd;   // Will spawn surface cursors of the appropriate type
    private Runnable executeOnStart;
    private Runnable executeOnPause;
    private Runnable executeOnEnd;

    private int maxTicks;
    private int maxTicksWarning;
    private boolean warningTriggered = false;

    private boolean shouldTick = true;
    private boolean isFinished = false;


    // Utilised during runtime
    private final ArrayList<BlockPos> topBlocks = new ArrayList<>();
    private final ArrayList<BlockPos> checkedBlocks = new ArrayList<>();

    private BlockPos finalTopBlock;
    private boolean primaryComplete = false;
    private int batchesSinceLastChange = 0;

    private int totalTicks = 0;
    private long startTime = 0;
    private long totalTime = 0;
    private long functionStartTime = 0;
    private int currentBatch = 0;
    private int currentBlock = 0;
    private int maxExtraBlocks = 0;
    private int xOffset = 0;
    private int zOffset = 0;
    private int areaLowestY;
    private boolean initResume = false;

    // Status
    private boolean active = false;         // Running / Should be running
    private State status = State.OFFLINE;   // Determines what functions to run

    protected enum State {
        OFFLINE,        // Hasn't been run since completion or reset
        READY,          // Ready to run
        INIT,           // Get Top Block Positions
        CAVER,          // Tries to get the top blocks within a cave (CaveMode only)
        SEARCH_CHANGE   // Search and Change at the same time, doesn't work well with obstruction disabled
    }

    public ChunkCursorBase() {initDefaults();}

    public boolean isFinished()
    {
        return isFinished;
    }

    // Control Functions -----------------------------------------------------------------------------------------------
    public void tick() {
        if (!shouldTick) return;

        if (totalTime == 0 && startTime == 0) {
            startTime = System.currentTimeMillis();

            debug.info("------ Settings Dump ------");
            debug.info("    Server Level: " + serverLevel);
            debug.info("    Center: " + center);
            debug.info("    POS_1: " + pos1);
            debug.info("    POS_2: " + pos2);
            debug.info("    maxTicks: " + maxTicks);
            debug.info("    blocksPerTick: " + blocksPerTick);
            debug.info("    fadeDistance: " + fadeDistance);
            debug.info("    caveMode: " + caveMode);
            debug.info("    fillMode: " + fillMode);
            debug.info("    solidFill: " + solidFill);
            debug.info("    disableObstruction: " + disableObstruction);
            debug.info("    doNotPlaceFeatures: " + doNotPlaceFeatures);

            if (executeOnStart != null) {
                debug.info("Executing the following command: " + executeOnStart);
                executeOnStart.run();
            }
        }

        active = true;
        run();
    }

    public void reset() {
        if (!active) {
            clear();
            isFinished = false;
            shouldTick = true;
        }
    }

    public void resume() {
        shouldTick = true;
    }

    public void pause() {
        totalTime = totalTime + (System.currentTimeMillis() - startTime);

        debug.info("Task: " + status + " Paused! | Time Taken So Far: " + ChunkCursorHelper.getTimeSince(functionStartTime));
        debug.info("Total Time Taken So Far: " + ChunkCursorHelper.textTime(totalTime) + " | Ticks: " + totalTicks);

        active = false;
        functionStartTime = 0;
        startTime = 0;

        shouldTick = false;

        if (executeOnPause != null) executeOnPause.run();
    }

    public void stop() {
        debug.info("Exiting Task...");
        finish();
    }


    /**
     * Clears all saved block positions and sets the status to: OFFLINE
     */
    public void clear() {
        if (!active) {
            resetTrackers();
            status = State.READY;
            topBlocks.clear();
            checkedBlocks.clear();

            warningTriggered = false;
            totalTicks = 0;

            startTime = 0;
            totalTime = 0;
            functionStartTime = 0;
            areaLowestY = 0;

            primaryComplete = false;
        }
    }

    /**
     * Resets center, pos1, pos2 & boundingBox. It will also run clear(): Resets status to: OFFLINE & clears all saved blocks and trackers
     */
    public void resetLocation() {
        if (!active) {
            status = State.OFFLINE;
            clear();

            center = null;
            pos1 = null;
            pos2 = null;
            boundingBox = null;
        }
    }


    /**
     * Resets serverLevel, level & lowestY. It will also run resetLocation(): Resets center, pos1, pos2 & boundingBox. It will also run clear(): Resets status to: OFFLINE & clears all saved blocks and trackers
     */
    public void resetLevel() {
        if (!active) {
            resetLocation();
            serverLevel = null;
            level = null;
            lowestY = 0;
        }
    }

    /**
     * Resets all settings back to their defaults specified in the initDefaults(); function
     */
    public void resetSettings() {
        if (!active) {
            initDefaults();
        }
    }

    /**
     * Clears the runnables executeOnStart, executeOnPause & executeOnEnd
     */
    public void resetRunnables() {
        if (!active) {
            executeOnStart = null;
            executeOnPause = null;
            executeOnEnd = null;
        }
    }


    /**
     * Resets everything back to "factory defaults"
     */
    public void resetAll() {
        if (!active) {
            resetLevel();
            resetSettings();
            resetRunnables();
        }
    }

    // Check Blocks ----------------------------------------------------------------------------------------------------
    protected boolean shouldRunAdjacent(ServerLevel serverLevel, BlockPos posY) {
        return (
                !disableObstruction &&
                !fillMode &&
                !solidFill &&
                totalExtraBlocks < maxExtraBlocks &&
                !isSolid(serverLevel, posY) &&
                isSolid(serverLevel, posY.above())
        );
    }

    protected boolean isValidAdjacent(ServerLevel serverLevel, BlockPos pos) {
        return (
                boundingBox.contains(pos.getCenter()) &&
                !isObstructed(serverLevel, pos) &&
                canChange(serverLevel, pos) &&
                !checkedBlocks.contains(pos)
        );
    }

    protected boolean isSolid(ServerLevel serverLevel, BlockPos pos) {
        return BlockAlgorithms.isSolid(serverLevel, pos);
    }

    protected boolean isAir(ServerLevel serverLevel, BlockPos pos) {
        return serverLevel.getBlockState(pos).isAir();
    }

    protected boolean isObstructed(ServerLevel serverLevel, BlockPos pos) {
        return BlockAlgorithms.isExposedToAir(serverLevel, pos);
    }

    protected boolean canChange(ServerLevel serverLevel, BlockPos pos) {
        return false;
    }

    protected boolean canConsume(ServerLevel serverLevel, BlockPos pos) {
        return false;
    }


    // Change Blocks ---------------------------------------------------------------------------------------------------
    protected void changeBlock(ServerLevel serverLevel, BlockPos pos) {}
    protected void consumeBlock(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }


    // AABB Interactions -----------------------------------------------------------------------------------------------
    protected void periodicEntityCheck(ServerLevel serverLevel, AABB boundingBox) {
        debug.info("Periodic Entity Check");
        consumeItems(serverLevel, boundingBox);
    }

    protected void firstEntityCheck(ServerLevel serverLevel, AABB boundingBox) {
        debug.info("First Entity Check");
        consumeItems(serverLevel, boundingBox);
    }

    protected void finalEntityCheck(ServerLevel serverLevel, AABB boundingBox) {
        debug.info("Final Entity Check");
        consumeItems(serverLevel, boundingBox);
    }

    protected void consumeItems(ServerLevel serverLevel, AABB boundingBox) {
        List<Entity> entities = serverLevel.getEntities(null, boundingBox);

        for (Entity entity : entities) {
            if (entity instanceof ItemEntity item) {
                if (ModConfig.SERVER.isItemEdibleToCursors(item)) {
                    item.discard();
                }
                else if (ComposterBlock.COMPOSTABLES.containsKey(item.getItem().getItem())) {
                    item.discard();
                }
            }
        }
    }


    // Initialisations -------------------------------------------------------------------------------------------------
    protected void initDefaults() {
        this.fadeDistance(0)
                .maxTicks(360000) // 5 Minutes | Set to -1 to disable
                .caveMode(false)
                .fillMode(false)
                .solidFill(false)
                .disableObstruction(false)
                .doNotPlaceFeatures(false)
                .spawnSurfaceCursorsAtEnd(false);
    }

    protected void initAABB(BlockPos pos1, BlockPos pos2) {
        boundingBox = new AABB(pos1, pos2);
    }


    // Main Functions --------------------------------------------------------------------------------------------------
    // OFFLINE | READY | INIT | CAVER | SEARCH | CHANGE | SEARCH_CHANGE
    protected void run() {
        if (isFinished) return;

        totalTicks++;

        if (maxTicks > 0) {
            if (!warningTriggered && totalTicks >= maxTicksWarning) {
                debug.error("Potential Runaway Cursor Detected! totalTicks has used 50% of its maxTicks [" + totalTicks + " ticks | " + maxTicks + " ticks] - Continuing...");
                warningTriggered = true;
            }
            if (totalTicks >= maxTicks) {
                debug.error("Runaway Cursor Detected! totalTicks has exceeded maxTicks [" + totalTicks + " ticks >= " + maxTicks + " ticks] - Stopping...");
                finish();
                return;
            }
        }

        if (active) {
            if (functionStartTime == 0) functionStartTime = System.currentTimeMillis();
            if (status.equals(State.READY)) status = State.INIT;

            switch (status) {
                case INIT ->   {init();}
                case CAVER -> {caver();}
                case SEARCH_CHANGE -> {searchAndChange();}
            }
        }
    }

    protected void finish() {
        if (executeOnEnd != null) executeOnEnd.run();

        totalTime = totalTime + (System.currentTimeMillis() - startTime);
        debug.info("Complete! | Total Time Taken: " + ChunkCursorHelper.textTime(totalTime) +  " | Ticks: " + totalTicks);

        active = false;
        shouldTick = false;
        isFinished = true;
        clear();
    }

    protected void resetTrackers() {
        functionStartTime = 0;
        currentBatch = 0;
        currentBlock = 0;

        initResume = false;
        xOffset = 0;
        zOffset = 0;
    }

    protected void completeTask() {
        resetTrackers();
        active = false;

        if (isFinished) return;

        debug.info("Task: " + status + " Complete! | Time Taken: " + ChunkCursorHelper.getTimeSince(functionStartTime));

        switch (status) {
            case INIT -> {
                int times = caveMode ? 8 : 1;
                maxExtraBlocks = (maxAdjacentBlocks < 0) ? topBlocks.size() * times : maxAdjacentBlocks;
                debug.info("Max Extra Blocks: " + maxExtraBlocks);

                if (caveMode) {
                    status = State.CAVER;
                }
                else {
                    status = State.SEARCH_CHANGE;
                    firstEntityCheck(serverLevel, boundingBox);
                    finalTopBlock = topBlocks.get(topBlocks.size()-1);
                }
            }
            case CAVER -> {
                status = State.SEARCH_CHANGE;
                firstEntityCheck(serverLevel, boundingBox);
                finalTopBlock = topBlocks.get(topBlocks.size()-1);
            }
            case SEARCH_CHANGE -> {
                finalEntityCheck(serverLevel, boundingBox);
                finish();
            }
        }
    }

    protected void caver() {
        if (!caveMode) {
            completeTask();
        }

        for (int i = 0; i < blocksPerTick; i++) {

            // Check if we have finished
            if (currentBlock >= topBlocks.size()) {
                break;
            }
            BlockPos block = topBlocks.get(currentBlock);
            BlockPos highest = null;

            // Find the highest block and set it to the topBlocks
            for (int y = block.getY(); y < serverLevel.getMaxBuildHeight(); y++) {
                BlockPos current = new BlockPos(block.getX(), y, block.getZ());
                boolean obstructed = isObstructed(serverLevel, current);

                // If we are obstructed and we have a highest block, break
                if (obstructed && highest != null) {
                    break;
                }
                // If we are not obstructed and we canChance, set the highest block
                else if (!obstructed && canChange(serverLevel, current)) {
                    highest = current;
                }
            }

            // If we have no highest block, set the topBlocks to the current block
            if (highest == null) {
                topBlocks.set(currentBlock, new BlockPos(block.getX(), serverLevel.getMaxBuildHeight(), block.getZ()));
            }
            else {
                topBlocks.set(currentBlock, highest);
            }

            currentBlock++;
        }

        // If we haven't finished yet, wait for the next tick
        if (currentBlock >= topBlocks.size()) {
            completeTask();
        }
    }

    protected void init() {
        debug.info("INIT");
        // Get Top Blocks
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());

        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        int topY = serverLevel.getMaxBuildHeight();
        areaLowestY = minY;

        if (!initResume) {
            if (fillMode) {
                initAABB(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
            }
            else {
                initAABB(new BlockPos(minX, lowestY, minZ), new BlockPos(maxX, serverLevel.getMaxBuildHeight(), maxZ));
            }
            debug.info("New AABB: " + boundingBox);
        }

        initResume = false;

        if (fillMode) {
            topY = maxY;
        }
        else if (caveMode) {
            topY = (center != null) ? center.getY() : minY;
        }

        int blocksChecked = 0;

        // Iterate through the x and z coordinates
        for (int x = minX + xOffset; x <= maxX; x++) {
            for (int z = minZ + zOffset; z <= maxZ; z++) {
                if (caveMode || fillMode) {
                    topBlocks.add(new BlockPos(x, topY, z));
                }
                else {
                    topBlocks.add(ChunkCursorHelper.pokeHeightMap(serverLevel, new BlockPos(x, topY, z)));
                }

                blocksChecked++;

                /*
                // Currently doesn't work, look into further
                if (blocksChecked >= blocksPerTick * 8) {
                    initResume = true;
                    xOffset = x;
                    zOffset = z;

                    debug.info("x: " + xOffset + " | z: " + zOffset + " | blocksChecked: " + blocksChecked + " | max: " + blocksPerTick * 8);
                    break;
                }
                 */
            }

            if (initResume) break;
        }

        if (!initResume) {
            Collections.shuffle(topBlocks);
            debug.info("Total Blocks Found: " + topBlocks.size());
            fullDebug.info("Blocks Found: " + topBlocks);
            completeTask();
        }

    }

    private int totalExtraBlocks = 0;



    protected void searchAndChange() {
        fullDebug.info("SEARCH_CHANGE");

        int blocksChecked = 0;
        int blocksChanged = 0;
        currentBatch++;

        ArrayList<BlockPos> adjacentBlocks = new ArrayList<>();

        for (int i = 0; i < blocksPerTick; i++) {

            if (currentBlock >= topBlocks.size()) break;

            BlockPos pos = topBlocks.get(currentBlock);
            int maxY = fillMode ? pos.getY() - areaLowestY : pos.getY() - lowestY;

            fullDebug.info("Current Top Position: " + pos + " | Max Y: " + maxY);

            boolean addedN = false;
            boolean addedE = false;
            boolean addedS = false;
            boolean addedW = false;

            if (pos.equals(finalTopBlock)) {
                debug.info("Primary Area complete! Awaiting adjacent completion...");
                primaryComplete = true;
            }

            for (int y = 0; y < maxY; y++)
            {
                BlockPos posY = pos.below(y);
                blocksChecked++;

                // If we are including adjacent blocks, check if we can add them
                if (shouldRunAdjacent(serverLevel, posY)) {
                    BlockPos posY2 = posY.above();
                    for (int a = 0; a <= 2; a++) {

                        checkedBlocks.add(posY2);

                        BlockPos posN = posY2.north();
                        BlockPos posE = posY2.east();
                        BlockPos posS = posY2.south();
                        BlockPos posW = posY2.west();

                        if (isValidAdjacent(serverLevel, posN) && !addedN) {
                            adjacentBlocks.add(posN);
                            addedN = true;
                            totalExtraBlocks++;
                        }
                        if (isValidAdjacent(serverLevel, posE) && !addedE) {
                            adjacentBlocks.add(posE);
                            addedE = true;
                            totalExtraBlocks++;
                        }
                        if (isValidAdjacent(serverLevel, posS) && !addedS) {
                            adjacentBlocks.add(posS);
                            addedS = true;
                            totalExtraBlocks++;
                        }
                        if (isValidAdjacent(serverLevel, posW) && !addedW) {
                            adjacentBlocks.add(posW);
                            addedW = true;
                            totalExtraBlocks++;
                        }

                        posY2 = (a==1) ? posY.above(2) : posY2.below();
                    }
                }

                if (solidFill) {
                    if (canChange(serverLevel, posY)) {
                        changeBlock(serverLevel, posY);
                        blocksChanged++;
                    }
                    else if (canConsume(serverLevel, posY)) {
                        consumeBlock(serverLevel, posY);
                        blocksChanged++;
                    }
                }
                else if (isObstructed(serverLevel, posY) && !disableObstruction && !fillMode) {
                    break;
                }
                else {
                    if (canChange(serverLevel, posY)) {
                        changeBlock(serverLevel, posY);
                        blocksChanged++;
                    }
                    else if (canConsume(serverLevel, posY)) {
                        consumeBlock(serverLevel, posY);
                        blocksChanged++;
                    }
                }
            }

            currentBlock++;

            if (blocksChecked > blocksPerTick*2) {
                break;
            }

        }

        // Add any adjacent blocks found in this batch to the topBlocks array for processing
        if (maxExtraBlocks > 0) {
            topBlocks.addAll(adjacentBlocks);
            fullDebug.info("Adding Adjacent Blocks to Top Blocks | Total: " + adjacentBlocks.size() + " | Total Adjacent: " + totalExtraBlocks + " - Max: " + maxExtraBlocks);
        }

        fullDebug.info("Batch Complete! Checked " + blocksChecked + " blocks | Changed " + blocksChanged + " blocks");

        // Every 32 batches, run the periodic check
        if (currentBatch % 32 == 0) periodicEntityCheck(serverLevel, boundingBox);

        // Reset to 0 if anything was changed in this batch
        batchesSinceLastChange = (blocksChanged == 0) ? batchesSinceLastChange + 1 : 0;

        // Exit early if all topBlocks have been run through and none of the last 12 batches of adjacentBlocks found anything changeable
        if (primaryComplete && batchesSinceLastChange >= 16) {
            debug.info("The last 16 batches did not find any changeable blocks. Primary area is complete, terminating early...");
            completeTask();
        }

        // No blocks left, finish
        if (currentBlock >= topBlocks.size()) {
            completeTask();
        }
    }


    protected void checkReady() {
        clear();
        if (serverLevel != null && pos1 != null && pos2 != null) {
            debug.info("ServerLevel: " + serverLevel + " | CONFIRMED");
            if (center != null) {debug.info("Center:     " + center + " | CONFIRMED");}
            else {debug.info("    Center: BlockPos{#N/A}                 | OFFLINE");}
            debug.info("Position 1: " + pos1 +   " | CONFIRMED");
            debug.info("Position 2: " + pos2 +   " | CONFIRMED");
            debug.info("System is now ready for execution!");
            status = State.READY;
        }
    }

    // Builder ---------------------------------------------------------------------------------------------------------
    public T level(ServerLevel serverLevel) {
        if (!active) {
            debug.info("Setting level set: " + serverLevel);
            this.serverLevel = serverLevel;
            this.level = serverLevel;
            this.lowestY = level.getMinBuildHeight();
            checkReady();
        }
        return (T) this;
    }

    public T blocksPerTick(int blocksPerTick) {
        debug.info("Setting blocksPerTick set: " + blocksPerTick);
        this.blocksPerTick = blocksPerTick;
        return (T) this;
    }

    /**
     * Makes the GhostCursor cube / square shaped. Even sizes are not possible with this method
     * If either pos1() or pos2() are run, the variable center is cleared
     * @param center The block position to center the cube / square on
     * @param radius Radius of the square - A radius of 1 will create a 3x3 square or 3x3x3 cube
     */

    public T center(BlockPos center, int radius) {
        // N: -Z | E: +X | S: +Z | W: -X
        if (!active) {
            debug.info("Setting center set: " + center + " | r = " + radius);
            this.center = center;
            this.pos1 = center.north(radius).west(radius).below(radius);
            this.pos2 = center.south(radius).east(radius).above(radius);
            checkReady();
        }
        return (T) this;
    }

    public T pos1(BlockPos pos1) {
        if (!active) {
            debug.info("Setting pos1 set: " + pos1);
            this.center = null;
            this.pos1 = pos1;
            checkReady();
        }
        return (T) this;
    }

    public T pos2(BlockPos pos2) {
        if (!active) {
            debug.info("Setting pos2 set: " + pos2);
            this.center = null;
            this.pos2 = pos2;
            checkReady();
        }
        return (T) this;
    }

    /**
     * Makes the GhostCursor cube / square shaped. Even sizes are not possible with this method
     * Utilises the pos1(), pos2() and level() functions
     * @param pos The blockPos in the chunk to center the cube / square on
     * @param radius Radius of the square - A radius of 1 will create a 3x3 square
     */

    public T chunkCenter(ServerLevel level, BlockPos pos, int radius) {
        // N: -Z | E: +X | S: +Z | W: -X
        if (!active) {
            debug.info("Setting center set: [Block: " + pos + " | Chunk: " + level.getChunkAt(pos) + "] | r = " + radius);

            LevelChunk centerChunk =  level.getChunkAt(pos);
            LevelChunk nwChunk = level.getChunk(centerChunk.getPos().x - radius, centerChunk.getPos().z - radius);
            LevelChunk seChunk = level.getChunk(centerChunk.getPos().x + radius, centerChunk.getPos().z + radius);

            level(level);
            pos1(new BlockPos(nwChunk.getPos().getMinBlockX(), level.getMinBuildHeight(), nwChunk.getPos().getMinBlockZ()));
            pos2(new BlockPos(seChunk.getPos().getMaxBlockX(), level.getMaxBuildHeight(), seChunk.getPos().getMaxBlockZ()));

            this.center = pos;
            checkReady();
        }
        return (T) this;
    }

    public T chunkArea(LevelChunk chunk1, LevelChunk chunk2) {
        if (!active) {
            pos1(null);
            pos2(null);

            ChunkPos chunkPos1 = chunk1.getPos();
            ChunkPos chunkPos2 = chunk2.getPos();
            ServerLevel l = (ServerLevel) chunk1.getLevel();

            int minX = Math.min(chunkPos1.x, chunkPos2.x);
            int minZ = Math.min(chunkPos1.z, chunkPos2.z);

            int maxX = Math.max(chunkPos1.x, chunkPos2.x);
            int maxZ = Math.max(chunkPos1.z, chunkPos2.z);

            chunkPos1 = l.getChunk(minX, minZ).getPos();
            chunkPos2 = l.getChunk(maxX, maxZ).getPos();

            level(l);
            pos1(new BlockPos(chunkPos1.getMinBlockX(), l.getMinBuildHeight(), chunkPos1.getMinBlockZ()));
            pos2(new BlockPos(chunkPos2.getMaxBlockX(), l.getMinBuildHeight(), chunkPos2.getMaxBlockZ()));

            checkReady();
        }
        return (T) this;
    }

    public T fadeDistance(int fadeDistance) {
        if (!active) {
            debug.info("Setting fadeDistance set: " + fadeDistance);
            this.fadeDistance = fadeDistance;
        }
        return (T) this;
    }

    /**
     * Overrides the maximum extra adjacent blocks the system can sort through
     * You can also use disableAdjacentBlocks() or enableAdjacentBlocks() instead of using 0 or -1;
     * @param max Set to -1 to base max on scale of area, 0 to disable
     */

    public T maxAdjacentBlocks(int max) {
        if (!active) {
            debug.info("Setting maxAdjacentBlocks set: " + maxAdjacentBlocks);
            this.maxAdjacentBlocks = max;
        }
        return (T) this;
    }

    public T disableAdjacentBlocks() {
        return maxAdjacentBlocks(0);
    }

    public T enableAdjacentBlocks() {
        return maxAdjacentBlocks(-1);
    }


    /** When run, caveMode is set to: true - Makes the GhostCursor start at the lowest Y position traveling up until a block is encountered
     * When a block is encountered, the position is saved as the highest block for that X,Z */
    public T caveMode() {return caveMode(true);}

    /**
     * When set to: true - Makes the GhostCursor start at the lowest Y position traveling up until a block is encountered
     * When a block is encountered, the position is saved as the highest block for that X,Z
     * @param caveMode Set to true to enable, set to false to disable
     */

    public T caveMode(boolean caveMode) {
        if (!active) {
            if (caveMode) fillMode(false);
            debug.info("Setting caveMode set: " + caveMode);
            this.caveMode = caveMode;
        }
        return (T) this;
    }


    /** When run, fillMode is set to: true - Makes the ChunkCursor get all non-obstructed blocks within the defined cuboid
     * (caveMode will be set to: false)
     */
    public T fillMode() {return fillMode(true);}

    /**
     * When set to: true - Makes the ChunkCursor get all non-obstructed blocks within the defined cuboid
     * (If true: caveMode will be set to: false)
     * @param fillMode Set to true to enable, set to false to disable
     */

    public T fillMode(boolean fillMode) {
        if (!active) {
            if (fillMode) caveMode(false);
            debug.info("Setting fillMode set: " + fillMode);
            this.fillMode = fillMode;
        }
        return (T) this;
    }


    /** When run, solidFill is set to: true - Makes the ChunkCursor get ALL blocks within the defined cuboid
     * (fillMode is set to: true & caveMode is set to: false)
     */
    public T solidFill() {return solidFill(true);}

    /**
     * When set to: true - All blocks will be infected even if obstructed
     * @param solidFill Set to true to enable, set to false to disable
     */

    public T solidFill(boolean solidFill) {
        if (!active) {
            debug.info("Setting solidFill set: " + solidFill);
            this.solidFill = solidFill;
        }
        return (T) this;
    }


    /** When run, disableObstruction is set to: true - Will ignore the output of isObstructed function, setting it to always be false */
    public T disableObstruction() {return disableObstruction(true);}

    /**
     * When set to: true - Will ignore the output of isObstructed function, setting it to always be false
     * @param disableObstruction Set to true to enable, set to false to disable
     */

    public T disableObstruction(boolean disableObstruction) {
        if (!active) {
            debug.info("Setting disableObstruction set: " + disableObstruction);
            this.disableObstruction = disableObstruction;
        }
        return (T) this;
    }


    /** When run, doNotPlaceFeatures is set to: true - Will attempt to prevent placement of features such as Spawners */
    public T doNotPlaceFeatures() {return doNotPlaceFeatures(true);}

    /**
     * When set to: true -  Will attempt to prevent placement of features such as Spawners
     * @param doNotPlaceFeatures Set to true to enable, set to false to disable
     */

    public T doNotPlaceFeatures(boolean doNotPlaceFeatures) {
        if (!active) {
            debug.info("Setting doNotPlaceFeatures set: " + doNotPlaceFeatures);
            this.doNotPlaceFeatures = doNotPlaceFeatures;
        }
        return (T) this;
    }


    /** When run, spawnSurfaceCursorsAtEnd is set to: true - Will attempt to spawn surface cursors after change is complete where appropriate */
    public T spawnSurfaceCursorsAtEnd() {return spawnSurfaceCursorsAtEnd(true);}

    /**
     * When set to: true -  Will attempt to spawn surface cursors after change is complete where appropriate
     * @param spawnSurfaceCursorsAtEnd Set to true to enable, set to false to disable
     */

    public T spawnSurfaceCursorsAtEnd(boolean spawnSurfaceCursorsAtEnd) {
        if (!active) {
            debug.info("Setting spawnSurfaceCursorsAtEnd set: " + spawnSurfaceCursorsAtEnd);
            this.spawnSurfaceCursorsAtEnd = spawnSurfaceCursorsAtEnd;
        }
        return (T) this;
    }

    public T executeOnStart(Runnable runnable) {
        if (!active) {
            debug.info("Setting executeOnStart set: " + runnable);
            this.executeOnStart = runnable;
        }
        return (T) this;
    }

    public T executeOnPause(Runnable runnable) {
        if (!active) {
            debug.info("Setting executeOnPause set: " + runnable);
            this.executeOnPause = runnable;
        }
        return (T) this;
    }

    public T executeOnEnd(Runnable runnable) {
        if (!active) {
            debug.info("Setting executeOnEnd set: " + runnable);
            this.executeOnEnd = runnable;
        }
        return (T) this;
    }

    public T maxTicks(int ticks) {
        debug.info("Setting maxTicks set: " + ticks + " | maxTicksWarning set: " + ticks/2);
        this.maxTicks = ticks;
        this.maxTicksWarning = ticks/2;
        return (T) this;
    }


    public boolean isActive() {return active;}
    public ServerLevel getServerLevel() {return serverLevel;}
    public BlockPos getPos1() {return pos1;}
    public BlockPos getPos2() {return pos2;}
    public BlockPos getCenter() {return center;}
    public int getFadeDistance() {return fadeDistance;}
    public int getMaxAdjacentBlocks() {return fadeDistance;}

    public boolean shouldIgnoreObstruction() {return disableObstruction;}
    public boolean shouldCave() {return caveMode;}
    public boolean shouldFill() {return fillMode;}
    public boolean shouldSolid() {return solidFill;}
    public boolean shouldPlaceFeatures() {return !doNotPlaceFeatures;}
    public boolean shouldSpawnCursors() {return spawnSurfaceCursorsAtEnd;}
}