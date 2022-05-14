package com.nktfh100.AmongUs.info;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.nktfh100.AmongUs.main.Main;

public class DeadBody {

	private Arena arena;
	private Player player;
	private PlayerInfo pInfo;
	private ColorInfo color;
	private Hologram holo;
	private Location loc;
	private FakePlayer fakePlayer;

	private Boolean isDeleted = false;
	private ArrayList<Player> playersShownTo = new ArrayList<Player>();

	@SuppressWarnings("deprecation")
	public DeadBody(Arena arena, Player player) {
		this.player = player;
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		this.pInfo = pInfo;
		this.color = pInfo.getColor();
		this.arena = arena;
		this.loc = player.getLocation();
		this.fakePlayer = new FakePlayer(arena, pInfo, (int) (Math.random() * Integer.MAX_VALUE), UUID.randomUUID());

		if (!player.isOnGround()) {
			Location startLoc = player.getLocation().clone();
			int startLocY = startLoc.getBlockY();
			World world = startLoc.getWorld();
			// so the body wont be floating
			for (int i = 0; i < 5; i++) {
				Block block = world.getBlockAt(startLoc.getBlockX(), startLocY - i, startLoc.getBlockZ());
				if (block == null || block.getType() != Material.AIR) {
					startLocY = startLocY - i;
					break;
				}
			}
			this.loc.setY(startLocY + 1);
		}
	}

	public void create() {
		this.holo = HologramsAPI.createHologram(Main.getPlugin(), this.loc.clone().add(0, 1.8, 0));
		this.holo.appendItemLine(pInfo.getHead()); // floating head
		this.arena.getVisibilityManager().resetBodyVis(this);
	}

	public void showTo(PlayerInfo toPInfo, Boolean register) {
		Player player = toPInfo.getPlayer();
		if (this.isDeleted) {
			return;
		}
		if (register) {
			this.playersShownTo.add(player);
		}
		if (this.holo != null) {
			this.holo.getVisibilityManager().showTo(player);
		}

		this.fakePlayer.showPlayerTo(toPInfo, this.loc, true, register);
	}

	public void hideFrom(Player player, Boolean register) {
		if (this.isDeleted) {
			return;
		}
		if (register) {
			this.playersShownTo.remove(player);
		}
		if (this.holo != null) {
			this.holo.getVisibilityManager().hideTo(player);
		}
		this.fakePlayer.hidePlayerFrom(player, register);
	}

	public Boolean isShownTo(Player p) {
		return this.playersShownTo.contains(p);
	}

	public void delete() {
		for (Player p : this.playersShownTo) {
			this.hideFrom(p, false);
		}
		this.playersShownTo.clear();
		this.holo.delete();
		this.isDeleted = true;
	}

	public Location getLocation() {
		return this.loc;
	}

	public Arena getArena() {
		return arena;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Hologram getHolo() {
		return holo;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public ArrayList<Player> getPlayersShownTo() {
		return playersShownTo;
	}

	public ColorInfo getColor() {
		return color;
	}

	public PlayerInfo getPlayerInfo() {
		return pInfo;
	}
}
