package com.nktfh100.AmongUs.events;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.DeadBody;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.PlayersManager;
import com.nktfh100.AmongUs.utils.Utils;

public class PlayerMove implements Listener {

	PlayersManager playersManager = Main.getPlayersManager();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void move(PlayerMoveEvent ev) {
		PlayerInfo pInfo = playersManager.getPlayerInfo(ev.getPlayer());
		if (pInfo != null && pInfo.getIsIngame()) {
			Arena arena = pInfo.getArena();
			if (pInfo.getIsInCameras()) {
				ev.getPlayer().setAllowFlight(true);
				ev.getPlayer().setFlying(true);
				ev.getPlayer().teleport(pInfo.getActiveCamera().getViewLoc());
				return;
			}
			if (arena.getIsInMeeting() || pInfo.getIsInVent()) {
				if (Utils.hasChangedBlockCoordinates(ev.getFrom(), ev.getTo())) {
					Location from = ev.getFrom();
					double x = from.getBlockX();
					double z = from.getBlockZ();

					x += .5;
					z += .5;
					ev.getPlayer().teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
				}
			} else if (arena.getGameState() == GameState.RUNNING && !pInfo.isGhost() && !pInfo.getIsInCameras() && !pInfo.getIsInVent()) {
				DeadBody db = arena.getDeadBodiesManager().isCloseToBody(ev.getTo());
				if (db != null) {
					if (!pInfo.getCanReportBody()) {
						pInfo.setCanReportBody(true, db);
					}
				} else if (pInfo.getCanReportBody()) {
					pInfo.setCanReportBody(false, null);
				}

				if (arena.getEnableReducedVision()) {
					if (Utils.hasChangedBlockCoordinates(ev.getFrom(), ev.getTo())) {
						arena.getVisibilityManager().playerMoved(pInfo, ev.getTo());
					}
				}

				if (arena.getDisableJumping()) {
					if (ev.getFrom().getY() < ev.getTo().getY()) {
						if (!ev.getPlayer().isOnGround()) {
							ev.setCancelled(true);
						}
					}
				}
			}

			if (pInfo.getIsImposter()) {
				pInfo.teleportImposterHolo();
			}

			if (arena.getGameState() == GameState.RUNNING && !arena.getIsInMeeting() && !pInfo.getIsInCameras() && !pInfo.getIsInVent()) {
				pInfo.updateUseItemState(ev.getTo());
			}
		}
	}
}
