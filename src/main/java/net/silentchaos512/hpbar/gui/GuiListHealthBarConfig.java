package net.silentchaos512.hpbar.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.silentchaos512.hpbar.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GuiListHealthBarConfig extends ExtendedList<GuiListHealthBarConfig.Entry> {
    private GuiConfigHealthBar gui;

    public GuiListHealthBarConfig(GuiConfigHealthBar gui) {
        super(gui.getMinecraft(), gui.width, gui.height, 23, gui.height - 32, 20);

        this.gui = gui;
        buildEntries();
    }

    @SuppressWarnings("unchecked")
    public void buildEntries() {
        Set<Entry> entries = new TreeSet<>((a, b) ->
        {
            //Sort by name of Entry type so we can have the same types of entries together
            int typeComp = a.getClass().getSimpleName().compareToIgnoreCase(b.getClass().getSimpleName());

            if (typeComp == 0) {
                //After we have separated entries by type, we want to sort by their names
                return a.name.compareToIgnoreCase(b.name);
            } else {
                return typeComp;
            }
        });

        Map<String, Object> spec = getAtPath(Config.getConfiguration().getValues().valueMap(), gui.getCurrentPath());
        Map<String, Object> valueSpec = Config.getConfiguration().valueMap();

        spec.forEach((k, v) ->
        {
            ForgeConfigSpec.ValueSpec vs = v instanceof ForgeConfigSpec.ConfigValue ? getValueSpec(valueSpec, gui.getCurrentPath(), k) : null;

            if (v instanceof ForgeConfigSpec.BooleanValue) {
                entries.add(new BooleanEntry(k, (ForgeConfigSpec.BooleanValue) v, vs));
            } else if (v instanceof ForgeConfigSpec.IntValue) {
                entries.add(new IntEntry(k, (ForgeConfigSpec.IntValue) v, vs));
            } else if (v instanceof ForgeConfigSpec.DoubleValue) {
                entries.add(new DoubleEntry(k, (ForgeConfigSpec.DoubleValue) v, vs));
            } else if (v instanceof ForgeConfigSpec.EnumValue) {
                entries.add(new EnumEntry<>(k, (ForgeConfigSpec.EnumValue<?>) v, vs));
            } else if (vs != null && vs.getDefault() instanceof String) {
                entries.add(new StringEntry(k, (ForgeConfigSpec.ConfigValue<String>) v, vs));
            } else if (v instanceof com.electronwill.nightconfig.core.Config || v instanceof Map) {
                entries.add(new GroupEntry(k));
            }
        });

        setSelected(null);
        setScrollAmount(0);
        replaceEntries(entries);
    }

    private Map<String, Object> getAtPath(Map<String, Object> spec, List<String> path) {
        path = new ArrayList<>(path);
        path.add(0, Config.CAT_BAR);

        if (path.isEmpty()) {
            return spec;
        }

        Map<String, Object> last = convertToMap(spec.get(path.get(0)));

        for (int i = 1; i < path.size(); i++) {
            last = convertToMap(last.get(path.get(i)));
        }

        return last;
    }

    private ForgeConfigSpec.ValueSpec getValueSpec(Map<String, Object> spec, List<String> path, String key) {
        path = new ArrayList<>(path);
        path.add(0, Config.CAT_BAR);

        Map<String, Object> last = convertToMap(spec.get(path.get(0)));

        for (int i = 1; i < path.size(); i++) {
            last = convertToMap(last.get(path.get(i)));
        }

        return (ForgeConfigSpec.ValueSpec) last.get(key);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object object) {
        if (object instanceof com.electronwill.nightconfig.core.Config) {
            return ((com.electronwill.nightconfig.core.Config) object).valueMap();
        }

        return (Map<String, Object>) object;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 60;
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    public void drawTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
        for (Entry e : getEventListeners()) {
            e.drawTooltip(matrixStack, mouseX, mouseY);
        }
    }

    public abstract class Entry extends ExtendedList.AbstractListEntry<Entry> {
        protected String name;

        protected Entry(String name) {
            this.name = name;
        }

        protected abstract void drawTooltip(MatrixStack matrixStack, int mouseX, int mouseY);
    }

    public abstract class ConfigEntry<T, V extends ForgeConfigSpec.ConfigValue<T>> extends Entry {
        protected V value;
        protected ForgeConfigSpec.ValueSpec spec;
        protected HoverChecker hoverChecker;

        protected ConfigEntry(String name, V value, ForgeConfigSpec.ValueSpec spec) {
            super(name);

            this.value = value;
            this.spec = spec;
        }

        @Override
        protected void drawTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
            if (this.hoverChecker != null && this.hoverChecker.checkHover(mouseX, mouseY)) {
                GuiUtils.drawHoveringText(matrixStack, Arrays.asList(
                        new StringTextComponent(this.name).setStyle(Style.EMPTY.setFormatting(TextFormatting.GREEN)),
                        new StringTextComponent(this.spec.getComment().substring(0, this.spec.getComment().length() - (this.spec.getRange() != null ? ("Range: " + this.spec.getRange()).length() + 1 : 0))).setStyle(Style.EMPTY.setFormatting(TextFormatting.YELLOW)),
                        new StringTextComponent("[" + (this.spec.getRange() != null ? "range: " + this.spec.getRange() + ", " : "") + "default: " + this.spec.getDefault() + "]").setStyle(Style.EMPTY.setFormatting(TextFormatting.AQUA))),
                        mouseX, mouseY, gui.width, gui.height, 300, minecraft.fontRenderer);
            }
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            if (this.hoverChecker == null) {
                this.hoverChecker = new HoverChecker(y, y + slotHeight, 20, 20 + minecraft.fontRenderer.getStringWidth(this.name), 400);
            } else {
                this.hoverChecker.updateBounds(y, y + slotHeight, 20, 20 + minecraft.fontRenderer.getStringWidth(this.name));
            }
        }

        //Why the hell is Range a private class Forge?
        @SuppressWarnings("unchecked")
        protected T getRangeMin() {
            try {
                return (T) ObfuscationReflectionHelper.findMethod(getRangeClass(), "getMin").invoke(this.spec.getRange());
            } catch (Exception e) {
                throw new RuntimeException("Unable to get range min?", e);
            }
        }

        @SuppressWarnings("unchecked")
        protected T getRangeMax() {
            try {
                return (T) ObfuscationReflectionHelper.findMethod(getRangeClass(), "getMax").invoke(this.spec.getRange());
            } catch (Exception e) {
                throw new RuntimeException("Unable to get range max?", e);
            }
        }

        private Class<?> getRangeClass() {
            try {
                return Class.forName(ForgeConfigSpec.class.getName() + "$Range");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Can't find Range class?", e);
            }
        }
    }

    public class BooleanEntry extends ConfigEntry<Boolean, ForgeConfigSpec.BooleanValue> {
        private ExtendedButton button;

        public BooleanEntry(String name, ForgeConfigSpec.BooleanValue value, ForgeConfigSpec.ValueSpec spec) {
            super(name, value, spec);

            this.button = new ExtendedButton(0, 0, 150, 18, new StringTextComponent(""), b -> this.value.set(!this.value.get()));
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.fontRenderer, this.name, 20, y + minecraft.fontRenderer.FONT_HEIGHT / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;
            this.button.setMessage(new StringTextComponent(this.value.get().toString()));
            this.button.setFGColor(this.value.get() ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true));
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            super.render(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }
    }

    public class IntEntry extends ConfigEntry<Integer, ForgeConfigSpec.IntValue> {
        private TextFieldWidget button;

        protected IntEntry(String name, ForgeConfigSpec.IntValue value, ForgeConfigSpec.ValueSpec spec) {
            super(name, value, spec);

            this.button = new TextFieldWidget(minecraft.fontRenderer, 0, 0, 150, 18, new StringTextComponent(""));
            this.button.setText(value.get().toString());
            this.button.setResponder(text -> {
                try {
                    int val = Integer.parseInt(text);

                    if (spec.test(val)) {
                        value.set(val);
                    }
                } catch (NumberFormatException ignored) {}
            });
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.fontRenderer, this.name, 20, y + minecraft.fontRenderer.FONT_HEIGHT / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;

            try {
                int val = Integer.parseInt(this.button.getText());

                if (!spec.test(val)) {
                    this.button.setTextColor(TextFormatting.RED.getColor());
                } else {
                    this.button.setTextColor(14737632);
                }
            }
            catch (NumberFormatException e) {
                this.button.setTextColor(TextFormatting.RED.getColor());
            }

            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            super.render(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            return this.button.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }

        @Override
        public boolean changeFocus(boolean p_changeFocus_1_) {
            return this.button.changeFocus(p_changeFocus_1_);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.button.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return this.button.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return this.button.charTyped(codePoint, modifiers);
        }
    }

    public class DoubleEntry extends ConfigEntry<Double, ForgeConfigSpec.DoubleValue> {
        private TextFieldWidget button;

        protected DoubleEntry(String name, ForgeConfigSpec.DoubleValue value, ForgeConfigSpec.ValueSpec spec) {
            super(name, value, spec);

            this.button = new TextFieldWidget(minecraft.fontRenderer, 0, 0, 150, 18, new StringTextComponent(""));
            this.button.setText(value.get().toString());
            this.button.setResponder(text -> {
                try {
                    double val = Double.parseDouble(text);

                    if (spec.test(val)) {
                        value.set(val);
                    }
                } catch (NumberFormatException ignored) {}
            });
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.fontRenderer, this.name, 20, y + minecraft.fontRenderer.FONT_HEIGHT / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;

            try {
                double val = Double.parseDouble(this.button.getText());

                if (!spec.test(val)) {
                    this.button.setTextColor(TextFormatting.RED.getColor());
                } else {
                    this.button.setTextColor(14737632);
                }
            }
            catch (NumberFormatException e) {
                this.button.setTextColor(TextFormatting.RED.getColor());
            }

            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            super.render(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            return this.button.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }

        @Override
        public boolean changeFocus(boolean p_changeFocus_1_) {
            return this.button.changeFocus(p_changeFocus_1_);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.button.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return this.button.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return this.button.charTyped(codePoint, modifiers);
        }
    }

    public class StringEntry extends ConfigEntry<String, ForgeConfigSpec.ConfigValue<String>> {
        private TextFieldWidget button;

        protected StringEntry(String name, ForgeConfigSpec.ConfigValue<String> value, ForgeConfigSpec.ValueSpec spec) {
            super(name, value, spec);

            this.button = new TextFieldWidget(minecraft.fontRenderer, 0, 0, 150, 18, new StringTextComponent(""));
            this.button.setText(value.get());
            this.button.setResponder(text -> {
                if (spec.test(text)) {
                    value.set(text);
                }
            });
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.fontRenderer, this.name, 20, y + minecraft.fontRenderer.FONT_HEIGHT / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;

            if (!spec.test(this.button.getText())) {
                this.button.setTextColor(TextFormatting.RED.getColor());
            } else {
                this.button.setTextColor(14737632);
            }

            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            super.render(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            return this.button.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }

        @Override
        public boolean changeFocus(boolean p_changeFocus_1_) {
            return this.button.changeFocus(p_changeFocus_1_);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.button.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return this.button.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return this.button.charTyped(codePoint, modifiers);
        }
    }

    public class EnumEntry<E extends Enum<E>> extends ConfigEntry<E, ForgeConfigSpec.EnumValue<E>> {
        private ExtendedButton button;
        private E[] values;

        @SuppressWarnings("unchecked")
        public EnumEntry(String name, ForgeConfigSpec.EnumValue<E> value, ForgeConfigSpec.ValueSpec spec) {
            super(name, value, spec);

            this.button = new ExtendedButton(0, 0, 150, 18, new StringTextComponent(""), b -> this.value.set(values[(this.value.get().ordinal() + 1) % values.length]));
            this.values = (E[]) spec.getClazz().getEnumConstants();
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.fontRenderer, this.name, 20, y + minecraft.fontRenderer.FONT_HEIGHT / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;
            this.button.setMessage(new StringTextComponent(this.value.get().toString()));
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            super.render(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }
    }

    public class GroupEntry extends Entry {
        private ExtendedButton button;

        public GroupEntry(String name) {
            super(name);

            this.button = new ExtendedButton(0, 0, 300, 18, new StringTextComponent(name), b -> {
            });
        }

        @Override
        protected void drawTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        }

        @Override
        public void render(MatrixStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            this.button.x = x + listWidth / 2 - 150;
            this.button.y = y;
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            if (this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
                gui.forwardPath(this.name);
            }

            return true;
        }
    }
}