/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Observable object
 * 
 * @param <TObserver> observer type
 * @author Adam Mummery-Smith
 */
public abstract class Observable<TObserver extends Observer>
{
	protected List<TObserver> observers;
	
	public void addObserver(TObserver observer)
	{
		if (this.observers == null)
		{
			this.observers = new ArrayList<TObserver>();
		}
		
		this.observers.add(observer);
	}
	
	protected void notifyObservers()
	{
		if (this.observers != null)
		{
			for (TObserver observer : this.observers)
			{
				observer.notifyChanged(this);
			}
		}
	}
}
