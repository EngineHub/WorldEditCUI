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

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.config.CUIConfiguration;
import org.enginehub.worldeditcui.event.listeners.CUIListenerChannel;
import org.enginehub.worldeditcui.event.listeners.CUIListenerWorldRender;
import org.enginehub.worldeditcui.gui.CUIConfigPanel;
import org.enginehub.worldeditcui.neoforge.protocol.NeoForgeCUIPacketHandler;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.render.OptifinePipelineProvider;
import org.enginehub.worldeditcui.render.PipelineProvider;
import org.enginehub.worldeditcui.render.VanillaPipelineProvider;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;

/**
 * NeoForge mod entrypoint
 *
 * @author Mark Vainomaa
 */
@Mod(NeoForgeModWorldEditCUI.MOD_ID)
public final class NeoForgeModWorldEditCUI {
    private static final int DELAYED_HELO_TICKS = 10;

    public static final String MOD_ID = "worldeditcui";
    private static NeoForgeModWorldEditCUI instance;

    // TODO: Key mappings could be moved to `common`: https://docs.architectury.dev/api/keymappings
    private static final KeyMapping.Category KEYBIND_CATEGORY_WECUI 
            = new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, "general"));

    private final KeyMapping keyBindToggleUI = key("toggle", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindClearSel = key("clear", GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindChunkBorder = key("chunk", GLFW.GLFW_KEY_UNKNOWN);

    private static final List<PipelineProvider> RENDER_PIPELINES = List.of(
            new OptifinePipelineProvider(),
            new VanillaPipelineProvider());

    private WorldEditCUI controller;
    private CUIListenerWorldRender worldRenderListener;
    private CUIListenerChannel channelListener;

    private Level lastWorld;
    private LocalPlayer lastPlayer;

    private boolean visible = true;
    private int delayedHelo = 0;

    private DeltaTracker lastPartialTicks;

    /**
     * Register a key binding
     *
     * @param name id, will be used as a localization key under
     *             {@code key.worldeditcui.<name>}
     * @param code default value
     * @return new, registered keybinding in the mod category
     */
    private static KeyMapping key(final String name, final int code) {
        return new KeyMapping("key." + MOD_ID + '.' + name, code, KEYBIND_CATEGORY_WECUI);
    }

    public NeoForgeModWorldEditCUI(IEventBus eventBus, ModContainer container) {
        if (Boolean.getBoolean("wecui.debug.mixinaudit")) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }

        instance = this;

        // Only subscribing on the client
        if (FMLEnvironment.getDist().isClient()) {
            // TODO: Check if possible to move some events to common: https://docs.architectury.dev/api/events
            // Mod Event Bus
            eventBus.addListener(NeoForgeModWorldEditCUI::onRegisterKeyMapping);
            eventBus.addListener(NeoForgeModWorldEditCUI::onClientLifecycleClientStarted);
            CUINetworking.subscribeToCuiPacket(this::onPluginMessage);

            // Game Event Bus
            NeoForge.EVENT_BUS.addListener(NeoForgeModWorldEditCUI::onClientTickEnd);
            NeoForge.EVENT_BUS.addListener(NeoForgeModWorldEditCUI::onClientPlayConnectionJoin);
            NeoForge.EVENT_BUS.addListener(NeoForgeModWorldEditCUI::onWorldEventEndExtraction);
            NeoForge.EVENT_BUS.addListener(NeoForgeModWorldEditCUI::onWorldEventEndMain);

            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (mc, parent) -> new CUIConfigPanel(parent, instance.getController().getConfiguration()));
        }
    }

    private static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(instance.keyBindChunkBorder);
        event.register(instance.keyBindClearSel);
        event.register(instance.keyBindToggleUI);
    }

    private static void onClientLifecycleClientStarted(FMLClientSetupEvent event) {
        instance.onGameInitDone(Minecraft.getInstance());
    }

    private static void onClientTickEnd(ClientTickEvent.Post event) {
        instance.onTick(Minecraft.getInstance());
    }

    private static void onClientPlayConnectionJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        instance.onJoinGame(Minecraft.getInstance().getConnection());
    }

    // Equivalent to Fabric's WorldRenderEvents.END_EXTRACTION
    private static void onWorldEventEndExtraction(ExtractLevelRenderStateEvent event) {
        instance.lastPartialTicks = event.getDeltaTracker();
    }

    // Equivalent to Fabric's WorldRenderEvents.END_MAIN
    private static void onWorldEventEndMain(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        try {
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().mul(event.getPoseStack().last().pose());
            // RenderSystem.applyModelViewMatrix();
            instance.onPostRenderEntities(instance.lastPartialTicks);
        }
        finally {
            RenderSystem.getModelViewStack().popMatrix();
            // RenderSystem.applyModelViewMatrix();
        }
    }

    private void onTick(final Minecraft mc) {
        final CUIConfiguration config = this.controller.getConfiguration();
        final boolean inGame = mc.player != null;
        final boolean clock = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false) > 0;

        if (inGame && mc.screen == null) {
            // TODO: If moving key mappings to common, replace with `this.controller.keyBindToggleUI`
            while (this.keyBindToggleUI.consumeClick()) {
                this.visible = !this.visible;
            }

            // TODO: If moving key mappings to common, replace with `this.controller.keyBindClearSel`
            while (this.keyBindClearSel.consumeClick()) {
                if (mc.player != null) {
                    mc.player.connection.sendUnattendedCommand("/sel", null);
                }

                if (config.isClearAllOnKey()) {
                    this.controller.clearRegions();
                }
            }

            // TODO: If moving key mappings to common, replace with `this.controller.keyBindChunkBorder`
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
                this.helo();
                this.delayedHelo = NeoForgeModWorldEditCUI.DELAYED_HELO_TICKS;
                if (mc.player != null && config.isPromiscuous()) {
                    mc.player.connection.sendUnattendedCommand("we cui", null); // Tricks WE to send the current selection
                }
            }

            if (this.delayedHelo > 0) {
                this.delayedHelo--;
                if (this.delayedHelo == 0) {
                    this.helo();
                }
            }
        }
    }

    public void onPluginMessage(final CUIPacket payload,  final NeoForgeCUIPacketHandler.PacketContext ctx) {
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

    public void onJoinGame(final ClientPacketListener handler) {
        this.visible = true;
        this.controller.getDebugger().debug("Joined game, sending initial handshake");
        this.helo();
    }

    public void onPostRenderEntities(final DeltaTracker timer) {
        if (this.visible) {
            this.worldRenderListener.onRender(timer.getGameTimeDeltaPartialTick(true));
        }
    }

    private void helo() {
        CUINetworking.send(new CUIPacket("v", CUIPacket.protocolVersion()));
    }

    public WorldEditCUI getController() {
        return this.controller;
    }

    public static NeoForgeModWorldEditCUI getInstance() {
        return instance;
    }
}
