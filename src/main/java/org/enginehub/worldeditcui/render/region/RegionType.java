/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render.region;

import org.enginehub.worldeditcui.WorldEditCUI;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The different types of regions and the classes which we use to display them
 * 
 * @author Adam Mummery-Smith
 */
public enum RegionType
{
	CUBOID    ("cuboid",     "Cuboid",     CuboidRegion::new),
	POLYGON   ("polygon2d",  "2D Polygon", PolygonRegion::new),
	ELLIPSOID ("ellipsoid",  "Ellipsoid",  EllipsoidRegion::new),
	CYLINDER  ("cylinder",   "Cylinder",   CylinderRegion::new),
	POLYHEDRON("polyhedron", "Polyhedron", PolyhedronRegion::new);

	private static final Map<String, RegionType> BY_NAME = new HashMap<>();
	
	private final String key;
	
	private final String name;

	private final Function<WorldEditCUI, Region> maker;

	private RegionType(String key, String name, Function<WorldEditCUI, Region> maker)
	{
		this.key = key;
		this.name = name;
		this.maker = maker;
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public String getName()
	{
		return this.name;
	}

	public Region make(WorldEditCUI controller) {
		return this.maker.apply(controller);
	}


	static {
		for (RegionType type : values()) {
			BY_NAME.put(type.getKey(), type);
		}
	}

	public static RegionType named(String key) {
		return BY_NAME.get(key);
	}
}
