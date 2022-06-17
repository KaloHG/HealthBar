package net.silentchaos512.hpbar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.silentchaos512.hpbar.config.Config;

public class HealthBarFabric extends HealthBar implements ClientModInitializer {

    public HealthBarFabric()
    {
        Config.init(FabricLoader.getInstance().getConfigDir().resolve("healthbar.json").toFile());
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
            if (minecraft.player != null) {
                onPlayerTick(minecraft.player);
            }
        });
        HudRenderCallback.EVENT.register(this::onRenderGameOverlay);
    }

    public void onRenderGameOverlay(PoseStack poseStack, float partialTicks) {
        guiHealthBar.onRenderGameOverlay(Minecraft.getInstance().getWindow(), poseStack, 59);
    }
}
