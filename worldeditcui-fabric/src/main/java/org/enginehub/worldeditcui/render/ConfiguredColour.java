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

import net.minecraft.client.resources.language.I18n;
import org.enginehub.worldeditcui.config.Colour;
import org.enginehub.worldeditcui.render.RenderStyle.RenderType;

/**
 * Stores style data for each type of line.
 * 
 * Each line has a normal line, and a hidden line.
 * The normal line has an alpha value of 0.8f, and
 * the hidden line has an alpha value of 0.2f. They
 * both have a thickness of 3.0f.
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public enum ConfiguredColour
{
	CUBOIDGRID     ("cuboidgrid",     Colour.parseRgba("#CC4C4CCC")),
	CUBOIDBOX      ("cuboidedge",     Colour.parseRgba("#CC3333CC")),
	CUBOIDPOINT1   ("cuboidpoint1",   Colour.parseRgba("#33CC33CC")),
	CUBOIDPOINT2   ("cuboidpoint2",   Colour.parseRgba("#3333CCCC")),
	POLYGRID       ("polygrid",       Colour.parseRgba("#CC3333CC")),
	POLYBOX        ("polyedge",       Colour.parseRgba("#CC4C4CCC")),
	POLYPOINT      ("polypoint",      Colour.parseRgba("#33CCCCCC")),
	ELLIPSOIDGRID  ("ellipsoidgrid",  Colour.parseRgba("#CC4C4CCC")),
	ELLIPSOIDCENTRE("ellipsoidpoint", Colour.parseRgba("#CCCC33CC")),
	CYLINDERGRID   ("cylindergrid",   Colour.parseRgba("#CC3333CC")),
	CYLINDERBOX    ("cylinderedge",   Colour.parseRgba("#CC4C4CCC")),
	CYLINDERCENTRE ("cylinderpoint",  Colour.parseRgba("#CC33CCCC")),
	CHUNKBOUNDARY  ("chunkboundary",  Colour.parseRgba("#33CC33CC")),
	CHUNKGRID      ("chunkgrid",      Colour.parseRgba("#4CCCAA99"));
	
	class Style implements RenderStyle
	{
		private RenderType renderType = RenderType.ANY;
		
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
		}

		@Override
		public Colour getColour()
		{
			return ConfiguredColour.this.getColour();
		}

		@Override
		public LineStyle[] getLines()
		{
			return ConfiguredColour.this.getLines();
		}
	}
	
	private final String displayName;
	private final Colour defaultColour;
	private Colour colour;
	private LineStyle normal, hidden;
	private LineStyle[] lines;
	
	private ConfiguredColour(String displayName, Colour colour)
	{
		this.displayName = displayName;
		this.colour = colour;
		this.defaultColour = colour;
		this.updateLines();
	}
	
	public String getDisplayName()
	{
		return I18n.get("worldeditcui.color." + this.displayName);
	}
	
	public RenderStyle style()
	{
		return new Style();
	}
	
	public void setColour(Colour colour)
	{
		this.colour = colour;
		this.updateLines();
	}

	public Colour getColour()
	{
		return this.colour;
	}
	
	public LineStyle getHidden()
	{
		return this.hidden;
	}
	
	public LineStyle getNormal()
	{
		return this.normal;
	}
	
	public LineStyle[] getLines()
	{
		return this.lines;
	}
	
	public void setDefault()
	{
	    this.colour = this.defaultColour;
		this.updateLines();
	}
	
	public Colour getDefault()
	{
		return this.defaultColour;
	}
	
	public void setColourIntRGBA(int argb)
	{
		this.colour = new Colour(argb);
		this.updateLines();
	}
	
	public int getColourIntARGB()
	{
		return this.colour.argb();
	}

	private void updateLines()
	{
		this.hidden = new LineStyle(
				RenderType.HIDDEN,
				LineStyle.DEFAULT_WIDTH,
				Math.round(this.colour.red() * 0.75f),
				Math.round(this.colour.green() * 0.75F),
				Math.round(this.colour.blue() * 0.75F),
				Math.round(this.colour.alpha() * 0.25F));
		this.normal = new LineStyle(
				RenderType.VISIBLE,
				LineStyle.DEFAULT_WIDTH,
				this.colour.red(),
				this.colour.green(),
				this.colour.blue(),
				this.colour.alpha());
		this.lines = new LineStyle[] { this.hidden, this.normal };
	}
}
