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
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class VanillaPipelineProvider implements PipelineProvider {

    public static class DefaultTypeFactory implements BatchedRenderSink.TypeFactory {
        public static final DefaultTypeFactory INSTANCE = new DefaultTypeFactory();
        private static final RenderPipeline.Snippet QUADS_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withCull(false)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .buildSnippet();
        private static final RenderPipeline.Snippet LINES_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader("core/rendertype_lines")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .buildSnippet();
        private static final Map<RenderStyle.RenderType, BatchedRenderSink.VariantSet> VARIANTS = createVariants();

        private DefaultTypeFactory() {}

        @Override
        public BatchedRenderSink.VariantSet forStyle(final RenderStyle.RenderType renderType) {
            final BatchedRenderSink.VariantSet variants = VARIANTS.get(renderType);
            if (variants == null) {
                throw new IllegalArgumentException("Unsupported render type " + renderType);
            }
            return variants;
        }

        private static Map<RenderStyle.RenderType, BatchedRenderSink.VariantSet> createVariants() {
            final EnumMap<RenderStyle.RenderType, BatchedRenderSink.VariantSet> variants = new EnumMap<>(RenderStyle.RenderType.class);
            for (final RenderStyle.RenderType renderType : RenderStyle.RenderType.values()) {
                final String idSuffix = renderType.name().toLowerCase(Locale.ROOT);
                final CompareOp depthTest = renderType.depthTest();
                final BatchedRenderSink.RenderTarget lines = createLinesTarget(idSuffix, depthTest);
                variants.put(renderType, new BatchedRenderSink.VariantSet(
                    createQuadsTarget(idSuffix, depthTest),
                    lines,
                    lines
                ));
            }
            return Map.copyOf(variants);
        }

        private static BatchedRenderSink.RenderTarget createQuadsTarget(final String idSuffix, final CompareOp depthTest) {
            final RenderPipeline pipeline = RenderPipeline.builder(QUADS_SNIPPET)
                .withLocation("pipeline/wecui_quads_" + idSuffix)
                .withDepthStencilState(new DepthStencilState(depthTest, true))
                .build();
            final RenderSetup setup = RenderSetup.builder(pipeline)
                .bufferSize(1536)
                .sortOnUpload()
                .createRenderSetup();
            final RenderType renderType = RenderType.create("wecui_quads_" + idSuffix, setup);
            return new BatchedRenderSink.RenderTarget(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, renderType::draw);
        }

        private static BatchedRenderSink.RenderTarget createLinesTarget(final String idSuffix, final CompareOp depthTest) {
            final RenderPipeline pipeline = RenderPipeline.builder(LINES_SNIPPET)
                .withLocation("pipeline/wecui_lines_" + idSuffix)
                .withDepthStencilState(new DepthStencilState(depthTest, true))
                .build();
            final RenderSetup setup = RenderSetup.builder(pipeline).createRenderSetup();
            final RenderType renderType = RenderType.create("wecui_lines_" + idSuffix, setup);
            return new BatchedRenderSink.RenderTarget(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, renderType::draw);
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
        return new BatchedRenderSink(DefaultTypeFactory.INSTANCE);
    }
}
