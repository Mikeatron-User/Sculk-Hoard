package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SculkFogEffect extends MobEffect {

    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;

    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected SculkFogEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public SculkFogEffect() {
        this(effectType, liquidColor);
    }

}
