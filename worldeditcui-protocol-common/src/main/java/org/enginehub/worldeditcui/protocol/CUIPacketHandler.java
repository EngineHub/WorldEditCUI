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
package org.enginehub.worldeditcui.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

/**
 * Utilities for receiving CUI packets.
 */
public interface CUIPacketHandler {
    /**
     * Get a handler instance to register with.
     *
     * @return the handler instance
     */
    static CUIPacketHandler instance() {
        return CUIPacketHandlers0.HANDLER_IMPL;
    }

    /**
     * Contextual info for the packet.
     *
     * @param player the player receiving the packet
     * @param workExecutor a work executor for the client/server main thread
     */
    record PacketContext(Player player, Executor workExecutor) {}

    /**
     * Register a handler that will receive packets on the logical client.
     *
     * @param clientbound the clientbound handler
     */
    void registerClientboundHandler(final BiConsumer<CUIPacket, PacketContext> clientbound);
    /**
     * Register a handler that will receive packets on the logical server.
     *
     * @param serverbound the serverbound handler
     */
    void registerServerboundHandler(final BiConsumer<CUIPacket, PacketContext> serverbound);
}
class CUIPacketHandlers0 {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ServiceLoader<CUIPacketHandler> HANDLER_DISCOVERY = ServiceLoader.load(CUIPacketHandler.class);
    public static final CUIPacketHandler HANDLER_IMPL;

    static {
        final Set<String> knownProviders = new HashSet<>();
        try {
            final List<CUIPacketHandler> handlers = HANDLER_DISCOVERY.stream()
                .peek(prov -> knownProviders.add(prov.type().getCanonicalName()))
                .map(ServiceLoader.Provider::get)
                .toList();

            if (handlers.isEmpty()) {
                throw new IllegalStateException("No CUI protocol providers available");
            } else {
                HANDLER_IMPL = handlers.getFirst();
            }
        } catch (final ServiceConfigurationError ex) {
            LOGGER.error("Failed to discover a CUI protocol handler, from known providers {}:", knownProviders, ex);
            throw new IllegalStateException("Failed to configure CUI protocol handlers", ex);
        }
    }
}

