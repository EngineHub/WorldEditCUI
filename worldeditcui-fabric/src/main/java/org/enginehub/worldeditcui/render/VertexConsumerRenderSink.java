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
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

public class VertexConsumerRenderSink implements RenderSink {

    private final ConfiguredRenderType lines;
    private final ConfiguredRenderType lineLoop;
    private final ConfiguredRenderType quads;
    private final Runnable preFlush;
    private final Runnable postFlush;
    private final MultiBufferSource.BufferSource multiBufferSource;
    private VertexConsumer vertexConsumer;
    private @Nullable VertexConsumerRenderSink.ConfiguredRenderType activeRenderType;
    private boolean active;
    private boolean canFlush;
    private float r = -1f, g, b, a;
    private float loopX, loopY, loopZ; // track previous vertices for lines_loop
    private float loopFirstX, loopFirstY, loopFirstZ; // track initial vertices for lines_loop
    private boolean canLoop;

    // line state
    private float lastLineWidth = -1;
    private @Nullable DepthTestFunction lastDepthFunc = null;

    public VertexConsumerRenderSink(final TypeFactory types, final MultiBufferSource.BufferSource multiBufferSource) {
        this(types, multiBufferSource, () -> { }, () -> { });
    }

    public VertexConsumerRenderSink(final TypeFactory types, final MultiBufferSource.BufferSource multiBufferSource, final Runnable preFlush, final Runnable postFlush) {
        this.lines = types.lines();
        this.lineLoop = types.linesLoop();
        this.quads = types.quads();
        this.multiBufferSource = multiBufferSource;
        this.preFlush = preFlush;
        this.postFlush = postFlush;
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
                this.lastLineWidth = line.lineWidth;
                this.lastDepthFunc = line.renderType.depthFunc();
                if (this.active && this.activeRenderType != null) {
                    this.canFlush = true;
                    this.vertexConsumer = this.activeRenderType.getBuffer(multiBufferSource, lastLineWidth, lastDepthFunc);
                }
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

        final VertexConsumer consumer = this.vertexConsumer;
        if (this.activeRenderType == this.lineLoop) {
            // duplicate last
            if (this.canLoop) {
                final Vector3f normal = this.activeRenderType.hasNormals ? this.computeNormal(this.loopX, this.loopY, this.loopZ, x, y, z) : null;
                consumer.addVertex(this.loopX, this.loopY, this.loopZ).setColor(this.r, this.g, this.b, this.a);
                if(normal != null) {
                    // we need to compute normals pointing directly towards the screen
                    consumer.setNormal(normal.x(), normal.y(), normal.z());
                }
                consumer.addVertex((float) x, (float) y, (float) z).setColor(this.r, this.g, this.b, this.a);
                if(normal != null) {
                    consumer.setNormal(normal.x(), normal.y(), normal.z());
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
                consumer.addVertex(this.loopX, this.loopY, this.loopZ).setColor(this.r, this.g, this.b, this.a);
                if(normal != null) {
                    consumer.setNormal(normal.x(), normal.y(), normal.z());
                }
                consumer.addVertex((float) x, (float) y, (float) z).setColor(this.r, this.g, this.b, this.a);
                if(normal != null) {
                    consumer.setNormal(normal.x(), normal.y(), normal.z());
                }
                this.canLoop = false;
            } else {
                this.loopX = (float) x;
                this.loopY = (float) y;
                this.loopZ = (float) z;
                this.canLoop = true;
            }
        } else {
            consumer.addVertex((float) x, (float) y, (float) z).setColor(this.r, this.g, this.b, this.a);
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
            this.vertexConsumer.addVertex(this.loopX, this.loopY, this.loopZ).setColor(this.r, this.g, this.b, this.a);
            if(normal != null) {
                this.vertexConsumer.setNormal(normal.x(), normal.y(), normal.z());
            }

            this.vertexConsumer.addVertex(this.loopFirstX, this.loopFirstY, this.loopFirstZ).setColor(this.r, this.g, this.b, this.a);
            if(normal != null) {
                this.vertexConsumer.setNormal(normal.x(), normal.y(), normal.z());
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
            multiBufferSource.endLastBatch();
        } finally {
            this.postFlush.run();
            this.vertexConsumer = null;
            this.activeRenderType = null;
        }
    }

    private void end(final ConfiguredRenderType renderType) {
        if (!this.active) {
            throw new IllegalStateException("Could not exit " + renderType + ", was not active");
        }
        if (this.activeRenderType != renderType) {
            throw new IllegalStateException("Expected to end state " + renderType + " but was in " + this.activeRenderType);
        }
        this.active = false;
    }

    private void transitionState(final ConfiguredRenderType renderType) {
        if (this.active) {
            throw new IllegalStateException("Tried to enter new state before previous operation had been completed");
        }
        if (this.activeRenderType != null && renderType != this.activeRenderType) {
            this.flush();
        }
        this.canFlush = true;
        this.vertexConsumer = renderType.getBuffer(multiBufferSource, lastLineWidth, lastDepthFunc);
        this.activeRenderType = renderType;
        this.active = true;
    }

    public static class ConfiguredRenderType {

        private final RenderPipeline.Snippet base;
        private final String name;
        private final boolean hasNormals;

        private static final Map<Options, RenderType> BUILT_TYPES = new HashMap<>();

        public ConfiguredRenderType(final RenderPipeline.Snippet base, final String name) {
            this.base = base;
            this.name = name;
            this.hasNormals = base.vertexFormat().orElseThrow().getElementAttributeNames().contains("Normal");
        }

        boolean hasNormals() {
            return this.hasNormals;
        }

        VertexConsumer getBuffer(MultiBufferSource.BufferSource source, float lineWidth, DepthTestFunction depthFunc) {
            return source.getBuffer(getBuiltType(lineWidth, depthFunc));
        }

        private RenderType getBuiltType(float lineWidth, DepthTestFunction depthFunc) {
            final var options = new Options(this, lineWidth, depthFunc);
            return BUILT_TYPES.computeIfAbsent(options, key -> {
                final var id = ResourceLocation.fromNamespaceAndPath("worldedit_cui", "configured/" + name + "/line_width-" + lineWidth + "/depth_func-" + depthFunc.ordinal());
                final var pipeline = RenderPipeline.builder(key.type().base)
                        .withDepthTestFunction(options.depthFunc)
                        .withCull(false)
                        .withLocation(id)
                        .build();
                        return RenderType.create(
                                id.toString(),
                                1536,
                                pipeline,
                                RenderType.CompositeState.builder()
                                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(lineWidth)))
                                        .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                                        .createCompositeState(false)
                        );
            });
        }

        private record Options(ConfiguredRenderType type, float lineWidth, DepthTestFunction depthFunc) { }
    }

    public interface TypeFactory {
        ConfiguredRenderType quads();
        ConfiguredRenderType lines();
        ConfiguredRenderType linesLoop();
    }
}
