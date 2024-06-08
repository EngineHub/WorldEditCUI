/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render;

import org.enginehub.worldeditcui.config.Colour;

/**
 * Server-defined style for multi selections
 * 
 * @author Adam Mummery-Smith
 */
public class CustomStyle implements RenderStyle
{
	private Colour colour;
	private RenderType renderType = RenderType.ANY;
	private final LineStyle[] lines = new LineStyle[2];

	public CustomStyle(Colour colour)
	{
		this.setColour(colour);
	}
	
	@Override
	public void setRenderType(RenderType renderType)
	{
		this.renderType = renderType;
	}
	
	@Override
	public RenderType getRenderType()
	{
		return this.renderType;
	}

	@Override
	public void setColour(Colour colour)
	{
		this.colour = colour;
		this.lines[0] = new LineStyle(
			 RenderType.HIDDEN,
			 LineStyle.DEFAULT_WIDTH,
			 Math.round(colour.red() * 0.75F),
			 Math.round(colour.green() * 0.75F),
			 Math.round(colour.blue() * 0.75F),
			 Math.round(colour.alpha() * 0.25F)
		);
		this.lines[1] =	new LineStyle(
			 RenderType.VISIBLE,
			 LineStyle.DEFAULT_WIDTH,
			 colour.red(),
			 colour.green(),
			 colour.blue(),
			 colour.alpha()
		);
	}
	
	@Override
	public Colour getColour()
	{
		return this.colour;
	}

	@Override
	public LineStyle[] getLines()
	{
		return this.lines;
	}
}
