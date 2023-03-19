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

public class TaskOpenWaterwaysInv extends TaskInvHolder {

	private static final ArrayList<Integer> circleSlots = new ArrayList<Integer>(Arrays.asList(12, 13, 14, 23, 32, 31, 30, 21));
	private static final ArrayList<Integer> waterBarSlots = new ArrayList<Integer>(Arrays.asList(44, 35, 26, 17, 8));

	private Integer waterProgress = 0;
	private Integer firstColor = 0;
	private long lastTimeClicked = System.currentTimeMillis() - 2000;
	private Boolean isDone = false;

	public TaskOpenWaterwaysInv(Arena arena, TaskPlayer taskPlayer) {
		super(45, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.update();
	}

	public void handleCircleClick() {
		if (this.isDone) {
			return;
		}
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - this.lastTimeClicked;
		if (timeElapsed > 1300) {
			Main.getSoundsManager().playSound("taskOpenWaterways_valveClick", this.getPlayerInfo().getPlayer(), this.getPlayerInfo().getPlayer().getLocation());
			this.lastTimeClicked = System.currentTimeMillis();
			this.waterProgress++;
			this.firstColor = this.firstColor == 0 ? 1 : 0;
			if (this.waterProgress >= 5) {
				this.isDone = true;
			}
			this.update();
			this.checkDone();
		}
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskOpenWaterwaysInv inv = this;
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
		TaskOpenWaterwaysInv inv = this;

		this.inv.setItem(7, Main.getItemsManager().getItem("openWaterways_info").getItem().getItem());

		ItemInfoContainer circleItems = Main.getItemsManager().getItem("openWaterways_circle");
		ItemStack circleItemS = circleItems.getItem().getItem();
		ItemStack circleItem2S = circleItems.getItem2().getItem();

		ClickAction ca = new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleCircleClick();
			}
		};
		Integer color_ = this.firstColor;
		for (Integer slot : circleSlots) {
			Icon icon = new Icon(color_ == 0 ? circleItemS : circleItem2S);
			icon.addClickAction(ca);
			this.setIcon(slot, icon);
			if (slot == 21) {
				icon = new Icon(color_ == 0 ? circleItemS : circleItem2S);
				icon.addClickAction(ca);
				this.setIcon(20, icon);
			} else if (slot == 13) {
				icon = new Icon(color_ == 0 ? circleItemS : circleItem2S);
				icon.addClickAction(ca);
				this.setIcon(4, icon);
			} else if (slot == 23) {
				icon = new Icon(color_ == 0 ? circleItemS : circleItem2S);
				icon.addClickAction(ca);
				this.setIcon(24, icon);
			} else if (slot == 31) {
				icon = new Icon(color_ == 0 ? circleItemS : circleItem2S);
				icon.addClickAction(ca);
				this.setIcon(40, icon);
			}
			color_ = color_ == 0 ? 1 : 0;
		}

		ItemInfoContainer waterBarItems = Main.getItemsManager().getItem("openWaterways_waterBar");
		ItemStack waterBarS = waterBarItems.getItem().getItem();
		ItemStack waterBar2S = waterBarItems.getItem2().getItem();

		for (int i = 0; i < 5; i++) {
			if (i < this.waterProgress) {
				this.inv.setItem(waterBarSlots.get(i), waterBar2S);
			} else {
				this.inv.setItem(waterBarSlots.get(i), waterBarS);
			}
		}

	}

	@Override
	public void invClosed() {
	}
}