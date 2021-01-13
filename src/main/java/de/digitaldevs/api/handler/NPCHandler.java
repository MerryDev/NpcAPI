package de.digitaldevs.api.handler;

import de.digitaldevs.api.npc.NPC;
import java.util.ArrayList;
import lombok.Getter;

public class NPCHandler {

  @Getter public ArrayList<NPC> npcs = new ArrayList<>();

  public void registerNPC(NPC npc) {
    npcs.add(npc);
  }

  public void unregisterNPC(NPC npc) {
    npcs.remove(npc);
  }

}
