package com.github.sculkhorde.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class ModColaborationHelper {


    // https://www.curseforge.com/minecraft/mc-mods/from-another-world
    public static boolean isFromAnotherWorldLoaded()
    {
        return ModList.get().isLoaded("fromanotherworld");
    }

    public static boolean isThisAFromAnotherWorldEntity(LivingEntity entity)
    {
        if(isFromAnotherWorldLoaded())
        {
            return false;
        }

        TagKey<EntityType<?>> fromAnotherWorldEntityTagKey = TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation("fromanotherworld:things"));

        return entity.getType().is(fromAnotherWorldEntityTagKey);
    }

    // https://www.curseforge.com/minecraft/mc-mods/fungal-infection-spore
    public static boolean isSporeLoaded()
    {
        return ModList.get().isLoaded("spore");
    }

    public static boolean isThisASporeEntity(LivingEntity entity)
    {
        if(isSporeLoaded())
        {
            return false;
        }

        TagKey<EntityType<?>> sporeEntityTagKey = TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation("spore:fungus_entities"));

        return entity.getType().is(sporeEntityTagKey);
    }
}
