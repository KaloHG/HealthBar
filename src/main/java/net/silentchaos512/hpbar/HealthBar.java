package net.silentchaos512.hpbar;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.silentchaos512.hpbar.config.Config;
import net.silentchaos512.hpbar.gui.GuiConfigHealthBar;
import net.silentchaos512.hpbar.gui.GuiHealthBar;

import java.util.Random;

@Mod(HealthBar.MOD_ID)
public class HealthBar {

  public static final String MOD_ID = "healthbar";
  public static final String RESOURCE_PREFIX = MOD_ID.toLowerCase(); //?

  private float playerCurrentHealth = 20f;
  private float playerMaxHealth = 20f;
  private float playerPrevCurrentHealth = 20f;
  private float playerPrevMaxHealth = 20f;
  private float playerLastDamageTaken = 0f;

  public static HealthBar instance;

  public static Random random = new Random();

  public HealthBar()
  {
    instance = this;

    Config.init();
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.getConfiguration());
    ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new GuiConfigHealthBar(screen)));

    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::preInit);
  }

  public void preInit(FMLClientSetupEvent event) {
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(new GuiHealthBar(Minecraft.getInstance()));
  }

  @SubscribeEvent
  public void onPlayerTick(TickEvent.PlayerTickEvent event) {

    if (event.player.isLocalPlayer()) {
      float current = event.player.getHealth();
      float max = event.player.getMaxHealth();

      boolean healthChanged = current != playerPrevCurrentHealth || max != playerPrevMaxHealth;
      if (healthChanged) {
        // Calculate health change, save the number if damage was taken.
        float diff = current - playerPrevCurrentHealth;
        if (diff < 0)
          playerLastDamageTaken = -diff;

        playerPrevCurrentHealth = current;
        playerPrevMaxHealth = max;
      }
    }
  }

  public float getPlayerHealth() {

    return Minecraft.getInstance().player.getHealth();
  }

  public float getPlayerMaxHealth() {

    return Minecraft.getInstance().player.getMaxHealth();
  }

  public float getPlayerLastDamageTaken() {

    return playerLastDamageTaken;
  }
}
