package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.VirtualSurfaceInfestorCursor;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactory;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

import static com.github.sculkhorde.common.block.SculkMassBlock.WATERLOGGED;

public class SculkMassBlockEntity extends BlockEntity {

    /**
     * storedSculkMass is the value of sculk mass was this block has.
     * This value is used to determine the mobs that are spawened or the area
     * that will be infected by the sculk.
     * storedSculkMassIdentifier is the string used to identify storedSculkMass
     * in CompoundNBT. It allows us to read/write to it.<br>
     */
    protected int storedSculkMass = 0;
    protected String storedSculkMassIdentifier = "storedSculkMass";
    protected long lastTickTime = 0;
    protected int tickInterval = TickUnits.convertSecondsToTicks(3);


    /**
     * The Constructor that takes in properties
     */
    public SculkMassBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCULK_MASS_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Called when loading block entity from world.
     * @param compoundNBT Where NBT data is stored.
     */
    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        this.storedSculkMass = compoundNBT.getInt(storedSculkMassIdentifier);
    }

    /**
     * ???
     * @param compoundNBT Where NBT data is stored??
     * @return ???
     */
    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        compoundNBT.putInt(storedSculkMassIdentifier, this.storedSculkMass);
        super.saveAdditional(compoundNBT);
    }

    public int getStoredSculkMass()
    {
        return storedSculkMass;
    }

    public void setStoredSculkMass(int value)
    {
        storedSculkMass = Math.max(0, value);
    }

    public void addStoredSculkMass(int value)
    {
        storedSculkMass = Math.max(0, storedSculkMass + value);
    }


    public static ReinforcementRequest createReinforcementRequest(Level level, BlockPos blockPos, BlockState blockState, SculkMassBlockEntity blockEntity)
    {
        ReinforcementRequest context = new ReinforcementRequest((ServerLevel) level, blockPos);

        context.sender = ReinforcementRequest.senderType.SculkMass;
        context.budget = blockEntity.getStoredSculkMass();


        // Better Control if aquatic mobs spawn
        if(blockState.getValue(WATERLOGGED))
        {
            context.deniedStrategicValues.add(EntityFactoryEntry.StrategicValues.EffectiveOnGround);
        }
        else
        {
            context.deniedStrategicValues.add(EntityFactoryEntry.StrategicValues.Aquatic);
        }

        return context;
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkMassBlockEntity blockEntity)
    {
        // If world is not a server world, return
        if(level.isClientSide)
        {
            return;
        }

        // Delay the spawning of mobs for balance, and so that the mass block can recognise if it is under water.
        // When they are first placed, they are not waterlogged. This will result in spawning a surface unit
        // for the first tick, instead of a aquatic unit.
        if(blockEntity.lastTickTime == 0)
        {
            blockEntity.lastTickTime = level.getGameTime();
        }


        // Tick every 10 seconds
        if(level.getGameTime() - blockEntity.lastTickTime < blockEntity.tickInterval)
        {
            return;
        }

        blockEntity.lastTickTime = level.getGameTime();

        if(blockEntity.getStoredSculkMass() <= 0)
        {
            level.destroyBlock(blockPos, false);
            return;
        }

        if(SculkHorde.populationHandler.isPopulationAtMax())
        {
            SculkHorde.savedData.addSculkAccumulatedMass(blockEntity.getStoredSculkMass());
            blockEntity.setStoredSculkMass(0);
            return;
        }

        // If we can spawn reinforcements

        EntityFactory entityFactory = SculkHorde.entityFactory;
        ReinforcementRequest context = createReinforcementRequest(level, blockPos, blockState, blockEntity);


        // Do not spawn infectors if infection not enabled.
        if(!ModConfig.SERVER.block_infestation_enabled.get() || SculkHorde.cursorSystem.isCursorPopulationAtMax())
        {
            SculkHorde.savedData.addSculkAccumulatedMass(blockEntity.getStoredSculkMass());
            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            return;
        }

        //Attempt to call in reinforcements and then update stored sculk mass
        entityFactory.requestReinforcementSculkMass(level, blockPos, context);

        // Spawn Block Infection
        Optional<VirtualSurfaceInfestorCursor> cursor = CursorSystem.createSurfaceInfestorVirtualCursor(level, blockPos);
        if(cursor.isPresent())
        {
            cursor.get().setMaxTransformations(blockEntity.getStoredSculkMass() * 100);
            cursor.get().setMaxRange(blockEntity.getStoredSculkMass() * 10);
        }
        level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());

    }
}
