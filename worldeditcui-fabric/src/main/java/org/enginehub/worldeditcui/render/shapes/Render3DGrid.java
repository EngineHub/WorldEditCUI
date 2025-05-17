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

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.util.BoundingBox;
import org.enginehub.worldeditcui.util.Observable;
import org.enginehub.worldeditcui.util.Vector3;

/**
 * Draws the grid for a region between
 * two corners in a cuboid region.
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class Render3DGrid extends RenderRegion
{
	private static final float CULL_RANGE = 128.0F;
	private static final double SKIP_THRESHOLD = 0.25f; // don't render another band if there is less than this amount left
	public static final double MIN_SPACING = 1.0;
	
	private Vector3 first, second;
	private double spacing = 1.0;

	public static Render3DGrid region3dGrid(final RenderStyle style, final BoundingBox region)
	{
		final var render = new Render3DGrid(style, region.getMin(), region.getMax());
		if (region.isDynamic())
		{
			region.addObserver(render);
		}
		return render;
	}
	
	public Render3DGrid(RenderStyle style, Vector3 first, Vector3 second)
	{
		super(style);
		this.first = first;
		this.second = second;
	}
	
	@Override
	public void notifyChanged(Observable<?> source)
	{
		this.setPosition((BoundingBox)source);
	}

	public void setPosition(BoundingBox region)
	{
		this.setPosition(region.getMin(), region.getMax());
	}
	
	public void setPosition(Vector3 first, Vector3 second)
	{
		this.first = first;
		this.second = second;
	}
	
	public Render3DGrid setSpacing(double spacing)
	{
		this.spacing = spacing;
		return this;
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		final Vector3 camera = ctx.cameraPos();
		double x1 = this.first.getX() - camera.getX();
		double y1 = this.first.getY() - camera.getY();
		double z1 = this.first.getZ() - camera.getZ();
		double x2 = this.second.getX() - camera.getX();
		double y2 = this.second.getY() - camera.getY();
		double z2 = this.second.getZ() - camera.getZ();

		if (this.spacing != 1.0)
		{
			GlStateManager._disableCull();

			double[] vertices = {
					x1, y1, z1,  x2, y1, z1,  x2, y1, z2,  x1, y1, z2, // bottom
					x1, y2, z1,  x2, y2, z1,  x2, y2, z2,  x1, y2, z2, // top
					x1, y1, z1,  x1, y1, z2,  x1, y2, z2,  x1, y2, z1, // west
					x2, y1, z1,  x2, y2, z1,  x2, y2, z2,  x2, y1, z2, // east
					x1, y1, z1,  x1, y2, z1,  x2, y2, z1,  x2, y1, z1, // north
					x1, y1, z2,  x2, y1, z2,  x2, y2, z2,  x1, y2, z2  // south
			};

			for (LineStyle line : this.style.getLines())
			{
				if (ctx.apply(line, this.style.getRenderType()))
				{
					ctx.color(line, 0.25f)
						.beginQuads();
					for (int i = 0; i < vertices.length; i += 3)
					{
						ctx.vertex(vertices[i], vertices[i + 1], vertices[i + 2]);
					}
					ctx.endQuads();
				}
			}

			ctx.flush(); // todo: only needed because of disable/enable cull
			GlStateManager._enableCull();
		}
		
		if (this.spacing < Render3DGrid.MIN_SPACING)
		{
			return;
		}
		
		final double cullAt = Render3DGrid.CULL_RANGE * this.spacing;
		final double cullAtY = cullAt - Mth.frac(y1);
		final double cullAtX = cullAt - Mth.frac(x1);
		final double cullAtZ = cullAt - Mth.frac(z1);
		for (LineStyle line : this.style.getLines())
		{
			if (!ctx.apply(line, this.style.getRenderType()))
			{
				continue;
			}
			
			ctx.color(line)
				.beginLines();

			final double yEnd = Math.min(y2 + OFFSET, cullAtY);
			for (double y = Math.max(y1, -cullAtY) + OFFSET; y <= yEnd; y += this.spacing)
			{
				if (yEnd - y < SKIP_THRESHOLD)
				{
					continue;
				}

				ctx.vertex(x1, y, z2)
					.vertex(x2, y, z2)
					.vertex(x1, y, z1)
					.vertex(x2, y, z1)
					.vertex(x1, y, z1)
					.vertex(x1, y, z2)
					.vertex(x2, y, z1)
					.vertex(x2, y, z2);
			}

			final double xEnd = Math.min(x2, cullAtX);
			for (double x = Math.max(x1, -cullAtX); x <= xEnd; x += this.spacing)
			{
				if (xEnd - x < SKIP_THRESHOLD)
				{
					continue;
				}

				ctx.vertex(x, y1, z1)
					.vertex(x, y2, z1)
					.vertex(x, y1, z2)
					.vertex(x, y2, z2)
					.vertex(x, y2, z1)
					.vertex(x, y2, z2)
					.vertex(x, y1, z1)
					.vertex(x, y1, z2);
			}

			final double zEnd = Math.min(z2, cullAtZ);
			for (double z = Math.max(z1, -cullAtZ); z <= zEnd; z += this.spacing)
			{
				if (zEnd - z < SKIP_THRESHOLD)
				{
					continue;
				}

				ctx.vertex(x1, y1, z)
					.vertex(x2, y1, z)
					.vertex(x1, y2, z)
					.vertex(x2, y2, z)
					.vertex(x2, y1, z)
					.vertex(x2, y2, z)
					.vertex(x1, y1, z)
					.vertex(x1, y2, z);
			}

			ctx.endLines();
		}
	}
}
