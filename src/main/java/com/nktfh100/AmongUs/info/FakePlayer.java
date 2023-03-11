package com.nktfh100.AmongUs.info;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Packets;

public class FakePlayer {

	private Arena arena;
	private Player orgPlayer;
	private PlayerInfo orgPInfo;
	private String name;
	private String customName;
	private ColorInfo color;
	private int entityId;
	private UUID uuid;
	private String textureValue = "";
	private String textureSignature = "";

	private ArrayList<Player> tabShownTo = new ArrayList<Player>();
	private ArrayList<Player> playerShownTo = new ArrayList<Player>();

	public FakePlayer(Arena arena, PlayerInfo pInfo) {
		this.arena = arena;
		this.orgPlayer = pInfo.getPlayer();
		this.orgPInfo = pInfo;
		this.name = this.orgPlayer.getName();
		this.customName = pInfo.getCustomName();
		this.color = pInfo.getColor();
		this.entityId = pInfo.getFakePlayerId();
		this.uuid = pInfo.getFakePlayerUUID();
		this.textureValue = pInfo.getTextureValue();
		this.textureSignature = pInfo.getTextureSignature();
	}

	public FakePlayer(Arena arena, PlayerInfo pInfo, int entityId, UUID uuid) {
		this.arena = arena;
		this.orgPlayer = pInfo.getPlayer();
		this.orgPInfo = pInfo;
		this.name = this.orgPlayer.getName();
		this.customName = pInfo.getCustomName();
		this.color = pInfo.getColor();
		this.entityId = entityId;
		this.uuid = uuid;
		this.textureValue = pInfo.getTextureValue();
		this.textureSignature = pInfo.getTextureSignature();
	}

	public void showPlayerTo(PlayerInfo pInfo, Location loc, Boolean dead, Boolean register) {
		Player player = pInfo.getPlayer();
		if (this.playerShownTo.contains(player)) {
			return;
		}
		Packets.sendPacket(player, Packets.ADD_PLAYER(this.uuid, this.name, this.customName, this.textureValue, this.textureSignature));

		Packets.sendPacket(player, Packets.SPAWN_PLAYER(loc, this.entityId, this.uuid));
		Packets.sendPacket(player, Packets.METADATA_SKIN(this.entityId, pInfo.getPlayer(), false));
		if (!dead) {
			Packets.sendPacket(player, Packets.ENTITY_HEAD_ROTATION(this.entityId, loc));
			Packets.sendPacket(player, Packets.ENTITY_LOOK(this.entityId, loc));
		}
		Packets.sendPacket(player, Packets.PLAYER_ARMOR(this.color, this.entityId));
		if (dead) {
			Packets.sendPacket(player, Packets.PLAYER_SLEEPING(this.entityId));
		}
		if (register) {
			this.playerShownTo.add(player);
		}
		FakePlayer fp = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				Packets.sendPacket(player, Packets.REMOVE_PLAYER(fp.getUuid()));
			}
		}.runTaskLater(Main.getPlugin(), 2L);
	}

	public void hidePlayerFrom(Player player, Boolean register) {
		if (this.playerShownTo.contains(player) || !register) {
			Packets.sendPacket(player, Packets.DESTROY_ENTITY(this.entityId));

			if (register) {
				this.playerShownTo.remove(player);
			}
		}
	}

	public void resetAllPlayerVis() {
		for (Player p : this.playerShownTo) {
			this.hidePlayerFrom(p, false);
		}
		this.playerShownTo.clear();
		;
	}

	public Boolean isTabShownTo(Player player) {
		return this.tabShownTo.contains(player);
	}

	public Boolean isPlayerShownTo(Player player) {
		return this.playerShownTo.contains(player);
	}

	public int getEntityId() {
		return entityId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getTextureValue() {
		return textureValue;
	}

	public String getTextureSignature() {
		return textureSignature;
	}

	public Arena getArena() {
		return arena;
	}

	public Player getOrgPlayer() {
		return orgPlayer;
	}

	public PlayerInfo getOrgPInfo() {
		return orgPInfo;
	}

	public String getName() {
		return name;
	}

	public ColorInfo getColor() {
		return color;
	}

	public String getCustomName() {
		return customName;
	}

	public ArrayList<Player> getTabShownTo() {
		return tabShownTo;
	}

	public ArrayList<Player> getPlayerShownTo() {
		return playerShownTo;
	}

}
