package com.github.sculkhorde.systems;

import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.modding_api.BlockInfestationAPI;
import com.github.sculkhorde.common.block.InfestationEntries.BlockInfestationTable;
import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.common.blockentity.SculkBeeNestBlockEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.IPlantable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class BlockInfestationSystem {

    public static ArrayList<BlockInfestationTable> INFESTATION_TABLES = new ArrayList<>();
    public static BlockInfestationTable explicitInfectableBlocks;
    public static BlockInfestationTable tagInfectableBlocks;
    public static BlockInfestationTable tagInfectableNonFullBlocks;
    public static BlockInfestationTable configInfectableBlocks;

    public static int WEIGHT_SOULITE = 20;
    public static int WEIGHT_LARGE_FLORA = 100;
    public static int WEIGHT_SCULK_CATALYST = 500;
    public static int WEIGHT_SCULK_SUMMONER = 4000;
    public static int WEIGHT_SCULK_SENSOR = 7000;
    public static int WEIGHT_SPIKE = 10000;
    public static int WEIGHT_SHROOMS = 12000;
    public static int WEIGHT_GRASS = 80000;

    public static void initialize()
    {
        initializeInfestationTables();
        initializeSculkFlora();
    }

    private static void initializeSculkFlora()
    {
        SculkHorde.randomSculkFlora = new PoolBlocks();
        SculkHorde.randomSculkFlora.addExperimentalEntry(ModBlocks.SOULITE_CORE_BLOCK.get(), WEIGHT_SOULITE);
        SculkHorde.randomSculkFlora.addExperimentalEntry(ModBlocks.FUNGAL_SHROOM_CORE_BLOCK.get(), WEIGHT_LARGE_FLORA);
        SculkHorde.randomSculkFlora.addExperimentalEntry(ModBlocks.TENDRIL_CORE_BLOCK.get(), WEIGHT_LARGE_FLORA);
        SculkHorde.randomSculkFlora.addExperimentalEntry(ModBlocks.BEE_COLONY_CORE_BLOCK.get(), WEIGHT_LARGE_FLORA);
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_CATALYST, WEIGHT_SCULK_CATALYST);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SCULK_SUMMONER_BLOCK.get(), WEIGHT_SCULK_SUMMONER);
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_SENSOR, WEIGHT_SCULK_SENSOR);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SPIKE.get(), WEIGHT_SPIKE);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SMALL_SHROOM.get(), WEIGHT_SHROOMS);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SCULK_SHROOM_CULTURE.get(), WEIGHT_SHROOMS);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.GRASS_SHORT.get(), WEIGHT_GRASS);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.GRASS.get(), WEIGHT_GRASS);
    }

    private static void initializeInfestationTables()
    {
        // Used to infect blocks that are explicitly listed. Priority Matters
        explicitInfectableBlocks = new BlockInfestationTable(0, false);

        explicitInfectableBlocks.addEntry(1, Blocks.DIRT, Blocks.SCULK.defaultBlockState());
        explicitInfectableBlocks.addEntry(2, Blocks.COARSE_DIRT, Blocks.SCULK.defaultBlockState());
        explicitInfectableBlocks.addEntry(3, Blocks.GRASS_BLOCK, Blocks.SCULK.defaultBlockState());
        explicitInfectableBlocks.addEntry(4, Blocks.DIRT_PATH, Blocks.SCULK.defaultBlockState());
        explicitInfectableBlocks.addEntry(5, Blocks.PODZOL, Blocks.SCULK.defaultBlockState());
        explicitInfectableBlocks.addEntry(6, Blocks.CLAY, ModBlocks.INFESTED_CLAY.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(7, Blocks.STONE, ModBlocks.INFESTED_STONE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(8, Blocks.STONE_STAIRS, ModBlocks.INFESTED_STONE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(9, Blocks.STONE_SLAB, ModBlocks.INFESTED_STONE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(10, Blocks.DEEPSLATE, ModBlocks.INFESTED_DEEPSLATE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(11, Blocks.COBBLED_DEEPSLATE, ModBlocks.INFESTED_COBBLED_DEEPSLATE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(12, Blocks.COBBLED_DEEPSLATE_STAIRS, ModBlocks.INFESTED_COBBLED_DEEPSLATE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(13, Blocks.COBBLED_DEEPSLATE_SLAB, ModBlocks.INFESTED_COBBLED_DEEPSLATE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(14, Blocks.COBBLED_DEEPSLATE_WALL, ModBlocks.INFESTED_COBBLED_DEEPSLATE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(15, Blocks.SAND, ModBlocks.INFESTED_SAND.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(16, Blocks.SANDSTONE, ModBlocks.INFESTED_SANDSTONE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(17, Blocks.SANDSTONE_STAIRS, ModBlocks.INFESTED_SANDSTONE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(18, Blocks.SANDSTONE_SLAB, ModBlocks.INFESTED_SANDSTONE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(19, Blocks.SANDSTONE_WALL, ModBlocks.INFESTED_SANDSTONE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(20, Blocks.RED_SAND, ModBlocks.INFESTED_RED_SAND.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(21, Blocks.DIORITE, ModBlocks.INFESTED_DIORITE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(22, Blocks.DIORITE_STAIRS, ModBlocks.INFESTED_DIORITE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(23, Blocks.DIORITE_SLAB, ModBlocks.INFESTED_DIORITE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(24, Blocks.DIORITE_WALL, ModBlocks.INFESTED_DIORITE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(25, Blocks.GRANITE, ModBlocks.INFESTED_GRANITE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(26, Blocks.GRANITE_STAIRS, ModBlocks.INFESTED_GRANITE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(27, Blocks.GRANITE_SLAB, ModBlocks.INFESTED_GRANITE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(28, Blocks.GRANITE_WALL, ModBlocks.INFESTED_GRANITE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(29, Blocks.ANDESITE, ModBlocks.INFESTED_ANDESITE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(30, Blocks.ANDESITE_STAIRS, ModBlocks.INFESTED_ANDESITE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(31, Blocks.ANDESITE_SLAB, ModBlocks.INFESTED_ANDESITE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(32, Blocks.ANDESITE_WALL, ModBlocks.INFESTED_ANDESITE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(33, Blocks.TUFF, ModBlocks.INFESTED_TUFF.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(34, Blocks.CALCITE, ModBlocks.INFESTED_CALCITE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(35, Blocks.COBBLESTONE, ModBlocks.INFESTED_COBBLESTONE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(36, Blocks.COBBLESTONE_STAIRS, ModBlocks.INFESTED_COBBLESTONE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(37, Blocks.COBBLESTONE_SLAB, ModBlocks.INFESTED_COBBLESTONE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(38, Blocks.COBBLESTONE_WALL, ModBlocks.INFESTED_COBBLESTONE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(39, Blocks.MOSSY_COBBLESTONE, ModBlocks.INFESTED_MOSSY_COBBLESTONE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(40, Blocks.MOSSY_COBBLESTONE_STAIRS, ModBlocks.INFESTED_MOSSY_COBBLESTONE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(41, Blocks.MOSSY_COBBLESTONE_SLAB, ModBlocks.INFESTED_MOSSY_COBBLESTONE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(42, Blocks.MOSSY_COBBLESTONE_WALL, ModBlocks.INFESTED_MOSSY_COBBLESTONE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(43, Blocks.GRAVEL, ModBlocks.INFESTED_GRAVEL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(44, Blocks.MUD, ModBlocks.INFESTED_MUD.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(45, Blocks.PACKED_MUD, ModBlocks.INFESTED_PACKED_MUD.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(46, Blocks.MUD_BRICKS, ModBlocks.INFESTED_MUD_BRICKS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(47, Blocks.MUD_BRICK_STAIRS, ModBlocks.INFESTED_MUD_BRICK_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(48, Blocks.MUD_BRICK_SLAB, ModBlocks.INFESTED_MUD_BRICK_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(49, Blocks.MUD_BRICK_WALL, ModBlocks.INFESTED_MUD_BRICK_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(50, Blocks.SNOW_BLOCK, ModBlocks.INFESTED_SNOW.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(51, Blocks.MOSS_BLOCK, ModBlocks.INFESTED_MOSS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(52, Blocks.TERRACOTTA, ModBlocks.INFESTED_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(53, Blocks.BLACK_TERRACOTTA, ModBlocks.INFESTED_BLACK_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(54, Blocks.BLUE_TERRACOTTA, ModBlocks.INFESTED_BLUE_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(55, Blocks.BROWN_TERRACOTTA, ModBlocks.INFESTED_BROWN_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(56, Blocks.CYAN_TERRACOTTA, ModBlocks.INFESTED_CYAN_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(57, Blocks.GRAY_TERRACOTTA, ModBlocks.INFESTED_GRAY_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(58, Blocks.GREEN_TERRACOTTA, ModBlocks.INFESTED_GREEN_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(59, Blocks.LIGHT_BLUE_TERRACOTTA, ModBlocks.INFESTED_LIGHT_BLUE_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(60, Blocks.LIGHT_GRAY_TERRACOTTA, ModBlocks.INFESTED_LIGHT_GRAY_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(61, Blocks.LIME_TERRACOTTA, ModBlocks.INFESTED_LIME_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(62, Blocks.MAGENTA_TERRACOTTA, ModBlocks.INFESTED_MAGENTA_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(63, Blocks.ORANGE_TERRACOTTA, ModBlocks.INFESTED_ORANGE_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(64, Blocks.PINK_TERRACOTTA, ModBlocks.INFESTED_PINK_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(65, Blocks.PURPLE_TERRACOTTA, ModBlocks.INFESTED_PURPLE_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(66, Blocks.RED_TERRACOTTA, ModBlocks.INFESTED_RED_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(67, Blocks.WHITE_TERRACOTTA, ModBlocks.INFESTED_WHITE_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(68, Blocks.YELLOW_TERRACOTTA, ModBlocks.INFESTED_YELLOW_TERRACOTTA.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(69, Blocks.CRYING_OBSIDIAN, ModBlocks.INFESTED_CRYING_OBSIDIAN.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(70, Blocks.NETHERRACK, ModBlocks.INFESTED_NETHERRACK.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(71, Blocks.CRIMSON_NYLIUM, ModBlocks.INFESTED_CRIMSON_NYLIUM.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(72, Blocks.WARPED_NYLIUM, ModBlocks.INFESTED_WARPED_NYLIUM.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(73, Blocks.BLACKSTONE, ModBlocks.INFESTED_BLACKSTONE.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(74, Blocks.BLACKSTONE_STAIRS, ModBlocks.INFESTED_BLACKSTONE_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(75, Blocks.BLACKSTONE_SLAB, ModBlocks.INFESTED_BLACKSTONE_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(76, Blocks.BLACKSTONE_WALL, ModBlocks.INFESTED_BLACKSTONE_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(77, Blocks.POLISHED_BLACKSTONE_BRICKS, ModBlocks.INFESTED_BLACKSTONE_BRICKS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(78, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, ModBlocks.INFESTED_BLACKSTONE_BRICK_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(79, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, ModBlocks.INFESTED_BLACKSTONE_BRICK_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(80, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, ModBlocks.INFESTED_BLACKSTONE_BRICK_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(81, Blocks.STONE_BRICKS, ModBlocks.INFESTED_STONE_BRICKS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(82, Blocks.STONE_BRICK_STAIRS, ModBlocks.INFESTED_STONE_BRICK_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(83, Blocks.STONE_BRICK_SLAB, ModBlocks.INFESTED_STONE_BRICK_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(84, Blocks.STONE_BRICK_WALL, ModBlocks.INFESTED_STONE_BRICK_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(85, Blocks.MOSSY_STONE_BRICKS, ModBlocks.INFESTED_MOSSY_STONE_BRICKS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(86, Blocks.MOSSY_STONE_BRICK_STAIRS, ModBlocks.INFESTED_MOSSY_STONE_BRICK_STAIRS.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(87, Blocks.MOSSY_STONE_BRICK_SLAB, ModBlocks.INFESTED_MOSSY_STONE_BRICK_SLAB.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(88, Blocks.MOSSY_STONE_BRICK_WALL, ModBlocks.INFESTED_MOSSY_STONE_BRICK_WALL.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(89, Blocks.BASALT, ModBlocks.INFESTED_BASALT.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(90, Blocks.SMOOTH_BASALT, ModBlocks.INFESTED_SMOOTH_BASALT.get().defaultBlockState());
        explicitInfectableBlocks.addEntry(91, Blocks.END_STONE, ModBlocks.INFESTED_ENDSTONE.get().defaultBlockState());
        if(ModConfig.isExperimentalFeaturesEnabled()) { explicitInfectableBlocks.addEntry(92, Blocks.KELP_PLANT, ModBlocks.DISEASED_KELP_BLOCK.get().defaultBlockState()); }
        if(ModConfig.isExperimentalFeaturesEnabled()) { explicitInfectableBlocks.addEntry(93, Blocks.KELP, ModBlocks.DISEASED_KELP_BLOCK.get().defaultBlockState()); }

        // Deeper and Darker Compatibility
        explicitInfectableBlocks.addEntry(1, "minecraft:deepslate", "deeperdarker:sculk_stone");
        explicitInfectableBlocks.addEntry(2, "minecraft:cobbled_deepslate", "deeperdarker:cobbled_sculk_stone");
        explicitInfectableBlocks.addEntry(3,"minecraft:deepslate_coal_ore", "deeperdarker:sculk_stone_coal_ore");
        explicitInfectableBlocks.addEntry(4, "minecraft:deepslate_iron_ore", "deeperdarker:sculk_stone_iron_ore");
        explicitInfectableBlocks.addEntry(5, "minecraft:deepslate_gold_ore", "deeperdarker:sculk_stone_gold_ore");
        explicitInfectableBlocks.addEntry(6, "minecraft:deepslate_copper_ore", "deeperdarker:sculk_stone_copper_ore");
        explicitInfectableBlocks.addEntry(7, "minecraft:deepslate_lapis_ore", "deeperdarker:sculk_stone_lapis_ore");
        explicitInfectableBlocks.addEntry(8, "minecraft:deepslate_redstone_ore", "deeperdarker:sculk_stone_redstone_ore");
        explicitInfectableBlocks.addEntry(9, "minecraft:deepslate_emerald_ore", "deeperdarker:sculk_stone_emerald_ore");
        explicitInfectableBlocks.addEntry(10, "minecraft:deepslate_diamond_ore", "deeperdarker:sculk_stone_diamond_ore");
        explicitInfectableBlocks.addEntry(11, "minecraft:moss_block", "deeperdarker:echo_soil");
        explicitInfectableBlocks.addEntry(12, "minecraft:flowering_azalea_leaves", "deeperdarker:echo_leaves");
        explicitInfectableBlocks.addEntry(13, "minecraft:oak_sapling", "deeperdarker:echo_sapling");
        explicitInfectableBlocks.addEntry(14, "minecraft:oak_log", "deeperdarker:echo_log");
        explicitInfectableBlocks.addEntry(15, "minecraft:oak_wood", "deeperdarker:echo_wood");
        explicitInfectableBlocks.addEntry(16, "minecraft:pearlescent_froglight", "deeperdarker:sculk_gleam");

        explicitInfectableBlocks.addEntry(17, "minecraft:blackstone", "deeperdarker:gloomslate");
        explicitInfectableBlocks.addEntry(18, "minecraft:blackstone", "deeperdarker:cobbled_gloomslate");
        explicitInfectableBlocks.addEntry(19, "minecraft:deepslate_coal_ore", "deeperdarker:gloomslate_coal_ore");
        explicitInfectableBlocks.addEntry(20, "minecraft:deepslate_iron_ore", "deeperdarker:gloomslate_iron_ore");
        explicitInfectableBlocks.addEntry(21, "minecraft:deepslate_gold_ore", "deeperdarker:gloomslate_gold_ore");
        explicitInfectableBlocks.addEntry(22, "minecraft:deepslate_copper_ore", "deeperdarker:gloomslate_copper_ore");
        explicitInfectableBlocks.addEntry(23, "minecraft:deepslate_lapis_ore", "deeperdarker:gloomslate_lapis_ore");
        explicitInfectableBlocks.addEntry(24, "minecraft:deepslate_redstone_ore", "deeperdarker:gloomslate_redstone_ore");
        explicitInfectableBlocks.addEntry(25, "minecraft:deepslate_emerald_ore", "deeperdarker:gloomslate_emerald_ore");
        explicitInfectableBlocks.addEntry(26, "minecraft:deepslate_diamond_ore", "deeperdarker:gloomslate_diamond_ore");
        explicitInfectableBlocks.addEntry(27, "minecraft:lava", "deeperdarker:gloomy_geyser");
        explicitInfectableBlocks.addEntry(28, "minecraft:smooth_basalt", "deeperdarker:gloomy_sculk");
        explicitInfectableBlocks.addEntry(29, "minecraft:ochre_froglight", "deeperdarker:crystallized_amber");

        explicitInfectableBlocks.addEntry(30, "minecraft:mud", "deeperdarker:sculk_grime");
        explicitInfectableBlocks.addEntry(31, "minecraft:air", "deeperdarker:sculk_tendrils");
        explicitInfectableBlocks.addEntry(32, "minecraft:mud", "deeperdarker:sculk_jaw");

        explicitInfectableBlocks.addEntry(33, "minecraft:moss_block", "deeperdarker:blooming_moss_block");
        explicitInfectableBlocks.addEntry(34, "minecraft:grass_block", "deeperdarker:blooming_sculk_stone");
        explicitInfectableBlocks.addEntry(35, "minecraft:cave_vines", "deeperdarker:glowing_vines");
        explicitInfectableBlocks.addEntry(36, "minecraft:cave_vines", "deeperdarker:glowing_vines_plant");
        explicitInfectableBlocks.addEntry(37, "minecraft:air", "deeperdarker:glowing_roots");
        explicitInfectableBlocks.addEntry(38, "minecraft:air", "deeperdarker:glowing_flowers");
        explicitInfectableBlocks.addEntry(39, "minecraft:cave_vines", "deeperdarker:sculk_vines");
        explicitInfectableBlocks.addEntry(40, "minecraft:torchflower", "deeperdarker:lily_flower");
        explicitInfectableBlocks.addEntry(41, "minecraft:lily_pad", "deeperdarker:ice_lily");

        // Remove Sculk Vein
        explicitInfectableBlocks.addEntry(0, "minecraft:air", "minecraft:sculk_vein");
        explicitInfectableBlocks.addEntry(0, "minecraft:air", "sculkhorde:tendrils");



        // Used to infect non-full blocks. Priority Matters
        tagInfectableNonFullBlocks = new BlockInfestationTable(1, false);
        tagInfectableNonFullBlocks.addEntry(1, BlockTags.STAIRS, BlockTags.MINEABLE_WITH_AXE, Tiers.IRON, ModBlocks.INFESTED_WOOD_STAIRS.get(), Blocks.OAK_STAIRS);
        tagInfectableNonFullBlocks.addEntry(2, BlockTags.STAIRS, BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_STAIRS.get(), Blocks.COBBLESTONE_STAIRS);
        tagInfectableNonFullBlocks.addEntry(3, BlockTags.STAIRS, ModBlocks.BlockTags.CONVERTS_TO_CRUMBLING_VARIANT, Tiers.IRON, ModBlocks.INFESTED_CRUMBLING_STAIRS.get(), Blocks.MOSSY_COBBLESTONE_STAIRS); //vanilla doesn't have any stairs that match these criteria so this is about the best i could get
        tagInfectableNonFullBlocks.addEntry(4, BlockTags.SLABS, BlockTags.MINEABLE_WITH_AXE, Tiers.IRON, ModBlocks.INFESTED_WOOD_SLAB.get(), Blocks.OAK_SLAB);
        tagInfectableNonFullBlocks.addEntry(5, BlockTags.SLABS, BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_SLAB.get(), Blocks.COBBLESTONE_SLAB);
        tagInfectableNonFullBlocks.addEntry(6, BlockTags.SLABS, ModBlocks.BlockTags.CONVERTS_TO_CRUMBLING_VARIANT, Tiers.IRON, ModBlocks.INFESTED_CRUMBLING_SLAB.get(), Blocks.MOSSY_COBBLESTONE_SLAB);
        tagInfectableNonFullBlocks.addEntry(7, BlockTags.WALLS, ModBlocks.BlockTags.CONVERTS_TO_CRUMBLING_VARIANT, Tiers.IRON, ModBlocks.INFESTED_CRUMBLING_WALL.get(), Blocks.MOSSY_COBBLESTONE_WALL);
        tagInfectableNonFullBlocks.addEntry(8, BlockTags.WALLS, Tiers.IRON, ModBlocks.INFESTED_STURDY_WALL.get(), Blocks.COBBLESTONE_WALL);
        tagInfectableNonFullBlocks.addEntry(9, BlockTags.WOODEN_FENCES, ModBlocks.INFESTED_WOOD_FENCE.get(), Blocks.OAK_FENCE);
        tagInfectableNonFullBlocks.addEntry(10, BlockTags.FENCES, ModBlocks.INFESTED_STURDY_FENCE.get(), Blocks.NETHER_BRICK_FENCE);
        tagInfectableNonFullBlocks.addEntry(11, BlockTags.FENCE_GATES, BlockTags.MINEABLE_WITH_AXE, Tiers.IRON, ModBlocks.INFESTED_WOOD_FENCE_GATE.get(), Blocks.OAK_FENCE_GATE);
        tagInfectableNonFullBlocks.addEntry(12, BlockTags.FENCE_GATES, ModBlocks.INFESTED_STURDY_FENCE_GATE.get(), Blocks.OAK_FENCE_GATE);

        // Used to infect generic types of blocks like wood-like, stone-like, etc. Priority Matters
        tagInfectableBlocks = new BlockInfestationTable(2, true);
        tagInfectableBlocks.addEntry(1, net.minecraft.tags.BlockTags.LOGS, ModBlocks.INFESTED_LOG.get(), Blocks.OAK_LOG);
        tagInfectableBlocks.addEntry(2, BlockTags.MINEABLE_WITH_AXE, ModBlocks.INFESTED_WOOD_MASS.get(), Blocks.OAK_PLANKS);
        tagInfectableBlocks.addEntry(3, BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_MASS.get(), Blocks.COBBLESTONE);
        tagInfectableBlocks.addEntry(4, BlockTags.MINEABLE_WITH_SHOVEL, Tiers.IRON, ModBlocks.INFESTED_CRUMPLED_MASS.get(), Blocks.COARSE_DIRT);
        tagInfectableBlocks.addEntry(5, BlockTags.MINEABLE_WITH_HOE, Tiers.IRON, ModBlocks.INFESTED_COMPOST_MASS.get(), Blocks.MOSS_BLOCK);

        configInfectableBlocks = new BlockInfestationTable(3, false);
        configInfectableBlocks.addEntry(ModBlocks.INFESTED_STURDY_MASS.get());

        BlockInfestationAPI.addBlockInfestationTable(explicitInfectableBlocks);
        BlockInfestationAPI.addBlockInfestationTable(tagInfectableNonFullBlocks);
        BlockInfestationAPI.addBlockInfestationTable(tagInfectableBlocks);
        BlockInfestationAPI.addBlockInfestationTable(configInfectableBlocks);
    }

    public static boolean isExplicitlyNotInfectable(BlockState blockState)
    {
        return blockState.is(ModBlocks.BlockTags.NOT_INFESTABLE) ||
                blockState.is(ModBlocks.BlockTags.INFESTED_BLOCK) ||
                blockState.isAir() ||
                blockState.hasBlockEntity();
    }

    public static boolean isInfectable(ServerLevel level, BlockPos pos)
    {
        BlockState blockState = level.getBlockState(pos);
        if(isExplicitlyNotInfectable(blockState))
        {
            return false;
        }

        for(BlockInfestationTable table : INFESTATION_TABLES)
        {
            if(table.canBeInfectedByThisTable(level, pos))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isCurable(ServerLevel level, BlockPos pos)
    {
        for(BlockInfestationTable table : INFESTATION_TABLES)
        {
            if(table.getNormalVariant(level, pos) != null)
            {
                return true;
            }
        }

        return false;
    }

    public static void tryToInfestBlock(ServerLevel world, BlockPos targetPos)
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return;
        }

        BlockState victimBlockState = world.getBlockState(targetPos);
        boolean wasAbleToInfestBlock = false;

        if(isExplicitlyNotInfectable(victimBlockState))
        {
            return;
        }

        for(BlockInfestationTable table : INFESTATION_TABLES)
        {
            if(table.canBeInfectedByThisTable(world, targetPos))
            {
                wasAbleToInfestBlock = table.infectBlock(world, targetPos);
                break;
            }
        }


        // If we did not successfully infect the block, return
        if(!wasAbleToInfestBlock)
        {
            return;
        }

        world.sendParticles(ParticleTypes.SCULK_CHARGE_POP, targetPos.getX() + 0.5D, targetPos.getY() + 1.15D, targetPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        world.playSound(null, targetPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);

        BlockInfestationSystem.removeNearbyVein(world, targetPos);

        BlockInfestationSystem.placeSculkFlora(world, targetPos);

        // Chance to place a sculk node above the block
        SculkNodeBlock.tryPlaceSculkNode(world, targetPos, false);

        // Chance to place a sculk bee hive above the block
        if(!ModConfig.isExperimentalFeaturesEnabled()) {
            BlockInfestationSystem.tryPlaceSculkBeeHive(world, targetPos.above());
        }

        BlockInfestationSystem.tryPlaceDiseasedKelp(world, targetPos.above());
    }

    public static boolean tryToCureBlock(ServerLevel world, BlockPos targetPos)
    {
        boolean wasAbleToCureBlock = false;
        BlockState getNormalVariant = null;

        if(!isCurable(world, targetPos)) { return false; }

        for(BlockInfestationTable table : INFESTATION_TABLES)
        {
            getNormalVariant = table.getNormalVariant(world, targetPos);

            if(getNormalVariant == null) { continue; }

            wasAbleToCureBlock = true;

            break;
        }

        // If we did not successfully cure the block, return
        if(!wasAbleToCureBlock)
        {
            return false;
        }

        // Convert Block
        world.setBlockAndUpdate(targetPos, getNormalVariant);

        if(shouldBeRemovedFromAboveBlock.test(world.getBlockState(targetPos.above())))
        {
            world.setBlockAndUpdate(targetPos.above(), Blocks.AIR.defaultBlockState());
        }

        boolean canCuredBlockSustatinPlant = world.getBlockState(targetPos).canSustainPlant(world, targetPos, Direction.UP, (IPlantable) Blocks.POPPY);
        Random rand = new Random();
        if(rand.nextBoolean() && canCuredBlockSustatinPlant && world.getBlockState(targetPos.above()).isAir())
        {
            world.setBlockAndUpdate(targetPos.above(), Blocks.GRASS.defaultBlockState());
        }

        return true;
    }

    /**
     * Determines if a blockstate is considered to be sculk Flora
     * @return True if Valid, False otherwise
     */
    public static Predicate<BlockState> shouldBeRemovedFromAboveBlock = (b) ->
    {
        if (b.is(ModBlocks.GRASS.get()))
        {
            return true;
        }

        if(b.is(ModBlocks.GRASS_SHORT.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SMALL_SHROOM.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SCULK_SHROOM_CULTURE.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SPIKE.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SCULK_SUMMONER_BLOCK.get()))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_CATALYST))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_SHRIEKER))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_VEIN))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_SENSOR))
        {
            return true;
        }

        if(b.is(ModBlocks.TENDRILS.get()))
        {
            return true;
        }

        return false;
    };

    public static void removeNearbyVein(ServerLevel world, BlockPos position)
    {
        // Update each adjacent block if it is a sculk vein
        // This is to prevent vein from staying on blocks that it does not belong on.
        List<BlockPos> adjacentBlockPos = BlockAlgorithms.getAdjacentNeighbors(position);
        for(BlockPos neighbors : adjacentBlockPos)
        {
            BlockState blockState = world.getBlockState(neighbors);
            if(blockState.getBlock() == ModBlocks.TENDRILS.get())
            {
                if(!blockState.getBlock().canSurvive(blockState, world, neighbors))
                    world.destroyBlock(neighbors, false);

            }
        }
    }

    public static void placeSculkFlora(ServerLevel world, BlockPos position)
    {
        // Given a 25% chance, place down sculk flora on block
        if (world.random.nextInt(4) <= 0)
        {
            BlockAlgorithms.tryPlaceSculkFlora(position.above(), world);
        }
    }

    public static boolean blockIsAirOrSnow(BlockState state)
    {
        return state.isAir() || state.getBlock() == Blocks.SNOW;
    }

    /**
     * Will only place Sculk Bee Hives
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */
    public static void tryPlaceSculkBeeHive(ServerLevel world, BlockPos targetPos)
    {

        //Given random chance and the target location can see the sky, create a sculk hive
        if(new Random().nextInt(4000) <= 1 && blockIsAirOrSnow(world.getBlockState(targetPos)) && blockIsAirOrSnow(world.getBlockState(targetPos.above())) && blockIsAirOrSnow(world.getBlockState(targetPos.above().above())))
        {
            world.setBlockAndUpdate(targetPos, ModBlocks.SCULK_BEE_NEST_BLOCK.get().defaultBlockState());
            SculkBeeNestBlockEntity nest = (SculkBeeNestBlockEntity) world.getBlockEntity(targetPos);

            //Add bees
            nest.addFreshInfectorOccupant();
            nest.addFreshInfectorOccupant();
            nest.addFreshHarvesterOccupant();
            nest.addFreshHarvesterOccupant();
        }

    }

    /**
     * Will only place Sculk Bee Hives
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */
    public static void tryPlaceDiseasedKelp(ServerLevel world, BlockPos targetPos)
    {

        //Given random chance and the target location can see the sky, create a sculk hive
        if(ModConfig.isExperimentalFeaturesEnabled() && new Random().nextInt(30) <= 1 && world.getFluidState(targetPos).is(Fluids.WATER))
        {
            boolean isTargetPosEmptyWater = world.getBlockState(targetPos).is(Blocks.WATER);
            if(!isTargetPosEmptyWater) { return; }


            int height = world.random.nextInt(25);
            for(int i = 0; i < height && !world.getBlockState(targetPos.above(i + 1)).getFluidState().isEmpty(); i++)
            {
                world.setBlockAndUpdate(targetPos.above(i), ModBlocks.DISEASED_KELP_BLOCK.get().defaultBlockState());
            }
        }

    }
}
