package com.github.sculkhorde.systems.cursor_system;

import com.github.sculkhorde.common.entity.infection.CursorEntity;
import com.github.sculkhorde.core.SculkHorde;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class CursorSystem {

    // Virtual Cursors Variables ---------------------------------------------------------------------------------------
    SortedVirtualCursorList virtualCursors = new SortedVirtualCursorList();
    private int virtualCursorIndex = 0;
    private int virtualTickDelay = 3;


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

    public void addVirtualCursor(ICursor entity)
    {
        virtualCursors.insertCursor(entity);
    }

    public void computeIfAbsentVirtualCursor(ICursor entity)
    {
        if(virtualCursors.getIndexOfCursor((CursorEntity) entity).isEmpty())
        {
            addVirtualCursor(entity);
        }
    }

    public int getSizeOfVirtualCursorList()
    {
        return virtualCursors.list.size();
    }

    public void tickVirtualCursors()
    {
        ArrayList<ICursor> listOfCursors = virtualCursors.getList();

        for(int i = 0; i < SculkHorde.autoPerformanceSystem.getCursorsToTickPerTick(); i++)
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

            cursorAtIndex.moveTo(cursorAtIndex.getUUID().getMostSignificantBits(), cursorAtIndex.getUUID().getLeastSignificantBits(), cursorAtIndex.getUUID().getLeastSignificantBits());

            virtualCursorIndex++;
        }
    }

    // Performance Mode Methods ----------------------------------------------------------------------------------------

    public boolean isPerformanceModeThresholdReached()
    {
        return getSizeOfCursorList() >= SculkHorde.autoPerformanceSystem.getInfectorCursorPopulationThreshold();
    }
    public boolean isCursorPopulationAtMax()
    {
        return getSizeOfCursorList() >= SculkHorde.autoPerformanceSystem.getMaxInfectorCursorPopulation();
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
        //Only Execute if the cooldown. Get the value from the config file.
        if(tickDelay < SculkHorde.autoPerformanceSystem.getDelayBetweenCursorTicks())
        {
            tickDelay++;
            return;
        }
        tickDelay = 0;

        // Virtual Cursors
        virtualCursors.clean(); // Clean the list before we start ticking cursors
        tickVirtualCursors();

        // Entity Cursors

        cursors.clean(); // Clean the list before we start ticking cursors

        if(isPerformanceModeThresholdReached())
        {
            setManualControlOfTickingEnabled(true);
            tickCursors();
            return;
        }

        setManualControlOfTickingEnabled(false);
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
}
