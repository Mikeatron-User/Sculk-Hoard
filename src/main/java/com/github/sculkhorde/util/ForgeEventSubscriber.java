package com.github.sculkhorde.util;

import com.github.sculkhorde.common.advancement.ContributeTrigger;
import com.github.sculkhorde.common.block.FleshyCompostBlock;
import com.github.sculkhorde.common.effect.SculkBurrowedEffect;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.misc.StatisticsData;
import com.github.sculkhorde.systems.AutoPerformanceSystem;
import com.github.sculkhorde.systems.BeeNestActivitySystem;
import com.github.sculkhorde.systems.DebugSlimeSystem;
import com.github.sculkhorde.systems.SculkNodesSystem;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkInfestationSystem;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.event_system.EventSystem;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.systems.raid_system.RaidHandler;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Predicate;


@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {

    private static long time_save_point; //Used to track time passage.
    private static int sculkMassCheck;


    /**
     * This event gets called when a world loads.
     * All we do is initialize the gravemind and some variables
     * used to track changes.
     * @param event The load event
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event)
    {
        //Initalize Gravemind
        if(!event.getLevel().isClientSide() && event.getLevel().equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            SculkHorde.statisticsData = new StatisticsData(); // Keep this above "SculkHorde.savedData". Otherwise, stats won't be loaded correctly.
            SculkHorde.savedData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(ModSavedData::load, ModSavedData::new, SculkHorde.SAVE_DATA_ID); //Initialize Saved Data
            SculkHorde.gravemind = new Gravemind(); //Initialize Gravemind
            SculkHorde.debugSlimeSystem = new DebugSlimeSystem();
            SculkHorde.deathAreaInvestigator = new DeathAreaInvestigator(); //Initialize Death Area Investigator
            SculkHorde.raidHandler = new RaidHandler((ServerLevel) event.getLevel()); //Initialize Raid Handler
            SculkHorde.sculkNodesSystem = new SculkNodesSystem(); //Initialize Sculk Nodes Handler
            SculkHorde.entityChunkLoaderHelper = new EntityChunkLoaderHelper(); //Initialize Entity Chunk Loader Helper
            SculkHorde.blockEntityChunkLoaderHelper = new BlockEntityChunkLoaderHelper(); //Initialize Block Entity Chunk Loader Helper
            SculkHorde.eventSystem = new EventSystem(); //Initialize Event Handler
            SculkHorde.beeNestActivitySystem = new BeeNestActivitySystem();
            SculkHorde.autoPerformanceSystem = new AutoPerformanceSystem();
            SculkHorde.chunkInfestationSystem = new ChunkInfestationSystem();
            SculkHorde.cursorSystem = new CursorSystem();
            ModConfig.SERVER.loadItemsInfectionCursorsCanEat();
            ModConfig.SERVER.loadConfiguredInfestableBlocks();

            time_save_point = 0; //Used to track time passage.
            sculkMassCheck = 0; //Used to track changes in sculk mass

            // Check if chunk 0,0 is loaded. If not, load it.
            if(!event.getLevel().getChunkSource().hasChunk(0,0))
            {
                BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(((ServerLevel)event.getLevel()), BlockPos.ZERO, 5, 0, TickUnits.convertMinutesToTicks(10));
            }

            if(ModConfig.SERVER.purification_speed_multiplier.get() <= 0)
            {
                ModConfig.SERVER.purification_speed_multiplier.set(1.0);
            }

            if(ModConfig.SERVER.infection_speed_multiplier.get() <= 0)
            {
                ModConfig.SERVER.infection_speed_multiplier.set(1.0);
            }

        }
    }

    /**
     * Gets Called Every tick when a world is running.
     * @param event The event with all the details
     */
    @SubscribeEvent
    public static void WorldTickEvent(TickEvent.LevelTickEvent event)
    {
        // If we are on client or the gravemind is null or we are not in the overworld, return
        if(event.level.isClientSide() || (event.phase == TickEvent.Phase.END) || (SculkHorde.gravemind == null) || !event.level.equals(ServerLifecycleHooks.getCurrentServer().overworld()))
        {
            return;
        }

        // Run this stuff every tick

        SculkHorde.savedData.incrementNoNodeSpawningTicksElapsed();

        SculkHorde.raidHandler.raidTick(); // Tick the raid handler
        SculkHorde.deathAreaInvestigator.tick();
        SculkHorde.sculkNodesSystem.tick();
        SculkHorde.eventSystem.serverTick();
        SculkHorde.cursorSystem.serverTick();
        SculkHorde.populationHandler.serverTick();
        SculkHorde.blockEntityChunkLoaderHelper.processBlockChunkLoadRequests();
        SculkHorde.entityChunkLoaderHelper.processEntityChunkLoadRequests();
        SculkHorde.beeNestActivitySystem.serverTick();
        SculkHorde.chunkInfestationSystem.serverTick();

        if(ModConfig.isExperimentalFeaturesEnabled())
        {
            SculkHorde.hitSquadDispatcherSystem.serverTick();
        }

        // Only run stuff below every 5 minutes
        if (event.level.getGameTime() - time_save_point < TickUnits.convertMinutesToTicks(5))
        {
            return;
        }

        time_save_point = event.level.getGameTime();//Set to current time so we can recalculate time passage
        SculkHorde.beeNestActivitySystem.activate();

        //Verification Processes to ensure our data is accurate
        SculkHorde.savedData.validateNodeEntries();
        SculkHorde.savedData.validateBeeNestEntries();
        SculkHorde.savedData.validateNoRaidZoneEntries();
        SculkHorde.savedData.validateAreasOfInterest();

        //Calculate Current State
        SculkHorde.gravemind.calulateCurrentState(); //Have the gravemind update it's state if necessary

        //Check How much Mass Was Generated over this period
        if(SculkHorde.isDebugMode()) System.out.println("Accumulated Mass Since Last Check: " + (SculkHorde.savedData.getSculkAccumulatedMass() - sculkMassCheck));
        sculkMassCheck = SculkHorde.savedData.getSculkAccumulatedMass();

    }

    @SubscribeEvent
    public static void onLivingEntityDeathEvent(LivingDeathEvent event)
    {
        if(event.getEntity().level().isClientSide())
        {
            return;
        }

        if(EntityAlgorithms.isSculkLivingEntity.test(event.getEntity()))
        {
            SculkHorde.savedData.reportDeath((ServerLevel) event.getEntity().level(), event.getEntity().blockPosition());
            SculkHorde.savedData.addHostileToMemory(event.getEntity().getLastHurtByMob());
            SculkHorde.statisticsData.incrementTotalUnitDeaths();
            SculkHorde.statisticsData.addTotalMassRemovedFromHorde((int) event.getEntity().getMaxHealth());
            return;

        }

        Entity killerEntity = event.getSource().getEntity();
        if(killerEntity instanceof LivingEntity killerLivingEntity)
        {
            if(EntityAlgorithms.isSculkLivingEntity.test(killerLivingEntity))
            {
                FleshyCompostBlock.placeBlock(event.getEntity());
            }
        }

    }

    @SubscribeEvent
    public static void onPotionExpireEvent(MobEffectEvent.Expired event)
    {
        if(event.getEntity().level().isClientSide() || SculkHorde.gravemind == null)
        {
            return;
        }

        MobEffectInstance effectInstance = event.getEffectInstance();

        if(effectInstance == null)
        {
            return;
        }

        if(effectInstance.getEffect() == ModMobEffects.SCULK_INFECTION.get())
        {
            SculkBurrowedEffect.onPotionExpire(event);
        }

    }

    @SubscribeEvent
    public static void OnLivingDamageEvent(LivingDamageEvent event)
    {


        // Get Item being used to attack
        ItemStack itemStack = ItemStack.EMPTY;
        Entity damageSourceEntity = event.getSource().getEntity();
        LivingEntity targetEntity = event.getEntity();

        if(EntityAlgorithms.isSculkLivingEntity.test(targetEntity) && damageSourceEntity instanceof LivingEntity)
        {
            SculkHorde.savedData.addHostileToMemory((LivingEntity) damageSourceEntity);
        }


        if(damageSourceEntity instanceof LivingEntity attackingEntity)
        {
            itemStack = attackingEntity.getMainHandItem();
            if(!itemStack.getItem().equals(ModItems.SCULK_SWEEPER_SWORD.get()))
            {
               return;
            }

            if(!EntityAlgorithms.isSculkLivingEntity.test(targetEntity))
            {
                event.setAmount(event.getAmount()/2);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.player.level().isClientSide())
        {
            return;
        }

        if(event.player.tickCount % 20 == 0)
        {
            AdvancementUtil.advancementHandlingTick((ServerLevel) event.player.level());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity().level().isClientSide())
        {
            return;
        }

        if(SculkHorde.contributionHandler.isContributor((ServerPlayer) event.getEntity()) && !SculkHorde.contributionHandler.doesPlayerHaveContributionAdvancement((ServerPlayer) event.getEntity()))
        {
            AdvancementUtil.giveAdvancementToPlayer((ServerPlayer) event.getEntity(), ContributeTrigger.INSTANCE);
            SculkHorde.contributionHandler.givePlayerCoinOfContribution(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        if(PlayerProfileHandler.isPlayerActiveVessel(event.getEntity()))
        {
            MobEffectInstance effectInstance = new MobEffectInstance(ModMobEffects.SCULK_VESSEL.get(), Integer.MAX_VALUE);
            event.getEntity().addEffect(effectInstance);
        }
    }



    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START) {
            SculkHorde.autoPerformanceSystem.onServerTick();
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event)
    {
        if(event.getLevel().isClientSide())
        {
            return;
        }

        // Event has no phases
        if(event.getEntity() instanceof Mob mob)
        {
            if(EntityAlgorithms.isLivingEntityExplicitDenyTarget(mob) || !mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE))
            {
                return;
            }

            mob.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(mob, LivingEntity.class, true, shouldEntitiesAttackTheSculkHorde));
        }
    }

    public static Predicate<LivingEntity> shouldEntitiesAttackTheSculkHorde = (e) ->
    {
        return ModConfig.SERVER.should_all_other_mobs_attack_the_sculk_horde.get() && EntityAlgorithms.isSculkLivingEntity.test(e);
    };
}
