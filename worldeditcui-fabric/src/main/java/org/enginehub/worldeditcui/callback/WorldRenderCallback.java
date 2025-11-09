/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.enginehub.worldeditcui.render.WecuiRenderContext;

/**
 * A re-implementation of the old world render events for Fabric.
 * Temporary until Fabric finishes their new rendering API.
 */
public interface WorldRenderCallback {
    void render(WecuiRenderContext ctx);

    /**
     * Fires at the end of world rendering, after all other rendering is complete.
     */
    Event<WorldRenderCallback> LAST = EventFactory.createArrayBacked(WorldRenderCallback.class, cbs -> ctx -> {
        for (var cb : cbs) cb.render(ctx);
    });

    /**
     * Fires immediately after the translucent rendering pass.
     */
    Event<WorldRenderCallback> AFTER_TRANSLUCENT = EventFactory.createArrayBacked(WorldRenderCallback.class, cbs -> ctx -> {
        for (var cb : cbs) cb.render(ctx);
    });
}
