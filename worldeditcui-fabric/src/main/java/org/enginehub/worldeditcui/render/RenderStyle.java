/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.render;

import com.mojang.blaze3d.platform.DepthTestFunction;
import org.enginehub.worldeditcui.config.Colour;
import org.lwjgl.opengl.GL32;

/**
 * Render style adapter, can be one of the built-in {@link ConfiguredColour}s
 * or a user-defined style from a custom payload
 * 
 * @author Adam Mummery-Smith
 */
public interface RenderStyle
{
	/**
	 * Rendering type for this line
	 */
	public enum RenderType
	{
		/**
		 * Render type to draw lines regardless of depth
		 */
		ANY(DepthTestFunction.NO_DEPTH_TEST),
		
		/**
		 * Render type for "hidden" lines (under world geometry)
		 */
		HIDDEN(DepthTestFunction.GREATER_DEPTH_TEST),
		
		/**
		 * Render type for visible lines (over world geometry) 
		 */
		VISIBLE(DepthTestFunction.LEQUAL_DEPTH_TEST);
		
		final DepthTestFunction depthFunc;

		private RenderType(DepthTestFunction depthFunc)
		{
			this.depthFunc = depthFunc;
		}

		public DepthTestFunction depthFunc()
		{
			return this.depthFunc;
		}
		
		public boolean matches(RenderType other)
		{
			return other == RenderType.ANY ? true : other == this;
		}
	}

	public abstract void setRenderType(RenderType renderType);
	
	public abstract RenderType getRenderType();
	
	public abstract void setColour(Colour colour);

	public abstract Colour getColour();
	
	public abstract LineStyle[] getLines();
}