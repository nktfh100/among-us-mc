package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.nktfh100.AmongUs.holograms.ImposterHologram;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.Vent;
import com.nktfh100.AmongUs.info.VentGroup;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class VentsManager {

	private Arena arena;
	private ArrayList<VentGroup> ventGroups = new ArrayList<VentGroup>();
	private ArrayList<ImposterHologram> holos = new ArrayList<ImposterHologram>();

	public VentsManager(Arena arena) {
		this.arena = arena;
	}

	public void ventHoloClick(PlayerInfo pInfo, Integer vgId, Integer vId) {
		Player player = pInfo.getPlayer();
		pInfo.setIsInVent(true);
		pInfo.setVentGroup(this.ventGroups.get(vgId));
		pInfo.setVent(this.ventGroups.get(vgId).getVent(vId));
		this.hideAllHolos(player);

		this.arena.giveGameInventory(pInfo);
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
		player.setVelocity(new Vector(0, 0, 0));
		player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1.3, 0), 40, 0.3D, 0.3D, 0.3D, Material.IRON_BLOCK.createBlockData());

		Main.getSoundsManager().playSound("playerGetInVent", player, pInfo.getVent().getLoc());

		for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
			if (pInfo1 != pInfo) {
				this.arena.getVisibilityManager().hidePlayer(pInfo1, pInfo, true);
			}
		}
		pInfo.getPlayer().teleport(pInfo.getVent().getPlayerLoc());
		this.arena.getVisibilityManager().playerMoved(pInfo, pInfo.getVent().getPlayerLoc());
	}

	public void playerLeaveVent(PlayerInfo pInfo, Boolean isForce, Boolean endGame) {
		Player player = pInfo.getPlayer();
		pInfo.setIsInVent(false);
		this.showAllHolos(player);
		
		if(!pInfo.isGhost()) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);			
		}
		if (!isForce) {
			player.getWorld().spawnParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1.3, 0), 40, 0.3D, 0.3D, 0.3D, Material.IRON_BLOCK.createBlockData());
		}

		if (!isForce) {
			Main.getSoundsManager().playSound("playerLeaveVent", player, pInfo.getVent().getLoc());
			this.arena.giveGameInventory(pInfo);
		}

		for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
			if (pInfo != pInfo1) {
				if (!pInfo.isGhost() || (pInfo.isGhost() && pInfo1.isGhost())) {
					this.arena.getVisibilityManager().showPlayer(pInfo1, pInfo, true);
				}
			}
		}
		if (!endGame) {
			pInfo.getArena().getVisibilityManager().playerMoved(pInfo);
		}

		pInfo.setVentGroup(null);
		pInfo.setVent(null);
	}

	public void playerPrevVent(PlayerInfo pInfo) {
		int id = pInfo.getVent().getId();
		if (id == 0) {
			if (!pInfo.getVentGroup().getLoop()) {
				return;
			}
			id = pInfo.getVentGroup().getVents().size() - 1;
		} else {
			id--;
		}
		pInfo.getPlayer().getInventory().clear();
		pInfo.setVent(pInfo.getVentGroup().getVent(id));
		pInfo.getPlayer().teleport(pInfo.getVent().getPlayerLoc());
		Main.getSoundsManager().playSound("playerNextVent", pInfo.getPlayer(), pInfo.getVent().getLoc());
		new BukkitRunnable() {
			@Override
			public void run() {
				if (pInfo.getIsIngame()) {
					pInfo.getArena().giveGameInventory(pInfo);
				}
			}
		}.runTaskLater(Main.getPlugin(), 5L);
		Utils.sendActionBar(pInfo.getPlayer(), arena.getVentsManager().getVentActionBar(pInfo.getVent()));
		pInfo.getArena().getVisibilityManager().playerMoved(pInfo, pInfo.getVent().getPlayerLoc());
	}

	public void playerNextVent(PlayerInfo pInfo) {
		int id = pInfo.getVent().getId();
		if (id == pInfo.getVentGroup().getVents().size() - 1) {
			if (!pInfo.getVentGroup().getLoop()) {
				return;
			}
			id = 0;
		} else {
			id++;
		}
		pInfo.getPlayer().getInventory().clear();
		pInfo.setVent(pInfo.getVentGroup().getVent(id));
		pInfo.getPlayer().teleport(pInfo.getVent().getPlayerLoc());
		Main.getSoundsManager().playSound("playerNextVent", pInfo.getPlayer(), pInfo.getVent().getLoc());
		new BukkitRunnable() {
			@Override
			public void run() {
				if (pInfo.getIsIngame()) {
					pInfo.getArena().giveGameInventory(pInfo);
				}
			}
		}.runTaskLater(Main.getPlugin(), 5L);
		Utils.sendActionBar(pInfo.getPlayer(), arena.getVentsManager().getVentActionBar(pInfo.getVent()));
		pInfo.getArena().getVisibilityManager().playerMoved(pInfo, pInfo.getVent().getPlayerLoc());
	}

	public void hideAllHolos(Player player) {
		for (VentGroup vg : this.ventGroups) {
			for (Vent v : vg.getVents()) {
				v.getHolo().hideTo(player);
			}
		}
	}

	public void showAllHolos(Player player) {
		for (VentGroup vg : this.ventGroups) {
			for (Vent v : vg.getVents()) {
				v.getHolo().showTo(player);
			}
		}
	}

	public String getVentActionBar(Vent vent) {
		if (vent.getLocName() == null) {
			return Main.getMessagesManager().getGameMsg("ventActionBar1", arena, null);
		} else {
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%location%", vent.getLocName().getName());
			return Main.getMessagesManager().getGameMsg("ventActionBar", arena, placeholders);
		}
	}

	public void addVentGroup(VentGroup vg) {
		this.ventGroups.add(vg);
		Collections.sort(this.ventGroups);
	}

	public VentGroup getVentGroup(Integer id) {
		return this.ventGroups.get(id);
	}

	public void addVent(Integer vgId, Vent v) {
		this.ventGroups.get(vgId).addVent(v);
	}
	
	public void delete() {
		this.arena = null;
		for(VentGroup vg : this.ventGroups) {
			vg.delete();
		}
		this.ventGroups = null;
		for(ImposterHologram holo : holos) {
			holo.deleteHologram();
		}
		this.holos = null;
	}

	public Arena getArena() {
		return arena;
	}

	public ArrayList<VentGroup> getVentGroups() {
		return ventGroups;
	}

	public ArrayList<ImposterHologram> getHolos() {
		return holos;
	}

}
