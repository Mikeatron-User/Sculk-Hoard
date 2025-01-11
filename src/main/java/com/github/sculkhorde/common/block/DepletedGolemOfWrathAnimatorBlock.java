package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.util.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class DepletedGolemOfWrathAnimatorBlock extends Block implements IForgeBlock {


    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public DepletedGolemOfWrathAnimatorBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public DepletedGolemOfWrathAnimatorBlock() {
        this(getProperties());
    }

    // #### Properties ####

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return GolemOfWrathAnimatorBlock.getProperties();
    }

    /**
     * This is the description the item of the block will display when hovered over.
     * @param stack The item stack
     * @param iBlockReader A block reader
     * @param tooltip The tooltip
     * @param flagIn The flag
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {

        super.appendHoverText(stack, iBlockReader, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.depleted_golem_of_wrath_animator_block")); //Text that displays if holding shift
    }

    /// #### Function ####

    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hitResult) {


        boolean IsPureSouls = playerIn.getMainHandItem().is(ModItems.PURE_SOULS.get());

        if(IsPureSouls)
        {
            // Only on client
            if(level.isClientSide())
            {
                ParticleUtil.spawnPurityDustParticlesOnClient((ClientLevel) level, pos);
                return InteractionResult.SUCCESS;
            }

            // Only on server
            playerIn.getMainHandItem().grow(-1);

            // Convert Back into normal version
            level.setBlockAndUpdate(pos, ModBlocks.GOLEM_OF_WRATH_ANIMATOR_BLOCK.get().defaultBlockState());

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    // Block Entity Related

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

}
