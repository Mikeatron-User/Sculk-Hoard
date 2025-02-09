package com.github.sculkhorde.systems.cursor_system;

import com.github.sculkhorde.common.entity.infection.CursorEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class CursorSystem {

    // Virtual Cursors Variables ---------------------------------------------------------------------------------------

    long INITIAL_WAIT_TIME_AFTER_SERVER_STARTUP = TickUnits.convertMinutesToTicks(1);
    long timeOfServerStartup = 0;

    SortedVirtualCursorList performanceExemptCursors = new SortedVirtualCursorList();
    SortedVirtualCursorList virtualCursors = new SortedVirtualCursorList();
    private int virtualCursorIndex = 0;


    // Entity Cursors Variables ----------------------------------------------------------------------------------------
    SortedCursorList cursors = new SortedCursorList();
    private int index = 0;

    private int tickDelay = 3;

    private boolean manualControlOfTickingEnabled = false;

    // Entity Cursors Methods ------------------------------------------------------------------------------------------

    public void setManualControlOfTickingEnabled(boolean value) { manualControlOfTickingEnabled = value; }
    public boolean isManualControlOfTickingEnabled() { return manualControlOfTickingEnabled; }

    public void addCursor(CursorEntity entity)
    {
        cursors.insertCursor(entity);
    }

    /**
     * Add a cursor to the list if it's not already in the list.
     * @param entity
     */
    public void computeIfAbsent(CursorEntity entity)
    {
        if(cursors.getIndexOfCursor(entity).isEmpty())
        {
            addCursor(entity);
        }
    }

    public int getSizeOfCursorList()
    {
        return cursors.list.size();
    }


    /**
     * Go through the list of cursors and tick them
     */
    public void tickCursors()
    {
        ArrayList<CursorEntity> listOfCursors = cursors.getList();

        for(int i = 0; i < SculkHorde.autoPerformanceSystem.getCursorsToTickPerTick(); i++)
        {
            if(index >= listOfCursors.size())
            {
                index = 0;
                continue;
            }

            CursorEntity cursorAtIndex = listOfCursors.get(index);

            cursorAtIndex.chanceToThanosSnapThisCursor();

            if(cursorAtIndex.canBeManuallyTicked())
            {
                cursorAtIndex.cursorTick();
                index++;
            }
        }
    }

    // Virtual Cursors Methods -----------------------------------------------------------------------------------------

    public void debugHowManyVirtualCursorsAreInThisArea(Level level, BlockPos pos, int radius)
    {
        ArrayList<ICursor> listOfCursors = virtualCursors.getList();
        int count = 0;
        for(ICursor cursorAtIndex : listOfCursors)
        {
            if(BlockAlgorithms.areTheseDimensionsEqual((ServerLevel) level, (ServerLevel) cursorAtIndex.getLevel()) && cursorAtIndex.getBlockPosition().distSqr(pos) <= radius * radius)
            {
                count++;
            }
        }
        SculkHorde.LOGGER.info("There are " + count + " virtual cursors in this area " + pos);
    }

    public static Optional<VirtualSurfaceInfestorCursor> createSurfaceInfestorVirtualCursor(Level level, BlockPos pos)
    {
        //SculkHorde.cursorSystem.debugHowManyVirtualCursorsAreInThisArea(level, pos, 5);
        if(SculkHorde.cursorSystem.isCursorPopulationAtMax())
        {
            return Optional.empty();
        }

        VirtualSurfaceInfestorCursor cursor = new VirtualSurfaceInfestorCursor(level);
        cursor.moveTo(pos.getX(), pos.getY(), pos.getZ());
        SculkHorde.cursorSystem.addVirtualCursor(cursor);
        return Optional.of(cursor);
    }

    public void addVirtualCursor(ICursor entity)
    {
        if(virtualCursors.getIndexOfCursor(entity).isEmpty())
        {
            virtualCursors.insertCursor(entity);
        }
    }

    public static VirtualOreMinerCursor createOreMinerCursor(Level level, Block blockToTarget, Player owner, BlockPos pos, ItemStack pickaxe)
    {
        VirtualOreMinerCursor cursor = new VirtualOreMinerCursor(level, blockToTarget, owner.getUUID(), pickaxe);
        cursor.moveTo(pos.getX(), pos.getY(), pos.getZ());
        SculkHorde.cursorSystem.addPerformanceExemptVirtualCursor(cursor);
        return cursor;
    }

    public static VirtualSurfaceInfestorCursor createPerformanceExemptSurfaceInfestorVirtualCursor(Level level, BlockPos pos)
    {
        VirtualSurfaceInfestorCursor cursor = new VirtualSurfaceInfestorCursor(level);
        cursor.moveTo(pos.getX(), pos.getY(), pos.getZ());
        SculkHorde.cursorSystem.addPerformanceExemptVirtualCursor(cursor);
        return cursor;
    }

    public void addPerformanceExemptVirtualCursor(ICursor entity)
    {
        if(performanceExemptCursors.getIndexOfCursor(entity).isEmpty())
        {
            performanceExemptCursors.insertCursor(entity);
        }
    }

    public int getSizeOfPerformanceExemptVirtualCursorList()
    {
        return performanceExemptCursors.list.size();
    }


    public int getSizeOfVirtualCursorList()
    {
        return virtualCursors.list.size();
    }

    public void tickVirtualCursors()
    {
        // Tick All Performance Exempt Cursors, regardless of performance mode
        ArrayList<ICursor> listOfPerformanceExemptCursors = performanceExemptCursors.getList();
        for(ICursor cursorAtIndex : listOfPerformanceExemptCursors)
        {
            cursorAtIndex.tick();
        }

        if(SculkHorde.savedData.isHordeDefeated())
        {
            return;
        }

        // Tick Virtual Cursors
        ArrayList<ICursor> listOfCursors = virtualCursors.getList();
        int cursorsTicked = 0;
        for(int i = 0; i < SculkHorde.cursorSystem.getSizeOfVirtualCursorList() && cursorsTicked <= SculkHorde.autoPerformanceSystem.getCursorsToTickPerTick(); i++)
        {
            if(virtualCursorIndex >= listOfCursors.size())
            {
                virtualCursorIndex = 0;
                continue;
            }

            ICursor cursorAtIndex = listOfCursors.get(virtualCursorIndex);

            if(cursorAtIndex.isSetToBeDeleted())
            {
                virtualCursorIndex++;
                continue;
            }

            if(isPerformanceModeThresholdReached())
            {
                cursorsTicked++;
            }

            cursorAtIndex.tick();
            virtualCursorIndex++;
        }
    }

    // Performance Mode Methods ----------------------------------------------------------------------------------------

    public boolean isPerformanceModeThresholdReached()
    {
        return getSizeOfCursorList() + getSizeOfVirtualCursorList() >= SculkHorde.autoPerformanceSystem.getInfectorCursorPopulationThreshold();
    }
    public boolean isCursorPopulationAtMax()
    {
        return getSizeOfCursorList() + getSizeOfVirtualCursorList() >= SculkHorde.autoPerformanceSystem.getMaxInfectorCursorPopulation();
    }

    // Main Methods ----------------------------------------------------------------------------------------------------

    /**
     * This runs every tick the server runs.
     * The purpose of this function is to manually tick all the cursors if it is enabled.
     * It enables if the population of cursor entities are too high. That way we can control
     * the rate they tick to conserve performance.
     */
    public void serverTick()
    {
        if(timeOfServerStartup == 0)
        {
            timeOfServerStartup = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();
            return;
        }

        /*  The reason we wait a minute after the server starts is due to a weird issue I experienced when developing the
            virtual cursor system. For some reason, the game will randomly stall upon generating a world at around 99%.
            Pausing this system for a minute seems to have resolved this issue.
         */
        if(Math.abs(ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - timeOfServerStartup) < INITIAL_WAIT_TIME_AFTER_SERVER_STARTUP)
        {
            return;
        }

        if(!isPerformanceModeThresholdReached())
        {
            setManualControlOfTickingEnabled(false);

            // Virtual Cursors
            performanceExemptCursors.clean(); // Clean the list before we start ticking cursors
            virtualCursors.clean(); // Clean the list before we start ticking cursors
            tickVirtualCursors();

            // Entity Cursors
            cursors.clean(); // Clean the list before we start ticking cursors
            tickCursors();

            return;
        }

        /// If performance mode is enabled, we need to manually tick cursors.
        //Only Execute if the cooldown. Get the value from the config file.
        if(tickDelay < SculkHorde.autoPerformanceSystem.getDelayBetweenCursorTicks())
        {
            tickDelay++;
            return;
        }
        tickDelay = 0;

        setManualControlOfTickingEnabled(true);

        // Virtual Cursors
        virtualCursors.clean(); // Clean the list before we start ticking cursors
        tickVirtualCursors();

        // Entity Cursors
        cursors.clean(); // Clean the list before we start ticking cursors
        tickCursors();
    }

    public class SortedCursorList
    {
        private ArrayList<CursorEntity> list;

        /**
         * Default Constructor
         */
        public SortedCursorList()
        {
            list = new ArrayList<>();
        }

        /**
         * Just get the list of cursors
         * @return The Array List of cursors
         */
        public ArrayList<CursorEntity> getList()
        {
            return list;
        }

        /**
         * Determines if a cursor entity should be deleted from the list.
         * @param entity The Cursor entity
         * @return True if the cursor should be deleted, false otherwise.
         */
        public boolean shouldCursorBeDeleted(CursorEntity entity)
        {
            return entity == null || entity.isRemoved();
        }

        /**
         * Go through the list, look for cursors that should be deleted,
         * then get rid of them from the list.
         * Note: Doing it this way is sort of cheesy. Removing in the middle
         * of a for loop is not advised.
         */
        public void clean()
        {
            for(int i = 0; i < list.size(); i++)
            {
                if(shouldCursorBeDeleted(list.get(i)))
                {
                    list.remove(i);
                    i--;
                }
            }
        }

        /**
         * Insert a cursor into the list based on the value of it's UUID.
         * This list is sorted, so we need to insert it into the correct place.
         * @param entity The Cursor to Insert.
         */
        public void insertCursor(CursorEntity entity)
        {
            int positionToInsert = 0;

            for(int index = 0; index < list.size(); index++)
            {
                CursorEntity cursorAtIndex = list.get(index);
                positionToInsert = index;

                if(entity.getUUID().compareTo(cursorAtIndex.getUUID()) >= 0)
                {
                    break;
                }
            }
            list.add(positionToInsert, entity);
        }


        /**
         * Use Binary Search Algorithm to find the Cursor Entity we are looking for.
         * @param entity The Cursor Entity
         * @return The potential position of the cursor in the list.
         */
        public Optional<Integer> getIndexOfCursor(CursorEntity entity) {
            // We use the UUID to compare Cursor Entities
            UUID uuid = entity.getUUID();

            int leftIndex = 0;
            int rightIndex = list.size() - 1;

            while (leftIndex <= rightIndex) {
                int midIndex = leftIndex + (rightIndex - leftIndex) / 2;
                int compareValue = list.get(midIndex).getUUID().compareTo(uuid);

                // Check if UUID is present at mid
                if (compareValue == 0)
                    return Optional.of(midIndex);

                // If UUID greater, ignore left half
                if (compareValue > 0)
                    leftIndex = midIndex + 1;

                    // If UUID is smaller, ignore right half
                else
                    rightIndex = midIndex - 1;
            }

            return Optional.empty();
        }
    }

    public class SortedVirtualCursorList
    {
        private ArrayList<ICursor> list;

        /**
         * Default Constructor
         */
        public SortedVirtualCursorList()
        {
            list = new ArrayList<>();
        }

        /**
         * Just get the list of cursors
         * @return The Array List of cursors
         */
        public ArrayList<ICursor> getList()
        {
            return list;
        }

        /**
         * Determines if a cursor entity should be deleted from the list.
         * @param cursor The Cursor entity
         * @return True if the cursor should be deleted, false otherwise.
         */
        public boolean shouldCursorBeDeleted(ICursor cursor)
        {
            return cursor == null || cursor.isSetToBeDeleted();
        }

        /**
         * Go through the list, look for cursors that should be deleted,
         * then get rid of them from the list.
         * Note: Doing it this way is sort of cheesy. Removing in the middle
         * of a for loop is not advised.
         */
        public void clean()
        {
            for(int i = 0; i < list.size(); i++)
            {
                if(shouldCursorBeDeleted(list.get(i)))
                {
                    list.remove(i);
                    i--;
                }
            }
        }

        /**
         * Insert a cursor into the list based on the value of it's UUID.
         * This list is sorted, so we need to insert it into the correct place.
         * @param entity The Cursor to Insert.
         */
        public void insertCursor(ICursor entity)
        {
            int positionToInsert = 0;

            for(int index = 0; index < list.size(); index++)
            {
                ICursor cursorAtIndex = list.get(index);
                positionToInsert = index;

                if(entity.getUUID().compareTo(cursorAtIndex.getUUID()) >= 0)
                {
                    break;
                }
            }
            list.add(positionToInsert, entity);
        }


        /**
         * Use Binary Search Algorithm to find the Cursor Entity we are looking for.
         * @param entity The Cursor Entity
         * @return The potential position of the cursor in the list.
         */
        public Optional<Integer> getIndexOfCursor(ICursor entity) {
            // We use the UUID to compare Cursor Entities
            UUID uuid = entity.getUUID();

            int leftIndex = 0;
            int rightIndex = list.size() - 1;

            while (leftIndex <= rightIndex) {
                int midIndex = leftIndex + (rightIndex - leftIndex) / 2;
                int compareValue = list.get(midIndex).getUUID().compareTo(uuid);

                // Check if UUID is present at mid
                if (compareValue == 0)
                    return Optional.of(midIndex);

                // If UUID greater, ignore left half
                if (compareValue > 0)
                    leftIndex = midIndex + 1;

                    // If UUID is smaller, ignore right half
                else
                    rightIndex = midIndex - 1;
            }

            return Optional.empty();
        }
    }
}
