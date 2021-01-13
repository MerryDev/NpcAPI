package de.digitaldevs.api.handler;

import static org.bukkit.Bukkit.getPluginManager;

import de.digitaldevs.api.events.NPCDespawnEvent;
import de.digitaldevs.api.events.NPCEquipEvent;
import de.digitaldevs.api.events.NPCFocusPlayerEvent;
import de.digitaldevs.api.events.NPCPlayAnimationEvent;
import de.digitaldevs.api.events.NPCPlayStatusEvent;
import de.digitaldevs.api.events.NPCRotateHeadEvent;
import de.digitaldevs.api.events.NPCSleepEvent;
import de.digitaldevs.api.events.NPCSneakEvent;
import de.digitaldevs.api.events.NPCSpawnEvent;
import de.digitaldevs.api.events.NPCTeleportEvent;
import de.digitaldevs.api.events.NPCUpdateSkinEvent;
import de.digitaldevs.api.events.PlayerAttackNPCEvent;
import de.digitaldevs.api.events.PlayerInteractAtNPCEvent;
import de.digitaldevs.api.npc.NPC;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EventHandler {

  @Getter private NPC npc;
  @Getter private Player player;
  @Getter private ItemStack itemStack;
  @Getter private Location location;
  @Getter private int integer;
  @Getter private boolean bool;
  @Getter private float[] floats;

  public void callEvent(NPCEventType eventType) {
    switch (eventType) {
      case SPAWN:
        getPluginManager().callEvent(new NPCSpawnEvent(npc));
        break;
      case DESPAWN:
        getPluginManager().callEvent(new NPCDespawnEvent(npc));
        break;
      case UPDATE_SKIN:
        getPluginManager().callEvent(new NPCUpdateSkinEvent(npc));
        break;
      case PLAY_ANIMATION:
        getPluginManager().callEvent(new NPCPlayAnimationEvent(npc, integer));
        break;
      case PLAY_STATUS:
        getPluginManager().callEvent(new NPCPlayStatusEvent(npc, integer));
        break;
      case EQUIP:
        getPluginManager().callEvent(new NPCEquipEvent(npc, itemStack));
        break;
      case SLEEP:
        getPluginManager().callEvent(new NPCSleepEvent(npc, bool));
        break;
      case TELEPORT:
        getPluginManager().callEvent(new NPCTeleportEvent(npc, location));
        break;
      case FOCUS_PLAYER:
        getPluginManager().callEvent(new NPCFocusPlayerEvent(npc, player));
        break;
      case SNEAK:
        getPluginManager().callEvent(new NPCSneakEvent(npc, bool));
        break;
      case ROTATE_HEAD:
        getPluginManager().callEvent(new NPCRotateHeadEvent(npc, floats[0], floats[1]));
        break;
    }
  }

  public void callEvent(PlayerEventType eventType) {
    switch (eventType) {
      case ATTACK:
        getPluginManager().callEvent(new PlayerAttackNPCEvent(player, npc));
        break;
      case INTERACT:
        getPluginManager().callEvent(new PlayerInteractAtNPCEvent(player, npc));
        break;
    }
  }

  public EventHandler invoke(Player player) {
    this.player = player;
    return this;
  }

  public EventHandler invoke(NPC npc) {
    this.npc = npc;
    return this;
  }

  public EventHandler invoke(ItemStack itemStack) {
    this.itemStack = itemStack;
    return this;
  }

  public EventHandler invoke(Location location) {
    this.location = location;
    return this;
  }

  public EventHandler invoke(int integer) {
    this.integer = integer;
    return this;
  }

  public EventHandler invoke(boolean bool) {
    this.bool = bool;
    return this;
  }

  public EventHandler invoke(float[] floats) {
    this.floats = floats;
    return this;
  }

  public EventHandler invoke(NPC npc, int integer) {
    invoke(npc);
    invoke(integer);
    return this;
  }

  public EventHandler invoke(NPC npc, boolean bool) {
    invoke(npc);
    invoke(bool);
    return this;
  }

  public EventHandler invoke(NPC npc, float[] floats) {
    invoke(npc);
    invoke(floats);
    return this;
  }

  public EventHandler invoke(NPC npc, Player player) {
    invoke(npc);
    invoke(player);
    return this;
  }

  public EventHandler invoke(NPC npc, ItemStack itemStack) {
    invoke(npc);
    invoke(itemStack);
    return this;
  }

  public EventHandler invoke(NPC npc, Location location) {
    invoke(npc);
    invoke(location);
    return this;
  }


  public enum NPCEventType {
    SPAWN, DESPAWN, UPDATE_SKIN, PLAY_ANIMATION, PLAY_STATUS, EQUIP, SLEEP, TELEPORT, ROTATE_HEAD, FOCUS_PLAYER, SNEAK
  }

  public enum PlayerEventType {
    INTERACT, ATTACK
  }

}
