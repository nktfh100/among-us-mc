package com.nktfh100.AmongUs.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class PlayerDrop implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void playerItemDrop(PlayerDropItemEvent ev) {
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(ev.getPlayer());
		if (pInfo.getIsIngame()) {
			ev.setCancelled(true);
		}
	}
}
