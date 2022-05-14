package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

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

public class TaskReplaceWaterJug extends TaskInvHolder {

	private static final ArrayList<Integer> buttonSlots = new ArrayList<Integer>(Arrays.asList(21, 22, 23, 30, 31, 32));
	private static final ArrayList<ArrayList<Integer>> leftJugSlots = new ArrayList<ArrayList<Integer>>();
	private static final ArrayList<ArrayList<Integer>> rightJugSlots = new ArrayList<ArrayList<Integer>>();

	static {
		leftJugSlots.add(new ArrayList<Integer>(Arrays.asList(9, 10, 11)));
		leftJugSlots.add(new ArrayList<Integer>(Arrays.asList(18, 19, 20)));
		leftJugSlots.add(new ArrayList<Integer>(Arrays.asList(27, 28, 29)));
		leftJugSlots.add(new ArrayList<Integer>(Arrays.asList(36, 37, 38)));

		rightJugSlots.add(new ArrayList<Integer>(Arrays.asList(15, 16, 17)));
		rightJugSlots.add(new ArrayList<Integer>(Arrays.asList(24, 25, 26)));
		rightJugSlots.add(new ArrayList<Integer>(Arrays.asList(33, 34, 35)));
		rightJugSlots.add(new ArrayList<Integer>(Arrays.asList(42, 43, 44)));
	}

	private Boolean isRunning = false;
	private Boolean isDone = false;
	private BukkitTask runnable = null;

	public TaskReplaceWaterJug(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Main.getMessagesManager().getTaskName(taskPlayer.getActiveTask().getTaskType().toString()), taskPlayer.getActiveTask().getLocationName().getName()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		this.update();
	}

	public void handleButtonClick() {
		if (this.isDone) {
			return;
		}
		this.isRunning = !this.isRunning;
		Main.getSoundsManager().playSound("taskReplaceWaterJug_buttonClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		if (this.isRunning) {
			TaskReplaceWaterJug inv = this;
			this.runnable = new BukkitRunnable() {
				@Override
				public void run() {
					inv.progressTick();
				}
			}.runTaskTimer(Main.getPlugin(), 20L, 25L);
		} else {
			this.runnable.cancel();
			this.runnable = null;
		}
		this.update();
	}

	public void progressTick() {
		this.taskPlayer.setWaterJugProgress_(this.taskPlayer.getWaterJugProgress_() + 1);
		if (this.taskPlayer.getWaterJugProgress_() >= 4) {
			this.isDone = true;
			this.runnable.cancel();
			this.runnable = null;
		}
		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskReplaceWaterJug inv = this;
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
		TaskReplaceWaterJug inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("replaceWaterJug_info").getItem().getItem());

		ItemInfoContainer buttonItem = Main.getItemsManager().getItem("replaceWaterJug_button");
		ItemStack buttonItemS = this.isRunning ? buttonItem.getItem2().getItem() : buttonItem.getItem().getItem();

		for (Integer slot : buttonSlots) {
			Icon icon = new Icon(buttonItemS);
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleButtonClick();
				}
			});
			this.setIcon(slot, icon);
		}

		ItemInfoContainer jugItem = Main.getItemsManager().getItem("replaceWaterJug_waterJug");
		ItemStack jugItemS = jugItem.getItem().getItem(); // jug
		ItemStack jugItem2S = jugItem.getItem2().getItem(); // water

		Integer progress = this.taskPlayer.getWaterJugProgress_();

		for (int i = 0; i < rightJugSlots.size(); i++) {
			for (Integer slot : rightJugSlots.get(i)) {
				if (i < progress) {
					this.inv.setItem(slot, jugItemS);
				} else {
					this.inv.setItem(slot, jugItem2S);
				}
			}
		}
		progress = 4 - progress;
		for (int i = 0; i < leftJugSlots.size(); i++) {
			for (Integer slot : leftJugSlots.get(i)) {
				if (i < progress) {
					this.inv.setItem(slot, jugItemS);
				} else {
					this.inv.setItem(slot, jugItem2S);
				}
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