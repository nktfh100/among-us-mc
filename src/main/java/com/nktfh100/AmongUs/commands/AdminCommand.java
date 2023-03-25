package com.nktfh100.AmongUs.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.nktfh100.AmongUs.enums.GameEndReasons;
import com.nktfh100.AmongUs.enums.GameEndWinners;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.LocationName;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.inventory.ArenaSetupGui;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class AdminCommand implements CommandExecutor {

	public final static ArrayList<String> settings = new ArrayList<String>(Arrays.asList("minplayers", "maxplayers", "gametimer", "votingtime", "discussiontime", "imposters", "commontasks",
			"longtasks", "shorttasks", "meetingsperplayer", "killcooldown", "meetingcooldown", "sabotagecooldown", "reportdistance", "impostervision", "crewmatevision"));

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0 && (sender.hasPermission("amongus.admin") || sender.hasPermission("amongus.admin.setup"))) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V" + Main.getPlugin().getDescription().getVersion());
			sender.sendMessage("         " + ChatColor.GOLD + "" + ChatColor.BOLD + "by nktfh100");
			sender.sendMessage(ChatColor.YELLOW + "/aua reload" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Reload the config");
			sender.sendMessage(ChatColor.YELLOW + "/aua listarenas" + ChatColor.WHITE + " - " + ChatColor.GOLD + "List all created arenas");
			sender.sendMessage(ChatColor.YELLOW + "/aua createarena <Arena Name> <Min players> <Max players> <Imposters>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Create a new arena");
			sender.sendMessage(ChatColor.YELLOW + "/aua setup" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Open arena setup menu");
			sender.sendMessage(ChatColor.YELLOW + "/aua addlocation <Arena name> <Location Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Add location name to arena");
			sender.sendMessage(ChatColor.YELLOW + "/aua setsetting <Arena name> <Setting To Change> <Target Value>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Change various arena settings");
			sender.sendMessage(ChatColor.YELLOW + "/aua setmainlobby" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Set the main lobby location");
			sender.sendMessage(ChatColor.YELLOW + "/aua start <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Start game");
			sender.sendMessage(ChatColor.YELLOW + "/aua endgame <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "End game");
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
		} else if (args.length == 0 && (sender.hasPermission("amongus.admin.startgame") && !sender.hasPermission("amongus.admin.endgame"))) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V" + Main.getPlugin().getDescription().getVersion());
			sender.sendMessage("         " + ChatColor.GOLD + "" + ChatColor.BOLD + "by nktfh100");
			sender.sendMessage(ChatColor.YELLOW + "/aua start <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Start game");
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
		} else if (args.length == 0 && (sender.hasPermission("amongus.admin.endgame") && !sender.hasPermission("amongus.admin.startgame"))) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V" + Main.getPlugin().getDescription().getVersion());
			sender.sendMessage("         " + ChatColor.GOLD + "" + ChatColor.BOLD + "by nktfh100");
			sender.sendMessage(ChatColor.YELLOW + "/aua endgame <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "End game");
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
		} else if (args.length == 0 && (sender.hasPermission("amongus.admin.endgame") && sender.hasPermission("amongus.admin.startgame"))) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V" + Main.getPlugin().getDescription().getVersion());
			sender.sendMessage("         " + ChatColor.GOLD + "" + ChatColor.BOLD + "by nktfh100");
			sender.sendMessage(ChatColor.YELLOW + "/aua start <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Start game");
			sender.sendMessage(ChatColor.YELLOW + "/aua endgame <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "End game");
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
		} else if (args.length != 0) {

			/* ---------------------------------------------------- */

			if (args[0].equalsIgnoreCase("reload") && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
				Main.reloadConfigs();
				sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Successfully loaded the config");
			}

			/* ---------------------------------------------------- */

			else if (args[0].equalsIgnoreCase("setup") && sender instanceof Player && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
				if (args.length == 1) {
					ArenaSetupGui.openArenaSetupSelector((Player) sender);
				} else {
					Player player = (Player) sender;
					Arena arena = Main.getArenaManager().getArenaByName(args[1]);
					if (arena != null) {
						if (args.length == 2) {
							ArenaSetupGui.openArenaEditor(player, arena);
						} else if (args.length == 3) {
							switch (args[2].toLowerCase()) {
							case "tasks":
								ArenaSetupGui.openTasksSelectLocation(player, arena, 1);
								return true;
							case "sabotages":
								ArenaSetupGui.openSabotageSelector(player, arena);
								return true;
							case "spawns":
								ArenaSetupGui.openSpawnsEditor(player, arena);
								return true;
							case "locations":
								ArenaSetupGui.openLocationSelector(player, arena, 1);
								return true;
							case "vents":
								ArenaSetupGui.openVentsGroupsSelector(player, arena);
								return true;
							case "cameras":
								ArenaSetupGui.openCamerasEdit(player, arena);
								return true;
							case "doors":
								ArenaSetupGui.openDoorGroupSelector(player, arena);
								return true;
							default:
								ArenaSetupGui.openArenaEditor(player, arena);
								return true;
							}
						}
					} else {
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena doesn't exist!");
					}
				}
			}

			/* ---------------------------------------------------- */

			else if (args[0].equalsIgnoreCase("addlocation") && sender instanceof Player && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
				if (args.length <= 2 || args[1] == null || args[2] == null) {
					sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "/aua addlocation <Arena Name> <Location Name>");
				} else {
					if (Main.getArenaManager().getArenaByName(args[1]) != null) {
						Arena arena = Main.getArenaManager().getArenaByName(args[1]);
						Player player = (Player) sender;
						Location loc = player.getLocation();
						String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
						ConfigurationSection locationsSC = arena.getArenaConfig().getConfigurationSection("locations");

						final String locID = Utils.getRandomString(4);
						ConfigurationSection locSC = locationsSC.createSection(locID);

						locSC.set("name", args[2]);
						locSC.set("location", locStr);

						arena.getLocations().put(locID, new LocationName(locID, args[2], loc));

						arena.saveConfig();

						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Added location " + args[2] + " successfully!");
					} else {
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena doesn't exist!");
					}
				}
			}

			/* ---------------------------------------------------- */

			else if (args[0].equalsIgnoreCase("setsetting") && sender instanceof Player && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
				if (args.length == 4) {
					if (Main.getArenaManager().getArenaByName(args[1]) != null) {
						Arena arena = Main.getArenaManager().getArenaByName(args[1]);

						args[2] = args[2].toLowerCase();
						String settingSelected = null;
						for (String ss : settings) {
							if (ss.equals(args[2])) {
								settingSelected = ss;
								break;
							}
						}
						if (settingSelected == null) {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Invalid setting type!");
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + settings.toString());
							return false;
						}
						ConfigurationSection arenaSC = arena.getArenaConfig();
						Double value;
						Integer value1;
						try {
							value = Double.valueOf(args[3]);
							value1 = Integer.valueOf(args[3]);
						} catch (Exception e) {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Target value must be a valid number!");
							return false;
						}

						switch (args[2]) {
						case "minplayers":
							arena.setMinPlayers(value1);
							arenaSC.set(args[2], value1);
							break;
						case "maxplayers":
							arena.setMaxPlayers(value1);
							arenaSC.set(args[2], value1);
							break;
						case "gametimer":
							arena.setGameTimer(value1);
							arenaSC.set(args[2], value1);
							break;
						case "votingtime":
							arena.setVotingTime(value1);
							arenaSC.set(args[2], value1);
							break;
						case "discussiontime":
							arena.setDiscussionTime(value1);
							arenaSC.set(args[2], value1);
							break;
						case "imposters":
							arena.setNumImposters(value1);
							arenaSC.set(args[2], value1);
							break;
						case "longtasks":
							arena.setLongTasks(value1);
							arenaSC.set(args[2], value1);
							break;
						case "shorttasks":
							arena.setShortTasks(value1);
							arenaSC.set(args[2], value1);
							break;
						case "commontasks":
							arena.setCommonTasks(value1);
							arenaSC.set(args[2], value1);
							break;
						case "meetingsperplayer":
							arena.setMeetingsPerPlayer(value1);
							arenaSC.set(args[2], value1);
							break;
						case "killcooldown":
							arena.setKillCooldown(value1);
							arenaSC.set(args[2], value1);
							break;
						case "meetingcooldown":
							arena.setMeetingCooldown(value1);
							arenaSC.set(args[2], value1);
							break;
						case "sabotagecooldown":
							arena.setSabotageCooldown(value1);
							arenaSC.set(args[2], value1);
							break;
						case "reportdistance":
							arena.setReportDistance(value);
							arenaSC.set(args[2], value);
							break;
						case "impostervision":
							arena.setImposterVision(value1);
							arenaSC.set(args[2], value1);
							break;
						case "crewmatevision":
							arena.setCrewmateVision(value1);
							arenaSC.set(args[2], value1);
							break;
						default:
							break;
						}

						arena.saveConfig();
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Successfully changed " + args[2] + " to " + args[3]);
					} else {
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena doesn't exist!");
					}
				} else {
					sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "/aua setsetting <Arena name> <Setting To Change> <Target Value>");
				}
			}

			/* ---------------------------------------------------- */

			else if (args[0].equalsIgnoreCase("start") && (sender.hasPermission("amongus.admin.startgame") || sender.hasPermission("amongus.admin"))) {
				if (args.length == 2) {
					if (Main.getArenaManager().getArenaByName(args[1]) != null) {
						Arena arena = Main.getArenaManager().getArenaByName(args[1]);
						if (arena.getPlayersInfo().size() >= arena.getMinPlayers()) {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Starting game!");
							arena.startGame();
						} else {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Not enough players!");
						}
					} else {
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena doesn't exist!");
					}
				} else {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
						if (pInfo.getIsIngame()) {
							if (pInfo.getArena().getGameState() == GameState.WAITING || pInfo.getArena().getGameState() == GameState.STARTING) {
								Main.getPlayersManager().getPlayerInfo(player).getArena().startGame();
								sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Starting game!");
							} else {
								sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena already in-game!");
							}
						} else {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Either be in-game or specify arena!");
						}
					}
				}
			}

			/* ---------------------------------------------------- */

			else if (args[0].equalsIgnoreCase("endgame") && (sender.hasPermission("amongus.admin.endgame") || sender.hasPermission("amongus.admin"))) {
				if (args.length == 2) {
					if (Main.getArenaManager().getArenaByName(args[1]) != null) {
						Main.getArenaManager().getArenaByName(args[1]).endGame(true, GameEndReasons.COMMAND, GameEndWinners.NOBODY);
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Ending game!");
					} else {
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena doesn't exist!");
					}
				} else {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
						if (pInfo.getIsIngame()) {
							if (pInfo.getArena().getGameState() != GameState.WAITING) {
								Main.getPlayersManager().getPlayerInfo(player).getArena().endGame(true, GameEndReasons.COMMAND, GameEndWinners.NOBODY);
								sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Ending game!");
							} else {
								sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena is not playing!");
							}
						} else {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Either be in-game or specify arena!");
						}
					}
				}
			}

			/* ---------------------------------------------------- */

			else if (args[0].equalsIgnoreCase("createarena") && sender instanceof Player && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
				if (args.length == 5) {
					if (Main.getArenaManager().getArenaByName(args[1]) == null) {
						Integer minPlayers = Integer.valueOf(args[2]);
						Integer maxPlayers = Integer.valueOf(args[3]);
						Integer imposters = Integer.valueOf(args[4]);
						if (maxPlayers > 16) {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Maximum players is 16!");
							return true;
						}
						if (imposters >= maxPlayers / 2) {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Too many imposters!");
							return true;
						}

						if (imposters <= 0) {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "There must be at least 1 imposter!");
							return true;
						}

						// Create the arena config file inside the arenas folder
						File arenasFolder = Main.getArenaManager().getArenasFolder();
						File file = new File(arenasFolder, args[1] + ".yml");
						try {
							file.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Something went wrong creating the arena file! (" + args[1] + ".yml)");
							return true;
						}
						YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

						config.set("displayname", args[1]);
						config.set("spawnpoints", new ArrayList<String>());
						config.set("minplayers", minPlayers);
						config.set("maxplayers", maxPlayers);
						Location loc = ((Player) sender).getLocation();
						config.set("mapcenter", loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
						config.set("meetingbtn", loc.getWorld().getName() + ",0,0,0");
						config.createSection("tasks");
						config.createSection("sabotages");
						config.createSection("locations");
						config.createSection("ventgroups");
						config.createSection("doorgroups");
						config.createSection("cameras");
						config.set("cameras.hologramloc", loc.getWorld().getName() + ",0,0,0");
						config.set("cameras.enable", false);
						config.createSection("cameras.cams");
						config.set("disablesprinting", false);
						config.set("disablejumping", false);
						config.set("disablemap", false);
						config.set("gametimer", 30);
						config.set("votingtime", 45);
						config.set("discussiontime", 30);
						config.set("proceedingtime", 5);
						config.set("enablevisualtasks", true);
						config.set("confirmejects", true);
						config.set("imposters", imposters);
						config.set("commontasks", 1);
						config.set("longtasks", 1);
						config.set("shorttasks", 2);
						config.set("mettingsperplayer", 1);
						config.set("killcooldown", 30);
						config.set("meetingcooldown", 10);
						config.set("sabotagecooldown", 25);
						config.set("reportdistance", 3.5D);
						config.set("enablereducedvision", true);
						config.set("hidehologramsoutofview", false);
						config.set("dynamicimposters", false);
						config.set("impostervision", 15);
						config.set("crewmatevision", 10);
						config.set("primeshieldsblocks", new ArrayList<String>());
						config.set("waitinglobby", loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
						config.set("movemapwithplayer", false);
						config.set("world", ((Player) sender).getWorld().getName());
						config.set("mapids", "");
						// Roles
						config.set("roles.scientist.chance", 0);
						config.set("roles.scientist.count", 0);
						config.set("roles.scientist.vitalsCooldown", 15);
						config.set("roles.scientist.batteryDuration", 5);
						config.set("roles.engineer.chance", 0);
						config.set("roles.engineer.count", 0);
						config.set("roles.engineer.ventCooldown", 30);
						config.set("roles.engineer.maxTimeInVents", 15);
						config.set("roles.angel.chance", 0);
						config.set("roles.angel.count", 0);
						config.set("roles.angel.cooldown", 60);
						config.set("roles.angel.duration", 10);
						config.set("roles.angel.protectVisibleToImposters", false);
						config.set("roles.shapeshifter.chance", 0);
						config.set("roles.shapeshifter.count", 0);
						config.set("roles.shapeshifter.duration", 30);
						config.set("roles.shapeshifter.cooldown", 10);
						config.set("roles.shapeshifter.leaveEvidence", false);
						
						try {
							config.save(file);
							
							Main.getArenaManager().loadArena(args[1], config, file);
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Created arena successfully!");
						} catch (Exception e) {
							e.printStackTrace();
							Bukkit.getLogger().warning("Something went wrong loading the newly created arena! (" + args[1] + ".yml)");
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Something went wrong loading the newly created arena! (" + args[1] + ".yml)");
							return true;
						}
					} else {
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Arena already exists!");
					}
				} else {
					sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "/aua createarena <Arena Name> <Min players> <Max players> <Imposters>");
				}
			}
			if (args.length == 1) {

				/* ---------------------------------------------------- */

				if (args[0].equalsIgnoreCase("listarenas") && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
					ArrayList<String> arenas = Main.getConfigManager().getBungeecord() ? Main.getBungeArenaManager().getAllArenasServerNames() : Main.getArenaManager().getAllArenasNames();
					if (arenas.size() == 0) {
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "No arenas found!");
					} else {
						sender.sendMessage(Main.getConfigManager().getPrefix() + StringUtils.join(arenas, ", "));
					}
				}

				/* ---------------------------------------------------- */

				else if (args[0].equalsIgnoreCase("setmainlobby") && sender instanceof Player && (sender.hasPermission("amongus.admin.setup") || sender.hasPermission("amongus.admin"))) {
					Main.getConfigManager().setMainLobby(((Player) sender).getLocation());
					FileConfiguration config = Main.getConfigManager().getConfig();
					sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Main lobby is set.");
					Location lobby_ = ((Player) sender).getLocation();
					config.set("mainLobby.world", lobby_.getWorld().getName());
					config.set("mainLobby.x", lobby_.getX());
					config.set("mainLobby.y", lobby_.getY());
					config.set("mainLobby.z", lobby_.getZ());
					config.set("mainLobby.yaw", lobby_.getYaw());
					config.set("mainLobby.pitch", lobby_.getPitch());
					Main.getPlugin().saveConfig();
				}
			}

		}
		return true;
	}

}
