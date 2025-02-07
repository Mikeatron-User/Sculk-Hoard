package com.github.sculkhorde.systems;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.events.HitSquadEvent;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;

public class HitSquadDispatcherSystem {

    protected static int CHECK_INTERVAL = TickUnits.convertMinutesToTicks(2);
    protected long timeOfLastCheckForDispatch = 0;
    protected static int MIN_NODES_DESTROYED = 2;
    protected static int MAX_RELATIONSHIP = -100;

    public HitSquadDispatcherSystem()
    {
    }

    protected Optional<Player> getNextTarget()
    {
        Optional<Player> target = Optional.empty();

        int worstReputationSoFar = MAX_RELATIONSHIP + 1;

        for(Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
        {
            ModSavedData.PlayerProfileEntry profile = PlayerProfileHandler.getOrCreatePlayerProfile(player);

            if(!profile.isPlayerOnline())
            {
                continue;
            }

            if(EntityAlgorithms.isLivingEntityExplicitDenyTarget(profile.getPlayer().get()))
            {
                continue;
            }

            if(SculkHorde.savedData.getNodeEntries().isEmpty())
            {
                continue;
            }

            if(SculkHorde.gravemind.isEvolutionInMatureState())
            {
                continue;
            }

            boolean hasNotDestroyedEnoughNodes = profile.getNodesDestroyed() < MIN_NODES_DESTROYED;
            boolean hasGoodRelationshipWithHorde = profile.getRelationshipToTheHorde() > MAX_RELATIONSHIP;
            boolean isHitCooldownNotOver = !profile.isHitCooldownOver();

            ModSavedData.NodeEntry entry = SculkHorde.savedData.getClosestNodeEntry((ServerLevel) player.level(), player.blockPosition());
            boolean isTooFarFromNode = BlockAlgorithms.getBlockDistanceXZ(player.blockPosition(), entry.getPosition()) > 100;

             if(isTooFarFromNode || isHitCooldownNotOver || hasGoodRelationshipWithHorde || hasNotDestroyedEnoughNodes)
            {
                continue;
            }

            if(target.isEmpty() || profile.getRelationshipToTheHorde() < worstReputationSoFar)
            {
                target = profile.getPlayer();
            }
        }

        if(SculkHorde.isDebugMode() && target.isPresent())
        {
            SculkHorde.LOGGER.info("HitSquadDispatcherSystem | DEBUG MODE ENABLED. " + target.get().getScoreboardName() + " IS BEING TARGETED. ");
            return target;
        }

        return target;
    }

    public void serverTick()
    {
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
        if(Math.abs(level.getGameTime() - timeOfLastCheckForDispatch) < CHECK_INTERVAL)
        {
            return;
        }

        if(SculkHorde.isDebugMode()) {
            SculkHorde.LOGGER.info("HitSquadDispatcherSystem | Checking To See if its time for hit event.");
        }

        Optional<Player> nextTarget = getNextTarget();

        if(nextTarget.isPresent())
        {
            SculkHorde.LOGGER.info("HitSquadDispatcherSystem | The Next Target is " + nextTarget.get().getScoreboardName());
            SculkHorde.eventSystem.addEvent(new HitSquadEvent(nextTarget.get().level().dimension(), nextTarget.get().getUUID()));
            PlayerProfileHandler.getOrCreatePlayerProfile(nextTarget.get()).setTimeOfLastHit(level.getGameTime());
        }
        else if(SculkHorde.isDebugMode())
        {
            SculkHorde.LOGGER.info("HitSquadDispatcherSystem | No Available Targets :(");
        }

        timeOfLastCheckForDispatch = level.getGameTime();
    }

}
