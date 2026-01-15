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

import org.enginehub.worldeditcui.config.Colour;

/**
 * A target that can be rendered to.
 *
 * <p>This minimizes verbosity in the individual shape rendering</p>
 */
public interface RenderSink {
    /**
     * Set the colour that will be used for further render operations.
     *
     * @param r the red (from 0 to 1)
     * @param g the green value (from 0 to 1)
     * @param b the blue value (from 0 to 1)
     * @param alpha the alpha value
     * @return this
     */
    RenderSink color(float r, float g, float b, float alpha);

    /**
     * Set the colour that will be used for further render operations.
     *
     * @param colour the colour to use
     * @return this
     */
    default RenderSink color(final Colour colour) {
        return color(colour.red() / 255f, colour.green() / 255f, colour.blue() / 255f, colour.alpha() / 255f);
    }

    default RenderSink color(final LineStyle style) {
        return this.color(style.red / 255f, style.green / 255f, style.blue / 255f, style.alpha / 255f);
    }

    default RenderSink color(final LineStyle style, final float tint) {
        return this.color(style.red / 255f, style.green / 255f, style.blue / 255f, (style.alpha / 255f) * tint);
    }

    /**
     * Apply the provided line style to this sink.
     *
     * @param line the line style
     * @param type the phase of rendering in progress
     * @return whether this line style is applicable to the type
     */
    boolean apply(final LineStyle line, final RenderStyle.RenderType type);

    /**
     * Add a new vertex to the operation.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return this
     */
    RenderSink vertex(double x, double y, double z);

    /**
     * Begin drawing a <em>line loop</em>.
     *
     * @return this
     */
    RenderSink beginLineLoop();

    /**
     * Finish drawing a <em>line loop</em>.
     *
     * This may not issue a draw call immediately.
     *
     * @return this
     */
    RenderSink endLineLoop();

    /**
     * Begin drawing <em>lines</em>.
     *
     * @return this
     */
    RenderSink beginLines();

    /**
     * Finish drawing <em>lines</em>.
     *
     * This may not issue a draw call immediately.
     *
     * @return this
     */
    RenderSink endLines();

    /**
     * Begin drawing quads (rectangles).
     *
     * @return this
     */
    RenderSink beginQuads();

    /**
     * Finish drawing quads.
     *
     * @return this
     */
    RenderSink endQuads();

    /**
     * Draw any queued operations.
     *
     * <p>If there are no queued operations, this is a no-op.</p>
     *
     * <p>This should not need to be used outside of lifecycle management</p>
     */
    void flush();


}
