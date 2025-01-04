package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.goal.AttackStepGoal;
import net.minecraft.world.entity.Mob;

public class ReaperCloseRangeAttackSequenceGoal extends ReaperAttackSequenceGoal {


    protected final int REQUIRED_TARGET_DISTANCE = 10;

    public ReaperCloseRangeAttackSequenceGoal(Mob mob, long executionCooldown, int minDifficulty, int maxDifficulty, AttackStepGoal... attacksIn) {
        super(mob, executionCooldown, minDifficulty, maxDifficulty, attacksIn);
    }
    @Override
    public boolean canUse() {
        if(!super.canUse())
        {
            return false;
        }

        if(getReaper().getTarget().distanceTo(getReaper()) > REQUIRED_TARGET_DISTANCE)
        {
            reasonForNoStart = "Entity too far for close range attack";
            return false;
        }

        return true;
    }
}
