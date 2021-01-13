package de.digitaldevs.api.listener;

import de.digitaldevs.api.NPCAPI;
import de.digitaldevs.api.events.NPCSpawnEvent;
import de.digitaldevs.api.npc.NPC;
import de.digitaldevs.api.netty.PacketReader;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ConnectionListener implements Listener {

  @Getter private static PacketReader packetReader;
  private NPC npc;

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    packetReader = new PacketReader(player);
    packetReader.inject();

    npc = new NPC("Test", player.getLocation(), player);
    npc.setVisibleOnTab(false);
    npc.setLocation(player.getLocation().add(3, 0, 0));
    npc.setSkin("MerryChrismas");
    npc.spawn();
  }

  @EventHandler
  public void onSpawn(NPCSpawnEvent event) {
    NPC npc = event.getNPC();
    System.out.println(npc.toString());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    packetReader.eject();
  }

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {
    npc.sneak(event.isSneaking());
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    npc.focusPlayer();
  }

}
