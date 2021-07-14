package net.silentchaos512.hpbar;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.silentchaos512.hpbar.config.Config;
import net.silentchaos512.hpbar.gui.GuiConfigHealthBar;
import net.silentchaos512.hpbar.gui.GuiHealthBar;
import net.silentchaos512.hpbar.network.MessageHealthUpdate;
import net.silentchaos512.hpbar.proxy.HealthBarCommonProxy;

import java.util.Optional;
import java.util.Random;

@Mod(HealthBar.MOD_ID)
public class HealthBar {

  public static final String MOD_ID = "healthbar";
  public static final String PROTOCOL_VERSION = "1";
  public static final String RESOURCE_PREFIX = MOD_ID.toLowerCase(); //?

  public static final float CLIENT_MODE_DELAY = 5000;

  private float playerCurrentHealth = 20f;
  private float playerMaxHealth = 20f;
  private float playerPrevCurrentHealth = 20f;
  private float playerPrevMaxHealth = 20f;
  private float playerLastDamageTaken = 0f;
  private long lastUpdatePacketTime = 0L;

  public static HealthBar instance;

  public static HealthBarCommonProxy proxy = new HealthBarCommonProxy();

  public static SimpleChannel network;

  public static Random random = new Random();

  public HealthBar()
  {
    instance = this;

    Config.init();
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.getConfiguration());
    ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) -> new GuiConfigHealthBar(screen));

    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::preInit);
    bus.addListener(this::postInit);
  }

  public void preInit(FMLCommonSetupEvent event) {
    network = NetworkRegistry.newSimpleChannel(new ResourceLocation(RESOURCE_PREFIX, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    network.registerMessage(0, MessageHealthUpdate.class, MessageHealthUpdate::toBytes,
        MessageHealthUpdate::fromBytes, new MessageHealthUpdate.Handler(), Optional.of(NetworkDirection.PLAY_TO_CLIENT));

    MinecraftForge.EVENT_BUS.register(this);
  }

  public void postInit(FMLLoadCompleteEvent event) {

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new GuiHealthBar(Minecraft.getInstance())));
  }

  @SubscribeEvent
  public void onEntityConstructing(EntityConstructing event) {

    if (event.getEntity() instanceof ServerPlayerEntity) {
      ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
      if (player.getAttribute(Attributes.MAX_HEALTH) != null) {
        float current = player.getHealth();
        float max = player.getMaxHealth();
        network.send(PacketDistributor.PLAYER.with(() -> player), new MessageHealthUpdate(current, max));
      }
    }
  }

  @SubscribeEvent
  public void onPlayerTick(TickEvent.PlayerTickEvent event) {

    // Send a health update packet to the player if necessary.
    if (event.player instanceof ServerPlayerEntity) {
      float current = event.player.getHealth();
      float max = event.player.getMaxHealth();

      boolean healthChanged = current != playerPrevCurrentHealth || max != playerPrevMaxHealth;
      boolean checkInTime = Config.checkinFrequency.get() <= 0 ? false
          : event.player.world.getGameTime() % Config.checkinFrequency.get() == 0;
      if (healthChanged || checkInTime) {
        // Calculate health change, save the number if damage was taken.
        float diff = current - playerPrevCurrentHealth;
        if (diff < 0)
          playerLastDamageTaken = -diff;

        network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player), new MessageHealthUpdate(current, max));
      }
    }
  }

  public float getPlayerHealth() {

    if (System.currentTimeMillis() - lastUpdatePacketTime > CLIENT_MODE_DELAY) {
      PlayerEntity clientPlayer = proxy.getClientPlayer();
      if (clientPlayer != null)
        return clientPlayer.getHealth();
    }

    return playerCurrentHealth;
  }

  public float getPlayerMaxHealth() {

    if (System.currentTimeMillis() - lastUpdatePacketTime > CLIENT_MODE_DELAY) {
      PlayerEntity clientPlayer = proxy.getClientPlayer();
      if (clientPlayer != null)
        return clientPlayer.getMaxHealth();
    }
    return playerMaxHealth;
  }

  public float getPlayerLastDamageTaken() {

    return playerLastDamageTaken;
  }

  public void handleUpdatePacket(float health, float maxHealth) {

    playerPrevCurrentHealth = playerCurrentHealth;
    playerPrevMaxHealth = playerMaxHealth;
    playerCurrentHealth = health;
    playerMaxHealth = maxHealth;
    lastUpdatePacketTime = System.currentTimeMillis();
  }
}
