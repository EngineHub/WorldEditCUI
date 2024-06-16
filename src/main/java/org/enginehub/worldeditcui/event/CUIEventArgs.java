/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.event;

import com.google.common.base.Joiner;
import org.enginehub.worldeditcui.WorldEditCUI;

import java.util.List;

/**
 * CUI communication event
 * Called when a CUI event is sent from the server.
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public final class CUIEventArgs
{
	private final WorldEditCUI controller;
	private final boolean multi;
	private final String type;
	private final List<String> params;
	
	public CUIEventArgs(WorldEditCUI controller, boolean multi, String type, List<String> params)
	{
		this.controller = controller;
		this.multi = multi;
		this.type = type;
		
		if (params.size() == 1 && params.get(0).isEmpty())
		{
			params = List.of();
		}
		
		this.params = params;
		this.controller.getDebugger().debug("CUI Event (" + type + ") - Params: " + Joiner.on(", ").join(params));
	}
	
	public WorldEditCUI getController()
	{
		return this.controller;
	}
	
	public List<String> getParams()
	{
		return this.params;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public boolean isMulti()
	{
		return this.multi;
	}
}
