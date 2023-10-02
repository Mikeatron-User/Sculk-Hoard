package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
public class NearestLivingEntityTargetGoal<T extends LivingEntity> extends TargetGoal {

    //protected EntityPredicate targetConditions;
    List<LivingEntity> possibleTargets;

    public NearestLivingEntityTargetGoal(Mob mobEntity, boolean mustSee, boolean mustReach)
    {
        this(mobEntity, false, false, null);
    }

    public NearestLivingEntityTargetGoal(Mob mobEntity, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> predicate)
    {
        super(mobEntity, false, false);
        this.setFlags(EnumSet.of(Flag.TARGET));
        //this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(predicate);
    }

    /** Functionality **/
    @Override
    public boolean canUse()
    {

        boolean canWeUse = !((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
        // If the mob is already targeting something valid, don't bother
        return canWeUse;
    }

    protected AABB getTargetSearchArea(double range)
    {
        return this.mob.getBoundingBox().inflate(range, 4.0D, range);
    }

    protected void findTarget()
    {
        possibleTargets =
                this.mob.level.getEntitiesOfClass(
                LivingEntity.class,
                this.getTargetSearchArea(this.getFollowDistance()),
                        ((ISculkSmartEntity)this.mob).getTargetParameters().isPossibleNewTargetValid);

        //If there is available targets
        if(possibleTargets.size() <= 0)
        {
            return;
        }

        LivingEntity closestLivingEntity = possibleTargets.get(0);

        //Return nearest Mob
        for(LivingEntity e : possibleTargets)
        {
            if(e.distanceTo(this.mob) < e.distanceTo(closestLivingEntity))
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

    public void setTargetMob(@Nullable LivingEntity targetIn) {
        this.targetMob = targetIn;
    }

    public LivingEntity getTargetMob() {
        return this.targetMob;
    }

}
