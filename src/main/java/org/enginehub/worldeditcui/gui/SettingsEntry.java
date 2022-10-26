package org.enginehub.worldeditcui.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * @author Jesús Sanz - Modified to implement Config GUI / First Version
 */
public class SettingsEntry extends ObjectSelectionList.Entry<SettingsEntry> {
    private static final int LINE_HEIGHT = 11;

    protected final Minecraft client;
    protected final SettingsWidget list;
    protected final Component keyword;
    private List<FormattedCharSequence> wrappedKeyword;
    private int lastWidth = -1;
    protected final AbstractWidget widgetButton;
    protected final AbstractWidget resetButton;

    public SettingsEntry(SettingsWidget list, Component keyword, AbstractWidget widgetButton, AbstractWidget resetButton) {
        this.list = list;
        this.client = Minecraft.getInstance();
        this.keyword = keyword;
        this.widgetButton = widgetButton;
        this.resetButton = resetButton;
    }

    @Override
    public void render(PoseStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovering, float delta) {
        // Label
        final int maxNameWidth = rowWidth / 2 - 40;
        if (maxNameWidth != this.lastWidth) {
            this.wrappedKeyword = this.client.font.split(this.keyword, maxNameWidth);
            this.lastWidth = maxNameWidth;
        }

        int quarterEntries = this.wrappedKeyword.size() >= 2 ? this.wrappedKeyword.size() / 2 : 0;
        int lineYOffset = (rowHeight / 2) - quarterEntries * (this.client.font.lineHeight + 2) - this.client.font.lineHeight / 2;
        for (final FormattedCharSequence text : this.wrappedKeyword) {
            this.client.font.draw(matrices, text, x + 50, y + lineYOffset, 0xFFFFFF);
            lineYOffset += LINE_HEIGHT;
        }

        // Widgets
        this.resetButton.setY(y + 1);
        this.widgetButton.setY(y + 1);
        this.widgetButton.render(matrices, mouseX, mouseY, delta);
        this.resetButton.render(matrices,mouseX, mouseY, delta);
    }

    @Override
    public Component getNarration() {
        return this.keyword;
    }
}
