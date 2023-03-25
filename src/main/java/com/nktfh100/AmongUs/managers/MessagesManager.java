package com.nktfh100.AmongUs.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.enums.StatInt;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.Task;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class MessagesManager {

	private HashMap<String, String> msgsGame = new HashMap<String, String>();
	private HashMap<String, String> scoreboard = new HashMap<String, String>();
	private HashMap<String, String> tasks = new HashMap<String, String>();
	private HashMap<String, String> sabotagesTitles = new HashMap<String, String>();
	private HashMap<String, ArrayList<String>> scoreboardLines = new HashMap<String, ArrayList<String>>();
	private ArrayList<String> signLines = new ArrayList<String>();
	private HashMap<String, ArrayList<String>> holograms = new HashMap<String, ArrayList<String>>();
	private ArrayList<String> estimatedTimes = new ArrayList<String>();
	private HashMap<GameState, String> gameStates = new HashMap<GameState, String>();

	public void loadAll() {
		File msgsConfigFIle = new File(Main.getPlugin().getDataFolder(), "messages.yml");
		if (!msgsConfigFIle.exists()) {
			try {
				Main.getPlugin().saveResource("messages.yml", false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		YamlConfiguration msgsConfig = YamlConfiguration.loadConfiguration(msgsConfigFIle);
		try {
			this.msgsGame = new HashMap<String, String>();
			this.scoreboard = new HashMap<String, String>();
			this.tasks = new HashMap<String, String>();
			this.scoreboardLines = new HashMap<String, ArrayList<String>>();
			this.signLines = new ArrayList<String>();
			this.holograms = new HashMap<String, ArrayList<String>>();
			this.estimatedTimes = new ArrayList<String>();
			this.sabotagesTitles = new HashMap<String, String>();

			ConfigurationSection gameMsgsSC = msgsConfig.getConfigurationSection("game");
			Set<String> gameMsgsKeys = gameMsgsSC.getKeys(false);
			for (String key : gameMsgsKeys) {
				this.msgsGame.put(key, ChatColor.translateAlternateColorCodes('&', gameMsgsSC.getString(key).replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}

			ConfigurationSection scoreboardSC = msgsConfig.getConfigurationSection("scoreboard");
			Set<String> scoreboardKeys = scoreboardSC.getKeys(false);
			for (String key : scoreboardKeys) {
				this.scoreboard.put(key, ChatColor.translateAlternateColorCodes('&', scoreboardSC.getString(key).replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}

			this.scoreboardLines.put("main-lobby", new ArrayList<String>());
			for (String line : scoreboardSC.getStringList("main-lobby-lines")) {
				this.scoreboardLines.get("main-lobby").add(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}

			this.scoreboardLines.put("waiting-lobby", new ArrayList<String>());
			for (String line : scoreboardSC.getStringList("waiting-lobby-lines")) {
				this.scoreboardLines.get("waiting-lobby").add(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}

			this.scoreboardLines.put("crewmate", new ArrayList<String>());
			for (String line : scoreboardSC.getStringList("crewmate-lines")) {
				this.scoreboardLines.get("crewmate").add(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}
			
			this.scoreboardLines.put("imposter", new ArrayList<String>());
			for (String line : scoreboardSC.getStringList("imposter-lines")) {
				this.scoreboardLines.get("imposter").add(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}

			this.scoreboardLines.put("dead-crewmate", new ArrayList<String>());
			for (String line : scoreboardSC.getStringList("dead-crewmate-lines")) {
				this.scoreboardLines.get("dead-crewmate").add(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}
			
			this.scoreboardLines.put("dead-imposter", new ArrayList<String>());
			for (String line : scoreboardSC.getStringList("dead-imposter-lines")) {
				this.scoreboardLines.get("dead-imposter").add(ChatColor.translateAlternateColorCodes('&', line.replaceAll("%prefix%", Main.getConfigManager().getPrefix())));
			}
			
			ConfigurationSection tasksSC = msgsConfig.getConfigurationSection("tasks");
			Set<String> tasksKeys = tasksSC.getKeys(false);
			for (String key : tasksKeys) {
				String name = tasksSC.getConfigurationSection(key).getString("name");
				this.tasks.put(key, ChatColor.translateAlternateColorCodes('&', name));
				String title = tasksSC.getConfigurationSection(key).getString("title"); // for sabotages
				if (title != null) {
					this.sabotagesTitles.put(key, title);
				}
			}

			for (String line : msgsConfig.getStringList("arenaSignsLines")) {
				this.signLines.add(ChatColor.translateAlternateColorCodes('&', line));
			}

			for (String line : msgsConfig.getStringList("estimatedTimes")) {
				this.estimatedTimes.add(ChatColor.translateAlternateColorCodes('&', line));
			}

			ConfigurationSection hologramsSC = msgsConfig.getConfigurationSection("holograms");
			Set<String> hologramsKeys = hologramsSC.getKeys(false);
			for (String key : hologramsKeys) {
				this.holograms.put(key, new ArrayList<String>());
				for (String line : hologramsSC.getStringList(key)) {
					this.holograms.get(key).add(ChatColor.translateAlternateColorCodes('&', line));
				}
			}

			ConfigurationSection gameStatesSC = msgsConfig.getConfigurationSection("gameStates");
			if (gameStatesSC != null) {
				Set<String> gameStatesKeys = gameStatesSC.getKeys(false);
				for (String key : gameStatesKeys) {
					GameState gm = GameState.valueOf(key);
					if (gm != null) {
						this.gameStates.put(gm, gameStatesSC.getString(key, gm.toString()));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().log(Level.SEVERE, "Something is wrong with your messages.yml file!");
			Main.getPlugin().getPluginLoader().disablePlugin(Main.getPlugin());
		}
	}

	private String replacePlaceholders(String line, HashMap<String, String> placeholders, Player player) {
		if (line == null) {
			return "";
		}

		if (placeholders != null) {
			for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
				line = line.replaceAll(placeholder.getKey(), placeholder.getValue());
			}
		}

		if (player != null && Main.getIsPlaceHolderAPI()) {
			line = PlaceholderAPI.setPlaceholders(player, line);
		}

		return line;
	}

	private String replaceArena(String output, Arena arena) {
		if (output == null) {
			return "";
		}
		if (arena != null) {
			output = output.replaceAll("%players%", arena.getPlayersInfo().size() + "");
			output = output.replaceAll("%arena%", arena.getName());
			output = output.replaceAll("%arena1%", arena.getDisplayName());
			output = output.replaceAll("%state%", Main.getMessagesManager().getGameState(arena.getGameState()));
			output = output.replaceAll("%maxplayers%", arena.getMaxPlayers() + "");
			output = output.replaceAll("%statecolor%", Utils.getStateColor(arena.getGameState()) + "");
		}
		return output;
	}

	public String getSignLine(int i, Arena arena) {
		String line = this.signLines.get(i);
		if (line == null) {
			return "";
		}
		if (i != 0 && arena != null) {
			line = replaceArena(line, arena);
		}
		return line;
	}

	public String getGameMsg(String key, Arena arena, HashMap<String, String> placeholders, Player player) {
		if (this.msgsGame == null) {
			return "";
		}
		String output = this.msgsGame.get(key);
		if (output == null) {
			Main.getPlugin().getLogger().warning("Game msg '" + key + "' is missing from your messages.yml file!");
			return "";
		}
		if (arena != null) {
			output = output.replaceAll("%arena%", arena.getDisplayName());
		}
		return replacePlaceholders(output, placeholders, player);
	}

	public String getTaskName(String task) {
		if (task == null) {
			return "";
		}
		String key = task.toLowerCase();
		if (this.tasks.get(key) == null) {
			return task.toLowerCase();
		}
		String name = this.tasks.get(key);
		if (name == null) {
			return task.toLowerCase();
		}
		return name;
	}

	public String getSabotageTitle(SabotageType st) {
		if (st == null) {
			return "";
		}
		String out = this.sabotagesTitles.get(st.toString().toLowerCase());
		if (out == null) {
			return "";
		}
		return out;
	}

	public String getScoreboard(String key) {
		String output = this.scoreboard.get(key);
		if (output == null) {
			return "";
		}
		return output;
	}

	public String getScoreboardTaskLine(Arena arena, TaskPlayer tp) {
		String output = this.scoreboard.get(tp.getIsDone() ? "taskDoneLine" : "taskLine");
		if (output == null) {
			return "";
		}
		if (arena != null) {
			output = output.replaceAll("%arena%", arena.getDisplayName());
		}
		if (tp != null) {
			Task task = tp.getActiveTask();
			output = output.replaceAll("%task%", getTaskName(task.getTaskType().toString()));
			output = output.replaceAll("%taskloc%", task.getLocationName().getName());
			output = output.replaceAll("%taskcolor%", tp.getColor() + "");
			if (tp.getTasks().size() > 1) {
				output = output.replaceAll("%state%", "(" + tp.getState() + "/" + tp.getTasks().size() + ")");
			} else {
				output = output.replaceAll("%state%", "");
			}
		}
		return output;
	}

	public String getScoreboardLine(String team, int i, PlayerInfo pInfo) {
		String line = this.scoreboardLines.get(team).get(i);
		line = line.replaceAll("%emptyline%", Utils.getRandomColors());
		if (pInfo != null) {
			if (pInfo.getArena() != null) {
				line = line.replaceAll("%arena%", pInfo.getArena().getDisplayName());

				line = line.replaceAll("%player%", pInfo.getPlayer().getName());
				line = line.replaceAll("%playercolor%", pInfo.getColor().getChatColor() + "");
				line = line.replaceAll("%playercolorname%", pInfo.getColor().toString().toLowerCase() + "");
				line = line.replaceAll("%coins%", pInfo.getStatsManager().getCoins() + "");
				if (pInfo.getArena().getGameState() == GameState.RUNNING || pInfo.getArena().getGameState() == GameState.FINISHING) {
					line = line.replaceAll("%playerteam%", pInfo.getIsImposter() ? "imposter" : "crewmate");
				} else {
					line = line.replaceAll("%minplayers%", pInfo.getArena().getMinPlayers() + "");
					line = line.replaceAll("%maxplayers%", pInfo.getArena().getMaxPlayers() + "");
					line = line.replaceAll("%players%", pInfo.getArena().getPlayers().size() + "");
					line = line.replaceAll("%gamestate%", this.getGameState(pInfo.getArena().getGameState()));
					if (pInfo.getArena().getGameState() == GameState.WAITING) {
						line = line.replaceAll("%gamestarttime%", pInfo.getArena().getGameTimer() + "");
					} else {
						line = line.replaceAll("%gamestarttime%", pInfo.getArena().getGameTimerActive() + "");
					}
				}
			} else if (Main.getConfigManager().getEnableLobbyScoreboard()) { // Main lobby scoreboard
				for (StatInt statIntE : StatInt.values()) {
					Integer stat_ = pInfo.getStatsManager().getStatInt(statIntE);
					if (stat_ == null) {
						stat_ = 0;
					}
					line = line.replaceAll("%" + statIntE.getName() + "%", stat_ + "");
				}
				line = line.replaceAll("%coins%", pInfo.getStatsManager().getCoins() + "");
			}
		}
		if (Main.getIsPlaceHolderAPI()) {
			line = PlaceholderAPI.setPlaceholders(pInfo.getPlayer(), line);
		}
		return line;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getScoreBoardLines(String key) {
		ArrayList<String> out = this.scoreboardLines.get(key);
		return (ArrayList<String>) out.clone();
	}

	public ArrayList<String> getHologramLines(String key, HashMap<String, String> placeholders) {
		ArrayList<String> lines = this.holograms.get(key);
		ArrayList<String> out = new ArrayList<String>();
		if (lines == null) {
			Main.getPlugin().getLogger().warning("Hologram '" + key + "' is missing from your messages.yml file!");
			return out;
		}
		for (String line : lines) {
			out.add(this.replacePlaceholders(line, placeholders, null));
		}
		return out;
	}

	public ArrayList<String> getEstimatedTimes() {
		return this.estimatedTimes;
	}

	public String getGameState(GameState gm) {
		if (this.gameStates == null || this.gameStates.get(gm) == null) {
			return gm.toString();
		}
		return this.gameStates.get(gm);
	}

	public void delete() {
		this.msgsGame = null;
		this.scoreboard = null;
		this.tasks = null;
		this.sabotagesTitles = null;
		this.scoreboardLines = null;
		this.signLines = null;
		this.holograms = null;
		this.estimatedTimes = null;
		this.gameStates = null;
	}

}
