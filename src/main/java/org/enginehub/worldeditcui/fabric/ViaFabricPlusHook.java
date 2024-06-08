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

import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;

public class ViaFabricPlusHook {
    public static void enable() {
        Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().put(CUINetworking.CHANNEL_LEGACY, CUINetworking.CHANNEL_WECUI.toString());
    }
}
