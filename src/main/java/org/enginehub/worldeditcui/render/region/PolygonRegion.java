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

import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.ConfiguredColour;
import org.enginehub.worldeditcui.render.points.PointRectangle;
import org.enginehub.worldeditcui.render.shapes.Render2DBox;
import org.enginehub.worldeditcui.render.shapes.Render2DGrid;

import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for a polygon-type region
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class PolygonRegion extends Region
{
	private final List<PointRectangle> points = new ArrayList<>();
	private int min, max;
	
	private Render2DBox box;
	private Render2DGrid grid;
	
	public PolygonRegion(WorldEditCUI controller)
	{
		super(controller, ConfiguredColour.POLYBOX.style(), ConfiguredColour.POLYGRID.style(), ConfiguredColour.POLYPOINT.style());
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		if (this.points.size() < 1)
		{
			return;
		}
		
		for (PointRectangle point : this.points)
		{
			if (point != null)
			{
				point.render(ctx);
			}
		}
		
		this.box.render(ctx);
		this.grid.render(ctx);
	}
	
	@Override
	public void setMinMax(int min, int max)
	{
		this.min = min;
		this.max = max;
		this.update();
	}
	
	@Override
	public void setPolygonPoint(int id, int x, int z)
	{
		PointRectangle point = new PointRectangle(x, z);
		point.setStyle(this.styles[0]);
		point.setMinMax(this.min, this.max);
		
		if (id < this.points.size())
		{
			this.points.set(id, point);
		}
		else
		{
			for (int i = 0; i < id - this.points.size(); i++)
			{
				this.points.add(null);
			}
			this.points.add(point);
		}
		this.update();
	}
	
	private void update()
	{
		if (this.points.size() <= 0)
		{
			return;
		}
		
		for (PointRectangle point : this.points)
		{
			if (point != null)
			{
				point.setMinMax(this.min, this.max);
			}
		}
		
		this.box = new Render2DBox(this.styles[0], this.points, this.min, this.max);
		this.grid = new Render2DGrid(this.styles[1], this.points, this.min, this.max);
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
		
		for (PointRectangle point : this.points)
		{
			point.setStyle(this.styles[0]);
		}
	}

	@Override
	public RegionType getType()
	{
		return RegionType.POLYGON;
	}
}

