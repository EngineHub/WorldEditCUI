/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.neoforge;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;
import org.enginehub.worldeditcui.neoforge.protocol.NeoForgeCUIPacketHandler;

import java.util.function.BiConsumer;

/**
 * Networking wrappers to integrate nicely with MultiConnect.
 *
 * <p>These methods generally first call </p>
 */
final class CUINetworking {

    private CUINetworking() {
    }

    public static void send(final CUIPacket pkt) {
        ClientPacketDistributor.sendToServer(pkt);
    }

    public static void subscribeToCuiPacket(final BiConsumer<CUIPacket, NeoForgeCUIPacketHandler.PacketContext> handler) {
        CUIPacketHandler.instance().registerClientboundHandler(handler);
    }
}
