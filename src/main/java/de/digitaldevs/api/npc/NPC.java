package de.digitaldevs.api.npc;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getScheduler;
import static org.bukkit.Bukkit.getWorld;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.digitaldevs.api.NPCAPI;
import de.digitaldevs.api.events.NPCSpawnEvent;
import de.digitaldevs.api.handler.EventHandler;
import de.digitaldevs.api.handler.EventHandler.NPCEventType;
import de.digitaldevs.api.reflection.Reflection;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class NPC extends Reflection implements Serializable {

  private static final EventHandler EVENT_HANDLER = NPCAPI.getEventHandler();
  @Getter private final int entityID;
  @Getter private final GameProfile gameProfile;
  @Getter private final DataWatcher dataWatcher;
  @Getter private final Player player;
  @Getter @Setter private boolean visibleOnTab;
  @Getter @Setter private Location location;

  public NPC(String displayName, Location location, Player player) {
    this.player = player;
    entityID = (int) Math.ceil(Math.random() * 1000) + 2000;
    gameProfile = new GameProfile(UUID.randomUUID(), displayName);
    dataWatcher = new DataWatcher(null);
    dataWatcher.a(6, (float) 20);
    dataWatcher.a(10, (byte) 127);
    setLocation(location);
  }

  public void spawn() {
    PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
    setValue(packet, "a", entityID);
    setValue(packet, "b", gameProfile.getId());
    setValue(packet, "c", getFixedLocation(location.getX()));
    setValue(packet, "d", getFixedLocation(location.getY()));
    setValue(packet, "e", getFixedLocation(location.getZ()));
    setValue(packet, "f", getFixedRotation(location.getYaw()));
    setValue(packet, "g", getFixedRotation(location.getPitch()));
    setValue(packet, "h", 0);
    setValue(packet, "i", dataWatcher);

    addToTablist();

    if (!visibleOnTab) {
      getScheduler().runTaskLater(NPCAPI.getInstance(), this::removeFromTablist, 2L);
    }

    sendPacket(packet, player);
    EVENT_HANDLER.invoke(this).callEvent(NPCEventType.SPAWN);
  }

  public void despawn() {
    PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityID);
    removeFromTablist();
    sendPacket(packet, player);
    NPCAPI.getNpcHandler().unregisterNPC(this);
    EVENT_HANDLER.invoke(this).callEvent(NPCEventType.DESPAWN);
  }

  public void setSkin(String skinName) {
    Gson gson = new Gson();
    String url = "https://api.mojang.com/users/profiles/minecraft/" + skinName;
    String json = getStringFromURL(url);
    String uuid = (gson.fromJson(json, JsonObject.class)).get("id").getAsString();
    url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
    json = getStringFromURL(url);
    JsonObject mainObject = gson.fromJson(json, JsonObject.class);
    JsonObject jObject = mainObject.get("properties").getAsJsonArray().get(0).getAsJsonObject();
    String value = jObject.get("value").getAsString();
    String signature = jObject.get("signature").getAsString();
    gameProfile.getProperties().put("textures", new Property("textures", value, signature));
    EVENT_HANDLER.invoke(this).callEvent(NPCEventType.UPDATE_SKIN);
  }

  public void playAnimation(int animationID) {
    PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
    setValue(packet, "a", entityID);
    setValue(packet, "b", (byte) animationID);
    sendPacket(packet, player);
    EVENT_HANDLER.invoke(this, animationID).callEvent(NPCEventType.PLAY_ANIMATION);
  }

  public void playStatus(int statusID) {
    PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus();
    setValue(packet, "a", entityID);
    setValue(packet, "a", (byte) statusID);
    sendPacket(packet, player);
    EVENT_HANDLER.invoke(this, statusID).callEvent(NPCEventType.PLAY_STATUS);
  }

  public void equip(Slot slot, ItemStack itemStack) {
    PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();
    setValue(packet, "a", entityID);
    setValue(packet, "b", slot.getID());
    setValue(packet, "c", itemStack);
    sendPacket(packet, player);
    EVENT_HANDLER.invoke(this, itemStack).callEvent(NPCEventType.EQUIP);
  }

  public void sleep(boolean sleep) {
    if (sleep) {
      Location bedLocation = new Location(location.getWorld(), 1, 1, 1);
      PacketPlayOutBed packet = new PacketPlayOutBed();
      setValue(packet, "a", entityID);
      setValue(packet, "b", new BlockPosition(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ()));
      player.sendBlockChange(bedLocation, Material.BED_BLOCK, (byte) 0);
      sendPacket(packet, player);
      teleport(location.clone().add(0, 0.3D, 0));
    } else {
      playAnimation(2);
      teleport(location.clone().subtract(0, 0.3D, 0));
    }
    EVENT_HANDLER.invoke(this, sleep).callEvent(NPCEventType.SLEEP);
  }

  public void teleport(Location location) {
    PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
    setValue(packet, "a", entityID);
    setValue(packet, "b", getFixedLocation(location.getX()));
    setValue(packet, "c", getFixedLocation(location.getY()));
    setValue(packet, "d", getFixedLocation(location.getZ()));
    setValue(packet, "e", getFixedRotation(location.getYaw()));
    setValue(packet, "f", getFixedRotation(location.getPitch()));
    sendPacket(packet, player);
    rotateHead(location.getYaw(), location.getPitch());
    setLocation(location);
    EVENT_HANDLER.invoke(this, location).callEvent(NPCEventType.TELEPORT);
  }

  public void rotateHead(float yaw, float pitch) {
    PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(entityID, getFixedRotation(yaw), getFixedRotation(pitch), true);
    PacketPlayOutEntityHeadRotation packet2 = new PacketPlayOutEntityHeadRotation();
    setValue(packet2, "a", entityID);
    setValue(packet2, "b", getFixedRotation(yaw));
    sendPacket(packet, player);
    sendPacket(packet2, player);
   EVENT_HANDLER.invoke(this, new float[]{yaw, pitch}).callEvent(NPCEventType.ROTATE_HEAD);
  }

  public void focusPlayer() {
    location.setDirection(player.getLocation().subtract(location).toVector());
    float yaw = location.getYaw();
    float pitch = location.getPitch();
    PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(entityID, (byte) (int) (yaw % 360.0F * 256.0F / 360.0F), (byte) (int) (pitch % 360.0F * 256.0F / 360.0F), false);
    sendPacket(packet, player);
    rotateHead(yaw, pitch);
    EVENT_HANDLER.invoke(this, player).callEvent(NPCEventType.FOCUS_PLAYER);
  }

  public void sneak(boolean sneak) {
    DataWatcher dataWatcher = ((CraftPlayer)player).getHandle().getDataWatcher();
    dataWatcher.watch(0, sneak ? (byte) 0x02 : (byte) 0x00);
    PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(entityID, dataWatcher, false);
    sendPacket(packet, player);
    NPCAPI.getEventHandler().callEvent(NPCEventType.SNEAK);
  }

  public HashMap<String, Object> encode() {
    HashMap<String, Object> map = new HashMap<>();
    map.put("X", location.getX());
    map.put("Y", location.getY());
    map.put("Z", location.getZ());
    map.put("Pitch", location.getPitch());
    map.put("Yaw", location.getY());
    map.put("World", location.getWorld().getName());
    map.put("entityID", entityID);
    map.put("name", gameProfile.getName());
    map.put("UUID", gameProfile.getId());
    map.put("Receiver", player);
    map.put("visibleOnTab", visibleOnTab);
    String value = "";
    String signature = "";
    for(Property property : gameProfile.getProperties().get("textures")) {
      value = property.getValue();
      signature = property.getSignature();
    }
    map.put("value", value);
    map.put("signature", signature);
    return map;
  }

  public NPC decode(HashMap<String, Object> map) {
    String name = (String) map.get("name");
    UUID uuid = (UUID) map.get("UUID");
    double x = (double) map.get("X");
    double y = (double) map.get("Y");
    double z = (double) map.get("Z");
    float pitch = (float) map.get("Pitch");
    float yaw = (float) map.get("Yaw");
    World world = getWorld((String) map.get("World"));
    Location location = new Location(world, x, y, z, yaw, pitch);
    Player player = (Player) map.get("Receiver");
    boolean visibleOnTablist = (boolean) map.get("visibleOnTab");
    NPC npc = new NPC(name, location, player);
    npc.setVisibleOnTab(visibleOnTablist);
    String value = (String) map.get("value");
    String signature = (String) map.get("signature");
    npc.gameProfile.getProperties().put("textures", new Property("textures", value, signature));
    return npc;
   }

  public String toString() {
    return encode().toString();
  }

  private void addToTablist() {
    PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
    PlayerInfoData data = packet.new PlayerInfoData(gameProfile, 1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(gameProfile.getName())[0]);

    @SuppressWarnings("unchecked")
    List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
    assert players != null;
    players.add(data);

    setValue(packet, "a", EnumPlayerInfoAction.ADD_PLAYER);
    setValue(packet, "b", players);

    sendPacket(packet, player);
  }

  private void removeFromTablist() {
    PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
    PlayerInfoData data = packet.new PlayerInfoData(gameProfile, 1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(gameProfile.getName())[0]);

    @SuppressWarnings("unchecked")
    List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
    assert players != null;
    players.add(data);

    setValue(packet, "a", EnumPlayerInfoAction.REMOVE_PLAYER);
    setValue(packet, "b", players);

    sendPacket(packet, player);
  }

  private int getFixedLocation(double coordinate) {
    return MathHelper.floor(coordinate * 32.0D);
  }

  private byte getFixedRotation(float look) {
    return (byte)(int)(look * 256.0D / 360.0D);
  }

  private void sendPacket(Packet<?> packet, Player player) {
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
  }

  private String getStringFromURL(String url) {
    StringBuilder text = new StringBuilder();
    try {
      Scanner scanner = new Scanner(new URL(url).openStream());
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        while (line.startsWith(" ")) {
          line = line.substring(1);
        }
        text.append(line);
      }
      scanner.close();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    return text.toString();
  }

  public enum Slot {

    MAIN_HAND(0), HELMET(4), CHESTPLATE(3), LEGGINGS(2), BOOTS(1);

    @Getter private final int ID;

    Slot(int ID) {
      this.ID = ID;
    }
  }

}
