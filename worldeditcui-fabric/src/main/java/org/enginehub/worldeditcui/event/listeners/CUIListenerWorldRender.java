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
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.PipelineProvider;
import org.enginehub.worldeditcui.render.RenderSink;
import org.enginehub.worldeditcui.util.Vector3;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL32;

import java.util.List;

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
	private int currentPipelineIdx;
	private RenderSink sink;

	public CUIListenerWorldRender(final WorldEditCUI controller, final Minecraft minecraft, final List<PipelineProvider> pipelines)
	{
		this.controller = controller;
		this.minecraft = minecraft;
		this.pipelines = List.copyOf(pipelines);
	}

	private RenderSink providePipeline()
	{
		if (this.sink != null)
		{
			return this.sink;
		}

		for (int i = this.currentPipelineIdx; i < this.pipelines.size(); i++)
		{
			final PipelineProvider pipeline = this.pipelines.get(i);
			if (pipeline.available())
			{
				try
				{
					final RenderSink sink = pipeline.provide();
					this.currentPipelineIdx = i;
					return this.sink = sink;
				}
				catch (final Exception ex)
				{
					this.controller.getDebugger().info("Failed to render with pipeline " + pipeline.id() + ", which declared itself as available... trying next");
				}
			}
		}

		throw new IllegalStateException("No pipeline available to render with!");
	}

	private void invalidatePipeline() {
		if (this.currentPipelineIdx < this.pipelines.size() - 1) {
			this.currentPipelineIdx++;
			this.sink = null;
		}
	}

	public void onRender(final float partialTicks) {
		try {
			final RenderSink sink = this.providePipeline();
			if (!this.pipelines.get(this.currentPipelineIdx).shouldRender())
			{
				// allow ignoring eg. shadow pass
				return;
			}
			final ProfilerFiller profiler = Profiler.get();
			profiler.push("worldeditcui");
			this.ctx.init(new Vector3(this.minecraft.gameRenderer.getMainCamera().getPosition()), partialTicks, sink);
			final GpuBufferSlice fogStart = RenderSystem.getShaderFog();
			RenderSystem.setShaderFog(this.minecraft.gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
			final Matrix4fStack poseStack = RenderSystem.getModelViewStack();
			poseStack.pushMatrix();
			GlStateManager._disableCull();
			GlStateManager._enableBlend();
			// RenderSystem.disableTexture();
			GlStateManager._enableDepthTest();
			GlStateManager._blendFuncSeparate(
					GlConst.toGl(SourceFactor.SRC_ALPHA),
					GlConst.toGl(DestFactor.ONE_MINUS_SRC_ALPHA),
					GlConst.toGl(SourceFactor.SRC_ALPHA),
					GlConst.toGl(DestFactor.ONE_MINUS_SRC_ALPHA));
			GlStateManager._depthMask(true);
			RenderSystem.lineWidth(LineStyle.DEFAULT_WIDTH);

			try {
				this.controller.renderSelections(this.ctx);
				this.sink.flush();
			} catch (final Exception e) {
				this.controller.getDebugger().error("Error while attempting to render WorldEdit CUI", e);
				this.invalidatePipeline();
			}

			GlStateManager._depthFunc(GL32.GL_LEQUAL);
			// RenderSystem.enableTexture();
			GlStateManager._disableBlend();
			GlStateManager._enableCull();
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
