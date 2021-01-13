package de.digitaldevs.api.events;

import de.digitaldevs.api.npc.NPC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
public class NPCEquipEvent extends Event implements Cancellable {

  private static final HandlerList handlerList = new HandlerList();
  private boolean canceled = false;
  @Getter private final NPC NPC;
  @Getter private final ItemStack itemStack;

  @Override
  public boolean isCancelled() {
    return canceled;
  }

  @Override
  public void setCancelled(boolean b) {
    canceled = b;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }
}
