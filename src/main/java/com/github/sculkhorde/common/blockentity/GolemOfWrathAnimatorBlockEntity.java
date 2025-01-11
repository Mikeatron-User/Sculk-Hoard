package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.entity.GolemOfWrathEntity;
import com.github.sculkhorde.common.entity.IPurityGolemEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

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
            blockEntity.spawnGolem();
        }

    }

    public IPurityGolemEntity spawnGolem()
    {
        IPurityGolemEntity golem = new GolemOfWrathEntity(getLevel());

        Optional<BlockPos> spawnPos = getSpawnPositionsInCube(getLevel(), getBlockPos(), 10);
        if(spawnPos.isPresent())
        {
            ((LivingEntity) golem).setPos(spawnPos.get().getCenter());
        }
        else
        {
            ((LivingEntity)golem).setPos(getBlockPos().above().getCenter());
        }

        if(SculkHorde.isDebugMode()) {SculkHorde.LOGGER.info("Spawning Sculk Golem at " + ((LivingEntity) golem).position());}

        level.addFreshEntity((LivingEntity)golem);
        setGolem(golem);
        getGolem().get().setBoundBlockPos(getBlockPos());
        return golem;
    }

    public Optional<BlockPos> getSpawnPositionsInCube(Level worldIn, BlockPos origin, int length)
    {
        ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, length);
        Optional<BlockPos> spawnPos = Optional.empty();
        Random rng = new Random();
        if (!listOfPossibleSpawns.isEmpty()) {
            int randomIndex = rng.nextInt(listOfPossibleSpawns.size());
            //Get random position between 0 and size of list
            spawnPos = Optional.of(listOfPossibleSpawns.get(randomIndex));
        }
        return spawnPos;
    }

    public ArrayList<BlockPos> getSpawnPositions(Level worldIn, BlockPos origin, double pDistance)
    {
        ArrayList<BlockPos> list = new ArrayList<>();

        //Search area for block
        for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i)
        {
            for(int j = 0; (double)j < pDistance; ++j)
            {
                for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k)
                {
                    for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l)
                    {
                        //blockpos$mutable.setWithOffset(origin, k, i - 1, l);
                        BlockPos temp = new BlockPos(origin.getX() + k, origin.getY() + i-1, origin.getZ() + l);

                        //If the block is close enough and is the right blockstate
                        if (origin.closerThan(temp, pDistance) && isValidSpawnPosition(worldIn, temp))
                        {
                            list.add(temp); //add position
                        }
                    }
                }
            }
        }
        //else return empty
        return list;
    }

    public boolean isValidSpawnPosition(Level worldIn, BlockPos pos)
    {
        return BlockAlgorithms.isSolid((ServerLevel) worldIn, pos.below()) && BlockAlgorithms.isReplaceable(worldIn.getBlockState(pos));
    }
}
