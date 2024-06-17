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
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.util.Observable;
import org.enginehub.worldeditcui.util.Observer;

/**
 * Base class for region renderers
 * 
 * @author Adam Mummery-Smith
 */
public abstract class RenderRegion implements Observer
{
	protected static final double OFFSET = 0.001d; // to avoid z-fighting with blocks

	protected RenderStyle style;
	
	protected RenderRegion(RenderStyle style)
	{
		this.style = style;
	}

	public final void setStyle(RenderStyle style)
	{
		this.style = style;
	}
	
	public abstract void render(CUIRenderContext ctx);
	
	@Override
	public void notifyChanged(Observable<?> source)
	{
		
	}
}
