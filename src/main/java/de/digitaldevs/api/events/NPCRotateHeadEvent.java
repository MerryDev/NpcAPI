package de.digitaldevs.api.events;

import de.digitaldevs.api.npc.NPC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
public class NPCRotateHeadEvent extends Event implements Cancellable {

  private static final HandlerList handlerList = new HandlerList();
  private boolean canceled = false;
  @Getter private final NPC NPC;
  @Getter private final float pitch;
  @Getter private final float yaw;

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
