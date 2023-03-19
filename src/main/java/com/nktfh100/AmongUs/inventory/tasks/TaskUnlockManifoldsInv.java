package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

public class TaskUnlockManifoldsInv extends TaskInvHolder {

	private ArrayList<Integer> numbers = new ArrayList<Integer>();
	private Integer activeNum = 0;
	private Boolean isRedActive = false;
	private Boolean isRed = false;

	public TaskUnlockManifoldsInv(Arena arena, TaskPlayer taskPlayer, ArrayList<Integer> numbers_) {
		super(36, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		if (numbers_ == null) {
			this.numbers = generateNumbers();
		} else {
			this.numbers = numbers_;
		}
		this.update();
	}

	public void numberClick(Player player, Integer num) {
		if (this.isRedActive) {
			return;
		}
		if (this.activeNum == num - 1) {
			this.activeNum++;
			Main.getSoundsManager().playSound("taskUnlockManifoldsClick" + num, player, player.getLocation());
			this.update();
			this.checkDone();
		} else {
			Main.getSoundsManager().playSound("taskUnlockManifoldsClickWrong", player, player.getLocation());
			this.activeNum = 0;
			this.isRedActive = true;
			this.isRed = true;
			this.update();
			TaskUnlockManifoldsInv unlockManifoldsInv = this;
			new BukkitRunnable() {
				Integer i = 0;

				@Override
				public void run() {
					if (i >= 2) {
						unlockManifoldsInv.setIsRedActive(false);
						unlockManifoldsInv.setIsRed(false);
						unlockManifoldsInv.update();
						this.cancel();
						return;
					}
					unlockManifoldsInv.setIsRed(!unlockManifoldsInv.getIsRed());
					unlockManifoldsInv.update();
					i++;
				}
			}.runTaskTimer(Main.getPlugin(), 8L, 8L);
		}
	}

	public static ArrayList<Integer> generateNumbers() {
		ArrayList<Integer> out = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
		Collections.shuffle(out);
		return out;
	}

	@Override
	public Boolean checkDone() {
		if (this.activeNum < 10) {
			return false;
		}
		this.taskPlayer.taskDone();
		TaskUnlockManifoldsInv unlockManifoldsInv = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = unlockManifoldsInv.getTaskPlayer().getPlayerInfo().getPlayer();
				if (player.getOpenInventory().getTopInventory() == unlockManifoldsInv.getInventory()) {
					player.closeInventory();
				}
			}
		}.runTaskLater(Main.getPlugin(), 15L);
		return true;
	}

	@Override
	public void update() {

		this.inv.setItem(8, Main.getItemsManager().getItem("unlockManifolds_info").getItem().getItem());

		TaskUnlockManifoldsInv unlockManifoldsInv = this;

		Integer slot = 11;
		for (Integer num : this.numbers) {
			ItemInfoContainer squareItem = Main.getItemsManager().getItem("unlockManifolds_square" + num);
			ItemStack squareItemS_ = this.isRed ? squareItem.getItem3().getItem() : (this.activeNum >= num ? squareItem.getItem2().getItem() : squareItem.getItem().getItem());
			squareItemS_.setAmount(num);
			Icon icon = new Icon(squareItemS_);
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					unlockManifoldsInv.numberClick(player, num);
				}
			});

			this.setIcon(slot, icon);
			slot++;
			if (slot == 16) {
				slot = 20;
			}
		}
	}

	@Override
	public void invClosed() {
	}

	public ArrayList<Integer> getNumbers() {
		return this.numbers;
	}

	public Integer getActiveNum() {
		return activeNum;
	}

	public void setActiveNum(Integer activeNum) {
		this.activeNum = activeNum;
	}

	public void setIsRed(Boolean is) {
		this.isRed = is;
	}

	public Boolean getIsRed() {
		return this.isRed;
	}

	public Boolean getIsRedActive() {
		return isRedActive;
	}

	public void setIsRedActive(Boolean isRedActive) {
		this.isRedActive = isRedActive;
	}
}