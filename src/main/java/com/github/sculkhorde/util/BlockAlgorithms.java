package com.github.sculkhorde.util;

import com.github.sculkhorde.common.structures.procedural.PlannedBlock;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

public class BlockAlgorithms {

    public static boolean cantBeDestroyedByStructures(Level level, BlockPos pos)
    {
        return !isDestroyableByStructures(level, pos);
    }
    public static boolean isDestroyableByStructures(Level level, BlockPos pos)
    {
        BlockState blockState = level.getBlockState(pos);

        // Explicity Deny
        if(isImmuneToWither(blockState) || isIndestructable(level, pos) || blockState.hasBlockEntity())
        {
            return false;
        }

        return isWeakBlock(blockState) || hasFastDestroySpeed(level, pos) || isMineableWithIronTools(blockState) || isInfestedBlock(blockState);
    }

    public static boolean isInfestedBlock(BlockState blockState)
    {
        return blockState.is(ModBlocks.BlockTags.INFESTED_BLOCK);
    }

    public static boolean isImmuneToWither(BlockState blockState)
    {
        return blockState.is(BlockTags.WITHER_IMMUNE);
    }

    public static boolean isIndestructable(Level level, BlockPos pos)
    {
        BlockState blockState = level.getBlockState(pos);
        return blockState.getDestroySpeed(level, pos) < 0;
    }

    public static boolean isWeakBlock(BlockState blockState)
    {
        return isReplaceable(blockState) || isAir(blockState);
    }

    public static boolean isAir(BlockState blockState)
    {
        return blockState.isAir() || blockState.is(Blocks.AIR) || blockState.is(Blocks.CAVE_AIR);
    }

    public static boolean doesNotNeedToolForDrops(BlockState blockState)
    {
        return !blockState.requiresCorrectToolForDrops();
    }
    public static boolean hasFastDestroySpeed(Level level, BlockPos pos)
    {
        BlockState blockState = level.getBlockState(pos);
        return blockState.getDestroySpeed(level, pos) <= 3.0F && blockState.getDestroySpeed(level, pos) >= 0;
    }
    public static boolean isMineableWithDiamondTools(BlockState blockState)
    {
        return blockState.is(BlockTags.NEEDS_DIAMOND_TOOL) || isMineableWithIronTools(blockState);
    }
    public static boolean requiresDiamondTools(BlockState blockState)
    {
        return blockState.is(BlockTags.NEEDS_DIAMOND_TOOL);
    }
    public static boolean isMineableWithIronTools(BlockState blockState)
    {
        return blockState.is(BlockTags.NEEDS_IRON_TOOL) || isMineableWithStoneTools(blockState);
    }
    public static boolean requiresIronTools(BlockState blockState)
    {
        return blockState.is(BlockTags.NEEDS_IRON_TOOL);
    }
    public static boolean isMineableWithStoneTools(BlockState blockState)
    {
        return blockState.is(BlockTags.NEEDS_STONE_TOOL) || doesNotNeedToolForDrops(blockState);
    }

    public static boolean isReplaceable(BlockState blockState)
    {
        return isReplaceableByWater(blockState) || blockState.canBeReplaced();
    }

    public static boolean isReplaceableByWater(BlockState blockState)
    {
        return blockState.canBeReplaced(Fluids.WATER);
    }

    public static boolean isWaterLoggable(BlockState state)
    {
        return state.getProperties().contains(BlockStateProperties.WATERLOGGED);
    }

    public static float getSudoRNGFromPosition(BlockPos position, int min, int max)
    {
        int range = max - min;
        long seed = (long) position.getX() * position.getY() * position.getZ() + (position.getX() + position.getY() + position.getZ());
        int rng = (int) (seed % (range + 1.0)); //Get output between 0 and range
        rng += min;
        return rng;
    }

    /**
     * Will return an array list that represents a 3x3x3 cube of all block
     * positions with the origin being the centroid. Does not include origin
     * in this list.
     * @return A list of Neighbors in a cube
     */
    public static ArrayList<BlockPos> getNeighborsCube(BlockPos pos, boolean includeOrigin) {
        ArrayList<BlockPos> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (i == 0 && j == 0 && k == 0 && !includeOrigin) {
                        continue;
                    }
                    neighbors.add(pos.offset(i, j, k));
                }
            }
        }
        return neighbors;
    }


    /**
     * Will return an array list that represents the neighbors that are directly
     * touching any face of the a block
     * @param origin The target position
     * @return A list of all adjacent neighbors
     */
    public static ArrayList<BlockPos> getAdjacentNeighbors(BlockPos origin)
    {
        ArrayList<BlockPos> list = new ArrayList<>();
        list.addAll(getNeighborsXZPlane(origin, false));
        list.addAll(getNeighborsXZPlane(origin.above(), true));
        list.addAll(getNeighborsXZPlane(origin.below(), true));
        return list;
    }

    /**
     * Will return an array list representing a 2D layer
     * @param origin The origin position
     * @return A list of block positions
     */
    public static ArrayList<BlockPos> getNeighborsXZPlane(BlockPos origin, boolean includeOrigin)
    {
        ArrayList<BlockPos> list = new ArrayList<>();
        list.add(origin.north());
        list.add(origin.north().east());
        list.add(origin.north().west());
        list.add(origin.east());
        list.add(origin.south());
        list.add(origin.south().east());
        list.add(origin.south().west());
        list.add(origin.west());
        if(includeOrigin) list.add(origin);

        return list;
    }

    public static float getBlockDistance(BlockPos pos1, BlockPos pos2)
    {
        return (float) Math.sqrt( Math.pow(pos2.getX() - pos1.getX(), 2) + Math.pow(pos2.getY() - pos1.getY(), 2) + Math.pow(pos2.getZ() - pos1.getZ(), 2));
    }

    public static float getBlockDistanceXZ(BlockPos pos1, BlockPos pos2)
    {
        return (float) Math.sqrt( Math.pow(pos2.getX() - pos1.getX(), 2) + Math.pow(pos2.getZ() - pos1.getZ(), 2));
    }

    public static BlockPos getCentroid(ArrayList<BlockPos> positions)
    {
        if(positions.isEmpty()) return new BlockPos(0, 0, 0); //Return origin if no positions (should never happen

        int x = 0;
        int y = 0;
        int z = 0;
        for(BlockPos pos : positions)
        {
            x += pos.getX();
            y += pos.getY();
            z += pos.getZ();
        }
        x /= positions.size();
        y /= positions.size();
        z /= positions.size();
        return new BlockPos(x, y, z);
    }

    /**
     * Returns an ArrayList of BlockPos that represent a cube
     * @param origin the center BlockPos of the cube
     * @param length the length of all sides of the cube
     * @param includeOrigin if true, the origin position will be included in the final output
     * @return an ArrayList of BlockPos that represent a cube
     */
    public static ArrayList<BlockPos> getBlockPosInCube(BlockPos origin, int length, boolean includeOrigin) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        boolean shouldWeAddThisPosition;

        //Origin position
        int center_x = origin.getX();
        int center_y = origin.getY();
        int center_z = origin.getZ();

        //Initial position is in corner
        int start_pos_x = center_x - (length/2);
        int start_pos_y = center_y - (length/2);
        int start_pos_z = center_z - (length/2);

        //Iterate over y positions
        for(int y_offset = 0; y_offset < length; y_offset++)
        {
            //Iterate over z positions
            int current_pos_y = start_pos_y + y_offset;
            for(int z_offset = 0; z_offset < length; z_offset++)
            {
                //Iterate over x positions
                int current_pos_z = start_pos_z + z_offset;
                for(int x_offset = 0; x_offset < length; x_offset++)
                {
                    int current_pos_x = start_pos_x + x_offset;
                    shouldWeAddThisPosition = true;

                    BlockPos targetPos = new BlockPos(current_pos_x, current_pos_y, current_pos_z);

                    //If we are not including origin and this is the origin, do not include
                    if(!includeOrigin && targetPos.getX() == origin.getX() && targetPos.getY() == origin.getY() && targetPos.getZ() == origin.getZ())
                        shouldWeAddThisPosition = false;

                    //If not debugging, function as normal
                    if(shouldWeAddThisPosition) positions.add(targetPos);
                }
            }
        }

        return positions;
    }

    /**
     * Gets all blocks in a circle and retuns it in a list
     * NOTE: Something is wrong with this algorithm, the size is too small
     * @param radius The radius
     * @param includeOrigin Whether to include the origin
     * @return A list of all the block positions
     */
    public static ArrayList<BlockPos> getBlockPosInCircle(BlockPos origin, int radius, boolean includeOrigin)
    {
        if(radius <= 0) { throw new IllegalArgumentException("Radius must be greater than 0. Radius given was " + radius + ".");}

        ArrayList<BlockPos> positions = new ArrayList<>();
        boolean shouldWeAddThisPosition;

        //Origin position
        int center_x = origin.getX();
        int center_y = origin.getY();
        int center_z = origin.getZ();

        //Initial position is in corner
        int start_pos_x = center_x - radius - 1;
        int start_pos_y = center_y - radius - 1;
        int start_pos_z = center_z - radius - 1;

        //Iterate over y positions
        for(int y_offset = 0; y_offset < (radius*2) + 2; y_offset++)
        {
            //Iterate over z positions
            int current_pos_y = start_pos_y + y_offset;
            for(int z_offset = 0; z_offset < (radius*2) + 2; z_offset++)
            {
                //Iterate over x positions
                int current_pos_z = start_pos_z + z_offset;
                for(int x_offset = 0; x_offset < (radius*2) + 2; x_offset++)
                {
                    int current_pos_x = start_pos_x + x_offset;
                    shouldWeAddThisPosition = true;

                    BlockPos targetPos = new BlockPos(current_pos_x, current_pos_y, current_pos_z);

                    //If distance between center and current block is less than radius, it is in the circle
                    double distance = getBlockDistance(origin, targetPos);

                    //If outside of radius, do not include
                    if(distance > radius)
                        shouldWeAddThisPosition = false;
                    //If we are not including origin and this is the origin, do not include
                    if(!includeOrigin && targetPos.getX() == origin.getX() && targetPos.getY() == origin.getY() && targetPos.getZ() == origin.getZ())
                        shouldWeAddThisPosition = false;

                    //If not debugging, function as normal
                    if(shouldWeAddThisPosition) positions.add(targetPos);
                }
            }
        }
        return positions;
    }


    /**
     * Finds the location of the nearest block given a block state predicate.
     * @param level The world
     * @param origin The origin of the search location
     * @param predicate The predicate that determines if a block is the one were searching for
     * @param distance The search distance
     * @return The position of the block
     */
    public static ArrayList<BlockPos> getBlocksInArea(ServerLevel level, BlockPos origin, Predicate<BlockState> predicate, int distance)
    {
        ArrayList<BlockPos> list = new ArrayList<>();

        BlockPos blockPos = origin;

        // Define the bounds of the cube
        int minX = blockPos.getX() - distance;
        int minY = blockPos.getY() - distance;
        int minZ = blockPos.getZ() - distance;
        int maxX = blockPos.getX() + distance;
        int maxY = blockPos.getY() + distance;
        int maxZ = blockPos.getZ() + distance;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutableBlockPos.set(x, y, z);
                    if (level.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                        continue;
                    }
                    if (predicate.test(level.getBlockState(mutableBlockPos))) {
                        list.add(mutableBlockPos.immutable());
                    }
                }
            }
        }
        return list;
    }


    /**
     * Finds the location of the nearest block given a block state predicate.
     * @param level The world
     * @param origin The origin of the search location
     * @param predicate The predicate that determines if a block is the one were searching for
     * @param distance The search distance
     * @return The position of the block
     */
    public static ArrayList<BlockPos> getBlocksInAreaWithBlockPosPredicate(ServerLevel level, BlockPos origin, Predicate<BlockPos> predicate, int distance)
    {
        ArrayList<BlockPos> list = new ArrayList<>();

        BlockPos blockPos = origin;

        // Define the bounds of the cube
        int minX = blockPos.getX() - distance;
        int minY = blockPos.getY() - distance;
        int minZ = blockPos.getZ() - distance;
        int maxX = blockPos.getX() + distance;
        int maxY = blockPos.getY() + distance;
        int maxZ = blockPos.getZ() + distance;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutableBlockPos.set(x, y, z);
                    if (level.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                        continue;
                    }
                    if (predicate.test(mutableBlockPos)) {
                        list.add(mutableBlockPos.immutable());
                    }
                }
            }
        }
        return list;
    }


    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param level The world
     * @param origin The origin of the search location
     * @param predicate The predicate that determines if a block is the one were searching for
     * @param distance The search distance
     * @return The position of the block
     */
    public static Optional<BlockPos> findBlockInCube(ServerLevel level, BlockPos origin, Predicate<BlockState> predicate, int distance) {
        BlockPos blockPos = origin;

        // Define the bounds of the cube
        int minX = blockPos.getX() - distance;
        int minY = blockPos.getY() - distance;
        int minZ = blockPos.getZ() - distance;
        int maxX = blockPos.getX() + distance;
        int maxY = blockPos.getY() + distance;
        int maxZ = blockPos.getZ() + distance;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutableBlockPos.set(x, y, z);
                    if (level.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                        continue;
                    }
                    if (predicate.test(level.getBlockState(mutableBlockPos))) {
                        return Optional.of(mutableBlockPos.immutable());
                    }
                }
            }
        }
        return Optional.empty();
    }


    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param level The world
     * @param origin The origin of the search location
     * @param predicate The predicate that determines if a block is the one were searching for
     * @param distance The search distance
     * @return The position of the block
     */
    public static Optional<BlockPos> findBlockInCubeBlockPosPredicate(ServerLevel level, BlockPos origin, Predicate<BlockPos> predicate, int distance) {
        BlockPos blockPos = origin;

        // Define the bounds of the cube
        int minX = blockPos.getX() - distance;
        int minY = blockPos.getY() - distance;
        int minZ = blockPos.getZ() - distance;
        int maxX = blockPos.getX() + distance;
        int maxY = blockPos.getY() + distance;
        int maxZ = blockPos.getZ() + distance;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutableBlockPos.set(x, y, z);
                    if (level.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                        continue;
                    }
                    if (predicate.test(mutableBlockPos)) {
                        return Optional.of(mutableBlockPos.immutable());
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks immediate blocks to see if any of them are air
     * @param serverWorld The world
     * @param targetPos The position to check
     * @return true if any air found, false otherwise
     */
    public static boolean isExposedToAir(ServerLevel serverWorld, BlockPos targetPos)
    {
        ArrayList<BlockPos> list = getAdjacentNeighbors(targetPos);

        for(BlockPos position : list)
        {
            if(BlockAlgorithms.isNotSolid(serverWorld, position))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks immediate blocks to see if any of them are infestation ward blocks
     * @param serverWorld The world
     * @param targetPos The position to check
     * @return true if any air found, false otherwise
     */
    public static boolean isExposedToInfestationWardBlock(ServerLevel serverWorld, BlockPos targetPos)
    {
        ArrayList<BlockPos> list = getAdjacentNeighbors(targetPos);

        for(BlockPos position : list)
        {
            if(serverWorld.getBlockState(position).is(ModBlocks.INFESTATION_WARD_BLOCK.get()))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * A Jank solution to spawning flora. Given a random chance, spawn flora.
     * @param targetPos The BlockPos to spawn it at
     * @param world The world to spawn it in.
     */
    public static void tryPlaceSculkFlora(BlockPos targetPos, ServerLevel world)
    {
        BlockState blockState = SculkHorde.randomSculkFlora.getRandomEntry().defaultBlockState();

        BlockPos blockBelow = targetPos.below();
        BlockState belowBlockState = world.getBlockState(blockBelow);

        boolean canBlockBeWaterLogged = blockState.hasProperty(BlockStateProperties.WATERLOGGED);
        FluidState fluidStateAtTargetPos = world.getFluidState(targetPos);

        if(world.getBlockState(targetPos.below()).is(ModBlocks.DISEASED_KELP_BLOCK.get()))
        {
            return;
        }

        // If block is water loggable and in water and can survive, then place
        if(canBlockBeWaterLogged && fluidStateAtTargetPos.getType() == Fluids.WATER && blockState.canSurvive(world, targetPos))
        {
            blockState.setValue(BlockStateProperties.WATERLOGGED, true); // This line doesn't work to set water logged
            world.setBlockAndUpdate(targetPos, blockState);
            world.setBlockAndUpdate(targetPos, world.getBlockState(targetPos).setValue(BlockStateProperties.WATERLOGGED, true)); // This one does though
        }

        //If block below target is valid and the target can be replaced by water and target is not waterloggable
        else if(belowBlockState.isFaceSturdy(world, blockBelow, Direction.UP) && blockState.canSurvive(world, targetPos) && (world.getBlockState(targetPos).isAir() || world.getBlockState(targetPos).is(Blocks.SNOW)))
        {
            world.setBlockAndUpdate(targetPos, blockState);
        }

    }

    /**
     * Generates a list of PlannedBlocks in the shape of a 2D circle.
     *
     * @param centerPos The center of the circle
     * @param diameter The diameter of the circle
     * @param world The world where the blocks will be placed
     * @param plannedBlock The type of block that will be placed in the circle
     * @return A list of PlannedBlocks in the shape of a 2D circle
     */
    public static ArrayList<PlannedBlock> generate2DCirclePlan(BlockPos centerPos, int diameter, ServerLevel world, BlockState plannedBlock) {
        ArrayList<PlannedBlock> circleBlocks = new ArrayList<>();
        // The radius of the circle is half the diameter
        int radius = diameter / 2;
        // Iterate through all blocks in a square that surrounds the circle
        for (int x = centerPos.getX() - radius; x <= centerPos.getX() + radius; x++) {
            for (int z = centerPos.getZ() - radius; z <= centerPos.getZ() + radius; z++) {
                // Calculate the distance between the current block and the center of the circle
                double distance = Math.sqrt((x - centerPos.getX()) * (x - centerPos.getX()) + (z - centerPos.getZ()) * (z - centerPos.getZ()));
                // If the distance is less than or equal to the radius, add a PlannedBlock for that block
                if (distance <= radius) {
                    BlockPos pos = new BlockPos(x, centerPos.getY(), z);
                    circleBlocks.add(new PlannedBlock(world, plannedBlock, pos));
                }
            }
        }
        return circleBlocks;
    }


    public static ArrayList<BlockPos> getPointsOnCircumference(BlockPos origin, int numPoints, int radius) {
        ArrayList<BlockPos> points = new ArrayList<>();
        double angleIncrement = (2 * Math.PI) / numPoints;
        for (int i = 0; i < numPoints; i++) {
            double angle = i * angleIncrement;
            int x = (int) (origin.getX() + radius * Math.cos(angle));
            int z = (int) (origin.getZ() + radius * Math.sin(angle));
            points.add(new BlockPos(x, origin.getY(), z));
        }
        return points;
    }

    public static int convertBlockLengthToChunkLength(int blockLength)
    {
        // Always make sure to round up in the calculation
        return (int) Math.ceil((double) blockLength / 16);
    }

    public static ArrayList<Vec3> getPointsOnCircumferenceVec3(Vec3 origin, int radiusOfCircle, int numberOfPositionsToCreate)
    {
        ArrayList<Vec3> positions = new ArrayList<Vec3>();
        float angleIncrement = (float) (2 * Math.PI / numberOfPositionsToCreate);
        for(int i = 0; i < numberOfPositionsToCreate; i++)
        {
            float angle = i * angleIncrement;
            double x = radiusOfCircle * Math.cos(angle);
            double z = radiusOfCircle * Math.sin(angle);
            positions.add(new Vec3(origin.x() + x, origin.y(), origin.z() + z));
        }
        return positions;
    }

    public static boolean isAreaFlat(ServerLevel level, BlockPos centerPos, int radius) {
        int totalBlocks = 0;
        int flatBlocks = 0;

        int startX = centerPos.getX() - radius;
        int startZ = centerPos.getZ() - radius;

        for (int x = startX; x <= centerPos.getX() + radius; x++) {
            for (int z = startZ; z <= centerPos.getZ() + radius; z++) {
                BlockPos currentPos = new BlockPos(x, centerPos.getY(), z);
                if (level.isLoaded(currentPos))
                {
                    totalBlocks++;
                    if (isBlockFlat(level, currentPos)) {
                        flatBlocks++;
                    }
                }
            }
        }

        // Calculate the flatness ratio
        double flatnessRatio = (double) flatBlocks / totalBlocks;
        // You can adjust the threshold value as needed
        double flatnessThreshold = 0.5;

        return flatnessRatio >= flatnessThreshold;
    }

    public static boolean isSolid(ServerLevel level, BlockPos pos) {
        return !isNotSolid(level, pos);
    }

    public static boolean isNotSolid(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        boolean canNotOcclude = !state.canOcclude();
        boolean isNotSolid = !state.isSolid();
        boolean isAir = state.isAir();
        boolean isNotSolidRender = !state.isSolidRender(level, pos);
        return canNotOcclude || isNotSolid || isAir || isNotSolidRender;
    }

    private static boolean isBlockFlat(ServerLevel level, BlockPos pos) {

        // If block is not solid, but block below is, then it is flat
        if(!isSolid(level, pos) && isSolid(level, pos.below()))
        {
            return true;
        }
        // If block is solid, with space above, then it is flat
        else if(isSolid(level, pos) && !isSolid(level, pos.above()))
        {
            return true;
        }
        // If block and block above are solid, but above that isnt, then it is flat.
        else if(isSolid(level, pos) && isSolid(level, pos.above()) && !isSolid(level, pos.above().above()))
        {
            return true;
        }

        return false;
    }


    public static BlockPos getGroundBlockPos(Level level, BlockPos origin, int startHeight)
    {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(origin.getX(), startHeight, origin.getZ());
        while(mutable.getY() > level.getMinBuildHeight() && level.isEmptyBlock(mutable))
        {
            mutable.move(Direction.DOWN);
        }
        return mutable;
    }

    public static boolean areTheseDimensionsEqual(ResourceKey<Level> dimension1, ResourceKey<Level> dimension2)
    {
        if(dimension1 == null || dimension2 == null)
        {
            return false;
        }
        return dimension1.location().equals(dimension2.location());
    }

    public static boolean areTheseDimensionsEqual(ServerLevel dimension1, ServerLevel dimension2)
    {
        if(dimension1 == null || dimension2 == null)
        {
            return false;
        }
        return dimension1.dimension().location().equals(dimension2.dimension().location());
    }

    public static boolean isNearFluid(ServerLevel level, BlockPos origin, int range)
    {
        for(BlockPos pos : getBlockPosInCube(origin, range, true))
        {
            if(level.getFluidState(pos).getType() != Fluids.EMPTY)
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isNearNonWaterFluid(ServerLevel level, BlockPos origin, int range)
    {
        for(BlockPos pos : getBlockPosInCube(origin, range, true))
        {
            if(level.getFluidState(pos).getType() != Fluids.EMPTY && !level.getFluidState(pos).is(FluidTags.WATER))
            {
                return true;
            }
        }

        return false;
    }


}
