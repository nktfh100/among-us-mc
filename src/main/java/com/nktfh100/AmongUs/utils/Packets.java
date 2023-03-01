package com.nktfh100.AmongUs.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.nktfh100.AmongUs.info.ColorInfo;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityPose;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;

public class Packets {

	public static byte toPackedByte(float f) {
		return (byte) ((int) (f * 256.0F / 360.0F));
	}

	public static void sendPacket(Player p, PacketContainer packet) {
		try {
			if (p.isOnline() && packet != null) {
				ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static PacketContainer UPDATE_DISPLAY_NAME(UUID uuid, String orgName, String newName) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
		packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME);
		WrappedGameProfile wgp = new WrappedGameProfile(uuid, orgName);
		packet.getPlayerInfoDataLists().write(0, new ArrayList<PlayerInfoData>(Arrays.asList(new PlayerInfoData(wgp, 50, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(newName)))));
		return packet;
	}

	public static PacketContainer ADD_PLAYER(UUID uuid, String name, String displayName, String textureValue, String textureSignature) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
		packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
		WrappedGameProfile wgp = new WrappedGameProfile(uuid, name);
		PlayerInfoData playerInfoData = new PlayerInfoData(wgp, 50, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(displayName));
		packet.getPlayerInfoDataLists().write(0, new ArrayList<PlayerInfoData>(Arrays.asList(playerInfoData)));

		wgp.getProperties().get("textures").clear();
		wgp.getProperties().get("textures").add(new WrappedSignedProperty("textures", textureValue, textureSignature));
		return packet;
	}

	public static PacketContainer REMOVE_PLAYER(UUID uuid, String name, String displayName) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
		packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
		packet.getPlayerInfoDataLists().write(0,
				new ArrayList<PlayerInfoData>(Arrays.asList(new PlayerInfoData(new WrappedGameProfile(uuid, name), 50, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(displayName)))));
		return packet;
	}

	public static PacketContainer NAMED_SOUND(Location loc, Sound sound) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
		packet.getSoundEffects().write(0, sound);
		packet.getSoundCategories().write(0, EnumWrappers.SoundCategory.BLOCKS);
		packet.getIntegers().write(0, (int) (loc.getX() * 8.0));
		packet.getIntegers().write(1, (int) (loc.getY() * 8.0));
		packet.getIntegers().write(2, (int) (loc.getZ() * 8.0));
		packet.getFloat().write(0, 50F); // volume
		packet.getFloat().write(1, 1F); // pitch

		return packet;
	}

	public static PacketContainer NAMED_SOUND(Location loc, Sound sound, Float volume, Float pitch) {
		PacketContainer packet = NAMED_SOUND(loc, sound);
		packet.getFloat().write(0, volume);
		packet.getFloat().write(1, pitch);
		return packet;
	}

	public static PacketContainer BLOCK_CHANGE(Location loc, WrappedBlockData wrappedData) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
		packet.getBlockPositionModifier().write(0, new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		if (wrappedData != null) {
			packet.getBlockData().write(0, wrappedData);
		}
		return packet;
	}

	public static PacketContainer SPAWN_PLAYER(Location loc, int entityId, UUID uuid) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
		packet.getIntegers().write(0, entityId);
		packet.getUUIDs().write(0, uuid);
		packet.getDoubles().write(0, loc.getX()).write(1, loc.getY()).write(2, loc.getZ());
		packet.getBytes().write(0, toPackedByte(loc.getYaw())).write(1, toPackedByte(loc.getPitch()));
		return packet;
	}

	public static PacketContainer ENTITY_HEAD_ROTATION(int entityId, Location loc) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
		packet.getIntegers().write(0, entityId);
		packet.getBytes().write(0, toPackedByte(loc.getYaw()));
//		packet.getBytes().write(0, (byte) loc.getYaw());
		return packet;
	}

	public static PacketContainer ENTITY_LOOK(int entityid, Location loc) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
		packet.getIntegers().write(0, entityid);
		packet.getBytes().write(0, toPackedByte(loc.getYaw())).write(1, toPackedByte((loc.getPitch())));
		packet.getBooleans().write(0, true);
		return packet;
	}

	public static PacketContainer PLAYER_ARMOR(ColorInfo color, int entityId) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
		packet.getIntegers().write(0, entityId);
		List<Pair<ItemSlot, ItemStack>> newSlotStack = new ArrayList<Pair<ItemSlot, ItemStack>>();
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.HEAD, new ItemStack(color.getGlass())));
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.CHEST, Utils.getArmorColor(color, Material.LEATHER_CHESTPLATE)));
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.LEGS, Utils.getArmorColor(color, Material.LEATHER_LEGGINGS)));
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.FEET, Utils.getArmorColor(color, Material.LEATHER_BOOTS)));
		packet.getSlotStackPairLists().write(0, newSlotStack);
		return packet;
	}

	public static PacketContainer ENTITY_EQUIPMENT_HEAD(int entityId, Material mat) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
		packet.getIntegers().write(0, entityId);
		List<Pair<ItemSlot, ItemStack>> newSlotStack = new ArrayList<Pair<ItemSlot, ItemStack>>();
		newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.HEAD, new ItemStack(mat)));
		packet.getSlotStackPairLists().write(0, newSlotStack);
		return packet;
	}

	public static PacketContainer DESTROY_ENTITY(int entityId) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getIntegerArrays().write(0, new int[] { entityId });
		return packet;
	}

	public static PacketContainer PLAYER_SLEEPING(int entityId) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entityId);
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		Serializer serializer = Registry.get(EnumWrappers.getEntityPoseClass());
		WrappedDataWatcherObject object = new WrappedDataWatcherObject(6, serializer);
		watcher.setObject(object, EntityPose.SLEEPING.toNms());
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		return packet;
	}

	public static PacketContainer PARTICLES(Location loc, Particle particle, Object data, Integer count, float offSetX, float offSetY, float offSetZ) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
		packet.getNewParticles().write(0, WrappedParticle.create(particle, data));
		packet.getIntegers().write(0, count); // count
		packet.getFloat().write(0, offSetX).write(1, offSetY).write(2, offSetZ); // offset
		packet.getBooleans().write(0, false); // long distance
		packet.getDoubles().write(0, loc.getX()).write(1, loc.getY()).write(2, loc.getZ());
		return packet;
	}

	public static PacketContainer ARMOR_STAND(Location loc, Integer entityId, UUID uuid) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

		packet.getIntegers().write(0, entityId); // entity id
		packet.getUUIDs().write(0, uuid); // uuid

		packet.getDoubles().write(0, loc.getX());
		packet.getDoubles().write(1, loc.getY()); // location
		packet.getDoubles().write(2, loc.getZ());
		packet.getIntegers().write(4, 0); // yaw & pitch ?
		packet.getIntegers().write(5, 0);

		packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);

		return packet;
	}

	public static PacketContainer ENTITY_TELEPORT(int entityId, Location loc) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
		packet.getIntegers().write(0, entityId); // entity id
		packet.getDoubles().write(0, loc.getX());
		packet.getDoubles().write(1, loc.getY());
		packet.getDoubles().write(2, loc.getZ());
		return packet;
	}

	public static PacketContainer METADATA_SKIN(int entityId, Player player) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entityId);
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		Serializer serializer = Registry.get(Byte.class);
		if (player != null) {
			watcher.setEntity(player);
		}
		watcher.setObject(16, serializer, (byte) (0x01)); // cape
		watcher.setObject(16, serializer, (byte) (0x02)); // jacket
		watcher.setObject(16, serializer, (byte) (0x04)); // left sleeve
		watcher.setObject(16, serializer, (byte) (0x08)); // right sleeve
		watcher.setObject(16, serializer, (byte) (0x10)); // left pants
		watcher.setObject(16, serializer, (byte) (0x20)); // right pants
		watcher.setObject(16, serializer, (byte) (0x40)); // hat
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		return packet;
	}
	
//	public static PacketContainer METADATA_INVIS(int entityId, Player player) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, entityId);
//		WrappedDataWatcher watcher = new WrappedDataWatcher();
//		Serializer serializer = Registry.get(Byte.class);
//		if (player != null) {
//			watcher.setEntity(player);
//		}
//		watcher.setObject(0, serializer, (byte) (0x20));
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
//		return packet;
//	}

//	public static PacketContainer ENTITY_INVIS(int entityId) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, entityId);
//		WrappedDataWatcher watcher = new WrappedDataWatcher();
//		Serializer serializer = Registry.get(Byte.class);
//		watcher.setObject(0, serializer, (byte) (0x20));
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
//		return packet;
//	}

//	public static PacketContainer ARMOR_STAND_ROTATION(int entityId, Vector3F vector) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, entityId);
//
//		WrappedDataWatcher watcher = new WrappedDataWatcher();
//		watcher.setObject(15, Registry.getVectorSerializer(), vector);
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
//		return packet;
//	}

//	public static PacketContainer GLOW(Player player) {
//		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//		packet.getIntegers().write(0, player.getEntityId()); // Set packet's entity id
//		WrappedDataWatcher watcher = new WrappedDataWatcher(); // Create data watcher, the Entity Metadata packet requires this
//		Serializer serializer = Registry.get(Byte.class); // Found this through google, needed for some stupid reason
//		watcher.setEntity(player); // Set the new data watcher's target
//		watcher.setObject(0, serializer, (byte) (0x40)); // Set status to glowing, found on protocol page
//		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); // Make the packet's datawatcher the one we created
//		return packet;
//	}
}
