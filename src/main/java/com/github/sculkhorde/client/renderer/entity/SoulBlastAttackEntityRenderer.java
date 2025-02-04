package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulBlastEntityAttackModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulBlastAttackEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;


public class SoulBlastAttackEntityRenderer extends GeoEntityRenderer<SoulBlastAttackEntity> {

    public SoulBlastAttackEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulBlastEntityAttackModel());
    }


}
