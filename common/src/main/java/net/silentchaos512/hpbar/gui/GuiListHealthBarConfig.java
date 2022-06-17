package net.silentchaos512.hpbar.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.silentchaos512.hpbar.config.Config;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GuiListHealthBarConfig extends ObjectSelectionList<GuiListHealthBarConfig.Entry> {
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

        Map<String, Config.ConfigSpecObject> spec = getAtPath(Config.rootSection.getSpec(), this.gui.getCurrentPath());

        spec.forEach((k, v) ->
        {
            if (v instanceof Config.BooleanValue) {
                entries.add(new BooleanEntry(k, (Config.BooleanValue) v));
            } else if (v instanceof Config.RangeIntValue) {
                entries.add(new IntEntry(k, (Config.RangeIntValue) v));
            } else if (v instanceof Config.RangeDoubleValue) {
                entries.add(new DoubleEntry(k, (Config.RangeDoubleValue) v));
            } else if (v instanceof Config.EnumValue) {
                entries.add(new EnumEntry<>(k, (Config.EnumValue<?>) v));
            } else if (v instanceof Config.StringValue) {
                entries.add(new StringEntry(k, (Config.StringValue) v));
            } else if (v instanceof Config.ConfigSection) {
                entries.add(new GroupEntry(k));
            }
        });


        setSelected(null);
        setScrollAmount(0);
        replaceEntries(entries);
    }

    private Map<String, Config.ConfigSpecObject> getAtPath(Map<String, Config.ConfigSpecObject> spec, List<String> path)
    {
        if (path.isEmpty())
        {
            return spec;
        }

        Map<String, Config.ConfigSpecObject> last = convertToMap(spec.get(path.get(0)));

        for (int i = 1; i < path.size(); i++)
        {
            last = convertToMap(last.get(path.get(i)));
        }

        return last;
    }

    private Map<String, Config.ConfigSpecObject> convertToMap(Object object)
    {
        return ((Config.ConfigSection) object).getSpec();
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 60;
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    public void drawTooltips(PoseStack matrixStack, int mouseX, int mouseY) {
        for (Entry e : children()) {
            e.drawTooltip(matrixStack, mouseX, mouseY);
        }
    }

    public abstract class Entry extends ObjectSelectionList.Entry<Entry> {
        protected String name;

        protected Entry(String name) {
            this.name = name;
        }

        protected abstract void drawTooltip(PoseStack matrixStack, int mouseX, int mouseY);
    }

    public abstract class ConfigEntry<T, V extends Config.ConfigValue<T>> extends Entry {
        protected V value;
        protected HoverChecker hoverChecker;

        protected ConfigEntry(String name, V value) {
            super(name);

            this.value = value;
        }

        @Override
        protected void drawTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
            if (this.hoverChecker != null && this.hoverChecker.checkHover(mouseX, mouseY)) {
                drawHoveringText(matrixStack, Arrays.asList(
                                new TextComponent(this.name).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
                                new TextComponent(this.value.getDescription()).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
                                new TextComponent("[default: " + this.value.getDefaultValue() + "]").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))),
                        mouseX, mouseY);
            }
        }

        protected void drawHoveringText(PoseStack matrixStack, List<Component> texts, int mouseX, int mouseY)
        {
            if (!texts.isEmpty())
            {
                int i = 0;
                Iterator<Component> var6 = texts.iterator();

                while(var6.hasNext())
                {
                    Component orderedText = var6.next();
                    int j = minecraft.font.width(orderedText);

                    if (j > i)
                    {
                        i = j;
                    }
                }

                int k = mouseX + 12;
                int l = mouseY - 12;
                int n = 8;

                if (texts.size() > 1)
                {
                    n += 2 + (texts.size() - 1) * 10;
                }

                if (k + i > width)
                {
                    k -= 28 + i;
                }

                if (l + n + 6 > height)
                {
                    l = height - n - 6;
                }

                matrixStack.pushPose();
                int o = -267386864;
                int p = 1347420415;
                int q = 1344798847;
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                Matrix4f matrix4f = matrixStack.last().pose();
                fillGradient(matrix4f, bufferBuilder, k - 3, l - 4, k + i + 3, l - 3, 400, o, o);
                fillGradient(matrix4f, bufferBuilder, k - 3, l + n + 3, k + i + 3, l + n + 4, 400, o, o);
                fillGradient(matrix4f, bufferBuilder, k - 3, l - 3, k + i + 3, l + n + 3, 400, o, o);
                fillGradient(matrix4f, bufferBuilder, k - 4, l - 3, k - 3, l + n + 3, 400, o, o);
                fillGradient(matrix4f, bufferBuilder, k + i + 3, l - 3, k + i + 4, l + n + 3, 400, o, o);
                fillGradient(matrix4f, bufferBuilder, k - 3, l - 3 + 1, k - 3 + 1, l + n + 3 - 1, 400, p, q);
                fillGradient(matrix4f, bufferBuilder, k + i + 2, l - 3 + 1, k + i + 3, l + n + 3 - 1, 400, p, q);
                fillGradient(matrix4f, bufferBuilder, k - 3, l - 3, k + i + 3, l - 3 + 1, 400, p, p);
                fillGradient(matrix4f, bufferBuilder, k - 3, l + n + 2, k + i + 3, l + n + 3, 400, q, q);
                RenderSystem.enableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                bufferBuilder.end();
                BufferUploader.end(bufferBuilder);
                RenderSystem.disableBlend();
                RenderSystem.enableTexture();
                MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                matrixStack.translate(0.0D, 0.0D, 400.0D);

                for (int s = 0; s < texts.size(); ++s)
                {
                    Component text2 = texts.get(s);

                    if (text2 != null)
                    {
                        minecraft.font.drawInBatch(text2, k, l, -1, true, matrix4f, immediate, false, 0, 15728880);
                    }

                    if (s == 0)
                    {
                        l += 2;
                    }

                    l += 10;
                }

                immediate.endBatch();
                matrixStack.popPose();
            }
        }

        @Override
        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            if (this.hoverChecker == null) {
                this.hoverChecker = new HoverChecker(y, y + slotHeight, 20, 20 + minecraft.font.width(this.name), 400);
            } else {
                this.hoverChecker.updateBounds(y, y + slotHeight, 20, 20 + minecraft.font.width(this.name));
            }
        }

        @Override
        public Component getNarration() {
            return TextComponent.EMPTY;
        }
    }

    public class BooleanEntry extends ConfigEntry<Boolean, Config.BooleanValue> {
        private ColorableButton button;

        public BooleanEntry(String name, Config.BooleanValue value) {
            super(name, value);

            this.button = new ColorableButton(0, 0, 150, 18, new TextComponent(""), b -> this.value.set(!this.value.get()));
        }

        @Override
        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.font, this.name, 20, y + minecraft.font.lineHeight / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;
            this.button.setMessage(new TextComponent(this.value.get().toString()));
            this.button.setFGColor(this.value.get() ? ChatFormatting.DARK_GREEN.getColor() : ChatFormatting.DARK_RED.getColor());
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            super.render(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }
    }

    public class IntEntry extends ConfigEntry<Integer, Config.RangeIntValue> {
        private EditBox button;

        protected IntEntry(String name, Config.RangeIntValue value) {
            super(name, value);

            this.button = new EditBox(minecraft.font, 0, 0, 150, 18, new TextComponent(""));
            this.button.setValue(value.get().toString());
            this.button.setResponder(text -> {
                try {
                    int val = Integer.parseInt(text);

                    if (value.test(val)) {
                        value.set(val);
                    }
                } catch (NumberFormatException ignored) {}
            });
        }

        @Override
        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.font, this.name, 20, y + minecraft.font.lineHeight / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;

            try {
                int val = Integer.parseInt(this.button.getValue());

                if (!value.test(val)) {
                    this.button.setTextColor(ChatFormatting.RED.getColor());
                } else {
                    this.button.setTextColor(14737632);
                }
            }
            catch (NumberFormatException e) {
                this.button.setTextColor(ChatFormatting.RED.getColor());
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

        @Override
        protected void drawTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
            if (this.hoverChecker != null && this.hoverChecker.checkHover(mouseX, mouseY)) {
                drawHoveringText(matrixStack, Arrays.asList(
                                new TextComponent(this.name).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
                                new TextComponent(this.value.getDescription()).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
                                new TextComponent("[range: " + rangeToString(this.value.getMin(), this.value.getMax()) + ", default: " + this.value.getDefaultValue() + "]").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))),
                        mouseX, mouseY);
            }
        }

        private String rangeToString(int min, int max) {
            if (max == Integer.MAX_VALUE) {
                return "> " + min;
            }
            else if (min == Integer.MIN_VALUE) {
                return "< " + max;
            }

            return min + " ~ " + max;
        }
    }

    public class DoubleEntry extends ConfigEntry<Double, Config.RangeDoubleValue> {
        private EditBox button;

        protected DoubleEntry(String name, Config.RangeDoubleValue value) {
            super(name, value);

            this.button = new EditBox(minecraft.font, 0, 0, 150, 18, new TextComponent(""));
            this.button.setValue(value.get().toString());
            this.button.setResponder(text -> {
                try {
                    double val = Double.parseDouble(text);

                    if (value.test(val)) {
                        value.set(val);
                    }
                } catch (NumberFormatException ignored) {}
            });
        }

        @Override
        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.font, this.name, 20, y + minecraft.font.lineHeight / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;

            try {
                double val = Double.parseDouble(this.button.getValue());

                if (!value.test(val)) {
                    this.button.setTextColor(ChatFormatting.RED.getColor());
                } else {
                    this.button.setTextColor(14737632);
                }
            }
            catch (NumberFormatException e) {
                this.button.setTextColor(ChatFormatting.RED.getColor());
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

        @Override
        protected void drawTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
            if (this.hoverChecker != null && this.hoverChecker.checkHover(mouseX, mouseY)) {
                drawHoveringText(matrixStack, Arrays.asList(
                                new TextComponent(this.name).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
                                new TextComponent(this.value.getDescription()).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)),
                                new TextComponent("[range: " + rangeToString(this.value.getMin(), this.value.getMax()) + ", default: " + this.value.getDefaultValue() + "]").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))),
                        mouseX, mouseY);
            }
        }

        private String rangeToString(double min, double max) {
            if (max >= Double.MAX_VALUE) {
                return "> " + min;
            }
            else if (min <= -Double.MAX_VALUE) {
                return "< " + max;
            }

            return min + " ~ " + max;
        }
    }

    public class StringEntry extends ConfigEntry<String, Config.StringValue> {
        private EditBox button;

        protected StringEntry(String name, Config.StringValue value) {
            super(name, value);

            this.button = new EditBox(minecraft.font, 0, 0, 150, 18, new TextComponent(""));
            this.button.setValue(value.get());
            this.button.setResponder(text -> {
                if (value.test(text)) {
                    value.set(text);
                }
            });
        }

        @Override
        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.font, this.name, 20, y + minecraft.font.lineHeight / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;

            if (!value.test(this.button.getValue())) {
                this.button.setTextColor(ChatFormatting.RED.getColor());
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

    public class EnumEntry<E extends Enum<E>> extends ConfigEntry<E, Config.EnumValue<E>> {
        private ColorableButton button;
        private E[] values;

        @SuppressWarnings("unchecked")
        public EnumEntry(String name, Config.EnumValue<E> value) {
            super(name, value);

            this.button = new ColorableButton(0, 0, 150, 18, new TextComponent(""), b -> this.value.set(values[(this.value.get().ordinal() + 1) % values.length]));
            this.values = value.getClazz().getEnumConstants();
        }

        @Override
        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            drawString(matrixStack, minecraft.font, this.name, 20, y + minecraft.font.lineHeight / 2, 14737632);

            this.button.x = x + listWidth / 2 + 30;
            this.button.y = y;
            this.button.setMessage(new TextComponent(this.value.get().toString()));
            this.button.render(matrixStack, mouseX, mouseY, partialTicks);

            super.render(matrixStack, slotIndex, y, x, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            return this.button.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        }
    }

    public class GroupEntry extends Entry {
        private ColorableButton button;

        public GroupEntry(String name) {
            super(name);

            this.button = new ColorableButton(0, 0, 300, 18, new TextComponent(name), b -> {
            });
        }

        @Override
        protected void drawTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
        }

        @Override
        public void render(PoseStack matrixStack, int slotIndex, int y, int x, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
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

        @Override
        public Component getNarration() {
            return TextComponent.EMPTY;
        }
    }
}