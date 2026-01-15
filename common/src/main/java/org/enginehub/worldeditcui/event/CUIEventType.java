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

import org.enginehub.worldeditcui.event.cui.CUIEventBounds;
import org.enginehub.worldeditcui.event.cui.CUIEventColour;
import org.enginehub.worldeditcui.event.cui.CUIEventCylinder;
import org.enginehub.worldeditcui.event.cui.CUIEventEllipsoid;
import org.enginehub.worldeditcui.event.cui.CUIEventGrid;
import org.enginehub.worldeditcui.event.cui.CUIEventPoint2D;
import org.enginehub.worldeditcui.event.cui.CUIEventPoint3D;
import org.enginehub.worldeditcui.event.cui.CUIEventPolygon;
import org.enginehub.worldeditcui.event.cui.CUIEventSelection;
import org.enginehub.worldeditcui.event.cui.CUIEventUpdate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Event type enum for CUI events. Also stores class, arguments, and key for each value.
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public enum CUIEventType
{
	SELECTION(CUIEventSelection::new, "Selection", "s", 1, 2),
	POINT(CUIEventPoint3D::new, "Point3D", "p", 5, 6),
	POINT2D(CUIEventPoint2D::new, "Point2D", "p2", 4, 5),
	ELLIPSOID(CUIEventEllipsoid::new, "Ellipsoid", "e", 4),
	CYLINDER(CUIEventCylinder::new, "Cylinder", "cyl", 5),
	MINMAX(CUIEventBounds::new, "Bounds", "mm", 2),
	UPDATE(CUIEventUpdate::new, "Update", "u", 1),
	POLYGON(CUIEventPolygon::new, "Polygon", "poly", 3, 99),
	COLOUR(CUIEventColour::new, "Colour", "col", 4),
	GRID(CUIEventGrid::new, "Grid", "grid", 1, 2);


	private final Function<CUIEventArgs, CUIEvent> maker;
	private final String key;
	private final String name;
	private final int minParams;
	private final int maxParams;
	
	private CUIEventType(Function<CUIEventArgs, CUIEvent> maker, String name, String key, int minParams, int maxParams)
	{
		this.maker = maker;
		this.name = name;
		this.key = key;
		this.minParams = minParams;
		this.maxParams = maxParams;
	}
	
	private CUIEventType(Function<CUIEventArgs, CUIEvent> maker, String name, String key, int paramCount)
	{
		this(maker, name, key, paramCount, paramCount);
	}

	public CUIEvent make(final CUIEventArgs args) {
		return this.maker.apply(args);
	}

	public String getKey()
	{
		return this.key;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int getMaxParameters()
	{
		return this.maxParams;
	}
	
	public int getMinParameters()
	{
		return this.minParams;
	}

	private static final Map<String, CUIEventType> BY_NAME = new HashMap<>();

	static {
		for(CUIEventType type : values())
		{
			BY_NAME.put(type.getKey(), type);
		}
	}

	/**
	 * Get a CUI event type by key.
	 *
	 * @param key protocol key
	 * @return the appropriate event type, or null if none found
	 */
	public static CUIEventType named(final String key) {
		return BY_NAME.get(key);
	}
}
