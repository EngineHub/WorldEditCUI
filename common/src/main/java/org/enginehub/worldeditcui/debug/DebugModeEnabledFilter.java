/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.debug;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.enginehub.worldeditcui.config.CUIConfiguration;

final class DebugModeEnabledFilter extends AbstractFilter {
	private final CUIConfiguration config;

	DebugModeEnabledFilter(final CUIConfiguration config) {
		this.config = config;
	}

	private Result debugMode() {
		return config.isDebugMode() ? Result.NEUTRAL : Result.DENY;
	}

	@Override
	public Result filter(LogEvent event) {
		return debugMode();
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
		return debugMode();
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
		return debugMode();
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
		return debugMode();
	}
}
