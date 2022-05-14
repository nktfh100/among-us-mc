package com.nktfh100.AmongUs.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.nktfh100.AmongUs.enums.SabotageLength;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.enums.TaskType;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.Camera;
import com.nktfh100.AmongUs.info.Door;
import com.nktfh100.AmongUs.info.DoorGroup;
import com.nktfh100.AmongUs.info.FakeBlock;
import com.nktfh100.AmongUs.info.LocationName;
import com.nktfh100.AmongUs.info.QueuedTasksVariant;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.info.Task;
import com.nktfh100.AmongUs.info.Vent;
import com.nktfh100.AmongUs.info.VentGroup;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.CamerasManager;
import com.nktfh100.AmongUs.managers.DoorsManager;
import com.nktfh100.AmongUs.managers.VentsManager;
import com.nktfh100.AmongUs.utils.Packets;
import com.nktfh100.AmongUs.utils.Utils;

public class ArenaSetupGui {

	private final static Integer pageSize = 21;

	public static void openArenaSetupSelector(Player player) {
		if (player.hasPermission("amongus.admin.setup") || player.hasPermission("amongus.admin")) {
			CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select arena");
			Utils.addBorder(invHolder.getInventory(), 45);
			for (Arena arena : Main.getArenaManager().getAllArenas()) {
				ItemStack item = Utils.createItem(Material.WHITE_WOOL, ChatColor.GREEN + arena.getName(), 1);
				Icon icon = new Icon(item);
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openArenaEditor(player, arena);
					}
				});
				invHolder.addIcon(icon);
			}
			player.openInventory(invHolder.getInventory());
		}
	}

	public static void openArenaEditor(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit arena: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));

		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Arena settings", 1, ChatColor.YELLOW + "Minimum players: " + arena.getMinPlayers(),
						ChatColor.YELLOW + "Maximum players: " + arena.getMaxPlayers(), ChatColor.YELLOW + "Imposters: " + arena.getNumImposters(),
						ChatColor.YELLOW + "Game timer: " + arena.getGameTimer() + "s", ChatColor.YELLOW + "Discussion time: " + arena.getDiscussionTime() + "s",
						ChatColor.YELLOW + "Voting time: " + arena.getVotingTime() + "s", ChatColor.YELLOW + "Kill cooldown: " + arena.getKillCooldown() + "s",
						ChatColor.YELLOW + "Sabotage cooldown: " + arena.getSabotageCooldown() + "s", ChatColor.YELLOW + "Emergency meetings cooldown: " + arena.getMeetingCooldown() + "s",
						ChatColor.YELLOW + "Emergency meetings per person: " + arena.getMeetingsPerPlayer(), ChatColor.YELLOW + "Common tasks: " + arena.getCommonTasks(),
						ChatColor.YELLOW + "Long tasks: " + arena.getLongTasks(), ChatColor.YELLOW + "Short tasks: " + arena.getShortTasks(),
						ChatColor.YELLOW + "Report distance: " + arena.getReportDistance(), ChatColor.YELLOW + "Crewmates vision: " + arena.getCrewmateVision(),
						ChatColor.YELLOW + "Imposters vision: " + arena.getImposterVision()));


		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaSetupSelector(player);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.OAK_SIGN, ChatColor.GREEN + "Edit tasks", 1, ChatColor.YELLOW + "Tasks count: " + arena.getAllTasks().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openTasksSelectLocation(player, arena, 1);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE_TORCH, ChatColor.GREEN + "Edit sabotages"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openSabotageSelector(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Edit spawns", 1, ChatColor.YELLOW + "Spawns count: " + arena.getPlayerSpawns().size(), "",
				ChatColor.YELLOW + "Locations around the emergency meeting button", ChatColor.YELLOW + "Must be the same as max players!"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openSpawnsEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Edit locations", 1, ChatColor.YELLOW + "Locations count: " + arena.getLocations().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openLocationSelector(player, arena, 1);
			}
		});
		invHolder.addIcon(icon);

		Integer ventsCount = 0;
		for (VentGroup vg_ : arena.getVentsManager().getVentGroups()) {
			ventsCount += vg_.getVents().size();
		}

		icon = new Icon(Utils.createItem(Material.IRON_TRAPDOOR, ChatColor.GREEN + "Edit vents", 1, ChatColor.YELLOW + "Vent groups count: " + arena.getVentsManager().getVentGroups().size(),
				ChatColor.YELLOW + "Vents count: " + ventsCount));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentsGroupsSelector(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createSkull(Camera.camera, ChatColor.GREEN + "Edit cameras", 1, ChatColor.YELLOW + "Cameras count: " + arena.getCamerasManager().getCameras().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCamerasEdit(player, arena);
			}
		});
		invHolder.addIcon(icon);

		Integer doorsCount = 0;
		for (DoorGroup dg_ : arena.getDoorsManager().getDoorGroups()) {
			doorsCount += dg_.getDoors().size();
		}
		icon = new Icon(Utils.createItem(Material.OAK_DOOR, ChatColor.GREEN + "Edit doors", 1, ChatColor.YELLOW + "Door groups count: " + arena.getDoorsManager().getDoorGroups().size(),
				ChatColor.YELLOW + "Doors count: " + doorsCount));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorGroupSelector(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.MAP, ChatColor.GREEN + "Map center", 1, ChatColor.YELLOW + Utils.locationToStringB(arena.getMapCenter()),
				ChatColor.YELLOW + "Click to change to your current location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				Location loc = player.getLocation();
				String centerLoc = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("mapcenter", centerLoc);

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed map center to (" + centerLoc + ")");
				ArenaSetupGui.openArenaEditor(player, Main.getArenaManager().getArenaByName(arena.getName()));
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.GREEN + "Meeting button location", 1, ChatColor.YELLOW + Utils.locationToStringB(arena.getMeetingButton()),
				ChatColor.YELLOW + "Click to change to your current location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				Location loc = player.getLocation();
				String holoLoc = loc.getWorld().getName() + "," + (loc.getX()) + "," + (loc.getBlockY() + 1.25) + "," + (loc.getZ());

				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("meetingbtn", holoLoc);

				Main.getPlugin().saveConfig();
				arena.setMeetingButton(loc.add(0, 1.25, 0));
				arena.deleteHolograms();
				arena.createHolograms();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed meeting button location to (" + holoLoc + ")");
				ArenaSetupGui.openArenaEditor(player, Main.getArenaManager().getArenaByName(arena.getName()));

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Waiting lobby location", 1, ChatColor.YELLOW + Utils.locationToStringB(arena.getWaitingLobby()),
				ChatColor.YELLOW + "Click to change to your current location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Location loc = player.getLocation();
				String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();

				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("waitinglobby", locStr);

				arena.setWaitingLobby(loc);
				Main.getPlugin().saveConfig();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed waiting lobby location to (" + locStr + ")");
				ArenaSetupGui.openArenaEditor(player, Main.getArenaManager().getArenaByName(arena.getName()));
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getEnableVisualTasks() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Enable visual tasks: " + arena.getEnableVisualTasks(), 1,
				ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("enablevisualtasks", !arena.getEnableVisualTasks());

				Main.getPlugin().saveConfig();
				arena.setEnableVisualTasks(!arena.getEnableVisualTasks());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed enable visual tasks to " + arena.getEnableVisualTasks());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getDisableSprinting() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Disable sprinting: " + arena.getDisableSprinting(), 1,
				ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("disablesprinting", !arena.getDisableSprinting());

				Main.getPlugin().saveConfig();
				arena.setDisableSprinting(!arena.getDisableSprinting());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed disable sprinting to " + arena.getDisableSprinting());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getDisableJumping() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Disable jumping: " + arena.getDisableJumping(), 1,
				ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("disablejumping", !arena.getDisableJumping());

				Main.getPlugin().saveConfig();
				arena.setDisableJumping(!arena.getDisableJumping());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed disable jumping to " + arena.getDisableJumping());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(
				Utils.createItem(arena.getDisableMap() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Disable map: " + arena.getDisableMap(), 1, ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("disablemap", !arena.getDisableMap());

				Main.getPlugin().saveConfig();
				arena.setDisableMap(!arena.getDisableMap());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed disable map to " + arena.getDisableMap());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getHideHologramsOutOfView() ? Material.LIME_DYE : Material.RED_DYE,
				ChatColor.GREEN + "Hide holograms out of view range: " + arena.getHideHologramsOutOfView(), 1, ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("hidehologramsoutofview", !arena.getHideHologramsOutOfView());

				Main.getPlugin().saveConfig();
				arena.setHideHologramsOutOfView(!arena.getHideHologramsOutOfView());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed hide holograms out of view range to " + arena.getHideHologramsOutOfView());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getEnableReducedVision() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Enable reduced vision: " + arena.getEnableReducedVision(), 1,
				ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("enablereducedvision", !arena.getEnableReducedVision());

				arena.endGame(false);

				Main.getPlugin().saveConfig();
				arena.setEnableReducedVision(!arena.getEnableReducedVision());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed enable reduced vision to " + arena.getEnableReducedVision());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getConfirmEjects() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Confirm ejects: " + arena.getConfirmEjects(), 1,
				ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("confirmejects", !arena.getConfirmEjects());

				arena.endGame(false);

				Main.getPlugin().saveConfig();
				arena.setConfirmEjects(!arena.getConfirmEjects());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed confirm ejects to " + arena.getConfirmEjects());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getMoveMapWithPlayer() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Move map with player: " + arena.getMoveMapWithPlayer(), 1,
				ChatColor.YELLOW + "Recommended if your map is big", ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("movemapwithplayer", !arena.getMoveMapWithPlayer());

				Main.getPlugin().saveConfig();
				arena.setMoveMapWithPlayer(!arena.getMoveMapWithPlayer());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed move map with player to " + arena.getMoveMapWithPlayer());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(arena.getDynamicImposters() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Dynamic number of imposters: " + arena.getDynamicImposters(), 1,
				ChatColor.YELLOW + "If there are less than 7 players:", ChatColor.YELLOW + "There will be 1 imposter", ChatColor.YELLOW + "More than 7 players:",
				ChatColor.YELLOW + "There will be 2 imposters"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("dynamicimposters", !arena.getDynamicImposters());

				Main.getPlugin().saveConfig();
				arena.setDynamicImposters(!arena.getDynamicImposters());
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed dynamic imposters to " + arena.getDynamicImposters());
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.GOLDEN_HOE, ChatColor.GREEN + "Prime shields blocks wand", 1, ChatColor.YELLOW + "Use to remove/add blocks from the prime shields visual task",
				ChatColor.YELLOW + "Break blocks to add/remove"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.getInventory()
						.addItem(Utils.createItem(Material.GOLDEN_HOE, ChatColor.GOLD + "Prime Shields Blocks Wand: " + arena.getName(), 1, ChatColor.YELLOW + "Break blocks to add/remove"));
				player.closeInventory();
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.SLIME_BALL, ChatColor.GREEN + "Vitals location", 1, ChatColor.YELLOW + Utils.locationToStringB(arena.getVitalsLoc()),
				ChatColor.YELLOW + "Click to change to your current location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				Location loc = player.getLocation();
				String holoLoc = loc.getWorld().getName() + "," + (loc.getX()) + "," + (loc.getBlockY() + 1.25) + "," + (loc.getZ());

				Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName()).set("vitalsloc", holoLoc);

				Main.getPlugin().saveConfig();
				arena.setVitalsLoc(loc.add(0, 1.25, 0));
				arena.deleteHolograms();
				arena.createHolograms();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed vitals location to (" + holoLoc + ")");
				ArenaSetupGui.openArenaEditor(player, Main.getArenaManager().getArenaByName(arena.getName()));

			}
		});
		invHolder.addIcon(icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openSpawnsEditor(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit spawns: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.END_CRYSTAL, ChatColor.YELLOW + "Spawns", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back", 1));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		int i = 0;
		for (Location loc : arena.getPlayerSpawns()) {
			icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Spawn: " + (i + 1), 1, ChatColor.YELLOW + Utils.locationToStringB(loc), ChatColor.YELLOW + "Click to delete"));
			final int num = i;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					List<String> allSpawns = Main.getConfigManager().getConfig().getStringList("arenas." + arena.getName() + ".spawnpoints");
					arena.endGame(false);
					allSpawns.remove(num);
					arena.removePlayerSpawn(num);
					Main.getConfigManager().getConfig().set("arenas." + arena.getName() + ".spawnpoints", allSpawns);
					Main.getPlugin().saveConfig();
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully removed spawn point " + (num + 1));
					ArenaSetupGui.openSpawnsEditor(player, Main.getArenaManager().getArenaByName(arena.getName()));
				}
			});
			invHolder.addIcon(icon);
			i++;
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Add spawn", 1, ChatColor.YELLOW + "Click to add a spawn at your current location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.closeInventory();
				arena.endGame(false);
				Location loc = player.getLocation();
				String spawnLoc = loc.getWorld().getName() + "," + (loc.getBlockX() + 0.5) + "," + loc.getBlockY() + "," + (loc.getBlockZ() + 0.5) + "," + loc.getYaw() + "," + loc.getPitch();
				List<String> allSpawns = Main.getConfigManager().getConfig().getStringList("arenas." + arena.getName() + ".spawnpoints");
				allSpawns.add(spawnLoc);
				arena.addPlayerSpawn(new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch()));
				Main.getConfigManager().getConfig().set("arenas." + arena.getName() + ".spawnpoints", allSpawns);
				Main.getPlugin().saveConfig();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully added spawn point " + allSpawns.size());
			}
		});
		invHolder.setIcon(44, icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openTasksSelectLocation(Player player, Arena arena, Integer currentPage) {
		Integer totalItems = arena.getLocations().size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select location name (tasks) " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Tasks", 1));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Select location name", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openTasksSelectLocation(player, arena, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openTasksSelectLocation(player, arena, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + arena.getLocations().get(key).getName(), 1,
					ChatColor.YELLOW + "Tasks count: " + arena.getAllTasksLocationName(key).size(), ChatColor.YELLOW + key));

			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openTasksSelectorArena(player, arena, key, currentPage);
				}
			});
			invHolder.addIcon(icon);
		}
		player.openInventory(invHolder.getInventory());
	}

	public static void openTasksSelectorArena(Player player, Arena arena, String locationId, Integer currentPage) {
		LocationName locName = arena.getLocations().get(locationId);
		if (locName == null) {
			return;
		}
		ArrayList<Task> allTasks = new ArrayList<Task>(arena.getAllTasks());
		allTasks.removeIf(n -> (!n.getLocationName().getId().equals(locName.getId())));

		Integer totalItems = allTasks.size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select task: " + arena.getName() + " (" + locName.getName() + ")");
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Tasks", 1));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Location name: " + locName.getName(), 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openTasksSelectLocation(player, arena, 1);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openTasksSelectorArena(player, arena, locationId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openTasksSelectorArena(player, arena, locationId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

		for (int i = startIndex; i <= endIndex; i++) {
			Task task = allTasks.get(i);
			icon = new Icon(Utils.createItem(Material.OAK_SIGN, ChatColor.GREEN + task.getLocationName().getName() + ": " + task.getName(), 1, ChatColor.YELLOW + task.getLocationName().getName(),
					ChatColor.YELLOW + task.getTaskType().toString(), ChatColor.YELLOW + "ID: " + task.getId(), ChatColor.YELLOW + "Click to edit"));
			String tId = task.getId();
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openTaskEdit(player, arena, tId);
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Create task", 1, ChatColor.YELLOW + "In this location name"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCreateTaskChooseType(player, arena, locationId, 1);
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	public static void openTaskEdit(Player player, Arena arena, String taskId) {
		Task task = arena.getTask(taskId);
		if (task == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit task: " + task.getLocationName().getName() + " - " + task.getName());

		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Location name: " + task.getLocationName().getName(), 1, ChatColor.YELLOW + task.getLocationName().getId()));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Task: " + task.getLocationName().getName() + " - " + task.getName(), 1,
				ChatColor.YELLOW + task.getTaskType().toString(), ChatColor.YELLOW + "ID: " + task.getId()));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openTasksSelectorArena(player, arena, task.getLocationName().getId(), 1);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Change location", 1, ChatColor.YELLOW + Utils.locationToStringB(task.getLocation()),
				ChatColor.YELLOW + "Click to set to your current location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				Location loc = player.getLocation();
				String taskLoc = loc.getWorld().getName() + "," + (loc.getX()) + "," + (loc.getBlockY() + 1.85) + "," + (loc.getZ());
				ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");

				ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

				taskSC.set("location", taskLoc);

				task.setLocation(new Location(loc.getWorld(), loc.getX(), loc.getBlockY() + 1.85, loc.getZ()));
				arena.deleteHolograms();
				arena.createHolograms();
				Main.getPlugin().saveConfig();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed " + task.getLocationName().getName() + ": " + task.getName() + " location");
				ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Change location name", 1, ChatColor.YELLOW + task.getLocationName().getName()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openTaskChangeChooseLocation(player, arena, taskId, 1);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.ANVIL, ChatColor.GREEN + "Queued tasks", 1, ChatColor.YELLOW + "If task has multiple stages"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openQueuedTasksVariantsSelector(player, arena, taskId);
			}
		});
		invHolder.addIcon(icon);

		Material mat = Material.RED_DYE;
		if (task.getIsEnabled()) {
			mat = Material.LIME_DYE;
		}

		icon = new Icon(Utils.createItem(mat, ChatColor.GREEN + "Task enabled: " + task.getIsEnabled(), 1, ChatColor.YELLOW + "Enabled: Task can be given normally",
				ChatColor.YELLOW + "Disabled: task can only be given", ChatColor.YELLOW + "if its queued by another task"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
				ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

				taskSC.set("isenabled", !task.getIsEnabled());

				Main.getPlugin().saveConfig();

				task.setIsEnabled(!task.getIsEnabled());

				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed " + task.getLocationName().getName() + ": " + task.getName() + " is enabled to "
						+ !task.getIsEnabled());
				ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport", 1, ChatColor.YELLOW + Utils.locationToStringB(task.getLocation()),
				ChatColor.YELLOW + "Click to teleport task's location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.teleport(task.getLocation());
			}
		});
		invHolder.addIcon(icon);

		if (task.getTaskType() == TaskType.PRIME_SHIELDS || task.getTaskType() == TaskType.EMPTY_GARBAGE || task.getTaskType() == TaskType.SCAN || task.getTaskType() == TaskType.CLEAR_ASTEROIDS) {
			icon = new Icon(Utils.createItem(task.getEnableVisuals() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Visuals enabled: " + task.getEnableVisuals(), 1,
					ChatColor.YELLOW + "Show visuals for players", ChatColor.YELLOW + "Click to change"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

					taskSC.set("enablevisuals", !task.getEnableVisuals());

					Main.getPlugin().saveConfig();

					task.setEnableVisuals(!task.getEnableVisuals());

					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed " + task.getLocationName().getName() + ": " + task.getName()
							+ " enable visuals to " + !task.getEnableVisuals());
					ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);
				}
			});
			invHolder.addIcon(icon);
		}

		if (task.getTaskType() == TaskType.CLEAR_ASTEROIDS) {
			icon = new Icon(Utils.createItem(Material.OAK_SAPLING, ChatColor.GREEN + "Cannon-1 location", 1, ChatColor.YELLOW + Utils.locationToStringB(task.getCannon1()),
					ChatColor.YELLOW + "Click to change to your location"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

					Location loc = player.getEyeLocation();
					String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();

					taskSC.set("cannon1", locStr);

					Main.getPlugin().saveConfig();
					task.setCannon1(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed " + task.getLocationName().getName() + ": " + task.getName()
							+ " cannon-1 location to (" + locStr + ")");
					ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);
				}
			});
			invHolder.addIcon(icon);

			icon = new Icon(Utils.createItem(Material.OAK_SAPLING, ChatColor.GREEN + "Cannon-2 location", 1, ChatColor.YELLOW + Utils.locationToStringB(task.getCannon2()),
					ChatColor.YELLOW + "Click to change to your location"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

					Location loc = player.getEyeLocation();
					String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();

					taskSC.set("cannon2", locStr);
					task.setCannon2(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));

					Main.getPlugin().saveConfig();
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed " + task.getLocationName().getName() + ": " + task.getName()
							+ " cannon-2 location to (" + locStr + ")");
					ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);
				}
			});
			invHolder.addIcon(icon);
		} else if (task.getTaskType() == TaskType.RECORD_TEMPERATURE) {
			icon = new Icon(Utils.createItem(task.getIsHot() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Is hot: " + task.getIsHot(), 1, ChatColor.YELLOW + "Click to change"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

					task.setIsHot(!task.getIsHot());
					taskSC.set("hot", task.getIsHot());

					Main.getPlugin().saveConfig();
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed " + task.getLocationName().getName() + ": " + task.getName() + " is hot to ("
							+ task.getIsHot() + ")");
					ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete task", 1));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				Main.getConfigManager().getConfig().set("arenas." + arena.getName() + ".tasks." + taskId, null);

				ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
				for (String taskKey : tasksSC.getKeys(false)) {
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskKey);
					if (taskSC.getConfigurationSection("queuedtasks") != null) {
						ConfigurationSection variantsSC = taskSC.getConfigurationSection("queuedtasks");
						for (String variantKey : variantsSC.getKeys(false)) {
							List<String> tasks_ = variantsSC.getStringList(variantKey);
							if (tasks_.remove(taskId)) {
								variantsSC.set(variantKey, tasks_);
							}
						}
					}
				}

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully deleted task");
				player.closeInventory();

				ArenaSetupGui.openLocationSelector(player, Main.getArenaManager().getArenaByName(arena.getName()), 1);
			}
		});
		invHolder.setIcon(44, icon);

		player.openInventory(invHolder.getInventory());
	}

	// choose the type of task
	public static void openCreateTaskChooseType(Player player, Arena arena, String locationId, Integer currentPage) {
		LocationName locName = arena.getLocations().get(locationId);
		if (locName == null) {
			return;
		}

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Create task: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));

		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Create task", 1, ChatColor.YELLOW + "Create task at your current location"));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Choose task type"));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openTasksSelectorArena(player, arena, locationId, 1);
			}
		});
		invHolder.setIcon(36, icon);

		TaskType[] taskTypes = TaskType.values();
		Integer totalItems = taskTypes.length;
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateTaskChooseType(player, arena, locationId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateTaskChooseType(player, arena, locationId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

		for (int i = startIndex; i <= endIndex; i++) {
			TaskType tt = taskTypes[i];
			icon = new Icon(Utils.createItem(Material.OAK_SIGN, ChatColor.GREEN + tt.toString(), 1, ChatColor.YELLOW + Main.getMessagesManager().getTaskName(tt.toString())));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					Location loc = player.getLocation();
					String taskLoc = loc.getWorld().getName() + "," + (loc.getX()) + "," + (loc.getBlockY() + 1.85) + "," + (loc.getZ());
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");

					String taskId = Utils.getRandomString(4);
					ConfigurationSection taskSC = tasksSC.createSection(taskId);

					taskSC.set("type", tt.toString());
					taskSC.set("location", taskLoc);
					taskSC.set("locationid", locationId);
					taskSC.createSection("queuedtasks");
					taskSC.set("queuedtasks." + Utils.getRandomString(4), new ArrayList<String>());
					taskSC.set("isenabled", true);
					if (tt == TaskType.PRIME_SHIELDS || tt == TaskType.EMPTY_GARBAGE || tt == TaskType.SCAN || tt == TaskType.CLEAR_ASTEROIDS) {
						taskSC.set("enablevisuals", true);
					} else if (tt == TaskType.RECORD_TEMPERATURE) {
						taskSC.set("hot", false);
					}

					Main.getPlugin().saveConfig();
					Main.getArenaManager().loadArenas();

					player.closeInventory();
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully added task " + Main.getMessagesManager().getTaskName(tt.toString()) + " at " + taskLoc);
					ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);

				}
			});
			invHolder.addIcon(icon);
		}
		player.openInventory(invHolder.getInventory());
	}

	// change task's location name after created
	public static void openTaskChangeChooseLocation(Player player, Arena arena, String taskId, Integer currentPage) {
		Task task = arena.getTask(taskId);
		if (task == null) {
			return;
		}
		Integer totalItems = arena.getLocations().size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Change task location: " + task.getLocationName().getName() + " - " + task.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Task: " + task.getLocationName().getName() + ": " + task.getName(), 1,
				ChatColor.YELLOW + task.getLocationName().getName() + ": " + Main.getMessagesManager().getTaskName(task.getTaskType().toString())));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Change task location name", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openTaskEdit(player, arena, taskId);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openTaskChangeChooseLocation(player, arena, taskId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openTaskChangeChooseLocation(player, arena, taskId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					arena.endGame(false);
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

					taskSC.set("locationid", key);

					Main.getPlugin().saveConfig();
					task.setLocationName(arena.getLocations().get(key));
					player.sendMessage(
							Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed task " + task.getName() + " location name to " + arena.getLocations().get(key).getName());

					ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);

				}
			});
			invHolder.addIcon(icon);
		}
		player.openInventory(invHolder.getInventory());
	}

	public static void openLocationSelector(Player player, Arena arena, Integer currentPage) {
		Integer totalItems = arena.getLocations().size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select location: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Locations", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openLocationSelector(player, arena, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openLocationSelector(player, arena, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openLocationEdit(player, arena, key);
				}
			});
			invHolder.addIcon(icon);
		}

		player.openInventory(invHolder.getInventory());
	}

	public static void openLocationEdit(Player player, Arena arena, String locId) {
		LocationName locName = arena.getLocations().get(locId);
		if (locName == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit location: " + locName.getName());

		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Location: " + locName.getName()));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openLocationSelector(player, arena, 1);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete location", 1, ChatColor.RED + "Warning:", ChatColor.RED + "Before you delete this location",
				ChatColor.RED + "Make sure nothing is using this location.", ChatColor.RED + "Otherwise some unexpected bugs will occur."));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				final String name_ = locName.getName();
				Main.getConfigManager().getConfig().set("arenas." + arena.getName() + ".locations." + locId, null);
				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully deleted " + name_);
				player.closeInventory();
				ArenaSetupGui.openLocationSelector(player, Main.getArenaManager().getArenaByName(arena.getName()), 1);
			}
		});
		invHolder.addIcon(icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openQueuedTasksVariantsSelector(Player player, Arena arena, String taskId) {
		Task task = arena.getTask(taskId);
		if (task == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit queued tasks: " + task.getLocationName().getName() + ": " + task.getName());

		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Task: " + taskId, 1, ChatColor.YELLOW + task.getLocationName().getName() + ": " + task.getName()));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.ANVIL, ChatColor.YELLOW + "Select Queued tasks variant", 1,
				ChatColor.YELLOW + "When giving task a random variant of queued tasks", ChatColor.YELLOW + "Will be selected"));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openTaskEdit(player, arena, taskId);
			}
		});
		invHolder.setIcon(36, icon);

		for (QueuedTasksVariant qtv : task.getQueuedTasksVariants()) {
			icon = new Icon(Utils.createItem(Material.ANVIL, ChatColor.GREEN + "Queued Task Variant: " + qtv.getId(), 1, ChatColor.YELLOW + "Queued tasks count: " + qtv.getQueuedTasks().size()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openQueuedTasksEditor(player, arena, taskId, qtv.getId());
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Add queued tasks variant", 1, ChatColor.YELLOW + "When giving tasks a random queued tasks variant",
				ChatColor.YELLOW + "Will be selected", ChatColor.YELLOW + "(So you can have multiple tasks starting with the same task"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				String configId_ = Utils.getRandomString(4);
				ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
				ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

				taskSC.getConfigurationSection("queuedtasks").set(configId_, new ArrayList<String>());

				Main.getPlugin().saveConfig();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully add queued tasks variant to " + task.getName());
				Main.getArenaManager().loadArenas();
				ArenaSetupGui.openQueuedTasksVariantsSelector(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);

			}
		});
		invHolder.setIcon(44, icon);

		player.openInventory(invHolder.getInventory());
	}

	// Edit queued tasks of variant
	public static void openQueuedTasksEditor(Player player, Arena arena, String taskId, Integer variantId) {
		Task task = arena.getTask(taskId);
		if (task == null) {
			return;
		}

		QueuedTasksVariant qtv = task.getQueuedTasksVarient(variantId);
		if (qtv == null) {
			return;
		}

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit queued tasks: " + task.getLocationName().getName() + ": " + task.getName() + " variant: " + variantId);

		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Task: " + taskId, 1, ChatColor.YELLOW + task.getLocationName().getName() + ": " + task.getName()));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.ANVIL, ChatColor.YELLOW + "Queued tasks variant: " + qtv.getId(), 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openQueuedTasksVariantsSelector(player, arena, taskId);
			}
		});
		invHolder.setIcon(36, icon);

		int i = 0;
		for (Task task_ : qtv.getQueuedTasksTasks()) {
			icon = new Icon(Utils.createItem(Material.OAK_SIGN, ChatColor.GREEN + "Task: " + task_.getLocationName().getName() + ": " + task_.getName(), 1,
					ChatColor.YELLOW + task_.getTaskType().toString(), ChatColor.YELLOW + "Click to remove"));
			final int tId = i;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					arena.endGame(false);
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

					ArrayList<String> queuedTasks = (ArrayList<String>) taskSC.getConfigurationSection("queuedtasks").getStringList(qtv.getConfigId());
					queuedTasks.remove(tId);

					taskSC.set("queuedtasks." + qtv.getConfigId(), queuedTasks);
					qtv.setQueuedTasks(queuedTasks);

					Main.getPlugin().saveConfig();

					Task removedTask = arena.getTask(task_.getId());
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully removed " + removedTask.getName() + " from queued tasks variant " + qtv.getId());
					ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);
				}
			});
			invHolder.addIcon(icon);
			i++;
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Add queued task"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openAddQueuedTasksSelectLocation(player, arena, taskId, variantId, 1);
			}
		});
		invHolder.setIcon(44, icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete this variant"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
				ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

				taskSC.getConfigurationSection("queuedtasks").set(qtv.getConfigId(), null);

				Main.getPlugin().saveConfig();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully removed queued tasks variant " + qtv.getId() + " from " + task.getName());
				Main.getArenaManager().loadArenas();
				ArenaSetupGui.openTaskEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId);

			}
		});
		invHolder.setIcon(43, icon);

		player.openInventory(invHolder.getInventory());
	}

	// add queued task select location name
	public static void openAddQueuedTasksSelectLocation(Player player, Arena arena, String taskId, Integer variantId, Integer currentPage) {
		Task task = arena.getTask(taskId);
		if (task == null) {
			return;
		}

		QueuedTasksVariant qtv = task.getQueuedTasksVarient(variantId);
		if (qtv == null) {
			return;
		}

		Integer totalItems = arena.getLocations().size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select location name (add queued task) " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + task.getLocationName().getName() + ": " + task.getName(), 1, ChatColor.YELLOW + task.getTaskType().toString()));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.ANVIL, ChatColor.YELLOW + "Variant: " + qtv.getId(), 1));
		invHolder.getInventory().setItem(3, Utils.createItem(Material.ANVIL, ChatColor.YELLOW + "Add queued task", 1));
		invHolder.getInventory().setItem(4, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Select location name", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openQueuedTasksEditor(player, arena, taskId, variantId);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openAddQueuedTasksSelectLocation(player, arena, taskId, variantId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openAddQueuedTasksSelectLocation(player, arena, taskId, variantId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + arena.getLocations().get(key).getName(), 1,
					ChatColor.YELLOW + "Tasks count: " + arena.getAllTasksLocationName(key).size(), ChatColor.YELLOW + key));

			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openAddQueuedTaskSelector(player, arena, taskId, variantId, key, currentPage);
				}
			});
			invHolder.addIcon(icon);
		}
		player.openInventory(invHolder.getInventory());
	}

	public static void openAddQueuedTaskSelector(Player player, Arena arena, String taskId, Integer variantId, String locationId, Integer currentPage) {
		Task task = arena.getTask(taskId);
		if (task == null) {
			return;
		}

		QueuedTasksVariant qtv = task.getQueuedTasksVarient(variantId);
		if (qtv == null) {
			return;
		}

		LocationName locName = arena.getLocations().get(locationId);
		if (locName == null) {
			return;
		}
		ArrayList<Task> tasks = arena.getAllTasksSorted();
		tasks.removeIf(n -> (!n.getLocationName().getId().equals(locationId)));

		Integer totalItems = tasks.size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Add queued task: " + task.getLocationName().getName() + ": " + task.getName() + " variant " + variantId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + task.getLocationName().getName() + ":" + task.getName(), 1, ChatColor.YELLOW + task.getTaskType().toString()));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.ANVIL, ChatColor.YELLOW + "Variant: " + qtv.getId(), 1));
		invHolder.getInventory().setItem(3, Utils.createItem(Material.ANVIL, ChatColor.YELLOW + "Add queued task", 1));
		invHolder.getInventory().setItem(4, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + locName.getName(), 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openAddQueuedTasksSelectLocation(player, arena, taskId, variantId, 1);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openAddQueuedTaskSelector(player, arena, taskId, variantId, locationId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openAddQueuedTaskSelector(player, arena, taskId, variantId, locationId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

		for (int i = startIndex; i <= endIndex; i++) {
			Task task_ = tasks.get(i);
			icon = new Icon(Utils.createItem(Material.OAK_SIGN, ChatColor.GREEN + task_.getLocationName().getName() + ": " + task_.getName(), 1, ChatColor.YELLOW + task_.getTaskType().toString(),
					ChatColor.YELLOW + task_.getId(), ChatColor.YELLOW + "Click to add"));
			final String tId = task_.getId();
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					arena.endGame(false);
					ConfigurationSection tasksSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".tasks");
					ConfigurationSection taskSC = tasksSC.getConfigurationSection(taskId + "");

					ArrayList<String> queuedTasks = (ArrayList<String>) taskSC.getConfigurationSection("queuedtasks").getStringList(qtv.getConfigId());
					queuedTasks.add(tId);
					taskSC.set("queuedtasks." + qtv.getConfigId(), queuedTasks);
					qtv.setQueuedTasks(queuedTasks);
					Main.getPlugin().saveConfig();

					Task addedTask = arena.getTask(tId);
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully added " + Main.getMessagesManager().getTaskName(addedTask.getTaskType().toString())
							+ " to queued tasks variant " + qtv.getId());
					ArenaSetupGui.openQueuedTasksEditor(player, Main.getArenaManager().getArenaByName(arena.getName()), taskId, variantId);

				}
			});
			invHolder.addIcon(icon);
		}

		player.openInventory(invHolder.getInventory());
	}

	public static void openSabotageSelector(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select sabotage: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.REDSTONE_TORCH, ChatColor.YELLOW + "Sabotages", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		for (SabotageArena sa : arena.getSabotages()) {
			String name = Main.getMessagesManager().getTaskName(sa.getTask1().getSabotageType().toString());
			icon = new Icon(Utils.createItem(Material.REDSTONE_TORCH, ChatColor.GREEN + "Sabotage: " + sa.getTask1().getSabotageType().toString(), 1, ChatColor.YELLOW + name,
					ChatColor.YELLOW + "Click to edit"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openSabotageEdit(player, arena, sa.getTask1().getSabotageType().toString());

				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Add sabotage", 1));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openAddSabotage(player, arena);
			}
		});
		invHolder.setIcon(44, icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openAddSabotage(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Add sabotage: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		ArrayList<SabotageType> types = new ArrayList<SabotageType>();
		for (SabotageType saboType : SabotageType.values()) {
			Boolean isOk = true;
			for (SabotageArena arenaS : arena.getSabotages()) {
				if (arenaS.getTask1().getSabotageType() == saboType) {
					isOk = false;
					break;
				}
			}
			if (isOk) {
				types.add(saboType);
			}
		}

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.REDSTONE_TORCH, ChatColor.YELLOW + "Add sabotage", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openSabotageSelector(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		for (SabotageType saboType : types) {
			String name = Main.getMessagesManager().getTaskName(saboType.toString());
			icon = new Icon(Utils.createItem(Material.REDSTONE_TORCH, ChatColor.GREEN + "Sabotage: " + saboType.toString(), 1, ChatColor.YELLOW + name, ChatColor.YELLOW + "Click to add"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection sabotagesSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".sabotages");
					ConfigurationSection sabotageSC = sabotagesSC.createSection(saboType.toString());

					Integer timer = 0;
					if (saboType == SabotageType.REACTOR_MELTDOWN || saboType == SabotageType.OXYGEN) {
						timer = 45;
					}
					sabotageSC.set("timer", timer);

					String locStr = player.getWorld().getName() + "," + player.getLocation().getX() + "," + (player.getLocation().getBlockY() + 1.85) + "," + player.getLocation().getZ();

					sabotageSC.set("location1", locStr);
					if (saboType.getSabotageLength() != SabotageLength.SINGLE) {
						sabotageSC.set("location2", locStr);
					}

					Main.getPlugin().saveConfig();
					Main.getArenaManager().loadArenas();

					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully added sabotage " + Main.getMessagesManager().getTaskName(saboType.toString()) + " to "
							+ arena.getName());
					ArenaSetupGui.openSabotageSelector(player, Main.getArenaManager().getArenaByName(arena.getName()));

				}
			});
			invHolder.addIcon(icon);
		}

		player.openInventory(invHolder.getInventory());
	}

	public static void openSabotageEdit(Player player, Arena arena, String sabotageType) {
		SabotageArena sabo = arena.getSabotageArena(SabotageType.valueOf(sabotageType));
		if (sabo == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit sabotage: " + arena.getName() + " - " + sabotageType);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.REDSTONE_TORCH, ChatColor.YELLOW + "Sabotage: " + sabotageType, 1, ChatColor.YELLOW + Main.getMessagesManager().getTaskName(sabotageType)));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openSabotageSelector(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Location: 1", 1, ChatColor.YELLOW + Utils.locationToStringB(sabo.getTask1().getLocation()),
				ChatColor.YELLOW + "Sabotage task 1 location", ChatColor.YELLOW + "Click to change to your current location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				Integer locId = 1;

				ConfigurationSection sabotagesSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".sabotages");
				ConfigurationSection sabotageSC = sabotagesSC.getConfigurationSection(sabo.getType().toString());

				String locStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getBlockY() + 1.85) + "," + (player.getLocation().getZ());
				sabotageSC.set("location" + locId, locStr);

				sabo.getTask1().setInfo(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getBlockY() + 1.85, player.getLocation().getZ()), arena);

				Main.getPlugin().saveConfig();
				arena.deleteHolograms();
				arena.createHolograms();
				ArenaSetupGui.openSabotageEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), sabo.getType().toString());

			}
		});
		invHolder.addIcon(icon);

		if (sabo.getLength() != SabotageLength.SINGLE) {

			icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Location: 2", 1, ChatColor.YELLOW + Utils.locationToStringB(sabo.getTask2().getLocation()),
					ChatColor.YELLOW + "Sabotage task 2 location", ChatColor.YELLOW + "Click to change to your current location"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					arena.endGame(false);
					Integer locId = 2;

					ConfigurationSection sabotagesSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".sabotages");
					ConfigurationSection sabotageSC = sabotagesSC.getConfigurationSection(sabo.getType().toString());

					String locStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getBlockY() + 1.85) + "," + (player.getLocation().getZ());
					sabotageSC.set("location" + locId, locStr);

					Main.getPlugin().saveConfig();

					sabo.getTask2().setInfo(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getBlockY() + 1.85, player.getLocation().getZ()), arena);
					arena.deleteHolograms();
					arena.createHolograms();
					ArenaSetupGui.openSabotageEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), sabo.getType().toString());

				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete sabotage"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection sabotagesSC = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".sabotages");
				sabotagesSC.set(sabo.getType().toString(), null);

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openSabotageSelector(player, Main.getArenaManager().getArenaByName(arena.getName()));
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	// -------------- vents --------------

	public static void openVentsGroupsSelector(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select vent group: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_TRAPDOOR, ChatColor.YELLOW + "Vent groups", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		VentsManager manager = arena.getVentsManager();
		for (VentGroup vg : manager.getVentGroups()) {

			icon = new Icon(Utils.createItem(Material.OAK_TRAPDOOR, ChatColor.GREEN + "Vent group: " + vg.getId(), 1, ChatColor.YELLOW + "Vents: " + vg.getVents().size(),
					ChatColor.YELLOW + "Loop: " + vg.getLoop()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openVentGroupEdit(player, arena, vg.getId());
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Create vent group", 1));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection ventGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".ventgroups");

				ConfigurationSection createdSec = ventGroupsSec.createSection(Utils.getRandomString(4));
				createdSec.set("id", manager.getVentGroups().size());
				createdSec.set("loop", false);
				createdSec.createSection("vents");

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openVentsGroupsSelector(player, Main.getArenaManager().getArenaByName(arena.getName()));
			}
		});
		invHolder.setIcon(44, icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openVentGroupEdit(Player player, Arena arena, Integer ventGroupId) {
		VentGroup ventGroup = arena.getVentsManager().getVentGroup(ventGroupId);
		if (ventGroup == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit vent group: " + arena.getName() + " - " + ventGroupId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_TRAPDOOR, ChatColor.YELLOW + "Vent group: " + ventGroupId, 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentsGroupsSelector(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.IRON_TRAPDOOR, ChatColor.GREEN + "Vents", 1, ChatColor.YELLOW + "Vent count: " + ventGroup.getVents().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentGroupVentsEdit(player, arena, ventGroupId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(ventGroup.getLoop() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Loop: " + ventGroup.getLoop(), 1,
				ChatColor.YELLOW + "Are vents only one way or loopable", ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection ventGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".ventgroups");
				ConfigurationSection ventGroupSec = ventGroupsSec.getConfigurationSection(ventGroup.getConfigId());
				ventGroupSec.set("loop", !ventGroup.getLoop());

				Main.getPlugin().saveConfig();
				ventGroup.setLoop(!ventGroup.getLoop());

				ArenaSetupGui.openVentGroupEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), ventGroupId);

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete vent group"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection ventGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".ventgroups");
				ventGroupsSec.set(ventGroup.getConfigId(), null);

				int i = 0;
				for (String vgKey : ventGroupsSec.getKeys(false)) {
					ventGroupsSec.getConfigurationSection(vgKey).set("id", i);
					i++;
				}

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openVentsGroupsSelector(player, Main.getArenaManager().getArenaByName(arena.getName()));
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	public static void openVentGroupVentsEdit(Player player, Arena arena, Integer ventGroupId) {
		VentGroup ventGroup = arena.getVentsManager().getVentGroup(ventGroupId);
		if (ventGroup == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit vent group vents: " + arena.getName() + " - " + ventGroupId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_TRAPDOOR, ChatColor.YELLOW + "Vent group: " + ventGroupId, 1));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.IRON_TRAPDOOR, ChatColor.YELLOW + "Vents", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentGroupEdit(player, arena, ventGroupId);
			}
		});
		invHolder.setIcon(36, icon);

		for (Vent v : ventGroup.getVents()) {
			String name = "none";
			if (v.getLocName() != null) {
				name = v.getLocName().getName();
			}
			icon = new Icon(Utils.createItem(Material.IRON_TRAPDOOR, ChatColor.GREEN + "Vent: " + v.getId(), 1, ChatColor.YELLOW + "Location name: " + name,
					ChatColor.YELLOW + Utils.locationToStringB(v.getLoc()), ChatColor.YELLOW + "Click to edit"));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openVentEdit(player, arena, ventGroupId, v.getId());
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Create vent"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCreateVentChooseLocation(player, arena, ventGroupId, 1);
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	public static void openVentEdit(Player player, Arena arena, Integer ventGroupId, Integer ventId) {
		VentGroup ventGroup = arena.getVentsManager().getVentGroup(ventGroupId);
		if (ventGroup == null) {
			return;
		}
		Vent vent = ventGroup.getVent(ventId);
		if (vent == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit vent: " + arena.getName() + " - " + ventGroupId + ": " + ventId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_TRAPDOOR, ChatColor.YELLOW + "Vent group: " + ventGroupId, 1));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.IRON_TRAPDOOR, ChatColor.YELLOW + "Vent: " + ventId, 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentGroupVentsEdit(player, arena, ventGroupId);
			}
		});
		invHolder.setIcon(36, icon);

		LocationName locName = vent.getLocName();
		String name = "none";
		if (locName != null) {
			name = locName.getName();
		}
		icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Change location name", 1, ChatColor.YELLOW + name));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentChangeChooseLocation(player, arena, ventGroupId, ventId, 1);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Change location", 1, ChatColor.YELLOW + Utils.locationToStringB(vent.getLoc()),
				ChatColor.YELLOW + "Click to change to your location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				ConfigurationSection ventGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".ventgroups");
				ConfigurationSection ventsSec = ventGroupsSec.getConfigurationSection(ventGroup.getConfigId() + ".vents");
				ConfigurationSection ventSec = ventsSec.getConfigurationSection(vent.getConfigId());

				String locStr = player.getWorld().getName() + "," + (player.getLocation().getBlockX() + 0.5) + "," + (player.getLocation().getBlockY() + 1.85) + ","
						+ (player.getLocation().getBlockZ() + 0.5) + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();

				ventSec.set("location", locStr);

				Main.getPlugin().saveConfig();
				vent.setLoc(new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getBlockY() + 1.85, player.getLocation().getBlockZ() + 0.5,
						player.getLocation().getYaw(), player.getLocation().getPitch()));

				arena.deleteHolograms();
				arena.createHolograms();
				ArenaSetupGui.openVentEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), ventGroupId, ventId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport to location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.closeInventory();
				player.teleport(vent.getLoc());
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete vent"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection ventGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".ventgroups");
				ConfigurationSection ventGroupSec = ventGroupsSec.getConfigurationSection(ventGroup.getConfigId());
				ConfigurationSection ventsSec = ventGroupSec.getConfigurationSection("vents");

				ventsSec.set(vent.getConfigId(), null);

				int i = 0;
				for (String vKey : ventsSec.getKeys(false)) {
					ventsSec.getConfigurationSection(vKey).set("id", i);
					i++;
				}

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openVentGroupVentsEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), ventGroupId);
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	// create vent choose location
	public static void openCreateVentChooseLocation(Player player, Arena arena, Integer ventGroupId, Integer currentPage) {
		VentGroup ventGroup = arena.getVentsManager().getVentGroup(ventGroupId);
		if (ventGroup == null) {
			return;
		}

		Integer totalItems = arena.getLocations().size() + 1;
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Choose vent location name: " + arena.getName() + " - " + ventGroupId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_TRAPDOOR, ChatColor.YELLOW + "Vent group: " + ventGroupId, 1));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.IRON_TRAPDOOR, ChatColor.YELLOW + "Create vent", 1));
		invHolder.getInventory().setItem(3, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Choose vent location name", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentGroupVentsEdit(player, arena, ventGroupId);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateVentChooseLocation(player, arena, ventGroupId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateVentChooseLocation(player, arena, ventGroupId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());
		keys.add(0, "----");

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			final Boolean isNone = key.equals("----");
			if (isNone) {
				icon = new Icon(
						Utils.createItem(Material.BARRIER, ChatColor.GREEN + "No location name", 1, ChatColor.YELLOW + "If you dont want this vent", ChatColor.YELLOW + "to have a location name"));
			} else {
				icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			}

			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection ventGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".ventgroups");
					ConfigurationSection ventGroupSec = ventGroupsSec.getConfigurationSection(ventGroup.getConfigId());

					ConfigurationSection ventSec = ventGroupSec.getConfigurationSection("vents").createSection(Utils.getRandomString(4));

					String locStr = player.getWorld().getName() + "," + (player.getLocation().getBlockX() + 0.5) + "," + (player.getLocation().getBlockY() + 1.85) + ","
							+ (player.getLocation().getBlockZ() + 0.5) + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();
					ventSec.set("location", locStr);
					ventSec.set("id", ventGroup.getVents().size());
					ventSec.set("locationid", key);

					Main.getPlugin().saveConfig();
					Main.getArenaManager().loadArenas();

					player.closeInventory();
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully created vent in group: " + ventGroupId);
				}
			});
			invHolder.addIcon(icon);
		}

		player.openInventory(invHolder.getInventory());
	}

	// change vent's location name after created
	public static void openVentChangeChooseLocation(Player player, Arena arena, Integer ventGroupId, Integer ventId, Integer currentPage) {
		VentGroup ventGroup = arena.getVentsManager().getVentGroup(ventGroupId);
		if (ventGroup == null) {
			return;
		}
		Vent vent = ventGroup.getVent(ventId);
		if (vent == null) {
			return;
		}

		LocationName locName = vent.getLocName();
		String name = "none";
		if (locName != null) {
			name = locName.getName();
		}

		Integer totalItems = arena.getLocations().size() + 1;
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Change vent location name: " + name + " - " + vent.getId());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_TRAPDOOR, ChatColor.YELLOW + "Vent group: " + ventGroupId, 1));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.IRON_TRAPDOOR, ChatColor.YELLOW + "vent: " + ventId, 1));
		invHolder.getInventory().setItem(3, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Change vent location name", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openVentEdit(player, arena, ventGroupId, ventId);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openVentChangeChooseLocation(player, arena, ventGroupId, ventId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openVentChangeChooseLocation(player, arena, ventGroupId, ventId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());
		keys.add(0, "----");

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			final Boolean isNone = key.equals("----");
			if (isNone) {
				icon = new Icon(
						Utils.createItem(Material.BARRIER, ChatColor.GREEN + "No location name", 1, ChatColor.YELLOW + "If you dont want this vent", ChatColor.YELLOW + "to have a location name"));
			} else {
				icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			}
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection ventGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".ventgroups");
					ConfigurationSection ventGroupSec = ventGroupsSec.getConfigurationSection(ventGroup.getConfigId());

					ConfigurationSection ventSec = ventGroupSec.getConfigurationSection("vents").getConfigurationSection(vent.getConfigId());

					ventSec.set("locationid", key);

					Main.getPlugin().saveConfig();
					if (isNone) {
						vent.setLocName(null);
					} else {
						vent.setLocName(arena.getLocations().get(key));
					}
					player.closeInventory();

					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed vent " + vent.getId() + " location name to "
							+ (isNone ? "none" : arena.getLocations().get(key)));

				}
			});
			invHolder.addIcon(icon);
		}

		player.openInventory(invHolder.getInventory());
	}

	// -------------- cameras --------------

	public static void openCamerasEdit(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit cameras: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createSkull(Camera.camera, ChatColor.YELLOW + "Cameras", 1, ""));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(arena.getEnableCameras() ? Material.LIME_DYE : Material.RED_DYE, ChatColor.GREEN + "Enable cameras: " + arena.getEnableCameras()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection camerasTopSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras");

				camerasTopSec.set("enable", !arena.getEnableCameras());

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();
				ArenaSetupGui.openCamerasEdit(player, Main.getArenaManager().getArenaByName(arena.getName()));

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Cameras hologram location", 1, ChatColor.YELLOW + Utils.locationToStringB(arena.getCamerasLoc()),
				ChatColor.YELLOW + "Click to change to your location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection camerasTopSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras");

				String locStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getBlockY() + 1.85) + "," + (player.getLocation().getZ());
				camerasTopSec.set("hologramloc", locStr);

				arena.setCamerasLoc(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getBlockY() + 1.85, player.getLocation().getZ()));

				Main.getPlugin().saveConfig();
				arena.deleteHolograms();
				arena.createHolograms();
				ArenaSetupGui.openCamerasEdit(player, Main.getArenaManager().getArenaByName(arena.getName()));

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createSkull(Camera.camera, ChatColor.GREEN + "Edit cameras", 1, ChatColor.YELLOW + "Cameras count: " + arena.getCamerasManager().getCameras().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCamerasSelector(player, arena);
			}
		});
		invHolder.addIcon(icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openCamerasSelector(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select camera: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createSkull(Camera.camera, ChatColor.YELLOW + "Cameras", 1, ""));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCamerasEdit(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		CamerasManager manager = arena.getCamerasManager();
		for (Camera cam : manager.getCameras()) {
			String locName = "None";
			if (cam.getLocName() != null) {
				locName = cam.getLocName().getName();
			}
			icon = new Icon(Utils.createSkull(Camera.camera, ChatColor.GREEN + "Camera: " + cam.getId(), 1, ChatColor.YELLOW + "Location name: " + locName));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openCameraEdit(player, arena, cam.getId());
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Create camera", 1));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCreateCameraChooseLocation(player, arena, 1);
			}
		});
		invHolder.setIcon(44, icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openCameraEdit(Player player, Arena arena, Integer camId) {
		Camera cam = arena.getCamerasManager().getCameras().get(camId);
		if (cam == null) {
			return;
		}

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit camera: " + arena.getName() + " - " + camId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createSkull(Camera.camera, ChatColor.YELLOW + "Camera: " + camId, 1, ""));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCamerasSelector(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		LocationName locName = cam.getLocName();
		String name = "none";
		if (locName != null) {
			name = locName.getName();
		}

		icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Change location name", 1, ChatColor.YELLOW + name));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCameraChangeChooseLocation(player, arena, camId, 1);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.BLACK_CONCRETE, ChatColor.GREEN + "Fake blocks", 1, ChatColor.YELLOW + "Fake blocks to limit the player vision", ChatColor.YELLOW + "Optional"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCameraFakeBlocksEdit(player, arena, camId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.GLASS, ChatColor.GREEN + "Fake air blocks", 1, ChatColor.YELLOW + "Fake air blocks to allow players to see through stuff",
				ChatColor.YELLOW + "For example: the camera is top down", ChatColor.YELLOW + "and you want players to see through the roof", ChatColor.YELLOW + "Optional"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCameraFakeAirBlocksEdit(player, arena, camId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createSkull(Camera.camera, ChatColor.GREEN + "Change camera head location", 1,
				ChatColor.YELLOW + Utils.locationToStringB(cam.getCamLoc()) + "/n" + ChatColor.YELLOW + "Click to change to your location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection camSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams." + cam.getConfigKey());

				String locStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getY()) + "," + (player.getLocation().getZ()) + ","
						+ player.getLocation().getYaw() + "," + player.getLocation().getPitch();
				camSec.set("camlocation", locStr);

				Main.getPlugin().saveConfig();
				cam.updateCamLoc(player.getLocation().clone());
				ArenaSetupGui.openCameraEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), camId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createSkull(Camera.lampOn, ChatColor.GREEN + "Change camera lamp location", 1,
				ChatColor.YELLOW + Utils.locationToStringB(cam.getLampLoc()) + "/n" + ChatColor.YELLOW + "Click to change to your location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection camSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams." + cam.getConfigKey());

				String locStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getY()) + "," + (player.getLocation().getZ()) + ","
						+ player.getLocation().getYaw() + "," + 0;
				camSec.set("lamplocation", locStr);

				Main.getPlugin().saveConfig();

				Location loc_ = player.getLocation().clone();
				loc_.setPitch(0);
				cam.updateLampLoc(loc_);

				ArenaSetupGui.openCameraEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), camId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.END_CRYSTAL, ChatColor.GREEN + "Change camera view location", 1, ChatColor.YELLOW + Utils.locationToStringB(cam.getViewLoc()),
				ChatColor.YELLOW + "Click to change to your location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection camSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams." + cam.getConfigKey());

				String locStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getY()) + "," + (player.getLocation().getZ()) + ","
						+ player.getLocation().getYaw() + "," + player.getLocation().getPitch();
				camSec.set("viewlocation", locStr);

				Main.getPlugin().saveConfig();

				cam.updateViewLoc(player.getLocation().clone());

				ArenaSetupGui.openCameraEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), camId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport to view location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.teleport(cam.getViewLoc());
				ArenaSetupGui.openCameraEdit(player, arena, camId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport to camera head location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.teleport(cam.getCamLoc());
				ArenaSetupGui.openCameraEdit(player, arena, camId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete camera"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection camerasSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams");

				cam.deleteArmorStands();

				camerasSec.set(cam.getConfigKey(), null);

				int i = 0;
				for (String cKey : camerasSec.getKeys(false)) {
					camerasSec.getConfigurationSection(cKey).set("id", i);
					i++;
				}

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openCamerasSelector(player, Main.getArenaManager().getArenaByName(arena.getName()));
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	// create camera choose location
	public static void openCreateCameraChooseLocation(Player player, Arena arena, Integer currentPage) {
		Integer totalItems = arena.getLocations().size() + 1;
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Choose camera location name: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createSkull(Camera.camera, ChatColor.YELLOW + "Create camera", 1, ""));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Choose camera location name", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCamerasSelector(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateCameraChooseLocation(player, arena, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateCameraChooseLocation(player, arena, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());
		keys.add(0, "----");

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			final Boolean isNone = key.equals("----");
			if (isNone) {
				icon = new Icon(
						Utils.createItem(Material.BARRIER, ChatColor.GREEN + "No location name", 1, ChatColor.YELLOW + "If you dont want this camera", ChatColor.YELLOW + "to have a location name"));
			} else {
				icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			}

			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection camerasSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams");

					ConfigurationSection createdSec = camerasSec.createSection(Utils.getRandomString(4));

					String locStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getY()) + "," + (player.getLocation().getZ()) + ","
							+ player.getLocation().getYaw() + ", " + player.getLocation().getPitch();

					createdSec.set("id", arena.getCamerasManager().getCameras().size());
					createdSec.set("viewlocation", locStr);
					createdSec.set("camlocation", locStr);
					createdSec.set("locationid", key);
					createdSec.set("fakeblocks", new ArrayList<String>());
					createdSec.set("fakeairblocks", new ArrayList<String>());

					String lampLocStr = player.getWorld().getName() + "," + (player.getLocation().getX()) + "," + (player.getLocation().getY() - 1) + "," + (player.getLocation().getZ()) + ","
							+ player.getLocation().getYaw() + ", " + 0;
					createdSec.set("lamplocation", lampLocStr);

					Main.getPlugin().saveConfig();
					Main.getArenaManager().loadArenas();

					player.closeInventory();
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully created camera");
				}
			});
			invHolder.addIcon(icon);
		}

		player.openInventory(invHolder.getInventory());
	}

	// change camera's location name after created
	public static void openCameraChangeChooseLocation(Player player, Arena arena, Integer camId, Integer currentPage) {
		Camera cam = arena.getCamerasManager().getCameras().get(camId);
		if (cam == null) {
			return;
		}

		LocationName locName = cam.getLocName();
		String name = "none";
		if (locName != null) {
			name = locName.getName();
		}

		Integer totalItems = arena.getLocations().size() + 1;
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Change camera location name: " + name + " - " + cam.getId());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createSkull(Camera.camera, ChatColor.YELLOW + "camera: " + camId, 1, ""));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Change camera location name", 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCameraEdit(player, arena, camId);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCameraChangeChooseLocation(player, arena, camId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCameraChangeChooseLocation(player, arena, camId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);
		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());
		keys.add(0, "----");

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			final Boolean isNone = key.equals("----");
			if (isNone) {
				icon = new Icon(
						Utils.createItem(Material.BARRIER, ChatColor.GREEN + "No location name", 1, ChatColor.YELLOW + "If you dont want this vent", ChatColor.YELLOW + "to have a location name"));
			} else {
				icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			}
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection camSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams." + cam.getConfigKey());

					camSec.set("locationid", key);

					Main.getPlugin().saveConfig();
					if (isNone) {
						cam.setLocName(null);
					} else {
						cam.setLocName(arena.getLocations().get(key));
					}
					player.closeInventory();
					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed camera " + cam.getId() + " location name to"
							+ (isNone ? "none" : arena.getLocations().get(key).getName()));
				}
			});
			invHolder.addIcon(icon);
		}
		player.openInventory(invHolder.getInventory());
	}

	public static void openCameraFakeBlocksEdit(Player player, Arena arena, Integer camId) {
		Camera cam = arena.getCamerasManager().getCameras().get(camId);
		if (cam == null) {
			return;
		}

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit camera fake blocks: " + arena.getName() + " - " + camId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createSkull(Camera.camera, ChatColor.YELLOW + "Camera: " + camId, 1, ""));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.BLACK_CONCRETE, ChatColor.YELLOW + "Edit fake blocks", 1, ChatColor.YELLOW + "Fake blocks count: " + cam.getFakeBlocks().size()));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCameraEdit(player, arena, camId);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.STICK, ChatColor.GREEN + "Get fake blocks wand", 1, ChatColor.YELLOW + "Break blocks to make them fake blocks"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.getInventory().addItem(Utils.createItem(Material.STICK, ChatColor.GOLD + "Fake Blocks Wand: " + arena.getName() + "|" + camId, 1,
						ChatColor.YELLOW + "Break blocks to make them fake blocks for camera " + camId));
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.BLAZE_ROD, ChatColor.GREEN + "Get remove fake blocks wand", 1, ChatColor.YELLOW + "Break blocks to remove them from the fake blocks list"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.getInventory().addItem(Utils.createItem(Material.BLAZE_ROD, ChatColor.GOLD + "Remove Fake Blocks Wand: " + arena.getName() + "|" + camId, 1,
						ChatColor.YELLOW + "Break blocks to remove them from the fake blocks list for cam " + camId));
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.GREEN + "Delete all fake blocks", 1, ChatColor.YELLOW + "Fake blocks count: " + cam.getFakeBlocks().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				ConfigurationSection camSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams." + cam.getConfigKey());

				camSec.set("fakeblocks", new ArrayList<String>());

				Main.getPlugin().saveConfig();
				cam.deleteFakeBlocks();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully removed all fake blocks for camera " + cam.getId());

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.BLACK_CONCRETE, ChatColor.GREEN + "Show all fake blocks to you"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				for (FakeBlock fb : cam.getFakeBlocks()) {
					fb.sendNewBlock(player);
				}
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.STONE, ChatColor.GREEN + "Hide all fake blocks to you", 1, ChatColor.YELLOW + "Hide all fake blocks only to you"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				for (FakeBlock fb : cam.getFakeBlocks()) {
					fb.sendOldBlock(player);
				}
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.BARRIER, ChatColor.GREEN + "Place all fake blocks", 1, ChatColor.YELLOW + "Replace all fake blocks with real blocks.",
				ChatColor.RED + "Warning: this will replace all existing blocks to the fake blocks."));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				for (FakeBlock fb : cam.getFakeBlocks()) {
					fb.getLoc().getBlock().setType(fb.getNewMat());
				}
			}
		});
		invHolder.addIcon(icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openCameraFakeAirBlocksEdit(Player player, Arena arena, Integer camId) {
		Camera cam = arena.getCamerasManager().getCameras().get(camId);
		if (cam == null) {
			return;
		}

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit camera fake air blocks: " + arena.getName() + " - " + camId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createSkull(Camera.camera, ChatColor.YELLOW + "Camera: " + camId, 1, ""));
		invHolder.getInventory().setItem(2,
				Utils.createItem(Material.GLASS, ChatColor.YELLOW + "Edit fake air blocks", 1, ChatColor.YELLOW + "Fake air blocks count: " + cam.getFakeAirBlocks().size()));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCameraEdit(player, arena, camId);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.STICK, ChatColor.GREEN + "Get fake air blocks wand", 1, ChatColor.YELLOW + "Break blocks to make them fake air blocks"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.getInventory().addItem(Utils.createItem(Material.STICK, ChatColor.GOLD + "Fake Air Blocks Wand: " + arena.getName() + "|" + camId, 1,
						ChatColor.YELLOW + "Break blocks to make them fake air blocks for camera " + camId));
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.BLAZE_ROD, ChatColor.GREEN + "Get remove fake air blocks wand", 1, ChatColor.YELLOW + "Break blocks to remove them from the fake air blocks list"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.getInventory().addItem(Utils.createItem(Material.BLAZE_ROD, ChatColor.GOLD + "Remove Fake Air Blocks Wand: " + arena.getName() + "|" + camId, 1,
						ChatColor.YELLOW + "Break blocks to remove them from the fake air blocks list for cam " + camId));
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.GREEN + "Delete all fake air blocks", 1, ChatColor.YELLOW + "Fake air blocks count: " + cam.getFakeAirBlocks().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				ConfigurationSection camSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".cameras.cams." + cam.getConfigKey());

				camSec.set("fakeairblocks", new ArrayList<String>());

				Main.getPlugin().saveConfig();
				cam.deleteFakeAirBlocks();
				player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully removed all fake air blocks for camera " + cam.getId());

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.GLASS, ChatColor.GREEN + "Show all fake air blocks to you"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				for (FakeBlock fb : cam.getFakeAirBlocks()) {
					fb.sendNewBlock(player);
				}
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.STONE, ChatColor.GREEN + "Hide all fake air blocks to you"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				for (FakeBlock fb : cam.getFakeAirBlocks()) {
					fb.sendOldBlock(player);
				}
			}
		});
		invHolder.addIcon(icon);

		player.openInventory(invHolder.getInventory());
	}

	// -------------- doors --------------

	public static void openDoorGroupSelector(Player player, Arena arena) {
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select door group: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_DOOR, ChatColor.YELLOW + "Door groups"));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openArenaEditor(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		DoorsManager manager = arena.getDoorsManager();
		for (DoorGroup dg : manager.getDoorGroups()) {
			icon = new Icon(Utils.createItem(Material.OAK_DOOR, ChatColor.GREEN + "Door group: " + dg.getId(), 1, ChatColor.YELLOW + "Location name: " + dg.getLocName().getName()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openDoorGroupEdit(player, arena, dg.getId());
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Add door group", 1, ChatColor.YELLOW + "Click to create a new door group"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openCreateDoorGroupChooseLocation(player, arena, 1);
			}
		});
		invHolder.setIcon(44, icon);

		player.openInventory(invHolder.getInventory());
	}

	public static void openDoorGroupEdit(Player player, Arena arena, Integer doorGroupId) {
		DoorGroup doorGroup = arena.getDoorsManager().getDoorGroup(doorGroupId);
		if (doorGroup == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit door group: " + arena.getName() + " - " + doorGroupId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.OAK_DOOR, ChatColor.YELLOW + "Door group: " + doorGroupId, 1, ChatColor.GREEN + "Location name: " + doorGroup.getLocName().getName()));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorGroupSelector(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location name: " + doorGroup.getLocName().getName(), 1, ChatColor.YELLOW + "Click to change"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorGroupChangeLocation(player, arena, doorGroupId, 1);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.IRON_DOOR, ChatColor.GREEN + "Doors", 1, ChatColor.YELLOW + "Doors count: " + doorGroup.getDoors().size()));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorsSelector(player, arena, doorGroupId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete door group"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection doorGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".doorgroups");
				doorGroupsSec.set(doorGroup.getConfigId(), null);

				int i = 0;
				for (String dgKey : doorGroupsSec.getKeys(false)) {
					doorGroupsSec.getConfigurationSection(dgKey).set("id", i);
					i++;
				}

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openDoorGroupSelector(player, Main.getArenaManager().getArenaByName(arena.getName()));
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	public static void openDoorsSelector(Player player, Arena arena, Integer doorGroupId) {
		DoorGroup doorGroup = arena.getDoorsManager().getDoorGroup(doorGroupId);
		if (doorGroup == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Select door: " + arena.getName() + " - " + doorGroupId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.OAK_DOOR, ChatColor.YELLOW + "Door group: " + doorGroupId, 1, ChatColor.GREEN + "Location name: " + doorGroup.getLocName().getName()));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.IRON_DOOR, ChatColor.YELLOW + "Select door"));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorGroupEdit(player, arena, doorGroupId);
			}
		});
		invHolder.setIcon(36, icon);

		for (Door door : doorGroup.getDoors()) {
			icon = new Icon(Utils.createItem(Material.IRON_DOOR, ChatColor.GREEN + "Door: " + door.getId()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ArenaSetupGui.openDoorEdit(player, arena, doorGroupId, door.getId());
				}
			});
			invHolder.addIcon(icon);
		}

		icon = new Icon(Utils.createItem(Material.CRAFTING_TABLE, ChatColor.YELLOW + "Add door"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				ConfigurationSection doorGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".doorgroups");
				ConfigurationSection doorGroupSec = doorGroupsSec.getConfigurationSection(doorGroup.getConfigId());
				ConfigurationSection doorsSec = doorGroupSec.getConfigurationSection("doors");

				String configId = Utils.getRandomString(4);
				ConfigurationSection doorSec = doorsSec.createSection(configId);

				Integer id = doorGroup.getDoors().size();
				doorSec.set("id", id);
				doorSec.set("corner1", "world,0,0,0");
				doorSec.set("corner2", "world,0,0,0");

				doorGroup.addDoor(new Door(arena, doorGroup, new Location(player.getWorld(), 0, 0, 0), new Location(player.getWorld(), 0, 0, 0), id, configId));
				Main.getPlugin().saveConfig();

				ArenaSetupGui.openDoorsSelector(player, Main.getArenaManager().getArenaByName(arena.getName()), doorGroupId);
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

	// create door group choose location
	public static void openCreateDoorGroupChooseLocation(Player player, Arena arena, Integer currentPage) {
		// so there wont be 2 door groups on the same location name
		ArrayList<String> locs_ = new ArrayList<String>();
		for (DoorGroup dg : arena.getDoorsManager().getDoorGroups()) {
			locs_.add(dg.getLocName().getId());
		}

		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());
		keys.removeIf(n -> (locs_.contains(n)));

		Integer totalItems = keys.size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Choose door group location name: " + arena.getName());
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_DOOR, ChatColor.YELLOW + "Create door group"));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Choose door group location name"));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorGroupSelector(player, arena);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateDoorGroupChooseLocation(player, arena, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openCreateDoorGroupChooseLocation(player, arena, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection doorGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".doorgroups");

					ConfigurationSection createdSec = doorGroupsSec.createSection(Utils.getRandomString(4));

					createdSec.set("id", arena.getDoorsManager().getDoorGroups().size());
					createdSec.set("locationid", key);
					createdSec.createSection("doors");

					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully created door group " + (arena.getDoorsManager().getDoorGroups().size() + 1));

					Main.getPlugin().saveConfig();
					Main.getArenaManager().loadArenas();

					player.closeInventory();
				}
			});
			invHolder.addIcon(icon);
		}
		player.openInventory(invHolder.getInventory());
	}

	public static void openDoorGroupChangeLocation(Player player, Arena arena, Integer doorGroupId, Integer currentPage) {
		DoorGroup doorGroup = arena.getDoorsManager().getDoorGroup(doorGroupId);
		if (doorGroup == null) {
			return;
		}
		// so there wont be 2 door groups on the same location name
		ArrayList<String> locs_ = new ArrayList<String>();
		for (DoorGroup dg : arena.getDoorsManager().getDoorGroups()) {
			locs_.add(dg.getLocName().getId());
		}

		ArrayList<String> keys = new ArrayList<String>(arena.getLocations().keySet());
		keys.removeIf(n -> (locs_.contains(n)));

		Integer totalItems = keys.size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Change door group location name: " + arena.getName() + " - " + doorGroupId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1,
				Utils.createItem(Material.OAK_DOOR, ChatColor.YELLOW + "Door group: " + doorGroupId, 1, ChatColor.GREEN + "Location name: " + doorGroup.getLocName().getName()));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.WHITE_BANNER, ChatColor.YELLOW + "Change door group location name"));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorGroupEdit(player, arena, doorGroupId);
			}
		});
		invHolder.setIcon(36, icon);

		if (totalPages > 1) {
			invHolder.getInventory().setItem(40, Utils.createItem(Material.OAK_SIGN, ChatColor.YELLOW + "Page: " + currentPage + "/" + totalPages, currentPage));

			if (currentPage > 1) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Previous page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openDoorGroupChangeLocation(player, arena, doorGroupId, currentPage - 1);
					}
				});
				invHolder.setIcon(39, icon);
			}
			if (currentPage < totalPages) {
				icon = new Icon(Utils.createItem(Material.PAPER, ChatColor.YELLOW + "Next page"));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						ArenaSetupGui.openDoorGroupChangeLocation(player, arena, doorGroupId, currentPage + 1);
					}
				});
				invHolder.setIcon(41, icon);
			}
		}

		Integer startIndex = (currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

		for (int i = startIndex; i <= endIndex; i++) {
			final String key = keys.get(i);
			icon = new Icon(Utils.createItem(Material.WHITE_BANNER, ChatColor.GREEN + "Location: " + arena.getLocations().get(key).getName()));
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					ConfigurationSection doorGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".doorgroups");

					ConfigurationSection doorSec = doorGroupsSec.getConfigurationSection(doorGroup.getConfigId());

					doorSec.set("locationid", key);

					Main.getPlugin().saveConfig();
					Main.getArenaManager().loadArenas();

					player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.YELLOW + "Successfully changed door group " + doorGroup.getId() + " location name to "
							+ arena.getLocations().get(key).getName());
					ArenaSetupGui.openDoorGroupEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), doorGroupId);
				}
			});
			invHolder.addIcon(icon);
		}

		player.openInventory(invHolder.getInventory());
	}

	public static void openDoorEdit(Player player, Arena arena, Integer doorGroupId, Integer doorId) {
		DoorGroup doorGroup = arena.getDoorsManager().getDoorGroup(doorGroupId);
		if (doorGroup == null) {
			return;
		}
		Door door = doorGroup.getDoor(doorId);
		if (door == null) {
			return;
		}
		CustomHolder invHolder = new CustomHolder(45, ChatColor.YELLOW + "Edit door: " + arena.getName() + " - " + doorGroupId + ": " + doorId);
		Utils.addBorder(invHolder.getInventory(), 45);

		invHolder.getInventory().setItem(0, Utils.createItem(Material.WHITE_WOOL, ChatColor.YELLOW + arena.getName(), 1));
		invHolder.getInventory().setItem(1, Utils.createItem(Material.OAK_DOOR, ChatColor.YELLOW + "Door group: " + doorGroupId, 1));
		invHolder.getInventory().setItem(2, Utils.createItem(Material.IRON_DOOR, ChatColor.YELLOW + "Door: " + doorId, 1));

		Icon icon = new Icon(Utils.createItem(Material.ARROW, ChatColor.YELLOW + "Back"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ArenaSetupGui.openDoorGroupEdit(player, arena, doorGroupId);
			}
		});
		invHolder.setIcon(36, icon);

		icon = new Icon(Utils.createItem(Material.IRON_BLOCK, ChatColor.GREEN + "Corner-1", 1, ChatColor.YELLOW + Utils.locationToStringB(door.getCorner1()),
				ChatColor.YELLOW + "Corner 1 for door's blocks", ChatColor.YELLOW + "Click to change to your location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				ConfigurationSection doorGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".doorgroups");
				ConfigurationSection doorGroupSec = doorGroupsSec.getConfigurationSection(doorGroup.getConfigId());
				ConfigurationSection doorsSec = doorGroupSec.getConfigurationSection("doors");

				ConfigurationSection doorSec = doorsSec.getConfigurationSection(door.getConfigId());

				doorSec.set("corner1", player.getWorld().getName() + "," + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ());

				if (door.getCorner2() != null && Utils.blocksFromTwoPoints(player.getLocation(), door.getCorner2()).size() <= 30) {
					for (Block block : Utils.blocksFromTwoPoints(player.getLocation(), door.getCorner2())) {
						Packets.sendPacket(player, Packets.BLOCK_CHANGE(block.getLocation(), WrappedBlockData.createData(Material.IRON_BLOCK)));
					}
				}

				door.setCorner1(player.getLocation());

				Main.getPlugin().saveConfig();
//				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openDoorEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), doorGroupId, doorId);
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.IRON_BLOCK, ChatColor.GREEN + "Corner-2", 1, ChatColor.YELLOW + Utils.locationToStringB(door.getCorner2()),
				ChatColor.YELLOW + "Corner 2 for door's blocks", ChatColor.YELLOW + "Click to change to your location"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				arena.endGame(false);
				ConfigurationSection doorGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".doorgroups");
				ConfigurationSection doorGroupSec = doorGroupsSec.getConfigurationSection(doorGroup.getConfigId());
				ConfigurationSection doorsSec = doorGroupSec.getConfigurationSection("doors");

				ConfigurationSection doorSec = doorsSec.getConfigurationSection(door.getConfigId());

				doorSec.set("corner2", player.getWorld().getName() + "," + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ());

				if (door.getCorner1() != null && Utils.blocksFromTwoPoints(door.getCorner1(), player.getLocation()).size() <= 30) {
					for (Block block : Utils.blocksFromTwoPoints(door.getCorner1(), player.getLocation())) {
						Packets.sendPacket(player, Packets.BLOCK_CHANGE(block.getLocation(), WrappedBlockData.createData(Material.IRON_BLOCK)));
					}
				}

				door.setCorner2(player.getLocation());

				Main.getPlugin().saveConfig();
//				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openDoorEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), doorGroupId, doorId);

			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport to door"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				player.teleport(door.getMidPoint());
				player.openInventory(invHolder.getInventory());
			}
		});
		invHolder.addIcon(icon);

		icon = new Icon(Utils.createItem(Material.REDSTONE, ChatColor.YELLOW + "Delete door"));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				ConfigurationSection doorGroupsSec = Main.getConfigManager().getConfig().getConfigurationSection("arenas." + arena.getName() + ".doorgroups");
				ConfigurationSection doorGroupSec = doorGroupsSec.getConfigurationSection(doorGroup.getConfigId());
				ConfigurationSection doorsSec = doorGroupSec.getConfigurationSection("doors");

				doorsSec.set(door.getConfigId(), null);

				int i = 0;
				for (String dKey : doorsSec.getKeys(false)) {
					doorsSec.getConfigurationSection(dKey).set("id", i);
					i++;
				}

				Main.getPlugin().saveConfig();
				Main.getArenaManager().loadArenas();

				ArenaSetupGui.openDoorGroupEdit(player, Main.getArenaManager().getArenaByName(arena.getName()), doorGroupId);
			}
		});
		invHolder.setIcon(44, icon);
		player.openInventory(invHolder.getInventory());
	}

}
