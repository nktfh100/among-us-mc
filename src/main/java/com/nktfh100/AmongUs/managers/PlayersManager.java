package com.nktfh100.AmongUs.managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class PlayersManager {

	private HashMap<UUID, PlayerInfo> players = new HashMap<>();

	public PlayersManager() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			PlayerInfo pInfo = new PlayerInfo(player);
			players.put(player.getUniqueId(), pInfo);

			if (Main.getConfigManager().getMysql_enabled()) {
				pInfo.getStatsManager().mysql_registerPlayer(true);
			} else {
				pInfo.getStatsManager().loadStats();
			}
		}
	}

	public PlayerInfo addPlayer(Player player) {
		PlayerInfo out = new PlayerInfo(player);

		if (Main.getConfigManager().getMysql_enabled()) {
			out.getStatsManager().mysql_registerPlayer(true);
		} else {
			out.getStatsManager().loadStats();
		}

		players.put(player.getUniqueId(), out);
		return out;
	}

	// This so it will only delete the playerinfo after the game ends
	public void deletePlayer(Player player) {
		players.remove(player.getUniqueId());
	}

	public PlayerInfo getPlayerInfo(Player player) {
		return players.get(player.getUniqueId());
	}

	public PlayerInfo getPlayerByUUID(String uuid) {
		return players.get(UUID.fromString(uuid));
	}

	public HashMap<UUID, PlayerInfo> getPlayers() {
		return players;
	}

	public void delete() {
		for (PlayerInfo pInfo : players.values()) {
			pInfo.delete();
		}

		players.clear();
		players = null;
	}
}
