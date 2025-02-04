package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Random;

public class SummonReinforcementsCommand implements Command<CommandSourceStack> {

    private final TargetParameters hostileTargetParameters = new TargetParameters().enableTargetHostiles().enableTargetInfected();
    private final TargetParameters infectableTargetParameters = new TargetParameters().enableTargetPassives();
    
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("summon_reinforcements").requires(command -> command.hasPermission(1))
                .executes(new SummonReinforcementsCommand());

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context)
    {
        context.getSource().sendSuccess(()->Component.literal(
                "Reinforcement Request Received."
                ), false);
        
        Vec3 reinforcementPosition = context.getSource().getPosition();

        summonReinforcement(context.getSource().getLevel(), BlockPos.containing(reinforcementPosition));
        
        return 0;
    }

    private void summonReinforcement(ServerLevel level, BlockPos blockPos)
    {         
        ((ServerLevel)level).sendParticles(ParticleTypes.SCULK_SOUL, blockPos.getX() + 0.5D, blockPos.getY() + 1.15D, blockPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        ((ServerLevel)level).playSound((Player)null, blockPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
        //Choose spawn positions
        ArrayList<BlockPos> possibleSpawnPositions = this.getSpawnPositionsInCube((ServerLevel) level, blockPos, 5, 10);

        BlockPos[] finalizedSpawnPositions = new BlockPos[10];

        //Create MAX_SPAWNED_ENTITIES amount of Reinforcement Requests
        for (int iterations = 0; iterations < possibleSpawnPositions.size(); iterations++)
        {
            finalizedSpawnPositions[iterations] = possibleSpawnPositions.get(iterations);
        }

        //If the array is empty, just spawn above block
        if (possibleSpawnPositions.isEmpty()) {
            finalizedSpawnPositions[0] = blockPos.above();
        }

        //Give gravemind context to our request to make more informed situations
        ReinforcementRequest request = new ReinforcementRequest((ServerLevel) level, finalizedSpawnPositions);
        request.sender = ReinforcementRequest.senderType.Summoner;
        request.is_aggressor_nearby = true;

        //Request reinforcement from entity factory (this request gets approved or denied by gravemind)
        SculkHorde.entityFactory.createReinforcementRequestFromSummoner(level, blockPos, false, request);
    }


    /**
     * Returns true if the block below is a sculk block,
     * and if the two blocks above it are free.
     * @param worldIn The World
     * @param pos The Position to spawn the entity
     * @return True/False
     */
    public boolean isValidSpawnPosition(ServerLevel worldIn, BlockPos pos)
    {
        boolean isBlockBelowSolid = BlockAlgorithms.isSolid(worldIn, pos.below());
        boolean isBaseBlockReplaceable = worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER);
        boolean isBlockAboveReplaceable = worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

        return isBlockBelowSolid && isBaseBlockReplaceable && isBlockAboveReplaceable;

    }

    public ArrayList<BlockPos> getSpawnPositionsInCube(ServerLevel worldIn, BlockPos origin, int length, int amountOfPositions)
    {
        ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, length);
        ArrayList<BlockPos> finalList = new ArrayList<>();
        Random rng = new Random();
        for(int count = 0; count < amountOfPositions && listOfPossibleSpawns.size() > 0; count++)
        {
            int randomIndex = rng.nextInt(listOfPossibleSpawns.size());
            //Get random position between 0 and size of list
            finalList.add(listOfPossibleSpawns.get(randomIndex));
            listOfPossibleSpawns.remove(randomIndex);
        }
        return finalList;
    }

    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param pDistance The search distance
     * @return The position of the block
     */
    public ArrayList<BlockPos> getSpawnPositions(ServerLevel worldIn, BlockPos origin, double pDistance)
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
                        if (origin.closerThan(temp, pDistance)
                                && isValidSpawnPosition(worldIn, temp))
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

}
