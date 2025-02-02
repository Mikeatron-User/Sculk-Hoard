package com.github.sculkhorde.systems.chunk_cursor_system;

import java.util.ArrayList;

public class ChunkInfestationSystem {

    protected static ArrayList<ChunkCursorInfector> chunkInfectors = new ArrayList<>();
    protected static ArrayList<ChunkCursorPurifier> chunkPurifiers = new ArrayList<>();


    public static void addChunkInfector(ChunkCursorInfector infector) {
        chunkInfectors.add(infector);
    }

    public static void addChunkPurifier(ChunkCursorPurifier purifier) {
        chunkPurifiers.add(purifier);
    }

    public static void clearChunkInfectorList() {
        chunkInfectors.clear();
    }

    public static void clearChunkPurifierList() {
        chunkPurifiers.clear();
    }

    public static void clearChunkInfectorAndPurifierLists() {
        chunkInfectors.clear();
        chunkPurifiers.clear();
    }

    public static ArrayList<ChunkCursorInfector> getChunkInfectors() {
        return chunkInfectors;
    }

    public static ArrayList<ChunkCursorPurifier> getChunkPurifiers() {
        return chunkPurifiers;
    }

    private void cleanupFinishedChunkCursors()
    {
        chunkInfectors.removeIf(ChunkCursorInfector::isFinished);
        chunkPurifiers.removeIf(ChunkCursorPurifier::isFinished);
    }

    public void tick()
    {
        for(ChunkCursorInfector infector : chunkInfectors)
        {
            infector.tick();
        }

        for(ChunkCursorPurifier purifier : chunkPurifiers)
        {
            purifier.tick();
        }
    }
}
