package com.nktfh100.AmongUs.events;

import java.util.HashMap;
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
			pInfo = Main.getPlayersManager().addPlayer(player);
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
			String msg = Main.getMessagesManager().getGameMsg("cantTalk", arena, null, player);
			if (!msg.isEmpty()) {
				player.sendMessage(msg);
			}
			return;
		}

		// Cancels the event so the message isn't sent  to players
		ev.setCancelled(true);

		// The message format to get from the messages.yml file depending on if the user is a ghost or not.
		String key = "chat";
		if (pInfo.isGhost() && arena.getGameState() != GameState.FINISHING) {
			key = "ghostsChat";
		}

		// Gets the message format from the messages.yaml file replacing the placeholders.
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("%player_name%", pInfo.getPlayer().getDisplayName());
		placeholders.put("%player_color%", pInfo.getColor().getChatColor() + "");
		placeholders.put("%player_color_name%", pInfo.getColor().getName());
		placeholders.put("%message%", ev.getMessage());
		String msg = Main.getMessagesManager().getGameMsg(key, arena, placeholders, player);

		// If the game is finishing, send the message to everyone
		if (arena.getGameState() == GameState.FINISHING) {
			for (PlayerInfo pInfo1 : arena.getPlayersInfo()) {
				pInfo1.getPlayer().sendMessage(msg);
			}

		// If the game is running (but players are in a meeting) and the player who sent the message is a ghost, only other
		// ghosts will see the message. Otherwise, everybody will see the message.
		} else {
			for (PlayerInfo pInfo1 : arena.getPlayersInfo()) {
				if (pInfo1 == null) {
					continue;
				}
				if (pInfo.isGhost() && pInfo1.isGhost()) {
					// only ghosts chat
					pInfo1.getPlayer().sendMessage(msg);
				} else if (!pInfo.isGhost()) {
					pInfo1.getPlayer().sendMessage(msg);
				}
			}
		}
	}
}
