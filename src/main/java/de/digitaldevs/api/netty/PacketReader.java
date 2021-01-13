package de.digitaldevs.api.netty;

import static org.bukkit.Bukkit.getScheduler;

import de.digitaldevs.api.NPCAPI;
import de.digitaldevs.api.handler.EventHandler;
import de.digitaldevs.api.handler.EventHandler.PlayerEventType;
import de.digitaldevs.api.npc.NPC;
import de.digitaldevs.api.reflection.Reflection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PacketReader extends Reflection {

  private static final EventHandler EVENT_HANDLER = NPCAPI.getEventHandler();
  private final Player player;
  private Channel channel;
  private Long lastInteract;
  private Long lastAttack;

  public void inject() {
    CraftPlayer craftPlayer = (CraftPlayer) player;
    channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
    channel.pipeline()
        .addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
          @Override
          protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet,
              List<Object> list) throws Exception {
            readPacket(packet);
            list.add(packet);
          }
        });
  }

  public void eject() {
    if (channel.pipeline().get("decoder") != null) {
      channel.pipeline().remove("PacketInjector");
    }
  }

  private void readPacket(Packet<?> packet) {
    getScheduler().runTaskAsynchronously(NPCAPI.getInstance(), () -> {
      if (packet.getClass().getSimpleName().equals("PacketPlayInUseEntity")) {
        int id = (int) getValue(packet, "a");
        Iterator<NPC> iterator = NPCAPI.getNpcHandler().getNpcs().iterator();
        while (true) {
          if (!iterator.hasNext()) {
            return;
          }
          NPC npc = iterator.next();
          if (getValue(packet, "action").toString().equalsIgnoreCase("ATTACK")) {
            boolean canCallEvent;
            if (lastAttack == null) {
              canCallEvent = true;
              lastAttack = System.currentTimeMillis();
            } else if (lastAttack + 500L <= System.currentTimeMillis()) {
              lastAttack = System.currentTimeMillis();
              canCallEvent = true;
            } else {
              canCallEvent = false;
            }
            if (canCallEvent) {
              EVENT_HANDLER.invoke(npc, player).callEvent(PlayerEventType.ATTACK);
            }
            continue;
          }
          if (getValue(packet, "action").toString().equalsIgnoreCase("INTERACT_AT")) {
            boolean canCallEvent;
            if (lastInteract == null) {
              canCallEvent = true;
              lastInteract = System.currentTimeMillis();
            } else if (lastInteract + 300L <= System.currentTimeMillis()) {
              lastInteract = System.currentTimeMillis();
              canCallEvent = true;
            } else {
              canCallEvent = false;
            }
            if (canCallEvent) {
              EVENT_HANDLER.invoke(npc, player).callEvent(PlayerEventType.INTERACT);
            }
          }
        }
      }
    });
  }

}
