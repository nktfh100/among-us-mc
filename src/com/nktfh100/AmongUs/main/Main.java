package com.nktfh100.AmongUs.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.EnumWrappers.SoundCategory;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.nktfh100.AmongUs.commands.AdminCommand;
import com.nktfh100.AmongUs.commands.AdminCommandTab;
import com.nktfh100.AmongUs.commands.PlayersCommand;
import com.nktfh100.AmongUs.commands.PlayersCommandTab;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.events.VentureChatEvent_;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.Camera;
import com.nktfh100.AmongUs.info.DeadBody;
import com.nktfh100.AmongUs.info.FakeArmorStand;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.managers.ArenaManager;
import com.nktfh100.AmongUs.managers.BungeArenaManager;
import com.nktfh100.AmongUs.managers.ConfigManager;
import com.nktfh100.AmongUs.managers.CosmeticsManager;
import com.nktfh100.AmongUs.managers.ItemsManager;
import com.nktfh100.AmongUs.managers.MessagesManager;
import com.nktfh100.AmongUs.managers.PlayersManager;
import com.nktfh100.AmongUs.managers.SoundsManager;
import com.nktfh100.AmongUs.utils.Metrics;

public class Main extends JavaPlugin {

	private static final PacketType[] ENTITY_PACKETS = { PacketType.Play.Server.ENTITY_EQUIPMENT, PacketType.Play.Server.ANIMATION, PacketType.Play.Server.COLLECT,
			PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, PacketType.Play.Server.ENTITY_VELOCITY, PacketType.Play.Server.REL_ENTITY_MOVE, PacketType.Play.Server.ENTITY_LOOK,
			PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.ENTITY_HEAD_ROTATION, PacketType.Play.Server.ENTITY_STATUS, PacketType.Play.Server.ATTACH_ENTITY,
			PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.ENTITY_EFFECT, PacketType.Play.Server.REMOVE_ENTITY_EFFECT, PacketType.Play.Server.BLOCK_BREAK_ANIMATION,
			PacketType.Play.Server.REL_ENTITY_MOVE_LOOK };

	private static Plugin plugin;

	private static ConfigManager configManager;
	private static PlayersManager playersManager;
	private static ArenaManager arenaManager;
	private static BungeArenaManager bungeArenaManager;
	private static MessagesManager messagesManager;
	private static ItemsManager itemsManager;
	private static SoundsManager soundsManager;
	private static CosmeticsManager cosmeticsManager;

	private static Boolean isVentureChat = false;
	private static Boolean isPlaceHolderAPI = false;
	private static Boolean isPlayerPoints = false;
	private static PlayerPointsAPI playerPointsApi = null;

	public void onEnable() {
		plugin = this;

		String ver = getServer().getVersion();
		Boolean isOk = false;
		if (ver.contains("1.16")) {
			isOk = true;
		}
		if (!isOk) {
			Bukkit.getLogger().log(Level.SEVERE, "Server version not supported! (only 1.16 - 1.16.5 are supported)");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		new Metrics(this, 12109);
		if (getServer().getPluginManager().getPlugin("VentureChat") != null) {
			isVentureChat = true;
			getServer().getPluginManager().registerEvents(new VentureChatEvent_(), this);
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new SomeExpansion(this).register();
			isPlaceHolderAPI = true;
		}

		if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
			isPlayerPoints = true;
			playerPointsApi = PlayerPoints.getInstance().getAPI();
		} else {
			Bukkit.getLogger().log(Level.WARNING, "The plugin 'PlayerPoints' is not preset, cosmetics will not work!");
		}
		cosmeticsManager = new CosmeticsManager();

		configManager = new ConfigManager(plugin.getConfig());

		configManager.loadConfig();
		if (!plugin.isEnabled()) {
			return;
		}
		if (configManager.getBungeecord()) {
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			if (configManager.getBungeecordIsLobby()) {
				bungeArenaManager = new BungeArenaManager(configManager.getGameServers());
				getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", bungeArenaManager);
			}

		}
		if (!plugin.isEnabled()) {
			return;
		}
		messagesManager = new MessagesManager();
		itemsManager = new ItemsManager();
		soundsManager = new SoundsManager();
		messagesManager.loadAll();
		itemsManager.loadItems();
		soundsManager.loadSounds();
		cosmeticsManager.loadCosmetics();
		arenaManager = new ArenaManager();
		arenaManager.loadArenas();
		playersManager = new PlayersManager();

		if (configManager.getBungeecord() && configManager.getBungeecordIsLobby()) {
			bungeArenaManager.createInventory();
			bungeArenaManager.updateArenaSelectorInv();
			if (arenaManager.getAllArenas().size() > 0) {
				arenaManager.sendBungeUpdate(arenaManager.getAllArenas().iterator().next());
			}
		}
		this.getCommand("aua").setExecutor(new AdminCommand());
		this.getCommand("aua").setTabCompleter(new AdminCommandTab());

		this.getCommand("au").setExecutor(new PlayersCommand());
		this.getCommand("au").setTabCompleter(new PlayersCommandTab());
		getServer().getPluginManager().registerEvents(playersManager, this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerDamage(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.BlockBreak(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.BlockPlace(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.HungerChange(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerDrop(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerRightClick(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerChat(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.InvClick(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerPickUp(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerSwapHand(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.SignChange(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.InvClose(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerMove(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.EntityInteract(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerSneak(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerCommand(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.ArmorStandManipulate(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.ServerPingEvent(), this);
		getServer().getPluginManager().registerEvents(new com.nktfh100.AmongUs.events.PlayerEnterPortal(), this);

		Bukkit.getLogger().info("[AmongUs] Plugin made by nktfh100");
		Bukkit.getLogger().info("[AmongUs] Made with love in israel!");

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

		// hide items in players hands
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
			@Override
			public void onPacketSending(PacketEvent ev) {
				if (ev.getPlayer() == null) {
					return;
				}
				PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(ev.getPlayer());
				if (pInfo != null && pInfo.getIsIngame()) {
					List<Pair<ItemSlot, ItemStack>> newSlotStack = new ArrayList<Pair<ItemSlot, ItemStack>>();

					for (Pair<ItemSlot, ItemStack> pair : ev.getPacket().getSlotStackPairLists().read(0)) {
						if (pair.getFirst() == EnumWrappers.ItemSlot.MAINHAND) {
							newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.MAINHAND, new ItemStack(Material.AIR, 1)));
						} else if (pair.getFirst() == EnumWrappers.ItemSlot.OFFHAND) {
							newSlotStack.add(new Pair<ItemSlot, ItemStack>(EnumWrappers.ItemSlot.OFFHAND, new ItemStack(Material.AIR, 1)));
						} else {
							newSlotStack.add(pair);
						}
					}
					ev.getPacket().getSlotStackPairLists().write(0, newSlotStack);
				}

			}
		});

		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
			@SuppressWarnings("deprecation")
			@Override
			public void onPacketReceiving(PacketEvent ev) {
				Player attacker = ev.getPlayer();
				PlayerInfo attackerInfo = Main.getPlayersManager().getPlayerInfo(ev.getPlayer());
				if (attackerInfo == null || attackerInfo.getArena() == null || attackerInfo.getArena().getPlayersInfo() == null) {
					return;
				}
				Entity victimEnt = ev.getPacket().getEntityModifier(ev.getPlayer().getWorld()).read(0);
				Player victim = null;
				PlayerInfo victimInfo = null;
				if (victimEnt == null) { // fake entity - players in cameras / scan armor stands
					int entityId = ev.getPacket().getIntegers().read(0);
					for (PlayerInfo pInfo1 : attackerInfo.getArena().getPlayersInfo()) {
						outer: if (pInfo1 != attackerInfo) {
							if (pInfo1.getIsInCameras() && pInfo1.getFakePlayerId().equals(entityId)) {
								victim = pInfo1.getPlayer();
								victimInfo = pInfo1;
								break outer;
							} else if (pInfo1.getIsScanning()) {
								for (FakeArmorStand fas : pInfo1.getScanArmorStands()) {
									if (fas.getEntityId() == entityId) {
										victim = pInfo1.getPlayer();
										victimInfo = pInfo1;
										break outer;
									}
								}
							}
						}
					}
					if (victim == null) {
						return;
					}
				} else if (!(victimEnt instanceof Player)) {
					if (attackerInfo.getIsIngame() && ev.getPacket().getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.ATTACK) {
						ev.setCancelled(true);
					}
					return;
				}

				if (victim == null) {
					victim = (Player) victimEnt;
					victimInfo = Main.getPlayersManager().getPlayerInfo(victim);
				}

				if (ev.getPacket().getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.ATTACK) {
					if ((!attackerInfo.getIsIngame() && victimInfo.getIsIngame()) || (attackerInfo.getIsIngame() && !victimInfo.getIsIngame())) {
						ev.setCancelled(true);
						return;
					}
					if (!attackerInfo.getIsIngame() || !victimInfo.getIsIngame()) {
						return;
					}
					ev.setCancelled(true);


					if (attackerInfo.getArena().getGameState() != GameState.RUNNING || !attackerInfo.getIsImposter() || attackerInfo.isGhost() || victimInfo.isGhost() || victimInfo.getIsImposter()) {
						return;
					}
					
					if (attackerInfo.getArena().getIsInMeeting()) {
						return;
					}
					
					if (attackerInfo.getKillCoolDown() > 0) {
						return;
					}
					if (attacker.getItemInHand() == null || attacker.getItemInHand().getItemMeta() == null) {
						ev.setCancelled(true);
						return;
					}
					// check if item in attackers hand is the kill item
					String itemName = attacker.getItemInHand().getItemMeta().getDisplayName();
					ItemInfoContainer killItem = Main.getItemsManager().getItem("kill");
					//if (attacker.getItemInHand().getType().equals(killItem.getItem2().getMat())) {
					if (killItem.getItem2().getTitle(attackerInfo.getKillCoolDown().toString()).equals(itemName)) {
						
						final Player victim_ = victim;
						final PlayerInfo victimInfo_ = victimInfo;
						new BukkitRunnable() {
							@Override
							public void run() {
								if (victimInfo_.getIsInCameras()) {
									victimInfo_.getArena().getCamerasManager().playerLeaveCameras(victimInfo_, false);
								}

								attackerInfo.setKillCoolDown(attackerInfo.getArena().getKillCooldown());
								Location vicLoc = victim_.getLocation();
								attacker.teleport(new Location(victim_.getWorld(), vicLoc.getX(), vicLoc.getY(), vicLoc.getZ(), attacker.getLocation().getYaw(), attacker.getLocation().getPitch()));
								victim_.getWorld().spawnParticle(Particle.BLOCK_CRACK, victim_.getLocation().getX(), victim_.getLocation().getY() + 1.3, victim_.getLocation().getZ(), 30, 0.4D, 0.4D,
										0.4D, Bukkit.createBlockData(Material.REDSTONE_BLOCK));

								Main.getSoundsManager().playSound("playerDeathAttacker", attacker, victim_.getLocation());
								Main.getSoundsManager().playSound("playerDeathVictim", victim_, victim_.getLocation());

								attackerInfo.getArena().playerDeath(attackerInfo, victimInfo_, true);

								for (PlayerInfo pInfo : attackerInfo.getArena().getPlayersInfo()) {
									if (!pInfo.isGhost()) {
										DeadBody db = attackerInfo.getArena().getDeadBodiesManager().isCloseToBody(pInfo.getPlayer().getLocation());
										if (db != null) {
											pInfo.setCanReportBody(true, db);
										}
									}
								}

							}
						}.runTask(Main.getPlugin());
					}
				}
			}
		});

		// disable hiting sound
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
			PlayersManager playersManager = Main.getPlayersManager();

			@Override
			public void onPacketSending(PacketEvent ev) {
				Player player = ev.getPlayer();
				Sound sound = ev.getPacket().getSoundEffects().read(0);
				if (ev.getPacket().getSoundCategories().read(0) == SoundCategory.PLAYERS) {
					PlayerInfo pInfo = playersManager.getPlayerInfo(player);
					if (pInfo == null) {
						return;
					}
					if (pInfo.getIsIngame() && !pInfo.isGhost()) {
						StructureModifier<Integer> ints = ev.getPacket().getIntegers();

						double x = ints.read(0) / 8D;
						double y = ints.read(1) / 8D;
						double z = ints.read(2) / 8D;

						Predicate<Entity> predicate = i -> (i instanceof Player && playersManager.getPlayerInfo((Player) i).isGhost());
						Collection<Entity> players_ = ev.getPlayer().getWorld().getNearbyEntities(new Location(player.getWorld(), x, y, z), 1, 1, 1, predicate);
						if (players_.size() > 0) {
							ev.setCancelled(true);
							return;
						}
					}
				}
				if (sound == Sound.ENTITY_PLAYER_ATTACK_NODAMAGE || sound == Sound.ITEM_ARMOR_EQUIP_GENERIC) {
					PlayerInfo pInfo = playersManager.getPlayerInfo(player);
					if (pInfo != null && pInfo.getIsIngame()) {
						ev.setCancelled(true);
					}
				}
			}
		});

		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
			@Override
			public void onPacketSending(PacketEvent ev) {
				Entity entity = ev.getPacket().getEntityModifier(ev.getPlayer().getWorld()).read(0);
				if (entity != null && entity instanceof Player) {
					PlayerInfo pInfoSentTo = Main.getPlayersManager().getPlayerInfo(ev.getPlayer());
					if (pInfoSentTo == null) {
						return;
					}
					Arena arena = pInfoSentTo.getArena();
					if (pInfoSentTo.getIsIngame() && arena.getGameState() == GameState.RUNNING) {
						PlayerInfo pInfoSpawned = Main.getPlayersManager().getPlayerInfo((Player) entity);
						if (pInfoSpawned == null) {
							return;
						}
						if (pInfoSpawned.getArena() == arena) {
							if (pInfoSpawned.getIsInVent() || pInfoSpawned.getIsInCameras()) {
								ev.setCancelled(true);
								return;
							}
							if ((!pInfoSentTo.isGhost() && !pInfoSpawned.isGhost()) && !arena.getIsInMeeting()) {
								if (!arena.getVisibilityManager().canSee(pInfoSentTo, pInfoSpawned)) {
									ev.setCancelled(true);
									return;
								}
							} else if ((!pInfoSentTo.isGhost() && pInfoSpawned.isGhost())) {
								ev.setCancelled(true);
								return;
							}
							if (pInfoSentTo.isGhost() && pInfoSpawned.isGhost()) {
								ev.setCancelled(false);
							}
						}
					}
				}

			}
		});

		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.HIGHEST, ENTITY_PACKETS) {
			@Override
			public void onPacketSending(PacketEvent ev) {
				try {
					if (ev.getPlayer() == null || ev.getPlayer().getWorld() == null) {
						return;
					}
					World world = ev.getPlayer().getWorld();
					Entity entity = ev.getPacket().getEntityModifier(world).read(0);
					if (entity != null && entity instanceof Player) {
						PlayerInfo sendPacketPlayerInfo = Main.getPlayersManager().getPlayerInfo((Player) entity);
						if (sendPacketPlayerInfo != null) {
							if (sendPacketPlayerInfo.getIsIngame() && sendPacketPlayerInfo.isGhost()) {
								PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(ev.getPlayer());
								if (!pInfo.isGhost()) {
									ev.setCancelled(true);
								}
							}
						}
					}
				} catch (Exception e) {
				}
			}
		});
	}

	public static void sendPlayerToLobby(Player player) {
		if (!plugin.isEnabled()) {
			return;
		}
		if (configManager.getBungeecord()) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(configManager.getBungeecordLobbyServer());
			player.sendPluginMessage(Main.getPlugin(), "BungeeCord", out.toByteArray());
		}
	}

	public static void sendPlayerToArena(Player player, String server) {
		if (!plugin.isEnabled()) {
			return;
		}
		if (configManager.getBungeecord()) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(server);
			player.sendPluginMessage(Main.getPlugin(), "BungeeCord", out.toByteArray());
		}
	}

	public static void reloadConfigs() {
		for (Arena arena : arenaManager.getAllArenas()) {
			arena.deleteHolograms();
			arena.endGame(false);
			for (Camera cam : arena.getCamerasManager().getCameras()) {
				cam.deleteArmorStands();
			}
		}
		configManager.loadConfigVars();
		messagesManager.loadAll();
		itemsManager.loadItems();
		soundsManager.loadSounds();
		arenaManager.loadArenas();
		cosmeticsManager.loadCosmetics();
	}

	private static Boolean runAsync = true;

	public void onDisable() {
		if (arenaManager != null) {
			for (Arena arena : arenaManager.getAllArenas()) {
				arena.deleteHolograms();
				arena.endGame(true);
				arena.delete();
			}
		}
		if (configManager != null) {
			configManager.delete();
		}
		if (messagesManager != null) {
			messagesManager.delete();
		}
		if (itemsManager != null) {
			itemsManager.delete();
		}
		if (soundsManager != null) {
			soundsManager.delete();
		}
		if (playersManager != null) {
			playersManager.delete();
		}
		if(cosmeticsManager != null) {
			cosmeticsManager.delete();
		}
	}

	public static Plugin getPlugin() {
		return plugin;
	}

	public static Boolean shouldRunAsync() {
		return runAsync;
	}

	public static MessagesManager getMessagesManager() {
		return messagesManager;
	}

	public static ItemsManager getItemsManager() {
		return itemsManager;
	}

	public static PlayersManager getPlayersManager() {
		return playersManager;
	}

	public static ArenaManager getArenaManager() {
		return arenaManager;
	}

	public static ConfigManager getConfigManager() {
		return configManager;
	}

	public static SoundsManager getSoundsManager() {
		return soundsManager;
	}

	public static Boolean getIsVentureChat() {
		return isVentureChat;
	}

	public static Boolean getIsPlaceHolderAPI() {
		return isPlaceHolderAPI;
	}

	public static BungeArenaManager getBungeArenaManager() {
		return bungeArenaManager;
	}

	public static Boolean getIsPlayerPoints() {
		return isPlayerPoints;
	}

	public static PlayerPointsAPI getPlayerPointsApi() {
		return playerPointsApi;
	}

	public static CosmeticsManager getCosmeticsManager() {
		return cosmeticsManager;
	}
}
