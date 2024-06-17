/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui;

import net.minecraft.client.Minecraft;
import org.enginehub.worldeditcui.config.CUIConfiguration;
import org.enginehub.worldeditcui.debug.CUIDebug;
import org.enginehub.worldeditcui.event.CUIEventDispatcher;
import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.exceptions.InitialisationException;
import org.enginehub.worldeditcui.render.CUISelectionProvider;
import org.enginehub.worldeditcui.render.ConfiguredColour;
import org.enginehub.worldeditcui.render.region.CuboidRegion;
import org.enginehub.worldeditcui.render.region.Region;
import org.enginehub.worldeditcui.render.shapes.RenderChunkBoundary;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main controller class. Uses a pseudo-JavaBeans paradigm. The only real
 * logic here is listener registration.
 * 
 * TODO: Preview mode
 * TODO: Command transactions
 * TODO: Add ability to flash selection
 *  
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class WorldEditCUI
{
	private final Map<UUID, Region> regions = new LinkedHashMap<>();
	private Region selection, activeRegion;
	private CUIDebug debugger;
	private CUIConfiguration configuration;
	private CUIEventDispatcher dispatcher;
	private CUISelectionProvider selectionProvider;
	private RenderChunkBoundary chunkBorderRenderer;
	private boolean chunkBorders;
	
	public void initialise(Minecraft minecraft)
	{
		this.selection = new CuboidRegion(this);
		this.configuration = CUIConfiguration.create();
		this.debugger = new CUIDebug(this);
		this.dispatcher = new CUIEventDispatcher(this);
		this.selectionProvider = new CUISelectionProvider(this);

		try
		{
			this.selection.initialise();
			this.configuration.initialise();
			this.debugger.initialise();
			this.dispatcher.initialise();
			this.selectionProvider.initialise();
		}
		catch (InitialisationException e)
		{
			e.printStackTrace();
			return;
		}
		
		this.chunkBorderRenderer = new RenderChunkBoundary(ConfiguredColour.CHUNKBOUNDARY.style(), ConfiguredColour.CHUNKGRID.style(), minecraft);
	}

	public CUIEventDispatcher getDispatcher()
	{
		return this.dispatcher;
	}
	
	public CUISelectionProvider getSelectionProvider()
	{
		return this.selectionProvider;
	}

	public CUIConfiguration getConfiguration()
	{
		return this.configuration;
	}
	
	public CUIDebug getDebugger()
	{
		return this.debugger;
	}
	
	public void clear()
	{
		this.clearSelection();
		this.clearRegions();
	}
	
	public void clearSelection()
	{
		this.selection = new CuboidRegion(this);
	}
	
	public void clearRegions()
	{
		this.activeRegion = null;
		this.regions.clear();
	}
	
	public Region getSelection(boolean multi)
	{
		return multi ? this.activeRegion : this.selection;
	}
	
	public void setSelection(UUID id, Region region)
	{
		if (id == null)
		{
			this.selection = region;
			return;
		}

		if (region == null)
		{
			this.regions.remove(id);
			this.activeRegion = null;
			return;
		}
		
		this.regions.put(id, region);
		this.activeRegion = region;
	}

	public void renderSelections(final CUIRenderContext ctx)
	{
		if (this.selection != null)
		{
			this.selection.render(ctx);
		}
		
		for (Region region : this.regions.values())
		{
			region.render(ctx);
		}
		
		if (this.chunkBorders)
		{
			this.chunkBorderRenderer.render(ctx);
		}
	}

	public void toggleChunkBorders()
	{
		this.chunkBorders = !this.chunkBorders;
	}
}
