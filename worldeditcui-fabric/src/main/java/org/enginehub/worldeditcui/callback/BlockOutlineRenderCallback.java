/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.callback;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;

public interface BlockOutlineRenderCallback {
    record Context(
            Minecraft client,
            Camera camera,
            PoseStack poseStack,
            GpuBufferSlice projectionMatrixBuffer,
            VertexConsumer vertexConsumer,
            double camX, double camY, double camZ,
            BlockOutlineRenderState state
    ) {}

    /**
     * Return true to cancel vanilla outline (replace it), false to let vanilla draw after you.
     */
    boolean render(Context ctx);

    Event<BlockOutlineRenderCallback> EVENT =
            EventFactory.createArrayBacked(BlockOutlineRenderCallback.class, cbs -> ctx -> {
                boolean cancel = false;
                for (var cb : cbs) cancel |= cb.render(ctx);
                return cancel;
            });
}

