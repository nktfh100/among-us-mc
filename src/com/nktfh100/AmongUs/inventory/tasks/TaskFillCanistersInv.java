package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskFillCanistersInv extends TaskInvHolder {

	private static final ArrayList<Integer> leftCanisterSlots = new ArrayList<Integer>(Arrays.asList(0, 9, 10, 18, 19, 27, 28, 36));
	private static final ArrayList<Integer> outerCanisterSlots = new ArrayList<Integer>(Arrays.asList(20, 12, 13, 14, 15, 16, 25, 24, 33, 34, 32, 31, 30));
	private static final ArrayList<Integer> canisterProgressSlots = new ArrayList<Integer>(Arrays.asList(21, 22, 23, 24));

	private Integer canisterProgress = -1;
	private Boolean isCanisterFillingUp = false;
	private Boolean isCanisterDone = false;
	private Boolean isDone = false;
	private BukkitTask runnable = null;

	public TaskFillCanistersInv(Arena arena, TaskPlayer taskPlayer) {
		super(45, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Main.getMessagesManager().getTaskName(taskPlayer.getActiveTask().getTaskType().toString()), taskPlayer.getActiveTask().getLocationName().getName()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		ItemStack item = Main.getItemsManager().getItem("fillCanisters_leftCanister").getItem().getItem();
		for (Integer slot : leftCanisterSlots) {
			this.inv.setItem(slot, item);
		}

		this.update();
	}

	public void canisterClick() {
		if (this.isDone || this.isCanisterDone) {
			return;
		}
		TaskFillCanistersInv inv = this;
		Main.getSoundsManager().playSound("taskFillCanistersClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		if (!this.isCanisterFillingUp) {
			this.isCanisterFillingUp = true;
			if (this.runnable != null) {
				this.runnable.cancel();
			}
			this.runnable = new BukkitRunnable() {
				@Override
				public void run() {
					inv.progressTick();
				}
			}.runTaskTimer(Main.getPlugin(), 20L, 30L);
			this.update();
		}
	}

	public void resetCanister() {
		this.canisterProgress = -1;
		this.isCanisterDone = false;
		this.isCanisterFillingUp = false;
		if (this.runnable != null) {
			this.runnable.cancel();
		}
		this.runnable = null;
	}

	public void progressTick() {
		Main.getSoundsManager().playSound("taskFillCanistersLoading", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		this.canisterProgress++;
		if (this.canisterProgress >= 3) {
			Main.getSoundsManager().playSound("taskFillCanistersLoadingDone", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
			TaskFillCanistersInv inv = this;
			this.update();
			this.getTaskPlayer().setCanistersLeft_(this.getTaskPlayer().getCanistersLeft_() - 1);
			this.isCanisterDone = true;
			if (this.taskPlayer.getCanistersLeft_() <= 0) {
				this.isDone = true;
			}
			if (this.runnable != null) {
				this.runnable.cancel();
			}
			this.runnable = new BukkitRunnable() {
				@Override
				public void run() {
					inv.resetCanister();
					inv.update();
					inv.checkDone();
				}
			}.runTaskLater(Main.getPlugin(), 20L);
		}
		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskFillCanistersInv inv = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = inv.getTaskPlayer().getPlayerInfo().getPlayer();
					if (player.getOpenInventory().getTopInventory() == inv.getInventory()) {
						player.closeInventory();
					}
				}
			}.runTaskLater(Main.getPlugin(), 15L);
			return true;
		}
		return false;
	}

	@Override
	public void update() {
		TaskFillCanistersInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("fillCanisters_info").getItem().getItem());

		ItemInfoContainer outerCanisterItem = Main.getItemsManager().getItem("fillCanisters_outerCanister");
		ItemStack outerCanisterItemS = null;
		if (!this.isCanisterFillingUp) {
			outerCanisterItemS = outerCanisterItem.getItem().getItem();
		} else if (this.canisterProgress < 3) {
			outerCanisterItemS = outerCanisterItem.getItem2().getItem();
		} else if (this.isCanisterDone) {
			outerCanisterItemS = outerCanisterItem.getItem3().getItem();
		}
		if (!this.isDone) {

			for (Integer slot : outerCanisterSlots) {
				Icon icon = new Icon(outerCanisterItemS);
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.canisterClick();
					}
				});
				this.setIcon(slot, icon);
			}

			ItemInfoContainer canisterProgressItem = Main.getItemsManager().getItem("fillCanisters_canisterProgress");
			ItemStack canisterProgressItemS = canisterProgressItem.getItem().getItem(); // not active
			ItemStack canisterProgressItem2S = canisterProgressItem.getItem2().getItem(); // light gray
			ItemStack canisterProgressItem3S = canisterProgressItem.getItem3().getItem(); // yellow

			ItemStack canisterProgressDoneItemS = Main.getItemsManager().getItem("fillCanisters_canisterProgressDone").getItem().getItem(); // done

			for (int i = 0; i < 4; i++) {
				Icon icon = new Icon(canisterProgressItemS);
				if (this.isCanisterFillingUp) {
					if (this.canisterProgress >= i) {
						if (this.isCanisterDone) {
							icon = new Icon(canisterProgressDoneItemS);
						} else {
							icon = new Icon(canisterProgressItem3S);
						}
					} else {
						icon = new Icon(canisterProgressItem2S);
					}
				}
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.canisterClick();
					}
				});
				this.setIcon(canisterProgressSlots.get(i), icon);
			}
		} else {
			ItemStack item = Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
			for (Integer slot : outerCanisterSlots) {
				this.setIcon(slot, new Icon(item));
			}
			for (Integer slot : canisterProgressSlots) {
				this.setIcon(slot, new Icon(item));
			}
		}
	}

	@Override
	public void invClosed() {
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
	}
}