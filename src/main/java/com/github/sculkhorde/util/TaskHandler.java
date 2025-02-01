package com.github.sculkhorde.util;

import net.minecraft.server.MinecraftServer;

public class TaskHandler {

    private final MinecraftServer server;
    private final Log debug = new Log(this.toString().replaceAll("com.github.sculkhorde.util.", ""));

    public TaskHandler(MinecraftServer server) {
        debug.info("Hello from new TaskHandler! Server: " + server);
        debug.enabled = false;
        this.server = server;
    }

    public void schedule (int delay, Runnable runnable) {
        debug.info("Schedule task in [" + delay + "] ticks: " + runnable);
        server.tell(new net.minecraft.server.TickTask(server.getTickCount() + delay, runnable));
    }
}
