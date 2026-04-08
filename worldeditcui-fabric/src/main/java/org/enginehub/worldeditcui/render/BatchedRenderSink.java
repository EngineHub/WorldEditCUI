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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class BatchedRenderSink implements RenderSink {
    private enum Primitive {
        LINE_LOOP,
        LINES,
        QUADS
    }

    private final TypeFactory types;
    private @Nullable VariantSet currentVariants;
    private @Nullable RenderTarget activeTarget;
    private @Nullable Primitive activePrimitive;
    private @Nullable BufferBuilder builder;
    private boolean active;
    private float r = -1f;
    private float g;
    private float b;
    private float a;
    private float currentLineWidth = LineStyle.DEFAULT_WIDTH;
    private float loopX;
    private float loopY;
    private float loopZ;
    private float loopFirstX;
    private float loopFirstY;
    private float loopFirstZ;
    private boolean canLoop;

    public BatchedRenderSink(final TypeFactory types) {
        this.types = types;
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
        if (!line.renderType.matches(type)) {
            return false;
        }
        this.currentLineWidth = line.lineWidth;
        this.currentVariants = this.types.forStyle(line.renderType);
        return true;
    }

    @Override
    public RenderSink vertex(final double x, final double y, final double z) {
        if (this.r == -1f) {
            throw new IllegalStateException("No colour has been set!");
        }
        if (!this.active || this.activeTarget == null || this.builder == null) {
            throw new IllegalStateException("Tried to draw when not active");
        }

        if (this.activePrimitive == Primitive.LINE_LOOP) {
            if (this.canLoop) {
                final Vector3f normal = this.activeTarget.hasNormals() ? this.computeNormal(this.loopX, this.loopY, this.loopZ, x, y, z) : null;
                this.addVertex(this.loopX, this.loopY, this.loopZ, normal);
                this.addVertex(x, y, z, normal);
            } else {
                this.loopFirstX = (float) x;
                this.loopFirstY = (float) y;
                this.loopFirstZ = (float) z;
            }
            this.loopX = (float) x;
            this.loopY = (float) y;
            this.loopZ = (float) z;
            this.canLoop = true;
        } else if (this.activePrimitive == Primitive.LINES) {
            if (this.canLoop) {
                final Vector3f normal = this.activeTarget.hasNormals() ? this.computeNormal(this.loopX, this.loopY, this.loopZ, x, y, z) : null;
                this.addVertex(this.loopX, this.loopY, this.loopZ, normal);
                this.addVertex(x, y, z, normal);
                this.canLoop = false;
            } else {
                this.loopX = (float) x;
                this.loopY = (float) y;
                this.loopZ = (float) z;
                this.canLoop = true;
            }
        } else {
            this.addVertex(x, y, z, null);
        }

        return this;
    }

    private void addVertex(final double x, final double y, final double z, @Nullable final Vector3f normal) {
        if (this.builder == null) {
            throw new IllegalStateException("No active builder");
        }
        this.builder.addVertex((float) x, (float) y, (float) z)
            .setColor(this.r, this.g, this.b, this.a)
            .setLineWidth(this.currentLineWidth);
        if (normal != null) {
            this.builder.setNormal(normal.x(), normal.y(), normal.z());
        }
    }

    private Vector3f computeNormal(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1) {
        final double dX = (x1 - x0);
        final double dY = (y1 - y0);
        final double dZ = (z1 - z0);
        final double length = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        return new Vector3f((float) (dX / length), (float) (dY / length), (float) (dZ / length));
    }

    @Override
    public RenderSink beginLineLoop() {
        this.transitionState(currentVariants().lineLoop(), Primitive.LINE_LOOP);
        return this;
    }

    @Override
    public RenderSink endLineLoop() {
        this.end(currentVariants().lineLoop(), Primitive.LINE_LOOP);
        if (this.canLoop) {
            this.canLoop = false;
            final Vector3f normal = activeTargetOrThrow().hasNormals()
                ? this.computeNormal(this.loopX, this.loopY, this.loopZ, this.loopFirstX, this.loopFirstY, this.loopFirstZ)
                : null;
            this.addVertex(this.loopX, this.loopY, this.loopZ, normal);
            this.addVertex(this.loopFirstX, this.loopFirstY, this.loopFirstZ, normal);
        }
        return this;
    }

    @Override
    public RenderSink beginLines() {
        this.transitionState(currentVariants().lines(), Primitive.LINES);
        return this;
    }

    @Override
    public RenderSink endLines() {
        this.end(currentVariants().lines(), Primitive.LINES);
        this.canLoop = false;
        return this;
    }

    @Override
    public RenderSink beginQuads() {
        this.transitionState(currentVariants().quads(), Primitive.QUADS);
        return this;
    }

    @Override
    public RenderSink endQuads() {
        this.end(currentVariants().quads(), Primitive.QUADS);
        return this;
    }

    @Override
    public void flush() {
        if (this.builder == null || this.activeTarget == null) {
            return;
        }
        if (this.active) {
            throw new IllegalStateException("Tried to flush while still active");
        }
        final MeshData mesh = this.builder.buildOrThrow();
        this.activeTarget.draw(mesh);
        this.builder = null;
        this.activeTarget = null;
        this.activePrimitive = null;
    }

    private void end(final RenderTarget target, final Primitive primitive) {
        if (!this.active) {
            throw new IllegalStateException("Could not exit " + target + ", was not active");
        }
        if (this.activeTarget != target || this.activePrimitive != primitive) {
            throw new IllegalStateException("Expected to end state " + primitive + " but was in " + this.activePrimitive);
        }
        this.active = false;
    }

    private void transitionState(final RenderTarget target, final Primitive primitive) {
        if (this.active) {
            throw new IllegalStateException("Tried to enter new state before previous operation had been completed");
        }
        if (this.activeTarget != null && this.activeTarget != target) {
            this.flush();
        }
        if (this.activeTarget == null) {
            this.builder = Tesselator.getInstance().begin(target.mode(), target.format());
            this.activeTarget = target;
        }
        this.activePrimitive = primitive;
        this.active = true;
    }

    private VariantSet currentVariants() {
        if (this.currentVariants == null) {
            throw new IllegalStateException("No render style has been applied");
        }
        return this.currentVariants;
    }

    private RenderTarget activeTargetOrThrow() {
        if (this.activeTarget == null) {
            throw new IllegalStateException("No active render target");
        }
        return this.activeTarget;
    }

    @FunctionalInterface
    public interface MeshDrawer {
        void draw(MeshData mesh);
    }

    public static final class RenderTarget {
        private final VertexFormat.Mode mode;
        private final VertexFormat format;
        private final boolean hasNormals;
        private final MeshDrawer drawer;

        public RenderTarget(final VertexFormat.Mode mode, final VertexFormat format, final MeshDrawer drawer) {
            this.mode = mode;
            this.format = format;
            this.hasNormals = format.getElementAttributeNames().contains("Normal");
            this.drawer = drawer;
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

        void draw(final MeshData mesh) {
            this.drawer.draw(mesh);
        }
    }

    public static final class VariantSet {
        private final RenderTarget quads;
        private final RenderTarget lines;
        private final RenderTarget lineLoop;

        public VariantSet(final RenderTarget quads, final RenderTarget lines, final RenderTarget lineLoop) {
            this.quads = quads;
            this.lines = lines;
            this.lineLoop = lineLoop;
        }

        RenderTarget quads() {
            return this.quads;
        }

        RenderTarget lines() {
            return this.lines;
        }

        RenderTarget lineLoop() {
            return this.lineLoop;
        }
    }

    public interface TypeFactory {
        VariantSet forStyle(RenderStyle.RenderType renderType);
    }
}
