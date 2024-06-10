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
import org.enginehub.worldeditcui.render.points.PointCube;

/**
 * Draws the grid lines around a cylindrical region
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class RenderCylinderGrid extends RenderRegion
{
	private final double radX;
	private final double radZ;
	private final int minY;
	private final int maxY;
	private final double centreX;
	private final double centreZ;
	
	public RenderCylinderGrid(RenderStyle style, PointCube centre, double radX, double radZ, int minY, int maxY)
	{
		super(style);
		this.radX = radX;
		this.radZ = radZ;
		this.minY = minY;
		this.maxY = maxY;
		this.centreX = centre.getPoint().getX() + 0.5;
		this.centreZ = centre.getPoint().getZ() + 0.5;
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		double xPos = this.centreX - ctx.cameraPos().getX();
		double zPos = this.centreZ - ctx.cameraPos().getZ();

		for (LineStyle line : this.style.getLines())
		{
			if (!ctx.apply(line, this.style.getRenderType()))
			{
				continue;
			}
			
			int tmaxY = this.maxY + 1;
			int tminY = this.minY;
			int posRadiusX = (int)Math.ceil(this.radX);
			int negRadiusX = (int)-Math.ceil(this.radX);
			int posRadiusZ = (int)Math.ceil(this.radZ);
			int negRadiusZ = (int)-Math.ceil(this.radZ);
			final double cameraY = ctx.cameraPos().getY();

			ctx.color(line);
			for (double tempX = negRadiusX; tempX <= posRadiusX; ++tempX)
			{
				double tempZ = this.radZ * Math.cos(Math.asin(tempX / this.radX));
				ctx.beginLineLoop()
					.vertex(xPos + tempX, tmaxY - cameraY, zPos + tempZ)
					.vertex(xPos + tempX, tmaxY - cameraY, zPos - tempZ)
					.vertex(xPos + tempX, tminY - cameraY, zPos - tempZ)
					.vertex(xPos + tempX, tminY - cameraY, zPos + tempZ)
					.endLineLoop();
			}
			
			for (double tempZ = negRadiusZ; tempZ <= posRadiusZ; ++tempZ)
			{
				double tempX = this.radX * Math.sin(Math.acos(tempZ / this.radZ));
				ctx.beginLineLoop()
					.vertex(xPos + tempX, tmaxY - cameraY, zPos + tempZ)
					.vertex(xPos - tempX, tmaxY - cameraY, zPos + tempZ)
					.vertex(xPos - tempX, tminY - cameraY, zPos + tempZ)
					.vertex(xPos + tempX, tminY - cameraY, zPos + tempZ)
					.endLineLoop();
			}
		}
	}
}
