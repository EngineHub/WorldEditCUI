/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set forth
 * in the Eclipse Public License, v. 2.0 are satisfied:
 *     GNU General Public License version 3 or later
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.neoforge.protocol;

import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

public class NeoForgeCUIPacketHandler implements CUIPacketHandler {
    private static final Set<BiConsumer<CUIPacket, PacketContext>> CLIENTBOUND_HANDLERS = ConcurrentHashMap.newKeySet();
    private static final Set<BiConsumer<CUIPacket, PacketContext>> SERVERBOUND_HANDLERS = ConcurrentHashMap.newKeySet();

    static IPayloadHandler<CUIPacket> asHandler() {
        return (pkt, ctx) -> {
            final Set<BiConsumer<CUIPacket, PacketContext>> handlers;
            if (ctx.flow().isClientbound()) {
                handlers = CLIENTBOUND_HANDLERS;
            } else {
                handlers = SERVERBOUND_HANDLERS;
            }

            final PacketContext cuiCtx = new PacketContext(ctx.player(), ctx::enqueueWork);
            for (BiConsumer<CUIPacket, PacketContext> handler : handlers) {
                handler.accept(pkt, cuiCtx);
            }
        };
    }

    @Override
    public void registerClientboundHandler(BiConsumer<CUIPacket, PacketContext> clientbound) {
        CLIENTBOUND_HANDLERS.add(requireNonNull(clientbound, "clientbound"));
    }

    @Override
    public void registerServerboundHandler(BiConsumer<CUIPacket, PacketContext> serverbound) {
        SERVERBOUND_HANDLERS.add(requireNonNull(serverbound, "clientbound"));
    }

}
