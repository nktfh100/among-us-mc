package com.nktfh100.AmongUs.events;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.nktfh100.AmongUs.enums.GameState;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.JoinSign;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.inventory.CosmeticSelectorInv;
import com.nktfh100.AmongUs.inventory.MeetingBtnInv;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.ItemsManager;

public class PlayerRightClick implements Listener {

	@EventHandler
	public void onPlayerUse(PlayerInteractEvent ev) {
		Player player = ev.getPlayer();
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if (ev.getAction() == Action.RIGHT_CLICK_BLOCK && pInfo.getIsIngame()) {
			ev.setCancelled(true);
		}
		if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = ev.getClickedBlock();
			if (block.getType().toString().contains("SIGN")) {
				Sign sign = (Sign) block.getState();
				if (sign.getLine(0).equalsIgnoreCase(Main.getMessagesManager().getSignLine(0, null))) {
					ev.setCancelled(true);
					for (Arena arena : Main.getArenaManager().getAllArenas()) {
						if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) {
							for (JoinSign joinSign : arena.getJoinSigns()) {
								if (joinSign.getBlock().equals(block)) {
									if (!arena.isPlayerInArena(player)) {
										if (Main.getConfigManager().getBungeecord()) {
											Main.sendPlayerToArena(player, arena.getName());
										} else {
											arena.playerJoin(player);
										}

										arena.playerJoin(player);
										return;
									}
								}
							}
						}
					}
					return;
				}
			}
		}
		if (ev.getItem() == null) {
			return;
		}
		String displayName = ev.getItem().getItemMeta().getDisplayName();
		if (ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (pInfo.getIsIngame()) {
				ItemsManager itemsManager = Main.getItemsManager();
				if (displayName.equals(itemsManager.getItem("leave").getItem().getTitle())) {
					ev.setCancelled(true);
					pInfo.getArena().playerLeave(player, false, false, true);
				} else if (displayName.equals(itemsManager.getItem("colorSelector").getItem().getTitle())) {
					ev.setCancelled(true);
					player.openInventory(pInfo.getArena().getColorSelectorInv(player).getInventory());
				} else if (displayName.equals(itemsManager.getItem("use").getItem2().getTitle())) {
					ev.setCancelled(true);
					switch (pInfo.getUseItemState()) {
						case 1 -> {
							Main.getSoundsManager().playSound("meetingBtnInvOpen", pInfo.getPlayer(), pInfo.getPlayer().getLocation());
							pInfo.getPlayer().openInventory(new MeetingBtnInv(pInfo.getArena(), pInfo).getInventory());
						}
						case 2 -> pInfo.getArena().getTasksManager().taskHoloClick(pInfo.getPlayer(), pInfo.getUseItemTask().getActiveTask());
						case 3 -> pInfo.getArena().getSabotageManager().sabotageHoloClick(pInfo.getPlayer(), pInfo.getUseItemSabotage().getId());
						case 4 -> pInfo.getArena().getVentsManager().ventHoloClick(pInfo, pInfo.getUseItemVent().getVentGroup().getId(), pInfo.getUseItemVent().getId());
						case 5 -> pInfo.getArena().getCamerasManager().camerasHoloClick(pInfo);
						case 6 -> pInfo.getArena().getVitalsManager().openInventory(player);
						default -> {
							return;
						}
					}

				} else if (pInfo.getArena().getIsInMeeting() && displayName.equals(itemsManager.getItem("vote").getItem().getTitle())) {
					ev.setCancelled(true);
					Main.getSoundsManager().playSound("votingInvOpen", player, player.getLocation());
					pInfo.getArena().getMeetingManager().openVoteInv(pInfo);
				} else if (pInfo.getIsInVent() && displayName.equals(itemsManager.getItem("vent_left").getItem().getTitle())) {
					ev.setCancelled(true);
					pInfo.getArena().getVentsManager().playerPrevVent(pInfo);
				} else if (pInfo.getIsInVent() && displayName.equals(itemsManager.getItem("vent_leave").getItem().getTitle())) {
					ev.setCancelled(true);
					pInfo.getArena().getVentsManager().playerLeaveVent(pInfo, false, false);
				} else if (pInfo.getIsInVent() && displayName.equals(itemsManager.getItem("vent_right").getItem().getTitle())) {
					ev.setCancelled(true);
					pInfo.getArena().getVentsManager().playerNextVent(pInfo);
				} else if (pInfo.getIsInCameras() && displayName.equals(itemsManager.getItem("cameras_left").getItem().getTitle())) {
					ev.setCancelled(true);
					pInfo.getArena().getCamerasManager().playerPrevCamera(pInfo);
				} else if (pInfo.getIsInCameras() && displayName.equals(itemsManager.getItem("cameras_right").getItem().getTitle())) {
					ev.setCancelled(true);
					pInfo.getArena().getCamerasManager().playerNextCamera(pInfo);
				} else if (pInfo.getIsInCameras() && displayName.equals(itemsManager.getItem("cameras_leave").getItem().getTitle())) {
					ev.setCancelled(true);
					pInfo.getArena().getCamerasManager().playerLeaveCameras(pInfo, false);
				} else if (pInfo.getPlayerDiedTemp() != null && pInfo.getArena().getGameState() == GameState.RUNNING && !pInfo.isGhost()) {
					String playerName = pInfo.getPlayerDiedTemp().getPlayer().getName();
					String playerColorName = pInfo.getPlayerDiedTemp().getColor().getName();
					String playerColor = String.valueOf(pInfo.getPlayerDiedTemp().getColor().getChatColor());

					if (displayName.equals(itemsManager.getItem("report").getItem().getTitle())
							|| displayName.equals(itemsManager.getItem("report").getItem2().getTitle(playerName, playerColorName, playerColor, null, null))) {
						ev.setCancelled(true);
						pInfo.getArena().getMeetingManager().callMeeting(player, true, pInfo.getPlayerDiedTemp());
					}
				}
			} else {
				if (displayName.equals(Main.getItemsManager().getItem("arenasSelector").getItem().getTitle())) {
					ev.setCancelled(true);
					if (Main.getConfigManager().getBungeecord() && Main.getConfigManager().getBungeecordIsLobby()) {
						Main.getBungeArenaManager().openArenaSelector(pInfo);
					} else if (!Main.getConfigManager().getBungeecord()) {
						Main.getArenaManager().openArenaSelector(pInfo);
					}
				} else if (displayName.equals(Main.getItemsManager().getItem("cosmeticsSelector").getItem().getTitle())) {
					ev.setCancelled(true);
					if(Main.getIsPlayerPoints()) {
						player.openInventory(new CosmeticSelectorInv(pInfo).getInventory());						
					}
				}
			}
		}

	}
}
