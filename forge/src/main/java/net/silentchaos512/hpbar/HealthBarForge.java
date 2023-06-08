package net.silentchaos512.hpbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new GuiConfigHealthBar(screen)));

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
    public void onRenderGameOverlay(RenderGuiOverlayEvent.Pre event) {

        // Hide vanilla health?
        if (Config.replaceVanillaHealth.get() && event.isCancelable() && event.getOverlay() == GuiOverlayManager.findOverlay(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            event.setCanceled(true);
            ((ForgeGui) Minecraft.getInstance().gui).leftHeight += 10;
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGuiOverlayEvent event) {

        // Only render on TEXT (seems to cause problems in other cases).
        //TODO i have no idea if this works, i figure this is probably just what it's looking for.
        if (event.isCancelable() || event.getOverlay() != GuiOverlayManager.findOverlay(VanillaGuiOverlay.DEBUG_TEXT.id())) {
            return;
        }

        guiHealthBar.onRenderGameOverlay(event.getWindow(), event.getPoseStack(), ((ForgeGui) Minecraft.getInstance().gui).leftHeight);
    }
}
