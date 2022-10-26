package org.enginehub.worldeditcui.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.enginehub.worldeditcui.config.CUIConfiguration;
import org.enginehub.worldeditcui.config.Colour;
import org.slf4j.Logger;

import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Mark Vainomaa
 * @author Jesús Sanz - Modified to implement Config GUI / First Version
 */
public class CUIConfigPanel extends Screen implements Supplier<Screen> {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TRUE = CommonComponents.OPTION_ON.copy().withStyle(s -> s.applyFormat(ChatFormatting.DARK_GREEN));
    private static final Component FALSE = CommonComponents.OPTION_OFF.copy().withStyle(s -> s.applyFormat(ChatFormatting.DARK_RED));

    private final Screen parent;
    private final Set<String> options;
    private final CUIConfiguration configuration;
    private double scrollPercent = 0;
    private SettingsWidget configList;
    private AbstractWidget done;

    private final Component screenTitle;

    public CUIConfigPanel(Screen parent, CUIConfiguration configuration) {
        super(Component.literal("WorldEditCUI"));
        this.parent = parent;
        this.configuration = configuration;
        this.options = this.configuration.getConfigArray().keySet();
        this.screenTitle = Component.translatable("worldeditcui.options.title");
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (configList.isMouseOver(mouseX, mouseY))
            return this.configList.mouseScrolled(mouseX, mouseY, amount);
        return false;
    }

    @Override
    protected void init() {
        super.init();
        configList = new SettingsWidget(this.minecraft, this.width - 8, this.height, 48, this.height - 36, 25, this.configuration, this);
        configList.setLeftPos(0);
        if (this.minecraft.level != null) {
            configList.setRenderBackground(false);
        }
        done = this.addRenderableWidget(new AbstractWidget((this.width - BUTTON_WIDTH) / 2, this.height - (BUTTON_HEIGHT + 7), BUTTON_WIDTH, BUTTON_HEIGHT, CommonComponents.GUI_DONE) {
            @Override
            protected void updateWidgetNarration(final NarrationElementOutput builder) {
                this.defaultButtonNarrationText(builder);
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                for (GuiEventListener button : children()) {
                    if (button instanceof EditBox widget) {
                        if (widget.isFocused()) {
                            widget.changeFocus(false);
                        }
                    }
                }
                configuration.configChanged();
                assert minecraft != null;
                minecraft.setScreen(parent);
            }
        });

        int y = 0;
        for (String text : options) {
            int buttonX = this.width - 140 - 20;
            Object value = configuration.getConfigArray().get(text);
            AbstractWidget element;
            Component textTemp = configuration.getDescription(text);
            if (value == null) {
                LOGGER.warn("value of '{}' null, adding nothing", text);
                continue;
            } else if(value instanceof Boolean) {
                element = this.addRenderableWidget(new AbstractWidget(buttonX, y, 70, BUTTON_HEIGHT, ((Boolean) value ? TRUE: FALSE)) {
                    @Override
                    protected void updateWidgetNarration(final NarrationElementOutput builder) {
                        this.defaultButtonNarrationText(builder);
                    }

                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        if ((Boolean) (configuration.getConfigArray().get(text))) {
                            configuration.changeValue(text, false);
                        } else {
                            configuration.changeValue(text, true);
                        }
                        this.changeFocus(true);
                    }

                    @Override
                    protected void onFocusedChanged(boolean bl) {
                        this.setMessage((Boolean) configuration.getConfigArray().get(text) ? TRUE : FALSE);
                        super.onFocusedChanged(bl);
                    }
                });
            } else if(value instanceof Colour) {
                element = this.addRenderableWidget(new TextFieldWidgetTemp(this.font, buttonX, y, 70, BUTTON_HEIGHT, Component.literal(((Colour)value).hexString()), ((Colour)value).hexString()) {
                    @Override
                    protected void onFocusedChanged(boolean bl) {
                        if (bl) {
                            this.setValue(((Colour)configuration.getConfigArray().get(text)).hexString());
                        } else {
                            configuration.changeValue(text, Colour.parseRgbaOrNull(this.getValue()));
                        }
                        super.onFocusedChanged(bl);
                    }

                    @Override
                    public boolean charTyped(char chr, int keyCode) {
                        boolean result = super.charTyped(chr, keyCode); // todo: only allow valid hex chars
                        configuration.changeValue(text, Colour.parseRgbaOrNull(this.getValue()));
                        return result;
                    }

                    @Override
                    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
                        configuration.changeValue(text, Colour.parseRgbaOrNull(this.getValue()));
                        return result;
                    }
                });
            } else {
                LOGGER.warn("WorldEditCUI has option {} with unknown data type {}", text, value.getClass().getName());
                continue;
            }
            this.configList.addEntry(new SettingsEntry(this.configList, (textTemp != null) ? textTemp : Component.literal(text), element, this.addRenderableWidget(new AbstractWidget(buttonX + 75, y, BUTTON_HEIGHT, BUTTON_HEIGHT, Component.empty()) {
                @Override
                protected void updateWidgetNarration(final NarrationElementOutput builder) {
                    this.defaultButtonNarrationText(builder);
                }

                @Override
                public void onClick(double mouseX, double mouseY) {
                    configuration.changeValue(text, configuration.getDefaultValue(text));
                    element.changeFocus(false);
                }
            })));
        }
        this.setFocused(null);
        this.addRenderableOnly(this.configList);
    }

    @Override
    public Screen get() {
        return this;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.configList.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, this.font, screenTitle, this.configList.getWidth() / 2, BUTTON_HEIGHT, 0xFFFFFF);
        this.done.render(matrices, mouseX, mouseY, delta);
    }

    double getScrollPercent() {
        return scrollPercent;
    }

    void updateScrollPercent(double scrollPercent) {
        this.scrollPercent = scrollPercent;
    }

    static class TextFieldWidgetTemp extends EditBox {

        public TextFieldWidgetTemp(Font textRenderer, int x, int y, int width, int height, Component message, String text) {
            super(textRenderer, x, y, width, height, message);
            this.setValue(text);
        }
    }
}
