package com.nktfh100.AmongUs.managers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.enums.CosmeticType;
import com.nktfh100.AmongUs.enums.StatInt;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;

public class StatsManager {

	private Player player;
	private PlayerInfo pInfo;
	private File statsFile;
	private YamlConfiguration statsConfig;

	private HashMap<StatInt, Integer> statsInt = new HashMap<StatInt, Integer>();

	private ArrayList<String> unlockedCosmetics = new ArrayList<String>();
	private HashMap<CosmeticType, String> selectedCosmetics = new HashMap<CosmeticType, String>();

	public StatsManager(PlayerInfo pInfo) {
		this.pInfo = pInfo;
		this.player = pInfo.getPlayer();
		if (!Main.getConfigManager().getMysql_enabled()) {
			this.statsFile = new File(Main.getPlugin().getDataFolder() + File.separator + "stats", player.getUniqueId().toString() + ".yml");
			if (!statsFile.exists()) {
				try {
					this.statsFile.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void loadStats() {
		StatsManager statsManager = this;
		if (Main.getConfigManager().getMysql_enabled()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (pInfo != null) {
						// load from mysql database
						Connection connection = Main.getConfigManager().getMysql_connection();
						try {
							PreparedStatement ps = connection.prepareStatement("SELECT * FROM stats WHERE UUID = ?");
							ps.setString(1, player.getUniqueId().toString());
							ResultSet rs = ps.executeQuery();
							rs.next();
							for (StatInt statIntE : StatInt.values()) {
								Integer stats_ = rs.getInt(statIntE.getName());
								statsManager.setStatInt(statIntE, stats_ == null ? 0 : stats_);
							}
							rs.close();
							ps.close();
							statsManager.getpInfo().updateScoreBoard();

							if (Main.getIsPlayerPoints()) {
								// load unlocked cosmetics
								PreparedStatement ps1 = connection.prepareStatement("SELECT * FROM unlocked_cosmetics WHERE UUID = ?");
								ps1.setString(1, player.getUniqueId().toString());
								ResultSet rs1 = ps1.executeQuery();
								statsManager.getUnlockedCosmetics().clear();
								while (rs1.next()) {
									statsManager.getUnlockedCosmetics().add(rs1.getString("cosmetic"));
								}
								rs1.close();
								ps1.close();

								// load selected cosmetics
								PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM selected_cosmetics WHERE UUID = ?");
								ps2.setString(1, player.getUniqueId().toString());
								ResultSet rs2 = ps2.executeQuery();
								while (rs2.next()) {
									statsManager.getSelectedCosmetics().put(CosmeticType.valueOf(rs2.getString("type").toUpperCase()), rs2.getString("selected"));
								}
								rs2.close();
								ps2.close();
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		} else {
			if (!this.statsFile.exists()) {
				try {
					this.statsFile.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.statsConfig = YamlConfiguration.loadConfiguration(this.statsFile);
			new BukkitRunnable() {
				@Override
				public void run() {
					if (statsManager.getpInfo() != null) {
						for (StatInt statIntE : StatInt.values()) {
							statsManager.getStatsInt().put(statIntE, statsManager.getStatsConfig().getInt(statIntE.getName(), 0));
						}

						// load unlocked cosmetics
						statsManager.getUnlockedCosmetics().clear();
						for (String cosmetic_ : statsManager.getStatsConfig().getStringList("unlocked_cosmetics")) {
							statsManager.getUnlockedCosmetics().add(cosmetic_);
						}

						// load selected cosmetics
						for (CosmeticType type : CosmeticType.values()) {
							String val_ = statsManager.getStatsConfig().getString(type.getName());
							if (val_ == null || val_.isEmpty()) {
								val_ = Main.getCosmeticsManager().getDefaultCosmetic(type);
							}
							statsManager.getSelectedCosmetics().put(type, val_);
						}

						statsManager.getpInfo().updateScoreBoard();
					}

				}
			}.runTaskAsynchronously(Main.getPlugin());
		}
	}

	public void saveStats(Boolean runAsync) {
		final File statsFile_ = this.statsFile;
		final YamlConfiguration statsConfig_ = this.statsConfig;
		final String uuid = this.player.getUniqueId().toString();
		final HashMap<StatInt, Integer> statsInt_ = new HashMap<StatInt, Integer>();
		for (StatInt statIntE : StatInt.values()) {
			statsInt_.put(statIntE, this.statsInt.get(statIntE));
		}
		final ArrayList<String> unlockedCosmetics_ = this.unlockedCosmetics;
		final HashMap<CosmeticType, String> selectedCosmetics_ = this.selectedCosmetics;
		if (Main.getConfigManager().getMysql_enabled()) {
			try {
				PreparedStatement ps = Main.getConfigManager().getMysql_connection().prepareStatement(
						"UPDATE stats SET games_played=?, imposter_wins=?, crewmate_wins=?, total_wins=?, imposter_kills=?, tasks_completed=?, emergencies_called=?, bodies_reported=?, times_murdered=?, times_ejected=?, time_played=? WHERE UUID=?");
				ps.setInt(1, statsInt_.get(StatInt.GAMES_PLAYED));
				ps.setInt(2, statsInt_.get(StatInt.IMPOSTER_WINS));
				ps.setInt(3, statsInt_.get(StatInt.CREWMATE_WINS));
				ps.setInt(4, statsInt_.get(StatInt.TOTAL_WINS));
				ps.setInt(5, statsInt_.get(StatInt.IMPOSTER_KILLS));
				ps.setInt(6, statsInt_.get(StatInt.TASKS_COMPLETED));
				ps.setInt(7, statsInt_.get(StatInt.EMERGENCIES_CALLED));
				ps.setInt(8, statsInt_.get(StatInt.BODIES_REPORTED));
				ps.setInt(9, statsInt_.get(StatInt.TIMES_MURDERED));
				ps.setInt(10, statsInt_.get(StatInt.TIMES_EJECTED));
				ps.setInt(11, statsInt_.get(StatInt.TIME_PLAYED));
				ps.setString(12, uuid);
				ps.execute();
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			BukkitRunnable func = new BukkitRunnable() {
				@Override
				public void run() {
					if (!statsFile_.exists()) {
						try {
							statsFile_.createNewFile();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					for (StatInt statIntE : statsInt_.keySet()) {
						statsConfig_.set(statIntE.getName(), statsInt_.get(statIntE));
					}

					statsConfig_.set("unlocked_cosmetics", unlockedCosmetics_);

					for (CosmeticType type : selectedCosmetics_.keySet()) {
						statsConfig_.set(type.getName(), selectedCosmetics_.get(type));
					}

					try {
						statsConfig_.save(statsFile_);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			if (runAsync) {
				func.runTaskAsynchronously(Main.getPlugin());
			} else {
				func.run();
			}
		}
	}

	public void mysql_registerPlayer(Boolean loadStats) {
		StatsManager statsManager = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					ConfigManager configManager = Main.getConfigManager();
					Connection connection = configManager.mysql_getConnection();

					Boolean status = configManager.mysql_checkConnection();
					if (!status) {
						System.out.println("Something is wrong with your MySQL server!");
						return;
					}

					// check if user already exists
					PreparedStatement ps = connection.prepareStatement("SELECT UUID FROM stats WHERE UUID = ?");
					ps.setString(1, player.getUniqueId().toString());
					ResultSet rs = ps.executeQuery();
					Boolean doesExists = rs.next();
					rs.close();
					ps.close();
					if (!doesExists) {
						String sql = "INSERT INTO stats(username, UUID) VALUES (?, ?)";
						PreparedStatement statement;
						statement = connection.prepareStatement(sql);
						statement.setString(1, player.getName());
						statement.setString(2, player.getUniqueId().toString());

						statement.execute();
						statement.close();
						
						if(Main.getIsPlayerPoints()) {
							try {
								// Add player to the selected cosmetics table
								String sql1 = "INSERT INTO selected_cosmetics(username, UUID, type, selected) VALUES (?, ?, ?, ?)";
								PreparedStatement statement1;
								statement1 = connection.prepareStatement(sql1);
								statement1.setString(1, player.getName());
								statement1.setString(2, player.getUniqueId().toString());
								statement1.setString(3, CosmeticType.KILL_SWORD.getName());
								statement1.setString(4, Main.getCosmeticsManager().getDefaultCosmetic(CosmeticType.KILL_SWORD));
								
								statement1.execute();
								statement1.close();
							} catch (Exception e) {
							}
						}

					}
					if (loadStats) {
						statsManager.loadStats();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(Main.getPlugin());
	}

	public void unlockCosmetic(CosmeticType type, String cosmetic) {
		this.unlockedCosmetics.add(cosmetic);
		if (Main.getConfigManager().getMysql_enabled()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						String sql1 = "INSERT INTO unlocked_cosmetics(username, UUID, cosmetic) VALUES (?, ?, ?)";
						PreparedStatement statement1;
						statement1 = Main.getConfigManager().getMysql_connection().prepareStatement(sql1);
						statement1.setString(1, player.getName());
						statement1.setString(2, player.getUniqueId().toString());
						statement1.setString(3, cosmetic);

						statement1.execute();
						statement1.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		} else {
			final File statsFile_ = this.statsFile;
			final YamlConfiguration statsConfig_ = this.statsConfig;
			final ArrayList<String> unlocked_ = this.unlockedCosmetics;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (!statsFile_.exists()) {
						try {
							statsFile_.createNewFile();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					statsConfig_.set("unlocked_cosmetics", unlocked_);

					try {
						statsConfig_.save(statsFile_);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		}
	}

	public void selectCosmetic(CosmeticType type, String selected) {
		this.selectedCosmetics.put(type, selected);
		if (Main.getConfigManager().getMysql_enabled()) {
			final String uuid_ = this.getPlayer().getUniqueId().toString();
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						PreparedStatement ps = Main.getConfigManager().getMysql_connection().prepareStatement("UPDATE selected_cosmetics SET selected=? WHERE UUID=? AND type=?");
						ps.setString(1, selected);
						ps.setString(2, uuid_);
						ps.setString(3, type.getName());
						ps.execute();
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		} else {
			final File statsFile_ = this.statsFile;
			final YamlConfiguration statsConfig_ = this.statsConfig;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (!statsFile_.exists()) {
						try {
							statsFile_.createNewFile();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					statsConfig_.set(type.getName(), selected);

					try {
						statsConfig_.save(statsFile_);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		}
	}

	public void plusOneStatInt(StatInt key) {
		this.statsInt.put(key, this.statsInt.get(key) + 1);
	}

	public void setStatInt(StatInt key, Integer value) {
		this.statsInt.put(key, value);
	}

	public Integer getStatInt(StatInt key) {
		return this.statsInt.get(key);
	}

	public Integer getCoins() {
		if (Main.getIsPlayerPoints()) {
			return Main.getPlayerPointsApi().look(player.getUniqueId());
		}
		return 0;
	}

	public void delete() {
		this.statsInt.clear();
	}

	public Player getPlayer() {
		return player;
	}

	public File getStatsFile() {
		return statsFile;
	}

	public YamlConfiguration getStatsConfig() {
		return statsConfig;
	}

	public HashMap<StatInt, Integer> getStatsInt() {
		return this.statsInt;
	}

	public PlayerInfo getpInfo() {
		return pInfo;
	}

	public String getSelectedCosmetic(CosmeticType key) {
		return selectedCosmetics.get(key);
	}

	public void setSelectedCosmetic(CosmeticType group, String value) {
		this.selectedCosmetics.put(group, value);
	}

	public ArrayList<String> getUnlockedCosmetics() {
		return unlockedCosmetics;
	}

	public HashMap<CosmeticType, String> getSelectedCosmetics() {
		return selectedCosmetics;
	}

}
