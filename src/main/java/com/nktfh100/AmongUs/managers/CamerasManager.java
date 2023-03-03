package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.filoghost.holographicdisplays.api.hologram.Hologram;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.Camera;
import com.nktfh100.AmongUs.info.DeadBody;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Packets;
import com.nktfh100.AmongUs.utils.Utils;

public class CamerasManager {

	private Arena arena;
	private Location holoLoc;
	private Hologram holo;
	private ArrayList<Camera> cameras = new ArrayList<Camera>();
	private ArrayList<PlayerInfo> playersInCameras = new ArrayList<PlayerInfo>();

	public CamerasManager(Arena arena) {
		this.arena = arena;
	}

	@SuppressWarnings("deprecation")
	public void camerasHoloClick(PlayerInfo pInfo) {
		if (this.cameras.size() == 0 || this.arena.getGameState() != GameState.RUNNING) {
			return;
		}
		if(this.arena.getSabotageManager().getIsSabotageActive() && this.arena.getSabotageManager().getActiveSabotage().getType() == SabotageType.COMMUNICATIONS) {
			return;
		}
		
		Iterator<PlayerInfo> iter = playersInCameras.iterator();
		while (iter.hasNext()) {
			PlayerInfo next = iter.next();
			if (!next.getIsIngame() || !next.getIsInCameras()) {
				iter.remove();
			}
		}

		Player player = pInfo.getPlayer();
		if (!pInfo.isGhost()) {
			this.playersInCameras.add(pInfo);
			for (Camera cam : this.cameras) {
				cam.updateLamp();
			}
		}

		Location loc_ = player.getLocation().clone();
		if (!player.isOnGround()) {
			loc_.setY(Math.floor(loc_.getY()));
			for (int i = 0; i < 3; i++) {
				loc_.add(0, -1, 0);
				if (!loc_.getBlock().isEmpty()) {
					loc_.add(0, 1, 0);
					break;
				}
			}
		}
		pInfo.setPlayerCamLocTemp(loc_);
		pInfo.setIsInCameras(true);
		pInfo.setActiveCamera(cameras.get(0));

		// send fake players at the position of the cameras holo
		// and hide this player from all other players
		for (PlayerInfo pInfo1 : pInfo.getArena().getPlayersInfo()) {
			if (pInfo1 != pInfo) {
				Packets.sendPacket(pInfo1.getPlayer(), Packets.DESTROY_ENTITY(pInfo.getPlayer().getEntityId()));
				if (!pInfo.isGhost() || (pInfo.isGhost() && pInfo1.isGhost())) {
					if (this.arena.getEnableReducedVision()) {
						if (Utils.isInsideCircle(pInfo1.getPlayer().getLocation(), (double) pInfo1.getVision(), pInfo.getPlayer().getLocation()) != 2) {
							pInfo.getFakePlayer().showPlayerTo(pInfo1, pInfo.getPlayerCamLocTemp(), false, true);
						}
					} else {
						pInfo.getFakePlayer().showPlayerTo(pInfo1, pInfo.getPlayerCamLocTemp(), false, true);
					}
				}
			}
		}

		pInfo.getActiveCamera().setIsActive(true);
		pInfo.getActiveCamera().showFakeBlocks(player);
		pInfo.getActiveCamera().showFakeAirBlocks(player);

		pInfo.removeVisionBlocks();
		this.arena.giveGameInventory(pInfo);
		player.setVelocity(new Vector(0, 0, 0));

		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(pInfo.getActiveCamera().getViewLoc());

		Main.getSoundsManager().playSound("playerGetInCameras", player, pInfo.getActiveCamera().getViewLoc());

		// show all players to this player
		if (!pInfo.isGhost()) {
			for (PlayerInfo pInfo1 : arena.getPlayersInfo()) {
				if (pInfo != pInfo1 && !pInfo1.isGhost() && !pInfo1.getIsInCameras() && !pInfo1.getIsInVent()) {
					this.arena.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
				}
			}
		}
		// show all bodies
		for (DeadBody db : this.arena.getDeadBodiesManager().getBodies()) {
			db.showTo(pInfo, true);
		}
	}

	public void playerLeaveCameras(PlayerInfo pInfo, Boolean isForce) {
		Player player = pInfo.getPlayer();

		if (!pInfo.isGhost()) {
			this.playersInCameras.remove(pInfo);
			for (Camera cam : this.cameras) {
				cam.updateLamp();
			}
		}

		pInfo.setIsInCameras(false);
		pInfo.getActiveCamera().setIsActive(false);
		pInfo.getActiveCamera().hideFakeBlocks(pInfo.getPlayer());
		pInfo.getActiveCamera().hideFakeAirBlocks(player);

		pInfo.getPlayer().teleport(pInfo.getPlayerCamLocTemp());
		Main.getSoundsManager().playSound("playerLeaveCameras", player, pInfo.getPlayerCamLocTemp());

		if (pInfo.isGhost() && Main.getConfigManager().getGhostsFly()) {
			pInfo.getPlayer().setAllowFlight(true);
		} else {
			pInfo.getPlayer().setAllowFlight(false);
		}
		pInfo.getPlayer().setFlying(false);
		pInfo.setActiveCamera(null);
		if (!isForce) {
			this.arena.giveGameInventory(pInfo);
			for (DeadBody db : this.arena.getDeadBodiesManager().getBodies()) {
				this.arena.getVisibilityManager().checkPlayerBodyVis(pInfo, db);
			}
		}
		for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
			if(pInfo1 == null) {
				continue;
			}
			if (pInfo1 != pInfo) {
				pInfo.getFakePlayer().hidePlayerFrom(pInfo1.getPlayer(), true);
				if (this.arena.getEnableReducedVision()) {
					if (Utils.isInsideCircle(pInfo1.getPlayer().getLocation(), (double) pInfo1.getVision(), pInfo.getPlayerCamLocTemp()) != 2) {
						this.arena.getVisibilityManager().showPlayer(pInfo1, pInfo, true);
					}
				} else {
					this.arena.getVisibilityManager().showPlayer(pInfo1, pInfo, true);
				}
			}
		}
		this.arena.getVisibilityManager().playerMoved(pInfo, pInfo.getPlayerCamLocTemp());
		pInfo.setPlayerCamLocTemp(null);
	}

	public void playerPrevCamera(PlayerInfo pInfo) {
		int id = pInfo.getActiveCamera().getId();
		if (id == 0) {
			id = this.cameras.size() - 1;
		} else {
			id--;
		}
		pInfo.getPlayer().getInventory().clear();
		pInfo.getActiveCamera().setIsActive(false);
		pInfo.getActiveCamera().hideFakeBlocks(pInfo.getPlayer());
		pInfo.getActiveCamera().hideFakeAirBlocks(pInfo.getPlayer());

		pInfo.setActiveCamera(this.cameras.get(id));

		pInfo.getActiveCamera().setIsActive(true);
		pInfo.getActiveCamera().showFakeBlocks(pInfo.getPlayer());
		pInfo.getActiveCamera().showFakeAirBlocks(pInfo.getPlayer());

		pInfo.getPlayer().setAllowFlight(true);
		pInfo.getPlayer().setFlying(true);
		pInfo.getPlayer().teleport(pInfo.getActiveCamera().getViewLoc());
		new BukkitRunnable() {
			@Override
			public void run() {
				if (pInfo.getIsIngame()) {
					pInfo.getArena().giveGameInventory(pInfo);
				}
			}
		}.runTaskLater(Main.getPlugin(), 5L);
		Utils.sendActionBar(pInfo.getPlayer(), this.getCameraActionBar(pInfo.getActiveCamera()));
		Main.getSoundsManager().playSound("playerNextCamera", pInfo.getPlayer(), pInfo.getActiveCamera().getViewLoc());

		for (PlayerInfo pInfo1 : pInfo.getArena().getPlayersInfo()) {
			if (pInfo1 != pInfo && !pInfo1.getIsInVent() && !pInfo1.getIsInCameras()) {
//				Packets.sendPacket(pInfo1.getPlayer(), Packets.DESTROY_ENTITY(pInfo.getPlayer().getEntityId()));
				if (pInfo1.isGhost()) {
					this.arena.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
				}
			}
		}
		// show all bodies
		for (DeadBody db : this.arena.getDeadBodiesManager().getBodies()) {
			db.showTo(pInfo, true);
		}
	}

	public void playerNextCamera(PlayerInfo pInfo) {
		int id = pInfo.getActiveCamera().getId();
		if (id == this.cameras.size() - 1) {
			id = 0;
		} else {
			id++;
		}
		pInfo.getPlayer().getInventory().clear();
		pInfo.getActiveCamera().setIsActive(false);
		pInfo.getActiveCamera().hideFakeBlocks(pInfo.getPlayer());
		pInfo.getActiveCamera().hideFakeAirBlocks(pInfo.getPlayer());

		pInfo.setActiveCamera(this.cameras.get(id));

		pInfo.getActiveCamera().showFakeBlocks(pInfo.getPlayer());
		pInfo.getActiveCamera().showFakeAirBlocks(pInfo.getPlayer());
		pInfo.getActiveCamera().setIsActive(true);

		pInfo.getPlayer().setAllowFlight(true);
		pInfo.getPlayer().setFlying(true);
		pInfo.getPlayer().teleport(pInfo.getActiveCamera().getViewLoc());
		new BukkitRunnable() {
			@Override
			public void run() {
				if (pInfo.getIsIngame()) {
					pInfo.getArena().giveGameInventory(pInfo);
				}
			}
		}.runTaskLater(Main.getPlugin(), 5L);
		Utils.sendActionBar(pInfo.getPlayer(), this.getCameraActionBar(pInfo.getActiveCamera()));
		Main.getSoundsManager().playSound("playerNextCamera", pInfo.getPlayer(), pInfo.getActiveCamera().getViewLoc());

		for (PlayerInfo pInfo1 : pInfo.getArena().getPlayersInfo()) {
			if (pInfo1 != pInfo && !pInfo1.getIsInVent() && !pInfo1.getIsInCameras()) {
//				Packets.sendPacket(pInfo1.getPlayer(), Packets.DESTROY_ENTITY(pInfo.getPlayer().getEntityId()));
				if (pInfo1.isGhost()) {
					this.arena.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
				}
			}
		}
		// show all bodies
		for (DeadBody db : this.arena.getDeadBodiesManager().getBodies()) {
			db.showTo(pInfo, true);
		}
	}

	public String getCameraActionBar(Camera camera) {
		if (camera.getLocName() == null) {
			return Main.getMessagesManager().getGameMsg("cameraActionBar1", arena, null);
		} else {
			return Main.getMessagesManager().getGameMsg("cameraActionBar", arena, camera.getLocName().getName());
		}
	}

	public void addCamera(Camera c) {
		this.cameras.add(c);
		Collections.sort(this.cameras);
	}

	public void delete() {
		for (Camera cam : this.cameras) {
			cam.delete();
		}
		this.arena = null;
		this.cameras = null;
		this.playersInCameras = null;
		this.holoLoc = null;
		this.holo = null;
		this.cameras = null;
		this.playersInCameras = null;
	}

	public Arena getArena() {
		return arena;
	}

	public ArrayList<Camera> getCameras() {
		return this.cameras;
	}

	public Hologram getHolo() {
		return holo;
	}

	public void setHolo(Hologram holo) {
		this.holo = holo;
	}

	public Location getHoloLoc() {
		return holoLoc;
	}

	public void setHoloLoc(Location holoLoc) {
		this.holoLoc = holoLoc;
	}

	public ArrayList<PlayerInfo> getPlayersInCameras() {
		return playersInCameras;
	}

}
