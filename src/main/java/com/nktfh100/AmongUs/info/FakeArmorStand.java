package com.nktfh100.AmongUs.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.nktfh100.AmongUs.utils.Packets;

public class FakeArmorStand {

	private PlayerInfo pInfo;
	private Location loc;
	private int entityId;
	private UUID uuid;
	private Vector3F headRotation = null;
	private Vector3F bodyRotation = null;

	private ArrayList<Player> shownTo = new ArrayList<Player>();

	public FakeArmorStand(PlayerInfo pInfo, Location loc, Vector3F headRotation, Vector3F bodyRotation) {
		this.pInfo = pInfo;
		this.loc = loc;
		this.entityId = (int) (Math.random() * Integer.MAX_VALUE);
		this.uuid = UUID.randomUUID();
		this.headRotation = headRotation;
		this.bodyRotation = bodyRotation;
	}

	public void updateLocation(Location newLoc) {
		this.loc = newLoc;
		for (Player player : this.shownTo) {
			Packets.sendPacket(player, Packets.ENTITY_TELEPORT(this.entityId, newLoc));
		}
	}

	public void updateRotation(Vector3F headRotation, Vector3F bodyRotation) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, this.entityId);

		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(0, Registry.get(Byte.class), (byte) (0x20)); // invis
		if (headRotation != null) {
			this.headRotation = headRotation;
			watcher.setObject(16, Registry.getVectorSerializer(), this.headRotation);
		}
		if (bodyRotation != null) {
			watcher.setObject(17, Registry.getVectorSerializer(), this.bodyRotation);
			this.bodyRotation = bodyRotation;
		}
		
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		for (Player player : this.shownTo) {
			Packets.sendPacket(player, packet);
		}
	}

	public void showTo(Player player, Boolean register) {
		Packets.sendPacket(player, Packets.ARMOR_STAND(this.loc, this.entityId, this.uuid));

		PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, this.entityId);

		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(0, Registry.get(Byte.class), (byte) (0x20)); // invis
		if (this.headRotation != null) {
			watcher.setObject(16, Registry.getVectorSerializer(), this.headRotation);
		}
		if (this.bodyRotation != null) {
			watcher.setObject(17, Registry.getVectorSerializer(), this.bodyRotation);
		}

		// for custom name
//		Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(this.customName)[0].getHandle());
//		watcher.setObject(new WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), opt);

		final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
		watcher.getWatchableObjects().stream().filter(Objects::nonNull).forEach(entry -> {
			final WrappedDataWatcher.WrappedDataWatcherObject dataWatcherObject = entry.getWatcherObject();
			wrappedDataValueList.add(new WrappedDataValue(dataWatcherObject.getIndex(), dataWatcherObject.getSerializer(), entry.getRawValue()));
		});
		packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);

		Packets.sendPacket(player, packet);

		Packets.sendPacket(player, Packets.ENTITY_EQUIPMENT_HEAD(this.entityId, Material.LIME_STAINED_GLASS_PANE));

		if (register) {
			this.shownTo.add(player);
		}
	}

	public void hideFrom(Player player, Boolean register) {
		Packets.sendPacket(player, Packets.DESTROY_ENTITY(this.entityId));
		if (register) {
			this.shownTo.remove(player);
		}

	}

	public void resetAllShownTo() {
		for (Player p : this.shownTo) {
			this.hideFrom(p, false);
		}
		this.shownTo.clear();
	}

	public Location getLoc() {
		return this.loc;
	}

	public int getEntityId() {
		return entityId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public PlayerInfo getPlayerInfo() {
		return pInfo;
	}

}
