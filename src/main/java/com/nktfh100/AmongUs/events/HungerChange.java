package com.nktfh100.AmongUs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class HungerChange implements Listener {

	@EventHandler
	public void FoodLevelChange(FoodLevelChangeEvent ev) {
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo((Player) ev.getEntity());
		if (pInfo != null && pInfo.getIsIngame()) {
			ev.setCancelled(true);
		}
	}
}
