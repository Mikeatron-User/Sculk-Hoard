package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.Optional;

public class AttackSequenceGoal extends Goal implements IDebuggableGoal
{
    protected String reasonForNoStart = "N/A";

    protected ArrayList<AttackStepGoal> attacks = new ArrayList<>();
    protected int currentAttackIndex = 0;
    protected long timeOfLastExecution = 0;
    protected Mob mob;
    protected boolean finishedAttackSequence = false;
    protected long executionCooldown = 0;

    public AttackSequenceGoal(Mob mob, long executionCooldown, AttackStepGoal... attacksIn)
    {
        this.mob = mob;
        this.executionCooldown = executionCooldown;
        for(AttackStepGoal goal : attacksIn)
        {
            goal.setSequenceParent(this);
            attacks.add(goal);
        }
    }

    public boolean isAttackSequenceFinished()
    {
        return finishedAttackSequence;
    }

    protected Goal getCurrentGoal()
    {
        return attacks.get(currentAttackIndex);
    }

    protected void incrementAttackIndexOrFinishSequence()
    {
        if(currentAttackIndex + 1 >= attacks.size())
        {
            finishedAttackSequence = true;
            return;
        }

        currentAttackIndex += 1;
    }

    protected long getExecutionCooldown() { return executionCooldown; }

    @Override
    public void start() {
        super.start();
        //SculkHorde.LOGGER.debug("Sculk Reaper Entity | Starting Attack: " + getCurrentGoal().getClass());
        getCurrentGoal().start();
    }

    @Override
    public boolean canUse() {

        if(attacks.isEmpty())
        {
            reasonForNoStart = "No attacks in this attack sequence.";
            return false;
        }

        if(Math.abs(mob.level().getGameTime() - timeOfLastExecution) < getExecutionCooldown())
        {
            reasonForNoStart = "On Attack Cooldown";
            return false;
        }

        if(!getCurrentGoal().canUse())
        {
            reasonForNoStart = "Attack in Sequence `canUse()` returned false.";
            cancelAttackSequence();
            return false;
        }

        return getCurrentGoal().canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return getCurrentGoal().canContinueToUse();
    }

    @Override
    public void tick() {

        if(mob.getTarget() == null)
        {
            cancelAttackSequence();
        }

        getCurrentGoal().tick();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return getCurrentGoal().requiresUpdateEveryTick();
    }

    @Override
    public void stop() {
        if(finishedAttackSequence)
        {
            currentAttackIndex = 0;
            timeOfLastExecution = mob.level().getGameTime();
            finishedAttackSequence = false;
        }
        else
        {
            //SculkHorde.LOGGER.debug("Sculk Reaper Entity | Stopping Attack: " + getCurrentGoal().getClass());
            getCurrentGoal().stop();
        }
        super.stop();
    }

    public void cancelAttackSequence()
    {
        finishedAttackSequence = true;
    }

    @Override
    public Optional<String> getLastReasonForGoalNoStart() {
        return Optional.of(reasonForNoStart);
    }

    @Override
    public Optional<String> getGoalName() {
        if(getCurrentGoal() != null)
        {
            return Optional.of("Attack Sequence | " + getCurrentGoal().getClass().getName());
        }

        return Optional.of("Attack Sequence");
    }

    @Override
    public long getLastTimeOfGoalExecution() {
        return timeOfLastExecution;
    }

    @Override
    public long getTimeRemainingBeforeCooldownOver() {
        return mob.level().getGameTime() - timeOfLastExecution;
    }
}