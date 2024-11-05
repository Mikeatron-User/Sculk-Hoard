package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals.*;
import com.github.sculkhorde.common.entity.goal.ImprovedRandomStrollGoal;
import com.github.sculkhorde.common.entity.goal.InvalidateTargetGoal;
import com.github.sculkhorde.common.entity.goal.NearestLivingEntityTargetGoal;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;

public class SculkSoulReaperEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link SculkSoulReaperEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkSoulReaperModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkSoulReaperRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 200F;
    //The armor of the mob
    public static final float ARMOR = 5F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 20F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 5F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 64F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.4F;
    protected int mobDifficultyLevel = 1;

    // Controls what types of entities this mob can target
    private final TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().disableBlackListMobs();

    // Timing Variables
    protected ServerBossEvent bossEvent;

    // Animation
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected Optional<LivingEntity> hitTarget = Optional.empty();

    protected boolean isUsingSpell = false;

    protected ReaperAttackSequenceGoal currentAttack;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkSoulReaperEntity(EntityType<? extends SculkSoulReaperEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setMaxUpStep(1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.bossEvent = this.createBossEvent();
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    public SculkSoulReaperEntity(Level level, BlockPos pos)
    {
        this(ModEntities.SCULK_SOUL_REAPER.get(), level);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static SculkSoulReaperEntity spawnWithDifficulty(Level level, Vec3 pos, int mobDifficultyLevel)
    {
        SculkSoulReaperEntity entity = new SculkSoulReaperEntity(ModEntities.SCULK_SOUL_REAPER.get(), level);
        entity.setPos(pos);
        entity.setMobDifficultyLevel(mobDifficultyLevel);
        level.addFreshEntity(entity);
        return entity;
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
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // Accessors and Modifiers

    public boolean isIdle() {
        return getTarget() == null;
    }

    private boolean isParticipatingInRaid = false;

    protected SquadHandler squad = new SquadHandler(this);
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

    public int getMobDifficultyLevel()
    {
        if(hasEffect(ModMobEffects.SOUL_DISRUPTION.get()))
        {
            return Math.max(1, mobDifficultyLevel - 1);
        }

        return mobDifficultyLevel;
    }

    public void setMobDifficultyLevel(int value)
    {
        mobDifficultyLevel = value;
    }

    public Optional<LivingEntity> getHitTarget()
    {
        return hitTarget;
    }

    public void setHitTarget(LivingEntity e)
    {
        hitTarget = Optional.of(e);
    }

    public boolean getIsUsingSpell()
    {
        return isUsingSpell;
    }

    public void startUsingSpell()
    {
        isUsingSpell = true;
    }

    public void stopUsingSpell()
    {
        isUsingSpell = false;
    }

    public ReaperAttackSequenceGoal getCurrentAttack()
    {
        return currentAttack;
    }

    public void setCurrentAttack(ReaperAttackSequenceGoal goal)
    {
        currentAttack = goal;
    }

    public void clearCurrentAttack()
    {
        currentAttack = null;
    }

    public boolean isCurrentAttackOrThereIsNone(ReaperAttackSequenceGoal goal)
    {
        if(currentAttack == null) { return true; };

        return currentAttack.equals(goal);
    }

    @Override
    public void checkDespawn() {}

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new SummonVexAttackGoal(this,1 , -1));



        // #### LEVEL 1 ####

        this.goalSelector.addGoal(1, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(1), 1,1,
                new FangsAttackGoal(this),
                new FangsAttackGoal(this),
                new FangsAttackGoal(this),
                new FangsAttackGoal(this),
                new ZoltraakAttackGoal(this)
        ));

        // #### LEVEL 2 ####

        this.goalSelector.addGoal(1, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(5), 2,2,
                new ZoltraakAttackGoal(this),
                new ZoltraakAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this)
        ));

        this.goalSelector.addGoal(1, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(5), 2,2,
                new FangsAttackGoal(this),
                new FangsAttackGoal(this),
                new FangsAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new FloorSoulSpearsAttackGoal(this)
        ));

        this.goalSelector.addGoal(1, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(15), 2,2,
                new ZoltraakAttackGoal(this),
                new ZoltraakAttackGoal(this),
                new ZoltraakAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new FloorSoulSpearsAttackGoal(this)
        ));

        // #### LEVEL 3+ ####

        this.goalSelector.addGoal(1, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(30), 3,-1,
                new ShootElementalSoulProjectilesGoal(this),
                new ZoltraakAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new ZoltraakAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new ZoltraakBarrageAttackGoal(this)

        ));

        this.goalSelector.addGoal(2, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(15), 3,-1,
                new ShootElementalSoulProjectilesGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new FloorSoulSpearsAttackGoal(this)

        ));

        this.goalSelector.addGoal(3, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(5), 3,-1,
                new ZoltraakAttackGoal(this),
                new ZoltraakAttackGoal(this),
                new ZoltraakAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this)

        ));

        this.goalSelector.addGoal(5, new SoulReapterNavigator(this, 20F, 10F));
        this.goalSelector.addGoal(6, new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true));
        this.targetSelector.addGoal(0, new InvalidateTargetGoal(this));
        this.targetSelector.addGoal(1, new TargetAttacker(this));
        this.targetSelector.addGoal(2, new NearestLivingEntityTargetGoal<>(this, false, false));
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount)
    {
        boolean isIndirectMagicDamageType = damageSource.is(DamageTypes.INDIRECT_MAGIC);
        if(isIndirectMagicDamageType)
        {
            return false;
        }

        return super.hurt(damageSource, amount);
    }

    public static void doMagicDamageToTargetsInHitBox(LivingEntity sourceEntity, AABB hitbox, float damage)
    {
        // Check for entities within the hitbox
        List<Entity> entitiesHit = sourceEntity.level().getEntities(null, hitbox);

        for (Entity entity : entitiesHit) {
            // Handle entity hit logic here
            if(entity.getUUID() != sourceEntity.getUUID())
            {
                if(entity instanceof LivingEntity livingEntity)
                {
                    livingEntity.hurt(sourceEntity.damageSources().magic(), damage);
                }
            }
        }
    }

    public static void performTargetedZoltraakAttack(LivingEntity reaper, Vec3 origin, Entity target, float damage)
    {
        // Perform ray trace
        HitResult hitResult = EntityAlgorithms.getHitScanAtTarget(reaper, reaper.getEyePosition(), target, 128);

        performZoltraakAttack(reaper, hitResult, origin, damage);
    }

    public static void performZoltraakAttack(LivingEntity reaper, HitResult hitResult, Vec3 origin, float damage)
    {
        Vec3 hitVector = hitResult.getLocation();

        Vec3 targetVector = hitVector.subtract(origin);
        Vec3 direction = targetVector.normalize();

        Vec3 beamPath = hitVector.subtract(origin);

        float radius = 0.3F;
        float thickness = 10F;

        // Create a hitbox along the beam path
        AABB hitbox = new AABB(origin, hitVector).inflate(radius);

        // Damage entities in hit box
        doMagicDamageToTargetsInHitBox(reaper, hitbox, damage);

        // Spawn magic particles
        ParticleUtil.spawnParticleBeam((ServerLevel) reaper.level(), ParticleTypes.SOUL_FIRE_FLAME, origin, direction, (float) beamPath.length(), radius, thickness);

        // Make Sound
        reaper.level().playSound(reaper,reaper.blockPosition(), ModSounds.ZOLTRAAK_ATTACK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

    }

    public static void shootZoltraakBeam(Vec3 origin, Mob shooter, LivingEntity target, float damage, float radius, float thickness)
    {

        if(target == null)
        {
            return;
        }

        shooter.getLookControl().setLookAt(target.position());
        Vec3 targetVector = shooter.getTarget().getEyePosition().subtract(origin);
        Vec3 direction = targetVector.normalize();

        // Perform ray trace
        HitResult hitResult = shooter.level().clip(new ClipContext(origin, origin.add(targetVector), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));

        Vec3 hitVector = hitResult.getLocation();

        Vec3 beamPath = hitVector.subtract(origin);


        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up).normalize();
        Vec3 forward = direction.cross(right).normalize();

        // Spawn Particles
        for (float i = 1; i < Mth.floor(beamPath.length()) + 1; i += 0.3F) {
            Vec3 vec33 = origin.add(direction.scale((double) i));

            // Create a circle of particles around vec33
            for (int j = 0; j < thickness; ++j) {
                double angle = 2 * Math.PI * j / thickness;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);
                Vec3 offset = right.scale(xOffset).add(forward.scale(zOffset));
                ((ServerLevel) shooter.level()).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, vec33.x + offset.x, vec33.y + offset.y, vec33.z + offset.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        shooter.level().playSound(shooter,shooter.blockPosition(), ModSounds.ZOLTRAAK_ATTACK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

        if(shooter.getSensing().hasLineOfSight(shooter.getTarget()))
        {
            shooter.getTarget().hurt(shooter.damageSources().magic(), damage);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void aiStep()
    {
        if (this.level().isClientSide) {
            for(int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.SCULK_SOUL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 0.8D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 0.8D);
            }
        }

        this.jumping = false;
        super.aiStep();
    }

    // ####### Boss Bar Event Stuff #######

    /**
     * Called every tick to update the entity's position/logic.
     */
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

    }

    protected ServerBossEvent createBossEvent() {
        ServerBossEvent event = new ServerBossEvent(Component.translatable("entity.sculkhorde.sculk_soul_reaper"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
        return event;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    // ###### Data Code ########
    protected void defineSynchedData()
    {
        super.defineSynchedData();
    }

    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("difficulty", getMobDifficultyLevel());
    }

    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        setMobDifficultyLevel(nbt.getInt("difficulty"));
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.WITHER_IMMUNE_TO);
    }

    // ####### Animation Code ###########

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this).transitionLength(5)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ####### Sound Code ###########

    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.EVOKER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }
    
}
