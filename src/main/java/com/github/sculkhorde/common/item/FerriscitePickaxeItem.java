package com.github.sculkhorde.common.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.extensions.IForgeItem;

public class FerriscitePickaxeItem extends PickaxeItem implements IForgeItem {
    protected static float ATTACK_SPEED = 1.0F;
    protected static int ATTACK_DAMAGE = 5;
    protected static Properties PROPERTIES = new Properties()
            .setNoRepair()
            .rarity(Rarity.EPIC)
            .durability(3000)
            .defaultDurability(3000);

    public FerriscitePickaxeItem() {
        super(Tiers.DIAMOND, ATTACK_DAMAGE, ATTACK_SPEED, PROPERTIES);
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
