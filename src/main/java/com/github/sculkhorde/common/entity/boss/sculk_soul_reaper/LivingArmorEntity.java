package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class LivingArmorEntity extends Monster implements GeoEntity, ISculkSmartEntity {

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
    public static final float MAX_HEALTH = 20F;
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
    public int shieldCoolDown;
    protected float lastHurtDistanceFromSourceEntity = 0;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableMustReachTarget();
    private SquadHandler squad = new SquadHandler(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public LivingArmorEntity(EntityType<? extends LivingArmorEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        this.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
    }

    public LivingArmorEntity(Level worldIn, Vec3 position) {
        this(ModEntities.LIVING_ARMOR.get(), worldIn);
        setPos(position);
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

    public boolean isIdle() {
        return getTarget() == null;
    }

    @Override
    public void checkDespawn() {}

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
                        new FloatGoal(this),
                        new MirrorArmorGoal(),
                        new SquadHandlingGoal(this),
                        new AttackGoal(),
                        new FollowSquadLeader(this),
                        //MoveTowardsTargetGoal(mob, speedModifier, within) THIS IS FOR NON-ATTACKING GOALS
                        new MoveTowardsTargetGoal(this, 0.8F, 20F),
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true),
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
                        //HurtByTargetGoal(mob)
                        new RaiseShield(),
                        new TargetAttacker(this),
                        new FocusSquadTarget(this),
                        new NearestLivingEntityTargetGoal<>(this, true, true)

                };
        return goals;
    }

    // #### Behavior Management ####

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {

        if(damageSource.getEntity() != null)
        {
            this.lastHurtDistanceFromSourceEntity = damageSource.getEntity().distanceTo(this);
        }
        else
        {
            this.lastHurtDistanceFromSourceEntity = 0;
        }
        return super.hurt(damageSource, amount);
    }

    public long getTicksSinceLastHurt()
    {
        return level().getGameTime()-getLastHurtMobTimestamp();
    }


    // #### Inventory Management ####

    protected ItemStack getItemInMainHand()
    {
        return getItemBySlot(EquipmentSlot.MAINHAND);
    }

    protected ItemStack getItemInOffHand()
    {
        return getItemBySlot(EquipmentSlot.OFFHAND);
    }

    protected ItemStack getItemInHead()
    {
        return getItemBySlot(EquipmentSlot.HEAD);
    }

    protected ItemStack getItemInChest()
    {
        return getItemBySlot(EquipmentSlot.CHEST);
    }

    protected ItemStack getItemInLegs()
    {
        return getItemBySlot(EquipmentSlot.LEGS);
    }

    protected ItemStack getItemInFeet()
    {
        return getItemBySlot(EquipmentSlot.FEET);
    }

    public boolean hasShield()
    {
        if(getItemInOffHand() == null || getItemInOffHand().isEmpty())
        {
            return false;
        }

        if(getItemInOffHand().getItem() instanceof ShieldItem)
        {
            return true;
        }

        return false;
    }

    @Override
    protected void blockUsingShield(LivingEntity entityIn) {
        super.blockUsingShield(entityIn);
        this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.0F);
        if (entityIn.getMainHandItem().canDisableShield(this.useItem, this, entityIn)) {
            this.disableShield(true);
        }
    }

    public void disableShield(boolean increase) {
        float chance = 0.25F + (float) EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
        if (increase) chance += 0.75;
        if (this.random.nextFloat() < chance) {
            this.stopUsingItem();
            level().broadcastEntityEvent(this, (byte) 30);
        }
    }

    // #### Animation Management ####

    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("attack", ATTACK_ANIMATION).transitionLength(5);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                //DefaultAnimations.genericWalkIdleController(this),
                //ATTACK_ANIMATION_CONTROLLER
        );
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }



    protected SoundEvent getAmbientSound() {
        return ModSounds.SCULK_ZOMBIE_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.SCULK_ZOMBIE_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return ModSounds.SCULK_ZOMBIE_DEATH.get();
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.DROWNED_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }

    class AttackGoal extends CustomMeleeAttackGoal
    {

        public AttackGoal()
        {
            super(LivingArmorEntity.this, 1.0D, true, 10);
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
            return 3.5F;
        }

        @Override
        protected int getAttackInterval() {
            return TickUnits.convertSecondsToTicks(0.5F);
        }

        @Override
        protected void triggerAnimation() {
            ((LivingArmorEntity)mob).triggerAnim("attack_controller", "attack");
        }
    }

    protected class MirrorArmorGoal extends Goal
    {
        protected long lastTimeOfArmorCheck = 0;
        protected final long ARMOR_CHECK_COOL_DOWN = TickUnits.convertSecondsToTicks(10);




        @Override
        public boolean canUse() {
            return Math.abs(level().getGameTime() - lastTimeOfArmorCheck) > ARMOR_CHECK_COOL_DOWN && getTarget() != null && getTarget() instanceof Player;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {


            lastTimeOfArmorCheck = level().getGameTime();

            if(getTarget() instanceof Player player)
            {
                ItemStack playerHead = player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack playerChest = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack playerLegs = player.getItemBySlot(EquipmentSlot.LEGS);
                ItemStack playerFeet = player.getItemBySlot(EquipmentSlot.FEET);

                if(playerHead.isEmpty())
                {
                    setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                }
                else
                {
                    setItemSlot(EquipmentSlot.HEAD, playerHead.copy());
                }

                if(playerChest.isEmpty())
                {
                    setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                }
                else
                {
                    setItemSlot(EquipmentSlot.CHEST, playerChest.copy());
                }

                if(playerLegs.isEmpty())
                {
                    setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                }
                else
                {
                    setItemSlot(EquipmentSlot.LEGS, playerLegs.copy());
                }

                if(playerFeet.isEmpty())
                {
                    setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                }
                else
                {
                    setItemSlot(EquipmentSlot.FEET, playerFeet.copy());
                }

            }
        }
    }

    protected class RaiseShield extends Goal
    {
        long timeOfRaiseShield = 0;
        long MAX_SHIELD_UP_TIME = TickUnits.convertSecondsToTicks(10);

        protected boolean shieldIsUp = false;
        @Override
        public boolean canUse() {
            if(getTarget() == null)
            {
                return false;
            }

            // We want to block if we get hurt from too far away.
            if(lastHurtDistanceFromSourceEntity > 5)
            {
                return true;
            }


            if(getTarget().distanceTo(LivingArmorEntity.this) > 5)
            {
                return false;
            }

            if(!getItemInOffHand().canPerformAction(ToolActions.SHIELD_BLOCK))
            {
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {

            if(getOffhandStackIfShield().isEmpty())
            {
                return false;
            }

            if(shieldIsUp && level().getGameTime() - timeOfRaiseShield > MAX_SHIELD_UP_TIME)
            {
                return false;
            }

            if(getTarget() == null || distanceTo(getTarget()) > 10)
            {
                return false;
            }

            return true;
        }

        protected Optional<ItemStack> getOffhandStackIfShield()
        {
            if(!getItemInOffHand().isEmpty() || getItemInOffHand().canPerformAction(ToolActions.SHIELD_BLOCK))
            {
                return Optional.of(getItemInOffHand());
            }

            return Optional.empty();
        }

        protected boolean raiseShield()
        {
            if(getItemInOffHand().canPerformAction(ToolActions.SHIELD_BLOCK))
            {
                startUsingItem(InteractionHand.OFF_HAND);
                timeOfRaiseShield = level().getGameTime();
                shieldIsUp = true;
                level().playSound(LivingArmorEntity.this, blockPosition(), SoundEvents.ARMOR_EQUIP_CHAIN, SoundSource.HOSTILE, 1.0F, 1.0F);
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            super.start();
            if(getOffhandStackIfShield().isEmpty())
            {
                return;
            }

            raiseShield();
        }

        @Override
        public void stop() {
            super.stop();
            shieldIsUp = false;
            stopUsingItem();
        }
    }
}
