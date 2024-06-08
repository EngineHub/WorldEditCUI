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
import org.enginehub.worldeditcui.InitialisationFactory;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.exceptions.InvalidSelectionTypeException;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.render.RenderStyle.RenderType;

/**
 * Base region storage class. Provides
 * abstract methods for setting various
 * points in the region. 
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public abstract class Region implements InitialisationFactory
{
	protected final WorldEditCUI controller;
	protected final RenderStyle[] defaultStyles;
	protected RenderStyle[] styles;
	protected RenderType renderType = RenderType.ANY;
	
	protected Region(WorldEditCUI controller, RenderStyle... styles)
	{
		this.controller = controller;
		this.styles = this.defaultStyles = styles;
	}
	
	@Override
	public void initialise()
	{
	}
	
	public abstract void render(CUIRenderContext ctx);
	
	public RenderStyle[] getDefaultStyles()
	{
		return this.defaultStyles;
	}
	
	public void setRenderType(RenderType renderType)
	{
		this.renderType = renderType;
		this.updateRenderStyle();
	}
	
	public void setStyles(RenderStyle... styles)
	{
		if (styles.length < this.defaultStyles.length)
		{
			throw new IllegalArgumentException("Invalid colour palette supplied for " + this.getType().getName() + " region");
		}
		
		this.styles = styles;
		this.updateRenderStyle();
		this.updateStyles();
	}
	
	protected void updateRenderStyle()
	{
		for (RenderStyle style : this.styles)
		{
			if (style != null)
			{
				style.setRenderType(this.renderType);
			}
		}
	}

	protected abstract void updateStyles();

	public void setGridSpacing(double spacing)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setGridSpacing");
	}
	
	public void setCuboidPoint(int id, double x, double y, double z)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setCuboidPoint");
	}
	
	public void setCuboidVertexLatch(int id, Entity entity, double traceDistance)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setCuboidVertexLatch");
	}
	
	public void setPolygonPoint(int id, int x, int z)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setPolygonPoint");
	}
	
	public void setEllipsoidCenter(int x, int y, int z)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setEllipsoidCenter");
	}
	
	public void setEllipsoidRadii(double x, double y, double z)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setEllipsoidRadii");
	}
	
	public void setMinMax(int min, int max)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setMinMax");
	}
	
	public void setCylinderCenter(int x, int y, int z)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setCylinderCenter");
	}
	
	public void setCylinderRadius(double x, double z)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "setCylinderRadius");
	}
	
	public void addPolygon(int[] vertexIds)
	{
		throw new InvalidSelectionTypeException(this.getType().getName(), "addPolygon");
	}
	
	public abstract RegionType getType();
}
