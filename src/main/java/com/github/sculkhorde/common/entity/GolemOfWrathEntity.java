package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.block.GolemOfWrathAnimatorBlock;
import com.github.sculkhorde.common.entity.goal.CustomMeleeAttackGoal;
import com.github.sculkhorde.common.entity.goal.NearestSculkOrSculkAllyEntityTargetGoal;
import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.SoundUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GolemOfWrathEntity extends PathfinderMob implements GeoEntity, IPurityGolemEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.core.ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}.java<br>
     * Added {@link GolemOfWrathEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.GolemOfWrathModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.GolemOfWrathRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 200F;
    //The armor of the mob
    public static final float ARMOR = 30F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 10F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 3F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 32F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.45F;
    public static final float KNOCKBACK_RESISTANCE = 100.0F;

    // Controls what types of entities this mob can target
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected BlockPos boundBlockPos = null;
    protected boolean belongsToBoundBlock = false;


    public GolemOfWrathEntity(Level worldIn) {
        super(ModEntities.GOLEM_OF_WRATH.get(), worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public GolemOfWrathEntity(EntityType<? extends GolemOfWrathEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

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
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, MOVEMENT_SPEED);
    }


    // #### Events ####

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        super.registerGoals();

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
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new GroundSlamAttackGoal(),
                        new MeleeAttackGoal(),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new WaterAvoidingRandomStrollGoal(this, 0.7D),
                        //LookRandomlyGoal(mob)
                        new RandomLookAroundGoal(this),
                        new OpenDoorGoal(this, true)
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
                        new NearestSculkOrSculkAllyEntityTargetGoal<>(this, true, true),
                        new HurtByTargetGoal(this)
                };
        return goals;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if(!hasEffect(ModMobEffects.PURITY.get()))
        {
            MobEffectInstance effect = new MobEffectInstance(ModMobEffects.PURITY.get(), Integer.MAX_VALUE, 1);
            addEffect(effect);
        }

        if(!hasEffect(MobEffects.REGENERATION))
        {
            MobEffectInstance effect = new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 0);
            addEffect(effect);
        }

        if(hasEffect(ModMobEffects.CORRODED.get()))
        {
            removeEffect(ModMobEffects.CORRODED.get());
        }

        // If we do not belong to a block, ignore
        if(!belongsToBoundBlock())
        {
            return;
        }
        // If we are block bound, but it is destroyed, die
        else if(!isBoundBlockPresent())
        {
            this.hurt(damageSources().genericKill(), Integer.MAX_VALUE);
        }
        // If we are block bound, but we travel too far from it, just die.
        else if(BlockAlgorithms.getBlockDistance(blockPosition(), getBoundBlockPos().get()) > getMaxDistanceFromBoundBlockBeforeDeath())
        {
            this.hurt(damageSources().genericKill(), Integer.MAX_VALUE);
        }

    }

    @Override
    protected void doPush(Entity pusher) {
        if(pusher instanceof LivingEntity livingEntity)
        {
            if ((EntityAlgorithms.isSculkLivingEntity.test(livingEntity) || EntityAlgorithms.isLivingEntityAllyToSculkHorde(livingEntity)) && this.getRandom().nextInt(20) == 0) {
                this.setTarget(livingEntity);
            }
        }
    }

    @Override
    public boolean belongsToBoundBlock() {
        return belongsToBoundBlock;
    }

    @Override
    public boolean isBoundBlockPresent() {


        return level().getBlockState(getBoundBlockPos().get()).getBlock() instanceof GolemOfWrathAnimatorBlock;
    }

    @Override
    public Optional<BlockPos> getBoundBlockPos() {
        return Optional.ofNullable(boundBlockPos);
    }

    @Override
    public void setBoundBlockPos(BlockPos pos)
    {
        boundBlockPos = pos;
        belongsToBoundBlock = true;
    }

    @Override
    public int getMaxDistanceFromBoundBlockBeforeDeath() {
        return 100;
    }

    @Override
    public int getMaxTravelDistanceFromBoundBlock() {
        return getMaxDistanceFromBoundBlockBeforeDeath() - 20;
    }

    // #### Animation Code ####

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // #### Sound Code ####

    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_REPAIR;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.15F, 1.0F);
    }

    class MeleeAttackGoal extends CustomMeleeAttackGoal
    {

        public MeleeAttackGoal()
        {
            super(GolemOfWrathEntity.this, 1.0D, false, 10);
        }

        @Override
        public boolean canUse()
        {
            return mob.getTarget() != null;
        }

        @Override
        public boolean canContinueToUse()
        {
            return canUse();
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget)
        {
            float entityBoundingBoxWidth = GolemOfWrathEntity.this.getBbWidth();
            return entityBoundingBoxWidth * 2.0F + pAttackTarget.getBbWidth();
        }

        @Override
        protected int getAttackInterval() {
            return TickUnits.convertSecondsToTicks(2);
        }

        @Override
        protected void triggerAnimation() {
            //((SculkRavagerEntity)mob).triggerAnim("attack_controller", "attack_animation");
        }

        @Override
        public void onTargetHurt(LivingEntity target)
        {
            AABB hitbox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(target.position(), 10);
            List<LivingEntity> enemies = EntityAlgorithms.getEntitiesExceptOwnerInBoundingBox(mob, (ServerLevel) mob.level(), hitbox);
            for(LivingEntity entity : enemies)
            {
                entity.hurt(mob.damageSources().mobAttack(mob), GolemOfWrathEntity.ATTACK_DAMAGE);
            }
            CursorSurfacePurifierEntity cursor = new CursorSurfacePurifierEntity(mob.level());
            cursor.setPos(target.position());
            cursor.setTickIntervalMilliseconds(10);
            cursor.setMaxLifeTimeMillis(TimeUnit.SECONDS.toMillis(60));
            cursor.setMaxTransformations(20);
            mob.level().addFreshEntity(cursor);
        }
    }

    class GroundSlamAttackGoal extends Goal
    {

        protected long ATTACK_COOLDOWN = TickUnits.convertSecondsToTicks(6);
        protected long timeOfLastAttack = 0;

        protected long CHECK_INTERVAL = TickUnits.convertSecondsToTicks(2);
        protected long timeOfLastCheck = 0;

        protected long timeOfAttackStart = 0;
        protected long DAMAGE_DELAY = TickUnits.convertSecondsToTicks(1);
        protected boolean isAttackOver = false;

        @Override
        public boolean canUse() {

            if(Math.abs(level().getGameTime() - timeOfLastAttack) < ATTACK_COOLDOWN)
            {
                return false;
            }

            if (Math.abs(level().getGameTime() - timeOfLastCheck) >= CHECK_INTERVAL) {
                timeOfLastCheck = level().getGameTime();

                List<LivingEntity> hostiles = EntityAlgorithms.getSculkHordeOrAllyEntitiesInBoundingBox((ServerLevel) level(), getBoundingBox().inflate(7));

                return hostiles.size() > 4;
            }


            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return !isAttackOver;
        }

        @Override
        public void start() {
            timeOfAttackStart = level().getGameTime();
            timeOfLastAttack = timeOfAttackStart;
        }

        @Override
        public void tick() {
            super.tick();

            if (level().isClientSide()) {
                return;
            }

            if (Math.abs(level().getGameTime() - timeOfAttackStart) >= DAMAGE_DELAY) {
                List<LivingEntity> entities = EntityAlgorithms.getEntitiesExceptOwnerInBoundingBox(GolemOfWrathEntity.this, (ServerLevel) level(), getBoundingBox().inflate(7));
                float pushAwayStrength = 5f; // Increased push strength for better outwards effect
                float pushUpStrength = 3f;   // Separate push up strength for vertical component.

                for (LivingEntity entity : entities)
                {
                    EntityAlgorithms.pushAwayEntitiesFromPosition(position(), entity, pushAwayStrength, pushUpStrength);
                }
                SoundUtil.playHostileSoundInLevel(level(), blockPosition(), SoundEvents.RAVAGER_ATTACK);
                isAttackOver = true;
            }
        }

        @Override
        public void stop() {
            super.stop();
            isAttackOver = false;
        }
    }
}
