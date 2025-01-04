package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.goal.AttackSequenceGoal;
import com.github.sculkhorde.common.entity.goal.AttackStepGoal;
import net.minecraft.world.entity.Mob;

public class ReaperAttackSequenceGoal extends AttackSequenceGoal {


    protected int minDifficulty = 0;
    protected int maxDifficulty = 0;

    public ReaperAttackSequenceGoal(Mob mob, long executionCooldown, int minDifficulty, int maxDifficulty, AttackStepGoal... attacksIn) {
        super(mob, executionCooldown, attacksIn);
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
    }

    public SculkSoulReaperEntity getReaper()
    {
        return(SculkSoulReaperEntity) mob;
    }

    @Override
    public boolean canUse() {
        if(!super.canUse())
        {
            return false;
        }

        if(getReaper().isThereAnotherAttackActive(this))
        {
            if(getReaper().getCurrentAttack().getCurrentGoal() == null)
            {
                reasonForNoStart = "There is already an attack going on: null";
                return false;
            }

            reasonForNoStart = "There is already an attack going on: \n   " + getReaper().getCurrentAttack().getCurrentGoal().getClass().getName();
            return false;
        }

        if(getReaper().getMobDifficultyLevel() < minDifficulty)
        {
            reasonForNoStart = "Incorrect Difficulty";
            return false;
        }

        if(getReaper().getMobDifficultyLevel() > maxDifficulty && maxDifficulty != -1)
        {
            reasonForNoStart = "Incorrect Difficulty";
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        super.start();

        if(getReaper().getCurrentAttack() == null)
        {
            getReaper().setCurrentAttack(this);
        }

    }

    @Override
    public void stop() {
        super.stop();

        if(finishedAttackSequence)
        {
            getReaper().clearCurrentAttack();
        }
    }
}
