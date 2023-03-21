package org.enginehub.worldeditcui.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.enginehub.worldeditcui.config.CUIConfiguration;
import org.enginehub.worldeditcui.config.Colour;
import org.slf4j.Logger;

import java.util.List;

public class CUIConfigList extends ContainerObjectSelectionList<CUIConfigList.ConfigEntry> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 20;
    private static final Style invalidFormat = Style.EMPTY
            .withColor(TextColor.parseColor("dark_red"))
            .withUnderlined(true);

    private final CUIConfiguration configuration;
    int maxNameWidth = 0;

    public CUIConfigList(CUIConfigPanel panel, Minecraft minecraft) {
        super(minecraft, panel.width + 45, panel.height, 20, panel.height - 32, 25);
        this.configuration = panel.configuration;

        for (String key : this.configuration.getConfigArray().keySet()) {
            Object value = configuration.getConfigArray().get(key);

            maxNameWidth = Math.max(maxNameWidth, minecraft.font.width(configuration.getDescription(key)));

            if (value instanceof Boolean) {
                this.addEntry(new OnOffEntry(this.configuration, key));
            } else if (value instanceof Colour) {
                this.addEntry(new ColorConfigEntry(this.configuration, key));
            } else {
                LOGGER.warn("WorldEditCUI has option {} with unknown data type {}", key, value == null ? "NULL" : value.getClass().getName());
            }
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public class OnOffEntry extends ConfigEntry {
        private final CycleButton<Boolean> toggleBotton;

        public OnOffEntry(CUIConfiguration config, String tag) {
            super (config, tag);
            Boolean value = (Boolean)config.getConfigArray().get(tag);

            toggleBotton = CycleButton.onOffBuilder(value).displayOnlyValue().create(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, name, (press, boolean_) -> {
                configuration.changeValue(tag, boolean_);
            });
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            super.render(poseStack, index, top, left, width, height, mouseX, mouseY, isMouseOver, partialTick);

            this.toggleBotton.setX(left + 105);
            this.toggleBotton.setY(top);
            this.toggleBotton.render(poseStack, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.resetButton, this.toggleBotton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.resetButton, this.toggleBotton);
        }

    }

    public class ColorConfigEntry extends ConfigEntry {
        private final EditBox textField;

        public ColorConfigEntry(CUIConfiguration config, String tag) {
            super(config, tag);

            Colour cValue = (Colour)config.getConfigArray().get(tag);
            textField = new EditBox(CUIConfigList.this.minecraft.font, 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal(cValue.hexString()));
            textField.setMaxLength(9); // # + 8 hex chars
            textField.setValue(cValue.hexString());
            textField.setResponder(updated -> {
                Colour tested = Colour.parseRgbaOrNull(updated);
                if (tested != null) {
                    config.changeValue(tag, tested);
                }
            });
            textField.setFormatter((string, integer) -> {
                if (string.length() != 9) {
                    return FormattedCharSequence.forward(string, invalidFormat);
                }
                TextColor parsed = TextColor.parseColor(string.substring(0, 7));
                if (parsed == null) {
                    return FormattedCharSequence.forward(string, invalidFormat);
                }
                return FormattedCharSequence.forward(string, Style.EMPTY.withColor(parsed));
            });
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            super.render(poseStack, index, top, left, width, height, mouseX, mouseY, isMouseOver, partialTick);
            this.textField.setX(left + 105);
            this.textField.setY(top);
            this.textField.render(poseStack, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.resetButton, this.textField);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.resetButton, this.textField);
        }

    }

    public abstract class ConfigEntry extends ContainerObjectSelectionList.Entry<ConfigEntry> {
        protected final Component name;
        protected final Button resetButton;

        public ConfigEntry(CUIConfiguration config, String tag) {
            this.name = config.getDescription(tag);

            this.resetButton = Button.builder(Component.translatable("controls.reset"), (button) ->
                    config.changeValue(tag, config.getDefaultValue(tag))
            ).bounds(0, 0, 50, BUTTON_HEIGHT).build();

        }
        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int y = top + height / 2;
            float textLeft = (float)(left + 90 - maxNameWidth);
            minecraft.font.draw(poseStack, this.name, textLeft, (float)(y - 9 / 2), 0xFFFFFF);
            this.resetButton.setX(left + 190);
            this.resetButton.setY(top);
            this.resetButton.render(poseStack, mouseX, mouseY, partialTick);
        }
    }
}
