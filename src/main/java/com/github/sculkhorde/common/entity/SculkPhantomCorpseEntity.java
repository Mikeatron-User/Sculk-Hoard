package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.VirtualSurfaceInfestorCursor;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class SculkPhantomCorpseEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/ModWorldEvents.java (this might not be necessary)<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkSporeSpewerEntity.java<br>
     * Added client/model/entity/ SculkSporeSpewerModel.java<br>
     * Added client/renderer/entity/ SculkSporeSpewerRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 40F;
    //The armor of the mob
    public static final float ARMOR = 10F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 0F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 0F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 0F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetPassives().enableTargetHostiles();

    private VirtualSurfaceInfestorCursor cursor;

    private long INFECTION_INTERVAL_TICKS = TickUnits.convertSecondsToTicks(2);
    private long lastInfectionTime = 0;

    public static final EntityDataAccessor<Integer> DATA_TICKS_ALIVE = SynchedEntityData.defineId(SculkEndermanEntity.class, EntityDataSerializers.INT);
    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkPhantomCorpseEntity(EntityType<? extends SculkPhantomCorpseEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkPhantomCorpseEntity(Level worldIn) {super(ModEntities.SCULK_SPORE_SPEWER.get(), worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }


    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return false;
    }

    private boolean isParticipatingInRaid = false;

    @Override
    public SquadHandler getSquad() {
        return null;
    }

    @Override
    public boolean isParticipatingInRaid() {
        return isParticipatingInRaid;
    }

    @Override
    public void setParticipatingInRaid(boolean isParticipatingInRaidIn) {
        isParticipatingInRaid = isParticipatingInRaidIn;
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        Goal[] goalSelectorPayload = goalSelectorPayload();
        for(int priority = 0; priority < goalSelectorPayload.length; priority++)
        {
            this.goalSelector.addGoal(priority, goalSelectorPayload[priority]);
        }

        Goal[] targetSelectorPayload = targetSelectorPayload();
        for(int priority = 0; priority < targetSelectorPayload.length; priority++)
        {
            this.targetSelector.addGoal(priority, targetSelectorPayload[priority]);
        }

    }

    /**
     * Prepares an array of goals to give to registerGoals() for the goalSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    public Goal[] goalSelectorPayload()
    {
        Goal[] goals =
                {
                        // MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new DestroyLeavesTouchingHitBoxGoal(this),
                        new dieAfterTimeGoal(this),
                };
        return goals;
    }

    /**
     * Prepares an array of goals to give to registerGoals() for the targetSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    public Goal[] targetSelectorPayload()
    {
        Goal[] goals =
                {
                        //HurtByTargetGoal(mob)
                        new TargetAttacker(this).setAlertAllies(),
                };
        return goals;
    }


    //Every tick, spawn a short range cursor
    @Override
    public void aiStep() {
        super.aiStep();

        // Only on the client side, spawn dust particles with a specific color
        // Have the partciles fly in random directions
        if (level().isClientSide)
        {
            Random random = new Random();
            //Choose a random position in the hitbox
            Vec3 randomPos = new Vec3(position().x + random.nextFloat(0.5F), position().y + random.nextFloat(0.5F), position().z + random.nextFloat(0.5F));

            for (int i = 0; i < 1; i++)
            {
                level().addParticle(ModParticles.SCULK_CRUST_PARTICLE.get(), randomPos.x, randomPos.y, randomPos.z, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3);
            }
            return;
        }

        // Prevent from Drowning
        this.setAirSupply(getMaxAirSupply());

        Random random = new Random();
        boolean passRandomChance = random.nextInt(100) == 0;
        boolean isCursorNullOrDead = cursor == null || cursor.isSetToBeDeleted();
        boolean isBlockInfestationEnabled = ModConfig.SERVER.block_infestation_enabled.get();
        // The reason we do this instead of just checking if the horde is active is because sometimes people will spawn these
        // without activating the horde.
        boolean isTheHordeNotDefeated = !SculkHorde.savedData.isHordeDefeated();
        boolean canSpawnCursor = passRandomChance && isCursorNullOrDead && isBlockInfestationEnabled && isTheHordeNotDefeated;

        if (canSpawnCursor && !SculkHorde.cursorSystem.isCursorPopulationAtMax()) {
            level().getServer().tell(new net.minecraft.server.TickTask(level().getServer().getTickCount() + 1, () -> {

                // Spawn Block Traverser
                Optional<VirtualSurfaceInfestorCursor> possibleCursor = CursorSystem.createSurfaceInfestorVirtualCursor(level(), blockPosition());
                if(possibleCursor.isPresent())
                {
                    cursor = possibleCursor.get();
                    cursor.setMaxTransformations(100);
                    cursor.setMaxRange(100);
                    cursor.setTickIntervalTicks(TickUnits.convertSecondsToTicks(0.5F));
                    cursor.setSearchIterationsPerTick(1);
                }
            }));
            triggerAnim("spread_controller", "spread_animation");
        }

        if (level().getGameTime() - lastInfectionTime > INFECTION_INTERVAL_TICKS)
        {
            lastInfectionTime = level().getGameTime();
            // Any entity within 10 blocks of the spewer will be infected
            ArrayList<LivingEntity> entities = (ArrayList<LivingEntity>) EntityAlgorithms.getNonSculkEntitiesAtBlockPos((ServerLevel) level(), this.blockPosition(), 10);
            for (LivingEntity victim : entities)
            {
                if(!((ISculkSmartEntity) this).getTargetParameters().isEntityValidTarget(victim, false))
                {
                    return;
                }

                EntityAlgorithms.reducePurityEffectDuration(victim, TickUnits.convertMinutesToTicks(1));
                EntityAlgorithms.applyEffectToTarget(victim, ModMobEffects.SCULK_INFECTION.get(), TickUnits.convertSecondsToTicks(15), 0);
                EntityAlgorithms.applyEffectToTarget(victim, ModMobEffects.SCULK_LURE.get(), TickUnits.convertMinutesToTicks(10), 0);

            }
        }
    }


    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation CORPSE_IDLE_ANIMATION = RawAnimation.begin().thenLoop("corpse");

    private static final RawAnimation SPREAD_ANIMATION = RawAnimation.begin().thenPlay("corpse.spread");
    private final AnimationController<SculkPhantomCorpseEntity> SPREAD_ANIMATION_CONTROLLER = new AnimationController<>(this, "spread_controller", state -> PlayState.STOP)
            .triggerableAnim("spread_animation", SPREAD_ANIMATION).transitionLength(5);
    protected PlayState pose(AnimationState<SculkPhantomCorpseEntity> state)
    {
        state.setAnimation(CORPSE_IDLE_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "body", 5, this::pose));
        controllers.add(SPREAD_ANIMATION_CONTROLLER);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * This is a custom goal that I made to make the mob die after a certain amount of time.
     * This is useful for mobs that are meant to be temporary, such as the Sculk Spore Spewer.
     */
    private class dieAfterTimeGoal extends Goal
    {
        private final SculkPhantomCorpseEntity entity;

        public dieAfterTimeGoal(SculkPhantomCorpseEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void start() {
        }

        @Override
        public void tick()
        {
            if(level().isClientSide())
            {
                return;
            }

            entityData.set(DATA_TICKS_ALIVE, entityData.get(DATA_TICKS_ALIVE) + 1);
            int ticksAlive = entityData.get(DATA_TICKS_ALIVE);
            if (ticksAlive > TickUnits.convertMinutesToTicks(15)) {
                entity.remove(RemovalReason.DISCARDED);
            }
        }
    }

    private class DestroyLeavesTouchingHitBoxGoal extends Goal
    {
        private final SculkPhantomCorpseEntity entity;

        private final long EXECUTION_COOLDOWN = TickUnits.convertSecondsToTicks(5);
        private long lastExecutionTime = 0;

        TagKey<Block> isLeaves = BlockTags.LEAVES;

        public DestroyLeavesTouchingHitBoxGoal(SculkPhantomCorpseEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean canUse() {

            return entity.level().getGameTime() - lastExecutionTime > EXECUTION_COOLDOWN;
        }

        @Override
        public void start() {
            AABB hitBox = entity.getBoundingBox().inflate(1);
            // Check hitbox for leaves block
            for (int x = (int) hitBox.minX; x < hitBox.maxX; x++)
            {
                for (int y = (int) hitBox.minY; y < hitBox.maxY; y++)
                {
                    for (int z = (int) hitBox.minZ; z < hitBox.maxZ; z++)
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState blockAtPosition = entity.level().getBlockState(pos);

                        if (blockAtPosition.is(isLeaves))
                        {
                            entity.level().destroyBlock(pos, false);
                            lastExecutionTime = entity.level().getGameTime();
                        }
                    }
                }
            }

        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    public boolean dampensVibrations() {
        return true;
    }

    String DATA_TICKS_ALIVE_IDENTIFIER = "ticks_alive";

    // ###### Data Code ########
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_TICKS_ALIVE, 0);
    }

    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putInt(DATA_TICKS_ALIVE_IDENTIFIER, this.entityData.get(DATA_TICKS_ALIVE));
    }

    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(DATA_TICKS_ALIVE, nbt.getInt(DATA_TICKS_ALIVE_IDENTIFIER));
    }

    /* DO NOT USE THIS FOR ANYTHING, CAUSES DESYNC
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.savedData.addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }
    */
}
