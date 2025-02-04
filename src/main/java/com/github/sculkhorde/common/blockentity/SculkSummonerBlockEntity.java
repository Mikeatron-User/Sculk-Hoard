package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.SculkSummonerBlock;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.systems.BlockInfestationSystem;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SculkSummonerBlockEntity extends BlockEntity implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem, GeoBlockEntity
{
    AABB searchArea;
    //ACTIVATION_DISTANCE - The distance at which this is able to detect mobs.
    private final int ACTIVATION_DISTANCE = 32;
    //possibleLivingEntityTargets - A list of nearby targets which are worth infecting.
    private List<LivingEntity> possibleLivingEntityTargets;
    //possibleAggressorTargets - A list of nearby targets which should be considered hostile.
    private List<LivingEntity> possibleAggressorTargets;
    private long lastGameTimeOfVibrationRecieve = 0;
    private final int MAX_SPAWNED_ENTITIES = 4;
    ReinforcementRequest request;
    private final TargetParameters hostileTargetParameters = new TargetParameters().enableTargetHostiles().enableTargetInfected();
    private final TargetParameters infectableTargetParameters = new TargetParameters().enableTargetPassives();

    // Vibration Code
    private final VibrationSystem.User vibrationUser = new VibrationUser(this);
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

    /**
     * The Constructor that takes in properties
     */
    public SculkSummonerBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.SCULK_SUMMONER_BLOCK_ENTITY.get(), blockPos, blockState);
        searchArea = EntityAlgorithms.getSearchAreaRectangle(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), ACTIVATION_DISTANCE, 5, ACTIVATION_DISTANCE);
        possibleLivingEntityTargets = new ArrayList<>();
        possibleAggressorTargets = new ArrayList<>();
    }

    /** ~~~~~~~~ Accessors ~~~~~~~~ **/

    private boolean isVibrationCooldownOver()
    {
        return level.getGameTime() - lastGameTimeOfVibrationRecieve > TickUnits.convertSecondsToTicks(15);
    }

    private boolean isBlockStateVibrationCooldownTrue()
    {
        return getBlockState().getValue(SculkSummonerBlock.VIBRATION_COOLDOWN);
    }

    private void setVibrationCooldown(boolean value)
    {
        assert level != null;
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SculkSummonerBlock.VIBRATION_COOLDOWN, value));
    }

    /** ~~~~~~~~ Modifiers ~~~~~~~~  **/


    /** ~~~~~~~~ Boolean ~~~~~~~~  **/


    /**
     * Check if all entries are alive
     */
    private boolean areAllReinforcementsDead()
    {
        if(request == null)
        {
            return true;
        }

        // iterate through each request, and for each one, iterate through spawnedEntities and check if they are alive
        for( LivingEntity entity : request.spawnedEntities )
        {
            if( entity == null )
            {
                continue;
            }

            if( entity.isAlive() )
            {
                return false;
            }
        }
        return true;
    }

    private boolean areAnyTargetsNearBy(BlockPos blockPos, SculkSummonerBlockEntity blockEntity)
    {
        //Create bounding box to detect targets
        blockEntity.searchArea = EntityAlgorithms.createBoundingBoxRectableAtBlockPos(blockPos.getCenter(), blockEntity.ACTIVATION_DISTANCE, 10, blockEntity.ACTIVATION_DISTANCE);

        //Get targets inside bounding box.
        blockEntity.possibleAggressorTargets =
                blockEntity.level.getEntitiesOfClass(
                        LivingEntity.class,
                        blockEntity.searchArea,
                        blockEntity.hostileTargetParameters.isPossibleNewTargetValid);

        //Get targets inside bounding box.
        blockEntity.possibleLivingEntityTargets =
                blockEntity.level.getEntitiesOfClass(
                        LivingEntity.class,
                        blockEntity.searchArea,
                        blockEntity.infectableTargetParameters.isPossibleNewTargetValid);

        if(!blockEntity.possibleAggressorTargets.isEmpty() || !blockEntity.possibleLivingEntityTargets.isEmpty())
        {
            return true;
        }

        return false;

    }

    public void requestReinforcementsFromGravemind()
    {
        boolean isSummonerWaterLogged = getBlockState().getValue(BlockStateProperties.WATERLOGGED);

        ((ServerLevel)level).sendParticles(ParticleTypes.SCULK_SOUL, this.getBlockPos().getX() + 0.5D, this.getBlockPos().getY() + 1.15D, this.getBlockPos().getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        ((ServerLevel)level).playSound(null, this.getBlockPos(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
        //Choose spawn positions
        ArrayList<BlockPos> possibleSpawnPositions = this.getSpawnPositionsInCube((ServerLevel) level, this.getBlockPos(), 5, this.MAX_SPAWNED_ENTITIES, isSummonerWaterLogged);

        BlockPos[] finalizedSpawnPositions = new BlockPos[this.MAX_SPAWNED_ENTITIES];

        //Create MAX_SPAWNED_ENTITIES amount of Reinforcement Requests
        for (int iterations = 0; iterations < possibleSpawnPositions.size(); iterations++)
        {
            finalizedSpawnPositions[iterations] = possibleSpawnPositions.get(iterations);
        }

        //If the array is empty, just spawn above block
        if (possibleSpawnPositions.isEmpty()) {
            finalizedSpawnPositions[0] = this.getBlockPos().above();
        }



        //Give gravemind context to our request to make more informed situations
        this.request = new ReinforcementRequest((ServerLevel) getLevel(), finalizedSpawnPositions);
        this.request.sender = ReinforcementRequest.senderType.Summoner;

        // Better Control if aquatic mobs spawn
        if(isSummonerWaterLogged)
        {
            request.deniedStrategicValues.add(EntityFactoryEntry.StrategicValues.EffectiveOnGround);
        }
        else
        {
            request.deniedStrategicValues.add(EntityFactoryEntry.StrategicValues.Aquatic);
        }

        if (!this.possibleAggressorTargets.isEmpty()) {
            this.request.is_aggressor_nearby = true;
        }

        if (!this.possibleLivingEntityTargets.isEmpty()) {
            this.request.is_non_sculk_mob_nearby = true;
        }

        //If there is some sort of enemy nearby, request reinforcement
        if (this.request.is_non_sculk_mob_nearby || this.request.is_aggressor_nearby) {
            //Request reinforcement from entity factory (this request gets approved or denied by gravemind)
            SculkHorde.entityFactory.createReinforcementRequestFromSummoner(level, this.getBlockPos(), false, this.request);
        }
    }


    /** ~~~~~~~~ Events ~~~~~~~~ **/
    public static void receiveVibrationTick(Level level, BlockPos vibrationSource, BlockState blockState, SculkSummonerBlockEntity blockEntity, Entity entity)
    {
        if(level == null || level.isClientSide)
        {
            return;
        }

        blockEntity.setVibrationCooldown(true);
        blockEntity.lastGameTimeOfVibrationRecieve = level.getGameTime();

        if(!blockEntity.areAllReinforcementsDead())
        {
            return;
        }

        if(blockEntity.areAnyTargetsNearBy(blockEntity.worldPosition, blockEntity))
        {
            blockEntity.requestReinforcementsFromGravemind();
            level.levelEvent(3007, blockEntity.worldPosition, 0);
            level.gameEvent(GameEvent.SHRIEK, blockEntity.worldPosition, GameEvent.Context.of(entity));
        }
    }

    public static void tickOnCoolDown(Level level, BlockPos blockPos, BlockState blockState, SculkSummonerBlockEntity blockEntity) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        if(blockEntity.isVibrationCooldownOver())
        {
            blockEntity.setVibrationCooldown(false);
        }
    }


    /**
     * Gets a list of all possible spawns, chooses a specified amount of them.
     * @param worldIn The World
     * @param origin The Origin Position
     * @param length The Length of the cube
     * @param amountOfPositions The amount of positions to get
     * @return A list of the spawn positions
     */
    public ArrayList<BlockPos> getSpawnPositionsInCube(ServerLevel worldIn, BlockPos origin, int length, int amountOfPositions, boolean requireWater)
    {
        //TODO Can potentially be optimized by not getting all the possible positions
        ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, length, requireWater);
        ArrayList<BlockPos> finalList = new ArrayList<>();
        Random rng = new Random();
        for(int count = 0; count < amountOfPositions && !listOfPossibleSpawns.isEmpty(); count++)
        {
            int randomIndex = rng.nextInt(listOfPossibleSpawns.size());
            //Get random position between 0 and size of list
            finalList.add(listOfPossibleSpawns.get(randomIndex));
            listOfPossibleSpawns.remove(randomIndex);
        }
        return finalList;
    }

    /**
     * Returns true if the block below is a sculk block,
     * and if the two blocks above it are free.
     * @param worldIn The World
     * @param pos The Position to spawn the entity
     * @return True/False
     */
    public boolean isValidSpawnPosition(ServerLevel worldIn, BlockPos pos, boolean requireWater)
    {
        boolean isBlockBelowCurable = BlockInfestationSystem.isCurable(worldIn, pos.below());
        boolean isBaseBlockReplaceable = worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER);
        boolean isBaseBlockWater = worldIn.getFluidState(pos).is(Fluids.WATER);
        boolean isBlockAboveReplaceable = worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

        if(!isBaseBlockWater && requireWater)
        {
            return false;
        }

        return isBlockBelowCurable && isBaseBlockReplaceable && isBlockAboveReplaceable;

    }

    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param pDistance The search distance
     * @return The position of the block
     */
    public ArrayList<BlockPos> getSpawnPositions(ServerLevel worldIn, BlockPos origin, double pDistance, boolean requireWater)
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
                        if (origin.closerThan(temp, pDistance) && isValidSpawnPosition(worldIn, temp, requireWater))
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

    // Save & Load Code

    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains("listener", 10)) {
            VibrationSystem.Data.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("listener"))).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((data) -> {
                this.vibrationData = data;
            });
        }

    }

    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((p_222871_) -> {
            nbt.put("listener", p_222871_);
        });
    }

    /** ~~~~~~~~ Vibration Events ~~~~~~~~  **/
    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    /**
     * The listener for the sculk summoner block entity.
     */
    class VibrationUser implements VibrationSystem.User
    {
        private static final int LISTENER_RADIUS = 24;
        private final PositionSource positionSource = new BlockPositionSource(SculkSummonerBlockEntity.this.worldPosition);
        private SculkSummonerBlockEntity summoner;

        public VibrationUser(SculkSummonerBlockEntity summoner) {
            this.summoner = summoner;
        }


        public int getListenerRadius() {
            return LISTENER_RADIUS;
        }

        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.SHRIEKER_CAN_LISTEN;
        }

        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event, GameEvent.Context context) {
                return !isBlockStateVibrationCooldownTrue() && !SculkHorde.populationHandler.isPopulationAtMax();
        }

        public void onReceiveVibration(ServerLevel level, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity1, float power)
        {
            receiveVibrationTick(level, blockPos, getBlockState(), summoner, entity);
        }

        public void onDataChanged()
        {
            setChanged();
        }

        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }

    /** ~~~~~~~~ Animation Events ~~~~~~~~  **/

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // We statically instantiate our RawAnimations for efficiency, consistency, and error-proofing
    private static final RawAnimation SCULK_SUMMONER_COOLDOWN_ANIMATION = RawAnimation.begin().thenPlayAndHold("cooldown");
    private static final RawAnimation SCULK_SUMMONER_READY_ANIMATION = RawAnimation.begin().thenPlay("powerup").thenLoop("idle");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state ->
        {
                BlockState blockState = state.getAnimatable().getLevel().getBlockState(state.getAnimatable().worldPosition);
                if(blockState.is(ModBlocks.SCULK_SUMMONER_BLOCK.get()))
                {
                    if(state.getAnimatable().getLevel().getBlockState(state.getAnimatable().worldPosition).getValue(SculkSummonerBlock.VIBRATION_COOLDOWN))
                    {
                        return state.setAndContinue(SCULK_SUMMONER_COOLDOWN_ANIMATION);
                    }
                }
                return state.setAndContinue(SCULK_SUMMONER_READY_ANIMATION);
        }
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}
