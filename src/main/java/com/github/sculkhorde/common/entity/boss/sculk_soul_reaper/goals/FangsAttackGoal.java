package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FangsAttackGoal extends ReaperCastSpellGoal
{
    public FangsAttackGoal(SculkSoulReaperEntity mob) {
        super(mob);
    }

    @Override
    public boolean canUse()
    {
        if(!super.canUse())
        {
            return false;
        }
        else if(!mob.getTarget().onGround())
        {
            return false;
        }

        return true;
    }

    @Override
    protected void doAttackTick() {
        performSpellCasting();
        setSpellCompleted();
    }

    // Performs the spell casting action
    protected void performSpellCasting() {
        LivingEntity targetEntity = mob.getTarget();
        double minY = Math.min(targetEntity.getY(), mob.getY());
        double maxY = Math.max(targetEntity.getY(), mob.getY()) + 1.0D;
        float angleToTarget = (float) Mth.atan2(targetEntity.getZ() - mob.getZ(), targetEntity.getX() - mob.getX());

        // If the target is within a close range
        if (mob.distanceToSqr(targetEntity) < 9.0D) {
            // Create 5 spell entities in a circular pattern
            for (int i = 0; i < 5; ++i) {
                float angleOffset = angleToTarget + (float)i * (float)Math.PI * 0.4F;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleOffset) * 1.5D, mob.getZ() + (double)Mth.sin(angleOffset) * 1.5D, minY, maxY, angleOffset, 0);
            }

            // Create 8 spell entities in a larger circular pattern
            for (int k = 0; k < 8; ++k) {
                float angleOffset = angleToTarget + (float)k * (float)Math.PI * 2.0F / 8.0F + 1.2566371F;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleOffset) * 2.5D, mob.getZ() + (double)Mth.sin(angleOffset) * 2.5D, minY, maxY, angleOffset, 3);
            }
        } else {
            // Create 16 spell entities in a line
            for (int l = 0; l < Math.min(mob.distanceToSqr(targetEntity), 64); ++l) {
                double distanceMultiplier = 1.25D * (double)(l + 1);
                int delay = 1 * l;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleToTarget) * distanceMultiplier, mob.getZ() + (double)Mth.sin(angleToTarget) * distanceMultiplier, minY, maxY, angleToTarget, delay);
            }
        }
    }

    // Creates a spell entity at the specified coordinates
    private void createSpellEntity(double x, double z, double minY, double maxY, float angle, int delay) {
        BlockPos blockPos = BlockPos.containing(x, maxY, z);
        boolean foundSuitablePosition = false;
        double yOffset = 0.0D;

        // Find a suitable position for the spell entity
        do {
            BlockPos belowBlockPos = blockPos.below();
            BlockState belowBlockState = mob.level().getBlockState(belowBlockPos);
            if (belowBlockState.isFaceSturdy(mob.level(), belowBlockPos, Direction.UP)) {
                if (!mob.level().isEmptyBlock(blockPos)) {
                    BlockState blockState = mob.level().getBlockState(blockPos);
                    VoxelShape voxelShape = blockState.getCollisionShape(mob.level(), blockPos);
                    if (!voxelShape.isEmpty()) {
                        yOffset = voxelShape.max(Direction.Axis.Y);
                    }
                }

                foundSuitablePosition = true;
                break;
            }

            blockPos = blockPos.below();
        } while (blockPos.getY() >= Mth.floor(minY) - 1);

        // Add the spell entity to the world if a suitable position is found
        if (foundSuitablePosition) {
            mob.level().addFreshEntity(new EvokerFangs(mob.level(), x, (double)blockPos.getY() + yOffset, z, angle, delay, mob));
        }
    }
}
