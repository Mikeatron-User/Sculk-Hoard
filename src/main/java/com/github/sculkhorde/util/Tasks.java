package com.github.sculkhorde.util;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkCursorBase;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber

public class Tasks {
    private static final Map<Integer, ArrayList<Runnable>> tasks = new HashMap<>();
    private static int currentTick = 0;

    /**
     * Schedule a task to run after a delay.
     *
     * @param delayInTicks The delay in ticks before the task runs.
     * @param task         The code to execute after the delay.
     */
    public static void schedule(int delayInTicks, Runnable task) {
        int goalTick = currentTick + delayInTicks;

        ArrayList<Runnable> all;
        if (tasks.containsKey(goalTick)) {
            all = tasks.get(goalTick);
        }
        else {
            all = new ArrayList<>();
        }
        all.add(task);
        tasks.put(goalTick, all);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.getServer().isShutdown()) {
            SculkHorde.LOGGER.info("Server is shutting down, stop ticking...");
            tasks.clear();
        }
        else if (event.phase == TickEvent.Phase.END) { // Run at the end of each tick
            currentTick++;

            // Execute and remove tasks scheduled for the current tick
            if (tasks.containsKey(currentTick)) {
                for (Runnable task : tasks.get(currentTick)) {
                    task.run();
                }
                tasks.remove(currentTick);
            }
        }
    }
}

/*
private static final Map<Integer, Runnable> tasks = new HashMap<>();
private static int currentTick = 0;

public static boolean schedule(int delayInTicks, Runnable task) {
        int goalTick = currentTick + delayInTicks;
        int attempts = 0;

        while (tasks.containsKey(goalTick)) {
            goalTick++;
            attempts++;
            if (attempts >= 100) {
                return false;
            }
        }

        tasks.put(goalTick, task);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { // Run at the end of each tick
            currentTick++;

            // Execute and remove tasks scheduled for the current tick
            if (tasks.containsKey(currentTick)) {
                tasks.get(currentTick).run();
                tasks.remove(currentTick);
            }
        }
    }
 */
