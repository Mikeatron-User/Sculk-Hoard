package com.github.sculkhorde.core;

import com.github.sculkhorde.common.blockentity.*;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {


    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SculkHorde.MOD_ID);

    public static RegistryObject<BlockEntityType<SculkMassBlockEntity>> SCULK_MASS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_mass_block_entity", () -> BlockEntityType.Builder.of(
                    SculkMassBlockEntity::new, ModBlocks.SCULK_MASS.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkNodeBlockEntity>> SCULK_NODE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_node_block_entity", () -> BlockEntityType.Builder.of(
                    SculkNodeBlockEntity::new, ModBlocks.SCULK_NODE_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkAncientNodeBlockEntity>> SCULK_ANCIENT_NODE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_ancient_node_block_entity", () -> BlockEntityType.Builder.of(
                    SculkAncientNodeBlockEntity::new, ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkBeeNestBlockEntity>> SCULK_BEE_NEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_bee_nest_block_entity", () -> BlockEntityType.Builder.of(
                    SculkBeeNestBlockEntity::new, ModBlocks.SCULK_BEE_NEST_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkBeeNestCellBlockEntity>> SCULK_BEE_NEST_CELL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_bee_nest_cell_block_entity", () -> BlockEntityType.Builder.of(
                    SculkBeeNestCellBlockEntity::new, ModBlocks.SCULK_BEE_NEST_CELL_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkSummonerBlockEntity>> SCULK_SUMMONER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_summoner_block_entity", () -> BlockEntityType.Builder.of(
                    SculkSummonerBlockEntity::new, ModBlocks.SCULK_SUMMONER_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkLivingRockRootBlockEntity>> SCULK_LIVING_ROCK_ROOT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_living_rock_root_block_entity", () -> BlockEntityType.Builder.of(
                    SculkLivingRockRootBlockEntity::new, ModBlocks.SCULK_LIVING_ROCK_ROOT_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<DevStructureTesterBlockEntity>> DEV_STRUCTURE_TESTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dev_structure_tester_block_entity", () -> BlockEntityType.Builder.of(
                    DevStructureTesterBlockEntity::new, ModBlocks.DEV_STRUCTURE_TESTER_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<DevMassInfectinator3000BlockEntity>> DEV_MASS_INFECTINATOR_3000_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dev_mass_infectinator_3000_block_entity", () -> BlockEntityType.Builder.of(
                    DevMassInfectinator3000BlockEntity::new, ModBlocks.DEV_MASS_INFECTINATOR_3000_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<InfestedTagBlockEntity>> INFESTED_LOG_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infested_log_block_entity", () -> BlockEntityType.Builder.of(
                    InfestedTagBlockEntity::new, ModBlocks.INFESTED_LOG.get()).build(null));

    public static RegistryObject<BlockEntityType<InfestedTagBlockEntity>> INFESTED_WOOD_MASS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infested_wood_mass_block_entity", () -> BlockEntityType.Builder.of(
                    InfestedTagBlockEntity::new, ModBlocks.INFESTED_WOOD_MASS.get()).build(null));
    
    public static RegistryObject<BlockEntityType<InfestedTagBlockEntity>> INFESTED_WOOD_STAIRS_BLOCK_ENTITY =
    		BLOCK_ENTITIES.register("infested_wood_stairs_block_entity", () -> BlockEntityType.Builder.of(
    				InfestedTagBlockEntity::new, ModBlocks.INFESTED_WOOD_STAIRS.get()).build(null));

    public static RegistryObject<BlockEntityType<SoulHarvesterBlockEntity>> SOUL_HARVESTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("soul_harvester_block_entity", () -> BlockEntityType.Builder.of(
                    SoulHarvesterBlockEntity::new, ModBlocks.SOUL_HARVESTER_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<FleshyCompostBlockEntity>> FLESHY_COMPOST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("fleshy_compost_block_entity", () -> BlockEntityType.Builder.of(
                    FleshyCompostBlockEntity::new, ModBlocks.PASTY_ORGANIC_MASS.get()).build(null));

    public static RegistryObject<BlockEntityType<StructureCoreBlockEntity>> STRUCTURE_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("structure_core_block_entity", () -> BlockEntityType.Builder.of(
                    StructureCoreBlockEntity::new, ModBlocks.STRUCTURE_CORE_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SouliteCoreBlockEntity>> SOULITE_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("soulite_core_block_entity", () -> BlockEntityType.Builder.of(
                    SouliteCoreBlockEntity::new, ModBlocks.SOULITE_CORE_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<FungalShroomCoreBlockEntity>> FUNGAL_SHROOM_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("fungal_shroom_core_block_entity", () -> BlockEntityType.Builder.of(
                    FungalShroomCoreBlockEntity::new, ModBlocks.FUNGAL_SHROOM_CORE_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<TendrilCoreBlockEntity>> TENDRIL_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("tendril_core_block_entity", () -> BlockEntityType.Builder.of(
                    TendrilCoreBlockEntity::new, ModBlocks.TENDRIL_CORE_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<GolemOfWrathAnimatorBlockEntity>> GOLEM_OF_WRATH_ANIMATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("golem_of_wrath_animator_block_entity", () -> BlockEntityType.Builder.of(
                    GolemOfWrathAnimatorBlockEntity::new, ModBlocks.GOLEM_OF_WRATH_ANIMATOR_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<BeeColonyCoreBlockEntity>> BEE_COLONY_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("bee_colony_core_block_entity", () -> BlockEntityType.Builder.of(
                    BeeColonyCoreBlockEntity::new, ModBlocks.BEE_COLONY_CORE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
