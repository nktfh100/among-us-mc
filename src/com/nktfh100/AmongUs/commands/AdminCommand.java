package com.nktfh100.AmongUs.commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.inventory.ArenaSetupGui;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class AdminCommand implements CommandExecutor {

	public final static ArrayList<String> settings = new ArrayList<String>(Arrays.asList("minplayers", "maxplayers", "gametimer", "votingtime", "discussiontime", "imposters", "commontasks", "longtasks", "shorttasks", "meetingsperplayer",
			"killcooldown", "meetingcooldown", "sabotagecooldown", "reportdistance", "impostervision", "crewmatevision"));

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0 && (sender.hasPermission("amongus.admin") || sender.hasPermission("amongus.admin.setup"))) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V" +  Main.getPlugin().getDescription().getVersion());
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
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V"  +  Main.getPlugin().getDescription().getVersion());
			sender.sendMessage("         " + ChatColor.GOLD + "" + ChatColor.BOLD + "by nktfh100");
			sender.sendMessage(ChatColor.YELLOW + "/aua start <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "Start game");
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
		} else if (args.length == 0 && (sender.hasPermission("amongus.admin.endgame") && !sender.hasPermission("amongus.admin.startgame"))) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V" +  Main.getPlugin().getDescription().getVersion());
			sender.sendMessage("         " + ChatColor.GOLD + "" + ChatColor.BOLD + "by nktfh100");
			sender.sendMessage(ChatColor.YELLOW + "/aua endgame <Arena Name>" + ChatColor.WHITE + " - " + ChatColor.GOLD + "End game");
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
		} else if (args.length == 0 && (sender.hasPermission("amongus.admin.endgame") && sender.hasPermission("amongus.admin.startgame"))) {
			sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "------------------------------------------");
			sender.sendMessage("       " + ChatColor.GOLD + "" + ChatColor.BOLD + "Among Us V"  +  Main.getPlugin().getDescription().getVersion());
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
						ConfigurationSection locationsSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".locations");

						ConfigurationSection locSC = locationsSC.createSection(Utils.getRandomString(4));

						locSC.set("name", args[2]);
						locSC.set("location", locStr);

						Main.getPlugin().saveConfig();
						Main.getArenaManager().loadArenas();

						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Added location successfully!");
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
						ConfigurationSection arenaSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName());
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

						Main.getPlugin().saveConfig();
//						Main.getArenaManager().loadArenas();
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
						Main.getArenaManager().getArenaByName(args[1]).endGame(true);
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
								Main.getPlayersManager().getPlayerInfo(player).getArena().endGame(true);
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
						if (imposters >= maxPlayers / 2 || imposters >= minPlayers) {
							sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.RED + "Too many imposters!");
							return true;
						}
						Main.getArenaManager().createArena(args[1]);
						FileConfiguration config = Main.getConfigManager().getConfig();
						String prefix = "arenas." + args[1];
						config.set(prefix + ".displayname", args[1]);
						config.set(prefix + ".spawnpoints", new ArrayList<String>());
						config.set(prefix + ".minplayers", minPlayers);
						config.set(prefix + ".maxplayers", maxPlayers);
						Location loc = ((Player) sender).getLocation();
						config.set(prefix + ".mapcenter", loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
						config.set(prefix + ".meetingbtn", loc.getWorld().getName() + ",0,0,0");
						config.createSection(prefix + ".tasks");
						config.createSection(prefix + ".sabotages");
						config.createSection(prefix + ".locations");
						config.createSection(prefix + ".ventgroups");
						config.createSection(prefix + ".doorgroups");
						config.createSection(prefix + ".cameras");
						config.set(prefix + ".cameras.hologramloc", loc.getWorld().getName() + ",0,0,0");
						config.set(prefix + ".cameras.enable", false);
						config.createSection(prefix + ".cameras.cams");
						config.set(prefix + ".disablesprinting", false);
						config.set(prefix + ".disablejumping", false);
						config.set(prefix + ".disablemap", false);
						config.set(prefix + ".gametimer", 30);
						config.set(prefix + ".votingtime", 45);
						config.set(prefix + ".discussiontime", 30);
						config.set(prefix + ".proceedingtime", 5);
						config.set(prefix + ".enablevisualtasks", true);
						config.set(prefix + ".confirmejects", true);
						config.set(prefix + ".imposters", imposters);
						config.set(prefix + ".commontasks", 1);
						config.set(prefix + ".longtasks", 1);
						config.set(prefix + ".shorttasks", 2);
						config.set(prefix + ".mettingsperplayer", 1);
						config.set(prefix + ".killcooldown", 30);
						config.set(prefix + ".meetingcooldown", 10);
						config.set(prefix + ".sabotagecooldown", 25);
						config.set(prefix + ".reportdistance", 3.5D);
						config.set(prefix + ".enablereducedvision", true);
						config.set(prefix + ".hidehologramsoutofview", false);
						config.set(prefix + ".dynamicimposters", false);
						config.set(prefix + ".impostervision", 15);
						config.set(prefix + ".crewmatevision", 10);
						config.set(prefix + ".primeshieldsblocks", new ArrayList<String>());
						config.set(prefix + ".waitinglobby", loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
						config.set(prefix + ".movemapwithplayer", false);
						config.set(prefix + ".world", ((Player) sender).getWorld().getName());
						config.set(prefix + ".mapids", "");

						Main.getPlugin().saveConfig();
						Main.getArenaManager().loadArenas();
						sender.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Created arena successfully!");
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
					config.set("mainLobby.x", (double) lobby_.getBlockX());
					config.set("mainLobby.y", (double) lobby_.getBlockY());
					config.set("mainLobby.z", (double) lobby_.getBlockZ());
					Main.getPlugin().saveConfig();
				}
			}

		}
		return true;
	}

}
