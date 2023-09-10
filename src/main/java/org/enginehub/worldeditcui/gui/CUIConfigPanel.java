package org.enginehub.worldeditcui.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
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
    private AbstractWidget done;
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

        done = this.addWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            configuration.configChanged();
            assert minecraft != null;
            this.minecraft.setScreen(parent);
        }).bounds((this.width - BUTTON_DONE_WIDTH) / 2, this.height - (BUTTON_HEIGHT + 7), BUTTON_DONE_WIDTH, BUTTON_HEIGHT).build());

        this.configList = new CUIConfigList(this, this.minecraft);
        this.addWidget(this.configList);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.render(gfx, mouseX, mouseY, delta);

        this.configList.render(gfx, mouseX, mouseY, delta);
        gfx.drawCenteredString(this.font, screenTitle, this.width / 2, 8, 0xFFFFFF);

        this.done.render(gfx, mouseX, mouseY, delta);
    }
}
