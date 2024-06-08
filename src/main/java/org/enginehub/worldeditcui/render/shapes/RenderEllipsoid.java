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
import org.enginehub.worldeditcui.util.Vector3;

/**
 * Draws an ellipsoid shape around a centre point.
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class RenderEllipsoid extends RenderRegion
{

	protected final static double TAU = Math.PI * 2.0;
	/**
	 * The number of intervals to draw in around the ellipsoid.
	 */
	protected static final double SUBDIVISIONS = 40;
	
	protected PointCube centre;
	private final Vector3 radii;
	
	protected final double centreX, centreY, centreZ;
	
	public RenderEllipsoid(RenderStyle style, PointCube centre, Vector3 radii)
	{
		super(style);
		this.centre = centre;
		this.radii = radii;
		this.centreX = centre.getPoint().getX() + 0.5;
		this.centreY = centre.getPoint().getY() + 0.5;
		this.centreZ = centre.getPoint().getZ() + 0.5;
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		ctx.flush();
		ctx.poseStack().pushPose();
		ctx.poseStack().translate(this.centreX - ctx.cameraPos().getX(), this.centreY - ctx.cameraPos().getY(), this.centreZ - ctx.cameraPos().getZ());
		ctx.applyMatrices();

		for (LineStyle line : this.style.getLines())
		{
			if (ctx.apply(line, this.style.getRenderType()))
			{
				ctx.color(line);
				this.drawXZPlane(ctx);
				this.drawYZPlane(ctx);
				this.drawXYPlane(ctx);
			}
		}

		ctx.flush();
		ctx.poseStack().popPose();
		ctx.applyMatrices();
	}
	
	protected void drawXZPlane(final CUIRenderContext ctx)
	{
		int yRad = (int)Math.floor(this.radii.getY());
		for (int yBlock = -yRad; yBlock < yRad; yBlock++)
		{

			ctx.beginLineLoop();
			for (int i = 0; i <= SUBDIVISIONS; i++)
			{
				double tempTheta = i * TAU / SUBDIVISIONS;
				double tempX = this.radii.getX() * Math.cos(tempTheta) * Math.cos(Math.asin(yBlock / this.radii.getY()));
				double tempZ = this.radii.getZ() * Math.sin(tempTheta) * Math.cos(Math.asin(yBlock / this.radii.getY()));
				
				ctx.vertex(tempX, yBlock, tempZ);
			}
			ctx.endLineLoop();
		}

		ctx.beginLineLoop();
		for (int i = 0; i <= SUBDIVISIONS; i++)
		{
			double tempTheta = i * TAU / SUBDIVISIONS;
			double tempX = this.radii.getX() * Math.cos(tempTheta);
			double tempZ = this.radii.getZ() * Math.sin(tempTheta);
			
			ctx.vertex(tempX, 0.0, tempZ);
		}
		ctx.endLineLoop();
	}
	
	protected void drawYZPlane(final CUIRenderContext ctx)
	{
		int xRad = (int)Math.floor(this.radii.getX());
		for (int xBlock = -xRad; xBlock < xRad; xBlock++)
		{
			ctx.beginLineLoop();
			for (int i = 0; i <= SUBDIVISIONS; i++)
			{
				double tempTheta = i * TAU / SUBDIVISIONS;
				double tempY = this.radii.getY() * Math.cos(tempTheta) * Math.sin(Math.acos(xBlock / this.radii.getX()));
				double tempZ = this.radii.getZ() * Math.sin(tempTheta) * Math.sin(Math.acos(xBlock / this.radii.getX()));
				
				ctx.vertex(xBlock, tempY, tempZ);
			}
			ctx.endLineLoop();
		}
		
		ctx.beginLineLoop();
		for (int i = 0; i <= SUBDIVISIONS; i++)
		{
			double tempTheta = i * TAU / SUBDIVISIONS;
			double tempY = this.radii.getY() * Math.cos(tempTheta);
			double tempZ = this.radii.getZ() * Math.sin(tempTheta);
			
			ctx.vertex(0.0, tempY, tempZ);
		}
		ctx.endLineLoop();
	}
	
	protected void drawXYPlane(final CUIRenderContext ctx)
	{
		int zRad = (int)Math.floor(this.radii.getZ());
		for (int zBlock = -zRad; zBlock < zRad; zBlock++)
		{
			ctx.beginLineLoop();
			for (int i = 0; i <= SUBDIVISIONS; i++)
			{
				double tempTheta = i * TAU / SUBDIVISIONS;
				double tempX = this.radii.getX() * Math.sin(tempTheta) * Math.sin(Math.acos(zBlock / this.radii.getZ()));
				double tempY = this.radii.getY() * Math.cos(tempTheta) * Math.sin(Math.acos(zBlock / this.radii.getZ()));
				
				ctx.vertex(tempX, tempY, zBlock);
			}
			ctx.endLineLoop();
		}

		ctx.beginLineLoop();
		for (int i = 0; i <= SUBDIVISIONS; i++)
		{
			double tempTheta = i * TAU / SUBDIVISIONS;
			double tempX = this.radii.getX() * Math.cos(tempTheta);
			double tempY = this.radii.getY() * Math.sin(tempTheta);
			
			ctx.vertex(tempX, tempY, 0.0);
		}
		ctx.endLineLoop();
	}
}
