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

/**
 * A wrapper around the WECUI pipeline, to allow manipulating the state used by various shader mods.
 */
public interface PipelineProvider {
    String id();
    boolean available();

    default boolean shouldRender() {
        return true;
    }

    /**
     * Create a render sink to be used for further operations.
     *
     * <p>This method will be called once to initialize the pipeline, and only if {@link #available()} is {@code true}</p>
     *
     * @return a sink for rendering operations
     */
    RenderSink provide();

}
