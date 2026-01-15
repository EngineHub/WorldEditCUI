/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.exceptions;

/**
 * Special exception that only gets called during initialisation
 * Throwing this halts the loading of the mod
 * 
 * @author yetanotherx
 * 
 */
public class InitialisationException extends Exception
{
	
	private static final long serialVersionUID = 1L;
	
	public InitialisationException(String string)
	{
		super(string);
	}
	
	public InitialisationException()
	{
	}
}
