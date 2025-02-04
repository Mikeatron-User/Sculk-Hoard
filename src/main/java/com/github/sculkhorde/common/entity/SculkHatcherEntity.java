package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The Sculk Hatcher.
 * @see com.github.sculkhorde.client.renderer.entity.SculkHatcherRenderer
 * @see com.github.sculkhorde.client.model.enitity.SculkHatcherModel
 */
public class SculkHatcherEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.common.world.ModWorldEvents} (this might not be necessary)<br>
     * Edited {@link com.github.sculkhorde.common.world.gen.ModEntityGen}<br>
     * Added {@link SculkHatcherEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkHatcherModel} <br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkHatcherRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 30F;
    //The armor of the mob
    public static final float ARMOR = 4F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 3F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 16F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.25F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().ignoreTargetBelow50PercentHealth().enableMustReachTarget();
    private SquadHandler squad = new SquadHandler(this);
    //factory The animation factory used for animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkHatcherEntity(EntityType<? extends SculkHatcherEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkHatcherEntity(Level worldIn) {super(ModEntities.SCULK_HATCHER.get(), worldIn);}

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
    public void checkDespawn() {}

    public boolean isIdle() {
        return getTarget() == null;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.COW_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.COW_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlockIn) {
        this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
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
                        new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(15)),
                        new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(2)),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new SquadHandlingGoal(this),
                        new ChuckMiteAttackGoal(this),
                        new AttackGoal(),
                        new FollowSquadLeader(this),
                        new PathFindToRaidLocation<>(this),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true),
                        //new RangedAttackGoal(this, new AcidAttack(this), 20),
                        //LookAtGoal(mob, targetType, lookDistance)
                        new LookAtPlayerGoal(this, Pig.class, 8.0F),
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
                        new InvalidateTargetGoal(this),
                        new TargetAttacker(this),
                        new FocusSquadTarget(this),
                        new NearestLivingEntityTargetGoal<>(this, true, true)
                };
        return goals;
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/

    private static final RawAnimation BODY_IDLE_ANIMATION = RawAnimation.begin().thenPlay("body.idle");
    private static final RawAnimation BODY_WALK_ANIMATION = RawAnimation.begin().thenPlay("body.walk");
    private static final RawAnimation LEGS_IDLE_ANIMATION = RawAnimation.begin().thenPlay("legs.idle");
    private static final RawAnimation LEGS_WALK_ANIMATION = RawAnimation.begin().thenLoop("legs.walk");
    private static final RawAnimation HEAD_ATTACK_ANIMATION = RawAnimation.begin().thenPlay("head.attack");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "Legs", 5, this::poseLegs),
                new AnimationController<>(this, "Body", 5, this::poseBody),
                DefaultAnimations.genericAttackAnimation(this, HEAD_ATTACK_ANIMATION),
                DefaultAnimations.genericLivingController(this)
        );
    }

    // Create the animation handler for the leg segment
    protected PlayState poseLegs(AnimationState<SculkHatcherEntity> state)
    {
        if(state.isMoving())
        {
            state.setAnimation(LEGS_WALK_ANIMATION);
        }
        else
        {
            //state.setAnimation(LEGS_IDLE_ANIMATION);
            state.setAnimation(LEGS_IDLE_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    // Create the animation handler for the body segment
    protected PlayState poseBody(AnimationState<SculkHatcherEntity> state)
    {
        if(state.isMoving())
        {
            state.setAnimation(BODY_WALK_ANIMATION);
        }
        else
        {
            state.setAnimation(BODY_IDLE_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private boolean isParticipatingInRaid = false;

    @Override
    public SquadHandler getSquad() {
        return squad;
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

    /** ~~~~~~~~ CLASSES ~~~~~~~~ **/

    public class ChuckMiteAttackGoal extends Goal
    {

        protected final SculkHatcherEntity mob;
        protected long timeOfLastMiteSpawn = 0;
        protected long MITE_SPAWN_COOLDOWN = TickUnits.convertSecondsToTicks(3);

        /**
         * The Constructor
         * @param mob The mob that called this
         */
        public ChuckMiteAttackGoal(SculkHatcherEntity mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse()
        {
            boolean canWeUse = ((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
            return canWeUse;
        }

        @Override
        public boolean canContinueToUse()
        {
            return false;
        }


        /**
         * Gets called every tick the attack is active<br>
         * We shouldn't have to check if the target is null since
         * the super class does this. However, something funky is going on that
         * causes a null pointer exception if we dont check this here. This is
         * absolutely some sort of bug that I was unable to figure out. For the
         * time being (assuming I ever fix this), this will have to do.
         */
        public void start()
        {
            if(this.mob.getTarget() == null)
            {
                return;
            }
            if(Math.abs(mob.level().getGameTime() - timeOfLastMiteSpawn) < MITE_SPAWN_COOLDOWN)
            {
                return;
            }

            if(mob.getHealth() <= SculkMiteEntity.MAX_HEALTH)
            {
                return;
            }

            timeOfLastMiteSpawn = mob.level().getGameTime();
            spawnMite();
        }
    }

    protected void spawnMite()
    {

        level().getServer().tell(new TickTask(level().getServer().getTickCount() + 1, () -> {

            SculkMiteEntity mite = ModEntities.SCULK_MITE.get().spawn((ServerLevel) this.level(), this.blockPosition().above(), MobSpawnType.SPAWNER);

            // Get the target (can be null)
            LivingEntity target = getTarget();

            if (target != null) {
                // Calculate the direction vector to the target
                Vec3 targetPosition = target.getEyePosition().add(0,1,0);
                Vec3 thisPosition = Vec3.atCenterOf(this.blockPosition());
                Vec3 direction = targetPosition.subtract(thisPosition).normalize();
                int targetDistance = (int) distanceTo(getTarget());


                // Adjust the velocity magnitude.
                double launchSpeed = Math.min(3, Math.max(1, targetDistance / 4));
                Vec3 launchVelocity = direction.scale(launchSpeed);


                mite.setDeltaMovement(launchVelocity);

            }

            this.hurt(damageSources().generic(), SculkMiteEntity.MAX_HEALTH);
        }));
    }

    public boolean dampensVibrations() {
        return true;
    }


    /** ~~~~~~~~ CLASSES ~~~~~~~~ **/

    class AttackGoal extends CustomMeleeAttackGoal
    {

        public AttackGoal()
        {
            super(SculkHatcherEntity.this, 1.0D, false, 10);
        }

        @Override
        public boolean canUse()
        {
            boolean canWeUse = ((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
            // If the mob is already targeting something valid, don't bother
            return canWeUse;
        }

        @Override
        public boolean canContinueToUse()
        {
            return canUse();
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget)
        {
            float f = SculkHatcherEntity.this.getBbWidth() - 0.1F;
            return (double)(f * 2.0F * f * 2.0F + pAttackTarget.getBbWidth());
        }

        @Override
        protected int getAttackInterval() {
            return TickUnits.convertSecondsToTicks(1);
        }

        @Override
        protected void triggerAnimation() {
            //((SculkRavagerEntity)mob).triggerAnim("attack_controller", "attack_animation");
        }
    }
}
