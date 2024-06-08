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
import org.enginehub.worldeditcui.util.BoundingBox;
import org.enginehub.worldeditcui.util.Observable;
import org.enginehub.worldeditcui.util.Vector3;

/**
 * Stores data about a cube surrounding a block in the world. Used to store info
 * about the selector blocks. Keeps track of colour, x/y/z values, and rendering
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class PointCube extends Observable<BoundingBox>
{
	private static final double PADDING = 0.03;
	
	protected static final Vector3 MIN_VEC = new Vector3(PointCube.PADDING, PointCube.PADDING, PointCube.PADDING);
	protected static final Vector3 MAX_VEC = new Vector3(PointCube.PADDING + 1, PointCube.PADDING + 1, PointCube.PADDING + 1);

	protected int id;
	protected Vector3 point;
	protected RenderStyle style = ConfiguredColour.CUBOIDPOINT1.style();
	
	protected Render3DBox box;
	
	public PointCube(double x, double y, double z)
	{
		this(new Vector3(x, y, z));
	}
	
	public PointCube(Vector3 point)
	{
		this.setPoint(point);
	}
	
	public boolean isDynamic()
	{
		return false;
	}
	
	public PointCube setId(int id)
	{
		this.id = id;
		return this;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void render(CUIRenderContext ctx)
	{
		this.box.render(ctx);
	}

	public void updatePoint(float partialTicks)
	{
	}

	public Vector3 getPoint()
	{
		return this.point;
	}
	
	public void setPoint(Vector3 point)
	{
		this.point = point;
		this.update();
	}

	public RenderStyle getStyle()
	{
		return this.style;
	}
	
	public PointCube setStyle(RenderStyle style)
	{
		this.style = style;
		this.update();
		return this;
	}

	private void update()
	{
		this.box = new Render3DBox(this.style, this.point.subtract(PointCube.MIN_VEC), this.point.add(PointCube.MAX_VEC));
	}
}
