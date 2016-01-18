package net.silentchaos512.hpbar;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = HealthBar.MOD_ID, name = HealthBar.MOD_NAME, version = HealthBar.VERSION_NUMBER, guiFactory = "net.silentchaos512.hpbar.GuiFactoryHealthBar")
public class HealthBar {

  public static final String MOD_ID = "HealthBar";
  public static final String MOD_NAME = "Health Bar";
  public static final String VERSION_NUMBER = "@VERSION@";
  public static final String CHANNEL_NAME = MOD_ID;
  public static final String RESOURCE_PREFIX = MOD_ID.toLowerCase();

  private float playerCurrentHealth = 20f;
  private float playerMaxHealth = 20f;
  private float playerPrevCurrentHealth = 20f;
  private float playerPrevMaxHealth = 20f;

  @Instance(MOD_ID)
  public static HealthBar instance;

  public static SimpleNetworkWrapper network;

  public static Random random = new Random();

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {

    Config.init(event.getSuggestedConfigurationFile());
    Config.save();

    network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
    int discriminator = -1;
    network.registerMessage(MessageHealthUpdate.Handler.class, MessageHealthUpdate.class,
        discriminator, Side.CLIENT);

    FMLCommonHandler.instance().bus().register(this);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {

    MinecraftForge.EVENT_BUS.register(new GuiHealthBar(Minecraft.getMinecraft()));
  }

  @SubscribeEvent
  public void onEntityConstructing(EntityConstructing event) {

    if (event.entity instanceof EntityPlayerMP) {
      EntityPlayerMP player = (EntityPlayerMP) event.entity;
      if (player.getEntityAttribute(SharedMonsterAttributes.maxHealth) != null) {
        float current = player.getHealth();
        float max = player.getMaxHealth();
        network.sendTo(new MessageHealthUpdate(current, max), player);
      }
    }
  }

  @SubscribeEvent
  public void onPlayerTick(TickEvent.PlayerTickEvent event) {

    // Send a health update packet to the player if necessary.
    if (event.player instanceof EntityPlayerMP) {
      float current = event.player.getHealth();
      float max = event.player.getMaxHealth();

      boolean healthChanged = current != playerPrevCurrentHealth || max != playerPrevMaxHealth;
      boolean checkInTime = Config.checkinFrequency <= 0 ? false
          : event.player.worldObj.getTotalWorldTime() % Config.checkinFrequency == 0;
      if (healthChanged || checkInTime) {
        // System.out.println("Sending player health update...");
        network.sendTo(new MessageHealthUpdate(current, max), (EntityPlayerMP) event.player);
      }
    }
  }

  public float getPlayerHealth() {

    return playerCurrentHealth;
  }

  public void setPlayerHealth(float value) {

    playerPrevCurrentHealth = playerCurrentHealth;
    playerCurrentHealth = value;
  }

  public float getPlayerMaxHealth() {

    return playerMaxHealth;
  }

  public void setPlayerMaxHealth(float value) {

    playerPrevMaxHealth = playerMaxHealth;
    playerMaxHealth = value;
  }
}
