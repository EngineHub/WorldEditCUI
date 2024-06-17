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

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.enginehub.worldeditcui.protocol.CUIPacket;

@Mod("worldeditcui_protocol")
public class NeoForgeProtocolMod {

    public NeoForgeProtocolMod(final IEventBus modBus) {
        modBus.register(this);
    }

    @SubscribeEvent
    public void registerPacket(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CUIPacket.protocolVersion()).optional();
        registrar.playBidirectional(CUIPacket.TYPE, CUIPacket.CODEC, NeoForgeCUIPacketHandler.asHandler());
    }
}
