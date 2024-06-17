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

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.enginehub.worldeditcui.InitialisationFactory;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.exceptions.InitialisationException;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Debugging helper class
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public final class CUIDebug implements InitialisationFactory
{
	private static final boolean LOG_ALL_ERRORS = Boolean.getBoolean("wecui.debug.logall") || FabricLoader.getInstance().isDevelopmentEnvironment();
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("WorldEditCUI");
	
	private final WorldEditCUI controller;
	private boolean debugLogged;

	public CUIDebug(WorldEditCUI controller)
	{
		this.controller = controller;
	}
	
	@Override
	public void initialise() throws InitialisationException
	{
		// Create a logger that logs to console (if in debug mode), and logs to a debug file
		final Logger loggerImpl = (Logger) LogManager.getLogger("WorldEditCUI");

		final Path debugFile = FabricLoader.getInstance().getGameDir().resolve("worldeditcui.debug.log");

		// Apply defined layout from MC's configured file appender, if possible
		Layout<?> layout = null;
		for (final Appender appender : loggerImpl.getContext().getConfiguration().getAppenders().values()) {
			if (appender instanceof FileAppender
					|| appender instanceof RollingFileAppender
					|| appender instanceof RollingRandomAccessFileAppender
					|| appender instanceof RandomAccessFileAppender) {
				layout = appender.getLayout();
				break;
			}
		}

		final FileAppender appender = FileAppender.newBuilder()
				.setName("WECUIDebug")
				.withFileName(debugFile.toAbsolutePath().toString())
				.withCreateOnDemand(true)
				.setFilter(new DebugModeEnabledFilter(this.controller.getConfiguration()))
				.setLayout(layout)
				.build();

		appender.start();
		loggerImpl.addAppender(appender);
	}
	
	/**
	 * Shows a message if debug mode is true.
	 *
	 * @param message the message to log
	 */
	public void debug(String message)
	{
		if (this.controller.getConfiguration().isDebugMode()) // TODO: do this with a filter and a MARKER, maybe eventually?
		{
			CUIDebug.LOGGER.info("Debug - {}", message);
		}
	}

	public void error(String message, Throwable exception) {
		if (!this.debugLogged || LOG_ALL_ERRORS) {
			CUIDebug.LOGGER.error(message, exception);
			this.debugLogged = true;
		}
	}
	
	public void info(String message)
	{
		CUIDebug.LOGGER.info(message);
	}
	
	public void info(String message, Throwable e)
	{
		CUIDebug.LOGGER.info(message, e);
	}
}
