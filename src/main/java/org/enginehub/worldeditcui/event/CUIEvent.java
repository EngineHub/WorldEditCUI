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

/**
 * Base event for CUI events, handles parameter validation and running the logic
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public abstract class CUIEvent
{
	protected final WorldEditCUI controller;
	protected final String[] params;
	protected final boolean multi;
	
	public CUIEvent(CUIEventArgs args)
	{
		this.controller = args.getController();
		this.params = args.getParams();
		this.multi = args.isMulti();
	}
	
	public abstract String raise();
	
	public abstract CUIEventType getEventType();
	
	public String getEventName()
	{
		return this.getEventType().getName();
	}
	
	/**
	 * Checks if the parameters match the required length.
	 * @return 
	 */
	public boolean isValid()
	{
		int max = this.getEventType().getMaxParameters();
		int min = this.getEventType().getMinParameters();
		
		if (max == min)
		{
			if (this.params.length != max)
			{
				return false;
			}
		}
		else
		{
			if (this.params.length > max || this.params.length < min)
			{
				return false;
			}
		}
		
		return true;
		
	}
	
	public void prepare()
	{
		if (this.controller == null || this.params == null)
		{
			throw new IllegalStateException("Controller and parameters must both be set.");
		}
		
		if (!this.isValid())
		{
			String message = String.format("Invalid number of parameters. %s event requires %s parameters but received %s [%s]", this.getEventName(), this.getRequiredParameterString(), this.params.length, Joiner.on(", ").join(this.params));
			throw new IllegalArgumentException(message);
		}
	}
	
	private String getRequiredParameterString()
	{
		if (this.getEventType().getMaxParameters() == this.getEventType().getMinParameters())
		{
			return String.valueOf(this.getEventType().getMaxParameters());
		}
		
		return String.format("between %d and %d", this.getEventType().getMinParameters(), this.getEventType().getMaxParameters());
	}

	public int getInt(int index)
	{
		return (int)Float.parseFloat(this.params[index]);
	}
	
	public double getDouble(int index)
	{
		return Double.parseDouble(this.params[index]);
	}
	
	public String getString(int index)
	{
		return this.params[index];
	}
}
