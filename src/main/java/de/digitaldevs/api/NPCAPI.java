package de.digitaldevs.api;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getOnlinePlayers;
import static org.bukkit.Bukkit.getPluginManager;

import de.digitaldevs.api.handler.EventHandler;
import de.digitaldevs.api.handler.NPCHandler;
import de.digitaldevs.api.listener.ConnectionListener;
import de.digitaldevs.api.npc.NPC;
import de.digitaldevs.api.netty.PacketReader;
import de.digitaldevs.api.utils.ConsoleHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCAPI extends JavaPlugin {

  @Getter @Setter static NPCAPI instance;
  @Getter static NPCHandler npcHandler;
  @Getter static EventHandler eventHandler;

  @Override
  public void onEnable() {
    setInstance(this);
    registerListener(getPluginManager());
    registerHandler();
    initConsole();
  }

  @Override
  public void onDisable() {
    setInstance(null);
    try {
      getOnlinePlayers().forEach(player -> {
        PacketReader packetReader = ConnectionListener.getPacketReader();
        if(packetReader != null) {
          packetReader.eject();
        }
      });
     for(NPC npc : npcHandler.getNpcs()) {
       npc.despawn();
     }
    }catch (Exception exception) {
    }
  }

  private void initConsole() {
    ConsoleHandler consoleHandler = new ConsoleHandler(getConsoleSender());
    consoleHandler.sendPluginName();
  }

  private void registerListener(PluginManager pluginManager) {
    pluginManager.registerEvents(new ConnectionListener(), getInstance());
  }

  private void registerHandler() {
    npcHandler = new NPCHandler();
    eventHandler = new EventHandler();
  }

}
