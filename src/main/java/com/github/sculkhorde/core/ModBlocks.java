package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SculkHorde.MOD_ID);
	public static final List<Pair<RegistryObject<? extends Block>, ResourceLocation>> BLOCKS_TO_DATAGEN = new ArrayList<>();

	//Method to Register Blocks & Register them as items
	private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block)
	{
		RegistryObject<T> toReturn = BLOCKS.register(name, block);
		registerBlockItem(name, toReturn);
		return toReturn;
	}

	//helper method to register a given block as a holdable item
	private static void registerBlockItem(String name, RegistryObject<? extends Block> block)
	{
		ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
				new Item.Properties()));
	}

	//simple methods to quickly register stairs
	private static RegistryObject<StairBlock> stairs(RegistryObject<Block> original) {
		return stairs(original.getId().getPath(), original);
	}

	private static RegistryObject<StairBlock> stairs(String id, RegistryObject<Block> original) {
		return registerBlock(id + "_stairs", () -> new StairBlock(() -> StairBlock.stateById(0), BlockBehaviour.Properties.copy(original.get())));
	}

	//simple methods to quickly register slabs
	private static RegistryObject<SlabBlock> slab(RegistryObject<Block> original) {
		return slab(original.getId().getPath(), original);
	}

	private static RegistryObject<SlabBlock> slab(String id, RegistryObject<Block> original) {
		return registerBlock(id + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(original.get())));
	}

	//simple methods to quickly register walls
	private static RegistryObject<WallBlock> wall(RegistryObject<Block> original) {
		return wall(original.getId().getPath(), original);
	}

	private static RegistryObject<WallBlock> wall(String id, RegistryObject<Block> original) {
		return wall(id, original, original.getId());
	}

	private static RegistryObject<WallBlock> wall(String id, RegistryObject<Block> original, ResourceLocation texture) {
		RegistryObject<WallBlock> wall = noDatagenWall(id, original);
		datagen(wall, texture); //the datagen methods aren't part of registerBlock bc i didn't want to have to go back and change everything to use the new system
		return wall; //but since i did datagen before starting walls it CAN be a part of this method
	}

	private static RegistryObject<WallBlock> noDatagenWall(String id, RegistryObject<Block> original) { //oops i was wrong :(
		return registerBlock(id + "_wall", () -> new WallBlock(BlockBehaviour.Properties.copy(original.get()).forceSolidOn()));
	}

	private static RegistryObject<WallBlock> noDatagenWall(RegistryObject<Block> original) {
		return noDatagenWall(original.getId().getPath(), original);
	}

	//methods to add blocks to datagen
	private static void datagen(RegistryObject<? extends Block> block, ResourceLocation textureId) {
		BLOCKS_TO_DATAGEN.add(new Pair<>(block, textureId));
	}

	private static void datagen(RegistryObject<? extends Block> block, String textureId) {
		datagen(block, new ResourceLocation(SculkHorde.MOD_ID, textureId));
	}

	private static void datagen(RegistryObject<? extends Block> block) {
		datagen(block, block.getId());
	}

	//NOTE: Learned from https://www.youtube.com/watch?v=4igJ_nsFAZs "Creating a Block - Minecraft Forge 1.16.4 Modding Tutorial"

	//Register Ancient Large Bricks
	public static final RegistryObject<Block> ANCIENT_LARGE_BRICKS =
			registerBlock("ancient_large_bricks", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)
					.requiresCorrectToolForDrops()
					.destroyTime(10f)
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	//Ancient Large Tile
	public static final RegistryObject<Block> ANCIENT_LARGE_TILE =
			registerBlock("ancient_large_tile", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.destroyTime(10f)
					.sound(SoundType.ANCIENT_DEBRIS)
			));
	public static final RegistryObject<SculkArachnoidBlock> SCULK_ARACHNOID =
			registerBlock("sculk_arachnoid", SculkArachnoidBlock::new);
	public static final RegistryObject<SculkDuraMatterBlock> SCULK_DURA_MATTER =
			registerBlock("sculk_dura_matter", SculkDuraMatterBlock::new);


	public static final RegistryObject<Block> CALCITE_ORE =
			registerBlock("calcite_ore", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			)
			{
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
					if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.calcite_ore.functionality"));
					}
					else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.calcite_ore.lore"));
					}
					else
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
					}
				}
			});

	public static final RegistryObject<Block> INFESTED_STONE =
			registerBlock("infested_stone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	public static final RegistryObject<StairBlock> INFESTED_STONE_STAIRS =
			stairs(INFESTED_STONE);

	public static final RegistryObject<SlabBlock> INFESTED_STONE_SLAB =
			slab(INFESTED_STONE);

	public static final RegistryObject<InfestedPillarBlock> INFESTED_LOG =
			registerBlock("infested_log", () -> new InfestedPillarBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			)
			{
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
					if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infested_log.functionality"));
					}
					else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infested_log.lore"));
					}
					else
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
					}
				}
			});

	public static final RegistryObject<Block> INFESTED_SAND =
			registerBlock("infested_sand", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.SAND)
			));

	public static final RegistryObject<Block> INFESTED_RED_SAND =
			registerBlock("infested_red_sand", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.SAND)
			));

	public static final RegistryObject<Block> INFESTED_DEEPSLATE =
			registerBlock("infested_deepslate", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.DEEPSLATE)
			));

	public static final RegistryObject<Block> INFESTED_SANDSTONE =
			registerBlock("infested_sandstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_SANDSTONE_STAIRS =
			stairs(INFESTED_SANDSTONE);

	public static final RegistryObject<SlabBlock> INFESTED_SANDSTONE_SLAB =
			slab(INFESTED_SANDSTONE);

	public static final RegistryObject<WallBlock> INFESTED_SANDSTONE_WALL =
			wall(INFESTED_SANDSTONE);

	public static final RegistryObject<Block> INFESTED_DIORITE =
			registerBlock("infested_diorite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_DIORITE_STAIRS =
			stairs(INFESTED_DIORITE);

	public static final RegistryObject<SlabBlock> INFESTED_DIORITE_SLAB =
			slab(INFESTED_DIORITE);

	public static final RegistryObject<WallBlock> INFESTED_DIORITE_WALL =
			wall(INFESTED_DIORITE);

	public static final RegistryObject<Block> INFESTED_GRANITE =
			registerBlock("infested_granite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_GRANITE_STAIRS =
			stairs(INFESTED_GRANITE);

	public static final RegistryObject<SlabBlock> INFESTED_GRANITE_SLAB =
			slab(INFESTED_GRANITE);

	public static final RegistryObject<WallBlock> INFESTED_GRANITE_WALL =
			wall(INFESTED_GRANITE);

	public static final RegistryObject<Block> INFESTED_ANDESITE =
			registerBlock("infested_andesite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_ANDESITE_STAIRS =
			stairs(INFESTED_ANDESITE);

	public static final RegistryObject<SlabBlock> INFESTED_ANDESITE_SLAB =
			slab(INFESTED_ANDESITE);

	public static final RegistryObject<WallBlock> INFESTED_ANDESITE_WALL =
			wall(INFESTED_ANDESITE);

	public static final RegistryObject<Block> INFESTED_TUFF =
			registerBlock("infested_tuff", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CALCITE =
			registerBlock("infested_calcite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.CALCITE)
			));

	public static final RegistryObject<Block> INFESTED_COBBLED_DEEPSLATE =
			registerBlock("infested_cobbled_deepslate", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_COBBLED_DEEPSLATE_STAIRS =
			stairs(INFESTED_COBBLED_DEEPSLATE);

	public static final RegistryObject<SlabBlock> INFESTED_COBBLED_DEEPSLATE_SLAB =
			slab(INFESTED_COBBLED_DEEPSLATE);

	public static final RegistryObject<WallBlock> INFESTED_COBBLED_DEEPSLATE_WALL =
			wall(INFESTED_COBBLED_DEEPSLATE);

	public static final RegistryObject<Block> INFESTED_GRAVEL =
			registerBlock("infested_gravel", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<Block> INFESTED_MOSS =
			registerBlock("infested_moss", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.GRASS)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MOSS)
			));

	public static final RegistryObject<Block> INFESTED_SNOW =
			registerBlock("infested_snow", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.SNOW)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.SNOW)
			));

	public static final RegistryObject<Block> INFESTED_TERRACOTTA =
			registerBlock("infested_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_ORANGE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BLACK_TERRACOTTA =
			registerBlock("infested_black_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BLUE_TERRACOTTA =
			registerBlock("infested_blue_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BROWN_TERRACOTTA =
			registerBlock("infested_brown_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CYAN_TERRACOTTA =
			registerBlock("infested_cyan_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRAY_TERRACOTTA =
			registerBlock("infested_gray_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GREEN_TERRACOTTA =
			registerBlock("infested_green_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_GREEN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIGHT_BLUE_TERRACOTTA =
			registerBlock("infested_light_blue_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIGHT_GRAY_TERRACOTTA =
			registerBlock("infested_light_gray_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIME_TERRACOTTA =
			registerBlock("infested_lime_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_MAGENTA_TERRACOTTA =
			registerBlock("infested_magenta_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_MAGENTA)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ORANGE_TERRACOTTA =
			registerBlock("infested_orange_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_ORANGE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_PINK_TERRACOTTA =
			registerBlock("infested_pink_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_PINK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_PURPLE_TERRACOTTA =
			registerBlock("infested_purple_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_PURPLE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_RED_TERRACOTTA =
			registerBlock("infested_red_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_RED)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_WHITE_TERRACOTTA =
			registerBlock("infested_white_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_YELLOW_TERRACOTTA =
			registerBlock("infested_yellow_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_COBBLESTONE =
			registerBlock("infested_cobblestone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_COBBLESTONE_STAIRS =
			stairs(INFESTED_COBBLESTONE);

	public static final RegistryObject<SlabBlock> INFESTED_COBBLESTONE_SLAB =
			slab(INFESTED_COBBLESTONE);

	public static final RegistryObject<WallBlock> INFESTED_COBBLESTONE_WALL =
			wall(INFESTED_COBBLESTONE);

	public static final RegistryObject<Block> INFESTED_CRYING_OBSIDIAN =
			registerBlock("infested_crying_obsidian", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
					.explosionResistance(1200f)
					.destroyTime(50f)
			));

	public static final RegistryObject<Block> INFESTED_MUD =
			registerBlock("infested_mud", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MUD)
			));

	public static final RegistryObject<Block> INFESTED_PACKED_MUD =
			registerBlock("infested_packed_mud", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.DIRT)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.PACKED_MUD)
			));

	public static final RegistryObject<Block> INFESTED_MUD_BRICKS =
			registerBlock("infested_mud_bricks", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.DIRT)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MUD_BRICKS)
			));

	public static final RegistryObject<StairBlock> INFESTED_MUD_BRICK_STAIRS =
			stairs("infested_mud_brick", INFESTED_MUD_BRICKS);

	public static final RegistryObject<SlabBlock> INFESTED_MUD_BRICK_SLAB =
			slab("infested_mud_brick", INFESTED_MUD_BRICKS);

	public static final RegistryObject<WallBlock> INFESTED_MUD_BRICK_WALL =
			wall("infested_mud_brick", INFESTED_MUD_BRICKS);

	public static final RegistryObject<Block> INFESTED_BLACKSTONE =
			registerBlock("infested_blackstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_BLACKSTONE_STAIRS =
			stairs(INFESTED_BLACKSTONE);

	public static final RegistryObject<SlabBlock> INFESTED_BLACKSTONE_SLAB =
			slab(INFESTED_BLACKSTONE);

	public static final RegistryObject<WallBlock> INFESTED_BLACKSTONE_WALL =
			noDatagenWall(INFESTED_BLACKSTONE);

	public static final RegistryObject<Block> INFESTED_BASALT =
			registerBlock("infested_basalt", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_SMOOTH_BASALT =
			registerBlock("infested_smooth_basalt", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ENDSTONE =
			registerBlock("infested_endstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_YELLOW)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_NETHERRACK =
			registerBlock("infested_netherrack", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.NETHER)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CRIMSON_NYLIUM =
			registerBlock("infested_crimson_nylium", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.NETHER)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_WARPED_NYLIUM =
			registerBlock("infested_warped_nylium", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.NETHER)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_MOSSY_COBBLESTONE =
			registerBlock("infested_mossy_cobblestone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor. STONE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_MOSSY_COBBLESTONE_STAIRS =
			stairs(INFESTED_MOSSY_COBBLESTONE);

	public static final RegistryObject<SlabBlock> INFESTED_MOSSY_COBBLESTONE_SLAB =
			slab(INFESTED_MOSSY_COBBLESTONE);

	public static final RegistryObject<WallBlock> INFESTED_MOSSY_COBBLESTONE_WALL =
			wall("infested_mossy_cobblestone", INFESTED_MOSSY_COBBLESTONE, new ResourceLocation(SculkHorde.MOD_ID, "infested_cobblestone"));

	public static final RegistryObject<Block> INFESTED_CLAY =
			registerBlock("infested_clay", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor. CLAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MUD)
			));

	public static final RegistryObject<Block> INFESTED_STONE_BRICKS =
			registerBlock("infested_stone_bricks", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_STONE_BRICK_STAIRS =
			stairs("infested_stone_brick", INFESTED_STONE_BRICKS);

	public static final RegistryObject<SlabBlock> INFESTED_STONE_BRICK_SLAB =
			slab("infested_stone_brick", INFESTED_STONE_BRICKS);

	public static final RegistryObject<WallBlock> INFESTED_STONE_BRICK_WALL =
			wall("infested_stone_brick", INFESTED_STONE_BRICKS);

	public static final RegistryObject<Block> INFESTED_MOSSY_STONE_BRICKS =
			registerBlock("infested_mossy_stone_bricks", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_MOSSY_STONE_BRICK_STAIRS =
			stairs("infested_mossy_stone_brick", INFESTED_STONE_BRICKS);

	public static final RegistryObject<SlabBlock> INFESTED_MOSSY_STONE_BRICK_SLAB =
			slab("infested_mossy_stone_brick", INFESTED_STONE_BRICKS);

	public static final RegistryObject<WallBlock> INFESTED_MOSSY_STONE_BRICK_WALL =
			wall("infested_mossy_stone_brick", INFESTED_MOSSY_STONE_BRICKS, new ResourceLocation(SculkHorde.MOD_ID, "infested_stone_bricks"));

	public static final RegistryObject<Block> INFESTED_BLACKSTONE_BRICKS =
			registerBlock("infested_blackstone_bricks", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<StairBlock> INFESTED_BLACKSTONE_BRICK_STAIRS =
			stairs("infested_blackstone_brick", INFESTED_STONE_BRICKS);

	public static final RegistryObject<SlabBlock> INFESTED_BLACKSTONE_BRICK_SLAB =
			slab("infested_blackstone_brick", INFESTED_STONE_BRICKS);

	public static final RegistryObject<WallBlock> INFESTED_BLACKSTONE_BRICK_WALL =
			wall("infested_blackstone_brick", INFESTED_BLACKSTONE_BRICKS);

	public static final RegistryObject<InfestedTagBlock> INFESTED_WOOD_MASS =
			registerBlock("infested_wood_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			));

	public static final RegistryObject<InfestedStairBlock> INFESTED_WOOD_STAIRS =
			registerBlock("infested_wood_stairs", () -> new InfestedStairBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			));

	public static final RegistryObject<InfestedSlabBlock> INFESTED_WOOD_SLAB =
			registerBlock("infested_wood_slab", () -> new InfestedSlabBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			));

	public static final RegistryObject<InfestedFenceBlock> INFESTED_WOOD_FENCE =
			registerBlock("infested_wood_fence", () -> new InfestedFenceBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			));

	public static final RegistryObject<InfestedFenceGateBlock> INFESTED_WOOD_FENCE_GATE =
			registerBlock("infested_wood_fence_gate", () -> new InfestedFenceGateBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_STURDY_MASS =
			registerBlock("infested_sturdy_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<InfestedStairBlock> INFESTED_STURDY_STAIRS =
			registerBlock("infested_sturdy_stairs", () -> new InfestedStairBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<InfestedSlabBlock> INFESTED_STURDY_SLAB =
			registerBlock("infested_sturdy_slab", () -> new InfestedSlabBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<InfestedWallBlock> INFESTED_STURDY_WALL =
			registerBlock("infested_sturdy_wall", () -> new InfestedWallBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<InfestedFenceBlock> INFESTED_STURDY_FENCE =
			registerBlock("infested_sturdy_fence", () -> new InfestedFenceBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<InfestedFenceGateBlock> INFESTED_STURDY_FENCE_GATE =
			registerBlock("infested_sturdy_fence_gate", () -> new InfestedFenceGateBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<InfestedStairBlock> INFESTED_CRUMBLING_STAIRS =
			registerBlock("infested_crumbling_stairs", () -> new InfestedStairBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<InfestedSlabBlock> INFESTED_CRUMBLING_SLAB =
			registerBlock("infested_crumbling_slab", () -> new InfestedSlabBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<InfestedWallBlock> INFESTED_CRUMBLING_WALL =
			registerBlock("infested_crumbling_wall", () -> new InfestedWallBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_CRUMPLED_MASS =
			registerBlock("infested_crumpled_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_COMPOST_MASS =
			registerBlock("infested_compost_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MOSS)
			));


	public static final RegistryObject<Block> INFESTATION_WARD_BLOCK =
			registerBlock("infestation_ward_block", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_YELLOW)
					.sound(SoundType.AMETHYST)
			)
			{
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
					if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infestation_ward_block.functionality"));
					}
					else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infestation_ward_block.lore"));
					}
					else
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
					}
				}
			});

	public static final RegistryObject<SpikeBlock> SPIKE =
			registerBlock("spike", SpikeBlock::new);

	public static final RegistryObject<SmallShroomBlock> SMALL_SHROOM =
			registerBlock("small_shroom", SmallShroomBlock::new);

	public static final RegistryObject<SculkFloraBlock> GRASS =
			registerBlock("grass", SculkFloraBlock::new);

	public static final RegistryObject<SculkFloraBlock> GRASS_SHORT =
			registerBlock("grass_short", SculkFloraBlock::new);

	public static final RegistryObject<SculkShroomCultureBlock> SCULK_SHROOM_CULTURE =
			registerBlock("sculk_shroom_culture", SculkShroomCultureBlock::new);

	public static final RegistryObject<SculkMassBlock> SCULK_MASS =
			registerBlock("sculk_mass", SculkMassBlock::new);

	public static final RegistryObject<TendrilsBlock> TENDRILS =
			registerBlock("tendrils", TendrilsBlock::new);

	public static final RegistryObject<SculkNodeBlock> SCULK_NODE_BLOCK =
			registerBlock("sculk_node", SculkNodeBlock::new);

	public static final RegistryObject<SculkAncientNodeBlock> SCULK_ANCIENT_NODE_BLOCK =
			registerBlock("sculk_ancient_node", SculkAncientNodeBlock::new);

	public static final RegistryObject<SculkBeeNestBlock> SCULK_BEE_NEST_BLOCK =
			registerBlock("sculk_bee_nest", SculkBeeNestBlock::new);

	public static final RegistryObject<SculkBeeNestCellBlock> SCULK_BEE_NEST_CELL_BLOCK =
			registerBlock("sculk_bee_nest_cell", SculkBeeNestCellBlock::new);

	public static final RegistryObject<SculkSummonerBlock> SCULK_SUMMONER_BLOCK =
			registerBlock("sculk_summoner", SculkSummonerBlock::new);

	public static final RegistryObject<SculkLivingRockBlock> SCULK_LIVING_ROCK_BLOCK =
			registerBlock("sculk_living_rock", SculkLivingRockBlock::new);

	public static final RegistryObject<SculkLivingRockRootBlock> SCULK_LIVING_ROCK_ROOT_BLOCK =
			registerBlock("sculk_living_rock_root", SculkLivingRockRootBlock::new);

	public static final RegistryObject<DevStructureTesterBlock> DEV_STRUCTURE_TESTER_BLOCK =
			registerBlock("dev_structure_tester", DevStructureTesterBlock::new);

	public static final RegistryObject<DevMassInfectinator3000Block> DEV_MASS_INFECTINATOR_3000_BLOCK =
			registerBlock("dev_mass_infectinator_3000", DevMassInfectinator3000Block::new);

	public static final RegistryObject<SoulHarvesterBlock> SOUL_HARVESTER_BLOCK =
			registerBlock("soul_harvester", SoulHarvesterBlock::new);

	public static final RegistryObject<FleshyCompostBlock> PASTY_ORGANIC_MASS =
			registerBlock("fleshy_compost_block", FleshyCompostBlock::new);

	public static final RegistryObject<DiseasedKelpBlock> DISEASED_KELP_BLOCK =
			registerBlock("diseased_kelp_block", DiseasedKelpBlock::new);

	public static final RegistryObject<SouliteBlock> SOULITE_BLOCK =
			registerBlock("soulite_block", SouliteBlock::new);

	public static final RegistryObject<SouliteClusterBlock> SOULITE_CLUSTER_BLOCK =
			registerBlock("soulite_cluster", SouliteClusterBlock::new);

	public static final RegistryObject<SouliteBudBlock> SOULITE_BUD_BLOCK =
			registerBlock("soulite_bud_block", SouliteBudBlock::new);
	public static final RegistryObject<DepletedSouliteBlock> DEPLETED_SOULITE_BLOCK =
			registerBlock("depleted_soulite_block", DepletedSouliteBlock::new);

	public static final RegistryObject<BuddingSouliteBlock> BUDDING_SOULITE_BLOCK =
			registerBlock("budding_soulite_block", BuddingSouliteBlock::new);
	public static final RegistryObject<StructureOriginBlock> STRUCTURE_ORIGIN_BLOCK =
			registerBlock("structure_origin_block", StructureOriginBlock::new);

	public static final RegistryObject<StructureCoreBlock> STRUCTURE_CORE_BLOCK =
			registerBlock("structure_core_block", StructureCoreBlock::new);

	public static final RegistryObject<SouliteCoreBlock> SOULITE_CORE_BLOCK =
			registerBlock("soulite_core_block", SouliteCoreBlock::new);

	public static final RegistryObject<FungalShroomCoreBlock> FUNGAL_SHROOM_CORE_BLOCK =
			registerBlock("fungal_shroom_core_block", FungalShroomCoreBlock::new);

	public static final RegistryObject<Block> FUNGAL_SCULK_STEM_BLOCK =
			registerBlock("fungal_sculk_stem_block", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> FUNGAL_SCULK_BLOCK =
			registerBlock("fungal_sculk_block", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<TendrilCoreBlock> TENDRIL_CORE_BLOCK =
			registerBlock("tendril_core_block", TendrilCoreBlock::new);

	public static final RegistryObject<GolemOfWrathAnimatorBlock> GOLEM_OF_WRATH_ANIMATOR_BLOCK =
			registerBlock("golem_of_wrath_animator_block", GolemOfWrathAnimatorBlock::new);

	static {
		datagen(INFESTED_STONE_BRICKS);
		datagen(INFESTED_MOSSY_STONE_BRICKS, "infested_stone_bricks");
		datagen(INFESTED_BLACKSTONE_BRICKS);
		datagen(INFESTED_STONE_BRICK_STAIRS, "infested_stone_bricks");
		datagen(INFESTED_MOSSY_STONE_BRICK_STAIRS, "infested_stone_bricks");
		datagen(INFESTED_BLACKSTONE_BRICK_STAIRS, "infested_blackstone_bricks");
		datagen(INFESTED_STONE_BRICK_SLAB, "infested_stone_bricks");
		datagen(INFESTED_MOSSY_STONE_BRICK_SLAB, "infested_stone_bricks");
		datagen(INFESTED_BLACKSTONE_BRICK_SLAB, "infested_blackstone_bricks");
		datagen(INFESTED_STURDY_WALL, "infested_sturdy_mass");
		datagen(INFESTED_CRUMBLING_WALL, "infested_crumpled_mass");
		datagen(INFESTED_WOOD_FENCE, "infested_wood_mass");
		datagen(INFESTED_STURDY_FENCE, "infested_sturdy_mass");
		datagen(INFESTED_WOOD_FENCE_GATE, "infested_wood_mass");
		datagen(INFESTED_STURDY_FENCE_GATE, "infested_sturdy_mass");
	}

	public static class BlockTags
	{
		public static final TagKey<Block> SCULK_RAID_TARGET_HIGH_PRIORITY = create("sculk_raid_target/high_priority");
		public static final TagKey<Block> SCULK_RAID_TARGET_MEDIUM_PRIORITY = create("sculk_raid_target/medium_priority");
		public static final TagKey<Block> SCULK_RAID_TARGET_LOW_PRIORITY = create("sculk_raid_target/low_priority");
		public static final TagKey<Block> SCULK_BEE_HARVESTABLE = create("sculk_bee_harvestable");
		public static final TagKey<Block> INFESTED_BLOCK = create("infested_block");
		public static final TagKey<Block> CONVERTS_TO_CRUMBLING_VARIANT = create("converts_to_crumbling_variant");

		public static final TagKey<Block> NOT_INFESTABLE = create("not_infestable");

		// Helper Function
		private static TagKey<Block> create(String location)
		{
			return net.minecraft.tags.BlockTags.create(new ResourceLocation(SculkHorde.MOD_ID, location));
		}

		// Helper Function
		private static TagKey<Block> createForge(String location)
		{
			return net.minecraft.tags.BlockTags.create(new ResourceLocation("forge", location));
		}

		// Helper Function
		private static TagKey<Block> createMinecraft(String location)
		{
			return net.minecraft.tags.BlockTags.create(new ResourceLocation(location));
		}
	}
}