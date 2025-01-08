package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SouliteBudBlock extends AmethystBlock implements SimpleWaterloggedBlock {

   public static final IntegerProperty AGE = BlockStateProperties.AGE_2;

   /**
    * HARDNESS determines how difficult a block is to break<br>
    * 0.6f = dirt<br>
    * 1.5f = stone<br>
    * 2f = log<br>
    * 3f = iron ore<br>
    * 50f = obsidian
    */
   public static float HARDNESS = 2f;

   /**
    * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
    * 0.5f = dirt<br>
    * 2f = wood<br>
    * 6f = cobblestone<br>
    * 1,200f = obsidian
    */
   public static float BLAST_RESISTANCE = 2f;

   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   protected final VoxelShape northAabb;
   protected final VoxelShape southAabb;
   protected final VoxelShape eastAabb;
   protected final VoxelShape westAabb;
   protected final VoxelShape upAabb;
   protected final VoxelShape downAabb;

   public SouliteBudBlock(int p_152015_, int p_152016_, Properties p_152017_) {
      super(p_152017_);
      this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.UP).setValue(AGE, 0));
      this.upAabb = Block.box((double)p_152016_, 0.0D, (double)p_152016_, (double)(16 - p_152016_), (double)p_152015_, (double)(16 - p_152016_));
      this.downAabb = Block.box((double)p_152016_, (double)(16 - p_152015_), (double)p_152016_, (double)(16 - p_152016_), 16.0D, (double)(16 - p_152016_));
      this.northAabb = Block.box((double)p_152016_, (double)p_152016_, (double)(16 - p_152015_), (double)(16 - p_152016_), (double)(16 - p_152016_), 16.0D);
      this.southAabb = Block.box((double)p_152016_, (double)p_152016_, 0.0D, (double)(16 - p_152016_), (double)(16 - p_152016_), (double)p_152015_);
      this.eastAabb = Block.box(0.0D, (double)p_152016_, (double)p_152016_, (double)p_152015_, (double)(16 - p_152016_), (double)(16 - p_152016_));
      this.westAabb = Block.box((double)(16 - p_152015_), (double)p_152016_, (double)p_152016_, 16.0D, (double)(16 - p_152016_), (double)(16 - p_152016_));
   }

   public SouliteBudBlock() {
      this(7 ,3, getProperties());
   }


   /**
    * Determines the properties of a block.<br>
    * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
    * @return The Properties of the block
    */
   public static Properties getProperties()
   {
      Properties prop = Properties.copy(Blocks.STONE)
              .mapColor(MapColor.COLOR_CYAN)
              .strength(HARDNESS, BLAST_RESISTANCE)//Hardness & Resistance
              .sound(SoundType.HONEY_BLOCK)
              .destroyTime(5f)
              .sound(SoundType.AMETHYST)
              .noOcclusion();
      return prop;
   }

   protected IntegerProperty getAgeProperty() {
      return AGE;
   }
   protected Direction getFacingDirection(BlockState blockState) {
      return blockState.getValue(FACING);
   }

   public int getAge(BlockState blockState) {
      return blockState.getValue(this.getAgeProperty());
   }

   public boolean isRandomlyTicking(BlockState blockState) {
      return true;
   }

   public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource randomSource) {
      if (!level.isAreaLoaded(pos, 1))
      {
         return; // Forge: prevent loading unloaded chunks when checking neighbor's light
      }

      boolean canGrow = randomSource.nextIntBetweenInclusive(1,10) == 1;

      if(!canGrow)
      {
         return;
      }

      if(getAge(blockState) == 2)
      {
         BlockState newBlockState = ModBlocks.SOULITE_CLUSTER_BLOCK.get().defaultBlockState()
                 .setValue(SouliteClusterBlock.FACING, blockState.getValue(SouliteBudBlock.FACING));
         level.setBlockAndUpdate(pos, newBlockState);
         return;
      }

      if (getAge(blockState) < 2)
      {
         int newAge = getAge(blockState) + 1;
         BlockState newBlockState = defaultBlockState()
                 .setValue(SouliteBudBlock.FACING, getFacingDirection(blockState))
                 .setValue(AGE, newAge);

         level.setBlockAndUpdate(pos, newBlockState);
      }
   }

   public VoxelShape getShape(BlockState p_152021_, BlockGetter p_152022_, BlockPos p_152023_, CollisionContext p_152024_) {
      Direction direction = p_152021_.getValue(FACING);
      switch (direction) {
         case NORTH:
            return this.northAabb;
         case SOUTH:
            return this.southAabb;
         case EAST:
            return this.eastAabb;
         case WEST:
            return this.westAabb;
         case DOWN:
            return this.downAabb;
         case UP:
         default:
            return this.upAabb;
      }
   }

   public void onProjectileHit(Level p_152001_, BlockState p_152002_, BlockHitResult p_152003_, Projectile p_152004_) {
      if (!p_152001_.isClientSide) {
         BlockPos blockpos = p_152003_.getBlockPos();
         p_152001_.playSound((Player)null, blockpos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.5F + p_152001_.random.nextFloat() * 1.2F);
         p_152001_.playSound((Player)null, blockpos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.5F + p_152001_.random.nextFloat() * 1.2F);
      }

   }

   public boolean canSurvive(BlockState p_152026_, LevelReader p_152027_, BlockPos p_152028_) {
      Direction direction = p_152026_.getValue(FACING);
      BlockPos blockpos = p_152028_.relative(direction.getOpposite());
      return p_152027_.getBlockState(blockpos).isFaceSturdy(p_152027_, blockpos, direction);
   }

   public BlockState updateShape(BlockState p_152036_, Direction p_152037_, BlockState p_152038_, LevelAccessor p_152039_, BlockPos p_152040_, BlockPos p_152041_) {
      if (p_152036_.getValue(WATERLOGGED)) {
         p_152039_.scheduleTick(p_152040_, Fluids.WATER, Fluids.WATER.getTickDelay(p_152039_));
      }

      return p_152037_ == p_152036_.getValue(FACING).getOpposite() && !p_152036_.canSurvive(p_152039_, p_152040_) ? Blocks.AIR.defaultBlockState() : super.updateShape(p_152036_, p_152037_, p_152038_, p_152039_, p_152040_, p_152041_);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext p_152019_) {
      LevelAccessor levelaccessor = p_152019_.getLevel();
      BlockPos blockpos = p_152019_.getClickedPos();
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER)).setValue(FACING, p_152019_.getClickedFace()).setValue(AGE, 0);
   }

   public BlockState rotate(BlockState p_152033_, Rotation p_152034_) {
      return p_152033_.setValue(FACING, p_152034_.rotate(p_152033_.getValue(FACING)));
   }

   public BlockState mirror(BlockState p_152030_, Mirror p_152031_) {
      return p_152030_.rotate(p_152031_.getRotation(p_152030_.getValue(FACING)));
   }

   public FluidState getFluidState(BlockState p_152045_) {
      return p_152045_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152045_);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_152043_) {
      p_152043_.add(WATERLOGGED, FACING, AGE);
   }
}