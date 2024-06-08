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

import java.util.UUID;

/**
 * Called when selection event is received
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIEventSelection extends CUIEvent
{
	public CUIEventSelection(CUIEventArgs args)
	{
		super(args);
	}
	
	@Override
	public CUIEventType getEventType()
	{
		return CUIEventType.SELECTION;
	}
	
	@Override
	public String raise()
	{
		String key = this.getString(0);
		Region selection = this.controller.getSelectionProvider().createSelection(key);
		
		if (selection != null)
		{
			selection.initialise();
		}
		
		UUID id = null;
		if (this.multi)
		{
			if (selection == null && this.params.length < 2)
			{
				this.controller.getDebugger().debug("Received clear selection event.");
				this.controller.clearRegions();
				return null;
			}
			
			id = UUID.fromString(this.getString(1));
		}
		
		this.controller.setSelection(id, selection);
		this.controller.getDebugger().debug("Received selection event, initializing new region instance.");
		
		return null;
	}
}
