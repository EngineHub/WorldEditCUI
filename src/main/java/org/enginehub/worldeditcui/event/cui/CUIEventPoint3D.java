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

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.enginehub.worldeditcui.event.CUIEvent;
import org.enginehub.worldeditcui.event.CUIEventArgs;
import org.enginehub.worldeditcui.event.CUIEventType;
import org.enginehub.worldeditcui.render.region.Region;

/**
 * Called when point event is received
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIEventPoint3D extends CUIEvent
{
	public CUIEventPoint3D(CUIEventArgs args)
	{
		super(args);
	}
	
	@Override
	public CUIEventType getEventType()
	{
		return CUIEventType.POINT;
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
		
		if (this.multi && "~".equals(this.getString(1)) && "~".equals(this.getString(2)) && "~".equals(this.getString(3)))
		{
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.getCameraEntity();
			double hitDistance = mc.gameMode.getPickRange();
			
			selection.setCuboidVertexLatch(id, entity, Math.min(Math.max(this.getDouble(4), hitDistance), 256.0));
			this.controller.getDebugger().debug("Setting vertex latch #" + id);
			return null;
		}
		
		double x = this.getDouble(1);
		double y = this.getDouble(2);
		double z = this.getDouble(3);
		
		selection.setCuboidPoint(id, x, y, z);
		this.controller.getDebugger().debug("Setting point #" + id);
		
		return null;
	}
}
