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

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public final class VanillaPipelineProvider implements PipelineProvider {

    public static class DefaultTypeFactory implements BatchedRenderSink.TypeFactory {
        public static final DefaultTypeFactory INSTANCE = new DefaultTypeFactory();
        private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");
        private static final RenderPipeline.Snippet QUADS_SNIPPET = createSnippet(
            RenderPipelines.DEBUG_QUADS,
            DefaultVertexFormat.POSITION_COLOR,
            PrimitiveTopology.QUADS
        );
        private static final RenderPipeline.Snippet LINES_SNIPPET = createSnippet(
            RenderPipelines.LINES,
            DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH,
            PrimitiveTopology.LINES
        );
        private static final Map<RenderStyle.RenderType, BatchedRenderSink.VariantSet> VARIANTS = createVariants();

        private DefaultTypeFactory() {}

        private static RenderPipeline.Snippet createSnippet(final RenderPipeline base, final VertexFormat vertexFormat, final PrimitiveTopology primitiveTopology) {
            final RenderPipeline.Builder builder = RenderPipeline.builder()
                .withVertexShader(base.getShaders().get(ShaderType.VERTEX))
                .withFragmentShader(base.getShaders().get(ShaderType.FRAGMENT))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withVertexBinding(0, vertexFormat)
                .withPrimitiveTopology(primitiveTopology)
                .withDepthStencilState(DepthStencilState.DEFAULT);
            base.getBindGroupLayouts().forEach(builder::withBindGroupLayout);
            return builder.buildSnippet();
        }

        private static OitPipelineSet createOitPipelines(
            final String name,
            final RenderPipeline.Snippet snippet,
            final CompareOp depthTest
        ) {
            final Consumer<RenderPipeline.Builder> applyDepthTest =
                builder -> builder.withDepthStencilState(new DepthStencilState(depthTest, false));
            return OitPipelineSet.builder(name, RenderPipeline.builder(snippet))
                .withDepthBoundsModifier(applyDepthTest)
                .withTransmittanceModifier(applyDepthTest)
                .withAccumulateModifier(applyDepthTest)
                .build();
        }

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
                .withLocation(Identifier.fromNamespaceAndPath("worldeditcui", "pipeline/quads_" + idSuffix))
                .withDepthStencilState(new DepthStencilState(depthTest, false))
                .build();
            if (IRIS_LOADED) {
                IrisPipelineIntegration.registerQuads(pipeline);
            }
            final OitPipelineSet oitPipelines = createOitPipelines(
                "wecui_quads_" + idSuffix,
                RenderPipelines.OIT_DEBUG_FILLED_SNIPPET,
                depthTest
            );
            final RenderSetup setup = RenderSetup.builder(pipeline)
                .setOitPipelines(oitPipelines)
                .sortOnUpload()
                .createRenderSetup();
            final RenderType renderType = RenderType.create("wecui_quads_" + idSuffix, setup);
            return new BatchedRenderSink.RenderTarget(
                renderType,
                DefaultVertexFormat.POSITION_COLOR
            );
        }

        private static BatchedRenderSink.RenderTarget createLinesTarget(final String idSuffix, final CompareOp depthTest) {
            final RenderPipeline pipeline = RenderPipeline.builder(LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("worldeditcui", "pipeline/lines_" + idSuffix))
                .withDepthStencilState(new DepthStencilState(depthTest, false))
                .build();
            if (IRIS_LOADED) {
                IrisPipelineIntegration.registerLines(pipeline);
            }
            final OitPipelineSet oitPipelines = createOitPipelines(
                "wecui_lines_" + idSuffix,
                RenderPipelines.OIT_LINES_SNIPPET,
                depthTest
            );
            final RenderSetup setup = RenderSetup.builder(pipeline)
                .setOitPipelines(oitPipelines)
                .createRenderSetup();
            final RenderType renderType = RenderType.create("wecui_lines_" + idSuffix, setup);
            return new BatchedRenderSink.RenderTarget(
                renderType,
                DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH
            );
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
