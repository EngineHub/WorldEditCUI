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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.util.Objects;
import java.util.function.Supplier;

public class BufferBuilderRenderSink implements RenderSink {

    private final RenderType lines;
    private final RenderType lineLoop;
    private final RenderType quads;
    private final Runnable preFlush;
    private final Runnable postFlush;
    private BufferBuilder builder;
    private @Nullable BufferBuilderRenderSink.RenderType activeRenderType;
    private boolean active;
    private boolean canFlush;
    private float r = -1f, g, b, a;
    private float loopX, loopY, loopZ; // track previous vertices for lines_loop
    private float loopFirstX, loopFirstY, loopFirstZ; // track initial vertices for lines_loop
    private boolean canLoop;

    // line state
    private float lastLineWidth = -1;
    private int lastDepthFunc = -1;

    public BufferBuilderRenderSink(final TypeFactory types) {
        this(types, () -> {}, () -> {});
    }

    public BufferBuilderRenderSink(final TypeFactory types, final Runnable preFlush, final Runnable postFlush) {
        this.lines = types.lines();
        this.lineLoop = types.linesLoop();
        this.quads = types.quads();
        this.preFlush = preFlush;
        this.postFlush = postFlush;
    }

    static class LineWidth {
        private static final boolean HAS_COMPATIBILITY = (GL11.glGetInteger(GL32.GL_CONTEXT_PROFILE_MASK) & GL32.GL_CONTEXT_COMPATIBILITY_PROFILE_BIT) != 0;
        private static float lineWidth = GL11.glGetInteger(GL11.GL_LINE_WIDTH);

        static void set(final float width) {
            if (HAS_COMPATIBILITY) {
                if (lineWidth != width) {
                    GL11.glLineWidth(width);
                    lineWidth = width;
                }
            }
            RenderSystem.lineWidth(width);
        }

    }

    @Override
    public RenderSink color(final float r, final float g, final float b, final float alpha) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = alpha;
        return this;
    }

    @Override
    public boolean apply(final LineStyle line, final RenderStyle.RenderType type) {
        if (line.renderType.matches(type))
        {
            if (line.lineWidth != this.lastLineWidth || line.renderType.depthFunc() != this.lastDepthFunc) {
                this.flush();
                if (this.active && this.activeRenderType != null) {
                    this.canFlush = true;
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    this.builder = Tesselator.getInstance().begin(this.activeRenderType.mode, this.activeRenderType.format);
                }
                LineWidth.set(this.lastLineWidth = line.lineWidth);
                RenderSystem.depthFunc(this.lastDepthFunc = line.renderType.depthFunc());
            }
            return true;
        }

        return false;
    }

    @Override
    public RenderSink vertex(final double x, final double y, final double z) {
        if (this.r == -1f) {
            throw new IllegalStateException("No colour has been set!");
        }
        if (!this.active) {
            throw new IllegalStateException("Tried to draw when not active");
        }

        final BufferBuilder builder = this.builder;
        if (this.activeRenderType == this.lineLoop) {
            // duplicate last
            if (this.canLoop) {
                final Vector3f normal = this.activeRenderType.hasNormals ? this.computeNormal(this.loopX, this.loopY, this.loopZ, x, y, z) : null;
                builder.addVertex(this.loopX, this.loopY, this.loopZ).setColor(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    // we need to compute normals pointing directly towards the screen
                    builder.setNormal(normal.x(), normal.y(), normal.z());
                }
                builder.addVertex((float) x, (float) y, (float) z).setColor(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    builder.setNormal(normal.x(), normal.y(), normal.z());
                }
            } else {
                this.loopFirstX = (float) x;
                this.loopFirstY = (float) y;
                this.loopFirstZ = (float) z;
            }
            this.loopX = (float) x;
            this.loopY = (float) y;
            this.loopZ = (float) z;
            this.canLoop = true;
        } else if (this.activeRenderType == this.lines) {
            // we buffer vertices so we can compute normals here
            if (this.canLoop) {
                final Vector3f normal = this.activeRenderType.hasNormals ? this.computeNormal(this.loopX, this.loopY, this.loopZ, x, y, z) : null;
                builder.addVertex(this.loopX, this.loopY, this.loopZ).setColor(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    builder.setNormal(normal.x(), normal.y(), normal.z());
                }
                builder.addVertex((float) x, (float) y, (float) z).setColor(this.r, this.g, this.b, this.a);
                if (normal != null) {
                    builder.setNormal(normal.x(), normal.y(), normal.z());
                }
                this.canLoop = false;
            } else {
                this.loopX = (float) x;
                this.loopY = (float) y;
                this.loopZ = (float) z;
                this.canLoop = true;
            }
        } else {
            builder.addVertex((float) x, (float) y, (float) z).setColor(this.r, this.g, this.b, this.a);
        }
        return this;
    }

    private Vector3f computeNormal(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1) {
        // we need to compute normals so all drawn planes appear perpendicular to the screen
        final double dX = (x1 - x0);
        final double dY = (y1 - y0);
        final double dZ = (z1 - z0);
        final double length = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        final Vector3f normal = new Vector3f((float) (dX / length), (float) (dY / length), (float) (dZ / length));
        // normal.transform(RenderSystem.getModelViewStack().last().normal());
        return normal;
    }

    @Override
    public RenderSink beginLineLoop() {
        this.transitionState(this.lineLoop);
        return this;
    }

    @Override
    public RenderSink endLineLoop() {
        this.end(this.lineLoop);
        if (this.canLoop) {
            this.canLoop = false;
            final Vector3f normal = this.activeRenderType.hasNormals ? this.computeNormal(this.loopX, this.loopY, this.loopZ, this.loopFirstX, this.loopFirstY, this.loopFirstZ) : null;
            this.builder.addVertex(this.loopX, this.loopY, this.loopZ).setColor(this.r, this.g, this.b, this.a);
            if (normal != null) {
                this.builder.setNormal(normal.x(), normal.y(), normal.z());
            }

            this.builder.addVertex(this.loopFirstX, this.loopFirstY, this.loopFirstZ).setColor(this.r, this.g, this.b, this.a);
            if (normal != null) {
                this.builder.setNormal(normal.x(), normal.y(), normal.z());
            }
        }
        return this;
    }

    @Override
    public RenderSink beginLines() {
        this.transitionState(this.lines);
        return this;
    }

    @Override
    public RenderSink endLines() {
        this.end(this.lines);
        return this;
    }

    @Override
    public RenderSink beginQuads() {
        this.transitionState(this.quads);
        return this;
    }

    @Override
    public RenderSink endQuads() {
        this.end(this.quads);
        return this;
    }

    @Override
    public void flush() {
        if (!this.canFlush) {
            return;
        }
        if (this.active) {
            throw new IllegalStateException("Tried to flush while still active");
        }
        this.canFlush = false;
        this.preFlush.run();
        try {
            if (this.activeRenderType != null) {
                RenderSystem.setShader(this.activeRenderType.shader);
            }
            BufferUploader.drawWithShader(this.builder.buildOrThrow());
        } finally {
            this.postFlush.run();
            this.builder = null;
            this.activeRenderType = null;
        }
    }

    private void end(final RenderType renderType) {
        if (!this.active) {
            throw new IllegalStateException("Could not exit " + renderType + ", was not active");
        }
        if (this.activeRenderType != renderType) {
            throw new IllegalStateException("Expected to end state " + renderType + " but was in " + this.activeRenderType);
        }
        this.active = false;
    }

    private void transitionState(final RenderType renderType) {
        if (this.active) {
            throw new IllegalStateException("Tried to enter new state before previous operation had been completed");
        }
        if (this.activeRenderType != null && renderType.mustFlushAfter(this.activeRenderType)) {
            this.flush();
        }
        if (this.activeRenderType == null || this.activeRenderType.mode != renderType.mode) {
            this.canFlush = true;
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.builder = Tesselator.getInstance().begin(renderType.mode, renderType.format);
        }
        this.activeRenderType = renderType;
        this.active = true;
    }

    public static class RenderType {

        private final VertexFormat.Mode mode;
        private final VertexFormat format;
        private final boolean hasNormals;
        private final Supplier<ShaderInstance> shader;

        public RenderType(final VertexFormat.Mode mode, final VertexFormat format, final Supplier<ShaderInstance> shader) {
            this.mode = mode;
            this.format = format;
            this.hasNormals = format.getElementAttributeNames().contains("Normal");
            this.shader = shader;
        }

        VertexFormat.Mode mode() {
            return this.mode;
        }

        VertexFormat format() {
            return this.format;
        }

        boolean hasNormals() {
            return this.hasNormals;
        }

        Supplier<ShaderInstance> shader() {
            return this.shader;
        }

        boolean mustFlushAfter(final RenderType previous) {
            return previous.mode != this.mode || !Objects.equals(previous.format, this.format);
        }
    }

    public interface TypeFactory {
        RenderType quads();
        RenderType lines();
        RenderType linesLoop();
    }
}
