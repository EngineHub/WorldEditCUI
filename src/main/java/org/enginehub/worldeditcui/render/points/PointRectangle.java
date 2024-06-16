/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render.points;

import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.ConfiguredColour;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.render.shapes.Render3DBox;
import org.enginehub.worldeditcui.util.Vector2;

/**
 * Stores data about a prism surrounding two blocks in the world. Used to store
 * info about the selector blocks for polys. Keeps track of colour, x/y/z
 * values, and rendering.
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class PointRectangle
{
	private static final double OFF = 0.03;
	private static final Vector2 MIN_VEC = new Vector2(PointRectangle.OFF, PointRectangle.OFF);
	private static final Vector2 MAX_VEC = new Vector2(PointRectangle.OFF + 1, PointRectangle.OFF + 1);
	
	protected Vector2 point;
	protected RenderStyle style = ConfiguredColour.POLYPOINT.style();
	
	private int min, max;
	
	private Render3DBox box;
	
	public static PointRectangle pointRectangle(final int x, final int z)
	{
		return new PointRectangle(new Vector2(x, z));
	}
	
	public static PointRectangle pointRectangle(final Vector2 point)
	{
		final var ret = new PointRectangle(point);
		ret.update();
		return ret;
	}

	private PointRectangle(final Vector2 pt) {
		this.point = pt;
	}
	
	public void render(CUIRenderContext ctx)
	{
		this.box.render(ctx);
	}
	
	public Vector2 getPoint()
	{
		return this.point;
	}
	
	public void setPoint(Vector2 point)
	{
		this.point = point;
	}
	
	public RenderStyle getStyle()
	{
		return this.style;
	}
	
	public void setStyle(RenderStyle style)
	{
		this.style = style;
	}
	
	public void setMinMax(int min, int max)
	{
		this.min = min;
		this.max = max;
		this.update();
	}

	public int getMin()
	{
		return this.min;
	}

	public int getMax()
	{
		return this.max;
	}
	
	private void update()
	{
		this.box = new Render3DBox(this.style, this.point.subtract(PointRectangle.MIN_VEC).toVector3(this.min - 0.03f), this.point.add(PointRectangle.MAX_VEC).toVector3(this.max + 1 + 0.03f));
	}
}
