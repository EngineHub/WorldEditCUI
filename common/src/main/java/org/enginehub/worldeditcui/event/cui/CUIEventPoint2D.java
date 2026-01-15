/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.event.cui;

import org.enginehub.worldeditcui.event.CUIEventArgs;
import org.enginehub.worldeditcui.event.CUIEventType;
import org.enginehub.worldeditcui.render.region.Region;

/**
 * Called when poly point event is received
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIEventPoint2D extends CUIEventPoint3D
{
	public CUIEventPoint2D(CUIEventArgs args)
	{
		super(args);
	}
	
	@Override
	public CUIEventType getEventType()
	{
		return CUIEventType.POINT2D;
	}
	
	@Override
	public String raise()
	{
		Region selection = this.controller.getSelection(this.multi);
		if (selection == null)
		{
			this.controller.getDebugger().debug("No active multi selection.");
			return null;
		}

		int id = this.getInt(0);
		int x = this.getInt(1);
		int z = this.getInt(2);
		@SuppressWarnings("unused")
		int regionSize = this.getInt(3);
		selection.setPolygonPoint(id, x, z);
		
		this.controller.getDebugger().debug("Setting point2d #" + id);
		
		return null;
	}
}
