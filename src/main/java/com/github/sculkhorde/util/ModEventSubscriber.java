package com.github.sculkhorde.util;

import com.github.sculkhorde.common.advancement.*;
import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.LivingArmorEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactory;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        EntityFactory.initialize();
        BlockInfestationSystem.initialize();

        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.SCULK_MITE.get(),
                    SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SculkMiteEntity::additionalSpawnCheck);
            afterCommonSetup();
        });
    }

    // runs on main thread after common setup event
    // adding things to unsynchronized registries (i.e. most vanilla registries) can be done here
    private static void afterCommonSetup()
    {
        CriteriaTriggers.register(GravemindEvolveImmatureTrigger.INSTANCE);
        CriteriaTriggers.register(GravemindEvolveMatureTrigger.INSTANCE);
        CriteriaTriggers.register(SculkHordeStartTrigger.INSTANCE);
        CriteriaTriggers.register(SculkNodeSpawnTrigger.INSTANCE);
        CriteriaTriggers.register(SoulHarvesterTrigger.INSTANCE);
        CriteriaTriggers.register(SculkHordeDefeatTrigger.INSTANCE);
        CriteriaTriggers.register(ContributeTrigger.INSTANCE);
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SCULK_ZOMBIE.get(), SculkZombieEntity.createAttributes().build());
        event.put(ModEntities.SCULK_MITE.get(), SculkMiteEntity.createAttributes().build());
        event.put(ModEntities.SCULK_MITE_AGGRESSOR.get(), SculkMiteAggressorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SPITTER.get(), SculkSpitterEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BEE_INFECTOR.get(), SculkBeeInfectorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BEE_HARVESTER.get(), SculkBeeHarvesterEntity.createAttributes().build());
        event.put(ModEntities.SCULK_HATCHER.get(), SculkHatcherEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SPORE_SPEWER.get(), SculkSporeSpewerEntity.createAttributes().build());
        event.put(ModEntities.SCULK_RAVAGER.get(), SculkRavagerEntity.createAttributes().build());
        event.put(ModEntities.INFESTATION_PURIFIER.get(), InfestationPurifierEntity.createAttributes().build());
        event.put(ModEntities.SCULK_VINDICATOR.get(), SculkVindicatorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_CREEPER.get(), SculkCreeperEntity.createAttributes().build());
        event.put(ModEntities.SCULK_ENDERMAN.get(), SculkEndermanEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PHANTOM.get(), SculkPhantomEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PHANTOM_CORPSE.get(), SculkPhantomCorpseEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SALMON.get(), SculkSalmonEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SQUID.get(), SculkSquidEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PUFFERFISH.get(), SculkPufferfishEntity.createAttributes().build());
        event.put(ModEntities.SCULK_WITCH.get(), SculkWitchEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SOUL_REAPER.get(), SculkSoulReaperEntity.createAttributes().build());
        event.put(ModEntities.SCULK_VEX.get(), SculkVexEntity.createAttributes().build());
        event.put(ModEntities.LIVING_ARMOR.get(), LivingArmorEntity.createAttributes().build());
        event.put(ModEntities.GOLEM_OF_WRATH.get(), GolemOfWrathEntity.createAttributes().build());
    }
}

