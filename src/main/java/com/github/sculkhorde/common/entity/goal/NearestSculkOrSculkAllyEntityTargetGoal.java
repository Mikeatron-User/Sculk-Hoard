package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class NearestSculkOrSculkAllyEntityTargetGoal<T extends LivingEntity> extends TargetGoal {

    protected List<LivingEntity> possibleTargets;

    protected long lastTimeSinceTargetSearch = 0;
    protected long targetSearchInterval = TickUnits.convertSecondsToTicks(2);

    protected boolean ignoreFlyingTargets = true;

    protected boolean ignoreSwimmingTargets = true;

    public NearestSculkOrSculkAllyEntityTargetGoal(Mob mobEntity, boolean mustSee, boolean mustReach)
    {
        super(mobEntity, mustSee, mustReach);
        this.setFlags(EnumSet.of(Flag.TARGET));
        //this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(predicate);
    }

    public void setTargetMob(@Nullable LivingEntity targetIn) {
        this.targetMob = targetIn;
    }

    public LivingEntity getTargetMob() {
        return this.targetMob;
    }

    protected AABB getTargetSearchArea(double range)
    {
        return this.mob.getBoundingBox().inflate(range, this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), range);
    }

    public NearestSculkOrSculkAllyEntityTargetGoal setIgnoreFlyingTargets(boolean value)
    {
        ignoreFlyingTargets = value;
        return this;
    }

    public NearestSculkOrSculkAllyEntityTargetGoal setIgnoreSwimmingTargets(boolean value)
    {
        ignoreSwimmingTargets = value;
        return this;
    }

    protected boolean isEntityValidTarget(LivingEntity livingEntity)
    {
        return (EntityAlgorithms.isSculkLivingEntity.test(livingEntity) || EntityAlgorithms.isLivingEntityAllyToSculkHorde(livingEntity))
                && (ignoreFlyingTargets && EntityAlgorithms.getHeightOffGround(livingEntity) > 3)
                && (ignoreSwimmingTargets && livingEntity.isSwimming());
    }

    public final Predicate<LivingEntity> isValidTarget = this::isEntityValidTarget;

    /** Functionality **/
    @Override
    public boolean canUse()
    {

        // If the mob is already targeting something valid, don't bother
        return true;
    }
    protected void findTarget()
    {
        if(this.mob.level().getGameTime() - lastTimeSinceTargetSearch < targetSearchInterval)
        {
            return;
        }

        lastTimeSinceTargetSearch = this.mob.level().getGameTime();

        possibleTargets =
                this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getTargetSearchArea(this.getFollowDistance()), isValidTarget);

        //If there is available targets
        if(possibleTargets.isEmpty())
        {
            return;
        }

        LivingEntity closestLivingEntity = possibleTargets.get(0);

        //Return nearest Mob
        for(LivingEntity e : possibleTargets)
        {
            if(e.distanceTo(this.mob) < closestLivingEntity.distanceTo(this.mob))
            {
                closestLivingEntity = e;
            }
        }
        setTargetMob(closestLivingEntity); //Return target

    }

    public void start()
    {
        this.findTarget();
        this.mob.setTarget(getTargetMob());
        super.start();
    }
}
