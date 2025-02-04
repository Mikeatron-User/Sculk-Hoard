package com.github.sculkhorde.client;

import com.github.sculkhorde.client.particle.BurrowedBurstParticle;
import com.github.sculkhorde.client.particle.SculkCrustParticle;
import com.github.sculkhorde.client.renderer.block.SculkSummonerBlockRenderer;
import com.github.sculkhorde.client.renderer.block.SoulHarvesterBlockRenderer;
import com.github.sculkhorde.client.renderer.entity.*;
import com.github.sculkhorde.common.screen.SoulHarvesterScreen;
import com.github.sculkhorde.core.*;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    public final Map<EntityType<?>, EntityRenderer<?>> renderers = Maps.newHashMap();

    public <T extends Entity> void register(EntityType<T> p_229087_1_, EntityRenderer<? super T> p_229087_2_) {
        this.renderers.put(p_229087_1_, p_229087_2_);
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenders(final EntityRenderersEvent.RegisterRenderers event) {

        // Register Renderers for Entities

        event.registerEntityRenderer(ModEntities.SCULK_ZOMBIE.get(), SculkZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_MITE.get(), SculkMiteRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_MITE_AGGRESSOR.get(), SculkMiteAggressorRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_SPITTER.get(), SculkSpitterRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_BEE_INFECTOR.get(), SculkBeeInfectorRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_BEE_HARVESTER.get(), SculkBeeHarvesterRenderer::new);
        event.registerEntityRenderer(ModEntities.CUSTOM_ITEM_PROJECTILE_ENTITY.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_ACIDIC_PROJECTILE_ENTITY.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.PURIFICATION_FLASK_PROJECTILE_ENTITY.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_HATCHER.get(), SculkHatcherRenderer::new);
        event.registerEntityRenderer(ModEntities.CURSOR_PROBER.get(), CursorProberRenderer::new);
        event.registerEntityRenderer(ModEntities.CURSOR_PURIFIER_PROBER.get(), CursorPurifierProberRenderer::new);
        event.registerEntityRenderer(ModEntities.CURSOR_BRIDGER.get(), CursorBridgerRenderer::new);
        event.registerEntityRenderer(ModEntities.CURSOR_SURFACE_INFECTOR.get(), CursorSurfaceInfectorRenderer::new);
        event.registerEntityRenderer(ModEntities.CURSOR_SURFACE_PURIFIER.get(), CursorSurfacePurifierRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_SPORE_SPEWER.get(), SculkSporeSpewerRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_RAVAGER.get(), SculkRavagerRenderer::new);
        event.registerEntityRenderer(ModEntities.INFESTATION_PURIFIER.get(), InfestationPurifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SCULK_SUMMONER_BLOCK_ENTITY.get(), context -> new SculkSummonerBlockRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.SOUL_HARVESTER_BLOCK_ENTITY.get(), context -> new SoulHarvesterBlockRenderer());
        event.registerEntityRenderer(ModEntities.SCULK_VINDICATOR.get(), SculkVindicatorRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_CREEPER.get(), SculkCreeperRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_ENDERMAN.get(), SculkEndermanRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_PHANTOM.get(), SculkPhantomRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_PHANTOM_CORPSE.get(), SculkPhantomCorpseRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_SALMON.get(), SculkSalmonRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_SQUID.get(), SculkSquidRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_PUFFERFISH.get(), SculkPufferfishRenderer::new);
        event.registerEntityRenderer(ModEntities.ENDER_BUBBLE_ATTACK.get(), EnderBubbleAttackRenderer::new);
        event.registerEntityRenderer(ModEntities.CHAOS_TELEPORATION_RIFT.get(), ChaosTeleporationRiftRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_SPINE_SPIKE_ATTACK.get(), SculkSpineSpikeAttackRenderer::new);
        event.registerEntityRenderer(ModEntities.AREA_EFFECT_SPHERICAL_CLOUD.get(), AreaEffectSphericalCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_WITCH.get(), SculkWitchRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_SOUL_REAPER.get(), SculkSoulReaperRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_VEX.get(), SculkVexRenderer::new);
        event.registerEntityRenderer(ModEntities.SCULK_WITCH.get(), SculkWitchRenderer::new);
        event.registerEntityRenderer(ModEntities.LIVING_ARMOR.get(), LivingArmorRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_OF_WRATH.get(), GolemOfWrathRenderer::new);

        event.registerEntityRenderer(ModEntities.SOUL_FIRE_PROJECTILE.get(), SoulFireProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_POISON_PROJECTILE.get(), SoulPoisonProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_ICE_PROJECTILE.get(), SoulIceProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_BREEZE_PROJECTILE.get(), SoulBreezeProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_SPEAR_PROJECTILE.get(), SoulSpearProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_FLY_SWATTER_PROJECTILE.get(), SoulFlySwatterProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.FLOOR_SOUL_SPEARS.get(), FloorSoulSpearsRenderer::new);
        event.registerEntityRenderer(ModEntities.ELEMENTAL_FIRE_MAGIC_CIRCLE.get(), ElementalFireMagicCircleRenderer::new);
        event.registerEntityRenderer(ModEntities.ELEMENTAL_BREEZE_MAGIC_CIRCLE.get(), ElementalBreezeMagicCircleRenderer::new);
        event.registerEntityRenderer(ModEntities.ELEMENTAL_POISON_MAGIC_CIRCLE.get(), ElementalPoisonMagicCircleRenderer::new);
        event.registerEntityRenderer(ModEntities.ELEMENTAL_ICE_MAGIC_CIRCLE.get(), ElementalIceMagicCircleRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_SPEAR_SUMMONER.get(), SoulSpearSummonerRenderer::new);
        event.registerEntityRenderer(ModEntities.ZOLTRAAK_ATTACK_ENTITY.get(), ZoltraakAttackRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_BLAST_ATTACK_ENTITY.get(), SoulBlastAttackEntityRenderer::new);
    }


    @SubscribeEvent
    public static void registerRenderers(final RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ModParticles.SCULK_CRUST_PARTICLE.get(), SculkCrustParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BURROWED_BURST_PARTICLE.get(), BurrowedBurstParticle.Factory::new);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            MenuScreens.register(ModMenuTypes.SOUL_HARVESTER_MENU.get(), SoulHarvesterScreen::new);
        }
    }
}
