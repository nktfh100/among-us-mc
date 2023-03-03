package com.nktfh100.AmongUs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.inventory.CustomHolder;
import com.nktfh100.AmongUs.inventory.sabotages.SabotageInvHolder;
import com.nktfh100.AmongUs.inventory.tasks.TaskInvHolder;
import com.nktfh100.AmongUs.main.Main;

public class InvClose implements Listener {
	@EventHandler
	public void onInvClose(InventoryCloseEvent ev) {
		Player player = (Player) ev.getPlayer();
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if (pInfo != null) {
			if (pInfo.getIsIngame()) {
				InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
				if (holder instanceof CustomHolder) {
					if (holder instanceof TaskInvHolder) {
						TaskInvHolder taskInvHolder = (TaskInvHolder) holder;
						taskInvHolder.invClosed();
					} else if (holder instanceof SabotageInvHolder) {
						SabotageInvHolder saboInvHolder = (SabotageInvHolder) holder;
						saboInvHolder.invClosed(player);
						if (pInfo.getIsImposter()) {
							pInfo.setKillCoolDownPaused(false);
						}
					}
				}
			}
		}
	}
}
