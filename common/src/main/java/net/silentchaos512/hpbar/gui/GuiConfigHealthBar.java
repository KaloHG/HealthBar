package net.silentchaos512.hpbar.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.silentchaos512.hpbar.config.Config;

import java.util.ArrayList;
import java.util.List;

public class GuiConfigHealthBar extends Screen {
    private Screen parentScreen;
    private GuiListHealthBarConfig list;
    private Button backButton;
    private List<String> currentPath = new ArrayList<>();

    public GuiConfigHealthBar(Screen parentScreen) {
        super(new TextComponent("Health Bar Config"));

        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        this.list = new GuiListHealthBarConfig(this);
        addWidget(this.list);

        this.backButton = new Button(this.width / 2 - 154, this.height - 30, 150, 20, new TextComponent("Back"), button -> onClose());
        addRenderableWidget(this.backButton);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, !this.currentPath.isEmpty() ? new TextComponent(this.currentPath.get(this.currentPath.size() - 1)) : this.title, this.width / 2, 8, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.list.drawTooltips(matrixStack, mouseX, mouseY);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean success = super.mouseClicked(mouseX, mouseY, button);

        for (GuiListHealthBarConfig.Entry e : list.children()) {
            if (!e.isMouseOver(mouseX, mouseY)) {
                e.mouseClicked(mouseX, mouseY, button);
            }
        }

        return success;
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_){
        this.list.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public void onClose() {
        if (!this.currentPath.isEmpty()) {
            backwardPath();
        } else {
            Config.save();
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public void forwardPath(String path){
        this.currentPath.add(path);
        reset();
    }

    public void backwardPath() {
        this.currentPath.remove(this.currentPath.size() - 1);
        reset();
    }

    public void reset() {
        this.list.buildEntries();
    }

    public List<String> getCurrentPath() {
        return this.currentPath;
    }

    public Minecraft getMinecraft()
    {
        return minecraft;
    }
}