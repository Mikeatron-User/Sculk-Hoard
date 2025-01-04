package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulBlastAttackEntity;
import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;

import java.util.Optional;

public class SoulBlastAttackGoal extends ReaperCastSpellGoal implements IDebuggableGoal
{

    protected String reasonForNoStart = "None";

    public SoulBlastAttackGoal(SculkSoulReaperEntity mob) {
        super(mob);
    }

    @Override
    protected boolean mustSeeTarget() {
        return false;
    }

    @Override
    public void start()
    {
        super.start();
        if(mob.level().isClientSide())
        {
            return;
        }

        this.mob.getNavigation().stop();
        EntityType.LIGHTNING_BOLT.spawn((ServerLevel) mob.level(), BlockPos.containing(mob.getEyePosition()), MobSpawnType.SPAWNER);
    }

    @Override
    protected void doAttackTick() {
        summonAttackEntity();
        setSpellCompleted();
    }

    public void summonAttackEntity()
    {

        SoulBlastAttackEntity attackEntity =  new SoulBlastAttackEntity(mob.level(), mob);
        attackEntity.setPos(mob.position().add(0, mob.getEyeHeight() + 5, 0));

        mob.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level().addFreshEntity(attackEntity);

    }

    @Override
    public Optional<String> getLastReasonForGoalNoStart() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getGoalName() {
        return Optional.of("SoulBlastGoal");
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