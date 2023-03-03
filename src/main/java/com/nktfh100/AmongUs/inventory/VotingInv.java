package com.nktfh100.AmongUs.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.MeetingManager;
import com.nktfh100.AmongUs.managers.MeetingManager.meetingState;
import com.nktfh100.AmongUs.utils.Utils;

public class VotingInv extends CustomHolder {

	private static final Integer pageSize = 10;

	private Arena arena;
	private PlayerInfo pInfo;
	private PlayerInfo activePInfo = null;
	private Boolean skipVoteActive = false;
	private Integer currentPage = 1;
	private ArrayList<PlayerInfo> players = new ArrayList<PlayerInfo>();

	public VotingInv(Arena arena, PlayerInfo pInfo) {
		super(54, arena.getMeetingManager().getState() == meetingState.VOTING_RESULTS ? Main.getMessagesManager().getGameMsg("votingInvTitle1", arena, null) : Main.getMessagesManager().getGameMsg("votingInvTitle", arena, null));
		this.arena = arena;
		this.pInfo = pInfo;
		Utils.fillInv(this.inv);
		// push ghosts to last
		this.players = new ArrayList<PlayerInfo>(arena.getPlayersInfo());
		ArrayList<PlayerInfo> playersGhosts = new ArrayList<PlayerInfo>(arena.getPlayersInfo());
		playersGhosts.removeIf(n -> (!n.isGhost()));
		this.players.removeIf(n -> (n.isGhost()));
		
		this.players.remove(this.pInfo);
		Collections.sort(this.players, new Comparator<PlayerInfo>() {
			@Override
			public int compare(PlayerInfo o1, PlayerInfo o2) {
				return o1.getJoinedId().compareTo(o2.getJoinedId());
			}
		});
		playersGhosts.remove(this.pInfo);
		Collections.sort(playersGhosts, new Comparator<PlayerInfo>() {
			@Override
			public int compare(PlayerInfo o1, PlayerInfo o2) {
				return o1.getJoinedId().compareTo(o2.getJoinedId());
			}
		});
		this.players.add(0, this.pInfo);
		this.players.addAll(playersGhosts);
		this.update();
	}

	private static final ArrayList<Integer> playersSlots = new ArrayList<Integer>(Arrays.asList(2, 6, 11, 15, 20, 24, 29, 33, 38, 42));

	public void update() {
		final MeetingManager manager = this.getArena().getMeetingManager();
		final VotingInv votingInv = this;
		this.icons.clear();

		ItemInfoContainer fillItem = Main.getItemsManager().getItem("voting_fill");
		if (manager.getState() == meetingState.VOTING && !this.pInfo.isGhost()) {
			if (manager.canVote(this.pInfo)) {
				Utils.fillInv(this.inv, fillItem.getItem2().getMat());
			} else {
				Utils.fillInv(this.inv, fillItem.getItem().getMat());
			}
		} else {
			Utils.fillInv(this.inv, fillItem.getItem().getMat());
		}

		Integer totalItems = this.players.size();
		Integer totalPages = (int) Math.ceil((double) totalItems / (double) pageSize);

		if (totalPages > 1) {
			this.inv.setItem(49, Main.getItemsManager().getItem("voting_currentPage").getItem().getItem(this.currentPage + "", totalPages + ""));

			if (currentPage > 1) {
				Icon icon = new Icon(Main.getItemsManager().getItem("voting_prevPage").getItem().getItem(this.currentPage + "", totalPages + ""));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						votingInv.setCurrentPage(currentPage - 1);
					}
				});
				this.setIcon(48, icon);
			}
			if (currentPage < totalPages) {
				Icon icon = new Icon(Main.getItemsManager().getItem("voting_nextPage").getItem().getItem(this.currentPage + "", totalPages + ""));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						votingInv.setCurrentPage(currentPage + 1);
					}
				});
				this.setIcon(50, icon);
			}
		}

		if (manager.getState() != meetingState.VOTING) {
			this.activePInfo = null;
			this.skipVoteActive = false;
		}

		String votedSkipLine = "";
		if (manager.getState() == meetingState.VOTING_RESULTS) {
			ArrayList<PlayerInfo> votedForSkip = manager.getSkippedVotes();
			StringBuilder votesLine = new StringBuilder();
			for (PlayerInfo voterPInfo : votedForSkip) {
				if (voterPInfo != null && voterPInfo.getColor() != null) {
					votesLine.append(Main.getMessagesManager().getGameMsg("voteSymbol", arena, "" + voterPInfo.getColor().getChatColor()));
				}
			}
			votedSkipLine = votesLine.toString();
		}
		ItemInfo deadIndicatorItem = Main.getItemsManager().getItem("voting_player_dead").getItem();
		ItemInfo votedIndicatorItem = Main.getItemsManager().getItem("voting_player_voted").getItem();

		Integer startIndex = (this.currentPage - 1) * pageSize;
		Integer endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

		Integer slotI = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			PlayerInfo pInfo_ = this.players.get(i);
			if (pInfo_ == null) {
				continue;
			}
			ItemInfoContainer playerItem = Main.getItemsManager().getItem("voting_player");
			if (this.pInfo.isGhost() || (!manager.canVote(this.pInfo))) {
				playerItem = Main.getItemsManager().getItem("voting_player_cantVote");
			}
			ArrayList<String> lore = new ArrayList<String>();

			if (manager.getWhoCalled() == pInfo_.getPlayer()) { // If this player called the meeting
				ItemInfo callerItem = Main.getItemsManager().getItem("voting_calledMeeting").getItem();
				this.inv.setItem(playersSlots.get(slotI) + 1, callerItem.getItem(pInfo_.getPlayer().getName(), null));
			}

			String playerValue3 = (pInfo.getIsImposter() && pInfo_.getIsImposter() ? ChatColor.DARK_RED + "" : "");
			String playerTitle = playerItem.getItem().getTitle(pInfo_.getPlayer().getName(), "" + pInfo_.getColor().getChatColor(), pInfo_.getColor().toString().toLowerCase(), playerValue3, null);
			Boolean didAddLore = false;
			if (manager.getState() == meetingState.VOTING_RESULTS) {
				ArrayList<PlayerInfo> votedForP = manager.getVotes(pInfo_.getPlayer());
				StringBuilder votesLine = new StringBuilder();
				for (PlayerInfo voterPInfo : votedForP) {
					if (voterPInfo != null && voterPInfo.getColor() != null && arena != null) {
						votesLine.append(Main.getMessagesManager().getGameMsg("voteSymbol", arena, "" + voterPInfo.getColor().getChatColor()));
					}
				}
				lore.add(votesLine.toString());
			}
			if (!pInfo_.isGhost() && !manager.canVote(pInfo_)) { // Player voted
				lore.addAll(playerItem.getItem2().getLore(pInfo_.getPlayer().getName(), "" + pInfo_.getColor().getChatColor(), pInfo_.getColor().toString().toLowerCase(), playerValue3, null));
				playerTitle = playerItem.getItem2().getTitle(pInfo_.getPlayer().getName(), "" + pInfo_.getColor().getChatColor(), pInfo_.getColor().toString().toLowerCase(), playerValue3, null);
				didAddLore = true;
				ItemStack votedIndicator_ = Utils.createItem(votedIndicatorItem.getMat(), votedIndicatorItem.getTitle(pInfo_.getPlayer().getName()), 1, votedIndicatorItem.getLore(pInfo_.getPlayer().getName()));
				this.inv.setItem(playersSlots.get(slotI) - 1, votedIndicator_);
			}
			if (pInfo_.isGhost()) { // Player is dead
				lore.addAll(playerItem.getItem3().getLore(pInfo_.getPlayer().getName(), "" + pInfo_.getColor().getChatColor(), pInfo_.getColor().toString().toLowerCase(), playerValue3, null));
				playerTitle = playerItem.getItem3().getTitle(pInfo_.getPlayer().getName(), "" + pInfo_.getColor().getChatColor(), pInfo_.getColor().toString().toLowerCase(), playerValue3, null);
				didAddLore = true;

				ItemStack deadIndicator_ = Utils.createItem(deadIndicatorItem.getMat(), deadIndicatorItem.getTitle(pInfo_.getPlayer().getName()), 1, deadIndicatorItem.getLore(pInfo_.getPlayer().getName()));
				this.inv.setItem(playersSlots.get(slotI) + 1, deadIndicator_);
				this.inv.setItem(playersSlots.get(slotI) - 1, deadIndicator_);
			}

			if (this.activePInfo == pInfo_ && !this.skipVoteActive) {
				ItemInfo acceptItem = Main.getItemsManager().getItem("voting_vote_accept").getItem();
				ItemInfo cancelItem = Main.getItemsManager().getItem("voting_vote_cancel").getItem();

				Icon icon = new Icon(acceptItem.getItem(pInfo_.getPlayer().getName(), null));
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						votingInv.handleAcceptClick();
					}
				});
				this.setIcon(playersSlots.get(slotI) + 1, icon);

				Icon icon1 = new Icon(Utils.createItem(cancelItem.getMat(), cancelItem.getTitle(pInfo_.getPlayer().getName()), 1, cancelItem.getLore(pInfo_.getPlayer().getName())));
				icon1.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						votingInv.handleCancelClick();
					}
				});
				this.setIcon(playersSlots.get(slotI) + 2, icon1);
			}

			if (!didAddLore) {
				lore.addAll(playerItem.getItem().getLore(pInfo_.getPlayer().getName(), "" + pInfo_.getColor().getChatColor(), pInfo_.getColor().toString().toLowerCase(), playerValue3, null));
			}

			Integer playerAmount = 1;
			if (manager.getState() == meetingState.VOTING_RESULTS) {
				playerAmount = manager.getVotes(pInfo_.getPlayer()).size();
				if (playerAmount == 0) {
					playerAmount = 1;
				}
			}

			ItemStack item = pInfo_.getHead().clone();
			Utils.setItemName(item, ChatColor.WHITE + playerTitle, lore);
			item.setAmount(playerAmount);

			Icon icon = new Icon(item);
			if (!this.pInfo.isGhost() && !pInfo_.isGhost() && manager.canVote(this.pInfo)) {
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						votingInv.handleHeadClick(pInfo_);
//						manager.vote(pInfo, pInfo_);
					}
				});
			}
			this.setIcon(playersSlots.get(slotI), icon);
			slotI++;
		}

		ItemInfoContainer skipItem = Main.getItemsManager().getItem("voting_skip");
		ItemStack skipItemS = null;
		if (manager.getState() == meetingState.VOTING) {
			skipItemS = skipItem.getItem().getItem();
		} else if (manager.getState() == meetingState.VOTING_RESULTS) {
			skipItemS = skipItem.getItem2().getItem();
		}else if(manager.getState() == meetingState.DISCUSSION) {
			skipItemS = skipItem.getItem3().getItem();
		}

		ArrayList<String> skipLore = new ArrayList<String>();
		if (manager.getState() == meetingState.VOTING_RESULTS) {
			skipLore.add(votedSkipLine);
			skipLore.addAll(skipItem.getItem2().getLore());
		} else {
			skipLore = skipItem.getItem().getLore();
			if (manager.getState() == meetingState.DISCUSSION || !manager.canVote(pInfo)) {
				skipLore = skipItem.getItem3().getLore();
			}
		}
		if (manager.getState() == meetingState.DISCUSSION || !manager.canVote(pInfo)) {
			skipItemS = skipItem.getItem3().getItem();
		}

		Integer skipAmount = 1;
		if (manager.getState() == meetingState.VOTING_RESULTS) {
			skipAmount = manager.getSkippedVotes().size();
			if (skipAmount == 0) {
				skipAmount = 1;
			}
		}

		if (!this.pInfo.isGhost() || manager.getState() == meetingState.VOTING_RESULTS) {
			Utils.setItemLore(skipItemS, skipLore);
			skipItemS.setAmount(skipAmount);
			Icon skipIcon = new Icon(skipItemS);
			if (manager.canVote(this.pInfo)) {
				skipIcon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						votingInv.handleSkipClick();
					}
				});
			}
			this.setIcon(skipItem.getSlot(), skipIcon);
		}

		if (this.skipVoteActive) {
			ItemInfoContainer acceptItem = Main.getItemsManager().getItem("voting_vote_accept");
			ItemInfoContainer cancelItem = Main.getItemsManager().getItem("voting_vote_cancel");

			Icon icon = new Icon(acceptItem.getItem2().getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					votingInv.handleAcceptClick();
				}
			});
			this.setIcon(skipItem.getSlot() + 1, icon);

			Icon icon1 = new Icon(cancelItem.getItem2().getItem());
			icon1.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					votingInv.handleCancelClick();
				}
			});
			this.setIcon(skipItem.getSlot() + 2, icon1);
		}

		ItemInfoContainer infoItem = Main.getItemsManager().getItem("voting_info");
		String timer = manager.getActiveTimer() + "";
		String color = "";
		if (manager.getActiveTimer() <= 10) {
			color = ChatColor.RED + "";
		}
		ItemStack infoItemS = infoItem.getItem().getItem(timer, color);
		if (manager.getState() == meetingState.VOTING) {
			infoItemS = infoItem.getItem2().getItem(timer, color);
		} else if (manager.getState() == meetingState.VOTING_RESULTS) {
			infoItemS = infoItem.getItem3().getItem(timer, color);
		}
		Integer infoAmount = 1;
		if (this.getArena().getMeetingManager().getActiveTimer() > 0) {
			infoAmount = this.getArena().getMeetingManager().getActiveTimer();
		}
		infoItemS.setAmount(infoAmount);
		this.inv.setItem(infoItem.getSlot(), infoItemS);
	}

	public void handleHeadClick(PlayerInfo clickedPInfo) {
		if (this.arena.getMeetingManager().getState() == meetingState.VOTING) {
			if (!clickedPInfo.isGhost()) {
				this.activePInfo = clickedPInfo;
				this.update();
			}
		}
	}

	public void handleSkipClick() {
		if (this.arena.getMeetingManager().getState() == meetingState.VOTING) {
			this.skipVoteActive = true;
			this.activePInfo = null;
			this.update();
		}
	}

	public void handleAcceptClick() {
		if (this.arena.getMeetingManager().getState() == meetingState.VOTING) {

			if (this.skipVoteActive) {
				this.skipVoteActive = false;
				this.getArena().getMeetingManager().voteSkip(pInfo);
			} else {
				PlayerInfo toVote = this.activePInfo;
				this.activePInfo = null;
				this.getArena().getMeetingManager().vote(pInfo, toVote);
			}
		}
	}

	public void handleCancelClick() {
		if (this.arena.getMeetingManager().getState() == meetingState.VOTING) {
			this.activePInfo = null;
			this.skipVoteActive = false;
			this.update();
		}
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
		this.update();
	}

	public Arena getArena() {
		return arena;
	}

	public PlayerInfo getpInfo() {
		return pInfo;
	}

	public ArrayList<PlayerInfo> getPlayers() {
		return players;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

}