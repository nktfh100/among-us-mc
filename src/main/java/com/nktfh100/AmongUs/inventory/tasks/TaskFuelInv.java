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

public class TaskFuelInv extends TaskInvHolder {

	private static ArrayList<ArrayList<Integer>> slots = new ArrayList<ArrayList<Integer>>();

	static {
		slots.add(new ArrayList<Integer>(Arrays.asList(38, 39)));
		slots.add(new ArrayList<Integer>(Arrays.asList(29, 30)));
		slots.add(new ArrayList<Integer>(Arrays.asList(20, 21)));
		slots.add(new ArrayList<Integer>(Arrays.asList(11, 12)));
	}

	private Boolean isDone = false;
	private Boolean isRunning = false;
	private Integer progress = 0;
	private BukkitTask runnable = null;

	public TaskFuelInv(Arena arena, TaskPlayer taskPlayer, Integer progress) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.progress = progress;
		TaskFuelInv inv = this;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (inv.getIsDone()) {
					this.cancel();
					return;
				}
				if (inv.getIsRunning()) {
					inv.tick();
				}
			}
		}.runTaskTimer(Main.getPlugin(), 20L, 20L);
		this.update();
	}

	public void handleClick(Player player) {
		if (this.isDone) {
			return;
		}
		Main.getSoundsManager().playSound("taskFuelLeverlClick", player, player.getLocation());
		this.isRunning = !this.isRunning;

		this.checkDone();
		this.update();
	}

	public void tick() {
		this.progress++;
		this.taskPlayer.setFuelProgress_(this.progress);
		if (this.progress >= 4) {
			this.isDone = true;
		}
		this.checkDone();
		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskFuelInv taskInv = this;
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

	@Override
	public void update() {
		
		this.inv.setItem(8, Main.getItemsManager().getItem("fuel_info").getItem().getItem());
		
		TaskFuelInv inv = this;
		ItemInfoContainer fuelItem = Main.getItemsManager().getItem("fuel_fuel");
		ItemStack fuelItemS = fuelItem.getItem().getItem();
		ItemStack fuelItemS2 = fuelItem.getItem2().getItem();

		for (int i = 0; i < slots.size(); i++) {
			for (Integer slot : slots.get(i)) {
				if (i < this.progress) {
					this.inv.setItem(slot, fuelItemS);
				} else {
					this.inv.setItem(slot, fuelItemS2);
				}
			}
		}

		ItemInfoContainer buttonItem = Main.getItemsManager().getItem("fuel_button");
		Icon icon = new Icon(this.isRunning ? buttonItem.getItem2().getItem() : buttonItem.getItem().getItem());
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleClick(player);
			}
		});
		this.setIcon(33, icon);
		this.setIcon(34, icon);
		this.setIcon(42, icon);
		this.setIcon(43, icon);
		
		ItemInfoContainer isRunningItem = Main.getItemsManager().getItem("fuel_isRunning");
		icon = new Icon(this.isRunning ? isRunningItem.getItem2().getItem() : isRunningItem.getItem().getItem());
		this.setIcon(24, icon);
		
		ItemInfoContainer isDoneItem = Main.getItemsManager().getItem("fuel_isDone");
		icon = new Icon(this.isDone ? isDoneItem.getItem2().getItem() : isDoneItem.getItem().getItem());
		this.setIcon(25, icon);
	}

	@Override
	public void invClosed() {
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
	}

	public Boolean getIsRunning() {
		return this.isRunning;
	}

	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}
}