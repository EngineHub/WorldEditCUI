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

import org.enginehub.worldeditcui.event.CUIEvent;
import org.enginehub.worldeditcui.event.CUIEventArgs;
import org.enginehub.worldeditcui.event.CUIEventType;
import org.enginehub.worldeditcui.render.region.Region;

/**
 * Called when ellipsoid event is received
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIEventEllipsoid extends CUIEvent
{
	public CUIEventEllipsoid(CUIEventArgs args)
	{
		super(args);
	}
	
	@Override
	public CUIEventType getEventType()
	{
		return CUIEventType.ELLIPSOID;
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
		
		if (id == 0)
		{
			int x = this.getInt(1);
			int y = this.getInt(2);
			int z = this.getInt(3);
			selection.setEllipsoidCenter(x, y, z);
		}
		else if (id == 1)
		{
			double x = this.getDouble(1);
			double y = this.getDouble(2);
			double z = this.getDouble(3);
			selection.setEllipsoidRadii(x, y, z);
		}
		
		this.controller.getDebugger().debug("Setting centre/radius");
		
		return null;
	}
}
