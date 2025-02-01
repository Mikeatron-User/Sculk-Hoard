package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ChunkInfectEntityRenderer extends EntityRenderer<CursorSurfaceInfectorEntity> {
    public ChunkInfectEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(CursorSurfaceInfectorEntity pEntity) {
        return null;
    }
}
