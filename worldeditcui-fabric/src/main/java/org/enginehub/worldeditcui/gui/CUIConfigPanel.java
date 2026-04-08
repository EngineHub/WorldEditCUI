/*
 * Copyright (c) 2011-2024 WorldEditCUI team and contributors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.enginehub.worldeditcui.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.enginehub.worldeditcui.config.CUIConfiguration;

/**
 * @author Mark Vainomaa
 * @author Jesús Sanz - Modified to implement Config GUI / First Version
 */
public class CUIConfigPanel extends Screen {
    private static final int BUTTON_DONE_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;
    final CUIConfiguration configuration;
    private CUIConfigList configList;
    private final Component screenTitle;

    public CUIConfigPanel(Screen parent, CUIConfiguration configuration) {
        super(Component.literal("WorldEditCUI"));
        this.parent = parent;
        this.configuration = configuration;
        this.screenTitle = Component.translatable("worldeditcui.options.title");
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            configuration.configChanged();
            assert minecraft != null;
            this.minecraft.setScreen(parent);
        }).bounds((this.width - BUTTON_DONE_WIDTH) / 2, this.height - (BUTTON_HEIGHT + 7), BUTTON_DONE_WIDTH, BUTTON_HEIGHT).build());

        this.configList = CUIConfigList.create(this, this.minecraft);
        this.addRenderableWidget(this.configList);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(gfx, mouseX, mouseY, delta);
        gfx.centeredText(this.font, screenTitle, this.width / 2, 8, 0xFFFFFF);
    }
}
