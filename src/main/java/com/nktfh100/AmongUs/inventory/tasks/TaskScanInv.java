package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ColorInfo;
import com.nktfh100.AmongUs.info.FakeArmorStand;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskScanInv extends TaskInvHolder {

	private ColorInfo color;
	private Integer secondsLeft = 10;
	private Boolean isQueue = false;
	private Boolean isDone = false;
	private BukkitTask runnable = null;
	private BukkitTask runnable1 = null;

	private Boolean removeFromQueue = true;

	public TaskScanInv(Arena arena, TaskPlayer taskPlayer) {
		super(arena.getScanQueue().size() == 0 ? 54 : 9, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		this.color = this.pInfo.getColor();
		if (arena.getScanQueue().size() == 0 || this.pInfo.isGhost()) {
			this.startScanning();
		} else {
			this.isQueue = true;
		}
		if (!this.pInfo.isGhost()) {
			arena.getScanQueue().add(this.pInfo);
		}

		TaskScanInv inv = this;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (inv.getIsDone() || inv.getSecondsLeft() == 0 || pInfo == null || !pInfo.getIsIngame()) {
					this.cancel();
					return;
				}
				if (!inv.getIsQueue()) {
					inv.tick();
				} else {
					if (inv.getArena().getScanQueue().get(0) == inv.getPlayerInfo()) {
						inv.startScanning();
					}
				}
			}
		}.runTaskTimer(Main.getPlugin(), 20L, 20L);

		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.arena.getScanQueue().remove(this.pInfo);
			this.taskPlayer.taskDone();
			TaskScanInv taskInv = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = taskInv.getTaskPlayer().getPlayerInfo().getPlayer();
					if (player.getOpenInventory().getTopInventory() == taskInv.getInventory()) {
						player.closeInventory();
					}
				}
			}.runTaskLater(Main.getPlugin(), 20L);
			return true;
		}
		return false;
	}

	public void startScanning() {
		this.pInfo.setIsScanning(true);
		this.isQueue = false;

		if (this.inv.getSize() == 9) {
			this.removeFromQueue = false;
			this.changeSize(54);
			this.pInfo.getPlayer().openInventory(this.inv);
			Utils.fillInv(this.inv);
			this.removeFromQueue = true;
		}

		if (arena.getEnableVisualTasks() && this.taskPlayer.getActiveTask().getEnableVisuals() && !this.pInfo.isGhost()) {
			ArrayList<FakeArmorStand> fakeArmorStands = this.pInfo.getScanArmorStands();
			for (FakeArmorStand fa : fakeArmorStands) {
				fa.resetAllShownTo();
			}

			Double extraDis = 0.8D;
			fakeArmorStands.get(0).updateLocation(this.pInfo.getPlayer().getLocation().add(-0.3, -1, 0.3 - extraDis));
			fakeArmorStands.get(1).updateLocation(this.pInfo.getPlayer().getLocation().add(0.3, -1, -0.3 - extraDis));
			fakeArmorStands.get(2).updateLocation(this.pInfo.getPlayer().getLocation().add(0.3, -1, 0.3 - extraDis));
			fakeArmorStands.get(3).updateLocation(this.pInfo.getPlayer().getLocation().add(-0.3, -1, -0.3 - extraDis));

			for (PlayerInfo pInfo_ : arena.getPlayersInfo()) {
				if (pInfo != pInfo_) {
					if (this.arena.getEnableReducedVision()) {
						if (pInfo_.isGhost() || !pInfo_.getPlayersHidden().contains(this.pInfo.getPlayer())) {
							for (FakeArmorStand fa : fakeArmorStands) {
								fa.showTo(pInfo_.getPlayer(), true);
							}
						}
					} else {
						for (FakeArmorStand fa : fakeArmorStands) {
							fa.showTo(pInfo_.getPlayer(), true);
						}
					}
				}
			}

			for (FakeArmorStand fa : fakeArmorStands) {
				fa.showTo(pInfo.getPlayer(), true);
			}

			TaskScanInv inv = this;

			this.runnable1 = new BukkitRunnable() {
				@Override
				public void run() {
					if (inv.getIsDone() || inv.getSecondsLeft() == 0 || pInfo == null || !pInfo.getIsIngame()) {
						this.cancel();
						return;
					}
					inv.updateArmorStands();
				}
			}.runTaskTimer(Main.getPlugin(), 5L, 5L);
		}
		this.update();
		this.arena.getVisibilityManager().playerMoved(this.pInfo);
	}

	private Integer dir = 0; // 0 = down

	public void updateArmorStands() {
		Double toAdd = dir == 0 ? -0.25D : 0.25D;
		Double armorStandY = this.pInfo.getScanArmorStands().get(0).getLoc().getY();
		if (armorStandY > this.pInfo.getPlayer().getLocation().getY() - 0.2) {
			dir = 0;
		} else if (armorStandY < this.pInfo.getPlayer().getLocation().getY() - 1.2) {
			dir = 1;
		}
		for (FakeArmorStand fa : this.pInfo.getScanArmorStands()) {
			fa.updateLocation(fa.getLoc().add(0, toAdd, 0));
		}
	}

	public void tick() {
		if (this.secondsLeft > 0) {
			this.secondsLeft--;
		}
		if (this.secondsLeft == 0) {
			if (this.runnable != null) {
				this.runnable.cancel();
				this.runnable = null;
			}
			this.isDone = true;
			this.checkDone();
		}

		this.update();
	}

	@Override
	public void update() {
		if (!this.isQueue) { // scanning inv

			this.inv.setItem(8, Main.getItemsManager().getItem("scan_info").getItem().getItem());

			ItemInfo idItem = Main.getItemsManager().getItem("scan_infoId").getItem();
			ItemInfo heightItem = Main.getItemsManager().getItem("scan_infoHeight").getItem();
			ItemInfo weightItem = Main.getItemsManager().getItem("scan_infoWeight").getItem();
			ItemInfo colorItem = Main.getItemsManager().getItem("scan_infoColor").getItem();
			ItemInfo bloodItem = Main.getItemsManager().getItem("scan_infoBloodType").getItem();
			if (this.secondsLeft < 10) {
				this.inv.setItem(20, idItem.getItem(this.color.getId(), this.taskPlayer.getPlayerInfo().getJoinedId() + ""));
			}
			if (this.secondsLeft < 8) {
				this.inv.setItem(21, heightItem.getItem(this.color.getHeight(), null));
			}
			if (this.secondsLeft < 6) {
				this.inv.setItem(22, weightItem.getItem(this.color.getWeight(), null));
			}
			if (this.secondsLeft < 4) {
				this.inv.setItem(23, colorItem.getItem(this.color.getName(), null));
			}
			if (this.secondsLeft < 3) {
				this.inv.setItem(24, bloodItem.getItem(this.color.getBloodType(), null));
			}

			ItemInfoContainer progressItem = Main.getItemsManager().getItem("scan_progress");
			ItemStack progressItemS = progressItem.getItem().getItem();
			ItemStack progressItem2S = progressItem.getItem2().getItem();
			Integer slot = 37;
			for (int i = 1; i < 8; i++) {
				if (i * 1.25 <= 10 - this.secondsLeft) {
					this.inv.setItem(slot, progressItem2S);
				} else {
					this.inv.setItem(slot, progressItemS);
				}
				slot++;
			}

			ItemInfoContainer infoItem = Main.getItemsManager().getItem("scan_seconds");
			ItemStack infoItemS = this.secondsLeft == 0 ? infoItem.getItem2().getItem() : infoItem.getItem().getItem(this.secondsLeft + "", null);
			if (this.secondsLeft > 0) {
				infoItemS.setAmount(this.secondsLeft);
			}
			this.inv.setItem(49, infoItemS);
		} else { // queue inv
			PlayerInfo playerScanning = this.arena.getScanQueue().get(0);
			if (playerScanning == this.pInfo) {
				this.startScanning();
				return;
			}
			ItemInfo infoItem = Main.getItemsManager().getItem("scan_queue_info").getItem();
			this.inv.setItem(4, infoItem.getItem(playerScanning.getPlayer().getName(), "" + playerScanning.getColor().getChatColor(), playerScanning.getColor().getName()));
		}
	}

	@Override
	public void invClosed() {
		if (this.removeFromQueue) {
			this.pInfo.setIsScanning(false);
			this.arena.getScanQueue().remove(this.pInfo);
			for (FakeArmorStand fas : this.pInfo.getScanArmorStands()) {
				fas.resetAllShownTo();
			}
			if (this.runnable != null) {
				this.runnable.cancel();
				this.runnable = null;
			}
			if (runnable1 != null) {
				this.runnable1.cancel();
				this.runnable1 = null;
			}
		}
	}

	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}

	public Integer getSecondsLeft() {
		return secondsLeft;
	}

	public void setSecondsLeft(Integer secondsLeft) {
		this.secondsLeft = secondsLeft;
	}

	public Boolean getIsQueue() {
		return isQueue;
	}

	public void setIsQueue(Boolean isQueue) {
		this.isQueue = isQueue;
	}
}