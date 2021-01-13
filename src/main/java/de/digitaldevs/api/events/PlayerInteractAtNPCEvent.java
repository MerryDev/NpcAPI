package de.digitaldevs.api.events;

import de.digitaldevs.api.npc.NPC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
public class PlayerInteractAtNPCEvent extends Event implements Cancellable {

  private static final HandlerList handlerList= new HandlerList();
  private boolean canceled = false;
  @Getter private final Player player;
  @Getter private final NPC NPC;

  @Override
  public boolean isCancelled() {
    return canceled;
  }

  @Override
  public void setCancelled(boolean b) {
    this.canceled = b;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }
}
