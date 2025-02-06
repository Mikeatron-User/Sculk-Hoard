package com.github.sculkhorde.client.renderer;

import com.github.sculkhorde.common.effect.SculkFogEffect;
import com.github.sculkhorde.core.ModMobEffects;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

public class SculkFogRenderer {

    @SubscribeEvent
    public void renderFogListener(ViewportEvent.RenderFog event) {
        Entity entity = event.getCamera().getEntity();

        if (entity instanceof Player player) {
            MobEffectInstance effect = player.getEffect(ModMobEffects.SCULK_FOG.get());
            if (effect != null) {

                float fogDistance = 16.0f;
                float distance = 0;

                int duration = effect.getDuration();
                int blockY = effect.getAmplifier();
                int playerY = player.blockPosition().getY();

                if ((playerY-64) > blockY) {
                    distance = (playerY - blockY) - 16;
                    distance = Math.max(distance, 16);

                    fogDistance += distance - 16;
                }

                //float times = (duration <= 40) ? 1.5f : 1f;
                if (duration <= 160) {fogDistance = fogDistance + (160 - duration);}

                RenderSystem.setShaderFogColor(0.071f, 0.118f, 0.188f);
                RenderSystem.setShaderFogStart(0.0F);
                RenderSystem.setShaderFogEnd(fogDistance);
            }
        }
    }

}