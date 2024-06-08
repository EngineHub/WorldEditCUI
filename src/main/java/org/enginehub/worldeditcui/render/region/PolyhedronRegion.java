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
import org.enginehub.worldeditcui.event.listeners.CUIRenderContext;
import org.enginehub.worldeditcui.render.ConfiguredColour;
import org.enginehub.worldeditcui.render.points.PointCube;
import org.enginehub.worldeditcui.render.shapes.Render3DPolygon;
import org.enginehub.worldeditcui.util.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for a polygon-type region
 * 
 * @author TomyLobo
 * @author Adam Mummery-Smith
 */
public class PolyhedronRegion extends Region
{
	private static final Vector3 HALF = new Vector3(0.5, 0.5, 0.5);
	
	private final List<PointCube> vertices = new ArrayList<>();
	private final List<Vector3[]> faces = new ArrayList<>();
	
	private final List<Render3DPolygon> faceRenders = new ArrayList<>();
	
	public PolyhedronRegion(WorldEditCUI controller)
	{
		super(controller, ConfiguredColour.POLYBOX.style(), ConfiguredColour.POLYPOINT.style(), ConfiguredColour.CUBOIDPOINT1.style());
	}
	
	@Override
	public void render(CUIRenderContext ctx)
	{
		for (PointCube vertex : this.vertices)
		{
			vertex.render(ctx);
		}
		
		for (Render3DPolygon face : this.faceRenders)
		{
			face.render(ctx);
		}
	}

	@Override
	public void setCuboidPoint(int id, double x, double y, double z)
	{
		final PointCube vertex = new PointCube(x, y, z).setId(id);
		vertex.setStyle(id == 0 ? this.styles[2] : this.styles[1]);
		
		if (id < this.vertices.size())
		{
			this.vertices.set(id, vertex);
		}
		else
		{
			for (int i = 0; i < id - this.vertices.size(); i++)
			{
				this.vertices.add(null);
			}
			this.vertices.add(vertex);
		}
	}
	
	@Override
	public void addPolygon(int[] vertexIds)
	{
		final Vector3[] face = new Vector3[vertexIds.length];
		for (int i = 0; i < vertexIds.length; ++i)
		{
			final PointCube vertex = this.vertices.get(vertexIds[i]);
			if (vertex == null)
			{
				// This should never happen
				return;
			}
			
			face[i] = vertex.getPoint().add(HALF);
		}
		this.faces.add(face);
		this.update();
	}
	
	private void update()
	{
		this.faceRenders.clear();
		
		for (Vector3[] face : this.faces)
		{
			this.faceRenders.add(new Render3DPolygon(this.styles[0], face));
		}
	}
	
	@Override
	protected void updateStyles()
	{
		for (PointCube vertex : this.vertices)
		{
			vertex.setStyle(vertex.getId() == 0 ? this.styles[2] : this.styles[1]);
		}
		
		for (Render3DPolygon face : this.faceRenders)
		{
			face.setStyle(this.styles[0]);
		}
	}

	@Override
	public RegionType getType()
	{
		return RegionType.POLYHEDRON;
	}
}
