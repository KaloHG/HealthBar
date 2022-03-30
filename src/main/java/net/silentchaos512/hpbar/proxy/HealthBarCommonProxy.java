package net.silentchaos512.hpbar.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class HealthBarCommonProxy {

  public Player getClientPlayer() {

    return DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().player);
  }
}
