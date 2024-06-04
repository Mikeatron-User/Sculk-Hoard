package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.github.sculkhorde.util.EntityAlgorithms.*;

public class TargetParameters
{
    private Mob mob;

    private boolean targetHostiles = false; //Should we attack hostiles?
    private boolean targetPassives = false; //Should we target passives?
    private boolean targetInfected = false;//If a passive or hostile is infected, should we attack it?
    private boolean targetBelow50PercentHealth = true; //Should we target entities below 50% health?
    private boolean targetSwimmers = false; //Should we target entities that can swim?
    private boolean targetEntitiesInWater = true; //Should we target entities that are in water?
    private boolean mustSeeTarget = false; //Should we only target entities we can see?
    private long lastTargetSeenTime = System.currentTimeMillis(); //The last time we saw the target
    private long MAX_TARGET_UNSEEN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(30); //The max time we can go without seeing the target
    private boolean mustReachTarget = false; //Should we only target entities we can reach?
    //A hash map which we store a blacklist of mobs we should not attack. Should use UUIDs of mobs to identify
    private HashMap<UUID, Long> blacklist = new HashMap<>();
    private boolean canBlackListMobs = true; //Should we blacklist mobs?
    private boolean targetWalkers;


    public TargetParameters()
    {
        this.mob = null;
    }

    public TargetParameters(Mob mob)
    {
        this.mob = mob;
    }


    // Predicate to test if valid target
    public final Predicate<LivingEntity> isPossibleNewTargetValid = (e) -> {
        return isEntityValidTarget(e, false);
    };


    public boolean isEntityValidTarget(LivingEntity e, boolean validatingExistingTarget)
    {
        boolean isValid = true;

        if(EntityAlgorithms.isLivingEntityExplicitDenyTarget(e))
        {
            isValid = false;
        }



        //If player is in creative or spectator
        else if(e instanceof Player && (((Player) e).isCreative() || ((Player) e).isSpectator()))
        {
            isValid = false;
        }

        //If we do not attack swimmers and target is a swimmer
        if(!isTargetingSwimmers() && isLivingEntitySwimmer(e))
        {
            isValid = false;
        }

        //If we do not attack entities in water and target is in water
        if(!isTargetingEntitiesInWater() && e.isInWater())
        {
            isValid = false;
        }

        if(isIgnoringTargetBelow50PercentHealth() && (e.getHealth() < e.getMaxHealth() / 2))
        {
            isValid = false;
        }

        if(!isTargetWalkers() && !e.isInWater())
        {
            isValid = false;
        }

        if(isMustSeeTarget() && !canSeeTarget(e))
        {
            isValid = false;
        }

        //If we must reach target and cannot reach target
        // NOTE: validating existing targets gets called significantly more often.
        // When we do this, we disable reach check because it lags to all hell.
        if(!validatingExistingTarget && mustReachTarget() && !canReach(e))
        {
            isValid = false;
        }

        if(e instanceof InfestationPurifierEntity)
        {
            isValid = true;
        }

        if(e instanceof Player)
        {
            isValid = true;
        }

        // If Blacklisted
        if(isOnBlackList((Mob) e))
        {
            isValid = false;
        }

        //If we do not attack infected and entity is infected
        if(!isTargetingInfected() && isLivingEntityInfected(e))
        {
            isValid = false;
        }

        //If we do not attack passives and entity is non-hostile
        if(!isTargetingPassives() && !isLivingEntityHostile(e)) //NOTE: horde assumes everything is passive until provoked
        {
            isValid = false;
        }

        //If we do not attack hostiles and target is hostile
        if(!isTargetingHostiles() && isLivingEntityHostile(e))
        {
            isValid = false;
        }

        return isValid;
    }

    public TargetParameters enableBlackListMobs()
    {
        canBlackListMobs = true;
        return this;
    }

    public TargetParameters disableBlackListMobs()
    {
        canBlackListMobs = false;
        return this;
    }

    public boolean canBlackListMobs()
    {
        return canBlackListMobs;
    }

    public TargetParameters enableTargetHostiles()
    {
        targetHostiles = true;
        return this;
    }

    public boolean isTargetingHostiles()
    {
        return targetHostiles;
    }

    public TargetParameters enableTargetPassives()
    {
        targetPassives = true;
        return this;
    }

    public boolean isTargetingPassives()
    {
        return targetPassives;
    }

    public TargetParameters enableTargetInfected()
    {
        targetInfected = true;
        return this;
    }

    public boolean isTargetingInfected()
    {
        return targetInfected;
    }

    public TargetParameters ignoreTargetBelow50PercentHealth()
    {
        targetBelow50PercentHealth = false;
        return this;
    }


    public boolean isIgnoringTargetBelow50PercentHealth()
    {
        return targetBelow50PercentHealth;
    }

    public TargetParameters disableTargetWalkers()
    {
        targetWalkers = false;
        return this;
    }

    public boolean isTargetWalkers()
    {
        return targetWalkers;
    }

    public TargetParameters enableTargetSwimmers()
    {
        targetSwimmers = true;
        return this;
    }

    public boolean isTargetingSwimmers()
    {
        return targetSwimmers;
    }

    public TargetParameters disableTargetingEntitiesInWater()
    {
        targetEntitiesInWater = false;
        return this;
    }

    public boolean isTargetingEntitiesInWater()
    {
        return targetEntitiesInWater;
    }

    public TargetParameters enableMustSeeTarget()
    {
        if(this.mob == null)
        {
            throw new IllegalStateException("Cannot enable must reach target without a mob");
        }
        mustSeeTarget = true;
        return this;
    }

    public boolean isMustSeeTarget()
    {
        return mustSeeTarget;
    }

    public boolean canSeeTarget(LivingEntity e)
    {
        if(this.mob == null)
        {
            throw new IllegalStateException("Cannot enable must see target without a mob");
        }
        if(e == null)
        {
            return false;
        }
        return mob.getSensing().hasLineOfSight(e);
    }

    public TargetParameters enableMustReachTarget()
    {
        if(this.mob == null)
        {
            throw new IllegalStateException("Cannot enable must reach target without a mob");
        }
        mustReachTarget = true;
        return this;
    }

    public boolean mustReachTarget()
    {
        return mustReachTarget;
    }

    private boolean canReach(LivingEntity pTarget)
    {
        Path path = this.mob.getNavigation().createPath(pTarget, 0);
        if (path == null)
        {
            return false;
        }
        else
        {
            Node pathpoint = path.getEndNode();
            if (pathpoint == null)
            {
                return false;
            }
            else
            {
                int i = pathpoint.x - Mth.floor(pTarget.getX());
                int j = pathpoint.z - Mth.floor(pTarget.getZ());
                return (double)(i * i + j * j) <= 50;
            }
        }
    }


    public void addToBlackList(Mob entity)
    {
        blacklist.put(entity.getUUID(), System.currentTimeMillis());
    }

    public void removeFromBlackList(Mob entity)
    {
        blacklist.remove(entity.getUUID());
    }

    // Is mob on blacklist
    public boolean isOnBlackList(Mob entity)
    {
        return blacklist.containsKey(entity.getUUID());
    }
}
