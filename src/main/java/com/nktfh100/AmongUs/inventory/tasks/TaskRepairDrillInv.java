package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
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

public class TaskRepairDrillInv extends TaskInvHolder {

	private static final ArrayList<ArrayList<Integer>> squaresSlots = new ArrayList<ArrayList<Integer>>();
	static {
		squaresSlots.add(new ArrayList<Integer>(Arrays.asList(1, 2, 10, 11)));
		squaresSlots.add(new ArrayList<Integer>(Arrays.asList(37, 38, 46, 47)));
		squaresSlots.add(new ArrayList<Integer>(Arrays.asList(6, 7, 15, 16)));
		squaresSlots.add(new ArrayList<Integer>(Arrays.asList(42, 43, 51, 52)));
	}
	private ArrayList<Integer> squaresLeft = new ArrayList<Integer>(Arrays.asList(4, 4, 4, 4));
	private Boolean isDone = false;

	public TaskRepairDrillInv(Arena arena, TaskPlayer taskPlayer, Boolean isHot) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Main.getMessagesManager().getTaskName(taskPlayer.getActiveTask().getTaskType().toString()), taskPlayer.getActiveTask().getLocationName().getName()), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.update();
	}

	public void handleSquareClick(Integer i) {
		if (this.isDone) {
			return;
		}
		Main.getSoundsManager().playSound("taskRepairDrill_click", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		if (this.squaresLeft.get(i) > 0) {
			this.squaresLeft.set(i, this.squaresLeft.get(i) - 1);
		}
		Boolean isDone_ = true;
		for (Integer left : this.squaresLeft) {
			if (left != 0) {
				isDone_ = false;
				break;
			}
		}
		this.isDone = isDone_;
		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskRepairDrillInv inv = this;
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
		TaskRepairDrillInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("repairDrill_info").getItem().getItem());

		ItemInfoContainer squareItem = Main.getItemsManager().getItem("repairDrill_square");

		for (int i = 0; i < 4; i++) {
			ItemStack squareItemS = squareItem.getItem().getItem(this.squaresLeft.get(i) + "", "");
			if (this.squaresLeft.get(i) == 0) {
				squareItemS = Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
			} else {
				squareItemS.setAmount(this.squaresLeft.get(i));
			}
			Icon icon = new Icon(squareItemS);
			final Integer i_ = i;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleSquareClick(i_);
				}
			});
			for (Integer slot : squaresSlots.get(i)) {
				this.setIcon(slot, icon);
			}
		}

		ItemInfoContainer statusItem = Main.getItemsManager().getItem("repairDrill_status");
		this.inv.setItem(49, this.isDone ? statusItem.getItem2().getItem() : statusItem.getItem().getItem());
	}

	@Override
	public void invClosed() {
	}
}