package com.wkrs14.oretracker.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class TrackerScreen extends Screen {
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 238;

    private final Map<OreType, Button> oreButtons = new EnumMap<>(OreType.class);
    private int panelX;
    private int panelY;
    private Button statusButton;
    private Button lineButton;

    public TrackerScreen() {
        super(Component.translatable("screen.oretracker.title"));
    }

    @Override
    protected void init() {
        oreButtons.clear();
        panelX = (width - Math.min(PANEL_WIDTH, width - 16)) / 2;
        panelY = Math.max(2, (height - PANEL_HEIGHT) / 2);
        int panelWidth = Math.min(PANEL_WIDTH, width - 16);
        int innerX = panelX + 12;
        int innerWidth = panelWidth - 24;
        int halfWidth = (innerWidth - 6) / 2;

        statusButton = addRenderableWidget(Button.builder(statusText(), button -> {
            OreTrackerConfig.setEnabled(!OreTrackerConfig.isEnabled());
            refreshStatus();
        }).bounds(innerX, panelY + 32, halfWidth, 18).build());

        lineButton = addRenderableWidget(Button.builder(lineText(), button -> {
            OreTrackerConfig.toggleShowLines();
            refreshStatus();
        }).bounds(innerX + halfWidth + 6, panelY + 32, halfWidth, 18).build());

        addRenderableWidget(new IntSlider(innerX, panelY + 56, innerWidth, 17,
                "screen.oretracker.scan_radius", 8, 64,
                OreTrackerConfig::getScanRadius, OreTrackerConfig::setScanRadius));
        addRenderableWidget(new IntSlider(innerX, panelY + 76, innerWidth, 17,
                "screen.oretracker.vertical_radius", 4, 48,
                OreTrackerConfig::getVerticalRadius, OreTrackerConfig::setVerticalRadius));
        addRenderableWidget(new IntSlider(innerX, panelY + 96, innerWidth, 17,
                "screen.oretracker.line_range", 8, 96,
                OreTrackerConfig::getLineRange, OreTrackerConfig::setLineRange));

        int oreWidth = (innerWidth - 6) / 2;
        int row = 0;
        for (OreType type : OreType.values()) {
            int column = row % 2;
            int line = row / 2;
            Button button = addRenderableWidget(Button.builder(oreText(type), pressed -> {
                OreTrackerConfig.setOreEnabled(type, !OreTrackerConfig.isOreEnabled(type));
                refreshOreButton(type);
            }).bounds(innerX + column * (oreWidth + 6), panelY + 127 + line * 16, oreWidth, 14).build());
            oreButtons.put(type, button);
            row++;
        }

        int bottomWidth = (innerWidth - 12) / 4;
        addRenderableWidget(Button.builder(Component.translatable("screen.oretracker.all_on"), button -> {
            OreTrackerConfig.setAllOres(true);
            oreButtons.forEach((type, widget) -> refreshOreButton(type));
        }).bounds(innerX, panelY + 211, bottomWidth, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.oretracker.all_off"), button -> {
            OreTrackerConfig.setAllOres(false);
            oreButtons.forEach((type, widget) -> refreshOreButton(type));
        }).bounds(innerX + bottomWidth + 4, panelY + 211, bottomWidth, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.oretracker.reset"), button -> {
            OreTrackerConfig.resetDefaults();
            rebuildWidgets();
        }).bounds(innerX + (bottomWidth + 4) * 2, panelY + 211, bottomWidth, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.oretracker.done"), button -> onClose())
                .bounds(innerX + (bottomWidth + 4) * 3, panelY + 211, bottomWidth, 18).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int panelWidth = Math.min(PANEL_WIDTH, width - 16);
        guiGraphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + PANEL_HEIGHT + 1, 0xFF686868);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + PANEL_HEIGHT, 0xE8121212);
        int centerX = panelX + panelWidth / 2;
        guiGraphics.drawCenteredString(font, title, centerX, panelY + 6, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(font, Component.translatable("screen.oretracker.subtitle", OreScanner.resultCount()),
                centerX, panelY + 18, 0xFFB8B8B8);
        guiGraphics.drawCenteredString(font, Component.translatable("screen.oretracker.ore_filters"),
                centerX, panelY + 115, 0xFFE0E0E0);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        OreTrackerConfig.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private void refreshStatus() {
        statusButton.setMessage(statusText());
        lineButton.setMessage(lineText());
    }

    private Component statusText() {
        Component state = Component.translatable(OreTrackerConfig.isEnabled()
                ? "screen.oretracker.enabled" : "screen.oretracker.disabled");
        return Component.translatable("screen.oretracker.status", state);
    }

    private Component lineText() {
        return Component.translatable(OreTrackerConfig.showLines()
                ? "screen.oretracker.lines_on" : "screen.oretracker.lines_off");
    }

    private static Component oreText(OreType type) {
        boolean enabled = OreTrackerConfig.isOreEnabled(type);
        return Component.literal(enabled ? "§a✓ " : "§8✗ ").append(Component.translatable(type.translationKey()));
    }

    private void refreshOreButton(OreType type) {
        Button button = oreButtons.get(type);
        if (button != null) {
            button.setMessage(oreText(type));
        }
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final String translationKey;
        private final int minimum;
        private final int maximum;
        private final IntConsumer setter;

        private IntSlider(int x, int y, int width, int height, String translationKey,
                          int minimum, int maximum, IntSupplier getter, IntConsumer setter) {
            super(x, y, width, height, Component.empty(),
                    (getter.getAsInt() - minimum) / (double) (maximum - minimum));
            this.translationKey = translationKey;
            this.minimum = minimum;
            this.maximum = maximum;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(translationKey, currentValue()));
        }

        @Override
        protected void applyValue() {
            setter.accept(currentValue());
        }

        private int currentValue() {
            return (int) Math.round(minimum + value * (maximum - minimum));
        }
    }
}



