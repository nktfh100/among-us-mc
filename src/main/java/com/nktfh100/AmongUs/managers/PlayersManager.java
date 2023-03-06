package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.events.PacketContainer;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Packets;

public class PlayersManager implements Listener {

	private HashMap<String, PlayerInfo> players = new HashMap<>();

	public PlayersManager() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			players.put(player.getUniqueId().toString(), new PlayerInfo(player));
		}
		for (PlayerInfo pInfo : this.getPlayers()) {
			if (Main.getConfigManager().getMysql_enabled()) {
				pInfo.getStatsManager().mysql_registerPlayer(true);
			} else {
				pInfo.getStatsManager().loadStats();
			}
		}
	}

	public PlayerInfo _addPlayer(Player player) {
		PlayerInfo out = new PlayerInfo(player);
		if (Main.getConfigManager().getMysql_enabled()) {
			out.getStatsManager().mysql_registerPlayer(true);
		} else {
			out.getStatsManager().loadStats();
		}
		players.put(player.getUniqueId().toString(), out);
		return out;
	}

	public PlayerInfo getPlayerInfo(Player player) {
		PlayerInfo pInfo = players.get(player.getUniqueId().toString());
		if (pInfo == null) {
			pInfo = this._addPlayer(player);
		}
		return pInfo;
	}

	public PlayerInfo getPlayerByUUID(String uuid) {
		return this.players.get(uuid);
	}

	public List<PlayerInfo> getPlayers() {
		List<PlayerInfo> players_ = new ArrayList<PlayerInfo>(this.players.values());
		return players_;
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent ev) {
		if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby() && Main.getArenaManager().getAllArenas().size() > 0) {
			Arena arena = Main.getArenaManager().getAllArenas().iterator().next();
			if (arena.getGameState() == GameState.RUNNING || arena.getGameState() == GameState.FINISHING) {
				if (ev.getPlayer().hasPermission("amongus.admin") || ev.getPlayer().hasPermission("amongus.admin.setup")) {
					ev.allow();
//					players.put(ev.getPlayer().getUniqueId().toString(), new PlayerInfo(ev.getPlayer()));
				} else {
					ev.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Arena running");
				}
			} else if (arena.getPlayers().size() == arena.getMaxPlayers()) {
				if (ev.getPlayer().hasPermission("amongus.admin") || ev.getPlayer().hasPermission("amongus.admin.setup")) {
					ev.allow();
//					players.put(ev.getPlayer().getUniqueId().toString(), new PlayerInfo(ev.getPlayer()));
				} else {
					ev.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Arena is full!");
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent ev) {
		Player player = ev.getPlayer();
		if (players.get(ev.getPlayer().getUniqueId().toString()) == null) {
			players.put(ev.getPlayer().getUniqueId().toString(), new PlayerInfo(ev.getPlayer()));
		} else {
			players.get(ev.getPlayer().getUniqueId().toString())._setPlayer(player);
		}
		if (Main.getConfigManager().getMysql_enabled()) {
			this.getPlayerInfo(player).getStatsManager().mysql_registerPlayer(true);
		} else {
			this.getPlayerInfo(player).getStatsManager().loadStats();
		}
		if (Main.getConfigManager().getGiveLobbyItems()) {
			player.getInventory().clear();

			ItemInfo arenasSelectorItem = Main.getItemsManager().getItem("arenasSelector").getItem();
			player.getInventory().setItem(Main.getConfigManager().getLobbyItemSlot("arenasSelector"), arenasSelectorItem.getItem());
			if (Main.getIsPlayerPoints()) {
				player.getInventory().setItem(Main.getConfigManager().getLobbyItemSlot("cosmeticsSelector"), Main.getItemsManager().getItem("cosmeticsSelector").getItem().getItem());
			}
		}
		if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby() && Main.getArenaManager().getAllArenas().size() > 0) {
			Arena arena = Main.getArenaManager().getAllArenas().iterator().next();
			if ((arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) && arena.getPlayersInfo().size() < arena.getMaxPlayers()) {
				arena.playerJoin(player);
				new BukkitRunnable() {
					@Override
					public void run() {
						Main.getArenaManager().sendBungeUpdate(arena);
					}
				}.runTaskLater(Main.getPlugin(), 10L);
			}
		}
		if (!Main.getConfigManager().getBungeecord() || (Main.getConfigManager().getBungeecord() && Main.getConfigManager().getBungeecordIsLobby())) {
			if (Main.getConfigManager().getTpToLobbyOnJoin()) {
				if (Main.getConfigManager().getMainLobby() != null) {
					player.teleport(Main.getConfigManager().getMainLobby());
				}
			}
			// update main lobby scoreboard
			if (Main.getConfigManager().getEnableLobbyScoreboard()) {
				for (PlayerInfo pInfo_ : this.getPlayers()) {
					if (!pInfo_.getIsIngame()) {
						pInfo_.updateScoreBoard();
					}
				}
				this.getPlayerInfo(player)._setMainLobbyScoreboard();
			}
			if (!Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby() && Main.getConfigManager().getHidePlayersOutSideArena()) {
				new BukkitRunnable() {
					@Override
					public void run() {
						PacketContainer packet = Packets.REMOVE_PLAYER(player.getUniqueId());
						for (Arena arena : Main.getArenaManager().getAllArenas()) {
							for (Player p : arena.getPlayers()) {
								Packets.sendPacket(p, packet);
								Packets.sendPacket(player, Packets.REMOVE_PLAYER(p.getUniqueId()));
							}
						}

					}
				}.runTaskLater(Main.getPlugin(), 5L);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		PlayerInfo pInfo = this.players.get(player.getUniqueId().toString());
		if (pInfo == null) {
			return;
		}
		if (pInfo.getIsIngame()) {
			pInfo.getArena().get_playersToDelete().add(player);
			pInfo.getArena().playerLeave(player, false, true, true);
		} else {
			players.remove(player.getUniqueId().toString());
		}
	}

	// This so it will only delete the playerinfo after the game ends
	public void deletePlayer(String UUID) {
		if (this.players.get(UUID) != null) {
			this.players.get(UUID).delete();
		}
		this.players.remove(UUID);
	}

	public void delete() {
		for (PlayerInfo pInfo : this.players.values()) {
			pInfo.delete();
		}
		players.clear();
		players = null;
	}
}
