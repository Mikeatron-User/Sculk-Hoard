package com.github.sculkhorde.util;

import com.github.sculkhorde.common.effect.SculkBurrowedEffect;
import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.goal.CustomMeleeAttackGoal;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.misc.ModColaborationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class EntityAlgorithms {

    public static void pushAwayEntitiesFromPosition(Vec3 origin, LivingEntity entityToPush, float pushAwayStrength, float pushUpStrength)
    {
        // Calculate the vector from the black hole to the entity
        double dx = entityToPush.getX() - origin.x;
        double dz = entityToPush.getZ() - origin.z;

        // Calculate the horizontal distance to the black hole (ignore vertical)
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);


        // Normalize the horizontal vector (prevent NaN)
        double normalizedDx = horizontalDistance == 0 ? 0 : dx / horizontalDistance;
        double normalizedDz = horizontalDistance == 0 ? 0 : dz / horizontalDistance;

        // Apply the push outwards
        entityToPush.push(normalizedDx * pushAwayStrength, pushUpStrength, normalizedDz * pushAwayStrength); //Apply the combined push
    }

    public static void lookAt(Entity entity, Vec3 target)
    {
        double deltaX = target.x() - entity.getX();
        double deltaY = target.y() - entity.getEyeY(); // Using eye height for better look-at
        double deltaZ = target.z() - entity.getZ();
        double horizontalDistance = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));

        float yaw = (float) (Mth.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;
        float pitch = (float) (-(Mth.atan2(deltaY, horizontalDistance) * (180 / Math.PI)));

        entity.setYRot(yaw);
        entity.setXRot(pitch);
    }

    // You can also pass in an x, y, and z for easier use elsewhere
    public static void lookAt(Entity entity, Entity target) {
        lookAt(entity, target.position());
    }
    public static void doSculkTypeDamageToEntity(LivingEntity aggressor, LivingEntity target, float totalDamage, float guaranteedDamage)
    {
        if(target.isInvulnerable())
        {
            return;
        }

        if(target instanceof Player player)
        {
            if(player.isSpectator() || player.isCreative())
            {
                return;
            }
        }


        float nonGuaranteedDamage = Math.max(totalDamage - guaranteedDamage, 0.1F);
        target.hurt(aggressor.damageSources().mobAttack(aggressor), nonGuaranteedDamage);

        float newHealth = Math.max(target.getHealth() - guaranteedDamage, 1);
        if(newHealth <= 1)
        {
            target.hurt(aggressor.damageSources().indirectMagic(aggressor, aggressor), guaranteedDamage);
        }
        else
        {
            target.setHealth(newHealth);
            target.hurt(aggressor.damageSources().indirectMagic(aggressor, aggressor), 1F);
        }


        /*
        float newHealth = Math.max(target.getHealth() - guaranteedDamage, 1);
        target.setHealth(newHealth);

        float nonGuaranteedDamage = Math.max(totalDamage - guaranteedDamage, 0.1F);
        target.hurt(aggressor.damageSources().mobAttack(aggressor), nonGuaranteedDamage);

         */
    }

    public static boolean canApplyEffectsToTarget(LivingEntity entity, MobEffect debuff)
    {
        boolean isEntityNull = entity == null;
        boolean isEntityDead = entity.isDeadOrDying();
        if(isEntityNull || isEntityDead)
        {
            return false;
        }
		
		boolean isEntityImmune = !entity.canBeAffected(new MobEffectInstance(debuff, 5, 0));
        boolean isEntityInvulnerable = entity.isInvulnerable();
        boolean isEntityAttackable = entity.isAttackable();
        boolean doesEntityHaveDebuffAlready = entity.hasEffect(debuff);
        if(isEntityImmune || isEntityInvulnerable || !isEntityAttackable || doesEntityHaveDebuffAlready)
        {
            return false;
        }

        if(entity instanceof InfestationPurifierEntity && debuff instanceof SculkBurrowedEffect)
        {
            return false;
        }

        return true;
    }

    public static void applyEffectToTarget(LivingEntity entity, MobEffect debuff, int duration, int amplifier)
    {
        if(canApplyEffectsToTarget(entity, debuff))
        {
            entity.getServer().tell(new TickTask(entity.getServer().getTickCount() + 1, () -> {
                entity.addEffect(new MobEffectInstance(debuff, duration, amplifier));
            }));

            if(debuff == ModMobEffects.SCULK_INFECTION.get() || debuff == ModMobEffects.DISEASED_CYSTS.get())
            {
                SculkHorde.statisticsData.incrementTotalVictimsInfested();
            }
        }
    }

    public static void reducePurityEffectDuration(LivingEntity entity, int amountInTicks)
    {
        if(entity.hasEffect(ModMobEffects.PURITY.get()))
        {
            entity.getServer().tell(new TickTask(entity.getServer().getTickCount() + 1, () -> {
                MobEffectInstance purityEffect = entity.getEffect(ModMobEffects.PURITY.get());
                int newDuration = Math.max(purityEffect.getDuration() - amountInTicks, 0);
                entity.removeEffect(ModMobEffects.PURITY.get());
                entity.addEffect(new MobEffectInstance(ModMobEffects.PURITY.get(), newDuration, purityEffect.getAmplifier()));
            }));
        }
    }

    /**
     * Returns the block position a player is staring at
     * @param player The player to check
     * @param isFluid Should we consider fluids
     * @return the position the player is staring at
     */
    @Nullable
    public static BlockPos playerTargetBlockPos(Player player, boolean isFluid)
    {
        HitResult block =  player.pick(200.0D, 0.0F, isFluid);

        if(block.getType() == HitResult.Type.BLOCK)
        {
            return ((BlockHitResult)block).getBlockPos();
        }
        return null;
    }

    /**
     * Creates a 3D cube around a given origin. The origin is the centroid.
     * @param originX The X coordinate of the origin
     * @param originY The Y coordinate of the origin
     * @param originZ The Z coordinate of the origin
     * @return Returns the Bounding Box
     */
    public static AABB getSearchAreaRectangle(double originX, double originY, double originZ, double w, double h, double l)
    {
        double x1 = originX - w;
        double y1 = originY - h;
        double z1 = originZ - l;
        double x2 = originX + w;
        double y2 = originY + h;
        double z2 = originZ + l;
        return new AABB(x1, y1, z1, x2, y2, z2);
    }


    /**
     * Determines if an Entity belongs to the sculk based on rules
     * @return True if Valid, False otherwise
     */
    public static Predicate<LivingEntity> isSculkLivingEntity = (e) ->
    {
        if(e == null)
        {
            return false;
        }
        boolean hasSculkEntityTag = e.getType().is(ModEntities.EntityTags.SCULK_ENTITY);
        boolean implementsISculkSmartEntity = e instanceof ISculkSmartEntity;

        // Making sure that the entity with this tag is an actual sculk horde entity.
        if(hasSculkEntityTag && !implementsISculkSmartEntity && !(e instanceof SculkBeeHarvesterEntity))
        {
            SculkHorde.LOGGER.debug("ERROR | Do not give non-sculk horde entity " + e.getName().getString() + " the sculk_entity tag. This will crash your game. Mod author or modpack author, use sculk_horde_do_not_attack");
            return false;
        }

        if(hasSculkEntityTag && implementsISculkSmartEntity)
        {
            return true;
        }

        return false;
    };


    /**
     * Determines if an Entity is Infected based on if it has a potion effect
     * @param e The Given Entity
     * @return True if Infected, False otherwise
     */
    public static boolean isLivingEntityInfected(LivingEntity e)
    {
        return e.hasEffect(ModMobEffects.SCULK_INFECTION.get());
    }


    /**
     * Determines if an Entity is an aggressor.
     * @param entity The Given Entity
     * @return True if enemy, False otherwise
     */
    public static boolean isLivingEntityHostile(LivingEntity entity)
    {
        return SculkHorde.savedData.getHostileEntries().get(entity.getType().toString()) != null;
    }

    public static boolean isLivingEntitySwimmer(LivingEntity entity)
    {
        // The gramemind does not store swimmers, we need to figure if a mob is swimming
        // by using the entity's ability to swim
        return entity instanceof WaterAnimal;
    }

    public static boolean isLivingEntityInvulnerable(LivingEntity entity)
    {
        return entity.isInvulnerable() || !entity.isAttackable();
    }

    public static boolean isLivingEntityAllyToSculkHorde(LivingEntity entity)
    {
        if(ModColaborationHelper.doesEntityBelongToFromAnotherWorldMod(entity) && !ModConfig.SERVER.target_faw_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToSporeMod(entity) && !ModConfig.SERVER.target_spore_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToDawnOfTheFloodMod(entity) && !ModConfig.SERVER.target_dawn_of_the_flood_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToAnotherDimensionInvasionMod(entity) && !ModConfig.SERVER.target_another_dimension_infection_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToSwarmInfectionMod(entity) && !ModConfig.SERVER.target_swarm_infection_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToTheFleshThatHatesMod(entity) && !ModConfig.SERVER.target_the_flesh_that_hates_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToWitheringAwayRebornMod(entity) && !ModConfig.SERVER.target_withering_away_reborn_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToAbominationsInfectionMod(entity) && !ModConfig.SERVER.target_abominations_infection_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToPrionInfectionMod(entity) && !ModConfig.SERVER.target_prion_infection_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToBulbusMod(entity) && !ModConfig.SERVER.target_bulbus_infection_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToEntomophobiaMod(entity) && !ModConfig.SERVER.target_entomophobia_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToCompleteDistortionInfectionMod(entity) && !ModConfig.SERVER.target_complete_distortion_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToPharyriosisParasiteInfectionMod(entity) && !ModConfig.SERVER.target_phayriosis_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToScapeAndRunParasitesMod(entity) && !ModConfig.SERVER.target_phayriosis_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToDeeperAndDarkerMod(entity) && !ModConfig.SERVER.target_phayriosis_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.doesEntityBelongToMIAllianceMod(entity) && !ModConfig.SERVER.target_phayriosis_entities.get())
        {
            return true;
        }

        if(entity instanceof Player player)
        {
            return PlayerProfileHandler.isPlayerActiveVessel(player);
        }


        return false;
    }

    /**
     * Determines if we should avoid targeting an entity at all costs.
     * @param entity The Given Entity
     * @return True if we should avoid, False otherwise
     */
    public static boolean isLivingEntityExplicitDenyTarget(LivingEntity entity)
    {
        if(entity == null)
        {
            return true;
        }

        // Is entity not a mob or player?
        if(!(entity instanceof Mob) && !(entity instanceof Player))
        {
            return true;
        }

        //If not attackable or invulnerable or is dead/dying
        if(!entity.isAttackable() || entity.isInvulnerable() || !entity.isAlive())
        {
            return true;
        }

        if(entity instanceof Player player)
        {
            if(player.isCreative() || player.isSpectator())
            {
                return true;
            }

            if(player.hasEffect(ModMobEffects.SCULK_VESSEL.get()))
            {
                return true;
            }
        }

        if(entity instanceof Creeper)
        {
            return true;
        }

        if(isSculkLivingEntity.test(entity))
        {
            return true;
        }

        if(entity.getType().is(ModEntities.EntityTags.SCULK_HORDE_DO_NOT_ATTACK))
        {
            return true;
        }

        if(ModConfig.SERVER.isEntityOnSculkHordeTargetBlacklist(entity))
        {
            return true;
        }

        if(entity.getType().is(ModEntities.EntityTags.SCULK_ENTITY))
        {
            return true;
        }

        if(isLivingEntityAllyToSculkHorde(entity))
        {
            return true;
        }

        if(ModColaborationHelper.isThisAnArsNouveauBlackListEntity(entity))
        {
            return true;
        }

        return false;
    }

    public static void spawnEntitiesOnCircumference(ServerLevel level, Vec3 origin, int radius, int amount, EntityType<?> type)
    {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        ArrayList<Vec3> possibleSpawns = BlockAlgorithms.getPointsOnCircumferenceVec3(origin, radius, amount);
        for(int i = 0; i < possibleSpawns.size(); i++)
        {
            Vec3 spawnPos = possibleSpawns.get(i);
            Entity entity = type.create(level);
            entity.setPos(spawnPos.x(), spawnPos.y(), spawnPos.z());
            entities.add(entity);
        }

        for (Entity entity : entities) {
            level.addFreshEntity(entity);
        }
    }

    public static Predicate<LivingEntity> isLivingEntity = new Predicate<LivingEntity>()
    {
        @Override
        public boolean test(LivingEntity livingEntity) {
            return true;
        }
    };

    public static Predicate<LivingEntity> isSculkHordeOrAllyEntity = new Predicate<LivingEntity>()
    {
        @Override
        public boolean test(LivingEntity livingEntity) {
            return isSculkLivingEntity.test(livingEntity) || isLivingEntityAllyToSculkHorde(livingEntity);
        }
    };

    public static Predicate<LivingEntity> isNotSculkHordeEntity = new Predicate<LivingEntity>()
    {
        @Override
        public boolean test(LivingEntity livingEntity) {
            return !isSculkLivingEntity.test(livingEntity);
        }
    };

    public static Predicate<LivingEntity> isHurtSculkHordeOrAllyEntity = new Predicate<LivingEntity>()
    {
        @Override
        public boolean test(LivingEntity livingEntity) {

            if(!isSculkLivingEntity.test(livingEntity) && !isLivingEntityAllyToSculkHorde(livingEntity))
            {
                return false;
            }

            return livingEntity.getHealth() < livingEntity.getMaxHealth();
        }
    };

    public static Predicate<LivingEntity> isHostileEntity = new Predicate<LivingEntity>()
    {
        @Override
        public boolean test(LivingEntity livingEntity) {
            return EntityAlgorithms.isLivingEntityHostile(livingEntity) && !EntityAlgorithms.isLivingEntityExplicitDenyTarget(livingEntity);
        }
    };

    /**
     * Gets all living entities in the given bounding box.
     * @param serverLevel The given world
     * @param boundingBox The given bounding box to search for a target
     * @return A list of valid targets
     */
    public static List<LivingEntity> getLivingEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox, Predicate<LivingEntity> predicate)
    {
        List<LivingEntity> livingEntitiesInRange = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, predicate);
        return livingEntitiesInRange;

    }

    /**
     * Gets all living entities in the given bounding box.
     * @param serverLevel The given world
     * @param boundingBox The given bounding box to search for a target
     * @return A list of valid targets
     */
    public static List<LivingEntity> getLivingEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox)
    {
        List<LivingEntity> livingEntitiesInRange = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, isLivingEntity);



        return livingEntitiesInRange;

    }

    public static List<Entity> getEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox, Predicate<Entity> predicate)
    {
        List<Entity> entities = serverLevel.getEntitiesOfClass(Entity.class, boundingBox, predicate);
        return entities;
    }

    public static List<Player> getPlayersInBoundingBox(ServerLevel serverLevel, AABB boundingBox, Predicate<Entity> predicate)
    {
        List<Player> entities = serverLevel.getEntitiesOfClass(Player.class, boundingBox, predicate);
        return entities;
    }

    public static List<LivingEntity> getHurtSculkHordeEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox)
    {
        List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, isHurtSculkHordeOrAllyEntity);
        return list;
    }

    public static List<LivingEntity> getSculkHordeEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox)
    {
        List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, isSculkLivingEntity);
        return list;
    }

    public static List<LivingEntity> getSculkHordeOrAllyEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox)
    {
        List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, isSculkHordeOrAllyEntity);
        return list;
    }

    public static List<LivingEntity> getNonSculkUnitsInBoundingBox(Level serverLevel, AABB boundingBox)
    {
        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, isNotSculkHordeEntity);
        return entities;
    }

    public static List<LivingEntity> getHostileEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox)
    {
        List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, isHostileEntity);
        return list;
    }

    public static List<LivingEntity> getEntitiesExceptOwnerInBoundingBox(LivingEntity entity, ServerLevel serverLevel, AABB boundingBox)
    {
        List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, new Predicate<LivingEntity>() {
            @Override
            public boolean test(LivingEntity livingEntity) {
                return entity != null && livingEntity.getUUID() != entity.getUUID();
            }
        });
        return list;
    }

    public static Optional<LivingEntity> getNearestHostile(ServerLevel serverLevel, BlockPos position, AABB boundingBox) {

        // Get the list of hostile entities within the bounding box
        List<LivingEntity> hostiles = getHostileEntitiesInBoundingBox(serverLevel, boundingBox);

        // Stream the list, calculate the distance to the position, and find the minimum
        return hostiles.stream()
                .min((entity1, entity2) -> {
                    double dist1 = entity1.distanceToSqr(position.getX(), position.getY(), position.getZ());
                    double dist2 = entity2.distanceToSqr(position.getX(), position.getY(), position.getZ());
                    return Double.compare(dist1, dist2);
                });
    }



    public static AABB createBoundingBoxCubeAtBlockPos(Vec3 origin, int squareLength)
    {
        double halfLength = squareLength/2;
        AABB boundingBox = new AABB(origin.x() - halfLength, origin.y() - halfLength, origin.z() - halfLength, origin.x() + halfLength, origin.y() + halfLength, origin.z() + halfLength);
        return boundingBox;
    }

    public static AABB createBoundingBoxRectableAtBlockPos(Vec3 origin, int width, int height, int length)
    {
        double halfWidth = width/2;
        double halfHeight = height/2;
        double halfLength = length/2;

        AABB boundingBox = new AABB(origin.x() - halfWidth, origin.y() - halfHeight, origin.z() - halfLength, origin.x() + halfWidth, origin.y() + halfHeight, origin.z() + halfLength);
        return boundingBox;
    }

    public static List<LivingEntity> getNonSculkEntitiesAtBlockPos(ServerLevel level, BlockPos origin, int squareLength)
    {
        AABB boundingBox = createBoundingBoxCubeAtBlockPos(origin.getCenter(), squareLength);
        List<LivingEntity> livingEntitiesInRange = level.getEntitiesOfClass(LivingEntity.class, boundingBox, new Predicate<LivingEntity>() {
            @Override
            public boolean test(LivingEntity livingEntity) {
                return !EntityAlgorithms.isSculkLivingEntity.test(livingEntity);
            }
        });
        return livingEntitiesInRange;
    }

    public static HitResult getHitScan(Entity entity, Vec3 origin, float xRot, float yRot, float maxDistance) {
        // Calculate direction vectors
        float cosYaw = Mth.cos(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float sinYaw = Mth.sin(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float cosPitch = -Mth.cos(-xRot * ((float) Math.PI / 180F));
        float sinPitch = Mth.sin(-xRot * ((float) Math.PI / 180F));
        float directionX = sinYaw * cosPitch;
        float directionZ = cosYaw * cosPitch;

        Vec3 endPosition = origin.add((double) directionX * maxDistance, (double) sinPitch * maxDistance, (double) directionZ * maxDistance);
        return entity.level().clip(new ClipContext(origin, endPosition, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
    }

    public static HitResult getHitScanAtTarget(Entity entity, Vec3 origin, Entity target, float maxDistance) {
        Vec3 startPosition = origin;
        Vec3 targetPosition = target.getEyePosition();

        // Calculate the difference in positions
        double deltaX = targetPosition.x - startPosition.x;
        double deltaY = targetPosition.y - startPosition.y;
        double deltaZ = targetPosition.z - startPosition.z;

        // Calculate the horizontal distance
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Calculate rotations
        float xRot = (float) -(Math.atan2(deltaY, horizontalDistance) * (180F / Math.PI));
        float yRot = (float) (Math.atan2(deltaZ, deltaX) * (180F / Math.PI)) - 90F;

        return getHitScan(entity, origin, xRot, yRot, maxDistance);
    }



    public static void announceToAllPlayers(ServerLevel level, Component message)
    {
        level.players().forEach((player) -> player.displayClientMessage(message, false));
    }

    public static double getHeightOffGround(Entity entity) {
        // Starting point of the ray (entity's position)
        Vec3 startPos = entity.position();

        // Ending point of the ray (directly below the entity)
        Vec3 endPos = startPos.subtract(0, entity.getY() + 256, 0); // 256 blocks down should be enough

        // Perform the ray trace
        HitResult hitResult = entity.level().clip(new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));

        // Calculate the distance from the entity to the hit point
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return startPos.y - hitResult.getLocation().y;
        } else {
            // If no block is hit, return a large number indicating the entity is very high off the ground
            return Double.MAX_VALUE;
        }
    }

    public static class DelayedHurtScheduler
    {
        private int ticksRemaining;
        private int delayInTicks;
        private Mob damageDealer;
        private boolean active = false;

        private double attackReach = 0.0;

        CustomMeleeAttackGoal parentAttackGoal;

        public DelayedHurtScheduler(CustomMeleeAttackGoal customMeleeAttackGoal, Mob damageDealer, int delayInTicks)
        {
            this.damageDealer = damageDealer;
            this.delayInTicks = delayInTicks;
            this.ticksRemaining = delayInTicks;
            this.parentAttackGoal = customMeleeAttackGoal;
        }

        private ISculkSmartEntity getDamageDealerAsISculkSmartEntity()
        {
            return (ISculkSmartEntity) damageDealer;
        }

        private Mob getDamageDealerAsMob()
        {
            return damageDealer;
        }

        public void tick()
        {
            if(!active)
            {
                return;
            }

            if(ticksRemaining > 0)
            {
                ticksRemaining--;
            }
            else
            {
                tryToDealDamage();
                reset();
            }
        }

        private boolean tryToDealDamage()
        {
            Optional<Entity> target = Optional.ofNullable(getDamageDealerAsMob().getTarget());


            if(damageDealer == null || !getDamageDealerAsMob().isAlive())
            {
                return false;
            }
            else if(target.isEmpty())
            {
                return false;
            }
            else if(!target.get().isAlive())
            {
                return false;
            }
            else if(getDamageDealerAsMob().distanceTo(target.get()) > attackReach)
            {
                return false;
            }

            getDamageDealerAsMob().swing(InteractionHand.MAIN_HAND);
            getDamageDealerAsMob().doHurtTarget(getDamageDealerAsMob().getTarget());
            parentAttackGoal.onTargetHurt(getDamageDealerAsMob().getTarget());
            return true;
        }


        public void trigger(double attackReach)
        {
            this.attackReach = attackReach;
            active = true;
        }

        public void reset()
        {
            ticksRemaining = delayInTicks;
            active = false;
        }
    }
}
