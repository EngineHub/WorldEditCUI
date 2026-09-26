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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.render.PipelineProvider;
import org.enginehub.worldeditcui.render.RenderSink;
import org.enginehub.worldeditcui.util.Vector3;
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
	private final CUIRenderContext ctx = new CUIRenderContext();
	private final List<PipelineProvider> pipelines;
	private final Set<String> disabledPipelines = new HashSet<>();
	private PipelineProvider activePipeline;
	private RenderSink sink;

	public CUIListenerWorldRender(final WorldEditCUI controller, final Minecraft minecraft, final List<PipelineProvider> pipelines)
	{
		this.controller = controller;
		this.minecraft = minecraft;
		this.pipelines = List.copyOf(pipelines);
	}

	private RenderSink providePipeline()
	{
		final PipelineProvider preferred = this.pipelines.getFirst();
		if (this.sink != null && this.activePipeline != null
			&& (this.activePipeline == preferred || !preferred.available() || this.disabledPipelines.contains(preferred.id()))) {
			return this.sink;
		} else if (this.sink != null && this.activePipeline != null && this.disabledPipelines.contains(this.activePipeline.id())) {
			this.activePipeline = null;
			this.sink = null;
		}

		for (final PipelineProvider pipeline : this.pipelines)
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

	public void onRender(final float partialTicks, final SubmitNodeCollector collector, final PoseStack poseStack) {
		try {
			final RenderSink sink = this.providePipeline();
			if (this.activePipeline != null && !this.activePipeline.shouldRender())
			{
				// allow ignoring eg. shadow pass
				return;
			}
			final ProfilerFiller profiler = Profiler.get();
			profiler.push("worldeditcui");
			final GpuBufferSlice fogStart = RenderSystem.getShaderFog();
			boolean posePushed = false;
			try {
				this.ctx.init(
					new Vector3(this.minecraft.gameRenderer.mainCamera().position()),
					partialTicks,
					sink,
					this.controller.getConfiguration().isHideObstructedLines(),
					poseStack
				);
				this.ctx.beginFrame(collector, poseStack);
				RenderSystem.setShaderFog(this.minecraft.gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
				poseStack.pushPose();
				posePushed = true;
				this.controller.renderSelections(this.ctx);
				this.sink.flush();
			} catch (final Exception e) {
				this.controller.getDebugger().error("Error while attempting to render WorldEdit CUI", e);
				this.invalidatePipeline();
			} finally {
				if (posePushed) {
					poseStack.popPose();
				}
				RenderSystem.setShaderFog(fogStart);
				this.ctx.reset();
				profiler.pop();
			}
		} catch (final Exception ex)
		{
			this.controller.getDebugger().error("Failed while preparing state for WorldEdit CUI", ex);
			this.invalidatePipeline();
		}
	}
}
