package com.nktfh100.AmongUs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class BlockPlace implements Listener {
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent ev) {
		Player player = ev.getPlayer();
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if (pInfo.getIsIngame()) {
			ev.setCancelled(true);
		}

		if (ev.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(Main.getItemsManager().getItem("arenasSelector").getItem().getTitle())) {
			ev.setCancelled(true);
			if (Main.getConfigManager().getBungeecord() && Main.getConfigManager().getBungeecordIsLobby()) {
				Main.getBungeArenaManager().openArenaSelector(pInfo);
			} else if (!Main.getConfigManager().getBungeecord()) {
				Main.getArenaManager().openArenaSelector(pInfo);
			}
		}
	}
}
