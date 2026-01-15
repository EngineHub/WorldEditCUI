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

import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.render.points.PointRectangle;
import org.enginehub.worldeditcui.util.Vector2;

import java.util.List;

/**
 * Draws the top and bottom rings of a polygon region
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class Render2DBox extends RenderRegion
{
	private final List<PointRectangle> points;
	private final int min, max;
	
	public Render2DBox(RenderStyle style, List<PointRectangle> points, int min, int max)
	{
		super(style);
		this.points = points;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		double off = 0.03 - ctx.cameraPos().getY();
		for (LineStyle line : this.style.getLines())
		{
			if (!ctx.apply(line, this.style.getRenderType()))
			{
				continue;
			}

			ctx.beginLines();
			ctx.color(line);

			for (PointRectangle point : this.points)
			{
				if (point != null)
				{
					Vector2 pos = point.getPoint();
					double x = pos.getX() - ctx.cameraPos().getX();
					double z = pos.getY() - ctx.cameraPos().getZ();
					ctx.vertex(x + 0.5, this.min + off, z + 0.5)
						.vertex(x + 0.5, this.max + 1 + off, z + 0.5);
				}
			}
			ctx.endLines();
		}
	}
}
