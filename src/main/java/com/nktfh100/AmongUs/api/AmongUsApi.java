package com.nktfh100.AmongUs.api;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.enums.CosmeticType;
import com.nktfh100.AmongUs.enums.StatInt;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class AmongUsApi {

	public static Arena getArena(String arena) {
		return Main.getArenaManager().getArenaByName(arena);
	}

	public static PlayerInfo getPlayerInfo(Player player) {
		return Main.getPlayersManager().getPlayerInfo(player);
	}

	/**
	 * You should always run this async!
	 * 
	 * @param player The player
	 * @return Returns copy of the player's stats
	 */
	public static HashMap<String, Integer> getPlayerStats(Player player) {
		if (player.isOnline()) {
			PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
			if (pInfo == null || pInfo.getStatsManager() == null) {
				return new HashMap<String, Integer>();
			}
			HashMap<String, Integer> out = new HashMap<String, Integer>();
			for (StatInt statIntE : StatInt.values()) {
				out.put(statIntE.getName(), pInfo.getStatsManager().getStatsInt().get(statIntE));
			}
			return out;
		} else {
			HashMap<String, Integer> out = new HashMap<String, Integer>();
			if (Main.getConfigManager().getMysql_enabled()) {
				Connection connection = Main.getConfigManager().getMysql_connection();
				try {
					PreparedStatement ps = connection.prepareStatement("SELECT * FROM stats WHERE UUID = ?");
					ps.setString(1, player.getUniqueId().toString());
					ResultSet rs = ps.executeQuery();
					rs.next();
					for (StatInt statIntE : StatInt.values()) {
						out.put(statIntE.getName(), rs.getInt(statIntE.getName()));
					}
					rs.close();
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return out;
			} else {
				File statsFile = new File(Main.getPlugin().getDataFolder() + File.separator + "stats", player.getUniqueId().toString() + ".yml");
				if (statsFile.exists()) {
					YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
					for (StatInt statIntE : StatInt.values()) {
						out.put(statIntE.getName(), statsConfig.getInt(statIntE.getName(), 0));
					}
				}
				return out;
			}
		}
	}

	/**
	 * You should always run this async!
	 * 
	 * @param player The player
	 * @return Returns copy of the player's unlocked cosmetics
	 */
	public static ArrayList<String> getUnlockedCosmetics(Player player) {
		if (player.isOnline()) {
			PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
			if (pInfo == null || pInfo.getStatsManager() == null) {
				return new ArrayList<String>();
			}
			ArrayList<String> out = new ArrayList<String>();
			for (String cosmetic_ : pInfo.getStatsManager().getUnlockedCosmetics()) {
				out.add(cosmetic_);
			}
			return out;
		} else {
			ArrayList<String> out = new ArrayList<String>();
			if (Main.getConfigManager().getMysql_enabled()) {
				Connection connection = Main.getConfigManager().getMysql_connection();
				try {
					PreparedStatement ps = connection.prepareStatement("SELECT * FROM unlocked_cosmetics WHERE UUID = ?");
					ps.setString(1, player.getUniqueId().toString());
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						out.add(rs.getString("cosmetic"));
					}
					rs.close();
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				File statsFile = new File(Main.getPlugin().getDataFolder() + File.separator + "stats", player.getUniqueId().toString() + ".yml");
				if (statsFile.exists()) {
					YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
					for (String cosmetic_ : statsConfig.getStringList("unlocked_cosmetics")) {
						out.add(cosmetic_);
					}
				}
			}
			return out;
		}
	}

	/**
	 * You should always run this async!
	 * 
	 * @param player The player
	 * @param type   Cosmetic type
	 * @return Returns selected cosmetic for the player
	 */
	public static String getSelectedCosmetic(Player player, CosmeticType type) {
		if (player.isOnline()) {
			PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
			if (pInfo == null || pInfo.getStatsManager() == null) {
				return Main.getCosmeticsManager().getDefaultCosmetic(type);
			}
			return pInfo.getStatsManager().getSelectedCosmetic(type);
		} else {
			if (Main.getConfigManager().getMysql_enabled()) {
				Connection connection = Main.getConfigManager().getMysql_connection();
				try {
					PreparedStatement ps = connection.prepareStatement("SELECT * FROM selected_cosmetics WHERE UUID=? AND type=?");
					ps.setString(1, player.getUniqueId().toString());
					ps.setString(2, type.getName());
					ResultSet rs = ps.executeQuery();
					String out = Main.getCosmeticsManager().getDefaultCosmetic(type);
					if (rs.next()) {
						out = rs.getString("selected");
					}
					rs.close();
					ps.close();
					return out;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				File statsFile = new File(Main.getPlugin().getDataFolder() + File.separator + "stats", player.getUniqueId().toString() + ".yml");
				if (statsFile.exists()) {
					YamlConfiguration statsConfig = YamlConfiguration.loadConfiguration(statsFile);
					return statsConfig.getString(type.getName(), Main.getCosmeticsManager().getDefaultCosmetic(type));
				}
			}
		}
		return Main.getCosmeticsManager().getDefaultCosmetic(type);

	}
}
