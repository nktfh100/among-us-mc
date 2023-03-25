package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskRecordTemperatureInv extends TaskInvHolder {

	private static final ArrayList<Integer> leftBGSlots = new ArrayList<Integer>(Arrays.asList(1, 3, 10, 12, 28, 30, 37, 38, 39));
	private static final ArrayList<Integer> rightBGSlots = new ArrayList<Integer>(Arrays.asList(5, 7, 14, 15, 16, 32, 33, 34, 41, 42, 43));

	private Boolean isHot = false;
	private Integer targetNumber = 0;
	private Integer activeNumber = 0;
	private Boolean isDone = false;

	public TaskRecordTemperatureInv(Arena arena, TaskPlayer taskPlayer, Boolean isHot) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.isHot = isHot;
		if (!this.isHot) {
			this.targetNumber = Utils.getRandomNumberInRange(10, 40) * -1;
			this.activeNumber = Utils.getRandomNumberInRange(1, 20);
		} else {
			this.targetNumber = Utils.getRandomNumberInRange(300, 340);
			this.activeNumber = Utils.getRandomNumberInRange(280, 300);
		}
		ItemInfoContainer bgItems = Main.getItemsManager().getItem("recordTemperature_background");
		ItemStack bgLeftS = bgItems.getItem().getItem();
		ItemStack bgRightS = this.isHot ? bgItems.getItem3().getItem() : bgItems.getItem2().getItem();
		for (Integer slot : leftBGSlots) {
			this.inv.setItem(slot, bgLeftS);
		}
		for (Integer slot : rightBGSlots) {
			this.inv.setItem(slot, bgRightS);
		}
		ItemInfoContainer topTextItems = Main.getItemsManager().getItem("recordTemperature_infoTop");
		this.inv.setItem(2, topTextItems.getItem().getItem());
		this.inv.setItem(6, topTextItems.getItem2().getItem());
		this.update();
	}

	public void handleButtonUpClick() {
		if (this.isDone) {
			return;
		}
		Main.getSoundsManager().playSound("taskRecordTemperature_click", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		this.activeNumber++;
		if (this.activeNumber > 99 && !this.isHot) {
			this.activeNumber = 99;
		}
		if (this.isHot && activeNumber > 999) {
			this.activeNumber = 999;
		}
		if (this.isHot && this.activeNumber >= this.targetNumber) {
			this.activeNumber = this.targetNumber;
			this.isDone = true;
		} else if (!this.isHot && this.activeNumber <= this.targetNumber) {
			this.activeNumber = this.targetNumber;
			this.isDone = true;
		}
		this.update();
		this.checkDone();
	}

	public void handleButtonDownClick() {
		if (this.isDone) {
			return;
		}
		Main.getSoundsManager().playSound("taskRecordTemperature_click", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		this.activeNumber--;
		if (this.activeNumber < -99 && !this.isHot) {
			this.activeNumber = -99;
		}
		if (this.isHot && this.activeNumber < 100) {
			this.activeNumber = 100;
		}
		if (this.isHot && this.activeNumber >= this.targetNumber) {
			this.activeNumber = this.targetNumber;
			this.isDone = true;
		} else if (!this.isHot && this.activeNumber <= this.targetNumber) {
			this.activeNumber = this.targetNumber;
			this.isDone = true;
		}
		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskRecordTemperatureInv inv = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = inv.getTaskPlayer().getPlayerInfo().getPlayer();
					if (player.getOpenInventory().getTopInventory() == inv.getInventory()) {
						player.closeInventory();
					}
				}
			}.runTaskLater(Main.getPlugin(), 20L);
			return true;
		}
		return false;
	}

	@Override
	public void update() {
		TaskRecordTemperatureInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("recordTemperature_info").getItem().getItem());

		ItemInfoContainer arrowsItem = Main.getItemsManager().getItem("recordTemperature_arrows");
		Icon icon = new Icon(arrowsItem.getItem().getItem(this.activeNumber + "", this.targetNumber + ""));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleButtonUpClick();
			}
		});
		this.setIcon(11, icon);

		icon = new Icon(arrowsItem.getItem2().getItem(this.activeNumber + "", this.targetNumber + ""));
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleButtonDownClick();
			}
		});
		this.setIcon(29, icon);

		String activeNumberStr = this.activeNumber.toString();
		if (this.activeNumber == 0) {
			activeNumberStr = "n0n";
		} else if (this.activeNumber > 0) {
			if (!this.isHot) {
				activeNumberStr = "+" + activeNumberStr;
			}
			if (this.activeNumber < 10) {
				activeNumberStr = "n" + activeNumberStr;
			}
		} else if (this.activeNumber < 0 && this.activeNumber > -10) {
			activeNumberStr = "n" + activeNumberStr;
		}
		Integer slot = 19;
		String numKey = "recordTemperature_num_";
		for (String char_ : activeNumberStr.split("")) {
			String keyEnd = char_;
			switch (char_) {
			case "-":
				keyEnd = "minus";
				break;
			case "+":
				keyEnd = "plus";
				break;
			case "n":
				this.inv.setItem(slot, Main.getItemsManager().getItem("recordTemperature_background").getItem().getItem(this.activeNumber + "", this.targetNumber + ""));
				slot++;
				continue;
			default:
				break;
			}
			this.inv.setItem(slot, Main.getItemsManager().getItem(numKey + keyEnd).getItem().getItem(this.activeNumber + "", this.targetNumber + ""));
			slot++;
		}

		String targetNumberStr = this.targetNumber.toString();
		if (this.targetNumber == 0) {
			targetNumberStr = "n0n";
		} else if (this.targetNumber > 0) {
			if (!this.isHot) {
				targetNumberStr = "+" + targetNumberStr;
			}
			if (this.activeNumber < 10) {
				targetNumberStr = "n" + targetNumberStr;
			}
		} else if (this.targetNumber < 0 && this.targetNumber > -10) {
			targetNumberStr = "n" + targetNumberStr;
		}
		slot = 23;
		for (String char_ : targetNumberStr.split("")) {
			String keyEnd = char_;
			switch (char_) {
			case "-":
				keyEnd = "minus";
				break;
			case "+":
				keyEnd = "plus";
				break;
			case "n":
				this.inv.setItem(slot, Main.getItemsManager().getItem("recordTemperature_background").getItem2().getItem(this.activeNumber + "", this.targetNumber + ""));
				slot++;
				continue;
			default:
				break;
			}
			this.inv.setItem(slot, Main.getItemsManager().getItem(numKey + keyEnd).getItem2().getItem(this.activeNumber + "", this.targetNumber + ""));
			slot++;
		}
	}

	@Override
	public void invClosed() {
	}
}