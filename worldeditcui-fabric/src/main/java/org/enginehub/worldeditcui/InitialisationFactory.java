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

import org.enginehub.worldeditcui.exceptions.InitialisationException;

/**
 * Simple interface to trace what needs to be initialised at mod loading.
 * Uses a unique exception to know when to halt initialisation and stop mod loading.
 * 
 * @author yetanotherx
 */
public interface InitialisationFactory
{
	
	public void initialise() throws InitialisationException;
}
