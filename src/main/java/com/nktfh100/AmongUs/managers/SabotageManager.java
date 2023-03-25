package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.enums.SabotageLength;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.DoorGroup;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.SabotageArena;
import com.nktfh100.AmongUs.inventory.MeetingBtnInv;
import com.nktfh100.AmongUs.inventory.sabotages.SabotageCommsInv;
import com.nktfh100.AmongUs.inventory.sabotages.SabotageInvHolder;
import com.nktfh100.AmongUs.inventory.sabotages.SabotageLightsInv;
import com.nktfh100.AmongUs.inventory.sabotages.SabotageOxygenInv;
import com.nktfh100.AmongUs.inventory.sabotages.SabotageReactorInv;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class SabotageManager {

	private Arena arena;
	private Integer sabotageTimer = 45;
	// uuid - timer: per player
	private HashMap<String, Integer> sabotageCoolDownTimer = new HashMap<String, Integer>();
	// uuid - bossbar: per player
	private HashMap<String, BossBar> sabotageCooldownBossBar = new HashMap<String, BossBar>();
	private HashMap<String, Integer> sabotageCooldownBossBarMax = new HashMap<String, Integer>();

	private SabotageArena activeSabotage = null;
	private Boolean isSabotageActive = false;
	private Boolean isTimerActive = false;
	private BossBar bossbar;
	private BukkitTask timerRunnable;
	private Boolean isTimerPaused = false;

	private SabotageInvHolder saboInvHolder = null; // for shared inv

	private ArrayList<Integer> oxygenCode = new ArrayList<Integer>(); // for oxygen

	public SabotageManager(Arena arena) {
		this.arena = arena;
		this.bossbar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SEGMENTED_10);
		this.bossbar.setProgress(1);

	}

	public void startSabotage(SabotageArena sabo) {
		if (this.isSabotageActive) {
			return;
		}
		for (String uuid : this.sabotageCoolDownTimer.keySet()) {
			this.setSabotageCoolDownTimer(uuid, this.arena.getSabotageCooldown());
		}
		this.activeSabotage = sabo;
		this.activeSabotage.resetTasksDone();
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("%sabotage%", Main.getMessagesManager().getTaskName(this.getActiveSabotage().getType().toString()));
		for (Player p : this.arena.getPlayers()) {
			String saboTitle = Main.getMessagesManager().getGameMsg("sabotageTitle", this.arena, placeholders, p);
			String saboSubTitle = Main.getMessagesManager().getGameMsg("sabotageSubTitle", this.arena, placeholders, p);
			this.bossbar.addPlayer(p);
			p.sendTitle(saboTitle, saboSubTitle, 15, 40, 15);
		}
		this.isSabotageActive = true;
		SabotageManager manager = this;
		if (this.activeSabotage.getHasTimer()) {
			this.sabotageTimer = this.activeSabotage.getTimer();

			this.isTimerActive = true;
			if (this.timerRunnable != null) {
				this.timerRunnable.cancel();
			}
			this.timerRunnable = new BukkitRunnable() {

				@Override
				public void run() {
					manager.timerTick();
				}
			}.runTaskTimer(Main.getPlugin(), 20L, 20L);
		} else {
			this.isTimerActive = false;
		}
		this.updateBossBar();
		this.activeSabotage.showHolos();

		for (PlayerInfo pInfo : arena.getGameImposters()) {
			int s_ = 9;
			String uuid = pInfo.getPlayer().getUniqueId().toString();
			for (DoorGroup dg : arena.getDoorsManager().getDoorGroups()) {
				dg.setCooldownTimer(uuid, arena.getDoorCooldown());
				pInfo.getPlayer().getInventory().setItem(s_, arena.getDoorsManager().getSabotageDoorItem(pInfo.getPlayer(), dg.getId()));
				s_++;
			}
		}

		for (Player player : arena.getPlayers()) {
			Main.getSoundsManager().playSound("sabotageStarted", player, player.getLocation());
		}

		if (this.activeSabotage.getType() == SabotageType.LIGHTS) {

//			if (!Main.getConfigManager().getEnableBlindness()) {
//				new BukkitRunnable() { // decrease vision slowly
//					Integer vision = arena.getCrewmateVision() - 1;
//
//					@Override
//					public void run() {
//						if (!manager.getIsSabotageActive()) {
//							this.cancel();
//							return;
//						}
//						if (vision <= arena.getLightsOutVision()) {
//							this.cancel();
//							return;
//						}
//						for (PlayerInfo pInfo : arena.getPlayersInfo()) {
//							if (!pInfo.isGhost() && !pInfo.getIsImposter()) {
//								pInfo.setVision(vision);
//								arena.getVisibilityManager().playerMoved(pInfo);
//							}
//						}
//						vision--;
//					}
//				}.runTaskTimer(Main.getPlugin(), 7L, 7L);
//			}

			for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
				if (!pInfo.isGhost() && !pInfo.getIsImposter()) {
					pInfo.removeVisionBlocks();
					pInfo.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, true, false));
				} else if (pInfo.getIsImposter()) {
					pInfo.getPlayer().setFoodLevel(6);
				}
			}
			for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
				if (!pInfo.isGhost() && !pInfo.getIsImposter()) {
					for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
						if (pInfo != pInfo1 && !pInfo1.isGhost()) {
							this.arena.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
						}
					}
				}
			}
		} else if (this.activeSabotage.getType() == SabotageType.COMMUNICATIONS) {
			this.arena.updateScoreBoard();
		} else if (this.activeSabotage.getType() == SabotageType.OXYGEN) {
			this.oxygenCode.clear();
			for (int i = 0; i < 5; i++) {
				this.oxygenCode.add(Utils.getRandomNumberInRange(0, 9));
			}
		}

		// Update the meeting button inventory
		for (Player player : arena.getPlayers()) {
			if (player.getOpenInventory().getTopInventory().getHolder() instanceof MeetingBtnInv) {
				((MeetingBtnInv) player.getOpenInventory().getTopInventory().getHolder()).update();
			}
		}
	}

	public void endSabotage(Boolean didFix, Boolean isForce, Player playerFixed) {
		for (Player p : this.arena.getPlayers()) {
			this.bossbar.removePlayer(p);
		}
		if (this.timerRunnable != null) {
			this.timerRunnable.cancel();
		}

		if (playerFixed != null) {
			Main.getConfigManager().executeCommands("sabotageFix", playerFixed);
			Main.getCosmeticsManager().addCoins("sabotageFix", playerFixed);
		}

		this.isSabotageActive = false;

		for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
			if (pInfo.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof SabotageInvHolder) {
				pInfo.getPlayer().closeInventory();
			}
			if (!pInfo.isGhost()) {
				pInfo.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
				if (pInfo.getIsImposter() && !this.arena.getDisableSprinting()) {
					pInfo.getPlayer().setFoodLevel(20);
				}
				if (this.arena.getEnableReducedVision()) {
					if (!pInfo.getIsImposter()) {
						pInfo.setVision(this.arena.getCrewmateVision());
						if (!isForce) {
							this.arena.getVisibilityManager().playerMoved(pInfo);
						}
					}
				} else {
					pInfo.removeVisionBlocks();
					for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
						if (pInfo != pInfo1) {
							if (pInfo.isGhost()) {
								this.arena.getVisibilityManager().showPlayer(pInfo1, pInfo, true);
								this.arena.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
							}
						}
					}
				}
			}
			if (didFix) {
				Main.getSoundsManager().playSound("sabotageFixed", pInfo.getPlayer(), pInfo.getPlayer().getLocation());
			}
		}

		if (didFix && !isForce) {
			this.arena.sendTitle("sabotageFixedTitle", null);
		}

		if (!didFix && this.activeSabotage != null) {
			this.activeSabotage.hideHolo(0);
			this.activeSabotage.hideHolo(1);
		}

		this.saboInvHolder = null;
		this.isTimerActive = false;
		this.activeSabotage = null;
		this.isTimerPaused = false;
		for (PlayerInfo pInfo : arena.getGameImposters()) {
			int s_ = 9;
			String uuid = pInfo.getPlayer().getUniqueId().toString();
			this.setSabotageCoolDownTimer(uuid, this.arena.getSabotageCooldown());
			for (DoorGroup dg : arena.getDoorsManager().getDoorGroups()) {
				dg.setCooldownTimer(uuid, 0);
				pInfo.getPlayer().getInventory().setItem(s_, arena.getDoorsManager().getSabotageDoorItem(pInfo.getPlayer(), dg.getId()));
				s_++;
			}
		}
		this.arena.updateScoreBoard();

		// Update the meeting button inventory
		for (Player player : arena.getPlayers()) {
			if (player.getOpenInventory().getTopInventory().getHolder() instanceof MeetingBtnInv) {
				((MeetingBtnInv) player.getOpenInventory().getTopInventory().getHolder()).update();
			}
		}

		if (!isForce) {
			if (!didFix) {
				this.arena.gameWin(true);
			} else {
				this.arena.getTasksManager().updateTasksDoneBar(true);
			}
		}

	}

//	public void sabotageDoors(Integer doorGroupId) {
//		DoorGroup doorGroup = arena.getDoorsManager().getDoorGroup(doorGroupId);
//		if (doorGroup == null || doorGroup.getCloseTimer() > 0 || this.isSabotageActive) {
//			return;
//		}
//
//	}

	public void timerTick() {
		if (!isTimerPaused) {
			this.sabotageTimer--;
			this.updateBossBar();
			if (sabotageTimer < 0) {
				this.endSabotage(false, false, null);
			}
		}
	}

	public void updateBossBar() {
		if (this.activeSabotage != null) {
			String ext = "";
			String timeLeft = "";
			if (this.isTimerActive) {
				double progress = (double) this.sabotageTimer / (double) this.activeSabotage.getTimer();
				if (progress >= 0 && progress <= 1) {
					this.bossbar.setProgress(progress);
				}
				timeLeft = this.sabotageTimer.toString();
			} else {
				this.bossbar.setProgress(1);
			}
			if (this.activeSabotage.getLength() == SabotageLength.DOUBLE) {
				ext += "(";
				Integer num = 0;
				if (this.activeSabotage.getTaskDone(0)) {
					num++;
				}
				if (this.activeSabotage.getTaskDone(1)) {
					num++;
				}
				ext += num + "/2)";
			} else if (this.activeSabotage.getLength() == SabotageLength.DOUBLE_SAME_TIME) {
				ext += "(";
				Integer num = 0;
				if (this.activeSabotage.getTaskActive(0)) {
					num++;
				}
				if (this.activeSabotage.getTaskActive(1)) {
					num++;
				}
				ext += num + "/2)";
			}
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%title%", Main.getMessagesManager().getSabotageTitle(this.activeSabotage.getType()));
			placeholders.put("%name%", Main.getMessagesManager().getTaskName(this.activeSabotage.getType().toString()));

			for (PlayerInfo imposter : this.arena.getGameImposters()) {
				if (this.isTimerActive) {
					placeholders.put("%time%", timeLeft);
					placeholders.put("%sabotages_fixed%", ext);
					this.bossbar.setTitle(Main.getMessagesManager().getGameMsg("sabotageBossBarTimer", arena, placeholders, imposter.getPlayer()));
				} else {
					this.bossbar.setTitle(Main.getMessagesManager().getGameMsg("sabotageBossBar", arena, placeholders, imposter.getPlayer()));
				}
			}
		}
	}

	public void sabotageHoloClick(Player player, Integer clickedTaskId) {
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if (!this.isSabotageActive || this.activeSabotage == null || pInfo == null || !pInfo.getIsIngame() || pInfo.isGhost() || pInfo.getArena().getIsInMeeting() || pInfo.getIsInCameras()) {
			return;
		}

		if (pInfo.getIsImposter()) {
			pInfo.setKillCoolDownPaused(true);
		}

		Main.getSoundsManager().playSound("sabotageInvOpen", player, player.getLocation());

		switch (this.activeSabotage.getType()) {
		case LIGHTS:
			if (this.saboInvHolder == null) {
				this.saboInvHolder = new SabotageLightsInv(this.getActiveSabotage(), player);
			}

			player.openInventory(this.saboInvHolder.getInventory());
			return;
		case COMMUNICATIONS:
			player.openInventory(new SabotageCommsInv(this.getActiveSabotage(), player).getInventory());
			return;
		case REACTOR_MELTDOWN:
			player.openInventory(new SabotageReactorInv(this.getActiveSabotage(), clickedTaskId, player).getInventory());
			return;
		case OXYGEN:
			player.openInventory(new SabotageOxygenInv(this.getActiveSabotage(), clickedTaskId, this.oxygenCode, player).getInventory());
			return;
		default:
			this.activeSabotage.taskDone(player);
			break;
		}
	}

	public void setSabotageCoolDownTimer(String uuid, Integer sabotageCoolDownTimer) {
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerByUUID(uuid);
		if (pInfo == null) {
			return;
		}
		if (!pInfo.getIsImposter()) {
			return;
		}
		Player player = pInfo.getPlayer();
		Integer sabotageCoolDownTimerP = this.getSabotageCoolDownTimer(player);
		BossBar sabotageCooldownBossBarP = this.getSabotageCooldownBossBar(player);
		if (sabotageCoolDownTimerP > 0 && sabotageCoolDownTimer == 0) {
			sabotageCooldownBossBarP.removePlayer(player);
		} else if (sabotageCoolDownTimerP == 0 && sabotageCoolDownTimer > 0) { // this.sabotageCoolDownTimer == 0 &&
			sabotageCooldownBossBarP.addPlayer(player);
		}
		Integer maxSecs = this.sabotageCooldownBossBarMax.get(uuid) == null ? arena.getSabotageCooldown() : this.sabotageCooldownBossBarMax.get(uuid);
		if (sabotageCoolDownTimer > sabotageCoolDownTimerP) {
			maxSecs = sabotageCoolDownTimer;
			this.sabotageCooldownBossBarMax.put(uuid, maxSecs);
		}
		double progress = (double) sabotageCoolDownTimer / (double) maxSecs;

		if (progress >= 0 && progress <= 1) {
			sabotageCooldownBossBarP.setProgress(progress);
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%time%", String.valueOf(sabotageCoolDownTimer));
			sabotageCooldownBossBarP.setTitle(Main.getMessagesManager().getGameMsg("sabotageCooldownBossBar", arena, placeholders, player));
		}

		if (!arena.getIsInMeeting()) {
			if (!pInfo.getIsInVent() && !pInfo.getIsInCameras()) {
				for (SabotageArena sa : arena.getSabotages()) {
					ItemInfoContainer saboInfo = this.getSabotageItemInfo(sa.getType());
					String name = Main.getMessagesManager().getTaskName(sa.getType().toString());
					pInfo.getPlayer().getInventory().setItem(saboInfo.getSlot(), getSabotageItem(sa.getType(), name, sabotageCoolDownTimer));
				}
			}
		}
		this.sabotageCoolDownTimer.put(uuid, sabotageCoolDownTimer);
	}

	public ItemInfoContainer getSabotageItemInfo(SabotageType st) {
		String key = "sabotage_";
		switch (st) {
		case OXYGEN:
			key += "oxygen";
			break;
		case REACTOR_MELTDOWN:
			key += "reactor";
			break;
		case COMMUNICATIONS:
			key += "comms";
			break;
		default:
			key += "lights";
			break;
		}

		return Main.getItemsManager().getItem(key);
	}

	public ItemStack getSabotageItem(SabotageType st, String name, Integer saboCoolDownTimer_) {
		if (saboCoolDownTimer_ == null) {
			saboCoolDownTimer_ = 0;
		}
		ItemInfoContainer sabotageItem = getSabotageItemInfo(st);
		String saboCoolDown = saboCoolDownTimer_.toString();
		Material mat = saboCoolDownTimer_ == 0 ? sabotageItem.getItem2().getMat() : sabotageItem.getItem().getMat();
		String title = saboCoolDownTimer_ == 0 ? sabotageItem.getItem2().getTitle(name, saboCoolDown) : sabotageItem.getItem().getTitle(name, saboCoolDown);
		ArrayList<String> lore = saboCoolDownTimer_ == 0 ? sabotageItem.getItem2().getLore(name, saboCoolDown) : sabotageItem.getItem().getLore(name, saboCoolDown);
		return Utils.createItem(mat, title, saboCoolDownTimer_ > 0 ? saboCoolDownTimer_ : 1, lore);
	}

	public void addImposter(Player player) {
		String uuid = player.getUniqueId().toString();
		this.sabotageCoolDownTimer.put(uuid, 0);
		this.sabotageCooldownBossBar.put(uuid, Bukkit.createBossBar(Main.getMessagesManager().getGameMsg("sabotageCooldownBossBar", this.arena, null, player), BarColor.RED, BarStyle.SOLID));
	}

	public void removeImposter(String uuid) {
		this.sabotageCoolDownTimer.remove(uuid);
		if (this.sabotageCooldownBossBar.get(uuid) != null) {
			this.sabotageCooldownBossBar.get(uuid).removeAll();
		}
		this.sabotageCooldownBossBar.remove(uuid);
		this.sabotageCooldownBossBarMax.remove(uuid);

	}

	public void resetImposters() {
		this.sabotageCoolDownTimer.clear();
		for (String uuid : this.sabotageCooldownBossBar.keySet()) {
			this.sabotageCooldownBossBar.get(uuid).removeAll();
		}
		this.sabotageCooldownBossBar.clear();
		this.sabotageCooldownBossBarMax.clear();
	}

	public void delete() {
		this.arena = null;
		this.sabotageCoolDownTimer = null;
		this.sabotageCooldownBossBar = null;
		this.sabotageCooldownBossBarMax = null;
		this.activeSabotage = null;
		this.isSabotageActive = false;
		this.isTimerActive = false;
		this.bossbar = null;
		this.timerRunnable = null;
		this.isTimerPaused = null;
		this.saboInvHolder = null;
	}

	public void addPlayerToBossBar(Player player) {
		this.bossbar.addPlayer(player);
	}

	public void removePlayerFromBossBar(Player player) {
		this.bossbar.removePlayer(player);
	}

	public SabotageArena getActiveSabotage() {
		return this.activeSabotage;
	}

	public Arena getArena() {
		return this.arena;
	}

	public Integer getSabotageTimer() {
		return this.sabotageTimer;
	}

	public void setSabotageTimer(Integer sabotageTimer) {
		this.sabotageTimer = sabotageTimer;
	}

	public Boolean getIsSabotageActive() {
		return isSabotageActive;
	}

	public Boolean getIsTimerPaused() {
		return isTimerPaused;
	}

	public void setIsTimerPaused(Boolean isTimerPaused) {
		this.isTimerPaused = isTimerPaused;
	}

	public Integer getSabotageCoolDownTimer(Player player) {
		return this.sabotageCoolDownTimer.get(player.getUniqueId().toString());
	}

	public BossBar getSabotageCooldownBossBar(Player player) {
		return this.sabotageCooldownBossBar.get(player.getUniqueId().toString());
	}
}
