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

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class VanillaPipelineProvider implements PipelineProvider {

    public static class DefaultTypeFactory implements BufferBuilderRenderSink.TypeFactory {
        public static final DefaultTypeFactory INSTANCE = new DefaultTypeFactory();
        private static final RenderSetup QUADS_SETUP = RenderSetup.builder(
                RenderPipeline.builder(RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                                .withVertexShader("core/position_color")
                                .withFragmentShader("core/position_color")
                                .withBlend(BlendFunction.TRANSLUCENT)
                                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                                .buildSnippet())
                        .withLocation("pipeline/wecui_quads").withCull(false).build()
        ).bufferSize(1536).sortOnUpload().createRenderSetup();
        private static final BufferBuilderRenderSink.RenderType QUADS = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR,
                RenderType.create("quads", QUADS_SETUP)
        );
        private static final BufferBuilderRenderSink.RenderType LINES = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, RenderTypes.LINES);
        private static final BufferBuilderRenderSink.RenderType LINES_LOOP = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, RenderTypes.LINES);

        private DefaultTypeFactory() {}

        @Override
        public BufferBuilderRenderSink.RenderType quads() {
            return QUADS;
        }

        @Override
        public BufferBuilderRenderSink.RenderType lines() {
            return LINES;
        }

        @Override
        public BufferBuilderRenderSink.RenderType linesLoop() {
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
        return new BufferBuilderRenderSink(DefaultTypeFactory.INSTANCE);
    }
}
