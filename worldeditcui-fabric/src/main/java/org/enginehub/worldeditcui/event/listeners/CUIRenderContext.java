/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.event.listeners;

import com.mojang.blaze3d.systems.RenderSystem;
import org.enginehub.worldeditcui.config.Colour;
import org.enginehub.worldeditcui.render.LineStyle;
import org.enginehub.worldeditcui.render.RenderSink;
import org.enginehub.worldeditcui.render.RenderStyle;
import org.enginehub.worldeditcui.util.Vector3;
import org.joml.Matrix4fStack;

import java.util.function.Consumer;

/**
 * State related to CUI rendering.
 *
 */
public final class CUIRenderContext implements RenderSink {
    private Vector3 cameraPos;
    private float dt;
    private RenderSink delegateSink;

    public Vector3 cameraPos() {
        return this.cameraPos;
    }

    public Matrix4fStack matrixStack() {
        return RenderSystem.getModelViewStack();
    }

    public void applyMatrices() {
        // RenderSystem.applyModelViewMatrix();
    }

    public float dt() {
        return this.dt;
    }

    public void withCameraAt(final Vector3 pos, final Consumer<CUIRenderContext> action) {
        final Vector3 oldPos = this.cameraPos;
        this.cameraPos = pos;
        try {
            action.accept(this);
        } finally {
            this.cameraPos = oldPos;
        }
    }

    void init(final Vector3 cameraPos, final float dt, final RenderSink sink) {
        this.cameraPos = cameraPos;
        this.dt = dt;
        this.delegateSink = sink;
    }

    /**
     * Empty state. To be called at the end of a frame.
     */
    void reset() {
        this.cameraPos = null;
        this.delegateSink = null;
    }

    // RenderSink delegation

    @Override
    public CUIRenderContext color(final float r, final float g, final float b, final float alpha) {
        this.delegateSink.color(r, g, b, alpha);
        return this;
    }

    @Override
    public CUIRenderContext color(final Colour colour) {
        this.delegateSink.color(colour);
        return this;
    }

    @Override
    public boolean apply(final LineStyle line, final RenderStyle.RenderType type) {
        return this.delegateSink.apply(line, type);
    }

    @Override
    public CUIRenderContext vertex(final double x, final double y, final double z) {
        this.delegateSink.vertex(x, y, z);
        return this;
    }

    @Override
    public CUIRenderContext beginLineLoop() {
        this.delegateSink.beginLineLoop();
        return this;
    }

    @Override
    public CUIRenderContext endLineLoop() {
        this.delegateSink.endLineLoop();
        return this;
    }

    @Override
    public RenderSink beginLines() {
        this.delegateSink.beginLines();
        return this;
    }

    @Override
    public RenderSink endLines() {
        this.delegateSink.endLines();
        return this;
    }

    @Override
    public CUIRenderContext beginQuads() {
        this.delegateSink.beginQuads();
        return this;
    }

    @Override
    public CUIRenderContext endQuads() {
        this.delegateSink.endQuads();
        return this;
    }

    @Override
    public void flush() {
        this.delegateSink.flush();
    }
}
