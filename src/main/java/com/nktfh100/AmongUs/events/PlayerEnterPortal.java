package com.nktfh100.AmongUs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.BungeArena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class PlayerEnterPortal implements Listener {

	private static final long cooldownTime = 2500;

	@EventHandler
	public void onEnter(EntityPortalEnterEvent ev) {
		if (!(ev.getEntity() instanceof Player)) {
			return;
		}
		if (Main.getConfigManager().getEnablePortalJoin()) {
			final Player player = (Player) ev.getEntity();
			PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
			if (pInfo == null) {
				pInfo = Main.getPlayersManager().addPlayer(player);
			}
			if (pInfo.getIsIngame()) {
				return;
			}

			long finish = System.currentTimeMillis();
			long timeElapsed = finish - pInfo.getPortalCooldown();
			if (timeElapsed >= cooldownTime) {
				pInfo.setPortalCooldown(System.currentTimeMillis());
				new BukkitRunnable() {
					@Override
					public void run() {
						// join arena with most players
						if (Main.getConfigManager().getBungeecord() && Main.getConfigManager().getBungeecordIsLobby()) {
							BungeArena arena_ = Main.getBungeArenaManager().getArenaWithMostPlayers();
							if (arena_ != null) {
								Main.sendPlayerToArena(player, arena_.getServer());
							}
						} else {
							Arena arena = Main.getArenaManager().getArenaWithMostPlayers();
							if (arena != null) {
								arena.playerJoin(player);
							}
						}
					}
				}.runTaskLater(Main.getPlugin(), 2L);
			}
		}
	}

}
