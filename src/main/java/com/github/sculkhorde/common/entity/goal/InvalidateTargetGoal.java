package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InvalidateTargetGoal extends Goal {

    private final ISculkSmartEntity mob; // We use this to retrieve the mob that is using this goal.
    private UUID lastTargetUUID;
    private long timeSinceLastTargetChange;
    private BlockPos ourLastPositionSinceCheck;
    private long UNREACHABLE_TARGET_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private long lastTimeSincePositionCheck = System.currentTimeMillis();
    private long POSITION_CHECK_INTERVAL = TimeUnit.SECONDS.toMillis(5);

    public InvalidateTargetGoal(ISculkSmartEntity mob)
    {
        super();
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public Mob getMob()
    {
        return (Mob) this.mob;
    }

    public LivingEntity getTarget()
    {
        return getMob().getTarget();
    }

    /**
     * Checks if we have been targetting the same entity as last check.
     * @return
     */
    private boolean hasTargetChanged()
    {
        if(getTarget() == null)
        {
            return false;
        }

        if(lastTargetUUID == null || !lastTargetUUID.equals(getTarget().getUUID()))
        {
            // If it has changed, update the lastTargetUUID
            lastTargetUUID = getTarget().getUUID();
            timeSinceLastTargetChange = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private boolean tooCloseToLastPosition()
    {
        return BlockAlgorithms.getBlockDistance(ourLastPositionSinceCheck, getMob().blockPosition()) < 5;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        if(getTarget() == null)
        {
            return false;
        }
        TargetParameters targetParameters = mob.getTargetParameters();

        if(System.currentTimeMillis() - lastTimeSincePositionCheck > POSITION_CHECK_INTERVAL)
        {
            ourLastPositionSinceCheck = getMob().blockPosition();
            lastTimeSincePositionCheck = System.currentTimeMillis();
        }

        // If target has not changed, and we reached threshold, and we are still within 15 blocks of last position, invalidate target.
        //BUG FIX: Forgot to check if the target was a MobEntity before casting it.
        if(targetParameters.canBlackListMobs() && getTarget() instanceof Mob && !hasTargetChanged() && System.currentTimeMillis() - timeSinceLastTargetChange > UNREACHABLE_TARGET_TIMEOUT && tooCloseToLastPosition())
        {
            targetParameters.addToBlackList((Mob) getTarget());
        }

        boolean result = !targetParameters.isEntityValidTarget(getTarget(), true);
        return result;
    }

    @Override
    public void start()
    {
        getMob().setTarget(null);
        getMob().getTarget();
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }
}
