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
package org.enginehub.worldeditcui.fabric.network;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.enginehub.worldeditcui.protocol.CUIPacket;

public class FabricCUIPacketRegistration implements ModInitializer {
    @Override
    public void onInitialize() {
        // Only register packet types when worldedit isn't loaded, because worldedit already
        // registers the same packets and packets can't be registered twice.
        // In case worldedit is loaded, the encoder and decoder for the clientside packet type are
        // instead directly placed into the codec from worldedit through WECUIPacketHandlerMixin
        // (causing a lack of type safety but whatever),
        // which allows the client to still send and receive the packets using the CUIPacket class instead
        // of depending on the worldedit packet class.
        if(!FabricLoader.getInstance().isModLoaded("worldedit")) {
            PayloadTypeRegistry.playS2C().register(CUIPacket.TYPE, CUIPacket.CODEC);
            PayloadTypeRegistry.playC2S().register(CUIPacket.TYPE, CUIPacket.CODEC);
        }
        FabricCUIPacketHandler.register();
    }
}
