package net.silentchaos512.hpbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.silentchaos512.hpbar.config.Config;
import net.silentchaos512.hpbar.gui.GuiConfigHealthBar;

@Mod(HealthBar.MOD_ID)
public class HealthBarForge extends HealthBar {

    public HealthBarForge()
    {
        Config.init(FMLPaths.CONFIGDIR.get().resolve("healthbar.json").toFile());
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new GuiConfigHealthBar(screen)));

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::preInit);
    }

    public void preInit(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.player.isLocalPlayer())
            onPlayerTick((LocalPlayer) event.player);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.PreLayer event) {

        // Hide vanilla health?
        if (Config.replaceVanillaHealth.get() && event.isCancelable() && event.getOverlay() == ForgeIngameGui.PLAYER_HEALTH_ELEMENT) {
            event.setCanceled(true);
            ((ForgeIngameGui) Minecraft.getInstance().gui).left_height += 10;
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {

        // Only render on TEXT (seems to cause problems in other cases).
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }

        guiHealthBar.onRenderGameOverlay(event.getWindow(), event.getMatrixStack(), ((ForgeIngameGui) Minecraft.getInstance().gui).left_height);
    }
}
