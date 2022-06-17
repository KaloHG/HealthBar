package net.silentchaos512.hpbar.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.hpbar.HealthBar;
import net.silentchaos512.hpbar.config.Color;
import net.silentchaos512.hpbar.config.Config;

public class GuiHealthBar extends Screen {

    public static final ResourceLocation TEXTURE_FRAME = new ResourceLocation(
            HealthBar.RESOURCE_PREFIX, "textures/frame.png");
    public static final ResourceLocation TEXTURE_BAR = new ResourceLocation(HealthBar.RESOURCE_PREFIX,
            "textures/bar.png");

    private Minecraft mc;

    public GuiHealthBar(Minecraft mc) {

        super(new TextComponent(""));
        this.mc = mc;
    }

    public void onRenderGameOverlay(Window res, PoseStack stack, int vanillaYOffset) {

        // Don't render in creative mode?
        if (Config.replaceVanillaHealth.get() && mc.player.getAbilities().instabuild) {
            return;
        }

        float currentHealth = HealthBar.instance.getPlayerHealth();
        float maxHealth = HealthBar.instance.getPlayerMaxHealth();
        float lastDamage = HealthBar.instance.getPlayerLastDamageTaken();
        float healthFraction = currentHealth / maxHealth;

        // Hide at full health
        if (healthFraction >= 1f && !Config.barShowAlways.get() && !Config.replaceVanillaHealth.get()) {
            return;
        }

        int posX, posY;
        float scale;

        final boolean replaceVanilla = Config.replaceVanillaHealth.get();
        final int barWidth = replaceVanilla ? 80 : Config.barWidth.get();
        final int barHeight = replaceVanilla ? 8 : Config.barHeight.get();
        final float xOffset = Config.xOffset.get().floatValue();
        final float yOffset = Config.yOffset.get().floatValue();

        /*
         * Render a health bar
         */
        RenderSystem.enableBlend();

        scale = Config.barScale.get().floatValue();
        if (scale > 0f) {
            stack.pushPose();

            // Quiver when low on health
            double quiverIntensity = Math.max(Config.barQuiverFraction.get() - healthFraction + 0.01f, 0f)
                    * Config.barQuiverIntensity.get() / Config.barQuiverFraction.get();
            double quiverX = HealthBar.instance.random.nextGaussian() * quiverIntensity;
            double quiverY = HealthBar.instance.random.nextGaussian() * quiverIntensity;
            double quiverScale = HealthBar.instance.random.nextGaussian() * quiverIntensity * 0.05 + 1.0;
            scale *= quiverScale;

            if (replaceVanilla) {
                posX = (int) (res.getGuiScaledWidth() / 2 - 91 + quiverX);
                posY = (int) (res.getGuiScaledHeight() - vanillaYOffset + 20 + quiverY);
            } else {
                posX = (int) (res.getGuiScaledWidth() / scale * xOffset - barWidth / 2 + quiverX);
                posY = (int) (res.getGuiScaledHeight() / scale * yOffset + quiverY);
                stack.scale(scale, scale, 1);
            }

            // Health bar
            drawBar(stack, posX, posY, barWidth, barHeight, Config.colorHealthBar, healthFraction);
            // Bar frame
            drawBarFrame(stack, posX, posY, barWidth, barHeight);

            RenderSystem.enableBlend();
            stack.popPose();
        }

        /*
         * Render current/max health
         */

        scale = Config.textScale.get().floatValue();
        scale = scale > 0f && replaceVanilla ? 0.8f : scale;
        if (scale > 0f) {
            stack.pushPose();

            Font fontRender = mc.font;
            String format = Config.healthStringFormat.get();
            String str = String.format(format, currentHealth, maxHealth);
            // Add padding if current health has fewer digits than max.
            int extraSpaces = String.format("%.1f", maxHealth).length()
                    - String.format("%.1f", currentHealth).length();
            for (int i = 0; i < extraSpaces; ++i) {
                str = " " + str;
            }
            int stringWidth = fontRender.width(str);

            if (replaceVanilla) {
                final float paddingX = (barWidth - stringWidth * scale) / 2f;
                final float paddingY = (barHeight - fontRender.lineHeight / scale) / 2f;
                posX = (int) (res.getGuiScaledWidth() / 2 - 91 + paddingX);
                posY = (int) (res.getGuiScaledHeight() - vanillaYOffset + 20 - paddingY);
                stack.translate(posX, posY, 0); // y pos is a bit off for scale != 0.8f
                stack.scale(scale, scale, 1);
                fontRender.drawShadow(stack, str, 0, 0, 0xFFFFFF);
            } else {
                posX = (int) (res.getGuiScaledWidth() / scale * xOffset - stringWidth / 2);
                posY = (int) (res.getGuiScaledHeight() / scale * yOffset - 2) - barHeight;
                stack.scale(scale, scale, 1);
                fontRender.drawShadow(stack, str, posX, posY, 0xFFFFFF);
                // Last damage taken?
                if (Config.showLastDamageTaken.get()) {
                    str = String.format(Config.damageStringFormat.get(), lastDamage);
                    stringWidth = fontRender.width(str);
                    posX = (int) (res.getGuiScaledWidth() / scale * xOffset - stringWidth / 2);
                    posY = (int) (res.getGuiScaledHeight() / scale * yOffset - 5 - fontRender.lineHeight) - barHeight;
                    fontRender.drawShadow(stack, str, posX, posY, 0xFF4444);
                }
            }

            stack.popPose();
        }

        /*
         * FOOD BAR TEST
         */

//    posX = 5;
//    posY = 5;
//    scale = 1f;
//
//    int currentFood = mc.thePlayer.getFoodStats().getFoodLevel();
//    int maxFood = 20;
//    float currentSaturation = mc.thePlayer.getFoodStats().getSaturationLevel();
//    float maxSaturation = currentFood;
//
//    GL11.glPushMatrix();
//    drawBar(posX, posY, barWidth, barHeight, new Color(1f, 0.5f, 0f), (float) currentFood / maxFood);
//    drawBar(posX, posY + 3f / 4f * barHeight, barWidth, barHeight / 4f, new Color(1f, 0f, 1f), currentSaturation / maxSaturation);
//    drawBarFrame(posX, posY, barWidth, barHeight);
//    FontRenderer fontRender = mc.fontRendererObj;
//    String test = "%d (%.1f)";
//    test = String.format(test, currentFood, currentSaturation);
//    fontRender.drawStringWithShadow(test, posX, posY + barHeight, 0xFFFFFF);
//    GL11.glPopMatrix();

        //renderFoodBar(event);
    }

    protected void drawBar(PoseStack stack, float x, float y, float width, float height, Color color, float fraction) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE_BAR);
        RenderSystem.setShaderColor(color.red, color.green, color.blue, Config.barOpacity.get().floatValue());
        float barPosWidth = width * fraction;
        float barPosX = x;
        if (Config.barJustification.get() == Config.Justification.CENTER) {
            barPosX += width * (1f - fraction) / 2;
        } else if (Config.barJustification.get() == Config.Justification.RIGHT) {
            barPosX += width * (1f - fraction);
        }
        drawRect(stack, barPosX, y, 0, 0, barPosWidth, height);
    }

    protected void drawBarFrame(PoseStack stack, float x, float y, float width, float height) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE_FRAME);
        RenderSystem.setShaderColor(1, 1, 1, Config.barOpacity.get().floatValue());
        drawRect(stack, x, y, 0, 0, width, height);
    }

    public void drawRect(PoseStack stack, float x, float y, float u, float v, float width, float height) {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buff = tess.getBuilder();
        Matrix4f mat = stack.last().pose();
        buff.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buff.vertex(mat, x, y + height, 0).uv(0, 1).endVertex();
        buff.vertex(mat, x + width, y + height, 0).uv(1, 1).endVertex();
        buff.vertex(mat, x + width, y, 0).uv(1, 0).endVertex();
        buff.vertex(mat, x, y, 0).uv(0, 0).endVertex();
        tess.end();
    }
}
