/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;

public final class VanillaPipelineProvider implements PipelineProvider {

    public static class DefaultTypeFactory implements VertexConsumerRenderSink.TypeFactory {
        public static final DefaultTypeFactory INSTANCE = new DefaultTypeFactory();

        private static final VertexConsumerRenderSink.ConfiguredRenderType QUADS = new VertexConsumerRenderSink.ConfiguredRenderType(RenderPipelines.DEBUG_FILLED_SNIPPET, "vanilla/quads");
        private static final VertexConsumerRenderSink.ConfiguredRenderType LINES = new VertexConsumerRenderSink.ConfiguredRenderType(RenderPipelines.LINES_SNIPPET, "vanilla/lines");
        private static final VertexConsumerRenderSink.ConfiguredRenderType LINES_LOOP = new VertexConsumerRenderSink.ConfiguredRenderType(RenderPipelines.LINES_SNIPPET, "vanilla/lines_loop");

        private DefaultTypeFactory() {}

        @Override
        public VertexConsumerRenderSink.ConfiguredRenderType quads() {
            return QUADS;
        }

        @Override
        public VertexConsumerRenderSink.ConfiguredRenderType lines() {
            return LINES;
        }

        @Override
        public VertexConsumerRenderSink.ConfiguredRenderType linesLoop() {
            return LINES_LOOP;
        }
    }

    @Override
    public String id() {
        return "vanilla";
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public RenderSink provide() {
        return new VertexConsumerRenderSink(DefaultTypeFactory.INSTANCE, Minecraft.getInstance().renderBuffers().bufferSource());
    }
}
