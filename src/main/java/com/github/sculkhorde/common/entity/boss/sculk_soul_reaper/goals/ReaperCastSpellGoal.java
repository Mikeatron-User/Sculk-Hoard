package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.goal.AttackStepGoal;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ReaperCastSpellGoal extends AttackStepGoal {
    protected final SculkSoulReaperEntity mob;
    protected int cooldownTicksElapsed = getExecutionCooldown();
    protected int castingTime = 0;
    protected boolean spellCasted = false;

    public ReaperCastSpellGoal(SculkSoulReaperEntity mob) {
        this.mob = mob;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    protected int getBaseCastingTime() { return TickUnits.convertSecondsToTicks(1);}
    protected int getCastingTimeElapsed()
    {
        return castingTime;
    }

    protected int getExecutionCooldown() { return TickUnits.convertSecondsToTicks(0); }
    protected int getCooldownTicksElapsed() { return cooldownTicksElapsed; }

    protected boolean mustSeeTarget()
    {
        return true;
    }

    protected void setSpellCompleted()
    {
        spellCasted = true;
    }


    @Override
    public boolean canUse()
    {
        return mob.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !spellCasted && mob.getTarget() != null;
    }

    @Override
    public void start()
    {
        super.start();

        if(mob.level().isClientSide())
        {
            return;
        }

        playCastingAnimation();
        mob.level().playSound(mob, mob.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    protected void playCastingAnimation()
    {
        mob.triggerAnim(SculkSoulReaperEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, SculkSoulReaperEntity.ATTACK_SPELL_CHARGE_ID);
    }

    protected void playAttackAnimation()
    {
        mob.triggerAnim(SculkSoulReaperEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, SculkSoulReaperEntity.ATTACK_SPELL_USE_ID);
    }

    protected void doAttackTick()
    {
        setSpellCompleted();
    }

    @Override
    public void tick()
    {
        super.tick();

        if(mob.level().isClientSide())
        {
            return;
        }

        if(getCastingTimeElapsed() < getBaseCastingTime())
        {
            castingTime++;
            return;
        }

        if(spellCasted)
        {
            return;
        }
        playAttackAnimation();
        doAttackTick();
    }

    @Override
    public void stop()
    {
        super.stop();
        cooldownTicksElapsed = 0;
        spellCasted = false;
        castingTime = 0;
    }
}
