package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

/**
 * The following java files were created/edited for this entity.<br>
 * Edited {@link ModEntities}<br>
 * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
 * Added {@link EnderBubbleAttackEntity}<br>
 * Added {@link com.github.sculkhorde.client.model.enitity.EnderBubbleAttackModel}<br>
 * Added {@link com.github.sculkhorde.client.renderer.entity.EnderBubbleAttackRenderer}
 */
public class EnderBubbleAttackEntity extends SpecialEffectEntity implements IAnimatable {

    public static int LIFE_TIME = TickUnits.convertSecondsToTicks(10);
    public int currentLifeTicks = 0;

    public EnderBubbleAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public EnderBubbleAttackEntity( Level level) {
        super(ModEntities.ENDER_BUBBLE_ATTACK.get(), level);
    }

    public EnderBubbleAttackEntity(EntityType<?> entityType, Level level, LivingEntity sourceEntity) {
        super(entityType, level);
    }

    public EnderBubbleAttackEntity enableDeleteAfterTime(int ticks)
    {
        LIFE_TIME = ticks;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        //TODO Uncomment
        //if (sourceEntity == null || !sourceEntity.isAlive()) this.discard();

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();

        //playSound(SoundEvents.GENERIC_EXPLODE);


        List<LivingEntity> hitList = getEntitiesNearbyCube(LivingEntity.class, 3);
        for (LivingEntity entity : hitList)
        {
            if (getOwner() != null && getOwner().equals(entity))
            {
                continue;
            }

            if(getOwner() != null)
            {
                entity.hurt(DamageSource.indirectMagic(entity, getOwner()), 5);
            }
            else
            {
                entity.hurt(DamageSource.indirectMagic(entity, this), 5);
            }
        }


    }

    // ### GECKOLIB Animation Code ###
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData data) {
        //controllers.add(DefaultAnimations.genericIdleController(this));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
