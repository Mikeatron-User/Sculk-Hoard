package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulBlastAttackEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SoulBlastEntityAttackModel extends DefaultedEntityGeoModel<SoulBlastAttackEntity> {
    public SoulBlastEntityAttackModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, ModEntities.SOUL_BLAST_ENTITY_ID));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SoulBlastAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
