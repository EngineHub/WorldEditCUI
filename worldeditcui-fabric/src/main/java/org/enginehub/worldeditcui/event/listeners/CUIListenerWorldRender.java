/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.event.listeners;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.config.CUIConfiguration;
import org.enginehub.worldeditcui.render.BufferBuilderRenderSink;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.PipelineProvider;
import org.enginehub.worldeditcui.render.RenderSink;
import org.enginehub.worldeditcui.util.Vector3;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL32;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listener for WorldRenderEvent
 *
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIListenerWorldRender
{
	private final WorldEditCUI controller;

	private final Minecraft minecraft;
	private final CUIConfiguration configuration;
	private final CUIRenderContext ctx = new CUIRenderContext();
	private final List<PipelineProvider> pipelines;
	private final Set<String> disabledPipelines = new HashSet<>();
	private boolean experimentalRendererSelected;
	private PipelineProvider activePipeline;
	private RenderSink sink;

	public CUIListenerWorldRender(final WorldEditCUI controller, final Minecraft minecraft, final CUIConfiguration configuration, final List<PipelineProvider> pipelines)
	{
		this.controller = controller;
		this.minecraft = minecraft;
		this.configuration = configuration;
		this.pipelines = List.copyOf(pipelines);
		this.experimentalRendererSelected = configuration.isExperimentalRenderer();
	}

	private RenderSink providePipeline()
	{
		final boolean useExperimentalRenderer = this.configuration.isExperimentalRenderer();
		final PipelineProvider preferred = preferredPipeline();
		if (useExperimentalRenderer != this.experimentalRendererSelected) {
			this.experimentalRendererSelected = useExperimentalRenderer;
			this.disabledPipelines.clear();
			this.activePipeline = null;
			this.sink = null;
		} else if (this.sink != null && this.activePipeline != null
			&& (this.activePipeline == preferred || !preferred.available() || this.disabledPipelines.contains(preferred.id()))) {
			return this.sink;
		} else if (this.sink != null && this.activePipeline != null && this.disabledPipelines.contains(this.activePipeline.id())) {
			this.activePipeline = null;
			this.sink = null;
		}

		for (final PipelineProvider pipeline : orderedPipelines())
		{
			if (this.disabledPipelines.contains(pipeline.id())) {
				continue;
			}
			if (pipeline.available())
			{
				try
				{
					final RenderSink sink = pipeline.provide();
					this.activePipeline = pipeline;
					return this.sink = sink;
				}
				catch (final Exception ex)
				{
					this.disabledPipelines.add(pipeline.id());
					this.controller.getDebugger().info("Failed to render with pipeline " + pipeline.id() + ", which declared itself as available... trying next");
				}
			}
		}

		throw new IllegalStateException("No pipeline available to render with!");
	}

	private void invalidatePipeline() {
		if (this.activePipeline != null) {
			this.disabledPipelines.add(this.activePipeline.id());
		}
		this.activePipeline = null;
		this.sink = null;
	}

	private PipelineProvider preferredPipeline() {
		return pipelineById(this.configuration.isExperimentalRenderer() ? "vanilla" : "legacy-vanilla");
	}

	private List<PipelineProvider> orderedPipelines() {
		if (this.configuration.isExperimentalRenderer()) {
			return List.of(
				pipelineById("vanilla"),
				pipelineById("legacy-vanilla"),
				pipelineById("optifine")
			);
		}
		return List.of(
			pipelineById("legacy-vanilla"),
			pipelineById("optifine"),
			pipelineById("vanilla")
		);
	}

	private PipelineProvider pipelineById(final String id) {
		for (final PipelineProvider pipeline : this.pipelines) {
			if (pipeline.id().equals(id)) {
				return pipeline;
			}
		}
		throw new IllegalStateException("Missing pipeline provider " + id);
	}

	public void onRender(final float partialTicks) {
		try {
			final RenderSink sink = this.providePipeline();
			if (this.activePipeline != null && !this.activePipeline.shouldRender())
			{
				// allow ignoring eg. shadow pass
				return;
			}
			final ProfilerFiller profiler = Profiler.get();
			profiler.push("worldeditcui");
			this.ctx.init(new Vector3(this.minecraft.gameRenderer.getMainCamera().position()), partialTicks, sink);
			final GpuBufferSlice fogStart = RenderSystem.getShaderFog();
			RenderSystem.setShaderFog(this.minecraft.gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
			final Matrix4fStack poseStack = RenderSystem.getModelViewStack();
            final boolean legacySink = sink instanceof BufferBuilderRenderSink;
			poseStack.pushMatrix();
            if (legacySink) {
			    GlStateManager._enableBlend();
			    // RenderSystem.disableTexture();
			    GlStateManager._enableDepthTest();
			    GlStateManager._depthMask(true);
			    BufferBuilderRenderSink.LineWidth.set(LineStyle.DEFAULT_WIDTH);
            }

			try {
				this.controller.renderSelections(this.ctx);
				this.sink.flush();
			} catch (final Exception e) {
				this.controller.getDebugger().error("Error while attempting to render WorldEdit CUI", e);
				this.invalidatePipeline();
			}

            if (legacySink) {
			    GlStateManager._depthFunc(GL32.GL_LEQUAL);
			    // RenderSystem.enableTexture();
			    GlStateManager._disableBlend();
            }
			poseStack.popMatrix();
			RenderSystem.setShaderFog(fogStart);
			profiler.pop();
		} catch (final Exception ex)
		{
			this.controller.getDebugger().error("Failed while preparing state for WorldEdit CUI", ex);
			this.invalidatePipeline();
		}
	}
}
