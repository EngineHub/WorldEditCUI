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

import com.mojang.blaze3d.platform.CompareOp;
import org.enginehub.worldeditcui.config.Colour;

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
		ANY(CompareOp.ALWAYS_PASS),
		
		/**
		 * Render type for "hidden" lines (under world geometry)
		 */
		HIDDEN(CompareOp.LESS_THAN),
		
		/**
		 * Render type for visible lines (over world geometry) 
		 */
		VISIBLE(CompareOp.GREATER_THAN_OR_EQUAL);
		
		final CompareOp depthTest;

		private RenderType(final CompareOp depthTest)
		{
			this.depthTest = depthTest;
		}

		public CompareOp depthTest()
		{
			return this.depthTest;
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
