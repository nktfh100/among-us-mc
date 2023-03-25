package com.nktfh100.AmongUs.info;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

import com.nktfh100.AmongUs.api.events.*;
import com.nktfh100.AmongUs.enums.*;
import com.nktfh100.AmongUs.holograms.HologramClickListener;
import com.nktfh100.AmongUs.holograms.ImposterHologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLineClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView.Scale;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.nktfh100.AmongUs.inventory.ColorSelectorInv;
import com.nktfh100.AmongUs.inventory.MeetingBtnInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskInvHolder;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.main.Renderer;
import com.nktfh100.AmongUs.managers.CamerasManager;
import com.nktfh100.AmongUs.managers.DeadBodiesManager;
import com.nktfh100.AmongUs.managers.DoorsManager;
import com.nktfh100.AmongUs.managers.ItemsManager;
import com.nktfh100.AmongUs.managers.MeetingManager;
import com.nktfh100.AmongUs.managers.SabotageManager;
import com.nktfh100.AmongUs.managers.TasksManager;
import com.nktfh100.AmongUs.managers.VentsManager;
import com.nktfh100.AmongUs.managers.VisibilityManager;
import com.nktfh100.AmongUs.managers.VitalsManager;
import com.nktfh100.AmongUs.managers.MeetingManager.meetingState;
import com.nktfh100.AmongUs.utils.Packets;
import com.nktfh100.AmongUs.utils.Utils;

public class Arena {
	private File arenaFile;
	private FileConfiguration arenaConfig;
	private String name;
	private String displayName;
	private Integer minPlayers;
	private Integer maxPlayers;
	private ArrayList<Location> playersSpawns = new ArrayList<Location>();;
	private HashMap<String, PlayerInfo> ingamePlayers = new HashMap<String, PlayerInfo>();
	private ArrayList<PlayerInfo> gameImposters = new ArrayList<PlayerInfo>();
	private ArrayList<PlayerInfo> impostersAlive = new ArrayList<PlayerInfo>();
	private ArrayList<PlayerInfo> ghosts = new ArrayList<PlayerInfo>();
	private HashMap<Short, Boolean> mapIds = new HashMap<Short, Boolean>();
	private BossBar bossBar;
	private World world;
	private Location waitingLobby;
	private Location mapCenter;
	private Location meetingButton;
	private Location camerasLoc = null;
	private Location vitalsLoc = null;

	private GameState gameState = GameState.WAITING;
	private Boolean isInMeeting = false;
	private HashMap<String, Task> tasks = new HashMap<String, Task>();
	private ArrayList<SabotageArena> sabotages = new ArrayList<SabotageArena>();

	private HashMap<String, LocationName> locations = new HashMap<String, LocationName>();

	private ArrayList<PlayerInfo> scanQueue = new ArrayList<PlayerInfo>();

	// game settings
	private Integer commonTasks = 3;
	private Integer longTasks = 2;
	private Integer shortTasks = 1;
	private Integer gameTimer = 30;
	private Integer votingTime = 30;
	private Integer discussionTime = 30;
	private Integer proceedingTime = 5;
	private Integer numImposters = 2;
	private Integer meetingsPerPlayer = 1;
	private Integer killCooldown = 30;
	private Integer meetingCooldown = 10;
	private Integer sabotageCooldown = 17;
	private Double reportDistance = 3.5D;
	private Integer imposterVision = 15;
	private Integer crewmateVision = 10;

	private Integer doorCloseTime = 10;
	private Integer doorCooldown = 30;

	private Boolean enableReducedVision = true;
	private Boolean hideHologramsOutOfView = false;
	private Boolean disableSprinting = true;
	private Boolean disableJumping = true;
	private Boolean disableMap = false;
	private Boolean enableVisualTasks = true;
	private Boolean confirmEjects = true;
	private Boolean moveMapWithPlayer = false;
	private Boolean dynamicImposters = false;

	// Roles

	private Integer scientistChance = 0;
	private Integer scientistCount = 0;
	private Integer scientistVitalsCooldown = 15;
	private Integer scientistBatteryDuration = 5;

	private Integer engineerChance = 0;
	private Integer engineerCount = 0;
	private Integer engineerVentCooldown = 30;
	private Integer engineerMaxTimeInVents = 15;

	private Integer angelChance = 0;
	private Integer angelCount = 0;
	private Integer angelCooldown = 60;
	private Integer angelDuration = 10;
	private boolean angelProtectVisibleToImposters = false;

	private Integer shapeshifterChance = 0;
	private Integer shapeshifterCount = 0;
	private Integer shapeshifterDuration = 30;
	private Integer shapeshifterCooldown = 10;
	private boolean shapeshifterLeaveEvidence = false;

	private BukkitTask gameTimerRunnable = null;
	private BukkitTask secondRunnable = null;

	private ArrayList<ImposterHologram> holograms = new ArrayList<ImposterHologram>();
	private ImposterHologram btnHolo;

	private Boolean enableCameras = false;

	private ArrayList<ColorInfo> colors_ = Utils.getPlayersColors();

	private TasksManager taskManager = new TasksManager(this);
	private SabotageManager sabotageManager = new SabotageManager(this);
	private MeetingManager meetingManager = new MeetingManager(this);
	private DeadBodiesManager deadBodiesManager = new DeadBodiesManager(this);
	private VentsManager ventsManager = new VentsManager(this);
	private CamerasManager camerasManager = new CamerasManager(this);
	private VisibilityManager visibilityManager = new VisibilityManager(this);
	private DoorsManager doorsManager = new DoorsManager(this);
	private VitalsManager vitalsManager = new VitalsManager(this);

	private ArrayList<JoinSign> joinSigns = new ArrayList<JoinSign>();

	private ColorSelectorInv colorSelectorInv = new ColorSelectorInv(this);

	// For Prime shields visual task
	private ArrayList<Block> primeShieldsBlocks = new ArrayList<Block>();

	// For asteroids visual task
	private long asteroidsLastTime = System.currentTimeMillis();

	private Integer gameTimerActive = 30;

	private ArrayList<Player> _playersToDelete = new ArrayList<Player>();

	public Arena(String name) {
		this.name = name;

		this.bossBar = Bukkit.createBossBar(Main.getMessagesManager().getGameMsg("tasksBar", this, null, null), BarColor.GREEN, BarStyle.SEGMENTED_20);
		this.bossBar.setProgress(0);
	}

	public void giveGameInventory(PlayerInfo pInfo) {
		pInfo.getPlayer().getInventory().clear();
		if (!pInfo.getIsInVent() && !pInfo.getIsInCameras()) {
			pInfo.giveArmor();
		}
		ItemsManager itemsManager = Main.getItemsManager();
		if (!this.disableMap) {
			// minimap
			if (pInfo.getMapId() == -1) {
				Short id = 0;
				for (Short id_ : this.mapIds.keySet()) {
					if (!this.mapIds.get(id_)) {
						id = id_;
						break;
					}
				}
				this.mapIds.put((short) id, true);
				pInfo.setMapId(id);
			}
			if (pInfo.getMapId() != -1) {
				this.giveGameMap(pInfo, itemsManager.getItem("map").getItem().getSlot());
			}
		}
		if (pInfo.getIsImposter()) {
			if (pInfo.getIsInVent()) {
				if (pInfo.getVent().getId() > 0 || (pInfo.getVentGroup().getLoop() && pInfo.getVent().getId() == 0)) {
					ItemInfo ventLeft = itemsManager.getItem("vent_left").getItem();
					pInfo.getPlayer().getInventory().setItem(ventLeft.getSlot(), ventLeft.getItem());
				}
				ItemInfo leaveVent = itemsManager.getItem("vent_leave").getItem();
				pInfo.getPlayer().getInventory().setItem(leaveVent.getSlot(), leaveVent.getItem());
				if (pInfo.getVent().getId() < pInfo.getVentGroup().getVents().size() - 1 || (pInfo.getVentGroup().getLoop() && pInfo.getVent().getId() == pInfo.getVentGroup().getVents().size() - 1)) {
					ItemInfo ventRight = itemsManager.getItem("vent_right").getItem();
					pInfo.getPlayer().getInventory().setItem(ventRight.getSlot(), ventRight.getItem());
				}
			} else if (!pInfo.getIsInCameras()) {
				for (SabotageArena sa : this.getSabotages()) {
					String name = Main.getMessagesManager().getTaskName(sa.getType().toString());
					ItemInfoContainer saboItemInfo = this.getSabotageManager().getSabotageItemInfo(sa.getType());
					ItemStack saboItem = this.getSabotageManager().getSabotageItem(sa.getType(), name, this.getSabotageManager().getSabotageCoolDownTimer(pInfo.getPlayer()));
					pInfo.getPlayer().getInventory().setItem(saboItemInfo.getSlot(), saboItem);
				}
				int s_ = 9;
				for (DoorGroup dg : this.getDoorsManager().getDoorGroups()) {
					pInfo.getPlayer().getInventory().setItem(s_, this.getDoorsManager().getSabotageDoorItem(pInfo.getPlayer(), dg.getId()));
					s_++;
				}
				if (!pInfo.isGhost()) {
					pInfo.giveKillItem(pInfo.getKillCoolDown());
				}
			}
		}
		if (pInfo.getIsInCameras()) {
			ItemInfo camerasLeft = itemsManager.getItem("cameras_left").getItem();
			ItemInfo camerasLeave = itemsManager.getItem("cameras_leave").getItem();
			ItemInfo camerasRight = itemsManager.getItem("cameras_right").getItem();

			pInfo.getPlayer().getInventory().setItem(camerasLeft.getSlot(), camerasLeft.getItem());
			pInfo.getPlayer().getInventory().setItem(camerasLeave.getSlot(), camerasLeave.getItem());
			pInfo.getPlayer().getInventory().setItem(camerasRight.getSlot(), camerasRight.getItem());
		}
		if (!pInfo.isGhost() && !pInfo.getIsInVent() && !pInfo.getIsInCameras()) {
			ItemInfo reportItem = itemsManager.getItem("report").getItem();
			pInfo.getPlayer().getInventory().setItem(reportItem.getSlot(), reportItem.getItem());
		}
		if (!this.getIsInMeeting() && !pInfo.getIsInCameras() && !pInfo.getIsInVent()) {
			pInfo.setUseItemState(0, true);
			pInfo.updateUseItemState(pInfo.getPlayer().getLocation());
		}
	}

	public void startSecondRunnable() {
		if (this.secondRunnable != null) {
			this.secondRunnable.cancel();
		}
		// runs every second
		// for timers and stuff
		Boolean arenaHasInspectSample1 = false;
		for (Task task : this.getAllTasks()) {
			if (task.getTaskType() == TaskType.INSPECT_SAMPLE || task.getTaskType() == TaskType.REBOOT_WIFI) {
				arenaHasInspectSample1 = true;
				break;
			}
			if (arenaHasInspectSample1) {
				break;
			}
			for (QueuedTasksVariant qtv : task.getQueuedTasksVariants()) {
				for (Task t1 : qtv.getQueuedTasksTasks()) {
					if (t1.getTaskType() == TaskType.INSPECT_SAMPLE || t1.getTaskType() == TaskType.REBOOT_WIFI) {
						arenaHasInspectSample1 = true;
						break;
					}
				}
			}
		}

		final Boolean arenaHasInspectSample = arenaHasInspectSample1;
		Arena arena = this;
		this.secondRunnable = new BukkitRunnable() {
			Boolean sendDamageAnim = true;

			@Override
			public void run() {
				if (arena.getGameState() == GameState.RUNNING) {
					if (arena.getMeetingManager().getMeetingCooldownTimer() > 0) {
						arena.getMeetingManager().setMeetingCooldownTimer(arena.getMeetingManager().getMeetingCooldownTimer() - 1);

						for (Player player : arena.getPlayers()) {
							if (player.getOpenInventory().getTopInventory().getHolder() instanceof MeetingBtnInv) {
								((MeetingBtnInv) player.getOpenInventory().getTopInventory().getHolder()).update();
							}
						}
					}

					if (arena.getIsInMeeting() && !arena.getMeetingManager().getIsSendingTitle()) {
						Integer timer = arena.getMeetingManager().getActiveTimer();
						// timer
						String msgKey = "votingBeginsIn";
						if (arena.getMeetingManager().getState() == meetingState.VOTING) {
							msgKey = "votingEndsIn";
						} else if (arena.getMeetingManager().getState() == meetingState.VOTING_RESULTS) {
							msgKey = "proceedingIn";
						}

						HashMap<String, String> placeholders = new HashMap<>();
						placeholders.put("%time%", String.valueOf(timer));
						if (!Main.getMessagesManager().getGameMsg(msgKey + "ActionBar", arena, placeholders, null).isEmpty()) {
							for (Player p : arena.getPlayers()) {
								Utils.sendActionBar(p, Main.getMessagesManager().getGameMsg(msgKey + "ActionBar", arena, placeholders, p));
							}
						}
						if (!Main.getMessagesManager().getGameMsg(msgKey + "Msg", arena, placeholders, null).isEmpty()) {
							if ((timer > 0 && timer <= 5) || timer == 10) {
								arena.sendMessage(msgKey + "Msg", placeholders);
							}
						}

					} else if (!arena.getIsInMeeting()) {
						if (!arena.getSabotageManager().getIsSabotageActive()) {
							for (PlayerInfo pInfo : arena.getGameImposters()) {
								Integer saboCooldown = arena.getSabotageManager().getSabotageCoolDownTimer(pInfo.getPlayer());
								if (saboCooldown > 0) {
									arena.getSabotageManager().setSabotageCoolDownTimer(pInfo.getPlayer().getUniqueId().toString(), saboCooldown - 1);
								}
								int s_ = 9;
								String uuid = pInfo.getPlayer().getUniqueId().toString();
								for (DoorGroup dg : arena.getDoorsManager().getDoorGroups()) {
									Integer doorCooldown = dg.getCooldownTimer(uuid);
									if (doorCooldown > 0) {
										dg.setCooldownTimer(uuid, doorCooldown - 1);
									}
									ItemStack item = arena.getDoorsManager().getSabotageDoorItem(pInfo.getPlayer(), dg.getId());
									pInfo.getPlayer().getInventory().setItem(s_, item);
									s_++;
								}
							}
						} else {
							if (Main.getConfigManager().getDamageOnSabotage()) {
								// Send damage animation / sound (while a sabotage is active)
								if (sendDamageAnim) {
									for (PlayerInfo pInfo : arena.getPlayersInfo()) {
										PacketContainer damagePacket = new PacketContainer(PacketType.Play.Server.ANIMATION);
										damagePacket.getIntegers().write(1, 1);
										damagePacket.getIntegers().write(0, pInfo.getPlayer().getEntityId());
										Packets.sendPacket(pInfo.getPlayer(), damagePacket);
										Packets.sendPacket(pInfo.getPlayer(), Packets.NAMED_SOUND(pInfo.getPlayer().getLocation(), Sound.ENTITY_PLAYER_HURT));
									}
									sendDamageAnim = false;
								} else {
									sendDamageAnim = true;
								}
							}

							if (arena.getSabotageManager().getActiveSabotage().getType() == SabotageType.REACTOR_MELTDOWN
									|| arena.getSabotageManager().getActiveSabotage().getType() == SabotageType.OXYGEN) {
								for (PlayerInfo pInfo : arena.getPlayersInfo()) {
									Main.getSoundsManager().playSound("sabotageAlarm", pInfo.getPlayer(), pInfo.getPlayer().getLocation());
								}
							}
						}

						// particles on tasks if enabled
						if (Main.getConfigManager().getParticlesOnTasks()) {
							TasksManager tasksManager = arena.getTasksManager();
							for (PlayerInfo pInfo : arena.getPlayersInfo()) {
								if (pInfo == null || pInfo.getIsImposter() == null) {
									continue;
								}
								if (!pInfo.getIsImposter()) {
									for (TaskPlayer tp : tasksManager.getTasksForPlayer(pInfo.getPlayer())) {
										if (!tp.getIsDone() && tp.getActiveTask().getHolo().isVisibleTo(pInfo.getPlayer())) {
											if (!arena.getEnableReducedVision()
													|| Utils.isInsideCircle(pInfo.getPlayer().getLocation(), (double) pInfo.getVision(), tp.getActiveTask().getLocation()) != 2) {
												Packets.sendPacket(pInfo.getPlayer(), Packets.PARTICLES(tp.getActiveTask().getHolo().getLocation().add(0, -0.3, 0),
														Main.getConfigManager().getParticlesOnTasksType(), null, 8, 0.4f, 0.3f, 0.4f));
											}
										}
									}
								}
							}
						}

						// timer for inspect sample task
						if (arenaHasInspectSample) {
							for (PlayerInfo pInfo : arena.getPlayersInfo()) {

								if (!pInfo.getIsImposter()) {
									for (TaskPlayer tp : arena.getTasksManager().getTasksForPlayer(pInfo.getPlayer())) {
										if (tp.getActiveTask().getTaskType() == TaskType.INSPECT_SAMPLE) {
											if (tp.getInspectIsRunning_()) {
												if (tp.getInspectTimer_() > 0) {
													tp.setInspectTimer_(tp.getInspectTimer_() - 1);
												}
											}
										} else if (tp.getActiveTask().getTaskType() == TaskType.REBOOT_WIFI) {
											if (tp.getRebootIsRunning_()) {
												if (tp.getRebootTimer_() > 0) {
													tp.setRebootTimer_(tp.getRebootTimer_() - 1);
												}
											}
										}
									}
								}
							}
						}

						// doors close timer
						for (DoorGroup dg : arena.getDoorsManager().getDoorGroups()) {
							Integer doorCloseTimer = dg.getCloseTimer();
							if (doorCloseTimer > 0) {
								dg.setCloseTimer(doorCloseTimer - 1);
								if (doorCloseTimer - 1 <= 0) {
									dg.openDoors(true);
								}
							}
						}

						// action bar for imposters
						String imposters_ = "";
						for (PlayerInfo pInfo : arena.getGameImposters()) {
							imposters_ += pInfo.getColor().getChatColor() + "" + ChatColor.BOLD + pInfo.getPlayer().getName() + " ";
						}

						HashMap<String, String> placeholders = new HashMap<>();
						placeholders.put("%imposters%", imposters_);

						for (PlayerInfo pInfo : arena.getGameImposters()) {
							String impostersActionBar = Main.getMessagesManager().getGameMsg("impostersActionBar", arena, placeholders, pInfo.getPlayer());
							if (pInfo.getIsInVent()) {
								Utils.sendActionBar(pInfo.getPlayer(), arena.getVentsManager().getVentActionBar(pInfo.getVent(), pInfo.getPlayer()));
							} else if (!pInfo.getIsInCameras()) {
								Utils.sendActionBar(pInfo.getPlayer(), impostersActionBar);
							}

							if (!pInfo.isGhost() && !pInfo.getIsInVent() && !pInfo.getIsInCameras() && !pInfo.getKillCoolDownPaused()) {
								if (pInfo.getKillCoolDown() > 0) {
									pInfo.setKillCoolDown(pInfo.getKillCoolDown() - 1);
									if (pInfo.getKillCoolDown() == 0) {
										pInfo.giveKillItem(pInfo.getKillCoolDown());
									}
								}
							}
						}
						for (PlayerInfo pInfo : arena.getPlayersInfo()) {
							if (pInfo.getIsInCameras()) {
								Utils.sendActionBar(pInfo.getPlayer(), arena.getCamerasManager().getCameraActionBar(pInfo.getActiveCamera(), pInfo.getPlayer()));
							}
						}
					}
				} else if (arena.getGameState() != GameState.FINISHING) {
					Integer players_ = arena.getPlayersInfo().size();
					if (players_ > 0) {
						HashMap<String, String> placeholders = new HashMap<>();
						placeholders.put("%players%", String.valueOf(arena.getPlayersInfo().size()));
						placeholders.put("%max_players%", String.valueOf(arena.getMaxPlayers()));

						if (Main.getConfigManager().getEnableDoubleImposterChance()) {
							for (Player player : arena.getPlayers()) {
								if (player.hasPermission("amongus.perk.double-imposter-chance")) {
									players_++;
								}
							}
							Double imposterChance = ((double) 1 / (double) players_) * 100D;
							Double imposterChance1 = ((double) 2 / (double) players_) * 100D;
							if (imposterChance > 100) {
								imposterChance = 100D;
							} else if (imposterChance < 0) {
								imposterChance = 0D;
							}
							if (imposterChance1 > 100) {
								imposterChance1 = 100D;
							} else if (imposterChance1 < 0) {
								imposterChance1 = 0D;
							}

							HashMap<String, String> doubleChancePl = new HashMap<>(placeholders);
							doubleChancePl.put("%imposter_chance%", String.valueOf(imposterChance1.intValue()));
							HashMap<String, String> normalChancePl = new HashMap<>(placeholders);
							normalChancePl.put("%imposter_chance%", String.valueOf(imposterChance.intValue()));

							for (Player player : arena.getPlayers()) {
								String msg = Main.getMessagesManager().getGameMsg("lobbyActionBar", arena, normalChancePl, player);
								String msg1 = Main.getMessagesManager().getGameMsg("lobbyActionBar", arena, doubleChancePl, player);
								if (player.hasPermission("amongus.perk.double-imposter-chance")) {
									Utils.sendActionBar(player, msg1);
								} else {
									Utils.sendActionBar(player, msg);
								}
							}
						} else {
							Double imposterChance = ((double) arena.getNumImposters() / (double) players_) * 100D;
							if (imposterChance > 100) {
								imposterChance = 100D;
							} else if (imposterChance < 0) {
								imposterChance = 0D;
							}
							placeholders.put("%imposter_chance%", String.valueOf(imposterChance.intValue()));
							for (PlayerInfo pInfo : arena.getPlayersInfo()) {
								String msg = Main.getMessagesManager().getGameMsg("lobbyActionBar", arena, placeholders, pInfo.getPlayer());
								Utils.sendActionBar(pInfo.getPlayer(), msg);
							}
						}
					}
				}

				if (arena.getGameState() == GameState.RUNNING || arena.getGameState() == GameState.FINISHING) {
					if (arena.getPlayersInfo() != null && !arena.getPlayersInfo().isEmpty()) {
						for (PlayerInfo pInfo : arena.getPlayersInfo()) {
							if (pInfo != null && pInfo.getStatsManager() != null) {
								pInfo.getStatsManager().plusOneStatInt(StatInt.TIME_PLAYED);
							}
						}
					}
				}
			}
		}.runTaskTimer(Main.getPlugin(), 20L, 20L);
	}

	public void stopSecondRunnable() {
		if (this.secondRunnable != null) {
			this.secondRunnable.cancel();
			this.secondRunnable = null;
		}
	}

	public void updatePlayerColor(PlayerInfo pInfo, ColorInfo color) {
		if (this.colors_.contains(color)) {
			this.colors_.add(pInfo.getColor());
			this.colors_.removeIf(n -> (n == color));
			Collections.sort(this.colors_);
			pInfo.setColor(color);
			pInfo.giveArmor();
			ItemInfo colorSelectorItem = Main.getItemsManager().getItem("colorSelector").getItem();
			pInfo.getPlayer().getInventory().setItem(colorSelectorItem.getSlot(), Utils.createItem(pInfo.getColor().getWool(), colorSelectorItem.getTitle(), 1, colorSelectorItem.getLore()));
			PacketContainer packet = Packets.UPDATE_DISPLAY_NAME(pInfo.getPlayer().getUniqueId(), pInfo.getPlayer().getName(), pInfo.getCustomName());
			for (Player player : this.getPlayers()) {
				Packets.sendPacket(player, packet);
			}
			pInfo.updateScoreBoard();
		}
	}

	@SuppressWarnings("deprecation")
	public void giveGameMap(PlayerInfo pInfo, int slot) {
		if (!this.disableMap) {
			ItemInfoContainer mapInfo = Main.getItemsManager().getItem("map");
			short id = pInfo.getMapId();
			ItemStack mapItem = Utils.createItem(Material.FILLED_MAP, mapInfo.getItem().getTitle(), 1, mapInfo.getItem().getLore());
			MapMeta meta = (MapMeta) mapItem.getItemMeta();
			meta.setMapView(Bukkit.getMap(id));
			if (!meta.hasMapView()) {
				return;
			}
			meta.getMapView().setScale(Scale.CLOSEST);
			meta.getMapView().setWorld(this.getWorld());
			int i__ = 1;
			for (MapRenderer r : meta.getMapView().getRenderers()) {
				if (i__ == 2) {
					meta.getMapView().removeRenderer(r);
				}
				i__++;
			}
			meta.getMapView().setUnlimitedTracking(false);
			meta.getMapView().addRenderer(new Renderer());
			mapItem.setItemMeta(meta);
			if (pInfo.getIsMapInOffHand()) {
				pInfo.getPlayer().getInventory().setItemInOffHand(mapItem);
				pInfo.getPlayer().getInventory().setItem(slot, Utils.createItem(mapInfo.getItem2().getMat(), mapInfo.getItem2().getTitle(), 1, mapInfo.getItem2().getLore()));
			} else {
				pInfo.getPlayer().getInventory().setItem(slot, mapItem);
			}
		}
	}

	public ArrayList<String> getTasksLength(TaskLength l) {
		ArrayList<String> out = new ArrayList<String>();
		for (Task task : this.tasks.values()) {
			if (task.getTaskType().getTaskLength() == l) {
				out.add(task.getId());
			}
		}
		return out;
	}

	public void addSign(Location loc) {
		this.joinSigns.add(new JoinSign(this, loc));
	}

	public void setMapIds(ArrayList<Short> ids) {
		for (Short i : ids) {
			this.mapIds.put(i, false);
		}
	}

	public void addPlayerSpawn(Location loc) {
		this.playersSpawns.add(loc);
		if (loc.getWorld() == null || loc == null) {
			return;
		}
		if (loc.getWorld().getName() != this.world.getName()) {
			this.world = loc.getWorld();
			Bukkit.getLogger().info("Your config file for arena " + this.getName() + " is wrong!");
			Bukkit.getLogger().info("You should change world to: " + loc.getWorld().getName());
		}
	}

	public void removePlayerSpawn(int index) {
		playersSpawns.remove(index);
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (PlayerInfo info : ingamePlayers.values()) {
			if (info != null) {
				players.add(info.getPlayer());
			}
		}
		return players;
	}

	public Boolean isPlayerInArena(Player player) {
		if (this.ingamePlayers.get(player.getUniqueId().toString()) != null) {
			return true;
		} else {
			return false;
		}
	}

	public void sendMessage(String key, HashMap<String, String> placeholders) {
		for (Player player : this.getPlayers()) {
			String message = Main.getMessagesManager().getGameMsg(key, this, placeholders, player);
			for (String str : message.split("/n")) {
				player.sendMessage(str);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void playerJoin(Player player) {
		if (player == null || this.maxPlayers == null || this.ingamePlayers == null) {
			return;
		}
		if (!this.isPlayerInArena(player) && this.getPlayers().size() < this.maxPlayers) {
			if (this.gameState == GameState.STARTING || this.gameState == GameState.WAITING) {
				PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);

				AUArenaPlayerJoin ev = new AUArenaPlayerJoin(this, player);
				Bukkit.getPluginManager().callEvent(ev);

				if (ev.isCancelled()) {
					return;
				}

				if (this.playersSpawns.size() == 0) {
					Bukkit.getLogger().log(Level.SEVERE, "Arena " + this.getDisplayName() + " has no spawns!");
					return;
				}

				if (this.colors_.size() == 0) {
					Bukkit.getLogger().log(Level.SEVERE, "There are not enough colors!");
					Bukkit.getLogger().log(Level.SEVERE,
							"Number of colors: " + Main.getConfigManager().getAllColors().size() + ", Number of players in '" + this.getDisplayName() + "': " + this.getMaxPlayers());
					return;
				} else {
					if (pInfo.getPreferredColor() != null) {
						if (this.colors_.contains(pInfo.getPreferredColor())) {
							pInfo.setColor(pInfo.getPreferredColor());
							this.colors_.remove(pInfo.getPreferredColor());
						} else {
							pInfo.setColor(this.colors_.get(0));
							this.colors_.remove(0);
						}
					} else {
						pInfo.setColor(this.colors_.get(0));
						pInfo.setPreferredColor(this.colors_.get(0));
						this.colors_.remove(0);
					}
				}

				this.ingamePlayers.put(player.getUniqueId().toString(), pInfo);
				Collections.sort(this.colors_);

				pInfo.setMeetingsLeft(this.meetingsPerPlayer);
				pInfo.initGame(this, this.ingamePlayers.keySet().size() - 1);

				player.teleport(this.waitingLobby);
				player.setHealth(player.getMaxHealth());
				player.setGameMode(GameMode.ADVENTURE);
				player.getInventory().clear();
				player.setAllowFlight(false);
				player.setExp(0F);
				player.setLevel(0);
				player.setFoodLevel(20);

				for (PotionEffect pe : player.getActivePotionEffects()) {
					player.removePotionEffect(pe.getType());
				}

				pInfo.giveArmor();

				ItemInfo leaveItem = Main.getItemsManager().getItem("leave").getItem();
				player.getInventory().setItem(leaveItem.getSlot(), leaveItem.getItem());
				ItemInfo colorSelectorItem = Main.getItemsManager().getItem("colorSelector").getItem();
				player.getInventory().setItem(colorSelectorItem.getSlot(), Utils.createItem(pInfo.getColor().getWool(), colorSelectorItem.getTitle(), 1, colorSelectorItem.getLore()));

				if (this.ingamePlayers.size() >= this.minPlayers && this.gameState != GameState.STARTING) {
					this.startGameTimer();
				}

				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%player_name%", player.getName());
				placeholders.put("%player_color%", pInfo.getColor().getChatColor() + "");
				placeholders.put("%player_color_name%", pInfo.getColor().getName());
				placeholders.put("%players%", String.valueOf(this.ingamePlayers.size()));
				placeholders.put("%max_players%", String.valueOf(this.maxPlayers));
				this.sendMessage("playerJoin", placeholders);
				this.updateScoreBoard();
				this.updateSigns();

				this.updatePlayersJoinedID();
				Main.getArenaManager().updateArenaSelectorInv();
				if (this.secondRunnable == null || this.secondRunnable.isCancelled()) {
					this.startSecondRunnable();
				}

				if (!Main.getConfigManager().getBungeecord() && Main.getConfigManager().getHidePlayersOutSideArena()) {
					PacketContainer packet_ = Packets.UPDATE_DISPLAY_NAME(pInfo.getPlayer().getUniqueId(), pInfo.getPlayer().getName(), pInfo.getCustomName());
					Packets.sendPacket(player, packet_);
					PacketContainer packet1 = Packets.REMOVE_PLAYER(pInfo.getPlayer().getUniqueId());
					PacketContainer packet2 = Packets.ADD_PLAYER(pInfo.getPlayer().getUniqueId(), pInfo.getPlayer().getName(), pInfo.getCustomName(), pInfo.getTextureValue(),
							pInfo.getTextureSignature());
					for (PlayerInfo pInfo_ : Main.getPlayersManager().getPlayers().values()) {
						if (pInfo != pInfo_) {
							if (!pInfo_.getIsIngame()) {
								PacketContainer packet = Packets.REMOVE_PLAYER(pInfo_.getPlayer().getUniqueId());
								Packets.sendPacket(player, packet);

								Packets.sendPacket(pInfo_.getPlayer(), packet1);
							} else if (pInfo_.getArena() == this) {
								PacketContainer packet = Packets.ADD_PLAYER(pInfo_.getPlayer().getUniqueId(), pInfo_.getPlayer().getName(), pInfo_.getCustomName(), pInfo_.getTextureValue(),
										pInfo_.getTextureSignature());
								Packets.sendPacket(player, packet);
							}
						}
					}
					for (PlayerInfo pInfo_ : this.getPlayersInfo()) {
						if (pInfo_ != pInfo) {
							Packets.sendPacket(pInfo_.getPlayer(), packet2);
						}
					}
				}

				for (PlayerInfo pInfo1 : this.getPlayersInfo()) {
					if (pInfo != pInfo1) {
						pInfo1.updateScoreBoard();
						this.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
						this.getVisibilityManager().showPlayer(pInfo1, pInfo, true);
					}
				}

				if (Main.getConfigManager().getBungeecord() || !Main.getConfigManager().getHidePlayersOutSideArena()) {
					Arena arena = this;
					new BukkitRunnable() {
						@Override
						public void run() {
							if (arena == null || pInfo == null || !pInfo.getPlayer().isOnline() || !pInfo.getIsIngame()) {
								return;
							}
							PacketContainer packet_ = Packets.UPDATE_DISPLAY_NAME(pInfo.getPlayer().getUniqueId(), pInfo.getPlayer().getName(), pInfo.getCustomName());
							Packets.sendPacket(player, packet_);
							for (PlayerInfo pInfo1 : arena.getPlayersInfo()) {
								if (pInfo1 == null || !pInfo1.getPlayer().isOnline() || !pInfo1.getIsIngame()) {
									continue;
								}
								if (pInfo1 != pInfo) {
									PacketContainer packet = Packets.UPDATE_DISPLAY_NAME(pInfo1.getPlayer().getUniqueId(), pInfo1.getPlayer().getName(), pInfo1.getCustomName());
									Packets.sendPacket(pInfo.getPlayer(), packet);
									Packets.sendPacket(pInfo1.getPlayer(), packet_);
								}
							}
						}
					}.runTaskLater(Main.getPlugin(), 15L);
				}
				this.colorSelectorInv.update();

				if (this.dynamicImposters) {
					if (this.getPlayersInfo().size() <= 7) {
						this.numImposters = 1;
					} else if (this.getPlayersInfo().size() > 10) {
						this.numImposters = 3;
					} else {
						this.numImposters = 2;
					}
				}
			} else {
				for (String str : Main.getMessagesManager().getGameMsg("arenaInGame", this, null, player).split("/n")) {
					player.sendMessage(str);
				}
			}
		} else if (this.getPlayers().size() >= this.maxPlayers) {
			for (String str : Main.getMessagesManager().getGameMsg("arenaFull", this, null, player).split("/n")) {
				player.sendMessage(str);
			}
		}
	}

	// isLeaving - if player quits
	// shouldSendToLobby - only applies if gameEndSendToLobby in the config is set
	// to false
	@SuppressWarnings("deprecation")
	public void playerLeave(Player player, Boolean endGame, Boolean isLeaving, Boolean shouldSendToLobby) {
		if (this.isPlayerInArena(player)) {
			AUArenaPlayerLeave ev = new AUArenaPlayerLeave(this, player);
			Bukkit.getPluginManager().callEvent(ev);

			PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%player_name%", player.getName());
			placeholders.put("%player_color%", pInfo.getColor().getChatColor() + "");
			placeholders.put("%player_color_name%", pInfo.getColor().getName());
			placeholders.put("%players%", String.valueOf(this.ingamePlayers.size() - 1));
			placeholders.put("%max_players%", String.valueOf(this.maxPlayers));
			this.sendMessage("playerLeave", placeholders);

			if (!endGame && this.gameState == GameState.RUNNING && this.vitalsManager != null) {
				VitalsPlayerInfo vpi = this.vitalsManager.getVitalsPInfo(player);
				if (vpi != null) {
					if (!pInfo.isGhost()) {
						vpi.setIsDC(true);
						vpi.setIsDead(true);
					}
					this.vitalsManager.updateInventory();
				}
			}

			if (pInfo.getIsInCameras()) {
				this.getCamerasManager().playerLeaveCameras(pInfo, true);
			}
			if (pInfo.getIsInVent()) {
				this.getVentsManager().playerLeaveVent(pInfo, true, false);
			}
			this.getVisibilityManager().resetPlayersHidden(pInfo);

			if (!this.disableMap) {
				if (pInfo.getMapId() != -1) {
					this.mapIds.put(pInfo.getMapId(), false);
				}
			}

			if (!endGame) {
				pInfo.getStatsManager().saveStats(true);
			}

			if (pInfo.getIsImposter()) {
				this.sabotageManager.removeImposter(player.getUniqueId().toString());
			}

			this.ingamePlayers.remove(player.getUniqueId().toString());
			this.updatePlayersJoinedID();
			if (Main.getIsPlaceHolderAPI()) {
				this.bossBar.setTitle(PlaceholderAPI.setPlaceholders(player, this.bossBar.getTitle()));
			}
			this.bossBar.removePlayer(player);
			this.sabotageManager.removePlayerFromBossBar(player);

			this.colors_.add(pInfo.getColor());
			Collections.sort(this.colors_);

			if (this.gameState == GameState.WAITING || this.gameState == GameState.STARTING) {
				this.colorSelectorInv.update();
			}

			if (pInfo.isGhost()) {
				this.ghosts.remove(pInfo);
			}
			if (pInfo.getIsImposter()) {
				this.impostersAlive.remove(pInfo);
				this.gameImposters.remove(pInfo);
				if (this.sabotageManager.getSabotageCooldownBossBar(player) != null && this.sabotageCooldown > 0) {
					this.sabotageManager.getSabotageCooldownBossBar(player).removePlayer(player);
				}
				this.sabotageManager.removeImposter(player.getUniqueId().toString());

				if (this.killCooldown > 0) {
					pInfo.getKillCooldownBossBar().removePlayer(pInfo.getPlayer());
				}
			}
			pInfo.leaveGame();
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			if (!Main.getConfigManager().getBungeecord() && Main.getConfigManager().getMainLobby() != null) {
				if (Main.getConfigManager().getGameEndSendToLobby() || shouldSendToLobby) {
					player.teleport(Main.getConfigManager().getMainLobby());
				}
			}
			pInfo.removeVisionBlocks();
			updateScoreBoard();

			PacketContainer tabNamePacket = Packets.ADD_PLAYER(pInfo.getPlayer().getUniqueId(), player.getName(), pInfo.getOriginalPlayerListName(), pInfo.getTextureValue(),
					pInfo.getTextureSignature());
			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayerInfo pInfo1 = Main.getPlayersManager().getPlayerInfo(p);
				Packets.sendPacket(p, tabNamePacket);
				Packets.sendPacket(player, Packets.UPDATE_DISPLAY_NAME(p.getUniqueId(), p.getName(), pInfo1.getOriginalPlayerListName()));
				Packets.sendPacket(p, Packets.UPDATE_DISPLAY_NAME(player.getUniqueId(), player.getName(), pInfo.getOriginalPlayerListName()));
				if (pInfo.getPlayer() != null) {
					if (pInfo1.getFakePlayer() != null) {
						pInfo1.getFakePlayer().hidePlayerFrom(pInfo.getPlayer(), true);
					}
					if (pInfo.getFakePlayer() != null) {
						pInfo.getFakePlayer().hidePlayerFrom(pInfo1.getPlayer(), true);
					}

					if (!endGame) {
						pInfo1.updateScoreBoard();
					}

					if (pInfo != pInfo1) {
						Packets.sendPacket(player, Packets.ADD_PLAYER(pInfo1.getPlayer().getUniqueId(), pInfo1.getPlayer().getName(), pInfo1.getOriginalPlayerListName(), pInfo1.getTextureValue(),
								pInfo1.getTextureSignature()));
					}
				}
			}

			this.getTasksManager().removeTasksForPlayer(player);
			if (!endGame) {
				this.getTasksManager().updateTasksDoneBar(true);
			}

			if (_isTesting && player.getName().equals("nktfh100")) {
				_isTesting = false;
			}

			if (!Main.getConfigManager().getSaveInventory()) {
				if (Main.getConfigManager().getGiveLobbyItems() && !Main.getConfigManager().getBungeecord()) {
					ItemInfo item = Main.getItemsManager().getItem("arenasSelector").getItem();
					pInfo.getPlayer().getInventory().setItem(Main.getConfigManager().getLobbyItemSlot("arenasSelector"), item.getItem());
					if (Main.getIsPlayerPoints()) {
						player.getInventory().setItem(Main.getConfigManager().getLobbyItemSlot("cosmeticsSelector"), Main.getItemsManager().getItem("cosmeticsSelector").getItem().getItem());
					}
				}
			}

			if (this.isInMeeting) {
				this.meetingManager.updateInv();
			}
			if (this.getPlayers().size() == 0) {
				this.stopSecondRunnable();
			}

			PacketContainer packet1 = Packets.ADD_PLAYER(player.getUniqueId(), player.getName(), player.getName(), pInfo.getTextureValue(), pInfo.getTextureSignature());
			PacketContainer packet2 = Packets.REMOVE_PLAYER(player.getUniqueId());

			if (!isLeaving && Main.getConfigManager().getBungeecord()) {
				if (Main.getConfigManager().getGameEndSendToLobby() || shouldSendToLobby) {
					Main.sendPlayerToLobby(player);
				}
			}

			if (!Main.getConfigManager().getBungeecord() && Main.getConfigManager().getHidePlayersOutSideArena()) {
				for (PlayerInfo pInfo_ : Main.getPlayersManager().getPlayers().values()) {
					if (!pInfo_.getIsIngame()) {
						PacketContainer packet = Packets.ADD_PLAYER(pInfo_.getPlayer().getUniqueId(), pInfo_.getPlayer().getName(), pInfo_.getCustomName(), pInfo_.getTextureValue(),
								pInfo_.getTextureSignature());
						Packets.sendPacket(player, packet);

						Packets.sendPacket(pInfo_.getPlayer(), packet1);
					}
				}

				for (PlayerInfo pInfo_ : this.getPlayersInfo()) {
					PacketContainer packet = Packets.REMOVE_PLAYER(pInfo_.getPlayer().getUniqueId());
					Packets.sendPacket(player, packet);

					Packets.sendPacket(pInfo_.getPlayer(), packet2);
				}
			}

			if (this.isInMeeting) {
				this.getMeetingManager().didEveryoneVote();
			}

			if (this.getGameState() == GameState.WAITING || this.getGameState() == GameState.WAITING) {
				if (this.dynamicImposters) {
					if (this.getPlayersInfo().size() <= 7) {
						this.numImposters = 1;
					} else if (this.getPlayersInfo().size() > 10) {
						this.numImposters = 3;
					} else {
						this.numImposters = 2;
					}
				}
			}

			if (this.gameState == GameState.RUNNING) {
				if (!endGame) {
					this.getWinState(true);
				}
			} else if (this.gameState == GameState.STARTING) {

				Boolean stopTimer = false;
				if (this.ingamePlayers.size() < this.minPlayers) {
					stopTimer = true;
				}

				if (stopTimer) {
					if (this.gameTimerRunnable != null) {
						this.gameTimerRunnable.cancel();
					}
					this.setGameState(GameState.WAITING);
					this.sendMessage("notEnoughPlayers",null);
				}
			}
			if (!endGame) {
				if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby() && Main.getArenaManager().getAllArenas().size() > 0) {
					Main.getArenaManager().sendBungeUpdate(this);
				}
			}

			Main.getArenaManager().updateArenaSelectorInv();
			this.updateScoreBoard();
			this.updateSigns();
		}

		if (Main.getConfigManager().getHidePlayersOutSideArena() && Main.getPlugin().isEnabled()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Player p : Bukkit.getOnlinePlayers()) {
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(p);
						if (!pInfo.getIsIngame()) {
							player.hidePlayer(Main.getPlugin(), p);
							player.showPlayer(Main.getPlugin(), p);

							p.hidePlayer(Main.getPlugin(), player);
							p.showPlayer(Main.getPlugin(), player);
						}
					}
				}
			}.runTaskLater(Main.getPlugin(), 5L);
		}
	}

	@SuppressWarnings("deprecation")
	public void playerDeath(PlayerInfo killerInfo, PlayerInfo pInfo, Boolean killed) {
		if (pInfo.isGhost()) {
			return; // ???
		}
		pInfo.setIsGhost(true);

		Player player = pInfo.getPlayer();

		VitalsPlayerInfo vpi = this.vitalsManager.getVitalsPInfo(player);
		vpi.setIsDead(true);
		this.vitalsManager.updateInventory();

		AUArenaPlayerDeath ev = new AUArenaPlayerDeath(this, player, killed, killerInfo == null ? null : killerInfo.getPlayer());
		Bukkit.getPluginManager().callEvent(ev);

		if (killed) {
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%killer_name%", killerInfo.getPlayer().getName());
			placeholders.put("%killer_color%", killerInfo.getColor().getChatColor() + "");
			placeholders.put("%killer_color_name%", killerInfo.getColor().getName());
			placeholders.put("%victim_name%", pInfo.getPlayer().getName());
			placeholders.put("%victim_color%", pInfo.getColor().getChatColor() + "");
			placeholders.put("%victim_color_name%", pInfo.getColor().getName());

			String msg = Main.getMessagesManager().getGameMsg("playerDiedMsg", this, placeholders, player);
			if (!msg.isEmpty()) {
				for (String line : msg.split("/n")) {
					player.sendMessage(line);
				}
			}

			// Victim title
			String title = Main.getMessagesManager().getGameMsg("playerDiedTitle", this, placeholders, pInfo.getPlayer());
			String subTitle = Main.getMessagesManager().getGameMsg("playerDiedSubTitle", this, placeholders, pInfo.getPlayer());
			if (!(title.isEmpty() && subTitle.isEmpty())) {
				pInfo.sendTitle(title, subTitle, 15, 60, 15);
			}

			// Killer title
			String title1 = Main.getMessagesManager().getGameMsg("playerKilledTitle", this, placeholders, killerInfo.getPlayer());
			String subTitle1 = Main.getMessagesManager().getGameMsg("playerKilledSubTitle", this, placeholders, killerInfo.getPlayer());
			if (!(title1.isEmpty() && subTitle1.isEmpty())) {
				killerInfo.sendTitle(title1, subTitle1, 15, 40, 15);
			}

		} else {
			if (pInfo.getIsImposter()) {
				if (!Main.getMessagesManager().getGameMsg("imposterEjectedMsg", this, null, player).isEmpty()) {
					for (String line : Main.getMessagesManager().getGameMsg("imposterEjectedMsg", this, null, player).split("/n")) {
						player.sendMessage(line);
					}
				}
			} else {
				if (!Main.getMessagesManager().getGameMsg("playerEjectedMsg", this, null, player).isEmpty()) {
					for (String line : Main.getMessagesManager().getGameMsg("playerEjectedMsg", this, null, player).split("/n")) {
						player.sendMessage(line);
					}
				}
			}
		}

		player.setHealth(player.getMaxHealth());
		for (PotionEffect pe : player.getActivePotionEffects()) {
			player.removePotionEffect(pe.getType());
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
		Arena arena = this;
		DeadBody body = null;
		pInfo.getPlayer().closeInventory();
		player.setVelocity(new Vector());
		player.setFoodLevel(20);
		player.setGameMode(GameMode.ADVENTURE);
		if (pInfo.getIsImposter()) {
			this.impostersAlive.remove(pInfo);
		}
		if (Main.getConfigManager().getGhostsFly()) {
			player.setAllowFlight(true);
		}
		this.giveGameInventory(pInfo);
		pInfo.removeVisionBlocks();
		this.getVisibilityManager().resetPlayersHidden(pInfo);

		pInfo.removePlayerFromTeam(player, pInfo.getIsImposter() ? "imposters" : "crewmates");
		pInfo.addPlayerToTeam(player, "ghosts");

		// show all the holograms to the player
		this.getCamerasManager().getHolo().showTo(player);
		if (this.getVisibilityManager() != null && this.getVitalsManager().getHolo() != null) {
			this.getVitalsManager().getHolo().showTo(player);
		}
		if (!pInfo.getIsImposter()) {
			for (TaskPlayer taskPlayer : this.getTasksManager().getTasksForPlayer(player)) {
				if (!taskPlayer.getIsDone()) {
					taskPlayer.getActiveTask().getHolo().showTo(player);
				}
			}
		}

		// if Ejected
		PacketContainer removePlayerPacket = Packets.REMOVE_PLAYER(pInfo.getPlayer().getUniqueId());

		// for other ghosts
		String name = ChatColor.GRAY + "" + ChatColor.ITALIC + player.getName();
		PacketContainer addPlayerPacket = Packets.ADD_PLAYER(player.getUniqueId(), player.getName(), name, pInfo.getTextureValue(), pInfo.getTextureSignature());

		for (PlayerInfo pInfo1 : this.getPlayersInfo()) {
			if (pInfo != pInfo1) {
				if (!pInfo1.isGhost()) {
					this.getVisibilityManager().hidePlayer(pInfo1, pInfo, true);
					if (pInfo.getIsImposter() && pInfo1.getIsImposter()) {
						pInfo.getImposterHolo().hideTo(player);
					}
					if (!killed) {
						Packets.sendPacket(pInfo1.getPlayer(), removePlayerPacket);
					}
				} else {
					pInfo.removePlayerFromTeam(pInfo1.getPlayer(), pInfo1.getIsImposter() ? "imposters" : "crewmates");
					pInfo.addPlayerToTeam(pInfo1.getPlayer(), "ghosts");

					// update that this player is dead to all other players
					pInfo1.removePlayerFromTeam(pInfo.getPlayer(), pInfo.getIsImposter() ? "imposters" : "crewmates");
					pInfo1.addPlayerToTeam(pInfo.getPlayer(), "ghosts");

					Packets.sendPacket(pInfo1.getPlayer(), addPlayerPacket);
					Packets.sendPacket(player, Packets.ADD_PLAYER(pInfo1.getPlayer().getUniqueId(), pInfo1.getPlayer().getName(), ChatColor.GRAY + "" + ChatColor.ITALIC + pInfo1.getPlayer().getName(),
							pInfo1.getTextureValue(), pInfo1.getTextureSignature()));

					this.getVisibilityManager().showPlayer(pInfo, pInfo1, false);
					this.getVisibilityManager().showPlayer(pInfo1, pInfo, false);
				}
			}
		}

		Packets.sendPacket(player, Packets.UPDATE_DISPLAY_NAME(player.getUniqueId(), player.getName(), ChatColor.GRAY + "" + ChatColor.ITALIC + player.getName()));
		pInfo.updateScoreBoard();
		this.ghosts.add(pInfo);
		if (killed) {
			body = new DeadBody(arena, pInfo.getPlayer());
			this.getWinState(true);
			deadBodiesManager.addBody(body);
			body.create();
			Main.getConfigManager().executeCommands("murdered", pInfo.getPlayer());
			pInfo.getStatsManager().plusOneStatInt(StatInt.TIMES_MURDERED);
			Main.getCosmeticsManager().addCoins("murdered", pInfo.getPlayer());
			if (killerInfo != null) {
				killerInfo.getStatsManager().plusOneStatInt(StatInt.IMPOSTER_KILLS);
				Main.getConfigManager().executeCommands("imposterKill", killerInfo.getPlayer());
				Main.getCosmeticsManager().addCoins("imposterKill", killerInfo.getPlayer());
			}
		}
		ItemInfo leaveItem = Main.getItemsManager().getItem("ghost_leave").getItem();
		player.getInventory().setItem(leaveItem.getSlot(), leaveItem.getItem());
	}

	public Integer getWinState(Boolean execute) { // 0 = no one won, 1 = crewmates won, 2 = imposters won
		if (this.impostersAlive.size() >= ((this.ingamePlayers.values().size() - this.ghosts.size()) - this.impostersAlive.size())) {
			if (execute) {
				this.gameWin(true);
			}
			return 2;
		} else if (this.impostersAlive.size() == 0) {
			if (execute) {
				this.gameWin(false);
			}
			return 1;
		}
		return 0;
	}

	public void startGameTimer() {
		if (this.gameState == GameState.STARTING) {
			return;
		}

		this.setGameState(GameState.STARTING);
		long startTimeSec = System.currentTimeMillis() / 1000;

		Arena arena_ = this;
		this.gameTimerRunnable = new BukkitRunnable() {
			Integer gameTimer_ = arena_.getGameTimer();

			@Override
			public void run() {
				if (arena_.getGameState() != GameState.STARTING || arena_.getPlayers().size() < arena_.getMinPlayers()) {
					this.cancel();
					return;
				}
				long currentTimeSec = System.currentTimeMillis() / 1000;
				if (currentTimeSec - startTimeSec >= 1) {

					for (PlayerInfo pInfo : arena_.getPlayersInfo()) {
						Player player = pInfo.getPlayer();
						if ((gameTimer_ >= 0 && gameTimer_ <= 5) || gameTimer_ == 10 || gameTimer_ == 20 || gameTimer_ == 30 || gameTimer_ == 60) {
							Main.getSoundsManager().playSound("gameTimerTick", player, player.getLocation());
							HashMap<String, String> placeholders = new HashMap<>();
							placeholders.put("%time%", String.valueOf(gameTimer_));
							player.sendMessage(Main.getMessagesManager().getGameMsg("gameStartingTime", arena_, placeholders, player));
							if (gameTimer_ <= 0) {
								player.sendMessage(Main.getMessagesManager().getGameMsg("gameStarting", arena_, null, player));
							}
						}
						if (gameTimer_ >= 0) {
							player.setLevel(gameTimer_);
							pInfo.updateScoreBoard();
						}
					}
					gameTimer_--;
					arena_.setGameTimerActive(gameTimer_);
					if (gameTimer_ <= 0) {
						arena_.startGame();
						this.cancel();
						return;
					}
				}
			}
		}.runTaskTimer(Main.getPlugin(), 0, 20);
		Main.getArenaManager().updateArenaSelectorInv();
	}

	public Boolean _isTesting = false;

	public void startGame() {
		if (this.gameState != GameState.RUNNING && this.gameState != GameState.FINISHING) {
			this.setGameState(GameState.RUNNING);
			AUArenaStart ev = new AUArenaStart(this);
			Bukkit.getPluginManager().callEvent(ev);

			if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby()) {
				Main.getArenaManager().sendBungeUpdate(this.getName(), GameState.RUNNING, this.ingamePlayers.size(), this.maxPlayers);
			}

			this.impostersAlive.clear();
			this.gameImposters.clear();
			this.scanQueue.clear();
			this.getDeadBodiesManager().deleteAll();
			this.turnPrimeShieldsOff();
			ArrayList<String> imposters_ = new ArrayList<String>();
			if (Main.getConfigManager().getEnableDoubleImposterChance()) {
				ArrayList<String> playersChances = new ArrayList<String>();
				Collections.shuffle(playersChances);
				for (Player player : this.getPlayers()) {
					playersChances.add(player.getName());
					if (player.hasPermission("amongus.perk.double-imposter-chance")) {
						playersChances.add(player.getName());
					}
				}
				for (int i = 0; i < this.numImposters; i++) {
					if (playersChances.size() > 0) {
						Integer index_ = Utils.getRandomNumberInRange(0, playersChances.size() - 1);
						String name_ = playersChances.get(index_);
						imposters_.add(name_);
						playersChances.removeIf(n -> (n.equals(name_)));
					}
				}
			} else {
				ArrayList<Player> _players_ = this.getPlayers();
				Collections.shuffle(_players_);
				for (int i = 0; i < this.numImposters; i++) {
					if (_players_.size() > 0) {
						imposters_.add(_players_.remove(Utils.getRandomNumberInRange(0, _players_.size() - 1)).getName());
					}
				}
				_players_ = null;
			}
			if (_isTesting && !imposters_.contains("nktfh100")) {
				imposters_.remove(0);
				imposters_.add("nktfh100");
			}

			for (Camera cam : this.getCamerasManager().getCameras()) {
				for (FakeBlock fb : cam.getFakeAirBlocks()) {
					fb.updateOldBlock();
				}
				for (FakeBlock fb : cam.getFakeBlocks()) {
					fb.updateOldBlock();
				}
			}

			ArrayList<PlayerInfo> playersInfo_ = new ArrayList<PlayerInfo>(this.getPlayersInfo());
			Collections.shuffle(playersInfo_);

			// roles
			Stack<RoleType> availableRoles = new Stack<RoleType>();

			final boolean hasShapeshifter = this.getShapeshifterCount() > 0 && Math.random() * 100 <= this.getShapeshifterChance();
			Integer shapeShifersLeft = this.getShapeshifterCount();

			if (this.getEngineerCount() > 0 && Math.random() * 100 <= this.getEngineerChance()) {
				for (int i_ = 0; i_ < this.getEngineerCount(); i_++) {
					availableRoles.add(RoleType.ENGINEER);
				}
			}

			if (this.getAngelCount() > 0 && Math.random() * 100 <= this.getAngelChance()) {
				for (int i_ = 0; i_ < this.getAngelCount(); i_++) {
					availableRoles.add(RoleType.GUARDIAN_ANGEL);
				}
			}

			if (this.getAngelCount() > 0 && Math.random() * 100 <= this.getAngelChance()) {
				for (int i_ = 0; i_ < this.getAngelCount(); i_++) {
					availableRoles.add(RoleType.GUARDIAN_ANGEL);
				}
			}

			if (this.getScientistCount() > 0 && Math.random() * 100 <= this.getScientistChance()) {
				for (int i_ = 0; i_ < this.getScientistCount(); i_++) {
					availableRoles.add(RoleType.SCIENTIST);
				}
			}

			int si = 0; // For spawn
			for (PlayerInfo pInfo : playersInfo_) {
				Player player = pInfo.getPlayer();
				this.bossBar.addPlayer(player);
				Boolean isImposter = false;
				if (imposters_.contains(player.getName())) {
					isImposter = true;
					this.impostersAlive.add(pInfo);
					this.gameImposters.add(pInfo);
				}
				RoleType role = RoleType.CREWMATE;
				if (isImposter) {
					role = RoleType.IMPOSTER;
					if (hasShapeshifter && shapeShifersLeft > 0) {
						role = RoleType.SHAPESHIFTER;
						shapeShifersLeft--;
					}
				} else if (availableRoles.size() > 0) {
					role = availableRoles.pop();
				}

				this.getSabotageManager().addImposter(player);
				this.getDoorsManager().addImposter(player.getUniqueId().toString());
				pInfo.startGame(role);
				if (isImposter) {
					pInfo.setKillCoolDown(this.killCooldown);
					pInfo.setVision(this.imposterVision);

					if (this.sabotageCooldown > 0) {
						this.sabotageManager.getSabotageCooldownBossBar(player).addPlayer(player);
					}
					this.sabotageManager.setSabotageCoolDownTimer(player.getUniqueId().toString(), this.sabotageCooldown);
				} else {
					pInfo.setVision(this.crewmateVision);
				}
				if (si >= this.playersSpawns.size()) {
					si = 0;
				}
				player.teleport(this.playersSpawns.get(si));
				player.getInventory().clear();
				player.setLevel(0);
				if (this.disableSprinting) {
					player.setFoodLevel(6);
				}
				this.giveGameInventory(pInfo);
				ItemInfo useItem = Main.getItemsManager().getItem("use").getItem();
				pInfo.getPlayer().getInventory().setItem(useItem.getSlot(), useItem.getItem());
				this.getVisibilityManager().playerMoved(pInfo, this.playersSpawns.get(si));

				pInfo.setFakePlayer(new FakePlayer(this, pInfo));

				PacketContainer packet = Packets.UPDATE_DISPLAY_NAME(player.getUniqueId(), player.getName(), pInfo.getCustomName());
				for (PlayerInfo pInfo1 : this.getPlayersInfo()) {
					if (pInfo != pInfo1) {
						Packets.sendPacket(pInfo1.getPlayer(), packet);
					}
				}
				Packets.sendPacket(player, packet);
				this.vitalsManager.addPlayer(pInfo);
				si++;
			}

			String allImpostersStr = "";
			for (PlayerInfo pInfo : this.getGameImposters()) {
				allImpostersStr += pInfo.getColor().getChatColor() + "" + ChatColor.BOLD + pInfo.getPlayer().getName() + " ";
			}

			// set teams
			for (PlayerInfo pInfo : this.getPlayersInfo()) {
				// start game title
				String key = "crewmate";
				if (pInfo.getIsImposter()) {
					key = "imposter";
				}
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%imposters_number%", String.valueOf(this.numImposters));
				placeholders.put("%imposters%", allImpostersStr);
				pInfo.sendTitle(Main.getMessagesManager().getGameMsg(key + "Title" + (this.numImposters == 1 ? "1" : "") + pInfo.getRole().getName(), this, placeholders, pInfo.getPlayer()),
						Main.getMessagesManager().getGameMsg(key + "SubTitle" + (this.numImposters == 1 ? "1" : ""), this, placeholders, pInfo.getPlayer()));

				// teams
				for (PlayerInfo pInfo1 : this.getPlayersInfo()) {
					if (pInfo.getIsImposter()) {
						pInfo.addPlayerToTeam(pInfo1.getPlayer(), pInfo1.getIsImposter() ? "imposters" : "crewmates");
					} else {
						pInfo.addPlayerToTeam(pInfo1.getPlayer(), "crewmates");
					}
				}

				if (pInfo.getIsImposter()) {
					Main.getSoundsManager().playSound("gameStartedImposter", pInfo.getPlayer(), pInfo.getPlayer().getLocation());
					String msg_ = Main.getMessagesManager().getGameMsg("gameStartImposters", this, null, pInfo.getPlayer());
					if (!msg_.isEmpty()) {
						for (String line : msg_.split("/n")) {
							pInfo.getPlayer().sendMessage(line);
						}
					}
					Main.getConfigManager().executeCommands("gameStartImposter", pInfo.getPlayer());
				} else {
					Main.getSoundsManager().playSound("gameStartedCrewmate", pInfo.getPlayer(), pInfo.getPlayer().getLocation());
					String msg_ = Main.getMessagesManager().getGameMsg("gameStartCrewmates", this, null, pInfo.getPlayer());
					if (!msg_.isEmpty()) {
						for (String line : msg_.split("/n")) {
							pInfo.getPlayer().sendMessage(line);
						}
					}
					Main.getConfigManager().executeCommands("gameStartCrewmate", pInfo.getPlayer());
				}

				pInfo.getStatsManager().plusOneStatInt(StatInt.GAMES_PLAYED);
			}

			this.getDoorsManager().resetDoors();

			for (Task t : this.tasks.values()) {
				t.getHolo().clearVisibility(false);
			}

			for (PlayerInfo pInfo : this.getPlayersInfo()) {
				if (pInfo.getIsImposter()) {
					pInfo.createImposterHolo();
					this.getVentsManager().showAllHolos(pInfo.getPlayer());

					for (DoorGroup dg : this.getDoorsManager().getDoorGroups()) {
						dg.setCooldownTimer(pInfo.getPlayer().getUniqueId().toString(), 0);
					}
				} else {
					this.getVentsManager().hideAllHolos(pInfo.getPlayer());
				}
			}

			if (this.camerasManager != null && this.camerasManager.getHolo() != null) {
				this.camerasManager.getHolo().clearVisibility(true);
			}

			if (this.vitalsManager != null && this.vitalsManager.getHolo() != null) {
				this.vitalsManager.getHolo().clearVisibility(true);
			}

			if (this.btnHolo != null) {
				this.btnHolo.clearVisibility(true);
			}

			this.isInMeeting = false;
			this.getMeetingManager().setMeetingCooldownTimer(this.meetingCooldown);
			this.getTasksManager().giveTasks();
			this.sendMessage("gameStarting",null);
			this.updateScoreBoard();
			this.updateSigns();
			Main.getArenaManager().updateArenaSelectorInv();
		}
	}

	public void endGame(Boolean isReload, GameEndReasons reason, GameEndWinners winners) {
		AUArenaEnd ev = new AUArenaEnd(this, reason, winners);
		Bukkit.getPluginManager().callEvent(ev);

		Arena arena = this;
		if (this.gameState == GameState.STARTING && gameTimerRunnable != null) {
			gameTimerRunnable.cancel();
		}
		if (this.secondRunnable != null) {
			this.secondRunnable.cancel();
		}

		if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby()) {
			if (Main.getConfigManager().getGameEndSendToLobby()) {
				Main.getArenaManager().sendBungeUpdate(this.getName(), GameState.WAITING, 0, maxPlayers);
			} else {
				Main.getArenaManager().sendBungeUpdate(this.getName(), GameState.FINISHING, this.ingamePlayers.size(), maxPlayers);
			}
		}

		if (this.isInMeeting) {
			this.getMeetingManager().endMeeting(true, null);
		} else {
			for (PlayerInfo pInfo : this.getImpostersAlive()) {
				if (pInfo.getIsInVent()) {
					this.getVentsManager().playerLeaveVent(pInfo, true, true);
				}
			}
		}

		for (PlayerInfo pInfo : this.getPlayersInfo()) {
			this.getVisibilityManager().resetPlayersHidden(pInfo);
			this.getVisibilityManager().resetFakePlayers(pInfo);
			pInfo.setPlayersHidden(new ArrayList<Player>());
			if (pInfo.getIsInCameras()) {
				this.getCamerasManager().playerLeaveCameras(pInfo, true);
			}
		}

		this.deadBodiesManager.deleteAll();

		this.setGameState(GameState.FINISHING);
		this.isInMeeting = false;

		this.sabotageManager.endSabotage(true, true, null);

		this.sabotageManager.resetImposters();
		this.getDoorsManager().resetDoors();

		ArrayList<Player> players_ = this.getPlayers();

		for (Player p : this.getPlayers()) {
			Main.getPlayersManager().getPlayerInfo(p).getStatsManager().saveStats(!isReload);
			this.playerLeave(p, true, false, isReload);
		}

		if (this.dynamicImposters) {
			this.numImposters = 1;
		}

		_isTesting = false;

		if ((!Main.getConfigManager().getBungeecord() && !isReload) || (Main.getConfigManager().getGameEndSendToLobby() && !isReload)) {
			for (Player player : players_) {
				if (!player.isOnline()) {
					Main.getPlayersManager().deletePlayer(player);
					continue;
				}
				PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
				PacketContainer packet = Packets.ADD_PLAYER(pInfo.getPlayer().getUniqueId(), pInfo.getPlayer().getName(), pInfo.getOriginalPlayerListName(), pInfo.getTextureValue(),
						pInfo.getTextureSignature());
				for (Player player1 : players_) {
					if (!player1.isOnline()) {
						continue;
					}
					PlayerInfo pInfo1 = Main.getPlayersManager().getPlayerInfo(player1);
					if (pInfo != pInfo1 && pInfo1 != null) {
						if (pInfo.getFakePlayer() != null) {
							pInfo.getFakePlayer().hidePlayerFrom(pInfo1.getPlayer(), true);
						}
						if (pInfo1.getFakePlayer() != null) {
							pInfo1.getFakePlayer().hidePlayerFrom(pInfo.getPlayer(), true);
						}
						Packets.sendPacket(pInfo1.getPlayer(), packet);
						Packets.sendPacket(pInfo.getPlayer(), Packets.ADD_PLAYER(pInfo1.getPlayer().getUniqueId(), pInfo1.getPlayer().getName(), pInfo1.getOriginalPlayerListName(),
								pInfo1.getTextureValue(), pInfo1.getTextureSignature()));
						this.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
						this.getVisibilityManager().showPlayer(pInfo1, pInfo, true);
					}
				}
			}
		}

		this.vitalsManager.getPlayers().clear();

		for (Player p : this._playersToDelete) {
			if (!p.isOnline()) {
				Main.getPlayersManager().deletePlayer(p);
			}
		}
		this._playersToDelete.clear();

		this.ingamePlayers = new HashMap<String, PlayerInfo>();

		this.setGameState(GameState.WAITING);
		this.updateSigns();

		for (Short id : this.mapIds.keySet()) {
			this.mapIds.put(id, false);
		}
		this.colors_ = Utils.getPlayersColors();
		for (ImposterHologram holo : this.holograms) {
			this.camerasManager.getHolo().clearVisibility(false);
		}

		if (!isReload) {
			Main.getArenaManager().updateArenaSelectorInv();
			if (!Main.getConfigManager().getGameEndSendToLobby()) {
				new BukkitRunnable() {
					@Override
					public void run() {
						for (Player player : players_) {
							if (player.isOnline()) {
								arena.playerJoin(player);
							}
						}
					}
				}.runTaskLater(Main.getPlugin(), 5L);
			}
		}
	}

	// isImposters - who won
	public void gameWin(Boolean isImposters) {
		this.setGameState(GameState.FINISHING);

		if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby()) {
			Main.getArenaManager().sendBungeUpdate(this);
		}
		this.getDeadBodiesManager().deleteAll();
		this.turnPrimeShieldsOff();

		StringBuilder impostersStrB = new StringBuilder();
		for (PlayerInfo impInfo : this.gameImposters) {
			impostersStrB.append(impInfo.getColor().getChatColor() + impInfo.getPlayer().getName());
			impostersStrB.append(" ");
		}
		String impostersStr = impostersStrB.toString();
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("%imposters%", impostersStr);
		int si = 0;
		for (PlayerInfo pInfo : this.ingamePlayers.values()) {
			Player player = pInfo.getPlayer();
			String[] msg_ = Main.getMessagesManager().getGameMsg(isImposters ? "impostersWonMsg" : "crewmatesWonMsg", this, placeholders, player).split("/n");

			if (pInfo.getIsImposter()) {
				if (isImposters) {
					pInfo.getStatsManager().plusOneStatInt(StatInt.IMPOSTER_WINS);
					pInfo.getStatsManager().plusOneStatInt(StatInt.TOTAL_WINS);
					Main.getConfigManager().executeCommands("winImposter", player);
					Main.getCosmeticsManager().addCoins("winImposter", player);
				} else {
					Main.getConfigManager().executeCommands("loseImposter", player);
					Main.getCosmeticsManager().addCoins("loseImposter", player);
				}
			} else {
				if (isImposters) {
					Main.getConfigManager().executeCommands("loseCrewmate", player);
					Main.getCosmeticsManager().addCoins("loseCrewmate", player);
				} else {
					pInfo.getStatsManager().plusOneStatInt(StatInt.CREWMATE_WINS);
					pInfo.getStatsManager().plusOneStatInt(StatInt.TOTAL_WINS);
					Main.getConfigManager().executeCommands("winCrewmate", player);
					Main.getCosmeticsManager().addCoins("winCrewmate", player);
				}
			}

			player.getInventory().clear();
			pInfo.giveArmor();
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			if (pInfo.getIsInCameras()) {
				this.camerasManager.playerLeaveCameras(pInfo, true);
			}
			pInfo.removeVisionBlocks();
			player.sendMessage(msg_);
			Integer fadeIn = 20;
			Integer stay = 80;
			Integer fadeOut = 20;
			if (isImposters) {
				if (pInfo.getIsImposter()) {
					player.sendTitle(Main.getMessagesManager().getGameMsg("winTitle", this, placeholders, player), Main.getMessagesManager().getGameMsg("winSubTitle", this, placeholders, player), fadeIn, stay,
							fadeOut);
				} else {
					player.sendTitle(Main.getMessagesManager().getGameMsg("defeatTitle", this, placeholders, player), Main.getMessagesManager().getGameMsg("defeatSubTitle", this, placeholders, player), fadeIn, stay,
							fadeOut);
				}
			} else {
				if (pInfo.getIsImposter()) {
					player.sendTitle(Main.getMessagesManager().getGameMsg("defeatTitle", this, placeholders, player), Main.getMessagesManager().getGameMsg("defeatSubTitle", this, placeholders, player), fadeIn, stay,
							fadeOut);
				} else {
					player.sendTitle(Main.getMessagesManager().getGameMsg("winTitle", this, placeholders, player), Main.getMessagesManager().getGameMsg("winSubTitle", this, placeholders, player), fadeIn, stay,
							fadeOut);
				}
			}
			if (si >= this.playersSpawns.size()) {
				si = 0;
			}
			try {
				player.teleport(this.playersSpawns.get(si));
			} catch (Exception e) {
				player.teleport(this.playersSpawns.get(0));
			}
			this.getVisibilityManager().resetPlayersHidden(pInfo);
			si++;
		}
		Arena arena = this;

		ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET, 1);
		FireworkMeta meta = (FireworkMeta) firework.getItemMeta();
		Builder fwBuilder = FireworkEffect.builder();
		fwBuilder.withColor(Color.BLUE, Color.RED, Color.GREEN, Color.AQUA);
		meta.addEffect(fwBuilder.build());
		new BukkitRunnable() {
			@Override
			public void run() {
				if (arena.getGameState() != GameState.FINISHING || arena == null || arena.ingamePlayers == null) {
					this.cancel();
					return;
				}
				for (PlayerInfo pInfo : arena.ingamePlayers.values()) {
					if (pInfo == null || arena == null || !pInfo.getIsIngame() || !pInfo.getPlayer().getWorld().getName().equals(arena.getWorld().getName())) {
						continue;
					}
					if (pInfo.getIsImposter() == isImposters) {
						Player player = pInfo.getPlayer();
						if (Math.random() < 0.3) {
							Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation().add(0, 3, 0), EntityType.FIREWORK);
							firework.setFireworkMeta(meta);
						}
					}
				}

			}
		}.runTaskTimer(Main.getPlugin(), 10L, 20L);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (arena.getGameState() == GameState.FINISHING) {
					if (Main.getConfigManager().getBungeecord() && !Main.getConfigManager().getBungeecordIsLobby()) {
						Main.getArenaManager().sendBungeUpdate(arena.getName(), GameState.WAITING, 0, 10);
					}

					GameEndWinners winners;
					if (isImposters) {
						winners = GameEndWinners.IMPOSTERS;
					} else {
						winners = GameEndWinners.CREWMATES;
					}
					arena.endGame(false, GameEndReasons.PLAYERS_WIN, winners);
				}
			}
		}.runTaskLater(Main.getPlugin(), 20L * 10L);

		this.updateScoreBoard();
		this.updateSigns();

		this.sabotageManager.endSabotage(false, true, null);

		for (PlayerInfo pInfo : this.impostersAlive) {
			pInfo.setKillCoolDown(0);
			if (pInfo.getIsInVent()) {
				this.getVentsManager().playerLeaveVent(pInfo, true, false);
			}
		}

		for (PlayerInfo pInfo1 : this.getPlayersInfo()) {
			for (PlayerInfo pInfo2 : this.getPlayersInfo()) {
				if (pInfo1 != pInfo2) {
					this.getVisibilityManager().showPlayer(pInfo1, pInfo2, true);
					this.getVisibilityManager().showPlayer(pInfo2, pInfo1, true);
					if (pInfo1.getFakePlayer() != null) {
						pInfo1.getFakePlayer().hidePlayerFrom(pInfo2.getPlayer(), true);
					}
					if (pInfo2.getFakePlayer() != null) {
						pInfo2.getFakePlayer().hidePlayerFrom(pInfo1.getPlayer(), true);
					}
				}
			}
		}
		new BukkitRunnable() {
			@Override
			public void run() {

				for (PlayerInfo pInfo : arena.getPlayersInfo()) {
					for (PlayerInfo pInfo1 : arena.getPlayersInfo()) {
						Packets.sendPacket(pInfo.getPlayer(), Packets.ADD_PLAYER(pInfo1.getPlayer().getUniqueId(), pInfo1.getPlayer().getName(), pInfo1.getOriginalPlayerListName(), pInfo1.getTextureValue(),
								pInfo.getTextureSignature()));
						Packets.sendPacket(pInfo.getPlayer(), Packets.UPDATE_DISPLAY_NAME(pInfo1.getPlayer().getUniqueId(), pInfo1.getPlayer().getName(), pInfo1.getOriginalPlayerListName()));
					}
				}
			}
		}.runTaskLater(Main.getPlugin(), 2L);
	}

	public void updateScoreBoard() {
		for (Player player : this.getPlayers()) {
			PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
			if (pInfo != null) {
				pInfo.updateScoreBoard();
			}
		}
		this.getTasksManager().updateTasksDoneBar(false);
	}

	public void updateSigns() {
		Boolean saveConfig = false;
		int i = 0;
		Iterator<JoinSign> itr = this.joinSigns.iterator();
		while (itr.hasNext()) {
			JoinSign sign = itr.next();
			if (sign.getBlock().getType().toString().contains("SIGN")) {
				sign.update();
			} else {
				itr.remove();
				List<String> signs_ = this.getArenaConfig().getStringList("signs");
				signs_.remove(i);
				this.getArenaConfig().set("signs", signs_);
				saveConfig = true;
			}
			i++;
		}
		if (saveConfig) {
			this.saveConfig();
		}
	}

	private void createLine(ImposterHologram holo, String line) {
		if (line.startsWith("@") && line.endsWith("@")) {
			line = line.replace("@", "");
			Material mat = Material.getMaterial(line);
			if (mat == null) {
				Main.getPlugin().getLogger().warning("Hologram item line 'task': " + line + " is not a valid material!");
				return;
			}
			holo.addLineWithItem(Utils.createItem(mat, " "));
		} else {
			holo.addLineWithText(line);
		}
	}

	public void createHolograms() {
		// create holograms

		// loop all tasks
		for (Task task : this.getAllTasks()) {
			ImposterHologram created = ImposterHologram.createHologram(task.getLocation(), "taskHologram_" + task.getArena().getName() + "_" + task.getId());
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%name%", Main.getMessagesManager().getTaskName(task.getTaskType().toString()));
			placeholders.put("%location%", task.getLocationName().getName());
			for (String line : Main.getMessagesManager().getHologramLines("task", placeholders)) {
				createLine(created, line);
			}
			created.setGlobalVisibility(false);
			created.setHologramClickListener(task.getTouchHandler());
			task.setHolo(created);
			this.holograms.add(created);
		}

		// loop all sabotages
		for (SabotageArena saboAr : this.sabotages) {
			ArrayList<SabotageTask> saboTasks = new ArrayList<SabotageTask>(Arrays.asList(saboAr.getTask1()));
			if (saboAr.getLength() != SabotageLength.SINGLE) {
				saboTasks.add(saboAr.getTask2());
			}
			String saboName = Main.getMessagesManager().getTaskName(saboAr.getType().toString());
			String saboTitle = Main.getMessagesManager().getSabotageTitle(saboAr.getType());
			for (SabotageTask saboTask : saboTasks) {
				ImposterHologram created = ImposterHologram.createHologram(saboTask.getLocation(), "sabotage_" + saboTask.getArena().getName() + "_" + saboTask.getSabotageType().name() + "_" + saboTask.getId());
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%sabotage_name%", saboName);
				placeholders.put("%sabotage_title%", saboTitle);
				for (String line : Main.getMessagesManager().getHologramLines("sabotage", placeholders)) {
					createLine(created, line);
				}
				created.setGlobalVisibility(false);
				created.setHologramClickListener(saboTask.getTouchHandler());
				saboTask.setHolo(created);
				this.holograms.add(created);
			}
		}

		// meeting button hologram
		ImposterHologram createdBtn = ImposterHologram.createHologram(this.meetingButton, "meetingButton_" + this.meetingButton.getWorld().getName());

		HologramClickListener meetingBtnClickListener = new HologramClickListener() {
			@Override
			public void onClick(HologramLineClickEvent event) {
				Player p = event.getPlayer();
				PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(p);
				if (!pInfo.getIsIngame()) {
					return;
				}
				if (pInfo.getArena().getGameState() == GameState.RUNNING && !pInfo.getArena().getIsInMeeting() && !pInfo.isGhost()) {
					MeetingBtnInv invHolder = new MeetingBtnInv(pInfo.getArena(), pInfo);
					Main.getSoundsManager().playSound("meetingBtnInvOpen", p, p.getLocation());
					p.openInventory(invHolder.getInventory());
				}
			}

			@Override
			public void onClick(HologramClickEvent event) {
				Player p = event.getPlayer();
				PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(p);
				if (!pInfo.getIsIngame()) {
					return;
				}
				if (pInfo.getArena().getGameState() == GameState.RUNNING && !pInfo.getArena().getIsInMeeting() && !pInfo.isGhost()) {
					MeetingBtnInv invHolder = new MeetingBtnInv(pInfo.getArena(), pInfo);
					Main.getSoundsManager().playSound("meetingBtnInvOpen", p, p.getLocation());
					p.openInventory(invHolder.getInventory());
				}
			}
		};

		for (String line : Main.getMessagesManager().getHologramLines("meetingButton", null)) {
			createLine(createdBtn, line);
		}
		createdBtn.setHologramClickListener(meetingBtnClickListener);
		this.btnHolo = createdBtn;

		// vents holograms
		for (VentGroup vg : this.getVentsManager().getVentGroups()) {
			for (Vent v : vg.getVents()) {
				ImposterHologram created = ImposterHologram.createHologram(v.getLoc(), "vent_" + vg.getArena().getName() + "_" + vg.getConfigId() + "_" + Utils.getRandomString(3));
				String locName = "";
				if (v.getLocName() != null) {
					v.getLocName().getName();
				}
				final Integer vgId = vg.getId();
				final Integer vId = v.getId();

				HologramClickListener clickListener = new HologramClickListener() {
					@Override
					public void onClick(HologramLineClickEvent event) {
						Player player = event.getPlayer();
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
						if (pInfo.getIsIngame() && pInfo.getIsImposter() && !pInfo.isGhost() && !pInfo.getIsInVent() && !pInfo.getArena().getIsInMeeting()) {
							pInfo.getArena().getVentsManager().ventHoloClick(pInfo, vgId, vId);
						} else if (!pInfo.getIsIngame()) {
							if (player.hasPermission("amongus.admin")) {
								player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Vent holo click group: " + vgId + " id: " + vId);
							}
						}
					}

					@Override
					public void onClick(HologramClickEvent event) {
						Player player = event.getPlayer();
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
						if (pInfo.getIsIngame() && pInfo.getIsImposter() && !pInfo.isGhost() && !pInfo.getIsInVent() && !pInfo.getArena().getIsInMeeting()) {
							pInfo.getArena().getVentsManager().ventHoloClick(pInfo, vgId, vId);
						} else if (!pInfo.getIsIngame()) {
							if (player.hasPermission("amongus.admin")) {
								player.sendMessage(Main.getConfigManager().getPrefix() + ChatColor.GREEN + "Vent holo click group: " + vgId + " id: " + vId);
							}
						}
					}
				};

				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%location%", locName);
				for (String line : Main.getMessagesManager().getHologramLines("vent", placeholders)) {
					this.createLine(created, line);
				}
				created.setGlobalVisibility(false);
				created.setHologramClickListener(clickListener);
				v.setHolo(created);
				this.holograms.add(created);
				this.getVentsManager().getHolos().add(created);
			}
		}

		// cameras holo
		if (this.camerasLoc != null) {
			for (Camera cam : this.camerasManager.getCameras()) {
				cam.createArmorStand();
			}
			ImposterHologram created = ImposterHologram.createHologram(this.camerasLoc, "camerasHologram_" + this.camerasLoc.getWorld().getName());
			HologramClickListener camerasHologramClickListener = new HologramClickListener() {
				@Override
				public void onClick(HologramLineClickEvent event) { }

				@Override
				public void onClick(HologramClickEvent event) { }
			};
			if (this.camerasManager.getCameras().size() > 0) {
				camerasHologramClickListener = new HologramClickListener() {
					@Override
					public void onClick(HologramLineClickEvent event) {
						Player player = event.getPlayer();
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
						if (pInfo.getIsIngame() && !pInfo.getIsInVent() && !pInfo.getArena().getIsInMeeting()) {
							pInfo.getArena().getCamerasManager().camerasHoloClick(pInfo);
						}
					}

					@Override
					public void onClick(HologramClickEvent event) {
						Player player = event.getPlayer();
						PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
						if (pInfo.getIsIngame() && !pInfo.getIsInVent() && !pInfo.getArena().getIsInMeeting()) {
							pInfo.getArena().getCamerasManager().camerasHoloClick(pInfo);
						}
					}
				};
			}

			for (String line : Main.getMessagesManager().getHologramLines("cameras", null)) {
				this.createLine(created, line);
			}
			created.setHologramClickListener(camerasHologramClickListener);
			this.camerasManager.setHolo(created);
			this.holograms.add(created);
		}

		if (this.vitalsLoc != null) {
			ImposterHologram created = ImposterHologram.createHologram(this.vitalsLoc, "vitalsHologram_" + this.vitalsLoc.getWorld().getName());
			HologramClickListener vitalsHologramClickListener = new HologramClickListener() {
				@Override
				public void onClick(HologramLineClickEvent event) {
					Player player = event.getPlayer();
					PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
					if (pInfo != null) {
						if (pInfo.getIsIngame() && !pInfo.getIsInVent() && !pInfo.getArena().getIsInMeeting() && pInfo.getArena().getGameState() == GameState.RUNNING) {
							pInfo.getArena().getVitalsManager().openInventory(player);
						}
					}
				}

				@Override
				public void onClick(HologramClickEvent event) {
					Player player = event.getPlayer();
					PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
					if (pInfo != null) {
						if (pInfo.getIsIngame() && !pInfo.getIsInVent() && !pInfo.getArena().getIsInMeeting() && pInfo.getArena().getGameState() == GameState.RUNNING) {
							pInfo.getArena().getVitalsManager().openInventory(player);
						}
					}
				}
			};

			for (String line : Main.getMessagesManager().getHologramLines("vitals", null)) {
				this.createLine(created, line);
			}
			created.setHologramClickListener(vitalsHologramClickListener);
			this.holograms.add(created);
			this.vitalsManager.setHolo(created);
		}
	}

	public void deleteHolograms() {
		for (ImposterHologram holo : this.holograms) {
			holo.deleteHologram();
		}

		if (this.btnHolo != null) {
			this.btnHolo.deleteHologram();
		}

		for (Camera cam : this.camerasManager.getCameras()) {
			cam.deleteArmorStands();
		}

		if (this.camerasManager.getHolo() != null) {
			this.camerasManager.getHolo().deleteHologram();
		}

		if (this.vitalsLoc != null && this.vitalsManager != null && this.vitalsManager.getHolo() != null) {
			this.vitalsManager.getHolo().deleteHologram();
		}
	}

	public Collection<PlayerInfo> getPlayersInfo() {
		if (this.ingamePlayers != null) {
			return this.ingamePlayers.values();
		}
		return null;
	}

	public Boolean canPlayerUseButton(PlayerInfo pInfo) {
		if (!this.getSabotageManager().getIsSabotageActive()) {
			if (pInfo.getMeetingsLeft() > 0) {
				if (this.getMeetingManager().getMeetingCooldownTimer() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public Integer getTasksNum(TaskLength tl) {
		switch (tl) {
		case COMMON:
			return this.getCommonTasks();
		case SHORT:
			return this.getShortTasks();
		case LONG:
			return this.getLongTasks();
		default:
			throw new IllegalArgumentException("Unexpected value: " + tl);
		}
	}

	public void resetMapIds() {
		for (Short id : this.mapIds.keySet()) {
			this.mapIds.put(id, false);
		}
	}

	public void updatePlayersJoinedID() {
		ArrayList<PlayerInfo> pInfoList = new ArrayList<>(this.getPlayersInfo());
		Comparator<PlayerInfo> compareById = new Comparator<PlayerInfo>() {
			@Override
			public int compare(PlayerInfo o1, PlayerInfo o2) {
				return o1.getJoinedId().compareTo(o2.getJoinedId());
			}
		};
		Collections.sort(pInfoList, compareById);
		int i = 0;
		for (PlayerInfo pInfo : pInfoList) {
			pInfo.setJoinedId(i);
			if (pInfo.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof TaskInvHolder) {
				((TaskInvHolder) pInfo.getPlayer().getOpenInventory().getTopInventory().getHolder()).update();
			}
			i++;
		}
	}

	public void sendTitle(String key, HashMap<String, String> placeholders) {
		if (Main.getMessagesManager().getGameMsg(key, this, placeholders, null).isEmpty()) {
			return;
		}
		for (Player p : this.getPlayers()) {
			String title = Main.getMessagesManager().getGameMsg(key, this, placeholders, p);
			String subTitle = Main.getMessagesManager().getGameMsg(key, this, placeholders, p);
			p.sendTitle(title, subTitle, 15, 80, 15);
		}
	}

	public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
		if (title.isEmpty() && subTitle.isEmpty()) {
			return;
		}
		for (Player p : this.getPlayers()) {
			String parsedTitle = title;
			String parsedSubtitle = subTitle;
			if (Main.getIsPlaceHolderAPI()) {
				parsedTitle = PlaceholderAPI.setPlaceholders(p, title);
				parsedSubtitle = PlaceholderAPI.setPlaceholders(p, subTitle);
			}
			p.sendTitle(parsedTitle, parsedSubtitle, fadeIn, stay, fadeOut);
		}
	}

	public void turnPrimeShieldsOn() {
		if (this.primeShieldsBlocks != null) {
			for (Block block : this.primeShieldsBlocks) {
				if (block.getState().getBlockData() instanceof Lightable) {
					Lightable lightable = (Lightable) block.getState().getBlockData();
					lightable.setLit(true);
					block.setBlockData(lightable, false);
				}
			}
		}
	}

	public void turnPrimeShieldsOff() {
		if (this.primeShieldsBlocks != null) {
			for (Block block : this.primeShieldsBlocks) {
				if (block.getState().getBlockData() instanceof Lightable) {
					Lightable lightable = (Lightable) block.getState().getBlockData();
					lightable.setLit(false);
					block.setBlockData(lightable, false);
				}
			}
		}
	}

	public ArrayList<Task> getAllTasksSorted() {
		ArrayList<Task> out = new ArrayList<Task>(this.tasks.values());
		Collections.sort(out);
		return out;
	}

	public ArrayList<Task> getAllTasksLocationName(String locId) {
		ArrayList<Task> out = new ArrayList<Task>(this.tasks.values());
		out.removeIf(n -> (!n.getLocationName().getId().equals(locId)));
		return out;
	}

	public void delete() {
		this.endGame(true, GameEndReasons.RELOAD, GameEndWinners.NOBODY);
		this.playersSpawns = null;
		this.ingamePlayers = null;
		this.gameImposters = null;
		this.impostersAlive = null;
		this.ghosts = null;
		this.mapIds = null;
		this.bossBar = null;
		this.world = null;
		this.waitingLobby = null;
		this.mapCenter = null;
		this.meetingButton = null;
		this.camerasLoc = null;
		this.gameState = null;
		this.isInMeeting = null;
		for (Task task : this.tasks.values()) {
			task.delete();
		}
		this.tasks = null;
		this.sabotages = null;
		this.locations = null;
		this.scanQueue = null;
		this.gameTimerRunnable = null;
		this.secondRunnable = null;
		this.deleteHolograms();
		for (ImposterHologram holo : this.holograms) {
			holo.deleteHologram();
		}
		this.holograms = null;
		this.btnHolo = null;
		this.colors_ = null;
		this.taskManager.delete();
		this.taskManager = null;
		this.sabotageManager.delete();
		this.sabotageManager = null;
		this.meetingManager.delete();
		this.meetingManager = null;
		this.deadBodiesManager.deleteAll();
		this.deadBodiesManager = null;
		this.ventsManager.delete();
		this.ventsManager = null;
		this.camerasManager.delete();
		this.camerasManager = null;
		this.visibilityManager = null;
		this.doorsManager.delete();
		this.doorsManager = null;
		this.joinSigns = null;
		this.colorSelectorInv = null;
		this.primeShieldsBlocks = null;
	}

	public void saveConfig() {
		try {
			if(this.arenaFile.exists()) {
				this.arenaConfig.save(this.arenaFile);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* ------- Getters / Setters ------- */

	public void addTask(Task t) {
		this.tasks.put(t.getId(), t);
	}

	public Task getTask(String taskId) {
		return this.tasks.get(taskId);
	}

	public ArrayList<Task> getAllTasks() {
		return new ArrayList<Task>(this.tasks.values());
	}

	public ArrayList<PlayerInfo> getGhosts() {
		return this.ghosts;
	}

	public void addSabotage(SabotageArena sa) {
		this.sabotages.add(sa);
	}

	public SabotageArena getSabotageArena(SabotageType sabotageType) {
		for (SabotageArena sa : this.sabotages) {
			if (sa.getType() == sabotageType) {
				return sa;
			}
		}
		return null;
	}

	public ArrayList<SabotageArena> getSabotages() {
		return this.sabotages;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return this.world;
	}

	public GameState getGameState() {
		return this.gameState;
	}

	public void setGameState(GameState state) {
		if (this.gameState != state) {
			AUArenaGameStateChange ev = new AUArenaGameStateChange(this, state);
			Bukkit.getPluginManager().callEvent(ev);
		}
		this.gameState = state;
	}

	public TasksManager getTasksManager() {
		return this.taskManager;
	}

	public SabotageManager getSabotageManager() {
		return this.sabotageManager;
	}

	public void setLocations(HashMap<String, LocationName> locations) {
		this.locations = locations;
	}

	public HashMap<String, LocationName> getLocations() {
		return this.locations;
	}

	public String getName() {
		return this.name;
	}

	public String getDisplayName() {
		if (this.displayName == null) {
			return this.name;
		}
		return this.displayName;
	}

	public Integer getGameTimer() {
		return this.gameTimer;
	}

	public void setGameTimer(Integer to) {
		this.gameTimer = to;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setVotingTime(Integer votingTime) {
		this.votingTime = votingTime;
	}

	public Integer getVotingTime() {
		return this.votingTime;
	}

	public void setDiscussionTime(Integer discussionTime) {
		this.discussionTime = discussionTime;
	}

	public Integer getDiscussionTime() {
		return this.discussionTime;
	}

	public void setNumImposters(Integer numImposters) {
		this.numImposters = numImposters;
	}

	public Integer getNumImposters() {
		return this.numImposters;
	}

	public int getMinPlayers() {
		return this.minPlayers;
	}

	public Integer getMaxPlayers() {
		return this.maxPlayers;
	}

	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public ArrayList<Location> getPlayerSpawns() {
		return this.playersSpawns;
	}

	public Integer getCommonTasks() {
		return this.commonTasks;
	}

	public void setCommonTasks(Integer commonTasks) {
		this.commonTasks = commonTasks;
	}

	public Integer getLongTasks() {
		return longTasks;
	}

	public void setLongTasks(Integer longTasks) {
		this.longTasks = longTasks;
	}

	public Integer getShortTasks() {
		return this.shortTasks;
	}

	public void setShortTasks(Integer shortTasks) {
		this.shortTasks = shortTasks;
	}

	public Integer getMeetingsPerPlayer() {
		return meetingsPerPlayer;
	}

	public void setMeetingsPerPlayer(Integer meetingsPerPlayer) {
		this.meetingsPerPlayer = meetingsPerPlayer;
	}

	public Integer getKillCooldown() {
		return killCooldown;
	}

	public void setKillCooldown(Integer killCooldown) {
		this.killCooldown = killCooldown;
	}

	public Integer getMeetingCooldown() {
		return meetingCooldown;
	}

	public void setMeetingCooldown(Integer meetingCooldown) {
		this.meetingCooldown = meetingCooldown;
	}

	public ArrayList<ImposterHologram> getHolograms() {
		return this.holograms;
	}

	public void setHolograms(ArrayList<ImposterHologram> holograms) {
		this.holograms = holograms;
	}

	public Location getMapCenter() {
		return mapCenter;
	}

	public void setMapCenter(Location mapCenter) {
		this.mapCenter = mapCenter;
	}

	public Boolean getDisableSprinting() {
		return this.disableSprinting;
	}

	public void setDisableSprinting(Boolean disableSprinting) {
		this.disableSprinting = disableSprinting;
	}

	public Boolean getDisableJumping() {
		return this.disableJumping;
	}

	public void setDisableJumping(Boolean disableJumping) {
		this.disableJumping = disableJumping;
	}

	public Boolean getDisableMap() {
		return this.disableMap;
	}

	public void setDisableMap(Boolean disableMap) {
		this.disableMap = disableMap;
	}

	public ArrayList<PlayerInfo> getGameImposters() {
		return this.gameImposters;
	}

	public Location getMeetingButton() {
		return meetingButton;
	}

	public void setMeetingButton(Location meetingButton) {
		this.meetingButton = meetingButton;
	}

	public Boolean getIsInMeeting() {
		return isInMeeting;
	}

	public void setIsInMeeting(Boolean isInMeeting) {
		this.isInMeeting = isInMeeting;
	}

	public MeetingManager getMeetingManager() {
		return meetingManager;
	}

	public ImposterHologram getBtnHolo() {
		return this.btnHolo;
	}

	public ArrayList<PlayerInfo> getImpostersAlive() {
		return this.impostersAlive;
	}

	public DeadBodiesManager getDeadBodiesManager() {
		return deadBodiesManager;
	}

	public Integer getSabotageCooldown() {
		return sabotageCooldown;
	}

	public void setSabotageCooldown(Integer sabotageCooldown) {
		this.sabotageCooldown = sabotageCooldown;
	}

	public VentsManager getVentsManager() {
		return this.ventsManager;
	}

	public CamerasManager getCamerasManager() {
		return this.camerasManager;
	}

	public Location getCamerasLoc() {
		return camerasLoc;
	}

	public void setCamerasLoc(Location camerasLoc) {
		this.camerasLoc = camerasLoc;
	}

	public Boolean getEnableCameras() {
		return enableCameras;
	}

	public void setEnableCameras(Boolean enableCameras) {
		this.enableCameras = enableCameras;
	}

	public VisibilityManager getVisibilityManager() {
		return visibilityManager;
	}

	public Integer getImposterVision() {
		return imposterVision;
	}

	public void setImposterVision(Integer imposterVision) {
		this.imposterVision = imposterVision;
	}

	public Integer getCrewmateVision() {
		return crewmateVision;
	}

	public void setCrewmateVision(Integer crewmateVision) {
		this.crewmateVision = crewmateVision;
	}

	public BossBar getTasksBossBar() {
		return this.bossBar;
	}

	public Double getReportDistance() {
		return reportDistance;
	}

	public void setReportDistance(Double reportDistance) {
		this.reportDistance = reportDistance;
	}

	public Boolean getEnableReducedVision() {
		return enableReducedVision;
	}

	public void setEnableReducedVision(Boolean enableReducedVision) {
		this.enableReducedVision = enableReducedVision;
	}

	public ArrayList<JoinSign> getJoinSigns() {
		return joinSigns;
	}

	public Integer getProceedingTime() {
		return proceedingTime;
	}

	public void setProceedingTime(Integer proceedingTime) {
		this.proceedingTime = proceedingTime;
	}

	public Boolean getHideHologramsOutOfView() {
		return hideHologramsOutOfView;
	}

	public void setHideHologramsOutOfView(Boolean hideHologramsOutOfView) {
		this.hideHologramsOutOfView = hideHologramsOutOfView;
	}

	public DoorsManager getDoorsManager() {
		return doorsManager;
	}

	public Integer getDoorCloseTime() {
		return doorCloseTime;
	}

	public void setDoorCloseTime(Integer doorCloseTime) {
		this.doorCloseTime = doorCloseTime;
	}

	public Integer getDoorCooldown() {
		return doorCooldown;
	}

	public void setDoorCooldown(Integer doorCooldown) {
		this.doorCooldown = doorCooldown;
	}

	public ArrayList<ColorInfo> getColors_() {
		return this.colors_;
	}

	public ColorSelectorInv getColorSelectorInv(Player p) {
		if (Main.getIsPlaceHolderAPI()) {
			colorSelectorInv.changeTitle(PlaceholderAPI.setPlaceholders(p, colorSelectorInv.getOriginalTitle()));
		}
		return colorSelectorInv;
	}

	public ArrayList<PlayerInfo> getScanQueue() {
		return scanQueue;
	}

	public Boolean getEnableVisualTasks() {
		return enableVisualTasks;
	}

	public void setEnableVisualTasks(Boolean enableVisualTasks) {
		this.enableVisualTasks = enableVisualTasks;
	}

	public ArrayList<Block> getPrimeShieldsBlocks() {
		return primeShieldsBlocks;
	}

	public long getAsteroidsLastTime() {
		return asteroidsLastTime;
	}

	public void setAsteroidsLastTime(long asteroidsLastTime) {
		this.asteroidsLastTime = asteroidsLastTime;
	}

	public Boolean getConfirmEjects() {
		return confirmEjects;
	}

	public void setConfirmEjects(Boolean confirmEjects) {
		this.confirmEjects = confirmEjects;
	}

	public Location getWaitingLobby() {
		return waitingLobby;
	}

	public void setWaitingLobby(Location waitingLobby) {
		this.waitingLobby = waitingLobby;
	}

	public Boolean getMoveMapWithPlayer() {
		return moveMapWithPlayer;
	}

	public void setMoveMapWithPlayer(Boolean moveMapWithPlayer) {
		this.moveMapWithPlayer = moveMapWithPlayer;
	}

	public Integer getGameTimerActive() {
		return gameTimerActive;
	}

	public void setGameTimerActive(Integer gameTimerActive) {
		this.gameTimerActive = gameTimerActive;
	}

	public ArrayList<Player> get_playersToDelete() {
		return _playersToDelete;
	}

	public VitalsManager getVitalsManager() {
		return vitalsManager;
	}

	public Location getVitalsLoc() {
		return vitalsLoc;
	}

	public void setVitalsLoc(Location vitalsLoc) {
		this.vitalsLoc = vitalsLoc;
	}

	public Boolean getDynamicImposters() {
		return dynamicImposters;
	}

	public void setDynamicImposters(Boolean dynamicImposters) {
		this.dynamicImposters = dynamicImposters;
	}

	public File getArenaFile() {
		return arenaFile;
	}

	public void setArenaFile(File arenaFile) {
		this.arenaFile = arenaFile;
		this.arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
	}

	public FileConfiguration getArenaConfig() {
		return this.arenaConfig;
	}

	public Integer getScientistCount() {
		return scientistCount;
	}

	public void setScientistCount(Integer scientistCount) {
		this.scientistCount = scientistCount;
	}

	public Integer getScientistVitalsCooldown() {
		return scientistVitalsCooldown;
	}

	public void setScientistVitalsCooldown(Integer scientistVitalsCooldown) {
		this.scientistVitalsCooldown = scientistVitalsCooldown;
	}

	public Integer getScientistBatteryDuration() {
		return scientistBatteryDuration;
	}

	public void setScientistBatteryDuration(Integer scientistBatteryDuration) {
		this.scientistBatteryDuration = scientistBatteryDuration;
	}

	public Integer getEngineerCount() {
		return engineerCount;
	}

	public void setEngineerCount(Integer engineerCount) {
		this.engineerCount = engineerCount;
	}

	public Integer getEngineerVentCooldown() {
		return engineerVentCooldown;
	}

	public void setEngineerVentCooldown(Integer engineerVentCooldown) {
		this.engineerVentCooldown = engineerVentCooldown;
	}

	public Integer getEngineerMaxTimeInVents() {
		return engineerMaxTimeInVents;
	}

	public void setEngineerMaxTimeInVents(Integer engineerMaxTimeInVents) {
		this.engineerMaxTimeInVents = engineerMaxTimeInVents;
	}

	public Integer getAngelCount() {
		return angelCount;
	}

	public void setAngelCount(Integer angelCount) {
		this.angelCount = angelCount;
	}

	public Integer getAngelCooldown() {
		return angelCooldown;
	}

	public void setAngelCooldown(Integer angelCooldown) {
		this.angelCooldown = angelCooldown;
	}

	public boolean getAngelProtectVisibleToImposters() {
		return angelProtectVisibleToImposters;
	}

	public void setAngelProtectVisibleToImposters(boolean angelProtectVisibleToImposters) {
		this.angelProtectVisibleToImposters = angelProtectVisibleToImposters;
	}

	public Integer getAngelDuration() {
		return angelDuration;
	}

	public void setAngelDuration(Integer angelDuration) {
		this.angelDuration = angelDuration;
	}

	public Integer getShapeshifterCount() {
		return shapeshifterCount;
	}

	public void setShapeshifterCount(Integer shapeshifterCount) {
		this.shapeshifterCount = shapeshifterCount;
	}

	public Integer getShapeshifterCooldown() {
		return shapeshifterCooldown;
	}

	public void setShapeshifterCooldown(Integer shapeshifterCooldown) {
		this.shapeshifterCooldown = shapeshifterCooldown;
	}

	public boolean getShapeshifterLeaveEvidence() {
		return shapeshifterLeaveEvidence;
	}

	public void setShapeshifterLeaveEvidence(boolean shapeshifterLeaveEvidence) {
		this.shapeshifterLeaveEvidence = shapeshifterLeaveEvidence;
	}

	public Integer getShapeshifterDuration() {
		return shapeshifterDuration;
	}

	public void setShapeshifterDuration(Integer shapeshifterDuration) {
		this.shapeshifterDuration = shapeshifterDuration;
	}

	public Integer getScientistChance() {
		return scientistChance;
	}

	public void setScientistChance(Integer scientistChance) {
		this.scientistChance = scientistChance;
	}

	public Integer getEngineerChance() {
		return engineerChance;
	}

	public void setEngineerChance(Integer engineerChance) {
		this.engineerChance = engineerChance;
	}

	public Integer getAngelChance() {
		return angelChance;
	}

	public void setAngelChance(Integer angelChance) {
		this.angelChance = angelChance;
	}

	public Integer getShapeshifterChance() {
		return shapeshifterChance;
	}

	public void setShapeshifterChance(Integer shapeshifterChance) {
		this.shapeshifterChance = shapeshifterChance;
	}

}
