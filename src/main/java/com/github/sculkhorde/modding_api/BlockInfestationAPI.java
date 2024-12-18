package com.github.sculkhorde.modding_api;

import com.github.sculkhorde.common.block.InfestationEntries.BlockInfestationTable;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;

public class BlockInfestationAPI {


    public static BlockInfestationTable addBlockInfestationTable(BlockInfestationTable table)
    {
        BlockInfestationSystem.INFESTATION_TABLES.add(table);
        BlockInfestationSystem.INFESTATION_TABLES.sort(Comparator.comparing(BlockInfestationTable::getPriority));
        return table;
    }

    public static BlockInfestationTable getExplicitBlockInfestationTable()
    {
        return BlockInfestationSystem.explicitInfectableBlocks;
    }

    public static BlockInfestationTable getTagBlockInfestationTable()
    {
        return BlockInfestationSystem.tagInfectableBlocks;
    }

    public static BlockInfestationTable getTagNonFullBlockInfestationTable()
    {
        return BlockInfestationSystem.tagInfectableNonFullBlocks;
    }

    public static BlockInfestationTable getConfigBlockInfestationTable()
    {
        return BlockInfestationSystem.configInfectableBlocks;
    }

    /**
     * Adds a flora block to be spawned on top of Sculk Infected Blocks.
     * Please make sure you only call this once or I'll have your head.
     * Here are some common weights used in {@link BlockInfestationSystem} <br>
     *     WEIGHT_SCULK_CATALYST = 1 <br>
     *     WEIGHT_SCULK_SUMMONER = 2 <br>
     *     WEIGHT_SCULK_SENSOR = 3 <br>
     *     WEIGHT_SPIKE = 4 <br>
     *     WEIGHT_SHROOMS = 6 <br>
     *     WEIGHT_GRASS = 200 <br>
     * @param flora The Block you want to spawn.
     * @param spawnWeight The chance it has of spawning.
     */
    public static void addSculkFloraEntry(Block flora, int spawnWeight)
    {
        SculkHorde.randomSculkFlora.addEntry(flora, spawnWeight);
    }
}
