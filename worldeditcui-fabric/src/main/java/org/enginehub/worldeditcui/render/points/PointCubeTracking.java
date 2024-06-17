/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render.points;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.enginehub.worldeditcui.util.Vector3;

/**
 * A PointCube which tracks the specified entity location
 * 
 * @author Adam Mummery-Smith
 */
public class PointCubeTracking extends PointCube
{
	private final Entity entity;
	private final double traceDistance;
	private int lastX, lastY, lastZ;

	public static PointCubeTracking pointCubeTracking(final Entity entity, final double traceDistance) {
		final var ret = new PointCubeTracking(entity, traceDistance);
		ret.update();
		return ret;
	}
	
	private PointCubeTracking(Entity entity, double traceDistance)
	{
		super(new Vector3(0, 0, 0));
		this.entity = entity;
		this.traceDistance = traceDistance;
	}
	
	@Override
	public boolean isDynamic()
	{
		return true;
	}
	
	@Override
	public Vector3 getPoint()
	{
		return this.point;
	}

	@Override
	public void updatePoint(float partialTicks)
	{
		HitResult res = this.entity.pick(this.traceDistance, partialTicks, false);
		if (!(res instanceof BlockHitResult)) {
			return;
		}

		BlockPos pos = ((BlockHitResult) res).getBlockPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if (this.lastX != x || this.lastY != y || this.lastZ != z)
		{
			this.lastX = x;
			this.lastY = y;
			this.lastZ = z;
			this.point = new Vector3(x, y, z);
			this.box.setPosition(this.point.subtract(PointCube.MIN_VEC), this.point.add(PointCube.MAX_VEC));
			this.notifyObservers();
		}
	}
}
