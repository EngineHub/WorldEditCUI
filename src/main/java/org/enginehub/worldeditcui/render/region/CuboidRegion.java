/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render.region;

import net.minecraft.world.entity.Entity;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.ConfiguredColour;
import org.enginehub.worldeditcui.render.points.PointCube;
import org.enginehub.worldeditcui.render.points.PointCubeTracking;
import org.enginehub.worldeditcui.render.shapes.Render3DBox;
import org.enginehub.worldeditcui.render.shapes.Render3DGrid;
import org.enginehub.worldeditcui.util.BoundingBox;

/**
 * Main controller for a cuboid-type region
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class CuboidRegion extends Region
{
	private final PointCube[] points = new PointCube[2];
	
	private Render3DGrid grid;
	private Render3DBox box;
	
	private double spacing = 1.0;
	
	public CuboidRegion(WorldEditCUI controller)
	{
		super(controller, ConfiguredColour.CUBOIDBOX.style(), ConfiguredColour.CUBOIDGRID.style(), ConfiguredColour.CUBOIDPOINT1.style(), ConfiguredColour.CUBOIDPOINT2.style());
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		if (this.points[0] != null && this.points[1] != null)
		{
			this.points[0].updatePoint(ctx.dt());
			this.points[1].updatePoint(ctx.dt());
			
			this.grid.render(ctx);
			this.box.render(ctx);
			
			this.points[0].render(ctx);
			this.points[1].render(ctx);
		}
		else if (this.points[0] != null)
		{
			this.points[0].updatePoint(ctx.dt());
			this.points[0].render(ctx);
		}
		else if (this.points[1] != null)
		{
			this.points[1].updatePoint(ctx.dt());
			this.points[1].render(ctx);
		}
	}
	
	@Override
	public void setGridSpacing(double spacing)
	{
		this.spacing = spacing;
		if (this.grid != null)
		{
			this.grid.setSpacing(spacing);
		}
	}
	
	@Override
	public void setCuboidPoint(int id, double x, double y, double z)
	{
		if (id < 2)
		{
			this.points[id] = PointCube.pointCube(x, y, z).setStyle(this.styles[id+2]);
		}
		
		this.updateBounds();
	}
	
	@Override
	public void setCuboidVertexLatch(int id, Entity entity, double traceDistance)
	{
		if (id < 2)
		{
			this.points[id] = PointCubeTracking.pointCubeTracking(entity, traceDistance).setStyle(this.styles[id+2]);
		}
		
		this.updateBounds();
	}

	private void updateBounds()
	{
		if (this.points[0] != null && this.points[1] != null)
		{
			BoundingBox bounds = BoundingBox.boundingBox(this.points[0], this.points[1]);
			this.grid = Render3DGrid.region3dGrid(this.styles[1], bounds).setSpacing(this.spacing);
			this.box = Render3DBox.region3dBox(this.styles[0], bounds);
		}
	}
	
	@Override
	protected void updateStyles()
	{
		if (this.box != null)
		{
			this.box.setStyle(this.styles[0]);
		}
		
		if (this.grid != null)
		{
			this.grid.setStyle(this.styles[1]);
		}
		
		if (this.points[0] != null)
		{
			this.points[0].setStyle(this.styles[2]);
		}
		
		if (this.points[1] != null)
		{
			this.points[1].setStyle(this.styles[3]);
		}
	}
	
	@Override
	public RegionType getType()
	{
		return RegionType.CUBOID;
	}
}
