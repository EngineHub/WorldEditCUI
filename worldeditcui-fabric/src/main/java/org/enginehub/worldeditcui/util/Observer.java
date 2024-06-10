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

/**
 * Observer for {@link Observable}
 * 
 * @author Adam Mummery-Smith
 */
public interface Observer
{
	public abstract void notifyChanged(Observable<?> source);
}
