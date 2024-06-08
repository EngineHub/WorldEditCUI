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

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.api.Protocols;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Networking wrappers to integrate nicely with MultiConnect.
 *
 * <p>These methods generally first call </p>
 */
final class CUINetworking {

    private static final boolean MULTICONNECT_AVAILABLE = FabricLoader.getInstance().isModLoaded("multiconnect");
    private static final boolean VIAFABRICPLUS_AVAILABLE = FabricLoader.getInstance().isModLoaded("viafabricplus");

    static final String CHANNEL_LEGACY = "WECUI"; // pre-1.13 channel name
    static final ResourceLocation CHANNEL_WECUI = new ResourceLocation("worldedit", "cui");

    private CUINetworking() {
    }

    public static void send(final ClientPacketListener handler, final FriendlyByteBuf codec) {
        if (!MULTICONNECT_AVAILABLE) {
            ClientPlayNetworking.send(CHANNEL_WECUI, codec);
            return;
        }

        sendUnchecked(handler, codec);
    }

    private static void sendUnchecked(final ClientPacketListener handler, final FriendlyByteBuf data) {
        final MultiConnectAPI api = MultiConnectAPI.instance();
        if (api.getProtocolVersion() <= Protocols.V1_12_2) {
            // Legacy string-based
            api.forceSendStringCustomPayload(handler, CHANNEL_LEGACY, data);
        } else {
            api.forceSendCustomPayload(handler, CHANNEL_WECUI, data);
        }
    }


    public static void subscribeToCuiPacket(final ClientPlayNetworking.PlayChannelHandler handler) {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_WECUI, handler);
        if (MULTICONNECT_AVAILABLE) {
            subscribeToCuiPacketUnchecked(handler);
        }
        if (VIAFABRICPLUS_AVAILABLE) {
            ViaFabricPlusHook.enable();
        }
    }

    private static void subscribeToCuiPacketUnchecked(final ClientPlayNetworking.PlayChannelHandler handler) {
        MultiConnectAPI.instance().addClientboundStringCustomPayloadListener(event -> {
            if (event.getChannel().equals(CHANNEL_LEGACY)) {
                handler.receive(Minecraft.getInstance(), event.getNetworkHandler(), event.getData(), ClientPlayNetworking.getSender());
            }
        });
        MultiConnectAPI.instance().addClientboundIdentifierCustomPayloadListener(event -> {
            if (event.getChannel().equals(CHANNEL_WECUI)) {
                handler.receive(Minecraft.getInstance(), event.getNetworkHandler(), event.getData(), ClientPlayNetworking.getSender());
            }
        });
    }

}
