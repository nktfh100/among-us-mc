package com.nktfh100.AmongUs.events;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class PlayerChat implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerChat(AsyncPlayerChatEvent ev) {
		Player player = ev.getPlayer();
		if (ev.getMessage() == null || ev.getMessage().isEmpty()) {
			return;
		}

		if (ev.isCancelled() && !Main.getIsVentureChat()) {
			return;
		}

		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if(pInfo == null) {
			pInfo = Main.getPlayersManager()._addPlayer(player);
		}
		if (!pInfo.getIsIngame()) {
			Set<Player> sentTo = ev.getRecipients();
			for (Arena arena : Main.getArenaManager().getAllArenas()) {
				for (Player player_ : arena.getPlayers()) {
					sentTo.remove(player_);
				}
			}
			return;
		}
		Arena arena = pInfo.getArena();

		if (!arena.getIsInMeeting() && !pInfo.isGhost() && arena.getGameState() == GameState.RUNNING) {
			ev.setCancelled(true);
			String msg = Main.getMessagesManager().getGameMsg("cantTalk", arena, null);
			if (!msg.isEmpty()) {
				player.sendMessage(msg);
			}
			return;
		}
		ev.getRecipients().clear();
		String key = "chat";
		if (pInfo.isGhost() && arena.getGameState() != GameState.FINISHING) {
			key = "ghostsChat";
		}

		String msg = Main.getMessagesManager().getGameMsg(key, arena, "%1\\$s", pInfo.getColor().getChatColor() + "", pInfo.getColor().getName(), "%2\\$s");
		ev.setFormat(msg);
		if (arena.getGameState() == GameState.FINISHING) {
			for (PlayerInfo pInfo1 : arena.getPlayersInfo()) {
				ev.getRecipients().add(pInfo1.getPlayer());
			}
		} else {
			for (PlayerInfo pInfo1 : arena.getPlayersInfo()) {
				if (pInfo1 == null) {
					continue;
				}
				if (pInfo.isGhost() && pInfo1.isGhost()) {
					// only ghosts chat
					ev.getRecipients().add(pInfo1.getPlayer());
				} else if (!pInfo.isGhost()) {
					ev.getRecipients().add(pInfo1.getPlayer());
				}
			}
		}
		ev.setCancelled(false);
	}
}
