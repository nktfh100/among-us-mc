package com.nktfh100.AmongUs.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.DeadBody;
import com.nktfh100.AmongUs.info.FakeArmorStand;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Packets;
import com.nktfh100.AmongUs.utils.Utils;
import com.nktfh100.AmongUs.holograms.ImposterHologram;

public class VisibilityManager {

	Arena arena;

	public VisibilityManager(Arena arena) {
		this.arena = arena;
	}

	public void resetBodyVis(DeadBody db) {
		db.getPlayersShownTo().clear();
		for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
			this.checkPlayerBodyVis(pInfo, db);
		}
	}

	// check if player can see the body or not and hide/show the body
	public void checkPlayerBodyVis(PlayerInfo pInfo, DeadBody db) {
		Player player = pInfo.getPlayer();
		Boolean isShown = db.isShownTo(player);
		if (!pInfo.isGhost() && this.arena.getEnableReducedVision()) {
			Integer state_ = Utils.isInsideCircle(player.getLocation(), (double) pInfo.getVision(), db.getLocation());
			if (state_ == 2 && isShown) { // outside view range
				db.hideFrom(player, true);
			} else if (state_ < 2 && !isShown) {
				db.showTo(pInfo, true);
			}
		} else if (!isShown) {
			db.showTo(pInfo, true);
		}
	}

	public void checkPlayerHoloVis(PlayerInfo pInfo, ImposterHologram holo) {
		if (holo == null || holo.isDeleted()) {
			return;
		}
		if (this.arena.getEnableReducedVision()) {
			Integer state = Utils.isInsideCircle(pInfo.getPlayer().getLocation(), (double) pInfo.getVision(), holo.getLocation());
			Boolean canSee = holo.isVisibleTo(pInfo.getPlayer());
			if (canSee && state == 2) {
				holo.hideTo(pInfo.getPlayer());
			} else if (!canSee && state != 2) {
				holo.showTo(pInfo.getPlayer());
			}
		} else {
			if (!holo.isVisibleTo(pInfo.getPlayer())) {
				holo.showTo(pInfo.getPlayer());
			}
		}
	}

	public void playerMoved(PlayerInfo pInfo, Location newLoc) {
		Boolean areLightsOut = (this.arena.getSabotageManager().getIsSabotageActive() && this.arena.getSabotageManager().getActiveSabotage().getType() == SabotageType.LIGHTS);

		if (this.arena.getEnableReducedVision()) {
			if (!pInfo.isGhost() && !this.arena.getIsInMeeting() && !pInfo.getIsInCameras()) {

				if (!areLightsOut || (pInfo.getIsImposter() && areLightsOut)) {
					pInfo.updateVisionBlocks(newLoc);
				}

				this.updateVisionOf(pInfo);
			}
		}
		for (DeadBody db : this.arena.getDeadBodiesManager().getBodies()) {
			this.checkPlayerBodyVis(pInfo, db);
		}
	}

	public void playerMoved(PlayerInfo pInfo) {
		this.playerMoved(pInfo, pInfo.getPlayer().getLocation());
	}

	private void updateVisionOf(PlayerInfo pInfo) {
		if (pInfo != null && pInfo.getIsIngame() && pInfo.getArena().getGameState() == GameState.RUNNING && !pInfo.getArena().getIsInMeeting()) {
			Player player = pInfo.getPlayer();

			// holograms
			if (this.arena.getHideHologramsOutOfView()) {
				if (this.arena.getSabotageManager().getIsSabotageActive()) {
					for (ImposterHologram holo : this.arena.getSabotageManager().getActiveSabotage().getHolos()) {
						this.checkPlayerHoloVis(pInfo, holo);
					}
				}
				if (pInfo.getIsImposter() && !pInfo.isGhost()) {
					for (ImposterHologram holo : this.arena.getVentsManager().getHolos()) {
						this.checkPlayerHoloVis(pInfo, holo);
					}
				}
				for (TaskPlayer tp : this.arena.getTasksManager().getTasksForPlayer(player)) {
					if (!tp.getIsDone()) {
						this.checkPlayerHoloVis(pInfo, tp.getActiveTask().getHolo());
					}
				}
				if (this.arena.getCamerasManager().getHolo() != null) {
					this.checkPlayerHoloVis(pInfo, this.arena.getCamerasManager().getHolo());
				}
				if (this.arena.getVitalsManager() != null && this.arena.getVitalsManager().getHolo() != null) {
					this.checkPlayerHoloVis(pInfo, this.arena.getVitalsManager().getHolo());
				}
				this.checkPlayerHoloVis(pInfo, this.arena.getBtnHolo());
			}

			// players

			for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
				if(pInfo1 == null) {
					continue;
				}
				Player player1 = pInfo1.getPlayer();
				if (!pInfo1.isGhost() && player1 != player) {
					if (!pInfo1.getIsInVent() && !pInfo.getIsInCameras()) {
						Location player1Loc = player1.getLocation();
						if (pInfo1.getIsInCameras()) {
							player1Loc = pInfo1.getPlayerCamLocTemp();
						}
						// if player sees player1
						if (this.arena.getEnableReducedVision()) {
							if (Utils.isInsideCircle(player.getLocation(), (double) pInfo.getVision(), player1Loc) == 2) { // if player1 goes outside view range
								if (!pInfo.getPlayersHidden().contains(player1)) {
									this.hidePlayer(pInfo, pInfo1, true);
								}
							} else if (pInfo.getPlayersHidden().contains(player1)) { // if player1 goes inside view range
								this.showPlayer(pInfo, pInfo1, true);
							}
						}
					}

					// if player1 sees player
					if (!pInfo.getIsInVent() && !pInfo1.getIsInCameras()) {
						Location playerLoc = player.getLocation();
						if (pInfo.getIsInCameras()) {
							playerLoc = pInfo.getPlayerCamLocTemp();
						}
						if (this.arena.getEnableReducedVision()) {
							if (Utils.isInsideCircle(player1.getLocation(), (double) pInfo1.getVision(), playerLoc) == 2) {
								if (!pInfo1.getPlayersHidden().contains(player)) {
									this.hidePlayer(pInfo1, pInfo, true);
								}
							} else if (pInfo1.getPlayersHidden().contains(player)) {
								this.showPlayer(pInfo1, pInfo, true);
							}
						}
					} else if (!pInfo.getIsInVent() && pInfo1.getIsInCameras() && pInfo1.getPlayersHidden().contains(player) && !pInfo.isGhost()) {
						this.showPlayer(pInfo1, pInfo, true);
					}
				}
			}
		}
	}

	public void showPlayer(PlayerInfo pInfoToShowTo, PlayerInfo pInfoToShow, Boolean changeList) {
		if(pInfoToShow == null || pInfoToShowTo == null) {
			return;
		}
		if (changeList) {
			pInfoToShowTo.getPlayersHidden().remove(pInfoToShow.getPlayer());
		}
		if (pInfoToShow.getIsInCameras()) {
			pInfoToShow.getFakePlayer().showPlayerTo(pInfoToShowTo, pInfoToShow.getPlayerCamLocTemp(), false, changeList);
		} else if (!pInfoToShow.getIsInVent()) {
			if (pInfoToShow.getIsScanning()) {
				for (FakeArmorStand fas : pInfoToShow.getScanArmorStands()) {
					fas.showTo(pInfoToShowTo.getPlayer(), true);
				}
			}

			PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
			Player playerToShow = pInfoToShow.getPlayer();
			spawnPacket.getIntegers().write(0, playerToShow.getEntityId());
			spawnPacket.getUUIDs().write(0, playerToShow.getUniqueId());
			Location loc = playerToShow.getLocation();
			spawnPacket.getDoubles().write(0, loc.getX()).write(1, loc.getY()).write(2, loc.getZ());
			spawnPacket.getBytes().write(0, Packets.toPackedByte(loc.getYaw())).write(1, Packets.toPackedByte(loc.getPitch()));
			Packets.sendPacket(pInfoToShowTo.getPlayer(), spawnPacket);

			// metadata packet
			Packets.sendPacket(pInfoToShowTo.getPlayer(), Packets.METADATA_SKIN(pInfoToShow.getPlayer().getEntityId(), pInfoToShow.getPlayer(), pInfoToShow.isGhost()));

			Packets.sendPacket(pInfoToShowTo.getPlayer(), Packets.ENTITY_LOOK(playerToShow.getPlayer().getEntityId(), loc));
			Packets.sendPacket(pInfoToShowTo.getPlayer(), Packets.ENTITY_HEAD_ROTATION(playerToShow.getPlayer().getEntityId(), loc));

			if (pInfoToShow.getColor() != null && arena.getGameState() != GameState.FINISHING) {
				Packets.sendPacket(pInfoToShowTo.getPlayer(), Packets.PLAYER_ARMOR(pInfoToShow.getColor(), playerToShow.getPlayer().getEntityId()));
			}
		}
		if (pInfoToShow.getIsImposter() && pInfoToShowTo.getIsImposter()) {
			if (pInfoToShow.getImposterHolo() != null) {
				pInfoToShow.getImposterHolo().showTo(pInfoToShow.getPlayer());
			}
		}
	}

	public void hidePlayer(PlayerInfo pInfoToHideTo, PlayerInfo pInfoToHide, Boolean changeList) {
		if (changeList) {
			pInfoToHideTo.getPlayersHidden().add(pInfoToHide.getPlayer());
		}
		if (pInfoToHide.getIsInCameras()) {
			pInfoToHide.getFakePlayer().hidePlayerFrom(pInfoToHideTo.getPlayer(), changeList);
		} else {
			if (pInfoToHide.getIsScanning()) {
				for (FakeArmorStand fas : pInfoToHide.getScanArmorStands()) {
					fas.hideFrom(pInfoToHideTo.getPlayer(), true);
				}
			}
			Packets.sendPacket(pInfoToHideTo.getPlayer(), Packets.DESTROY_ENTITY(pInfoToHide.getPlayer().getEntityId()));
		}
		if (pInfoToHide.getIsImposter() && pInfoToHideTo.getIsImposter() && pInfoToHide.getImposterHolo() != null) {
			pInfoToHide.getImposterHolo().hideTo(pInfoToHideTo.getPlayer());
		}
	}

	public void resetHologramsVis(PlayerInfo pInfo) {
		if (this.arena.getHideHologramsOutOfView()) {
			Player player = pInfo.getPlayer();
			if (this.arena.getSabotageManager().getIsSabotageActive()) {
				for (ImposterHologram holo : this.arena.getSabotageManager().getActiveSabotage().getHolos()) {
					holo.showTo(player);
				}
			}
			if (pInfo.getIsImposter() && !pInfo.isGhost()) {
				for (ImposterHologram holo : this.arena.getVentsManager().getHolos()) {
					holo.showTo(player);
				}
			}
			for (TaskPlayer tp : this.arena.getTasksManager().getTasksForPlayer(player)) {
				if (!tp.getIsDone()) {
					tp.getActiveTask().getHolo().showTo(player);
				}
			}
			if (this.arena.getCamerasManager().getHolo() != null) {
				this.arena.getCamerasManager().getHolo().showTo(player);
			}
			this.arena.getBtnHolo().showTo(player);
		}
	}

	public void resetPlayersHidden(PlayerInfo pInfo) {
		for (Player player1 : pInfo.getPlayersHidden()) {
			PlayerInfo pInfo1 = Main.getPlayersManager().getPlayerInfo(player1);
			if (pInfo1 != null) {
				this.showPlayer(pInfo, pInfo1, false);
			}
		}
		pInfo.getPlayersHidden().clear();
	}

	public void resetFakePlayers(PlayerInfo pInfo) {
		if (pInfo.getFakePlayer() != null) {
			pInfo.getFakePlayer().resetAllPlayerVis();
		}
	}

	public Boolean canSee(PlayerInfo pInfo, PlayerInfo pInfo1) {
		return !pInfo.getPlayersHidden().contains(pInfo1.getPlayer());
	}

	public Arena getArena() {
		return this.arena;
	}

}
