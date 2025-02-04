package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class SculkWitchEntity extends Monster implements GeoEntity, ISculkSmartEntity, RangedAttackMob {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/ModWorldEvents.java (this might not be necessary)<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkZombie.java<br>
     * Added client/model/entity/ SculkZombieModel.java<br>
     * Added client/renderer/entity/ SculkZombieRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 10F;
    //The armor of the mob
    public static final float ARMOR = 0F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 5F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.35F;

    public static MobEffectInstance effect = new MobEffectInstance(MobEffects.REGENERATION, TickUnits.convertMinutesToTicks(5), 1);

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().enableMustReachTarget();
    private SquadHandler squad = new SquadHandler(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkWitchEntity(EntityType<? extends SculkWitchEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);

        int rng = random.nextInt(0,6);

        switch(rng)
        {
            case 0:
                effect = new MobEffectInstance(MobEffects.REGENERATION, TickUnits.convertMinutesToTicks(5), 1);
                break;
            case 1:
                effect = new MobEffectInstance(MobEffects.DAMAGE_BOOST, TickUnits.convertMinutesToTicks(5), 0);
                break;
            case 2:
                effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, TickUnits.convertMinutesToTicks(5), 0);
                break;
            case 3:
                effect = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, TickUnits.convertMinutesToTicks(5), 0);
                break;
            case 4:
                effect = new MobEffectInstance(MobEffects.ABSORPTION, TickUnits.convertMinutesToTicks(5), 1);
                break;
            case 5:
                effect = new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TickUnits.convertMinutesToTicks(5), 0);
                break;
        }
    }

    public SculkWitchEntity(Level worldIn)
    {
        this(ModEntities.SCULK_WITCH.get(), worldIn);
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
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return getTarget() == null;
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
                        new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(5)),
                        new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(2)),
                        new OpenDoorGoal(this, true),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new SquadHandlingGoal(this),
                        new RunFromHostilesGoal<>(this, 4, 1.0F, 1.0F),
                        new BuffNearbyAllies(this),
                        // RangedAttackGoal(thisMob, speedModifier, Min Attack Interval, Max Attack Interval, Attack Radius)
                        new RangedAttackGoal(this, 1.0D, 30, 30, 10.0F),
                        new FollowSquadLeader(this),
                        new PathFindToRaidLocation<>(this),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true),

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
                        //HurtByTargetGoal(mob)
                        new TargetAttacker(this),
                        new NearestLivingEntityTargetGoal<>(this, true, true)

                };
        return goals;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount)
    {
        boolean isIndirectMagicDamageType = damageSource.is(DamageTypes.INDIRECT_MAGIC);
        boolean isMagicDamageType = damageSource.is(DamageTypes.MAGIC);
        if(isIndirectMagicDamageType || isMagicDamageType)
        {
            return false;
        }

        return super.hurt(damageSource, amount);
    }

    @Override
    protected void customServerAiStep() {
        if(!hasEffect(MobEffects.REGENERATION))
        {
            addEffect(effect);
        }
        super.customServerAiStep();
    }

    public void performRangedAttack(LivingEntity target, float p_34144_) {

        triggerAnim("attack_controller", "throwpotion");

        Vec3 vec3 = target.getDeltaMovement();
        double d0 = target.getX() + vec3.x - this.getX();
        double d1 = target.getEyeY() - (double)1.1F - this.getY();
        double d2 = target.getZ() + vec3.z - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        Potion potion = Potions.HARMING;
        float rng = random.nextFloat();

        if(rng > 0.9)
        {
            potion = Potions.HARMING;
        }
        else if(rng > 0.6)
        {
            potion = Potions.POISON;
        }
        else {
            potion = Potions.WEAKNESS;
        }

        ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
        thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
        thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
        thrownpotion.shoot(d0, d1 + d3 * 0.2D, d2, 0.75F, 8.0F);
        if (!this.isSilent()) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }

        this.level().addFreshEntity(thrownpotion);

    }

    // Animation Code

    private static final RawAnimation THROW_POTION_ANIMATION = RawAnimation.begin().thenPlay("throwpotion");
    private static final RawAnimation BUFF_ALLIES_ANIMATION = RawAnimation.begin().thenPlay("dispense");

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("throwpotion", THROW_POTION_ANIMATION).transitionLength(5)
            .triggerableAnim("dispense", BUFF_ALLIES_ANIMATION).transitionLength(5);



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this).transitionLength(5),
                ATTACK_ANIMATION_CONTROLLER,
                DefaultAnimations.genericLivingController(this)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.WITCH_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.DROWNED_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }

    private class BuffNearbyAllies extends Goal {

        private final ISculkSmartEntity thisEntity; // the skeleton mob
        private int timeToRecalcPath;
        long lastTimeOfGoalExecution = 0;
        long EXECUTION_INTERVAL = TickUnits.convertSecondsToTicks(5);
        private final int BUFF_RADIUS = 10;

        List<LivingEntity> hurtUnits;

        public BuffNearbyAllies(ISculkSmartEntity mob) {
            this.thisEntity = mob;
        }

        private Mob getMob() {
            return (Mob) this.thisEntity;
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse()
        {
            long currentTime = getMob().level().getGameTime();
            if(currentTime - lastTimeOfGoalExecution < EXECUTION_INTERVAL)
            {
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        @Override
        public void start()
        {
            this.timeToRecalcPath = 0;
            lastTimeOfGoalExecution = level().getGameTime();

            hurtUnits = EntityAlgorithms.getSculkHordeEntitiesInBoundingBox((ServerLevel) level(), EntityAlgorithms.createBoundingBoxCubeAtBlockPos(position(), BUFF_RADIUS));

            for(LivingEntity e : hurtUnits)
            {
                e.addEffect(effect);
            }

            triggerAnim("attack_controller", "dispense");
        }

        @Override
        public void tick()
        {
            if(level().isClientSide())
            {
                return;
            }


        }
    }

    public class RunFromHostilesGoal<T extends LivingEntity> extends Goal {
        protected final PathfinderMob mob;
        private final double walkSpeedModifier;
        private final double sprintSpeedModifier;
        @Nullable
        protected Optional<LivingEntity> toAvoid;
        protected final float triggerDistance;
        @Nullable
        protected Path path;
        protected final PathNavigation pathNav;
        public RunFromHostilesGoal(PathfinderMob mob, float triggerDistance, double walkSpeedMod, double sprintSpeedMod) {
            this.mob = mob;
            this.triggerDistance = triggerDistance;
            this.walkSpeedModifier = walkSpeedMod;
            this.sprintSpeedModifier = sprintSpeedMod;
            this.pathNav = mob.getNavigation();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse()
        {
            this.toAvoid = EntityAlgorithms.getNearestHostile((ServerLevel) level(), blockPosition(), getBoundingBox().inflate(triggerDistance));
            if (this.toAvoid.isEmpty()) {
                return false;
            } else {
                Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.get().position());
                if (vec3 == null) {
                    return false;
                } else if (this.toAvoid.get().distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.get().distanceToSqr(this.mob)) {
                    return false;
                } else {
                    this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                    return this.path != null;
                }
            }
        }

        public boolean canContinueToUse() {
            return !this.pathNav.isDone();
        }

        public void start() {
            this.pathNav.moveTo(this.path, this.walkSpeedModifier);

            if(!hasEffect(MobEffects.INVISIBILITY) && getHealth() < getMaxHealth())
            {
                level().playSound((Player)null, getX(), getY(), getZ(), SoundEvents.WITCH_DRINK, getSoundSource(), 1.0F, 0.8F + random.nextFloat() * 0.4F);
                addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, TickUnits.convertSecondsToTicks(10), 0));
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, TickUnits.convertSecondsToTicks(10), 0));
            }
        }

        public void stop() {
            this.toAvoid = null;
        }

        public void tick() {

            if(toAvoid.isEmpty()) { return; }

            if (this.mob.distanceToSqr(this.toAvoid.get()) < 49.0D) {
                this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
            } else {
                this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
            }

        }
    }

}
