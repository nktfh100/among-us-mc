package com.nktfh100.AmongUs.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class PlayerSwapHand implements Listener {

	@EventHandler
	public void swapHand(PlayerSwapHandItemsEvent ev) {
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(ev.getPlayer());
		if (pInfo.getIsIngame()) {
			ItemInfoContainer mapInfo = Main.getItemsManager().getItem("map");
			if (ev.getOffHandItem().getType() == Material.FILLED_MAP) {
				ev.setMainHandItem(Utils.createItem(mapInfo.getItem2().getMat(), mapInfo.getItem2().getTitle(), 1, mapInfo.getItem2().getLore()));
				pInfo.setIsMapInOffHand(true);
				return;
			} else if (ev.getMainHandItem().getType() == Material.FILLED_MAP) {
				if (ev.getOffHandItem().isSimilar(Utils.createItem(mapInfo.getItem2().getMat(), mapInfo.getItem2().getTitle(), 1, mapInfo.getItem2().getLore()))) {
					ev.setOffHandItem(new ItemStack(Material.AIR));
					pInfo.setIsMapInOffHand(false);
					return;
				}
			}
			ev.setCancelled(true);
		}
	}

}
