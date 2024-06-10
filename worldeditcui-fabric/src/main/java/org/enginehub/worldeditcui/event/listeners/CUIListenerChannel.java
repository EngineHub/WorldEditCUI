/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.event.listeners;

import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.event.CUIEventArgs;
import org.enginehub.worldeditcui.network.CUIEventPayload;

/**
 * Listener class for incoming plugin channel messages
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class CUIListenerChannel
{
	private WorldEditCUI controller;
	
	public CUIListenerChannel(WorldEditCUI controller)
	{
		this.controller = controller;
	}
	
	public void onMessage(final CUIEventPayload message)
	{
		this.controller.getDebugger().debug("Received CUI event from server: " + message);
		
		try
		{
			CUIEventArgs eventArgs = new CUIEventArgs(this.controller, message.multi(), message.eventType(), message.args());
			this.controller.getDispatcher().raiseEvent(eventArgs);
		}
		catch (Exception ex)
		{
//			ex.printStackTrace();
		}
	}
}
