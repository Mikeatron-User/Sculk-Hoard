package com.github.sculkhorde.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeItem;

public class FerrisciteHoeItem extends HoeItem implements IForgeItem, IHealthRepairable {
    protected static float ATTACK_SPEED = 1.0F;
    protected static int ATTACK_DAMAGE = 5;
    public static String blocksBrokenTagID = "blocks_broken";
    public static String lastBlockBrokenID = "last_block_broken";

    protected static Properties PROPERTIES = new Properties()
            .setNoRepair()
            .rarity(Rarity.EPIC)
            .durability(3000)
            .defaultDurability(3000);

    public FerrisciteHoeItem() {
        super(Tiers.DIAMOND, ATTACK_DAMAGE, ATTACK_SPEED, PROPERTIES);
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if(isCorrectToolForDrops(itemStack, blockState))
        {
            float baseMiningSpeed = 10F;
            float additionalMiningSpeed = Math.abs(Math.min(getBlocksBroken(itemStack) / 10F, 100F));
            return baseMiningSpeed + additionalMiningSpeed;
        }
        else
        {
            return super.getDestroySpeed(itemStack, blockState);
        }

    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack pickaxe, Level level, BlockState blockState, BlockPos pos, LivingEntity entity) {

        if(!level.isClientSide())
        {
            if(!isBlockEqualToLastBlockBroken(pickaxe, blockState.getBlock()))
            {
                updateLastBlockBroken(pickaxe, blockState.getBlock());
                resetBlocksBroken(pickaxe);
                super.mineBlock(pickaxe, level, blockState, pos, entity);
            }

            incrementBlocksBroken(pickaxe);
        }

        return super.mineBlock(pickaxe, level, blockState, pos, entity);
    }

    public static int getBlocksBroken(ItemStack stack)
    {
        CompoundTag nbt = stack.getOrCreateTag();
        if(!nbt.contains(blocksBrokenTagID))
        {
            return 0;
        }

        return nbt.getInt(blocksBrokenTagID);
    }

    public static void incrementBlocksBroken(ItemStack stack)
    {
        CompoundTag nbt = stack.getOrCreateTag();
        if(!nbt.contains(blocksBrokenTagID))
        {
            return;
        }

        nbt.putInt(blocksBrokenTagID, Math.min(nbt.getInt(blocksBrokenTagID) + 1, 1000));
    }

    public static void resetBlocksBroken(ItemStack stack)
    {
        CompoundTag nbt = stack.getOrCreateTag();
        if(!nbt.contains(blocksBrokenTagID))
        {
            return;
        }

        nbt.putInt(blocksBrokenTagID, Math.max(0, nbt.getInt(blocksBrokenTagID) - 20));
    }

    public static boolean isBlockEqualToLastBlockBroken(ItemStack stack, Block block)
    {
        CompoundTag nbt = stack.getOrCreateTag();
        if(!nbt.contains(lastBlockBrokenID))
        {
            return false;
        }

        return nbt.getString(lastBlockBrokenID).equals(block.toString());
    }

    public static void updateLastBlockBroken(ItemStack stack, Block block)
    {
        CompoundTag nbt = stack.getOrCreateTag();

        if(!nbt.contains(lastBlockBrokenID))
        {
            return;
        }


        nbt.putString(lastBlockBrokenID, block.toString());
    }



    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        if(!level.isClientSide())
        {
            CompoundTag nbt = itemStack.getOrCreateTag();
            if(!nbt.contains(blocksBrokenTagID))
            {
                nbt.putInt(blocksBrokenTagID, 0);
            }

            if(!nbt.contains(lastBlockBrokenID))
            {
                nbt.putString(lastBlockBrokenID, "");
            }

        }

        super.inventoryTick(itemStack, level, entity, slot, selected);
    }

    @Override
    public void repair(ItemStack stack, int amount) {
        stack.setDamageValue(Math.max(stack.getDamageValue() - amount, 0));
    }
}
