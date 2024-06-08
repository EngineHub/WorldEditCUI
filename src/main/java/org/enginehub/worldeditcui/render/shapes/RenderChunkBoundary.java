/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render.shapes;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.util.Vector3;

public class RenderChunkBoundary extends RenderRegion
{
	private final Minecraft mc;
	private final Render3DGrid grid;

	public RenderChunkBoundary(final RenderStyle boundaryStyle, final RenderStyle gridStyle, final Minecraft minecraft)
	{
		super(boundaryStyle);

		this.mc = minecraft;

		this.grid = new Render3DGrid(gridStyle, Vector3.ZERO, Vector3.ZERO);
		this.grid.setSpacing(4.0);
	}

	@Override
	public void render(final CUIRenderContext ctx)
	{
		final double yMin = this.mc.level != null ? this.mc.level.dimensionType().minY() : 0.0;
		final double yMax = this.mc.level != null ? this.mc.level.dimensionType().logicalHeight() - yMin : 256.0;

		final long xBlock = Mth.floor(ctx.cameraPos().getX());
		final long zBlock = Mth.floor(ctx.cameraPos().getZ());

		final int xChunk = (int)(xBlock >> 4);
		final int zChunk = (int)(zBlock >> 4);

		final double xBase = 0 - (xBlock - (xChunk * 16)) - (ctx.cameraPos().getX() - xBlock);
		final double zBase = (0 - (zBlock - (zChunk * 16)) - (ctx.cameraPos().getZ() - zBlock)) + 16;

		this.grid.setPosition(new Vector3(xBase - OFFSET, yMin, zBase - 16 - OFFSET), new Vector3(xBase + 16 + OFFSET, yMax, zBase + OFFSET));

		ctx.flush();
		ctx.poseStack().pushPose();
		ctx.poseStack().translate(0.0, -ctx.cameraPos().getY(), 0.0);
		ctx.applyMatrices();

		ctx.withCameraAt(Vector3.ZERO, this.grid::render);

		this.renderChunkBorder(ctx, yMin, yMax, xBase, zBase);

		if (this.mc.level != null)
		{
			this.renderChunkBoundary(ctx, xChunk, zChunk, xBase, zBase);
		}

		ctx.flush();
		ctx.poseStack().popPose();
		ctx.applyMatrices();
	}

	private void renderChunkBorder(final CUIRenderContext ctx, final double yMin, final double yMax, final double xBase, final double zBase)
	{
		final int spacing = 16;

		for (final LineStyle line : this.style.getLines())
		{
			if (ctx.apply(line, this.style.getRenderType()))
			{
				ctx.color(line)
					.beginLines();

				for (int x = -16; x <= 32; x += spacing)
				{
					for (int z = -16; z <= 32; z += spacing)
					{
						ctx.vertex(xBase + x, yMin, zBase - z)
							.vertex(xBase + x, yMax, zBase - z);
					}
				}

				for (double y = yMin; y <= yMax; y += yMax)
				{
					ctx.vertex(xBase, y, zBase)
						.vertex(xBase, y, zBase - 16)
						.vertex(xBase, y, zBase - 16)
						.vertex(xBase + 16, y, zBase - 16)
						.vertex(xBase + 16, y, zBase - 16)
						.vertex(xBase + 16, y, zBase)
						.vertex(xBase + 16, y, zBase)
						.vertex(xBase, y, zBase);
				}

				ctx.endLines();
			}
		}
	}

	private void renderChunkBoundary(final CUIRenderContext ctx, final int xChunk, final int zChunk, final double xBase, final double zBase)
	{
		final ChunkAccess chunk = this.mc.level.getChunk(xChunk, zChunk);
		final Heightmap heightMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);

		for (final LineStyle line : this.style.getLines())
		{
			if (ctx.apply(line, this.style.getRenderType()))
			{
				ctx.beginLines()
						.color(line);

				final int[][] lastHeight = { { -1, -1 }, { -1, -1 } };
				for (int i = 0, height = 0; i < 16; i++)
				{
					for (int j = 0; j < 2; j++)
					{
						for (int axis = 0; axis < 2; axis++)
						{
							height = axis == 0 ? heightMap.getFirstAvailable(j * 15, i) : heightMap.getFirstAvailable(i, j * 15);
							final double xPos = axis == 0 ? xBase + (j * 16) : xBase + i;
							final double zPos = axis == 0 ? zBase - 16 + i : zBase - 16 + (j * 16);
							if (lastHeight[axis][j] > -1 && height != lastHeight[axis][j])
							{
								ctx.vertex(xPos, lastHeight[axis][j] + OFFSET, zPos)
									.vertex(xPos, height + OFFSET, zPos);
							}
							ctx.vertex(xPos, height + OFFSET, zPos)
								.vertex(xPos + axis, height + OFFSET, zPos + (1 - axis));
							lastHeight[axis][j] = height;
						}
					}
				}

				ctx.endLines();
			}
		}
	}

}
