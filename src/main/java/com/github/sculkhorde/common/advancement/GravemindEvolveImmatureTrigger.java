package com.github.sculkhorde.common.advancement;

import com.github.sculkhorde.core.SculkHorde;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class GravemindEvolveImmatureTrigger extends SimpleCriterionTrigger<GravemindEvolveImmatureTrigger.GravemindEvoleImmatureCriterion> implements CustomCriterionTrigger{

    public static final GravemindEvolveImmatureTrigger INSTANCE = new GravemindEvolveImmatureTrigger();
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "gravemind_evolve_immature");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public GravemindEvoleImmatureCriterion createInstance(JsonObject jsonObject, EntityPredicate.Composite awarePredicate, DeserializationContext deserializationContext) {
        return new GravemindEvoleImmatureCriterion(awarePredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    public static class GravemindEvoleImmatureCriterion extends AbstractCriterionTriggerInstance {

        public GravemindEvoleImmatureCriterion(EntityPredicate.Composite awarePredicate) {
            super(ID, awarePredicate);
        }
    }
}
