package com.github.sculkhorde.util;

import com.github.sculkhorde.common.item.IHealthRepairable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class InventoryUtil {

    public static void repairIHealthRepairableItemStacks(Inventory inventory, int amount)
    {
        for(ItemStack inventoryItemStack : inventory.items)
        {
            if(inventoryItemStack.getItem() instanceof IHealthRepairable repairable)
            {
                repairable.repair(inventoryItemStack, amount);
            }
        }
    }
}
