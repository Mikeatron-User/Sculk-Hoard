package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Predicate;

/**
 * The following java files were created/edited for this entity.<br>
 * Edited {@link ModEntities}<br>
 * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
 * Added {@link SoulBlastAttackEntity}<br>
 * Added {@link com.github.sculkhorde.client.model.enitity.EnderBubbleAttackModel}<br>
 * Added {@link com.github.sculkhorde.client.renderer.entity.EnderBubbleAttackRenderer}
 */
public class SoulBlastAttackEntity extends SpecialEffectEntity implements GeoEntity {

    public static int LIFE_TIME = TickUnits.convertSecondsToTicks(1);
    public int currentLifeTicks = 0;
    private double pushUpStrength = 0.1; // Strength of the push effect
    private boolean startedPlayingSFX = false;

    public SoulBlastAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public SoulBlastAttackEntity(Level level) {
        this(ModEntities.ENDER_BUBBLE_ATTACK.get(), level);
    }

    public SoulBlastAttackEntity(Level level, LivingEntity sourceEntity) {
        this(level);
        setOwner(sourceEntity);
    }

    public SoulBlastAttackEntity enableDeleteAfterTime(int ticks)
    {
        LIFE_TIME = ticks;
        return this;
    }

    private void pushOutEntities(double range) {
        if (level().isClientSide()) return;

        Predicate<Entity> predicate = (entity) -> {
            if (entity == null) {
                return false;
            } else if (entity instanceof Player p) {
                //p.hurtMarked = true;
                return false;
            } else if (entity instanceof SculkEndermanEntity) {
                return false;
            }

            boolean entityIsPushable = !entity.noPhysics;
            return entityIsPushable;
        };

        List<Entity> pushAwayList = EntityAlgorithms.getEntitiesInBoundingBox((ServerLevel) level(), this.getBoundingBox().inflate(range, range, range), predicate);

        float pushAwayStrength = 0.3f; // Increased push strength for better outwards effect
        float pushUpStrength = 0.1f;   // Separate push up strength for vertical component.

        for (Entity entity : pushAwayList) {
            // Calculate the vector from the black hole to the entity
            double dx = entity.getX() - getX();
            double dz = entity.getZ() - getZ();

            // Calculate the horizontal distance to the black hole (ignore vertical)
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);


            // Normalize the horizontal vector (prevent NaN)
            double normalizedDx = horizontalDistance == 0 ? 0 : dx / horizontalDistance;
            double normalizedDz = horizontalDistance == 0 ? 0 : dz / horizontalDistance;

            // Apply the push outwards
            entity.push(normalizedDx * pushAwayStrength, pushUpStrength, normalizedDz * pushAwayStrength); //Apply the combined push
        }
    }

    @Override
    public void tick() {
        super.tick();

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();

        if(!startedPlayingSFX)
        {
            level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), ModSounds.ENDER_BUBBLE_LOOP.get(), this.getSoundSource(), 1.0F, 1F);
            startedPlayingSFX = true;
        }

        pushOutEntities(10);

        List<LivingEntity> damageHitList = getEntitiesNearbyCube(LivingEntity.class, 8);

        for (LivingEntity entity : damageHitList)
        {
            if (getOwner() != null && getOwner().equals(entity))
            {
                continue;
            }

            if(getOwner() != null)
            {
                entity.hurt(damageSources().indirectMagic(entity, getOwner()), 5);
            }
            else
            {
                entity.hurt(damageSources().indirectMagic(entity, this), 5);
            }
        }


    }

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericLivingController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
