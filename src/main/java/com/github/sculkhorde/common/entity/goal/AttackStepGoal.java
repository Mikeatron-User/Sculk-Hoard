package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Optional;

public class AttackStepGoal extends Goal implements IDebuggableGoal
{
    protected String lastReasonOfNoStart = "None";
    protected AttackSequenceGoal sequenceParent;

    protected AttackSequenceGoal getSequenceParent()
    {
        return sequenceParent;
    }

    protected void setSequenceParent(AttackSequenceGoal parent)
    {
        sequenceParent = parent;
    }

    @Override
    public boolean canUse() {
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        if(sequenceParent != null) { sequenceParent.incrementAttackIndexOrFinishSequence(); }
    }

    @Override
    public Optional<String> getLastReasonForGoalNoStart() {
        return Optional.of(lastReasonOfNoStart);
    }

    @Override
    public Optional<String> getGoalName() {
        return Optional.empty();
    }

    @Override
    public long getLastTimeOfGoalExecution() {
        return -1;
    }

    @Override
    public long getTimeRemainingBeforeCooldownOver() {
        return -1;
    }
}
