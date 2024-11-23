package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulBreezeProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulBreezeProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SoulBreezeProjectileRenderer extends GeoEntityRenderer<SoulBreezeProjectileEntity> {
    public SoulBreezeProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulBreezeProjectileModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }

    @Override
    public void render(SoulBreezeProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
