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

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.callback.WorldRenderCallback;
import org.enginehub.worldeditcui.config.CUIConfiguration;
import org.enginehub.worldeditcui.event.listeners.CUIListenerChannel;
import org.enginehub.worldeditcui.event.listeners.CUIListenerWorldRender;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;
import org.enginehub.worldeditcui.render.OptifinePipelineProvider;
import org.enginehub.worldeditcui.render.PipelineProvider;
import org.enginehub.worldeditcui.render.VanillaPipelineProvider;
import org.enginehub.worldeditcui.render.WecuiRenderContext;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;

/**
 * Fabric mod entrypoint
 *
 * @author Mark Vainomaa
 */
public final class FabricModWorldEditCUI implements ModInitializer {
    private static final int DELAYED_HELO_TICKS = 10;

    public static final String MOD_ID = "worldeditcui";
    private static FabricModWorldEditCUI instance;

    private static final KeyMapping.Category KEYBIND_CATEGORY_WECUI
            = new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, "general"));

    private final KeyMapping keyBindToggleUI = key("toggle", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindClearSel = key("clear", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindChunkBorder = key("chunk", GLFW.GLFW_KEY_UNKNOWN);

    private static final List<PipelineProvider> RENDER_PIPELINES = List.of(
            new OptifinePipelineProvider(),
            new VanillaPipelineProvider()
    );

    private WorldEditCUI controller;
    private CUIListenerWorldRender worldRenderListener;
    private CUIListenerChannel channelListener;

    private Level lastWorld;
    private LocalPlayer lastPlayer;

    private boolean visible = true;
    private int delayedHelo = 0;

    /**
     * Register a key binding
     *
     * @param name id, will be used as a localization key under {@code key.worldeditcui.<name>}
     * @param code default value
     * @return new, registered keybinding in the mod category
     */
    private static KeyMapping key(final String name, final int code) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key." + MOD_ID + '.' + name, code, KEYBIND_CATEGORY_WECUI));
    }

    @Override
    public void onInitialize() {
        if (Boolean.getBoolean("wecui.debug.mixinaudit")) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }

        instance = this;

        // Set up event listeners
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientLifecycleEvents.CLIENT_STARTED.register(this::onGameInitDone);
        CUINetworking.subscribeToCuiPacket(this::onPluginMessage);
        ClientPlayConnectionEvents.JOIN.register(this::onJoinGame);
        WorldRenderCallback.AFTER_TRANSLUCENT.register(ctx -> {
            if (ctx.advancedTranslucency()) {
                try {
                    RenderSystem.getModelViewStack().pushMatrix();
                    RenderSystem.getModelViewStack().mul(ctx.poseStack().last().pose());
                    // RenderSystem.applyModelViewMatrix();
                    //ctx.worldRenderer().getTranslucentTarget().bindWrite(false);
                    this.onPostRenderEntities(ctx);
                } finally {
                    //Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                    RenderSystem.getModelViewStack().popMatrix();
                }
            }
        });
        WorldRenderCallback.LAST.register(ctx -> {
            if (!ctx.advancedTranslucency()) {
                try {
                    RenderSystem.getModelViewStack().pushMatrix();
                    RenderSystem.getModelViewStack().mul(ctx.poseStack().last().pose());
                    // RenderSystem.applyModelViewMatrix();
                    this.onPostRenderEntities(ctx);
                } finally {
                    RenderSystem.getModelViewStack().popMatrix();
                    // RenderSystem.applyModelViewMatrix();
                }
            }
        });
    }

    private void onTick(final Minecraft mc) {
        final CUIConfiguration config = this.controller.getConfiguration();
        final boolean inGame = mc.player != null;
        final boolean clock = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false) > 0;

        if (inGame && mc.screen == null) {
            while (this.keyBindToggleUI.consumeClick()) {
                this.visible = !this.visible;
            }

            while (this.keyBindClearSel.consumeClick()) {
                if (mc.player != null) {
                    mc.player.connection.sendUnattendedCommand("/sel", null);
                }

                if (config.isClearAllOnKey()) {
                    this.controller.clearRegions();
                }
            }

            while (this.keyBindChunkBorder.consumeClick()) {
                this.controller.toggleChunkBorders();
            }
        }

        if (inGame && clock && this.controller != null) {
            if (mc.level != this.lastWorld || mc.player != this.lastPlayer) {
                this.lastWorld = mc.level;
                this.lastPlayer = mc.player;

                this.controller.getDebugger().debug("World change detected, sending new handshake");
                this.controller.clear();
                this.helo(mc.getConnection());
                this.delayedHelo = FabricModWorldEditCUI.DELAYED_HELO_TICKS;
                if (mc.player != null && config.isPromiscuous()) {
                    mc.player.connection.sendUnattendedCommand("we cui", null); // Tricks WE to send the current selection
                }
            }

            if (this.delayedHelo > 0) {
                this.delayedHelo--;
                if (this.delayedHelo == 0) {
                    this.helo(mc.getConnection());
                }
            }
        }
    }

    private void onPluginMessage(final CUIPacket payload, final CUIPacketHandler.PacketContext ctx) {
        try {
            ctx.workExecutor().execute(() -> this.channelListener.onMessage(payload));
        } catch (final Exception ex) {
            this.getController().getDebugger().info("Error decoding payload from server", ex);
        }
    }

    public void onGameInitDone(final Minecraft client) {
        this.controller = new WorldEditCUI();
        this.controller.initialise(client);
        this.worldRenderListener = new CUIListenerWorldRender(this.controller, client, RENDER_PIPELINES);
        this.channelListener = new CUIListenerChannel(this.controller);
    }

    public void onJoinGame(final ClientPacketListener handler, final PacketSender sender, final Minecraft client) {
        this.visible = true;
        this.controller.getDebugger().debug("Joined game, sending initial handshake");
        this.helo(handler);
    }

    public void onPostRenderEntities(final WecuiRenderContext ctx) {
        if (this.visible) {
            this.worldRenderListener.onRender(ctx.delta().getRealtimeDeltaTicks());
        }
    }

    private void helo(final ClientPacketListener handler) {
        CUINetworking.send(new CUIPacket("v", CUIPacket.protocolVersion()));
    }

    public WorldEditCUI getController()
    {
        return this.controller;
    }

    public static FabricModWorldEditCUI getInstance() {
        return instance;
    }
}
