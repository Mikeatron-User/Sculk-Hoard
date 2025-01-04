package com.github.sculkhorde.common.entity.entity_debugging;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public class GoalDebuggerUtility {

    public static void printGoalToConsole(Level level, Goal goal)
    {
        SculkHorde.LOGGER.info(goalToString(level, goal));
    }
    public static String goalToString(Level level, Goal goal)
    {
        String result = "ERROR: " + goal.getClass() + " is not a Debuggable Goal";

        if(goal instanceof IDebuggableGoal)
        {
            result = "\nGoal = " + getGoalName(goal);
            result += "\n{";
            result += "\n   LastTimeOfExecution = " + getSecondsSinceLastExecution(level.getGameTime(), goal) + " seconds ago.";
            result += "\n   getLastReasonOfGoalNoStart = " + getLastReasonOfGoalNoStart(goal) + ".";
            result += "\n   getTimeRemainingBeforeCooldownOver = " + TickUnits.convertTicksToSeconds(getTimeRemainingBeforeCooldownOver(goal)) + " seconds.";
            result += "\n}\n";
        }

        return result;
    }

    public static String getGoalName(Goal goal)
    {
        String result = "None";

        if(goal instanceof IDebuggableGoal debuggableGoal)
        {
            if(debuggableGoal.getGoalName().isPresent())
            {
                result = debuggableGoal.getGoalName().get();
            }
        }

        return result;
    }

    public static long getSecondsSinceLastExecution(long currentTime, Goal goal)
    {
        return TickUnits.convertTicksToSeconds(currentTime - getLastTimeOfGoalExecution(goal));
    }

    public static String getLastReasonOfGoalNoStart(Goal goal)
    {
        String result = "None";

        if(goal instanceof IDebuggableGoal debuggableGoal)
        {
            if(debuggableGoal.getLastReasonForGoalNoStart().isPresent())
            {
                result = debuggableGoal.getLastReasonForGoalNoStart().get();
            }
        }

        return result;
    }

    public static long getLastTimeOfGoalExecution(Goal goal)
    {
        if(goal instanceof IDebuggableGoal debuggableGoal)
        {
            return debuggableGoal.getLastTimeOfGoalExecution();
        }

        return -1;
    }

    public static long getTimeRemainingBeforeCooldownOver(Goal goal)
    {

        if(goal instanceof IDebuggableGoal debuggableGoal)
        {
            return debuggableGoal.getTimeRemainingBeforeCooldownOver();
        }

        return -1;
    }
}
