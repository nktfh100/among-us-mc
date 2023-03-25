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

public class TaskStabilizeSteeringInv extends TaskInvHolder {

	private static final ArrayList<Integer> availableSlots = new ArrayList<Integer>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));

	private Integer activeSlot = 0;
	private Boolean isDone = false;

	public TaskStabilizeSteeringInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		this.activeSlot = availableSlots.get(Utils.getRandomNumberInRange(0, availableSlots.size() - 1));

		this.update();
	}

	public void crosshairClick() {
		if (!this.isDone) {
			Main.getSoundsManager().playSound("taskStabilizeSteeringClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
			this.isDone = true;
			this.update();
			this.checkDone();
		}
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskStabilizeSteeringInv inv = this;
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
		TaskStabilizeSteeringInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("StabilizeSteering_info").getItem().getItem());

		ItemInfoContainer crosshairItem = Main.getItemsManager().getItem("StabilizeSteering_crosshair");
		ItemStack crosshairItemS = this.isDone ? crosshairItem.getItem2().getItem() : crosshairItem.getItem().getItem();

		ClickAction action = new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.crosshairClick();
			}
		};

		Icon icon = new Icon(crosshairItemS);
		icon.addClickAction(action);
		this.setIcon(this.activeSlot, icon);

		icon = new Icon(crosshairItemS);
		icon.addClickAction(action);
		this.setIcon(this.activeSlot + 1, icon);

		icon = new Icon(crosshairItemS);
		icon.addClickAction(action);
		this.setIcon(this.activeSlot - 1, icon);

		icon = new Icon(crosshairItemS);
		icon.addClickAction(action);
		this.setIcon(this.activeSlot - 9, icon);

		icon = new Icon(crosshairItemS);
		icon.addClickAction(action);
		this.setIcon(this.activeSlot + 9, icon);
	}

	@Override
	public void invClosed() {
	}

}