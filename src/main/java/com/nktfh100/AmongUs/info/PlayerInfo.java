package com.nktfh100.AmongUs.info;

import java.util.ArrayList;
import java.util.UUID;

import com.nktfh100.AmongUs.holograms.ImposterHologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.nktfh100.AmongUs.enums.CosmeticType;
import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.RoleType;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.MessagesManager;
import com.nktfh100.AmongUs.managers.StatsManager;
import com.nktfh100.AmongUs.utils.Utils;

public class PlayerInfo {

	private Player player;
	private Arena arena = null;
	private Integer joinedId = 0;
	private Boolean isInGame = false;
	private Boolean isGhost = false;
	private RoleType role = null;
	
	private ColorInfo color;
	private ColorInfo preferredColor = null;
	private Integer meetingsLeft = 1;
	private Integer killCoolDown = 0;
	private Integer sabotageCoolDown = 30;
	private Boolean canSabotage = false;
	private Boolean canReportBody = false;
	private Integer vision = 10;

	private Boolean isMapInOffHand = true;

	private Boolean isInVent = false;
	private VentGroup ventGroup = null;
	private Vent vent = null;

	private Boolean isInCameras = false;
	private Camera activeCamera = null;

	private DeadBody playerDiedTemp = null;
	private Location playerCamLocTemp = null;

	// For bukkit API
	private Scoreboard board;
	private Objective objective;
	private short currentMapId = -1;

	private int outOfAreaTimeOut; // remove

	private String originalPlayerListName;

	private ImposterHologram imposterHolo = null;

	private BossBar killCooldownBossBar = null;

	private Boolean killCoolDownPaused = false;

	private ArrayList<FakeBlock> tempReducedVisBlocks = new ArrayList<FakeBlock>();
	private ArrayList<Player> playersHidden = new ArrayList<Player>();

	private Integer fakePlayerId = (int) (Math.random() * Integer.MAX_VALUE);
	private UUID fakePlayerUUID = UUID.randomUUID();

	private String textureValue = "";
	private String textureSignature = "";
	private ItemStack head;

	private FakePlayer fakePlayer;

	// For use item
	private Integer useItemState = 0;
	private TaskPlayer useItemTask = null;
	private SabotageTask useItemSabotage = null;
	private Vent useItemVent = null;
	private Double useDistance = 2.5;

	// For medbay visual task
	private Boolean isScanning = false;
	private ArrayList<FakeArmorStand> scanArmorStands = new ArrayList<FakeArmorStand>();

	private StatsManager statsManager = null;

	private long portalCooldown = System.currentTimeMillis();

	private GameMode gameModeBefore = GameMode.SURVIVAL;
	private Float expBefore = 0f;
	private ItemStack[] inventoryBefore = null;
	private ItemStack[] inventoryExtraBefore = null;
	private ItemStack[] inventoryArmorBefore = null;

	public PlayerInfo(Player player) {
		this.player = player;
		this.originalPlayerListName = player.getPlayerListName();
		PlayerInfo pInfo = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					pInfo.setHead(Utils.getHead(pInfo.getPlayer().getName()));
					String[] textures = Utils.getSkinData(pInfo.getPlayer().getName());
					if (!textures[0].isEmpty()) {
						pInfo.setTextureValue(textures[0]);
						pInfo.setTextureSignature(textures[1]);
					}
				} catch (Exception e) {
					System.out.println("Could not get skin data for: " + pInfo.getPlayer().getName());
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(Main.getPlugin());
		this.scanArmorStands = new ArrayList<FakeArmorStand>();
		for (int i = 0; i < 4; i++) {
			this.scanArmorStands.add(new FakeArmorStand(pInfo, player.getLocation(), new Vector3F(90, 0, 0), null));
		}
		this.statsManager = new StatsManager(this);
		if (!Main.getConfigManager().getBungeecord() && Main.getConfigManager().getEnableLobbyScoreboard()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (pInfo != null && !pInfo.getIsIngame()) {
						pInfo._setMainLobbyScoreboard();
					}
				}
			}.runTaskLater(Main.getPlugin(), 20L);
		}
	}

	public void _setPlayer(Player p) {
		this.player = p;
	}

	public void _setMainLobbyScoreboard() {
		this.board = null;
		this.objective = null;
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		this.objective = this.board.registerNewObjective(this.player.getName(), "dummy", Main.getMessagesManager().getScoreboard("title"));
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.player.setScoreboard(this.board);
		this.setScoreBoard();
	}

	public String getCustomName() {
		if (this.color != null && this.arena != null) {
			return Main.getMessagesManager().getGameMsg("tabName", this.getArena(), this.player.getName(), this.color.getChatColor() + "", this.color.getName(), null);
		} else {
			return this.player.getName();
		}
	}

	public void initGame(Arena arena, Integer joinedId) {
		this.arena = arena;
		this.joinedId = joinedId;
		this.role = RoleType.CREWMATE; // only known when game starts
		this.isInGame = true;
		this.killCoolDownPaused = false;
		this.isScanning = false;
		if (Main.getConfigManager().getSaveInventory()) {
			this.setGameModeBefore(player.getGameMode());
			this.setExpBefore(player.getExp());
			this.setInventoryBefore(player.getInventory().getStorageContents().clone());
			this.setInventoryExtraBefore(player.getInventory().getExtraContents().clone());
			this.setInventoryArmorBefore(player.getInventory().getArmorContents().clone());
		}
		for (FakeArmorStand fakeArmorStand : this.scanArmorStands) {
			fakeArmorStand.resetAllShownTo();
		}
		String title = Main.getMessagesManager().getScoreboard("title");

		this.board = null;
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		this.objective = this.board.registerNewObjective(this.player.getName(), "dummy", title);
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.setScoreBoard();
		this.player.setScoreboard(this.board);
	}

	private Boolean isAlreadyRunning = false;

	public void updateVisionBlocks(Location newLoc) {
		if (isAlreadyRunning || this.getIsInCameras()) {
			return;
		}

		if (this.arena != null && this.arena.getEnableReducedVision() && Main.getConfigManager() != null && Main.getConfigManager().getViewGlassMat() != Material.AIR) {
			isAlreadyRunning = true;
			Location loc = new Location(newLoc.getWorld(), newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ());
			loc.add(0, -1, 0);
			int height = 5;
			if (loc.getBlock().isEmpty()) { // to keep the height the same relative to the player (when jumping)
				loc.add(0, -1, 0);
			}

			ArrayList<Location> newLocs = Utils.generateHollowCircle(loc, this.vision, height);
			ArrayList<FakeBlock> newFakeBlocks = new ArrayList<FakeBlock>();

			for (Location loc_ : newLocs) {
				if (loc_.getBlock().getType() == Material.AIR) {
					FakeBlock fb = new FakeBlock(loc_, loc_.getBlock().getType(), Main.getConfigManager().getViewGlassMat(), WrappedBlockData.createData(loc_.getBlock().getBlockData()));
					newFakeBlocks.add(fb);
					fb.sendNewBlock(this.player);
				}
			}

			for (FakeBlock oldFB : this.tempReducedVisBlocks) {
				Boolean isOk = true;
				for (FakeBlock newFB : newFakeBlocks) {
					if (newFB.getLoc().getBlockX() == oldFB.getLoc().getBlockX()) {
						if (newFB.getLoc().getBlockY() == oldFB.getLoc().getBlockY()) {
							if (newFB.getLoc().getBlockZ() == oldFB.getLoc().getBlockZ()) {
								isOk = false;
								break;
							}
						}
					}
				}
				if (isOk) {
					Boolean send = true;
					// To prevent bug where the blocks remove door blocks
					for (DoorGroup dg : this.arena.getDoorsManager().getDoorGroups()) {
						if (dg.getCloseTimer() > 0) {
							for (Door door : dg.getDoors()) {
								if (door.getBlocks_().contains(oldFB.getBlock())) {
									send = false;
									break;
								}
							}
						}
					}
					if (send) {
						oldFB.sendOldBlock(this.player);
					}
				}
			}

			this.tempReducedVisBlocks = newFakeBlocks;
			isAlreadyRunning = false;
		}
	}

	public void removeVisionBlocks() {
		for (FakeBlock oldFB : this.tempReducedVisBlocks) {
			oldFB.sendOldBlock(this.player);
		}
		this.tempReducedVisBlocks.clear();
	}

	public void startGame(RoleType givenRole) {
		this.role = givenRole;
		if (this.getIsImposter()) {
			this.killCooldownBossBar = Bukkit.createBossBar(Main.getMessagesManager().getGameMsg("killCooldownBossBar", this.getArena(), ""), BarColor.RED, BarStyle.SOLID);
			this.killCooldownBossBar.setProgress(1);
			this.killCooldownBossBar.addPlayer(this.player);
		}

		this.board = null;
		this.objective = null;
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		this.objective = this.board.registerNewObjective(this.player.getName(), "dummy", Main.getMessagesManager().getScoreboard("title"));
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		this.setScoreBoard();

		Team crewmates = this.board.registerNewTeam("crewmates");
		crewmates.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
		crewmates.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		crewmates.setAllowFriendlyFire(false);
		crewmates.setCanSeeFriendlyInvisibles(false);
		crewmates.setColor(ChatColor.WHITE);
		crewmates.setPrefix("");

		Team imposters = this.board.registerNewTeam("imposters");
		imposters.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
		imposters.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		imposters.setAllowFriendlyFire(false);
		imposters.setCanSeeFriendlyInvisibles(false);
		imposters.setColor(ChatColor.RED);
		imposters.setPrefix("");

		Team ghosts = this.board.registerNewTeam("ghosts");
		ghosts.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
		ghosts.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
		ghosts.setAllowFriendlyFire(false);
		ghosts.setCanSeeFriendlyInvisibles(true);
		ghosts.setColor(ChatColor.GRAY);
		ghosts.setPrefix("");

		this.addPlayerToTeam(this.getPlayer(), this.getIsImposter() ? "imposters" : "crewmates");
		this.player.setScoreboard(this.board);

		this.canReportBody = false;
	}

	public void meetingStarted() {
		if (this.board == null) {
			return;
		}

		Team crewmates = this.board.getTeam("crewmates");
		if (crewmates == null) {
			crewmates = this.board.registerNewTeam("crewmates");
			crewmates.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			crewmates.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
			crewmates.setAllowFriendlyFire(false);
			crewmates.setCanSeeFriendlyInvisibles(false);
			crewmates.setColor(ChatColor.WHITE);
			crewmates.setPrefix("");
			return;
		}
		crewmates.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);

		Team imposters = this.board.getTeam("imposters");
		if (imposters == null) {
			imposters = this.board.registerNewTeam("imposters");
			imposters.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			imposters.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
			imposters.setAllowFriendlyFire(false);
			imposters.setCanSeeFriendlyInvisibles(false);
			imposters.setColor(ChatColor.RED);
			imposters.setPrefix("");
			return;
		}
		imposters.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
	}

	public void meetingEnded() {
		Team crewmates = this.board.getTeam("crewmates");
		if (crewmates == null) {
			crewmates = this.board.registerNewTeam("crewmates");
			crewmates.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			crewmates.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
			crewmates.setAllowFriendlyFire(false);
			crewmates.setCanSeeFriendlyInvisibles(false);
			crewmates.setColor(ChatColor.WHITE);
			crewmates.setPrefix("");
			return;
		}
		crewmates.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

		Team imposters = this.board.getTeam("imposters");
		if (imposters == null) {
			imposters = this.board.registerNewTeam("imposters");
			imposters.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			imposters.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
			imposters.setAllowFriendlyFire(false);
			imposters.setCanSeeFriendlyInvisibles(false);
			imposters.setColor(ChatColor.RED);
			imposters.setPrefix("");
			return;
		}
		imposters.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
	}

	public void createImposterHolo() {
		this.imposterHolo = ImposterHologram.createHologram(this.getPlayer().getLocation().add(0, 2.8, 0), "imposterHolo");
		this.imposterHolo.addLineWithItem(Utils.createItem(Material.RED_CONCRETE, " "));
		this.imposterHolo.setGlobalVisibility(false);
		for (PlayerInfo pInfo1 : this.arena.getGameImposters()) {
			if (pInfo1 != this) {
				this.imposterHolo.showTo(pInfo1.getPlayer());
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void addPlayerToTeam(Player player, String team) {
		if (player == null) {
			return;
		}

		if (this.board != null && this.board.getTeam(team) != null) {
			this.board.getTeam(team).addPlayer(player);
		}
	}

	@SuppressWarnings("deprecation")
	public void removePlayerFromTeam(Player player, String team) {
		if (player == null) {
			return;
		}
		if (this.board != null && this.board.getTeam(team) != null) {
			this.board.getTeam(team).removePlayer(player);
		}
	}

	private String activeKey = "";

	private void setScoreBoard() {
		MessagesManager messagesManager = Main.getMessagesManager();
		this.activeKey = this.getScoreBoardKey();

		ArrayList<String> lines = new ArrayList<String>();
		int i = 0;
		int score = 99;
		for (String line : messagesManager.getScoreBoardLines(this.activeKey)) {
			if (line.contains("%tasks%")) {
				for (TaskPlayer tp : this.getArena().getTasksManager().getTasksForPlayer(this.player)) {
					String line_ = messagesManager.getScoreboardTaskLine(arena, tp);

					Team team_ = this.registerTeam(score);
					team_.setPrefix(line_);
					score--;
				}
				i++;
			} else {
				String line_ = messagesManager.getScoreboardLine(this.getScoreBoardKey(), i, this);

				Team team_ = this.registerTeam(score);
				team_.setPrefix(line_);
				score--;
				i++;
			}
		}
	}

	public void updateScoreBoard() {
		if (this.board == null) {
			return;
		}
		Boolean isCommsDisabled = false;
		if (this.getIsIngame()) {
			if (this.arena.getGameState() == GameState.RUNNING || this.arena.getGameState() == GameState.FINISHING) {
				if (this.arena.getSabotageManager().getIsSabotageActive()) {
					if (this.arena.getSabotageManager().getActiveSabotage().getType() == SabotageType.COMMUNICATIONS) {
						isCommsDisabled = true;
						for (Team team : this.board.getTeams()) {
							if (team.getName().startsWith("team")) {
								for (String entry : team.getEntries()) {
									team.removeEntry(entry);
									this.board.resetScores(entry);
								}
								team.unregister();
							}
						}
					}
				}
			}
		}

		ArrayList<String> lines = new ArrayList<String>();
		int i = 0;
		int score = 99;
		MessagesManager messagesManager = Main.getMessagesManager();
		for (String line : messagesManager.getScoreBoardLines(this.getScoreBoardKey())) {
			if (line.contains("%tasks%")) {
				if (!isCommsDisabled) {
					for (TaskPlayer tp : this.getArena().getTasksManager().getTasksForPlayer(this.getPlayer())) {
						String line_ = messagesManager.getScoreboardTaskLine(this.getArena(), tp);
						if (this.board.getTeam("team" + score) == null) {
							this.registerTeam(score);
						}
						this.board.getTeam("team" + score).setPrefix(line_);
						score--;
					}
				} else {
					String line_ = ChatColor.RED + "" + ChatColor.BOLD + Main.getMessagesManager().getSabotageTitle(SabotageType.COMMUNICATIONS);
					if (this.board.getTeam("team" + score) == null) {
						this.registerTeam(score);
					}
					this.board.getTeam("team" + score).setPrefix(line_);
					score--;
				}
				i++;
			} else {
				String line_ = messagesManager.getScoreboardLine(this.getScoreBoardKey(), i, this);
				if (this.board.getTeam("team" + score) == null) {
					this.registerTeam(score);
				}
				this.board.getTeam("team" + score).setPrefix(line_);
				score--;
				i++;
			}
		}

		if (activeKey != this.getScoreBoardKey()) {
			for (Team team : this.board.getTeams()) {
				if (team.getName().startsWith("team")) {
					for (String entry : team.getEntries()) {
						team.removeEntry(entry);
						this.board.resetScores(entry);
					}
					team.unregister();
				}
			}
			this.activeKey = this.getScoreBoardKey();
			this.updateScoreBoard();
		}
	}

	private String getScoreBoardKey() {
		if (this.getIsIngame()) {
			if (this.getArena().getGameState() == GameState.RUNNING || this.getArena().getGameState() == GameState.FINISHING) {
				String linesKey = this.getIsImposter() ? "imposter" : "crewmate";
				if (this.isGhost()) {
					linesKey = this.getIsImposter() ? "dead-imposter" : "dead-crewmate";
				}
				return linesKey;
			} else {
				return "waiting-lobby";
			}
		} else {
			return "main-lobby";
		}
	}

	private Team registerTeam(int score) {
		Team team_ = this.board.registerNewTeam("team" + score);
		String entry = Utils.getRandomColors();
		team_.addEntry(entry);
		this.objective.getScore(entry).setScore(score);
		return team_;
	}

	public void giveArmor() {
		player.getEquipment().setHelmet(this.getHelmet());
		player.getEquipment().setChestplate(this.getChestplate());
		player.getEquipment().setLeggings(this.getLeggings());
		player.getEquipment().setBoots(this.getBoots());
	}

	public ItemStack getHelmet() {
		if (Main.getConfigManager().getEnableGlassHelmet()) {
			return new ItemStack(this.color.getGlass(), 1);
		} else {
			return Utils.getArmorColor(this.color, Material.LEATHER_HELMET);
		}
	}

	public ItemStack getChestplate() {
		return Utils.getArmorColor(this.color, Material.LEATHER_CHESTPLATE);
	}

	public ItemStack getLeggings() {
		return Utils.getArmorColor(this.color, Material.LEATHER_LEGGINGS);
	}

	public ItemStack getBoots() {
		return Utils.getArmorColor(this.color, Material.LEATHER_BOOTS);
	}

	// 0 - cant use
	// 1 - meeting button
	// 2 - task
	// 3 - sabotage
	// 4 - vent
	// 5 - cameras
	// 6 - vitals
	public void setUseItemState(Integer useItemState, Boolean updateItem) {
		this.useItemState = useItemState;
		if (updateItem) {
			ItemInfoContainer useItem = Main.getItemsManager().getItem("use");
			if (useItemState == 0) {
				this.player.getInventory().setItem(useItem.getSlot(), useItem.getItem().getItem());
			} else {
				this.player.getInventory().setItem(useItem.getSlot(), useItem.getItem2().getItem());
			}
		}
	}

	public void updateUseItemState(Location loc) {
		if (this.arena == null) {
			return;
		}
		// meeting button
		if (this.arena.getMeetingButton() != null && !this.isGhost()) {
			if (Utils.isInsideCircle(this.arena.getMeetingButton(), useDistance, loc) != 2) {
				this.setUseItemState(1, true);
				return;
			}
		}

		// cameras
		if (arena.getCamerasLoc() != null) {
			if (Utils.isInsideCircle(this.arena.getCamerasLoc(), useDistance, loc) != 2) {
				this.setUseItemState(5, true);
				return;
			}
		}

		// vitals
		if (arena.getVitalsLoc() != null) {
			if (Utils.isInsideCircle(this.arena.getVitalsLoc(), useDistance, loc) != 2) {
				this.setUseItemState(6, true);
				return;
			}
		}

		if (!this.getIsImposter()) {
			// tasks
			for (TaskPlayer task : arena.getTasksManager().getTasksForPlayer(this.getPlayer())) {
				if (!task.getIsDone()) {
					if (Utils.isInsideCircle(task.getActiveTask().getLocation(), useDistance, loc) != 2) {
						this.setUseItemTask(task);
						this.setUseItemState(2, true);
						return;
					}
				}
			}

		} else {
			// vents
			for (VentGroup vg : arena.getVentsManager().getVentGroups()) {
				for (Vent v : vg.getVents()) {
					if (Utils.isInsideCircle(v.getLoc(), useDistance, loc) != 2) {
						this.setUseItemVent(v);
						this.setUseItemState(4, true);
						return;
					}
				}
			}

		}
		// sabotages
		if (arena.getSabotageManager().getIsSabotageActive() && !this.isGhost()) {
			SabotageArena activeSabotage = arena.getSabotageManager().getActiveSabotage();
			if (Utils.isInsideCircle(activeSabotage.getTask1().getLocation(), useDistance, loc) != 2) {
				this.setUseItemSabotage(activeSabotage.getTask1());
				this.setUseItemState(3, true);
				return;
			}
			if (activeSabotage.getTask2() != null) {
				if (Utils.isInsideCircle(activeSabotage.getTask2().getLocation(), useDistance, loc) != 2) {
					this.setUseItemSabotage(activeSabotage.getTask2());
					this.setUseItemState(3, true);
					return;
				}
			}
		}

		if (this.getUseItemState() != 0) {
			this.setUseItemState(0, true);
		}
	}

	public void setCanReportBody(Boolean is, DeadBody db) {
		if (this.isInGame && !this.isGhost) {
			ItemInfoContainer reportItem = Main.getItemsManager().getItem("report");
			if (is) {
				this.playerDiedTemp = db;
				String playerName = db.getPlayer().getName();
				String playerColorName = db.getColor().getName();
				String playerColor = db.getColor().getChatColor() + "";
				this.player.getInventory().setItem(1, Utils.setItemName(Utils.getHead(db.getPlayer().getName()), reportItem.getItem2().getTitle(playerName, playerColorName, playerColor, null, null),
						reportItem.getItem2().getLore(playerName, playerColorName, playerColor, null, null)));
			} else {
				this.playerDiedTemp = null;
				this.player.getInventory().setItem(1, reportItem.getItem().getItem());
			}
		}
		this.canReportBody = is;
	}

	public void leaveGame() {
		if (this.getIsImposter()) {
			this.killCooldownBossBar.removePlayer(this.player);
			this.killCooldownBossBar = null;
		}
		if (this.imposterHolo != null) {
			this.imposterHolo.deleteHologram();
			this.imposterHolo = null;
		}
		this.arena = null;
		this.isInGame = false;
		this.canReportBody = false;
		this.currentMapId = -1;
		this.joinedId = 0;
		this.setIsGhost(false);
		this.isScanning = false;
		for (FakeArmorStand fakeArmorStand : this.scanArmorStands) {
			fakeArmorStand.resetAllShownTo();
		}
		this.useItemState = 0;

		if (this.board != null) {
			for (Team team : this.board.getTeams()) {
				for (String entry : team.getEntries()) {
					team.removeEntry(entry);
					this.board.resetScores(entry);
				}
				team.unregister();
			}
		}
		if (this.objective != null) {
			this.objective.unregister();
		}
		this.board = null;
		this.objective = null;
		this.player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		if (!Main.getConfigManager().getBungeecord() && Main.getConfigManager().getEnableLobbyScoreboard()) {
			this._setMainLobbyScoreboard();
			this.updateScoreBoard();
		}
		if (Main.getConfigManager().getSaveInventory()) {
			this.player.setGameMode(this.gameModeBefore);
			this.player.setExp(this.expBefore);
			this.player.getInventory().setContents(this.inventoryBefore);
			this.player.getInventory().setExtraContents(this.inventoryExtraBefore);
			this.player.getInventory().setArmorContents(this.inventoryArmorBefore);
		}
	}

	public void teleportImposterHolo() {
		if (this.getImposterHolo() != null) {
			this.getImposterHolo().setLocation(this.getPlayer().getLocation().add(0, 2.8, 0));
		}
	}

	public void setKillCoolDown(Integer killCoolDown) {
		if (this.killCoolDown > 0 && killCoolDown == 0) {
			this.killCooldownBossBar.removePlayer(this.player);
		} else if (this.killCoolDown == 0 && killCoolDown > 0) {
			this.killCooldownBossBar.addPlayer(this.player);
		}
		Integer maxSecs = this.arena.getKillCooldown();
		double progress = (double) killCoolDown / (double) maxSecs;

		if (progress >= 0 && progress <= 1) {
			this.killCooldownBossBar.setProgress(progress);
			this.killCooldownBossBar.setTitle(Main.getMessagesManager().getGameMsg("killCooldownBossBar", arena, killCoolDown + ""));
		}
		if (!arena.getIsInMeeting() && !this.isGhost() && !this.getIsInVent() && !this.getIsInCameras()) {
			this.giveKillItem(killCoolDown);
		}

		this.killCoolDown = killCoolDown;
	}

	public void sendTitle(String title, String subTitle) {
		if (title.isEmpty() && subTitle.isEmpty()) {
			return;
		}
		this.player.sendTitle(title, subTitle, 15, 80, 15);
	}

	public void sendTitle(String title, String subTitle, Integer fadeIn, Integer stay, Integer fadeOut) {
		if (title.isEmpty() && subTitle.isEmpty()) {
			return;
		}
		this.player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
	}

	public void giveKillItem(Integer killCoolDown_) {
		String killCoolDownStr = killCoolDown_.toString();
		ItemInfoContainer killItem = Main.getItemsManager().getItem("kill");
		ItemStack item_ = killCoolDown_ == 0 ? killItem.getItem2().getItem(killCoolDownStr, "") : killItem.getItem().getItem(killCoolDownStr, "");
		if (killCoolDown_ == 0) {
			if (Main.getIsPlayerPoints()) {
				if (this.getStatsManager().getSelectedCosmetic(CosmeticType.KILL_SWORD) != null && !this.getStatsManager().getSelectedCosmetic(CosmeticType.KILL_SWORD).isEmpty()) {
					item_.setType(Main.getCosmeticsManager().getCosmeticItem(CosmeticType.KILL_SWORD, this.getStatsManager().getSelectedCosmetic(CosmeticType.KILL_SWORD)).getMat());
				} else {
					item_.setType(Main.getCosmeticsManager().getCosmeticItem(CosmeticType.KILL_SWORD, Main.getCosmeticsManager().getDefaultCosmetic(CosmeticType.KILL_SWORD)).getMat());
				}
			}
		}
		this.player.getInventory().setItem(killItem.getSlot(), item_);
	}

	public void delete() {
		this.arena = null;
		this.joinedId = null;
		this.isInGame = null;
		this.isGhost = null;
		this.color = null;
		this.meetingsLeft = null;
		this.killCoolDown = null;
		this.sabotageCoolDown = null;
		this.canSabotage = null;
		this.canReportBody = null;
		this.vision = null;
		this.isMapInOffHand = null;
		this.isInVent = null;
		this.ventGroup = null;
		this.vent = null;
		this.isInCameras = null;
		this.activeCamera = null;
		this.playerDiedTemp = null;
		this.playerCamLocTemp = null;
		this.board = null;
		this.objective = null;
		this.imposterHolo = null;
		this.killCooldownBossBar = null;
		this.killCoolDownPaused = null;
		this.tempReducedVisBlocks = null;
		this.playersHidden = null;
		this.fakePlayerId = null;
		this.fakePlayerUUID = null;
		this.textureValue = null;
		this.textureSignature = null;
		this.head = null;
		this.fakePlayer = null;
		this.useItemState = null;
		this.useItemTask = null;
		this.useItemSabotage = null;
		this.useItemVent = null;
		this.useDistance = null;
		this.scanArmorStands = null;
		this.preferredColor = null;
		this.statsManager.delete();
		this.statsManager = null;
	}

	public void setColor(ColorInfo color) {
		this.color = color;
	}

	public Boolean isGhost() {
		return this.isGhost;
	}

	public void setIsGhost(Boolean is) {
		this.isGhost = is;
	}

	public ColorInfo getColor() {
		return color;
	}

	public Boolean getCanSabotage() {
		return canSabotage;
	}

	public void setCanSabotage(Boolean canSabotage) {
		this.canSabotage = canSabotage;
	}

	public Integer getKillCoolDown() {
		return killCoolDown;
	}

	public Integer getSabotageCoolDown() {
		return sabotageCoolDown;
	}

	public void setSabotageCoolDown(Integer sabotageCoolDown) {
		this.sabotageCoolDown = sabotageCoolDown;
	}

	public Integer getMeetingsLeft() {
		return meetingsLeft;
	}

	public void setMeetingsLeft(Integer meetingsLeft) {
		this.meetingsLeft = meetingsLeft;
	}

	public String getOriginalPlayerListName() {
		return originalPlayerListName;
	}

	public ImposterHologram getImposterHolo() {
		return imposterHolo;
	}

	public Boolean getCanReportBody() {
		return canReportBody;
	}

	public DeadBody getPlayerDiedTemp() {
		return playerDiedTemp;
	}

//	public Scoreboard getScoreBoard() {
//		return this.board;
//	}

	public void setMapId(short id) {
		this.currentMapId = id;
	}

	public short getMapId() {
		return this.currentMapId;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Arena getArena() {
		return arena;
	}

	public Boolean getIsImposter() {
		return this.role == RoleType.IMPOSTER || this.role == RoleType.SHAPESHIFTER;
	}

	public int getOutOfAreaTimeOut() {
		return this.outOfAreaTimeOut;
	}

	public void setOutOfAreaTimeOut(int i) {
		this.outOfAreaTimeOut = i;
	}

	public Boolean getIsIngame() {
		return this.isInGame;
	}

	public BossBar getKillCooldownBossBar() {
		return killCooldownBossBar;
	}

	public Boolean getIsInVent() {
		return isInVent;
	}

	public void setIsInVent(Boolean isInVent) {
		this.isInVent = isInVent;
	}

	public VentGroup getVentGroup() {
		return ventGroup;
	}

	public void setVentGroup(VentGroup ventGroup) {
		this.ventGroup = ventGroup;
	}

	public Vent getVent() {
		return vent;
	}

	public void setVent(Vent vent) {
		this.vent = vent;
	}

	public Boolean getIsInCameras() {
		return isInCameras;
	}

	public void setIsInCameras(Boolean isInCameras) {
		this.isInCameras = isInCameras;
	}

	public Camera getActiveCamera() {
		return activeCamera;
	}

	public void setActiveCamera(Camera activeCamera) {
		this.activeCamera = activeCamera;
	}

	public Location getPlayerCamLocTemp() {
		return playerCamLocTemp;
	}

	public void setPlayerCamLocTemp(Location playerCamLocTemp) {
		this.playerCamLocTemp = playerCamLocTemp;
	}

	public ArrayList<Player> getPlayersHidden() {
		return playersHidden;
	}

	public void setPlayersHidden(ArrayList<Player> playersHidden) {
		this.playersHidden = playersHidden;
	}

	public Integer getVision() {
		return vision;
	}

	public void setVision(Integer vision) {
		this.vision = vision;
	}

	public Boolean getIsMapInOffHand() {
		return isMapInOffHand;
	}

	public void setIsMapInOffHand(Boolean isMapInOffHand) {
		this.isMapInOffHand = isMapInOffHand;
	}

	public Integer getFakePlayerId() {
		return fakePlayerId;
	}

	public UUID getFakePlayerUUID() {
		return fakePlayerUUID;
	}

	public String getTextureValue() {
		return textureValue;
	}

	public String getTextureSignature() {
		return textureSignature;
	}

	public void setTextureValue(String value) {
		this.textureValue = value;
	}

	public void setTextureSignature(String value) {
		this.textureSignature = value;
	}

	public FakePlayer getFakePlayer() {
		return fakePlayer;
	}

	public void setFakePlayer(FakePlayer fakePlayer) {
		this.fakePlayer = fakePlayer;
	}

	public Boolean getKillCoolDownPaused() {
		return killCoolDownPaused;
	}

	public void setKillCoolDownPaused(Boolean killCoolDownPaused) {
		this.killCoolDownPaused = killCoolDownPaused;
	}

	public Integer getUseItemState() {
		return useItemState;
	}

	public TaskPlayer getUseItemTask() {
		return useItemTask;
	}

	public void setUseItemTask(TaskPlayer useItemTask) {
		this.useItemTask = useItemTask;
	}

	public SabotageTask getUseItemSabotage() {
		return useItemSabotage;
	}

	public void setUseItemSabotage(SabotageTask useItemSabotage) {
		this.useItemSabotage = useItemSabotage;
	}

	public Vent getUseItemVent() {
		return useItemVent;
	}

	public void setUseItemVent(Vent useItemVent) {
		this.useItemVent = useItemVent;
	}

	public Integer getJoinedId() {
		return joinedId;
	}

	public void setJoinedId(Integer joinedId) {
		this.joinedId = joinedId;
	}

	public Boolean getIsScanning() {
		return isScanning;
	}

	public void setIsScanning(Boolean isScanning) {
		this.isScanning = isScanning;
	}

	public ArrayList<FakeArmorStand> getScanArmorStands() {
		return scanArmorStands;
	}

	public ItemStack getHead() {
		return head;
	}

	public void setHead(ItemStack head) {
		this.head = head;
	}

	public ColorInfo getPreferredColor() {
		return preferredColor;
	}

	public void setPreferredColor(ColorInfo preferredColor) {
		this.preferredColor = preferredColor;
	}

	public StatsManager getStatsManager() {
		return statsManager;
	}

	public long getPortalCooldown() {
		return portalCooldown;
	}

	public void setPortalCooldown(long portalCooldown) {
		this.portalCooldown = portalCooldown;
	}

	public ItemStack[] getInventoryBefore() {
		return inventoryBefore;
	}

	public void setInventoryBefore(ItemStack[] inventoryBefore) {
		this.inventoryBefore = inventoryBefore;
	}

	public ItemStack[] getInventoryExtraBefore() {
		return inventoryExtraBefore;
	}

	public void setInventoryExtraBefore(ItemStack[] inventoryExtraBefore) {
		this.inventoryExtraBefore = inventoryExtraBefore;
	}

	public ItemStack[] getInventoryArmorBefore() {
		return inventoryArmorBefore;
	}

	public void setInventoryArmorBefore(ItemStack[] inventoryArmorBefore) {
		this.inventoryArmorBefore = inventoryArmorBefore;
	}

	public GameMode getGameModeBefore() {
		return gameModeBefore;
	}

	public void setGameModeBefore(GameMode gameModeBefore) {
		this.gameModeBefore = gameModeBefore;
	}

	public Float getExpBefore() {
		return expBefore;
	}

	public void setExpBefore(Float expBefore) {
		this.expBefore = expBefore;
	}

	public RoleType getRole() {
		return role;
	}

	public void setRole(RoleType role) {
		this.role = role;
	}
}
