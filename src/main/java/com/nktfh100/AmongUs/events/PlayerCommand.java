package com.nktfh100.AmongUs.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class PlayerCommand implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCommand(PlayerCommandPreprocessEvent ev) {
		Player player = ev.getPlayer();
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if (!pInfo.getIsIngame()) {
			return;
		}
		if (pInfo.getArena().getGameState() == GameState.RUNNING && !ev.getMessage().isEmpty()) {
			String cmd = ev.getMessage().toLowerCase().split(" ")[0];
			for (String cmd1 : Main.getConfigManager().getBlockedCommands()) {
				if (cmd.equalsIgnoreCase(cmd1)) {
					ev.setCancelled(true);
					return;
				}
			}
		}
		if (ev.getMessage().equalsIgnoreCase("/aua test") && player.hasPermission("aua.test")) {
			pInfo.getArena()._isTesting = true;
			pInfo.getArena().testingPlayer = player;
			player.sendMessage(ChatColor.GREEN + "Enabled testing mode!");
		}
	}
}
