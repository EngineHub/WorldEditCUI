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

import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import org.enginehub.worldeditcui.network.CUIEventPayload;

public class ViaFabricPlusHook {
    public static void enable() {
        Protocol1_12_2To1_13.MAPPINGS.getChannelMappings().put(CUINetworking.CHANNEL_LEGACY, CUIEventPayload.TYPE.id().toString());
    }
}
