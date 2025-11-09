/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.enginehub.worldeditcui.callback.WorldRenderCallback;
import org.enginehub.worldeditcui.render.WecuiRenderContext;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to LevelRenderer to inject our world render callbacks.
 * Temporary until Fabric finishes their new rendering API.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    // LAST (old LAST)
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void wecui$last(
            GraphicsResourceAllocator gfx, DeltaTracker delta, boolean renderBlockOutline,
            Camera camera, Matrix4f modelView, Matrix4f projection, Matrix4f inverseProjection,
            GpuBufferSlice slice, Vector4f clearColor, boolean renderSky, CallbackInfo ci
    ) {
        PoseStack pose = new PoseStack();
        pose.last().pose().set(modelView);
        WorldRenderCallback.LAST.invoker()
                .render(new WecuiRenderContext(this.minecraft, camera, delta, pose, projection));
    }

    // AFTER_TRANSLUCENT — fires immediately after translucent pass queued/executed
    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;addParticlesPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
            )
    )
    private void wecui$afterTranslucent(
            GraphicsResourceAllocator gfx, DeltaTracker delta, boolean renderBlockOutline,
            Camera camera, Matrix4f modelView, Matrix4f projection, Matrix4f inverseProjection,
            GpuBufferSlice slice, Vector4f clearColor, boolean renderSky,
            CallbackInfo ci
    ) {
        var pose = new PoseStack();
        pose.last().pose().set(modelView);
        WorldRenderCallback.AFTER_TRANSLUCENT.invoker()
                .render(new WecuiRenderContext(this.minecraft, camera, delta, pose, projection));
    }
}
