package com.github.sculkhorde.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SculkHorde.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SCULK_HORDE_TAB = TABS.register("sculk_horde_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sculkhorde_tab"))
            .icon(() -> new ItemStack(ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get()))
            .displayItems((enabledFeatures, event) -> {
                if(!FMLLoader.isProduction()) event.accept(ModItems.WARDEN_BEEF.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_NODE_SPAWNER.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_CONVERSION_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_RAID_WAND.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.STRUCTURE_ORIGIN_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.SOULITE_CORE_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.BUDDING_SOULITE_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.SOULITE_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.DEPLETED_SOULITE_BLOCK .get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.SOULITE_BUD_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.SOULITE_CLUSTER_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.FUNGAL_SHROOM_CORE_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.FUNGAL_SCULK_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.FUNGAL_SCULK_STEM_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.TENDRIL_CORE_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModItems.FERRISCITE.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModItems.DIASCITE.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModItems.SOUL_DISRUPTER.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModItems.SOUL_ANIMATOR.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.GOLEM_OF_WRATH_ANIMATOR_BLOCK.get());
                if(ModConfig.isExperimentalFeaturesEnabled()) event.accept(ModBlocks.DEPLETED_GOLEM_OF_WRATH_ANIMATOR_BLOCK.get());


                event.accept(ModItems.SCULK_SPORE_SPEWER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_MITE_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_MITE_AGGRESSOR_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_ZOMBIE_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_SPITTER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_CREEPER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_HATCHER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_VINDICATOR_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_RAVAGER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_PHANTOM_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_ENDERMAN_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_SALMON_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_SQUID_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_PUFFERFISH_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_WITCH_SPAWN_EGG.get());

                event.accept(ModBlocks.DEV_MASS_INFECTINATOR_3000_BLOCK.get());
                event.accept(ModItems.ESSENCE_OF_PURITY.get());
                event.accept(ModItems.EYE_OF_PURITY.get());
                event.accept(ModItems.PURIFICATION_FLASK.get());
                event.accept(ModBlocks.INFESTATION_WARD_BLOCK.get());
                event.accept(ModItems.CHUNK_O_BRAIN.get());
                event.accept(ModItems.DORMANT_HEART_OF_THE_HORDE.get());
                event.accept(ModItems.HEART_OF_THE_HORDE.get());
                event.accept(ModItems.HEART_OF_PURITY.get());
                event.accept(ModItems.INFESTATION_PURIFIER.get());
                event.accept(ModBlocks.SOUL_HARVESTER_BLOCK.get());
                event.accept(ModBlocks.INFESTED_CRYING_OBSIDIAN.get());
                event.accept(ModItems.CRYING_SOULS.get());
                event.accept(ModItems.PURE_SOULS.get());
                event.accept(ModItems.SCULK_ENDERMAN_CLEAVER.get());
                event.accept(ModItems.SCULK_SWEEPER_SWORD.get());
                event.accept(ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get());
                event.accept(ModBlocks.SCULK_NODE_BLOCK.get());
                event.accept(ModBlocks.SCULK_ARACHNOID.get());
                event.accept(ModBlocks.SCULK_DURA_MATTER.get());



                event.accept(ModBlocks.SCULK_BEE_NEST_BLOCK.get());
                event.accept(ModBlocks.SCULK_BEE_NEST_CELL_BLOCK.get());
                event.accept(ModItems.SCULK_RESIN.get());
                event.accept(ModBlocks.SCULK_LIVING_ROCK_ROOT_BLOCK.get());
                event.accept(ModBlocks.SCULK_LIVING_ROCK_BLOCK.get());
                event.accept(ModBlocks.CALCITE_ORE.get());
                event.accept(ModItems.CALCITE_CLUMP.get());
                event.accept(ModBlocks.BUDDING_SOULITE_BLOCK.get());
                event.accept(ModBlocks.SOULITE_BLOCK.get());
                event.accept(ModBlocks.DEPLETED_SOULITE_BLOCK.get());
                event.accept(ModBlocks.SOULITE_CLUSTER_BLOCK.get());
                event.accept(ModItems.SOULITE_SHARD.get());
                event.accept(ModItems.SCULK_ACIDIC_PROJECTILE.get());


                event.accept(ModItems.DEEP_GREEN_MUSIC_DISC.get());
                event.accept(ModItems.BLIND_AND_ALONE_MUSIC_DISC.get());

                event.accept(ModBlocks.GRASS.get());
                event.accept(ModBlocks.GRASS_SHORT.get());
                event.accept(ModBlocks.SCULK_SHROOM_CULTURE.get());
                event.accept(ModBlocks.SMALL_SHROOM.get());
                event.accept(ModBlocks.SPIKE.get());
                event.accept(ModBlocks.TENDRILS.get());
                event.accept(ModBlocks.DISEASED_KELP_BLOCK.get());
                event.accept(Blocks.SCULK);

                event.accept(ModBlocks.SCULK_SUMMONER_BLOCK.get());
                event.accept(Blocks.SCULK_CATALYST);
                event.accept(Blocks.SCULK_SHRIEKER);
                event.accept(Blocks.SCULK_SENSOR);

                event.accept(ModBlocks.INFESTED_LOG.get());
                event.accept(ModBlocks.INFESTED_SAND.get());
                event.accept(ModBlocks.INFESTED_RED_SAND.get());
                event.accept(ModBlocks.INFESTED_SANDSTONE.get());
                event.accept(ModBlocks.INFESTED_SANDSTONE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_SANDSTONE_SLAB.get());
                event.accept(ModBlocks.INFESTED_SANDSTONE_WALL.get());
                event.accept(ModBlocks.INFESTED_GRAVEL.get());
                event.accept(ModBlocks.INFESTED_CLAY.get());
                event.accept(ModBlocks.INFESTED_STONE.get());
                event.accept(ModBlocks.INFESTED_STONE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_STONE_SLAB.get());
                event.accept(ModBlocks.INFESTED_COBBLESTONE.get());
                event.accept(ModBlocks.INFESTED_COBBLESTONE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_COBBLESTONE_SLAB.get());
                event.accept(ModBlocks.INFESTED_COBBLESTONE_WALL.get());
                event.accept(ModBlocks.INFESTED_MOSSY_COBBLESTONE.get());
                event.accept(ModBlocks.INFESTED_MOSSY_COBBLESTONE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_MOSSY_COBBLESTONE_SLAB.get());
                event.accept(ModBlocks.INFESTED_MOSSY_COBBLESTONE_WALL.get());
                event.accept(ModBlocks.INFESTED_DEEPSLATE.get());
                event.accept(ModBlocks.INFESTED_COBBLED_DEEPSLATE.get());
                event.accept(ModBlocks.INFESTED_COBBLED_DEEPSLATE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_COBBLED_DEEPSLATE_SLAB.get());
                event.accept(ModBlocks.INFESTED_COBBLED_DEEPSLATE_WALL.get());
                event.accept(ModBlocks.INFESTED_ANDESITE.get());
                event.accept(ModBlocks.INFESTED_ANDESITE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_ANDESITE_SLAB.get());
                event.accept(ModBlocks.INFESTED_ANDESITE_WALL.get());
                event.accept(ModBlocks.INFESTED_DIORITE.get());
                event.accept(ModBlocks.INFESTED_DIORITE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_DIORITE_SLAB.get());
                event.accept(ModBlocks.INFESTED_DIORITE_WALL.get());
                event.accept(ModBlocks.INFESTED_GRANITE.get());
                event.accept(ModBlocks.INFESTED_GRANITE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_GRANITE_SLAB.get());
                event.accept(ModBlocks.INFESTED_GRANITE_WALL.get());
                event.accept(ModBlocks.INFESTED_TUFF.get());
                event.accept(ModBlocks.INFESTED_CALCITE.get());
                event.accept(ModBlocks.INFESTED_SNOW.get());
                event.accept(ModBlocks.INFESTED_MOSS.get());
                event.accept(ModBlocks.INFESTED_MUD.get());
                event.accept(ModBlocks.INFESTED_PACKED_MUD.get());
                event.accept(ModBlocks.INFESTED_MUD_BRICKS.get());
                event.accept(ModBlocks.INFESTED_MUD_BRICK_STAIRS.get());
                event.accept(ModBlocks.INFESTED_MUD_BRICK_SLAB.get());
                event.accept(ModBlocks.INFESTED_MUD_BRICK_WALL.get());
                event.accept(ModBlocks.INFESTED_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_BLACK_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_BLUE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_BROWN_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_CYAN_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_GRAY_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_GREEN_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_LIGHT_BLUE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_LIGHT_GRAY_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_LIME_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_MAGENTA_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_ORANGE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_PINK_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_PURPLE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_RED_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_WHITE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_YELLOW_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_NETHERRACK.get());
                event.accept(ModBlocks.INFESTED_CRIMSON_NYLIUM.get());
                event.accept(ModBlocks.INFESTED_WARPED_NYLIUM.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE_STAIRS.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE_SLAB.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE_WALL.get());
                event.accept(ModBlocks.INFESTED_BASALT.get());
                event.accept(ModBlocks.INFESTED_SMOOTH_BASALT.get());
                event.accept(ModBlocks.INFESTED_ENDSTONE.get());
                event.accept(ModBlocks.INFESTED_STONE_BRICKS.get());
                event.accept(ModBlocks.INFESTED_STONE_BRICK_STAIRS.get());
                event.accept(ModBlocks.INFESTED_STONE_BRICK_SLAB.get());
                event.accept(ModBlocks.INFESTED_STONE_BRICK_WALL.get());
                event.accept(ModBlocks.INFESTED_MOSSY_STONE_BRICKS.get());
                event.accept(ModBlocks.INFESTED_MOSSY_STONE_BRICK_STAIRS.get());
                event.accept(ModBlocks.INFESTED_MOSSY_STONE_BRICK_SLAB.get());
                event.accept(ModBlocks.INFESTED_MOSSY_STONE_BRICK_WALL.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE_BRICKS.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE_BRICK_STAIRS.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE_BRICK_SLAB.get());
                event.accept(ModBlocks.INFESTED_BLACKSTONE_BRICK_WALL.get());
                event.accept(ModBlocks.INFESTED_WOOD_MASS.get());
                event.accept(ModBlocks.INFESTED_WOOD_STAIRS.get());
                event.accept(ModBlocks.INFESTED_WOOD_SLAB.get());
                event.accept(ModBlocks.INFESTED_WOOD_FENCE.get());
                event.accept(ModBlocks.INFESTED_CRUMPLED_MASS.get());
                event.accept(ModBlocks.INFESTED_CRUMBLING_STAIRS.get());
                event.accept(ModBlocks.INFESTED_CRUMBLING_SLAB.get());
                event.accept(ModBlocks.INFESTED_CRUMBLING_WALL.get());
                event.accept(ModBlocks.INFESTED_STURDY_MASS.get());
                event.accept(ModBlocks.INFESTED_STURDY_STAIRS.get());
                event.accept(ModBlocks.INFESTED_STURDY_SLAB.get());
                event.accept(ModBlocks.INFESTED_STURDY_WALL.get());
                event.accept(ModBlocks.INFESTED_STURDY_FENCE.get());
                event.accept(ModBlocks.INFESTED_WOOD_FENCE.get());
                event.accept(ModBlocks.INFESTED_WOOD_FENCE_GATE.get());
                event.accept(ModBlocks.INFESTED_STURDY_FENCE_GATE.get());
            })
            .build());
}
