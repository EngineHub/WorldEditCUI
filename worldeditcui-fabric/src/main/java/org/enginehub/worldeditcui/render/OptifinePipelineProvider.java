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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Optifine shaders uses a lot more frame buffers to render the world -- we have to make sure we're on the right one.
 *
 * @see <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt">the shaders documentation</a>
 */
public final class OptifinePipelineProvider implements PipelineProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // For inspection purposes, get the active program
    private static final MethodHandle SHADERS_ACTIVE_PROGRAM;
    private static final MethodHandle PROGRAM_GET_NAME;

    // Our part of the rendering pipeline -- we want to disable textures (maybe more?)
    private static final MethodHandle CONFIG_IS_SHADERS;
    private static final MethodHandle SHADERS_BEGIN_LEASH;
    private static final MethodHandle SHADERS_END_LEASH;
    private static final MethodHandle SHADERS_IS_SHADOW_PASS;
    private static boolean optifineDisabled = false;

    static {
        MethodHandle configIsShaders = null;
        MethodHandle shadersEndLeash = null;
        MethodHandle shadersBeginLeash = null;
        MethodHandle programGetName = null;
        MethodHandle shadersActiveProgram = null;
        MethodHandle shadersIsShadowPass = null;
        try {
            final Class<?> config = Class.forName("net.optifine.Config");
            final Class<?> shaders = Class.forName("net.optifine.shaders.Shaders");
            final Class<?> program = Class.forName("net.optifine.shaders.Program");
            configIsShaders = LOOKUP.findStatic(config, "isShaders", MethodType.methodType(boolean.class));
            shadersEndLeash = LOOKUP.findStatic(shaders, "endLeash", MethodType.methodType(void.class));
            shadersBeginLeash = LOOKUP.findStatic(shaders, "beginLeash", MethodType.methodType(void.class));
            programGetName = LOOKUP.findVirtual(program, "getName", MethodType.methodType(String.class));
            shadersActiveProgram = LOOKUP.findStaticGetter(shaders, "activeProgram", program);
            shadersIsShadowPass = LOOKUP.findStaticGetter(shaders, "isShadowPass", boolean.class);
            LOGGER.debug("Optifine integration successfully initialized");
        } catch (final IllegalAccessException | ClassNotFoundException | NoSuchMethodException | NoSuchFieldException ignore) {
            // Optifine not available
            optifineDisabled = true;
        }

        CONFIG_IS_SHADERS = configIsShaders;
        SHADERS_END_LEASH = shadersEndLeash;
        SHADERS_BEGIN_LEASH = shadersBeginLeash;
        PROGRAM_GET_NAME = programGetName;
        SHADERS_ACTIVE_PROGRAM = shadersActiveProgram;
        SHADERS_IS_SHADOW_PASS = shadersIsShadowPass;
    }

    static String activeProgram() {
        if (SHADERS_ACTIVE_PROGRAM == null || PROGRAM_GET_NAME == null) {
            return "<optifine not detected>";
        }

        try {
            return (String) PROGRAM_GET_NAME.invoke(SHADERS_ACTIVE_PROGRAM.invoke());
        } catch (final Throwable ex) {
            LOGGER.debug("Failed to query OptiFine shader program", ex);
            return "<program query failed>";
        }
    }

    public static class OptifineTypeFactory implements BufferBuilderRenderSink.TypeFactory {
        public static final OptifineTypeFactory INSTANCE = new OptifineTypeFactory();

        private static final BufferBuilderRenderSink.RenderType QUADS = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR, CoreShaders.POSITION_COLOR);
        private static final BufferBuilderRenderSink.RenderType LINES = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR, CoreShaders.POSITION_COLOR);
        private static final BufferBuilderRenderSink.RenderType LINES_LOOP = new BufferBuilderRenderSink.RenderType(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR, CoreShaders.POSITION_COLOR);

        private OptifineTypeFactory() {}

        @Override
        public BufferBuilderRenderSink.RenderType quads() {
            return QUADS;
        }

        @Override
        public BufferBuilderRenderSink.RenderType lines() {
            return LINES;
        }

        @Override
        public BufferBuilderRenderSink.RenderType linesLoop() {
            return LINES_LOOP;
        }
    }

    @Override
    public String id() {
        return "optifine";
    }

    @Override
    public boolean available() {
        return SHADERS_END_LEASH != null && SHADERS_BEGIN_LEASH != null && CONFIG_IS_SHADERS != null && !optifineDisabled;
    }

    @Override
    public boolean shouldRender() {
        try {
            return !(boolean) SHADERS_IS_SHADOW_PASS.invoke();
        } catch (final Throwable thr) {
            optifineDisabled = true;
            LOGGER.error("Failed to render WECUI using OptiFine hooks", thr);
            return true; // skip optifine hooks
        }
    }

    /**
     * If optifine is available, modify shader state to do our rendering.
     *
     * <p>Otherwise, just perform standard operations.</p>
     */
    @Override
    public RenderSink provide() {
        return new BufferBuilderRenderSink(
                OptifineTypeFactory.INSTANCE, // optifine doesn't use the vanilla shader system?
                () -> {
                    if (!this.available()) {
                        return;
                    }

                    try {
                        final boolean shadersEnabled = (boolean) CONFIG_IS_SHADERS.invoke();
                        if (shadersEnabled) {
                            SHADERS_BEGIN_LEASH.invoke();
                        }
                    } catch (final Throwable thr) {
                        optifineDisabled = true;
                        LOGGER.error("Failed to render WECUI using OptiFine hooks", thr);
                    }
                },
                () -> {
                    if (!this.available()) {
                        return;
                    }

                    try {
                        final boolean shadersEnabled = (boolean) CONFIG_IS_SHADERS.invoke();
                        if (shadersEnabled) {
                            SHADERS_END_LEASH.invoke();
                        }
                    } catch (final Throwable thr) {
                        optifineDisabled = true;
                        LOGGER.error("Failed to render WECUI using OptiFine hooks", thr);
                    }
                }
        );
    }
}
