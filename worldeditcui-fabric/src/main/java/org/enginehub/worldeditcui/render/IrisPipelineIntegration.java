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

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;

final class IrisPipelineIntegration {
    private IrisPipelineIntegration() {
    }

    static void registerQuads(final RenderPipeline pipeline) {
        IrisApi.getInstance().assignPipeline(pipeline, IrisProgram.BASIC);
    }

    static void registerLines(final RenderPipeline pipeline) {
        IrisApi.getInstance().assignPipeline(pipeline, IrisProgram.LINES);
    }
}
