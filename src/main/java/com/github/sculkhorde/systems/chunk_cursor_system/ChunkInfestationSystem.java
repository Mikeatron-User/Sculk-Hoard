package com.github.sculkhorde.systems.chunk_cursor_system;

import java.util.ArrayList;

public class ChunkInfestationSystem {

    protected ArrayList<ChunkCursorInfector> chunkInfectors = new ArrayList<>();
    protected ArrayList<ChunkCursorPurifier> chunkPurifiers = new ArrayList<>();


    public void addChunkInfector(ChunkCursorInfector infector) {
        chunkInfectors.add(infector);
    }

    public void addChunkPurifier(ChunkCursorPurifier purifier) {
        chunkPurifiers.add(purifier);
    }

    public void clearChunkInfectorList() {
        chunkInfectors.clear();
    }

    public void clearChunkPurifierList() {
        chunkPurifiers.clear();
    }

    public void clearChunkInfectorAndPurifierLists() {
        chunkInfectors.clear();
        chunkPurifiers.clear();
    }

    public ArrayList<ChunkCursorInfector> getChunkInfectors() {
        return chunkInfectors;
    }

    public ArrayList<ChunkCursorPurifier> getChunkPurifiers() {
        return chunkPurifiers;
    }

    private void cleanupFinishedChunkCursors()
    {
        chunkInfectors.removeIf(ChunkCursorInfector::isFinished);
        chunkPurifiers.removeIf(ChunkCursorPurifier::isFinished);
    }

    public void serverTick()
    {
        for(ChunkCursorInfector infector : chunkInfectors)
        {
            infector.tick();
        }

        for(ChunkCursorPurifier purifier : chunkPurifiers)
        {
            purifier.tick();
        }

        cleanupFinishedChunkCursors(); // Moved for debug testing, place back at line 50 once complete
    }
}
