package com.github.sculkhorde.common.item;

import net.minecraft.world.item.ItemStack;

public interface IHealthRepairable {

    void repair(ItemStack stack, int amount);
}
