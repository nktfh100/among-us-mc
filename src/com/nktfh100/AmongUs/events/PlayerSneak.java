package com.nktfh100.AmongUs.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.Vent;
import com.nktfh100.AmongUs.info.VentGroup;
import com.nktfh100.AmongUs.main.Main;

public class PlayerSneak implements Listener {
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent ev) {
		Player player = ev.getPlayer();
		if (ev.isSneaking()) {
			PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
			if (pInfo == null) {
				return;
			}

			if (pInfo.getIsIngame()) {
				if (pInfo.getIsInVent()) {
					pInfo.getArena().getVentsManager().playerLeaveVent(pInfo, false, false);
				} else if (pInfo.getIsInCameras()) {
					pInfo.getArena().getCamerasManager().playerLeaveCameras(pInfo, false);
				} else if (Main.getConfigManager().getSneakToVent() && pInfo.getIsImposter() && !pInfo.getIsInCameras() && !pInfo.getIsInVent()) {
					Arena arena = pInfo.getArena();
					Location pLoc = player.getLocation();
					World pWorld = player.getWorld();
					for (VentGroup vg : arena.getVentsManager().getVentGroups()) {
						for (Vent v : vg.getVents()) {
							if (v.getLoc().getWorld() == pWorld) {
								if (pLoc.distance(v.getLoc()) <= 3) {
									arena.getVentsManager().ventHoloClick(pInfo, vg.getId(), v.getId());
								}
							}
						}
					}
				}
			}
		}
	}
}
