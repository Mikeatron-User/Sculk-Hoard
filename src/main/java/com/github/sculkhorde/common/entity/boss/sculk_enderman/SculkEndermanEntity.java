package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.raid_system.RaidHandler;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;

public class SculkEndermanEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link SculkEndermanEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkEndermanModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkEndermanRenderer}
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

    // Controls what types of entities this mob can target
    private final TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().disableBlackListMobs();

    // Timing Variables
    public boolean canTeleport = true;
    protected int TELEPORT_COOLDOWN = TickUnits.convertSecondsToTicks(8);
    protected int ticksSinceLastTeleport = 0;

    protected int SPECIAL_ATTACK_COOLDOWN = TickUnits.convertSecondsToTicks(5);
    protected int ticksSinceLastSpecialAttack = 0;

    protected ServerBossEvent bossEvent;

    // Data
    public static final EntityDataAccessor<Boolean> DATA_AGGRO = SynchedEntityData.defineId(SculkEndermanEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_SCOUTING = SynchedEntityData.defineId(SculkEndermanEntity.class, EntityDataSerializers.BOOLEAN);

    // Animation
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkEndermanEntity(EntityType<? extends SculkEndermanEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setMaxUpStep(1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.bossEvent = this.createBossEvent();
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    public SculkEndermanEntity(Level level, BlockPos pos)
    {
        this(ModEntities.SCULK_ENDERMAN.get(), level);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
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

    public boolean isSpecialAttackOnCooldown() {
        return ticksSinceLastSpecialAttack < SPECIAL_ATTACK_COOLDOWN;
    }

    public void incrementSpecialAttackCooldown() {
        ticksSinceLastSpecialAttack++;
    }

    public void resetSpecialAttackCooldown() {
        ticksSinceLastSpecialAttack = 0;
    }

    public boolean isIdle() {
        return getTarget() == null;
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

    public boolean isScouting() {
        return entityData.get(DATA_SCOUTING);
    }

    public void setScouting(boolean value)
    {
        entityData.set(DATA_SCOUTING, value);
    }

    public boolean isAggro() {
        return entityData.get(DATA_AGGRO);
    }

    public void setAggro()
    {
        entityData.set(DATA_AGGRO, true);
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    public boolean isTeleportCooldownOver() {
        if(getTarget() != null)
        {
            return ticksSinceLastTeleport >= TELEPORT_COOLDOWN/8;
        }

        return ticksSinceLastTeleport >= TELEPORT_COOLDOWN;

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


        //All Phase Attacks
        this.goalSelector.addGoal(1, new ChaosRiftAttackGoal(this, TickUnits.convertSecondsToTicks(3)));
        this.goalSelector.addGoal(1, new SculkSpineSpikeRadialAttack(this));
        //this.goalSelector.addGoal(1, new SculkSpineSpikeLineAttack(this));
        this.goalSelector.addGoal(1, new SummonSkeletonsAttackUnits(this, TickUnits.convertSecondsToTicks(3)));
        this.goalSelector.addGoal(1, new RangedSonicBoomAttackGoal(this, TickUnits.convertSecondsToTicks(1)));
        this.goalSelector.addGoal(2, new SummonRandomAttackUnits(this, TickUnits.convertSecondsToTicks(3)));
        this.goalSelector.addGoal(2, new SummonMitesAttackUnits(this, TickUnits.convertSecondsToTicks(3)));

        // Phase 2 Attacks
        this.goalSelector.addGoal(1, new RainDragonBallAttackGoal(this, TickUnits.convertSecondsToTicks(5)));
        this.goalSelector.addGoal(1, new EnderBubbleAttackGoal(this, TickUnits.convertSecondsToTicks(10)));
        this.goalSelector.addGoal(2, new SummonCreepersAttackUnits(this, TickUnits.convertSecondsToTicks(5)));

        this.goalSelector.addGoal(3, new AttackGoal());
        this.goalSelector.addGoal(4, new PathFindToRaidLocation<>(this));
        this.goalSelector.addGoal(5, new MoveTowardsTargetGoal(this, 1.0F, 20F));
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

        // 50% chance to teleport randomly
        if(this.random.nextInt(2) == 0) {
            teleportRandomly(32);
        }

        setAggro();
        return super.hurt(damageSource, amount);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void aiStep()
    {
        if (this.level().isClientSide) {
            for(int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
            }
        }
        // IF target isnt null and we cannot see them, teleport to them
        if(this.getTarget() != null && !TARGET_PARAMETERS.canSeeTarget(getTarget()))
        {
            teleportBehindEntity(getTarget());
        }

        if(this.getTarget() != null && !this.getTarget().onGround())
        {
            stayInSpecificRangeOfTarget(16, 32);
        }

        this.jumping = false;
        super.aiStep();
    }

    private boolean isWithinRaidLocation()
    {
        return BlockAlgorithms.getBlockDistance(RaidHandler.raidData.getRaidLocation(), this.blockPosition()) <= 32;
    }

    private void teleportToRaidLocationIfOutside()
    {
        teleport(RaidHandler.raidData.getRaidLocation().getX(), RaidHandler.raidData.getRaidLocation().getY(), RaidHandler.raidData.getRaidLocation().getZ());
    }

    public void stayInSpecificRangeOfTarget(int min, int max)
    {
        if(getTarget() == null)
        {
            return;
        }

        if(!isTeleportCooldownOver())
        {
            return;
        }

        // If Too Far, teleport closer
        if(distanceTo(getTarget()) > max)
        {
            teleportTowardsEntity(getTarget());
        }
        // If Too Close, teleport away
        else if(distanceTo(getTarget()) < min)
        {
            teleportAwayFromEntity(getTarget());
        }
    }

    /**
     * Called every tick to update the entity's position/logic.
     */
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        ticksSinceLastTeleport++;

        incrementSpecialAttackCooldown();

        if(!SculkHorde.raidHandler.isRaidInactive() && !isWithinRaidLocation() && isScouting() && isTeleportCooldownOver())
        {
            teleportToRaidLocationIfOutside();
        }

        if(SculkHorde.raidHandler.isRaidInactive() && isScouting())
        {
            discard();
        }
    }

    /**
     * Teleports the entity to a random position within 64 blocks of the entity
     */
    protected void teleportRandomly(int distance)
    {
        if (this.level().isClientSide() || !this.isAlive() || !canTeleport)
        {
            return;
        }

        double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * distance;
        double d1 = this.getY() + (int)(this.random.nextDouble() - 0.5D) * distance;
        double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * distance;
        this.teleport(d0, d1, d2);

    }

    /**
     * Teleports the entity towards the given entity
     *
     * @param entity The entity to teleport towards
     */
    public void teleportTowardsEntity(Entity entity)
    {
        if(!canTeleport || !isTeleportCooldownOver())
        {
            return;
        }

        ticksSinceLastTeleport = 0;

        Vec3 vec3 = new Vec3(this.getX() - entity.getX(), this.getY(0.5D) - entity.getEyeY(), this.getZ() - entity.getZ());
        vec3 = vec3.normalize();
        double teleportDistance = 8.0D;
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.x * teleportDistance;
        double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3.y * teleportDistance;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.z * teleportDistance;
        this.teleport(d1, d2, d3);
    }

    /**
     * Teleports the entity towards the given entity
     * @param pos The position to teleport towards
     * @return Returns true if the teleport was successful
     */
    protected boolean teleportTowardsPos(BlockPos pos)
    {
        if(!canTeleport)
        {
            return false;
        }

        Vec3 vec3 = new Vec3(this.getX() - pos.getX(), this.getY(0.5D), this.getZ() - pos.getZ());
        vec3 = vec3.normalize();
        double teleportDistance = 8.0D;
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.x * teleportDistance;
        double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3.y * teleportDistance;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vec3.z * teleportDistance;
        return this.teleport(d1, d2, d3);
    }

    /**
     * Teleports the entity away from the given entity
     * @param entity The entity to teleport away from
     * @return Returns true if the teleport was successful
     */
    public boolean teleportAwayFromEntity(Entity entity)
    {
        if(!canTeleport)
        {
            return false;
        }

        Vec3 vec3 = new Vec3(this.getX() - entity.getX(), this.getY(0.5D) - entity.getEyeY(), this.getZ() - entity.getZ());
        vec3 = vec3.normalize();
        double teleportDistance = 8.0D;
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D + vec3.x * teleportDistance;
        double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) + vec3.y * teleportDistance;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D + vec3.z * teleportDistance;
        return this.teleport(d1, d2, d3);
    }

    protected void teleportBehindEntity(Entity entity)
    {
        if(!canTeleport)
        {
            return;
        }

        this.teleport(entity.getX(), entity.getY(), entity.getZ());
    }

    /**
     * Teleport the enderman to a random nearby position
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @return Returns true if the teleport was successful
     */
    protected boolean teleport(double x, double y, double z)
    {
        if(!canTeleport)
        {
            return false;
        }

        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(x, y, z);

        while(blockpos$mutableblockpos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(blockpos$mutableblockpos).blocksMotion())
        {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
        boolean isMotionBlockFlag = false; blockstate.blocksMotion();
        boolean isWaterFlag = blockstate.getFluidState().is(FluidTags.WATER);
        if (!isWaterFlag)
        {
            net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(this, x, y, z);
            if (event.isCanceled())
            {
                return false;
            }
            Vec3 vec3 = this.position();
            boolean ifCanRandomTeleport = this.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
            if (ifCanRandomTeleport)
            {
                this.level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
                if (!this.isSilent())
                {
                    this.level().playSound((Player)null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(ModSounds.SCULK_ENDERMAN_PORTAL.get(), 1.0F, 1.0F);
                    ticksSinceLastTeleport = 0;
                }
            }

            return ifCanRandomTeleport;
        }
        else
        {
            return false;
        }
    }

    protected ServerBossEvent createBossEvent() {
        ServerBossEvent event = new ServerBossEvent(Component.literal("Sculk Enderman"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
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
    String DATA_IS_SCOUTING_IDENTIFIER = "is_scouting";
    String DATA_IS_AGGRO_IDENTIFIER = "is_aggro";
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_AGGRO, false);
        this.entityData.define(DATA_SCOUTING, false);
    }

    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean(DATA_IS_SCOUTING_IDENTIFIER, this.entityData.get(DATA_SCOUTING));
        nbt.putBoolean(DATA_IS_AGGRO_IDENTIFIER, this.entityData.get(DATA_AGGRO));
    }

    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(DATA_SCOUTING, nbt.getBoolean(DATA_IS_SCOUTING_IDENTIFIER));
        this.entityData.set(DATA_AGGRO, nbt.getBoolean(DATA_IS_AGGRO_IDENTIFIER));
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.WITHER_IMMUNE_TO);
    }

    // ####### Animation Code ###########

    private static final RawAnimation IDLE_BODY_ANIMATION = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation IDLE_TWITCH_ANIMATION = RawAnimation.begin().thenPlay("idle.twitch");
    private static final RawAnimation IDLE_TENDRILS_ANIMATION = RawAnimation.begin().thenPlay("idle.tendrils");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenPlay("move.walk");
    private static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenPlay("move.run");
    private static final RawAnimation COMBAT_ATTACK_ANIMATION_1 = RawAnimation.begin().thenPlay("combat.attack1");
    private static final RawAnimation COMBAT_ATTACK_ANIMATION_2 = RawAnimation.begin().thenPlay("combat.attack2");
    private static final RawAnimation COMBAT_ATTACK_ANIMATION_3 = RawAnimation.begin().thenPlay("combat.attack3");
    private static final RawAnimation COMBAT_FIREBALL_SHOOT_ANIMATION = RawAnimation.begin().thenPlay("combat.fireball.face");
    private static final RawAnimation COMBAT_FIREBALL_SKY_SUMMON_ANIMATION = RawAnimation.begin().thenPlay("combat.fireball.sky.summon");
    private static final RawAnimation COMBAT_FIREBALL_SKY_TWITCH_ANIMATION = RawAnimation.begin().thenPlay("combat.fireball.sky.twitch");
    private static final RawAnimation COMBAT_SUMMON_ANIMATION = RawAnimation.begin().thenLoop("combat.summon");
    private static final RawAnimation COMBAT_SUMMON_TWITCH_ANIMATION = RawAnimation.begin().thenLoop("combat.summon.twitch");
    private static final RawAnimation COMBAT_RIFTS_SUMMON_ANIMATION = RawAnimation.begin().thenPlay("combat.rifts.summon");
    private static final RawAnimation COMBAT_SPIKE_LINE = RawAnimation.begin().thenPlay("combat.spike.line");
    private static final RawAnimation COMBAT_SPIKE_TWITCH = RawAnimation.begin().thenPlay("combat.spike.line.twitch");
    private static final RawAnimation COMBAT_SPIKE_RADIAL = RawAnimation.begin().thenPlay("combat.spike.around");
    private static final RawAnimation COMBAT_SPIKE_RADIAL_TWITCH = RawAnimation.begin().thenPlay("combat.spike.around.twitch");
    private static final RawAnimation COMBAT_BUBBLE = RawAnimation.begin().thenPlay("combat.forcefieldbubble.activate");
    private static final RawAnimation COMBAT_BUBBLE_TWITCH = RawAnimation.begin().thenPlay("combat.forcefieldbubble.twitch");

    private final AnimationController COMBAT_ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .transitionLength(5)
            .triggerableAnim("melee_attack_animation_1", COMBAT_ATTACK_ANIMATION_1)
            .triggerableAnim("melee_attack_animation_2", COMBAT_ATTACK_ANIMATION_2)
            .triggerableAnim("melee_attack_animation_3", COMBAT_ATTACK_ANIMATION_3)
            .triggerableAnim("fireball_shoot_animation", COMBAT_FIREBALL_SHOOT_ANIMATION)
            .triggerableAnim("fireball_sky_summon_animation", COMBAT_FIREBALL_SKY_SUMMON_ANIMATION)
            .triggerableAnim("fireball_sky_twitch_animation", COMBAT_FIREBALL_SKY_TWITCH_ANIMATION)
            .triggerableAnim("summon_animation", COMBAT_SUMMON_ANIMATION)
            .triggerableAnim("rifts_summon_animation", COMBAT_RIFTS_SUMMON_ANIMATION)
            .triggerableAnim("spike_line_animation", COMBAT_SPIKE_LINE)
            .triggerableAnim("spike_radial_animation", COMBAT_SPIKE_RADIAL)
            .triggerableAnim("bubble_animation", COMBAT_BUBBLE);

    private final AnimationController COMBAT_TWITCH_ANIMATION_CONTROLLER = new AnimationController<>(this, "twitch_controller", state -> PlayState.STOP)
            .transitionLength(5)
            .triggerableAnim("fireball_sky_twitch_animation", COMBAT_FIREBALL_SKY_TWITCH_ANIMATION)
            .triggerableAnim("summon_twitch_animation", COMBAT_SUMMON_TWITCH_ANIMATION)
            .triggerableAnim("spike_line_twitch_animation", COMBAT_SPIKE_TWITCH)
            .triggerableAnim("spike_radial_twitch_animation", COMBAT_SPIKE_RADIAL_TWITCH)
            .triggerableAnim("bubble_twitch_animation", COMBAT_BUBBLE_TWITCH);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(
                new AnimationController<>(this, "walk_cycle", 5, this::poseWalk),
                new AnimationController<>(this, "twitch", 5, this::poseTwitch),
                new AnimationController<>(this, "tendrils", 5, this::poseTendrils),
                COMBAT_ATTACK_ANIMATION_CONTROLLER
                //COMBAT_TWITCH_ANIMATION_CONTROLLER
        );
    }

    // Create the animation handler for the leg segment
    protected PlayState poseWalk(AnimationState<SculkEndermanEntity> state)
    {
        if(state.isMoving() && state.getAnimatable().isAggro())
        {
            state.setAnimation(RUN_ANIMATION);
        }
        else if(state.isMoving())
        {
            state.setAnimation(WALK_ANIMATION);
        }
        else
        {
            state.setAnimation(IDLE_BODY_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    protected PlayState poseTwitch(AnimationState<SculkEndermanEntity> state)
    {
        state.setAnimation(IDLE_TWITCH_ANIMATION);
        return PlayState.CONTINUE;
    }

    protected PlayState poseTendrils(AnimationState<SculkEndermanEntity> state)
    {
        state.setAnimation(IDLE_TENDRILS_ANIMATION);
        return PlayState.CONTINUE;
    }

    protected float getStandingEyeHeight(Pose p_32517_, EntityDimensions p_32518_) {
        return 2.55F;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ####### Sound Code ###########

    protected SoundEvent getAmbientSound() {
        return ModSounds.SCULK_ENDERMAN_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.SCULK_ENDERMAN_HIT.get();
    }

    protected SoundEvent getDeathSound() {
        return ModSounds.SCULK_ENDERMAN_DEATH.get();
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }


    /* DO NOT USE THIS FOR ANYTHING, CAUSES DESYNC
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.savedData.addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }
    */


    class AttackGoal extends CustomMeleeAttackGoal
    {

        public AttackGoal()
        {
            super(SculkEndermanEntity.this, 1.0D, true, 17);
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget)
        {
            return 6;
        }

        @Override
        protected float getMinimumDistanceToTarget()
        {
            return 0.5F;
        }

        @Override
        protected void triggerAnimation()
        {
            // Choose between 3 animations randomly
            int random = new Random().nextInt(3);
            if(random == 0)
            {
                ((SculkEndermanEntity)mob).triggerAnim("attack_controller", "melee_attack_animation_1");
            }
            else if(random == 1)
            {
                ((SculkEndermanEntity)mob).triggerAnim("attack_controller", "melee_attack_animation_2");
            }
            else
            {
                ((SculkEndermanEntity)mob).triggerAnim("attack_controller", "melee_attack_animation_3");
            }
        }
    }
}
