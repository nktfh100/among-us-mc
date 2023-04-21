package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.enums.StatInt;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.DeadBody;
import com.nktfh100.AmongUs.info.DoorGroup;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.VitalsPlayerInfo;
import com.nktfh100.AmongUs.inventory.VotingInv;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Packets;

public class MeetingManager {

	public enum meetingState {
		DISCUSSION, VOTING, VOTING_RESULTS
	};

	private Arena arena;

	private Integer meetingCooldownTimer = 10;
	private Integer activeTimer = 0;

	private BukkitTask timerRunnable = null;
	private meetingState state;

	private ArrayList<Player> playersVoted = new ArrayList<Player>();
	private HashMap<String, ArrayList<PlayerInfo>> votes = new HashMap<String, ArrayList<PlayerInfo>>();
	private ArrayList<PlayerInfo> skipVotes = new ArrayList<PlayerInfo>();

	private SabotageType activeSabotage = null; // if sabotage is active before the body was found
	private Player whoCalled;

	private Boolean isSendingTitle = false;

	public MeetingManager(Arena arena) {
		this.arena = arena;
	}

	public void callMeeting(Player caller, Boolean isBodyFound, DeadBody db) {
		if (isBodyFound && this.arena.getSabotageManager().getIsSabotageActive()) {
			SabotageType saboType = this.arena.getSabotageManager().getActiveSabotage().getType();
			if (saboType != SabotageType.COMMUNICATIONS) {
				this.arena.getSabotageManager().endSabotage(false, true, null);
				if (saboType == SabotageType.LIGHTS) {
					this.activeSabotage = saboType;
				}
			}
		}
		arena.setIsInMeeting(true);
		this.isSendingTitle = false;
		PlayerInfo callerInfo = Main.getPlayersManager().getPlayerInfo(caller);

		if (!isBodyFound) {
			callerInfo.setMeetingsLeft(callerInfo.getMeetingsLeft() - 1);
		}

		this.playersVoted.clear();
		this.skipVotes.clear();
		this.votes = new HashMap<String, ArrayList<PlayerInfo>>();
		int si = 0;
		for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
			Player player = pInfo.getPlayer();

			if (!pInfo.isGhost()) {
				for (PotionEffect pe : player.getActivePotionEffects()) {
					player.removePotionEffect(pe.getType());
				}
				// hide ghosts tab names
				for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
					if(pInfo1 == null) {
						continue;
					}
					if (pInfo1.isGhost() && pInfo1 != pInfo) {
						Packets.sendPacket(pInfo.getPlayer(), Packets.REMOVE_PLAYER(pInfo.getPlayer(), pInfo1.getPlayer().getUniqueId()));
					}
				}
			} else {
				VitalsPlayerInfo vpi = arena.getVitalsManager().getVitalsPInfo(player);
				vpi.setIsDead(true);
				vpi.setIsDC(true);
			}

			pInfo.meetingStarted();
			this.votes.put(pInfo.getPlayer().getUniqueId().toString(), new ArrayList<PlayerInfo>());

			Boolean showPlayerToEveryone = false;

			if (pInfo.getIsImposter()) {
				if (pInfo.getIsInVent()) {
					this.arena.getVentsManager().playerLeaveVent(pInfo, true, false);
					showPlayerToEveryone = true;
				}
				pInfo.setKillCoolDown(0);
				this.arena.getSabotageManager().getSabotageCooldownBossBar(pInfo.getPlayer()).removePlayer(pInfo.getPlayer());
			}
			if (pInfo.getIsInCameras()) {
				this.arena.getCamerasManager().playerLeaveCameras(pInfo, true);
				showPlayerToEveryone = true;
			}

			if (si >= this.arena.getPlayerSpawns().size()) {
				si = 0;
			}

			pInfo.setCanReportBody(false, null);
			player.teleport(this.arena.getPlayerSpawns().get(si));
			player.setAllowFlight(false);

			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%caller_name%", player.getName());
			placeholders.put("%caller_color%", callerInfo.getColor().getChatColor() + "");
			placeholders.put("%caller_color_name%", callerInfo.getColor().getName());

			if (isBodyFound && db != null) {
				placeholders.put("%dead_player_name%", db.getPlayer().getName());
				placeholders.put("%dead_player_color%", db.getColor().getChatColor() + "");
				placeholders.put("%dead_player_color_name%", db.getColor().getName());
				placeholders.put("%called_player_name%", pInfo.getPlayer().getName());
				placeholders.put("%called_player_color%", pInfo.getColor().getChatColor() + "");
				placeholders.put("%called_player_color_name%", pInfo.getColor().getName());
				player.sendTitle(
						Main.getMessagesManager().getGameMsg("bodyFoundTitle", arena, placeholders, player),
						Main.getMessagesManager().getGameMsg("bodyFoundSubTitle", arena, placeholders, player),
						15, 80, 15);

			} else {
				player.sendTitle(
						Main.getMessagesManager().getGameMsg("emergencyMeetingTitle", arena, placeholders, player),
						Main.getMessagesManager().getGameMsg("emergencyMeetingSubTitle", arena, placeholders, player),
						15, 80, 15);
			}

			if (pInfo.getIsImposter()) {
				pInfo.teleportImposterHolo();
			}

			pInfo.removeVisionBlocks();

			if (isBodyFound) {
				Main.getSoundsManager().playSound("bodyReported", player, player.getLocation());
			} else {
				Main.getSoundsManager().playSound("meetingStarted", player, player.getLocation());
			}

			if (!pInfo.isGhost()) {
				for (PlayerInfo pInfo1 : this.arena.getPlayersInfo()) {
					if (pInfo != pInfo1 && !pInfo1.isGhost()) {
						this.arena.getVisibilityManager().showPlayer(pInfo, pInfo1, true);
						if (showPlayerToEveryone) {
							this.arena.getVisibilityManager().showPlayer(pInfo1, pInfo, true);
						}
					}
				}
			}

			ItemInfo voteItem = Main.getItemsManager().getItem("vote").getItem();
			player.getInventory().clear();
			pInfo.giveArmor();
			player.getInventory().setItem(voteItem.getSlot(), voteItem.getItem());
			si++;
		}

		this.arena.getDeadBodiesManager().deleteAll();

		this.arena.getDoorsManager().openDoorsForce();

		if (isBodyFound) {
			Main.getConfigManager().executeCommands("reportedBody", caller);
			callerInfo.getStatsManager().plusOneStatInt(StatInt.BODIES_REPORTED);
			Main.getCosmeticsManager().addCoins("reportedBody",  caller);
		} else {
			Main.getConfigManager().executeCommands("calledMeeting", caller);
			callerInfo.getStatsManager().plusOneStatInt(StatInt.EMERGENCIES_CALLED);
			Main.getCosmeticsManager().addCoins("calledMeeting",  caller);
		}

		this.whoCalled = caller;
		this.setState(meetingState.DISCUSSION);
		this.setActiveTimer(this.arena.getDiscussionTime());
		if (this.timerRunnable != null) {
			this.timerRunnable.cancel();
		}
		MeetingManager manager = this;
		this.timerRunnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (manager.getActiveTimer() > 0) {
					manager.setActiveTimer(manager.getActiveTimer() - 1);
				} else {
					switch (manager.getState()) {
					case DISCUSSION:
						manager.setState(meetingState.VOTING);
						manager.setActiveTimer(manager.getArena().getVotingTime());
						return;
					case VOTING:
						manager.setState(meetingState.VOTING_RESULTS);
						manager.setActiveTimer(arena.getProceedingTime());
						for (PlayerInfo pInfo : manager.getArena().getPlayersInfo()) {
							if (pInfo.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof VotingInv) {
								manager.openVoteInv(pInfo);
							}
						}
						manager.sendVotingResults();
						return;
					case VOTING_RESULTS:
//						manager.endMeeting(false);
						manager.startEndMeetingTitle();
						this.cancel();
						return;
					default:
						break;
					}
				}
			}
		}.runTaskTimer(Main.getPlugin(), 20L, 20L);
		this.updateInv();
		arena.getBtnHolo().clearVisibility(false);
	}

	public void startEndMeetingTitle() {
		MeetingManager manager = this;
		this.isSendingTitle = true;
		for (Player p : this.getArena().getPlayers()) {
			if (p.getOpenInventory().getTopInventory().getHolder() instanceof VotingInv) {
				p.closeInventory();
			}
		}

		Integer highestVotes = this.skipVotes.size();
		String highestPlayer = "skip";

		Integer highestVotes2 = 0;

		for (Entry<String, ArrayList<PlayerInfo>> en : this.votes.entrySet()) {
			if (en.getValue().size() > highestVotes) {
				highestVotes = en.getValue().size();
				highestPlayer = en.getKey();
			} else if (en.getValue().size() > highestVotes2) {
				highestVotes2 = en.getValue().size();
			}
		}

		PlayerInfo pInfoEject_ = null;

		String title_ = "";
		String subTitle_ = "";

		if (highestVotes == highestVotes2 || highestPlayer == "skip") {
			String cause = "tie";
			if (highestPlayer == "skip") {
				cause = "skipped";
			}
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%cause%", Main.getMessagesManager().getGameMsg(cause, arena, null, null));
			placeholders.put("%remaining_imposters%", String.valueOf(arena.getImpostersAlive().size()));
			title_ = Main.getMessagesManager().getGameMsg("noOneEjectedTitle" + (arena.getConfirmEjects() ? "" : "1"), arena, placeholders, null);
			subTitle_ = Main.getMessagesManager().getGameMsg("noOneEjectedSubTitle" + (arena.getConfirmEjects() ? "" : "1"), arena, placeholders, null);
		} else {
			if (highestPlayer != "skip") {
				pInfoEject_ = Main.getPlayersManager().getPlayerByUUID(highestPlayer);
				Integer numImposters = this.arena.getImpostersAlive().size();
				if (pInfoEject_.getIsImposter()) {
					numImposters--;
				}
				String titleKey = "playerWasTheImposterTitle";
				String subTitleKey = "playerWasTheImposterSubTitle";
				if (arena.getConfirmEjects()) {
					if (this.arena.getNumImposters() == 1) {
						if (!pInfoEject_.getIsImposter()) {
							titleKey = "playerWasNotTheImposterTitle";
							subTitleKey = "playerWasNotTheImposterSubTitle";
						}
					} else {
						if (pInfoEject_.getIsImposter()) {
							titleKey = "playerWasAnImposterTitle";
							subTitleKey = "playerWasAnImposterSubTitle";
						} else {
							titleKey = "playerWasNotAnImposterTitle";
							subTitleKey = "playerWasNotAnImposterSubTitle";
						}
					}
				} else {
					titleKey = "playerEjectedTitle";
					subTitleKey = "playerEjectedSubTitle";
				}
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%ejected_player_name%", pInfoEject_.getPlayer().getName());
				placeholders.put("%ejected_player_color%", pInfoEject_.getColor().getChatColor() + "");
				placeholders.put("%ejected_player_color_name%", pInfoEject_.getColor().getName());
				placeholders.put("%remaining_imposters%", String.valueOf(numImposters));
				title_ = Main.getMessagesManager().getGameMsg(titleKey, arena, placeholders, null);
				subTitle_ = Main.getMessagesManager().getGameMsg(subTitleKey, arena, placeholders, null);
			}
		}

		for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
			pInfo.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false));
		}

		final PlayerInfo pInfoEject = pInfoEject_;
		final String title = title_;
		final String subTitle = subTitle_;

		new BukkitRunnable() {
			int subTitleIndex = 0;

			@Override
			public void run() {
				if (manager.getArena().getGameState() != GameState.RUNNING) {
					this.cancel();
					return;
				}
				if (subTitleIndex < subTitle.toCharArray().length) {
					manager.getArena().sendTitle("", subTitle.substring(0, subTitleIndex + 1), 0, 35, 0);
					subTitleIndex++;
				} else {
					new BukkitRunnable() {
						@Override
						public void run() {
							if (manager.getArena().getGameState() != GameState.RUNNING) {
								this.cancel();
								return;
							}

							manager.getArena().sendTitle(title, subTitle, 0, 40, 15);
							new BukkitRunnable() {
								@Override
								public void run() {
									if (manager.getArena().getGameState() != GameState.RUNNING) {
										this.cancel();
										return;
									}
									for (PlayerInfo pInfo : manager.getArena().getPlayersInfo()) {
										pInfo.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
									}
									manager.endMeeting(false, pInfoEject);
								}
							}.runTaskLater(Main.getPlugin(), 40L);
						}
					}.runTaskLater(Main.getPlugin(), 30L);
					this.cancel();
					return;
				}

			}
		}.runTaskTimer(Main.getPlugin(), 0L, 2L);
	}

	public void endMeeting(Boolean isForce, PlayerInfo pInfoEject) {

		this.setState(meetingState.DISCUSSION);
		this.setActiveTimer(0);
		if (this.timerRunnable != null) {
			this.timerRunnable.cancel();
		}

		arena.setIsInMeeting(false);
		this.setIsSendingTitle(false);

		for (PlayerInfo pInfo : this.arena.getPlayersInfo()) {
			this.arena.giveGameInventory(pInfo);
			pInfo.meetingEnded();
			if (!isForce) {
				this.arena.getVisibilityManager().playerMoved(pInfo);
			}
			if (pInfo.isGhost()) {
				if (Main.getConfigManager().getGhostsFly()) {
					pInfo.getPlayer().setAllowFlight(true);
				}
			}
		}
		this.votes = new HashMap<String, ArrayList<PlayerInfo>>();
		this.playersVoted.clear();
		this.skipVotes.clear();
		this.whoCalled = null;
		arena.getBtnHolo().clearVisibility(true);

		if (pInfoEject != null && !isForce) {
			this.arena.playerDeath(null, pInfoEject, false);
		}

		this.setMeetingCooldownTimer(arena.getMeetingCooldown());
		for (PlayerInfo pInfo_ : this.arena.getGameImposters()) {
			int s_ = 9;
			arena.getSabotageManager().setSabotageCoolDownTimer(pInfo_.getPlayer().getUniqueId().toString(), this.arena.getSabotageCooldown());
			for (DoorGroup dg : arena.getDoorsManager().getDoorGroups()) {
				dg.setCooldownTimer(pInfo_.getPlayer().getUniqueId().toString(), arena.getDoorCooldown());
				pInfo_.getPlayer().getInventory().setItem(s_, arena.getDoorsManager().getSabotageDoorItem(pInfo_.getPlayer(), dg.getId()));
				s_++;
			}
			if (!pInfo_.isGhost()) {
				pInfo_.setKillCoolDown(this.arena.getKillCooldown());
			}
		}

		if (pInfoEject != null) {
			Main.getConfigManager().executeCommands("ejected", pInfoEject.getPlayer());
			pInfoEject.getStatsManager().plusOneStatInt(StatInt.TIMES_EJECTED);
			Main.getCosmeticsManager().addCoins("ejected",  pInfoEject.getPlayer());
		}

		if (!isForce) {
			Integer winState = arena.getWinState(false);
			if (winState != 0) {
				arena.setGameState(GameState.FINISHING);
				for (PlayerInfo pInfo : arena.getPlayersInfo()) {
					pInfo.removeVisionBlocks();
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						arena.gameWin(winState == 2);
					}
				}.runTaskLater(Main.getPlugin(), 80L);
			} else {
				if (this.activeSabotage != null) {
					this.arena.getSabotageManager().startSabotage(this.arena.getSabotageArena(this.activeSabotage));
					this.activeSabotage = null;
				}
			}
		}
	}

	public void sendVotingResults() {
		MessagesManager messagesManager = Main.getMessagesManager();
		StringBuilder outputB = new StringBuilder();
		for (String uuid1 : this.votes.keySet()) {
			PlayerInfo pInfo1 = Main.getPlayersManager().getPlayerByUUID(uuid1);
			if (this.votes.get(uuid1).size() > 0) {
				String symbols = "";
				for (PlayerInfo voterInfo : this.votes.get(pInfo1.getPlayer().getUniqueId().toString())) {
					HashMap<String, String> placeholders = new HashMap<>();
					placeholders.put("%voter_color%", voterInfo.getColor().getChatColor() + "" );
					String symbol_ = messagesManager.getGameMsg("voteSymbol", arena, placeholders, null);
					symbols = symbols + symbol_;
				}
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("%voted_player_name%", pInfo1.getPlayer().getName());
				placeholders.put("%voted_player_color%", pInfo1.getColor().getChatColor() + "");
				placeholders.put("%voted_player_color_name%", pInfo1.getColor().getName());
				placeholders.put("%votes%", symbols);
				String line_ = messagesManager.getGameMsg("playerLine", arena, placeholders, null);
				outputB.append(line_ + "\n");
			}
		}

		String symbols = "";
		for (PlayerInfo voterInfo : this.skipVotes) {
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%voter_color%", voterInfo.getColor().getChatColor() + "");
			String symbol_ = messagesManager.getGameMsg("voteSymbol", arena, placeholders, null);
			symbols = symbols + symbol_;
		}
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("%votes%", symbols);
		outputB.append(messagesManager.getGameMsg("skipVoteLine", arena, placeholders, null));

		HashMap<String, String> placeholders2 = new HashMap<>();
		placeholders2.put("%results%", outputB.toString());

		this.getArena().sendMessage("votingResults", placeholders2);
	}

	public void updateInv() {
		for (Player player : this.arena.getPlayers()) {
			if (player.getOpenInventory().getTopInventory().getHolder() instanceof VotingInv) {
				((VotingInv) player.getOpenInventory().getTopInventory().getHolder()).update();
			}
		}
	}

	public void openVoteInv(PlayerInfo pInfo_) {
		if (this.getArena().getIsInMeeting()) {
			VotingInv votingInv = new VotingInv(this.arena, pInfo_);
			pInfo_.getPlayer().openInventory(votingInv.getInventory());
		}
	}

	public Boolean canVote(PlayerInfo pInfo) {
		return !this.playersVoted.contains(pInfo.getPlayer());
	}

	public void didEveryoneVote() {
		Integer size = 0;
		for (PlayerInfo pInfo_ : this.arena.getPlayersInfo()) {
			if (!pInfo_.isGhost()) {
				size++;
			}
		}
		if (playersVoted.size() >= size) {
			this.setState(meetingState.VOTING_RESULTS);
			this.setActiveTimer(10);
			for (PlayerInfo pInfo : this.getArena().getPlayersInfo()) {
				if (pInfo.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof VotingInv) {
					this.openVoteInv(pInfo);
				}
			}
			this.sendVotingResults();
		}
	}

	public void vote(PlayerInfo voter, PlayerInfo voted) {
		if (this.state == meetingState.VOTING && this.canVote(voter) && voter != null && voted != null && voted.getPlayer().isOnline()) {
			if (this.votes.get(voted.getPlayer().getUniqueId().toString()) != null) {
				this.votes.get(voted.getPlayer().getUniqueId().toString()).add(voter);
			} else {
				this.votes.put(voted.getPlayer().getUniqueId().toString(), new ArrayList<PlayerInfo>());
				this.votes.get(voted.getPlayer().getUniqueId().toString()).add(voter);
			}
			this.playersVoted.add(voter.getPlayer());
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%voter_name%", voter.getPlayer().getName());
			placeholders.put("%voter_color%", voter.getColor().getChatColor() + "");
			placeholders.put("%voter_color_name%", voter.getColor().getName());
			this.arena.sendMessage("playerVoted", placeholders);
			this.updateInv();
			this.didEveryoneVote();
			for (Player player : this.arena.getPlayers()) {
				Main.getSoundsManager().playSound("playerVoted", player, player.getLocation());
			}
		}
	}

	public void voteSkip(PlayerInfo voter) {
		if (this.state == meetingState.VOTING && this.canVote(voter)) {
			this.skipVotes.add(voter);
			this.playersVoted.add(voter.getPlayer());
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("%voter_name%", voter.getPlayer().getName());
			placeholders.put("%voter_color%", voter.getColor().getChatColor() + "");
			placeholders.put("%voter_color_name%", voter.getColor().getName());
			this.arena.sendMessage("playerVoted", placeholders);
			this.updateInv();
			this.didEveryoneVote();
			for (Player player : this.arena.getPlayers()) {
				Main.getSoundsManager().playSound("playerVoted", player, player.getLocation());
			}
		}
	}

	public void delete() {
		this.arena = null;
		this.meetingCooldownTimer = null;
		this.activeTimer = null;
		this.timerRunnable = null;
		this.state = null;
		this.playersVoted = null;
		this.votes = null;
		this.skipVotes = null;
		this.activeSabotage = null;
		this.whoCalled = null;
		this.isSendingTitle = null;
	}

	public Integer getActiveTimer() {
		return activeTimer;
	}

	public void setActiveTimer(Integer activeTimer) {
		this.activeTimer = activeTimer;
		this.updateInv();
	}

	public ArrayList<PlayerInfo> getVotes(Player p) {
		return this.votes.get(p.getUniqueId().toString());
	}

	public ArrayList<PlayerInfo> getSkippedVotes() {
		return this.skipVotes;
	}

	public meetingState getState() {
		return state;
	}

	public void setState(meetingState state) {
		this.state = state;
	}

	public Arena getArena() {
		return this.arena;
	}

	public Player getWhoCalled() {
		return whoCalled;
	}

	public void setWhoCalled(Player whoCalled) {
		this.whoCalled = whoCalled;
	}

	public Integer getMeetingCooldownTimer() {
		return meetingCooldownTimer;
	}

	public void setMeetingCooldownTimer(Integer meetingCooldownTimer) {
		this.meetingCooldownTimer = meetingCooldownTimer;
	}

	public Boolean getIsSendingTitle() {
		return isSendingTitle;
	}

	public void setIsSendingTitle(Boolean isSendingTitle) {
		this.isSendingTitle = isSendingTitle;
	}
}
