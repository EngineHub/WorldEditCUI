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
import org.enginehub.worldeditcui.util.BoundingBox;
import org.enginehub.worldeditcui.util.Observable;
import org.enginehub.worldeditcui.util.Vector3;

/**
 * Draws a rectangular prism around 2 corners
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class Render3DBox extends RenderRegion
{
	private Vector3 first, second;
	
	public Render3DBox(RenderStyle style, BoundingBox region)
	{
		this(style, region.getMin(), region.getMax());
		if (region.isDynamic())
		{
			region.addObserver(this);
		}
	}
	
	public Render3DBox(RenderStyle style, Vector3 first, Vector3 second)
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
		
		for (LineStyle line : this.style.getLines())
		{
			if (!ctx.apply(line, this.style.getRenderType()))
			{
				continue;
			}
			
			// Draw bottom face
			ctx.color(line);
			ctx.beginLineLoop()
				.vertex(x1, y1, z1)
				.vertex(x2, y1, z1)
				.vertex(x2, y1, z2)
				.vertex(x1, y1, z2)
				.endLineLoop();

			// Draw top face
			ctx.beginLineLoop()
			.vertex(x1, y2, z1)
			.vertex(x2, y2, z1)
			.vertex(x2, y2, z2)
			.vertex(x1, y2, z2)
			.endLineLoop();

			// Draw join top and bottom faces
			ctx.beginLines()
				.vertex(x1, y1, z1)
				.vertex(x1, y2, z1)

				.vertex(x2, y1, z1)
				.vertex(x2, y2, z1)

				.vertex(x2, y1, z2)
				.vertex(x2, y2, z2)

				.vertex(x1, y1, z2)
				.vertex(x1, y2, z2)
				.endLines();
		}
	}
}
