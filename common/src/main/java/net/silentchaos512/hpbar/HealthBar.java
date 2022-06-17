package net.silentchaos512.hpbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.silentchaos512.hpbar.config.Config;
import net.silentchaos512.hpbar.gui.GuiHealthBar;

import java.io.File;
import java.util.Random;

public abstract class HealthBar {

  public static final String MOD_ID = "healthbar";
  public static final String RESOURCE_PREFIX = MOD_ID.toLowerCase();

  private float playerPrevCurrentHealth = 20f;
  private float playerPrevMaxHealth = 20f;
  private float playerLastDamageTaken = 0f;

  protected GuiHealthBar guiHealthBar;

  public static HealthBar instance;

  public static Random random = new Random();

  public HealthBar()
  {
    instance = this;
    guiHealthBar = new GuiHealthBar(Minecraft.getInstance());
  }

  public void onPlayerTick(LocalPlayer player) {

    float current = player.getHealth();
    float max = player.getMaxHealth();

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
