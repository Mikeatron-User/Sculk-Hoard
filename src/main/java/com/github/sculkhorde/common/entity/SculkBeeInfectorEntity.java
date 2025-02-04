package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.VirtualSurfaceInfestorCursor;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.function.Predicate;

public class SculkBeeInfectorEntity extends SculkBeeHarvesterEntity implements GeoEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added common/entity/ SculkBeeInfectorEntity.java<br>
     * Added client/model/entity/ SculkBeeInfectorModel.java<br>
     * Added client/renderer/entity/ SculkBeeInfectorRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 20F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how fast this mob moves
    public static final float MOVEMENT_SPEED = 0.5F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBeeInfectorEntity(EntityType<? extends SculkBeeInfectorEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkBeeInfectorEntity(Level worldIn) {super(ModEntities.SCULK_BEE_INFECTOR.get(), worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FLYING_SPEED, 1.5F);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }


    private final Predicate<BlockPos> IS_VALID_FLOWER = (blockPos) -> {
        BlockState blockState = level().getBlockState(blockPos);
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED))
        {
            return false;
        }

        if(!BlockInfestationSystem.isInfectable((ServerLevel) level(), blockPos))
        {
            return false;
        }

        if(!BlockAlgorithms.isExposedToAir((ServerLevel) level(), blockPos))
        {
            return false;
        }
        return true;

    };

    @Override
    public Predicate<BlockPos> getIsFlowerValidPredicate() {
        return this.IS_VALID_FLOWER;
    }

    public double getArrivalThreshold() {
        return 3D;
    }

    @Override
    protected void executeCodeOnPollination()
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get() || SculkHorde.cursorSystem.isCursorPopulationAtMax())
        {
            return;
        }
        level().getServer().tell(new TickTask(level().getServer().getTickCount() + 1, () -> {
            Optional<VirtualSurfaceInfestorCursor> cursor = CursorSystem.createSurfaceInfestorVirtualCursor(level(), blockPosition());

            if(cursor.isPresent())
            {
                cursor.get().setMaxTransformations(100);
                cursor.get().setMaxRange(100);
                cursor.get().setTickIntervalTicks(1);
                cursor.get().setSearchIterationsPerTick(20);
            }
        }));
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericFlyController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public boolean isFlying() {
        return true;
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

}
