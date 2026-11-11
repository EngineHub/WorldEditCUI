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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class BatchedRenderSink implements RenderSink {
    private enum Primitive {
        LINE_LOOP,
        LINES,
        QUADS
    }

    /**
     * Minimum length of a subdivided line piece, in blocks.
     */
    private static final double MIN_SEGMENT_LENGTH = 0.5;
    /**
     * Maximum length of a line piece, relative to its distance from the camera.
     */
    private static final double SEGMENT_DISTANCE_FACTOR = 0.15;

    private final TypeFactory types;
    private @Nullable SubmitNodeCollector collector;
    private @Nullable PoseStack poseStack;
    private @Nullable VariantSet currentVariants;
    private @Nullable RenderTarget activeTarget;
    private @Nullable Primitive activePrimitive;
    private @Nullable List<Vertex> vertices;
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
    public void beginFrame(final SubmitNodeCollector collector, final PoseStack poseStack) {
        this.collector = collector;
        this.poseStack = poseStack;
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
        if (!this.active || this.activeTarget == null || this.vertices == null) {
            throw new IllegalStateException("Tried to draw when not active");
        }

        if (this.activePrimitive == Primitive.LINE_LOOP) {
            if (this.canLoop) {
                this.addLineSegment(this.loopX, this.loopY, this.loopZ, x, y, z);
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
                this.addLineSegment(this.loopX, this.loopY, this.loopZ, x, y, z);
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
        if (this.vertices == null) {
            throw new IllegalStateException("No active vertex list");
        }
        this.vertices.add(new Vertex(
            (float) x, (float) y, (float) z,
            this.r, this.g, this.b, this.a,
            this.currentLineWidth,
            // normals are never mutated, so pieces of a subdivided line can share one instance
            normal
        ));
    }

    /**
     * Emits a line segment, split into pieces whose length is proportional to their distance from the camera.
     *
     * <p>The vanilla line shader expands every segment into a quad by offsetting each endpoint
     * perpendicular to the line in NDC space and multiplying that offset back by the endpoint's own
     * {@code w}. When one endpoint lies behind the camera ({@code w < 0}), its offset flips sign in clip
     * space, so after near-plane clipping the quad widens by roughly {@code |w_behind| / w}. For long
     * segments that pass close to the camera (e.g. tall vertical grid lines while looking up or down)
     * this turns a thin line into a wide band.</p>
     *
     * <p>Limiting each piece to a fraction of its distance from the camera keeps that factor below one
     * for any piece that is actually on screen, so pieces crossing the camera plane end up entirely
     * outside the view frustum. Pieces are never shorter than {@link #MIN_SEGMENT_LENGTH}.</p>
     */
    private void addLineSegment(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1) {
        final Vector3f normal = activeTargetOrThrow().hasNormals() ? this.computeNormal(x0, y0, z0, x1, y1, z1) : null;

        // Measure distances in camera space: some shapes translate the pose stack instead of
        // submitting camera-relative coordinates (e.g. chunk boundaries, ellipsoids).
        final Vector3f start = new Vector3f((float) x0, (float) y0, (float) z0);
        final Vector3f end = new Vector3f((float) x1, (float) y1, (float) z1);
        if (this.poseStack != null) {
            final Matrix4f pose = this.poseStack.last().pose();
            pose.transformPosition(start);
            pose.transformPosition(end);
        }
        final double length = start.distance(end);
        if (!(length > MIN_SEGMENT_LENGTH)) {
            this.addVertex(x0, y0, z0, normal);
            this.addVertex(x1, y1, z1, normal);
            return;
        }

        // The pose is affine, so walking the segment by a parameter in [0, 1] is valid in both spaces.
        double t = 0.0;
        double px = x0;
        double py = y0;
        double pz = z0;
        while (t < 1.0) {
            final double cx = start.x() + (end.x() - start.x()) * t;
            final double cy = start.y() + (end.y() - start.y()) * t;
            final double cz = start.z() + (end.z() - start.z()) * t;
            final double distance = Math.sqrt(cx * cx + cy * cy + cz * cz);
            final double step = Math.max(MIN_SEGMENT_LENGTH, distance * SEGMENT_DISTANCE_FACTOR);
            final double next = Math.min(1.0, t + step / length);
            final double nx = next >= 1.0 ? x1 : x0 + (x1 - x0) * next;
            final double ny = next >= 1.0 ? y1 : y0 + (y1 - y0) * next;
            final double nz = next >= 1.0 ? z1 : z0 + (z1 - z0) * next;
            this.addVertex(px, py, pz, normal);
            this.addVertex(nx, ny, nz, normal);
            px = nx;
            py = ny;
            pz = nz;
            t = next;
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
            this.addLineSegment(this.loopX, this.loopY, this.loopZ, this.loopFirstX, this.loopFirstY, this.loopFirstZ);
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
        if (this.vertices == null || this.activeTarget == null) {
            return;
        }
        if (this.active) {
            throw new IllegalStateException("Tried to flush while still active");
        }
        if (!this.vertices.isEmpty()) {
            final SubmitNodeCollector collector = this.collector;
            final PoseStack poseStack = this.poseStack;
            if (collector == null || poseStack == null) {
                throw new IllegalStateException("Render sink was not prepared for this frame");
            }
            this.activeTarget.draw(collector, poseStack, List.copyOf(this.vertices));
        }
        this.vertices = null;
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
            this.activeTarget = target;
            this.vertices = new ArrayList<>();
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

    private record Vertex(
        float x,
        float y,
        float z,
        float red,
        float green,
        float blue,
        float alpha,
        float lineWidth,
        @Nullable Vector3f normal
    ) {}

    public static final class RenderTarget {
        private final boolean hasNormals;
        private final RenderType renderType;

        public RenderTarget(final RenderType renderType, final VertexFormat format) {
            this.renderType = renderType;
            this.hasNormals = format.contains("Normal");
        }

        boolean hasNormals() {
            return this.hasNormals;
        }

        void draw(
            final SubmitNodeCollector collector,
            final PoseStack poseStack,
            final List<Vertex> vertices
        ) {
            collector.submitCustomGeometry(poseStack, this.renderType, (pose, consumer) -> {
                for (final Vertex vertex : vertices) {
                    consumer.addVertex(pose, vertex.x(), vertex.y(), vertex.z())
                        .setColor(vertex.red(), vertex.green(), vertex.blue(), vertex.alpha())
                        .setLineWidth(vertex.lineWidth());
                    if (this.hasNormals() && vertex.normal() != null) {
                        consumer.setNormal(pose, vertex.normal().x(), vertex.normal().y(), vertex.normal().z());
                    }
                }
            });
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
