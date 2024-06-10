/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public record CUIEventPayload(boolean multi, String eventType, List<String> args) implements CustomPacketPayload {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CustomPacketPayload.Type<CUIEventPayload> TYPE = new Type<>(new ResourceLocation("worldedit", "cui"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CUIEventPayload> CODEC = CustomPacketPayload.codec(CUIEventPayload::encode, CUIEventPayload::decode);

    public CUIEventPayload {
        args = List.copyOf(args);
    }

    public CUIEventPayload(final String eventType, final String... args) {
        this(false, eventType, List.of(args));
    }

    public static CUIEventPayload decode(final FriendlyByteBuf buf) {
        final int readableBytes = buf.readableBytes();
        if (readableBytes <= 0) {
            throw new IllegalStateException("Warning, invalid (zero length) payload received");
        }
        final String payload = buf.toString(StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + buf.readableBytes());

        final String[] split = payload.split("\\|", -1);
        boolean multi = split[0].startsWith("+");
        String type = split[0].substring(multi ? 1 : 0);
        List<String> args = split.length > 1 ? List.of(Arrays.copyOfRange(split, 1, split.length)) : List.of();

        final CUIEventPayload pkt = new CUIEventPayload(multi, type, args);
        LOGGER.debug("Received CUI event from server: {}", pkt);
        return pkt;
    }

    public void encode(final FriendlyByteBuf buf) {
        final StringBuilder builder = new StringBuilder();
        if (this.multi()) {
            builder.append('+');
        }
        builder.append(this.eventType());
        for (final String arg : this.args()) {
            builder.append('|');
            builder.append(arg);
        }
        buf.writeBytes(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
