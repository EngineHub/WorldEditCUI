package com.mumfrey.worldeditcui.render;

import com.mumfrey.worldeditcui.render.RenderStyle.RenderType;

/**
 * Stores data about a line that can be rendered
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class LineStyle
{
	public static final float DEFAULT_WIDTH = 6.0f;

	public final float lineWidth;
	public final int red, green, blue, alpha;
	public final RenderType renderType;
	
	public LineStyle(RenderType renderType, float lineWidth, int red, int green, int blue)
	{
		this(renderType, lineWidth, red, green, blue, 0xff);
	}
	
	public LineStyle(RenderType renderType, float lineWidth, int red, int green, int blue, int alpha)
	{
		this.lineWidth = lineWidth;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.renderType = renderType;
	}
}
