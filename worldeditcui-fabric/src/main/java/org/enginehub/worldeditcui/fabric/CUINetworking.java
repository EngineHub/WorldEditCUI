/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.enginehub.worldeditcui.network.CUIEventPayload;

/**
 * Networking wrappers to integrate nicely with MultiConnect.
 *
 * <p>These methods generally first call </p>
 */
final class CUINetworking {

    private static final boolean VIAFABRICPLUS_AVAILABLE = FabricLoader.getInstance().isModLoaded("viafabricplus");

    static final String CHANNEL_LEGACY = "WECUI"; // pre-1.13 channel name

    private CUINetworking() {
    }

    public static void send(final CUIEventPayload pkt) {
        ClientPlayNetworking.send(pkt);
    }

    public static void subscribeToCuiPacket(final ClientPlayNetworking.PlayPayloadHandler<CUIEventPayload> handler) {
        PayloadTypeRegistry.playS2C().register(CUIEventPayload.TYPE, CUIEventPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CUIEventPayload.TYPE, CUIEventPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(CUIEventPayload.TYPE, handler);
        if (VIAFABRICPLUS_AVAILABLE) {
            ViaFabricPlusHook.enable();
        }
    }
}
