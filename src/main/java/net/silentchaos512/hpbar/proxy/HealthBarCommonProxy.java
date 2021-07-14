package net.silentchaos512.hpbar.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class HealthBarCommonProxy {

  public PlayerEntity getClientPlayer() {

    return DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
  }
}
