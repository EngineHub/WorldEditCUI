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

import org.enginehub.worldeditcui.render.RenderStyle.RenderType;

/**
 * Stores data about a line that can be rendered
 *
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class LineStyle
{
	public static final float DEFAULT_WIDTH = 3.0f;

	public final float lineWidth;
	public final int red, green, blue, alpha;
	public final RenderType renderType;

	public LineStyle(final RenderType renderType, final float lineWidth, final int red, final int green, final int blue)
	{
		this(renderType, lineWidth, red, green, blue, 0xff);
	}

	public LineStyle(final RenderType renderType, final float lineWidth, final int red, final int green, final int blue, final int alpha)
	{
		this.lineWidth = lineWidth;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.renderType = renderType;
	}
}
