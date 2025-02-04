package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.ImprovedFlyingNavigator;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class SculkPhantomEntity extends FlyingMob implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added client/model/entity/ SculkPhantomModel.java<br>
     * Added client/renderer/entity/ SculkPhantomRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 15F;
    //The armor of the mob
    public static final float ARMOR = 1F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 3F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 2F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 32F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.35F;

    // Controls what types of entities this mob can target
    protected final TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetPassives().enableTargetHostiles().ignoreTargetBelow50PercentHealth().disableTargetingEntitiesInWater();

    protected BlockPos anchorPoint = BlockPos.ZERO;
    public static final int TICKS_PER_FLAP = Mth.ceil(24.166098F);
    Vec3 moveTargetPoint = new Vec3(anchorPoint.getX(), anchorPoint.getY(), anchorPoint.getZ());

    ArrayList<BlockPos> searchPositions = new ArrayList<>();
    Vec3 spawnPoint = null;

    protected boolean isScouter = false;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkPhantomEntity(EntityType<? extends SculkPhantomEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static void spawnPhantom(Level worldIn, BlockPos spawnPos, boolean isScouter)
    {
        SculkPhantomEntity phantom = ModEntities.SCULK_PHANTOM.get().create(worldIn);
        assert phantom != null;
        phantom.setScouter(isScouter);
        phantom.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        phantom.spawnPoint = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        worldIn.addFreshEntity(phantom);
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
                .add(Attributes.FLYING_SPEED, 3F)
                .add(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get(), 0.0);
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
        return new Goal[]{
                new Despawn(this, TickUnits.convertMinutesToTicks(15)),
                new FallToTheGroundIfMobsUnder(),
                new SweepAttackGoal(),
                new selectRandomLocationToVisit(),
                new SculkPhantomGoToAnchor(this),
                new SculkPhantomWanderGoal(this, 1.0F, TickUnits.convertSecondsToTicks(3), 10)
        };
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
        return new Goal[]{
                new InvalidateTargetGoal(this),
                new TargetAttacker(this),
                new NearestLivingEntityTargetGoal<>(this, true, false)
        };
    }

    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        ImprovedFlyingNavigator flyingpathnavigation = new ImprovedFlyingNavigator(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    /** Getters and Setters **/

    public Vec3 getAnchorPoint() {
        return this.anchorPoint.getCenter();
    }

    @Override
    public SquadHandler getSquad() {
        return null;
    }

    @Override
    public boolean isParticipatingInRaid() {
        return false;
    }

    @Override
    public void setParticipatingInRaid(boolean isParticipatingInRaidIn) {
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    public double getPassengersRidingOffset() {
        return this.getEyeHeight();
    }

    public boolean isIdle() {
        return getTarget() == null;
    }

    public boolean isScouter() {
        return isScouter;
    }

    public void setScouter(boolean isScouter) {
        this.isScouter = isScouter;
    }

    /** Attributes **/

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        // Calculate the difference between the entity's position and the camera's position
        double deltaX = this.getX() - cameraX;
        double deltaY = this.getY() - cameraY;
        double deltaZ = this.getZ() - cameraZ;
        // Calculate the squared distance between the entity and the camera
        double squaredDistance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        // Return true if the entity is within the rendering distance, false otherwise
        return this.shouldRenderAtSqrDistance(squaredDistance);
    }
    @Override
    public boolean shouldRenderAtSqrDistance(double squaredDistance) {

        // Get the size of the entity's bounding box
        double size = this.getBoundingBox().getSize();
        // If the size is not a valid number, set it to 1.0
        if (Double.isNaN(size)) {
            size = 1.0D;
        }

        // Multiply the size by a constant factor and the view scale
        size *= 64.0D * getViewScale();
        // Return true if the squared distance is less than the cubed of the size, false otherwise
        return squaredDistance < size * size * size;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }


    @Override
    public void checkDespawn() {}

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    public boolean dampensVibrations() {
        return true;
    }

    public boolean isFlapping() {
        return (this.getUniqueFlapTickOffset() + this.tickCount) % TICKS_PER_FLAP == 0;
    }

    protected float getStandingEyeHeight(@NotNull Pose p_33136_, EntityDimensions p_33137_) {
        return p_33137_.height * 0.35F;
    }

    public int getUniqueFlapTickOffset() {
        return this.getId() * 3;
    }

    // This method allows the entity to travel in a given direction
    @Override
    public void travel(@NotNull Vec3 direction) {
        // If the entity is controlled by the local player
        if (this.isControlledByLocalInstance()) {
            // Move the entity relative to its orientation and the direction vector
            this.moveRelative(getTarget() == null ? 0.04F : 0.05F, direction);

            // Move the entity according to its current velocity
            this.move(MoverType.SELF, this.getDeltaMovement());

            // If the entity is in water, reduce its velocity by 10%
            if (this.isInWater()) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9F));
                // If the entity is in lava, reduce its velocity by 40%
            } else if (this.isInLava()) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.6F));
            }
            else
            {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95F));
            }
        }

        // Update the entity's animation based on its movement
        this.calculateEntityAnimation(false);
    }

    /** Events **/

    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
            float f = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount) * 7.448451F * ((float)Math.PI / 180F) + (float)Math.PI);
            float f1 = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount + 1) * 7.448451F * ((float)Math.PI / 180F) + (float)Math.PI);
            if (f > 0.0F && f1 <= 0.0F) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, this.getSoundSource(), 0.95F + this.random.nextFloat() * 0.05F, 0.95F + this.random.nextFloat() * 0.05F, false);
            }

            float f2 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F)) * (1.3F + 0.21F);
            float f3 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F)) * (1.3F + 0.21F);
            float f4 = (0.3F + f * 0.45F) * (0.2F + 1.0F);
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() + (double)f2, this.getY() + (double)f4, this.getZ() + (double)f3, 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() - (double)f2, this.getY() + (double)f4, this.getZ() - (double)f3, 0.0D, 0.0D, 0.0D);
            return;
        }

        if(spawnPoint == null)
        {
            spawnPoint = new Vec3(getX(), getY(), getZ());
        }

        // If this phantom is not scouting, don't bother chunk loading.
        if(isScouter() && ModConfig.SERVER.should_phantoms_load_chunks.get())
        {
            EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(this,5, 3, TickUnits.convertMinutesToTicks(1));
        }
    }

    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor p_33126_, @NotNull DifficultyInstance p_33127_, @NotNull MobSpawnType p_33128_, @Nullable SpawnGroupData p_33129_, @Nullable CompoundTag p_33130_) {
        this.anchorPoint = this.blockPosition().above(5);
        return super.finalizeSpawn(p_33126_, p_33127_, p_33128_, p_33129_, p_33130_);
    }

    protected @NotNull BodyRotationControl createBodyControl() {
        return new PhantomBodyRotationControl(this);
    }

    /** Save Data **/

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AX")) {
            this.anchorPoint = new BlockPos(tag.getInt("AX"), tag.getInt("AY"), tag.getInt("AZ"));
        }
        isScouter = tag.getBoolean("scouter");
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AX", this.anchorPoint.getX());
        tag.putInt("AY", this.anchorPoint.getY());
        tag.putInt("AZ", this.anchorPoint.getZ());
        tag.putBoolean("scouter", isScouter);

    }

    /** Animation **/
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation TUMOR_IDLE_ANIMATION = RawAnimation.begin().thenLoop("tumor");

    protected PlayState poseTumorCycle(AnimationState<SculkPhantomEntity> state)
    {
        state.setAnimation(TUMOR_IDLE_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this).transitionLength(5));
        controllers.add(new AnimationController<>(this, "blob_idle", 5, this::poseTumorCycle));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Sounds **/

    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    protected void tellServerToSpawnCorpseNextTick()
    {
        if(level().isClientSide()) { return; }

        level().getServer().tell(new TickTask(level().getServer().getTickCount() + 1, () -> {
            SculkPhantomEntity.this.discard();
            SculkPhantomCorpseEntity corpse = new SculkPhantomCorpseEntity(ModEntities.SCULK_PHANTOM_CORPSE.get(), level());
            corpse.setPos(SculkPhantomEntity.this.getX(), SculkPhantomEntity.this.getY(), SculkPhantomEntity.this.getZ());
            level().addFreshEntity(corpse);

            // Give spore spewer slow falling
            EntityAlgorithms.applyEffectToTarget(corpse, MobEffects.SLOW_FALLING, TickUnits.convertSecondsToTicks(20), 0);
        }));
    }

    protected void dieAndSpawnCorpse()
    {
        tellServerToSpawnCorpseNextTick();
    }

    protected class FallToTheGroundIfMobsUnder extends Goal
    {
        protected long lastTimeOfCheck = 0;
        protected long checkCooldown = TickUnits.convertSecondsToTicks(15);

        protected final int mobCheckRadius = 32;

        public boolean canUse()
        {
            boolean cooldownNotMet = level().getGameTime() - lastTimeOfCheck < checkCooldown;
            boolean isNotScouter = !isScouter();
            boolean spawnPointIsNull = spawnPoint == null;

            if(cooldownNotMet || isNotScouter || spawnPointIsNull)
            {
                return false;
            }

            // If less than 300 blocks from spawn point, do not explode.
            if(BlockAlgorithms.getBlockDistanceXZ(blockPosition(), BlockPos.containing(spawnPoint)) < 100)
            {
                return false;
            }

            lastTimeOfCheck = level().getGameTime();

            //Spawn Bounding Box on floor and check for mobs
            BlockPos groundBlockPos = BlockAlgorithms.getGroundBlockPos(level(), blockPosition(), level().getMaxBuildHeight());

            // Find any non-sculk mobs in the area
            List<LivingEntity> nearbyMobs = EntityAlgorithms.getNonSculkEntitiesAtBlockPos((ServerLevel) level(), groundBlockPos, mobCheckRadius);

            for(LivingEntity mob : nearbyMobs)
            {
                boolean isValidTarget = ((ISculkSmartEntity) SculkPhantomEntity.this).getTargetParameters().isEntityValidTarget(mob, false);
                if(isValidTarget)
                {
                    return true;
                }
            }

            return false;
        }

        public boolean canContinueToUse()
        {
            return false;
        }

        public void start()
        {
            dieAndSpawnCorpse();
        }
    }

    public boolean isAnchorPosValid(BlockPos pos)
    {
        boolean isThereIsNoFluid = level().getFluidState(pos).isEmpty() && level().getFluidState(pos.below()).isEmpty();
        boolean isItFarEnoughAway = distanceToSqr(Vec3.atCenterOf(pos)) > 30;
        // As long as its not a fluid, its valid
        return isThereIsNoFluid && isItFarEnoughAway;
    }

    protected class selectRandomLocationToVisit extends Goal
    {
        protected long lastTimeOfExecution = 0;
        protected boolean hasExecutedOnce = false;
        protected int circleRadiusVariance = 50;
        protected final int BASE_CIRCLE_RADIUS = 200;
        protected final int CIRCLE_RADIUS_INCREASE = 100;
        protected int currentCircleRadius = BASE_CIRCLE_RADIUS + circleRadiusVariance;

        protected long gameTimeOfFirstEnteringAreaOfAnchor = 0;
        protected final long TIME_TO_WAIT_BEFORE_MOVING_ON = TickUnits.convertSecondsToTicks(60);

        public boolean canUse()
        {
            //boolean cooldownNotMet = level().getGameTime() - lastTimeOfExecution < executionCooldown;
            boolean isNotScouter = !isScouter();
            boolean hasTarget = getTarget() != null;
            boolean isWithin100BlocksOfAnchor = distanceToSqr(Vec3.atCenterOf(anchorPoint)) <= 100;
            boolean isItTimeToMoveOn = level().getGameTime() - gameTimeOfFirstEnteringAreaOfAnchor > TIME_TO_WAIT_BEFORE_MOVING_ON && gameTimeOfFirstEnteringAreaOfAnchor != 0;

            if(isNotScouter)
            {
                return false;
            }

            // Remember when we first entered the area of the anchor
            if(isWithin100BlocksOfAnchor && gameTimeOfFirstEnteringAreaOfAnchor == 0)
            {
                gameTimeOfFirstEnteringAreaOfAnchor = level().getGameTime();
            }

            if((!isItTimeToMoveOn || hasTarget) && hasExecutedOnce)
            {
                return false;
            }

            return level().canSeeSky(blockPosition().above());
        }

        public boolean canContinueToUse()
        {
            return false;
        }

        public Vec3 getRandomTravelLocationVec3()
        {
            int MAX_ATTEMPTS = 5;

            if(searchPositions.isEmpty())
            {
                searchPositions = BlockAlgorithms.getPointsOnCircumference(blockPosition(), MAX_ATTEMPTS, currentCircleRadius);
                Collections.shuffle(searchPositions);
                currentCircleRadius += CIRCLE_RADIUS_INCREASE;
            }

            for(BlockPos searchPos : searchPositions)
            {
                BlockPos groundBlockPos = BlockAlgorithms.getGroundBlockPos(level(), searchPos, level().getMaxBuildHeight());
                BlockPos potentialNewAnchorPoint = groundBlockPos.above(20);
                if(isAnchorPosValid(potentialNewAnchorPoint))
                {
                    gameTimeOfFirstEnteringAreaOfAnchor = 0;
                    return potentialNewAnchorPoint.getCenter();
                }

                //Else remove it from the list
                searchPositions.remove(searchPos);
                break;
            }

            return moveTargetPoint;
        }

        public void start()
        {
            lastTimeOfExecution = level().getGameTime();
            moveTargetPoint = getRandomTravelLocationVec3();
            anchorPoint = BlockPos.containing(moveTargetPoint);
            hasExecutedOnce = true;
        }
    }

    abstract static class MoveTargetGoal extends Goal {
        public MoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }
    }

    class SweepAttackGoal extends MoveTargetGoal {

        private long lastTimeOfAttack = 0;
        private final int COOLDOWN = TickUnits.convertSecondsToTicks(0);

        public boolean canUse() {

            if(level().getGameTime() - lastTimeOfAttack < COOLDOWN)
            {
                return false;
            }

            return SculkPhantomEntity.this.getTarget() != null;
        }

        public boolean canContinueToUse()
        {
            LivingEntity target = SculkPhantomEntity.this.getTarget();
            if (target == null)
            {
                return false;
            }

            if (!target.isAlive())
            {
                return false;
            }

            return this.canUse();
        }

        public void stop() {
            SculkPhantomEntity.this.setTarget(null);
        }

        public void tick() {
            LivingEntity target = SculkPhantomEntity.this.getTarget();
            boolean isPhantomNull = target == null;

            if(isPhantomNull)
            {
                return;
            }

            SculkPhantomEntity.this.getNavigation().moveTo(target, 2.0D);
            //AABB boundingBox = SculkPhantomEntity.this.getBoundingBox().inflate(0.2F);
            float attackReach = (getBbWidth()/2) + 2;
            //boolean doesPhantomIntersectTarget = boundingBox.intersects(target.getBoundingBox());
            boolean doesPhantomIntersectTarget = SculkPhantomEntity.this.distanceTo(target) <= attackReach;

            if (doesPhantomIntersectTarget)
            {
                SculkPhantomEntity.this.doHurtTarget(target);
                EntityAlgorithms.reducePurityEffectDuration(target, TickUnits.convertMinutesToTicks(5));
                EntityAlgorithms.applyEffectToTarget(target, ModMobEffects.DISEASED_CYSTS.get(), TickUnits.convertSecondsToTicks(30), 0);
                lastTimeOfAttack = level().getGameTime();
                return;
            }

            if (SculkPhantomEntity.this.horizontalCollision || SculkPhantomEntity.this.hurtTime > 0) {
                lastTimeOfAttack = level().getGameTime();
            }
        }
    }

    class Despawn extends DespawnAfterTime
    {
        public Despawn(ISculkSmartEntity mob, int ticksThreshold) {
            super(mob, ticksThreshold);
        }

        public long calculateTicksThreshold()
        {
            if(isScouter()) { return ticksThreshold; }

            return ticksThreshold/3;
        }

        @Override
        public boolean canUse()
        {
            boolean mobHasBeenNameTagged = ((Mob) mob).hasCustomName();
            if(level.getGameTime() - creationTime > calculateTicksThreshold() && !mob.isParticipatingInRaid() && !mobHasBeenNameTagged)
            {
                return true;
            }
            return false;
        }

        @Override
        public void start()
        {
            if(isScouter()) { discard(); }
        }
    }

    class PhantomBodyRotationControl extends BodyRotationControl {
        public PhantomBodyRotationControl(Mob p_33216_) {
            super(p_33216_);
        }

        public void clientTick() {
            SculkPhantomEntity.this.yHeadRot = SculkPhantomEntity.this.yBodyRot;
            SculkPhantomEntity.this.yBodyRot = SculkPhantomEntity.this.getYRot();
        }
    }
}
