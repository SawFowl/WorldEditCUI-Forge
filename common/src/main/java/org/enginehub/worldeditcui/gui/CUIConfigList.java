package org.enginehub.worldeditcui.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
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
            .withColor(ChatFormatting.DARK_RED)
            .withUnderlined(true);

    private final CUIConfiguration configuration;
    int maxNameWidth = 0;

    public CUIConfigList(CUIConfigPanel panel, Minecraft minecraft) {
        super(minecraft, panel.width + 45, panel.height, 20, panel.height - 32, 25);
        this.configuration = panel.configuration;
        this.setRenderBackground(minecraft.level == null);

        for (String key : this.configuration.getConfigArray().keySet()) {
            Object value = configuration.getConfigArray().get(key);

            maxNameWidth = Math.max(maxNameWidth, minecraft.font.width(configuration.getDescription(key)));

            if (value instanceof Boolean) {
                this.addEntry(new OnOffEntry(key));
            } else if (value instanceof Colour) {
                this.addEntry(new ColorConfigEntry(key));
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

        public OnOffEntry(String tag) {
            super (tag);
            Boolean value = (Boolean)configuration.getConfigArray().get(tag);

            toggleBotton = CycleButton.onOffBuilder(value).displayOnlyValue().create(0, 0,
                    BUTTON_WIDTH, BUTTON_HEIGHT, configuration.getDescription(tag),
                    (press, boolean_) -> configuration.changeValue(tag, boolean_)
            );
        }

        @Override
        public void render(PoseStack gfx, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            super.render(gfx, index, top, left, width, height, mouseX, mouseY, isMouseOver, partialTick);

            this.toggleBotton.setX(left + 105);
            this.toggleBotton.setY(top);
            this.toggleBotton.render(gfx, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.resetButton, this.toggleBotton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.resetButton, this.toggleBotton);
        }

        @Override
        protected void updateFromConfig() {
            this.toggleBotton.setValue((Boolean)configuration.getConfigArray().get(tag));
        }
    }

    public class ColorConfigEntry extends ConfigEntry {
        private final EditBox textField;

        public ColorConfigEntry(String tag) {
            super(tag);

            Colour cValue = (Colour)configuration.getConfigArray().get(tag);
            textField = new EditBox(CUIConfigList.this.minecraft.font, 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal(cValue.hexString()));
            textField.setMaxLength(9); // # + 8 hex chars
            textField.setValue(cValue.hexString());
            textField.setResponder(updated -> {
                Colour tested = Colour.parseRgbaOrNull(updated);
                if (tested != null) {
                    configuration.changeValue(tag, tested);
                }
            });
            textField.setFormatter((string, integer) -> {
                final String colorSource = textField.getValue();
                if (colorSource.length() != 9) {
                    return FormattedCharSequence.forward(string, invalidFormat);
                }
                TextColor parsed = TextColor.parseColor(colorSource.substring(0, 7));
                if (parsed == null) {
                    return FormattedCharSequence.forward(string, invalidFormat);
                }
                return FormattedCharSequence.forward(string, Style.EMPTY.withColor(parsed));
            });
            textField.setFilter(value -> {
                // filter for #AARRGGBB
                if (value.length() >= 1 && value.charAt(0) != '#') { // does not start with hex
                    return false;
                }

                for (int i = 1; i < value.length(); i++) { // any characters that are not valid in a hex string
                    final char c = value.charAt(i);
                    if ((c < '0' || c > '9') && (c < 'A' || c > 'F') && (c < 'a' || c > 'f')) {
                        return false;
                    }
                }

                return true;
            });
        }

        @Override
        public void render(PoseStack gfx, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            super.render(gfx, index, top, left, width, height, mouseX, mouseY, isMouseOver, partialTick);
            this.textField.setX(left + 105);
            this.textField.setY(top);
            this.textField.render(gfx, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.resetButton, this.textField);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.resetButton, this.textField);
        }

        @Override
        protected void updateFromConfig() {
            this.textField.setValue(((Colour)configuration.getConfigArray().get(tag)).hexString());
        }
    }

    public abstract class ConfigEntry extends ContainerObjectSelectionList.Entry<ConfigEntry> {
        protected final String tag;
        protected final Button resetButton;
        protected final StringWidget textField;

        public ConfigEntry(String tag) {
            this.tag = tag;

            this.resetButton = Button.builder(Component.translatable("controls.reset"), (button) -> {
                configuration.changeValue(tag, configuration.getDefaultValue(tag));
                updateFromConfig();
            }).bounds(0, 0, 50, BUTTON_HEIGHT).build();

            textField = new StringWidget(configuration.getDescription(tag), minecraft.font);
            textField.alignLeft();
            Component tooltip = configuration.getTooltip(tag);
            if (tooltip != null) {
                textField.setTooltip(Tooltip.create(tooltip));
            }

        }
        @Override
        public void render(PoseStack gfx, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int textLeft = left + 90 - maxNameWidth;

            this.textField.setX(textLeft);
            this.textField.setY(top);
            this.textField.render(gfx, mouseX, mouseY, partialTick);

            this.resetButton.setX(left + 190);
            this.resetButton.setY(top);
            this.resetButton.render(gfx, mouseX, mouseY, partialTick);
        }

        protected abstract void updateFromConfig();
    }
}
