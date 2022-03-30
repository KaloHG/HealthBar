package net.silentchaos512.hpbar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.silentchaos512.hpbar.HealthBar;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MessageHealthUpdate {

  private float currentHealth;
  private float maxHealth;

  public MessageHealthUpdate() {

  }

  public MessageHealthUpdate(float current, float max) {

    this.currentHealth = current;
    this.maxHealth = max;
  }

  public static MessageHealthUpdate fromBytes(FriendlyByteBuf buf) {

    MessageHealthUpdate msg = new MessageHealthUpdate();
    msg.currentHealth = buf.readFloat();
    msg.maxHealth = buf.readFloat();

    return msg;
  }

  public static void toBytes(MessageHealthUpdate msg, FriendlyByteBuf buf) {

    buf.writeFloat(msg.currentHealth);
    buf.writeFloat(msg.maxHealth);
  }

  public static class Handler implements BiConsumer<MessageHealthUpdate, Supplier<NetworkEvent.Context>> {

    @Override
    public void accept(MessageHealthUpdate message, Supplier<NetworkEvent.Context> ctx) {

      if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
        HealthBar.instance.handleUpdatePacket(message.currentHealth, message.maxHealth);
      ctx.get().setPacketHandled(true);
    }
  }
}
