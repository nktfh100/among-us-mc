package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.Collections;

import com.nktfh100.AmongUs.holograms.ImposterHologram;
import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.VitalsPlayerInfo;
import com.nktfh100.AmongUs.inventory.VitalsInv;

public class VitalsManager {

	private Arena arena;
	private ArrayList<VitalsPlayerInfo> players = new ArrayList<VitalsPlayerInfo>();
	private ImposterHologram holo;

	public VitalsManager(Arena arena) {
		this.arena = arena;
	}

	public void addPlayer(PlayerInfo pInfo) {
		this.players.add(new VitalsPlayerInfo(pInfo));
		Collections.sort(this.players);
	}

	public VitalsPlayerInfo getVitalsPInfo(Player player) {
		for (VitalsPlayerInfo vpi : this.players) {
			if (vpi.getPlayer() == player) {
				return vpi;
			}
		}
		return null;
	}

	public void openInventory(Player player) {
		player.openInventory(new VitalsInv(this).getInventory());
	}

	public void updateInventory() {
		for (Player player : this.arena.getPlayers()) {
			if(player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null) {				
				if (player.getOpenInventory().getTopInventory().getHolder() instanceof VitalsInv) {
					((VitalsInv) player.getOpenInventory().getTopInventory().getHolder()).update();
				}
			}
		}
	}

	public Arena getArena() {
		return arena;
	}

	public ArrayList<VitalsPlayerInfo> getPlayers() {
		return players;
	}

	public ImposterHologram getHolo() {
		return holo;
	}

	public void setHolo(ImposterHologram holo) {
		this.holo = holo;
	}

}
