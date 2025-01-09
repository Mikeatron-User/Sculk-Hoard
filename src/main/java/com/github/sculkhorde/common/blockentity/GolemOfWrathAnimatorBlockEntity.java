package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.entity.GolemOfWrathEntity;
import com.github.sculkhorde.common.entity.IPurityGolemEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class GolemOfWrathAnimatorBlockEntity extends BlockEntity {

    protected long lastTickTime = 0;

    protected int tickInterval = TickUnits.convertSecondsToTicks(3);

    protected Optional<IPurityGolemEntity> golem = Optional.empty();


    /**
     * The Constructor that takes in properties
     */
    public GolemOfWrathAnimatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOLEM_OF_WRATH_ANIMATOR_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Called when loading block entity from world.
     * @param compoundNBT Where NBT data is stored.
     */
    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        //this.storedSculkMass = compoundNBT.getInt(storedSculkMassIdentifier);
    }

    /**
     * ???
     * @param compoundNBT Where NBT data is stored??
     * @return ???
     */
    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        //compoundNBT.putInt(storedSculkMassIdentifier, this.storedSculkMass);
        super.saveAdditional(compoundNBT);
    }

    public Optional<LivingEntity> getGolemAsLivingEntity()
    {
        if(golem.isPresent())
        {
            return Optional.of((LivingEntity) golem.get());
        }

        return Optional.empty();
    }

    public Optional<IPurityGolemEntity> getGolem()
    {
        // If golem is dead, set to null
        if(getGolemAsLivingEntity().isPresent())
        {
            if(getGolemAsLivingEntity().get().isDeadOrDying())
            {
                golem = Optional.empty();
            }
        }

        return golem;
    }

    public Optional<IPurityGolemEntity> setGolem(IPurityGolemEntity golemIn)
    {
        golem = Optional.of(golemIn);
        return golem;
    }

    public IPurityGolemEntity spawnGolem(BlockPos pos)
    {
        IPurityGolemEntity golem = new GolemOfWrathEntity(getLevel());
        ((LivingEntity)golem).setPos(pos.getCenter());
        level.addFreshEntity((LivingEntity)golem);
        setGolem(golem);
        getGolem().get().setBoundBlockPos(getBlockPos());
        return golem;
    }



    public static void tick(Level level, BlockPos blockPos, BlockState blockState, GolemOfWrathAnimatorBlockEntity blockEntity)
    {
        // If world is not a server world, return
        if(level.isClientSide)
        {
            return;
        }
        // Tick every 10 seconds
        if(level.getGameTime() - blockEntity.lastTickTime < blockEntity.tickInterval)
        {
            return;
        }
        blockEntity.lastTickTime = level.getGameTime();

        // If our golem is not spawned yet, spawn him.
        if(blockEntity.getGolem().isEmpty())
        {
            blockEntity.spawnGolem(blockPos.above());
        }

    }



}
