package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskCalibrateDistributorInv extends TaskInvHolder {

	private final static ArrayList<Integer> circleSlots = new ArrayList<Integer>(Arrays.asList(21, 30, 29, 28, 19, 10, 11, 12));

	private Boolean isDone = false;
	private Integer activeCircle = 0; // 0 - 2
	private Integer activeCursor = Utils.getRandomNumberInRange(1, 5); // 0 - 7
	private BukkitTask runnable = null;

	public TaskCalibrateDistributorInv(Arena arena, TaskPlayer taskPlayer) {
		super(45, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		
		this.inv.setItem(8, Main.getItemsManager().getItem("calibrateDistributor_info").getItem().getItem());
		
		TaskCalibrateDistributorInv inv = this;
		ItemInfo btnItem = Main.getItemsManager().getItem("calibrateDistributor_button").getItem();
		int btnSlot = 23;
		for (int i = 0; i < 3; i++) {
			Icon icon = new Icon(btnItem.getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleButtonClick(player);
				}
			});
			this.setIcon(btnSlot, icon);
			btnSlot++;
		}
		
		ItemInfo midItem = Main.getItemsManager().getItem("calibrateDistributor_middle").getItem();
		this.setIcon(22, new Icon(midItem.getItem()));
		
		this.update();
		this.runnable = new BukkitRunnable() {

			@Override
			public void run() {
				if (!inv.isDone) {
					inv.updateCursor();
				} else {
					this.cancel();
				}
			}
		}.runTaskTimer(Main.getPlugin(), 5L, 9L);
	}

	public void updateCursor() {
		this.activeCursor++;
		if (this.activeCursor > 7) {
			this.activeCursor = 0;
		}
		this.update();
	}

	public void handleButtonClick(Player player) {
		if (this.isDone) {
			return;
		}

		if (this.activeCursor == 0) {
			Main.getSoundsManager().playSound("taskCalibrateDistributorClickGood", player, player.getLocation());
			if (this.activeCircle == 2) {
				this.isDone = true;
				if(this.runnable != null) {
					this.runnable.cancel();
					this.runnable = null;
				}
			} else {
				this.activeCircle++;
				this.activeCursor = Utils.getRandomNumberInRange(1, 5);
			}
		} else {
			Main.getSoundsManager().playSound("taskCalibrateDistributorClickWrong", player, player.getLocation());
			this.activeCircle = 0;
			this.activeCursor = Utils.getRandomNumberInRange(1, 5);
		}

		this.checkDone();
		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskCalibrateDistributorInv taskInv = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = taskInv.getTaskPlayer().getPlayerInfo().getPlayer();
					if (player.getOpenInventory().getTopInventory() == taskInv.getInventory()) {
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
		ItemInfoContainer barItem = Main.getItemsManager().getItem("calibrateDistributor_bar");
		ItemStack barItemS = barItem.getItem().getItem();

		// circle
		ItemInfoContainer cursorItem = Main.getItemsManager().getItem("calibrateDistributor_cursor");
		ItemInfoContainer circleItem = Main.getItemsManager().getItem("calibrateDistributor_circle");
		ItemStack circleItemS = circleItem.getItem().getItem();
		if (this.activeCircle == 1) {
			circleItemS = circleItem.getItem2().getItem();

			barItemS = barItem.getItem2().getItem();
		} else if (this.activeCircle == 2) {
			circleItemS = circleItem.getItem3().getItem();

			barItemS = barItem.getItem().getItem();
		}
		int circleSlot = 0;
		for (Integer slot : circleSlots) {
			Icon icon;
			if (circleSlot == this.activeCursor) {
				icon = new Icon(cursorItem.getItem().getItem());
			} else {
				icon = new Icon(circleItemS);
			}
			this.setIcon(slot, icon);
			circleSlot++;
		}

		// bar above button
		Icon barIcon = new Icon(barItemS);
		this.setIcon(14, barIcon);
		if (this.activeCursor == 0) {
			this.setIcon(15, barIcon);
			this.setIcon(16, barIcon);
		} else {
			ItemStack bgItem = Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
			this.inv.setItem(15, bgItem);
			this.inv.setItem(16, bgItem);
		}

		ItemInfo circleMidItem = Main.getItemsManager().getItem("calibrateDistributor_circleMiddle").getItem();
		ItemStack circleMidItemS = circleMidItem.getItem();
		circleMidItemS.setAmount(this.activeCircle + 1);
		this.setIcon(20, new Icon(circleMidItemS));
	}

	@Override
	public void invClosed() {
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
	}

	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}
}