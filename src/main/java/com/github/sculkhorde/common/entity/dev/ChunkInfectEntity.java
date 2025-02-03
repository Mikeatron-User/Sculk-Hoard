package com.github.sculkhorde.common.entity.dev;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkCursorInfector;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ChunkInfectEntity extends Entity {

    public ChunkInfectEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        infector.level((ServerLevel) p_19871_);
    }

    public ChunkInfectEntity(Level worldIn) {
        this(ModEntities.CHUNK_INFECT_ENTITY.get(), worldIn);
    }

    protected boolean shouldDestroy = false;

    protected boolean ready = false;
    protected int radius = 0;

    public final ChunkCursorInfector infector =
            ChunkCursorInfector.of()
                    .blocksPerTick(128)
                    .disableAdjacentBlocks()
                    .executeOnEnd(this::end);

    protected Entity trackedEntity;
    protected BlockPos currentBlock = null;
    protected ResourceKey<Level> currentLevel = null;


    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setCenter(BlockPos center) {
        this.setPos(center.getCenter());
        infector.center(center, radius);
    }

    public void shouldDestroy(boolean state) {
        shouldDestroy = state;
    }

    public void setTrackedEntity(Entity entity) {
        trackedEntity = entity;
    }

    //BlockAlgorithms.areTheseDimensionsEqual(entry.getDimension().dimension(), worldIn.dimension())

    protected int t = 0;

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        SculkHorde.LOGGER.info(this + ": Tracked Entity: " + trackedEntity);

        currentLevel = level().dimension();
        currentBlock = this.blockPosition();
        this.setPos(currentBlock.getCenter());

        infector.level((ServerLevel) level())
                .center(this.blockPosition(), radius);

        ready = true;
    }



    @Override
    public void tick() {
        super.tick();
        if (isAddedToWorld()) {
            t++;
            if (t >= 10 && trackedEntity != null) {
                this.setPos(trackedEntity.position());
                SculkHorde.LOGGER.info("Updating Location to: " + trackedEntity.position());
                t = 0;
            }

            if (!(this.blockPosition().getX() == currentBlock.getX() && this.blockPosition().getZ() == currentBlock.getZ()) || level().dimension() != currentLevel) {
                currentBlock = this.blockPosition();
                infector.pause();
                infector.level((ServerLevel) level()).center(this.blockPosition(), radius);
                infector.resume();
                ready = true;
            }

            if (ready) infector.tick();
        }
    }

    public void end() {
        ready = false;
        if (shouldDestroy) {
            SculkHorde.LOGGER.info(this + ": Task Complete! Destroying...");
            this.discard();
        }
        else {
            SculkHorde.LOGGER.info(this + ": Task Complete!");
        }
    }

    @Override protected void defineSynchedData() {}
    @Override protected void readAdditionalSaveData(CompoundTag p_20052_) {}
    @Override protected void addAdditionalSaveData(CompoundTag p_20139_) {}
}
