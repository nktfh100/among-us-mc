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

public class TaskAcceptDivertedPowerInv extends TaskInvHolder {

	private final static ArrayList<Integer> leftSlots = new ArrayList<Integer>(Arrays.asList(0, 1, 11, 18, 19, 36, 37, 20, 29));
	private final static ArrayList<Integer> rightSlots = new ArrayList<Integer>(Arrays.asList(15, 7, 24, 25, 26, 33, 43, 44));

	private Boolean isDone = false;

	public TaskAcceptDivertedPowerInv(Arena arena, TaskPlayer taskPlayer) {
		super(45, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);

		this.update();
	}

	public void handleSwitchClick() {
		if (!this.isDone) {
			Main.getSoundsManager().playSound("taskAcceptDivertedPower_click", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
			this.isDone = true;
			this.checkDone();
			this.update();
		}
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskAcceptDivertedPowerInv taskInv = this;
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
		
		this.inv.setItem(8, Main.getItemsManager().getItem("acceptDivertedPower_info").getItem().getItem());
		
		TaskAcceptDivertedPowerInv inv = this;
		ItemInfoContainer wireItem = Main.getItemsManager().getItem("acceptDivertedPower_line");
		ItemStack wireItemS = wireItem.getItem().getItem();
		ItemStack wireItemS2 = wireItem.getItem2().getItem();

		for (Integer slot : leftSlots) {
			this.inv.setItem(slot, wireItemS);
		}

		for (Integer slot : rightSlots) {
			if (this.isDone) {
				this.inv.setItem(slot, wireItemS);
			} else {
				this.inv.setItem(slot, wireItemS2);
			}
		}
		if (this.isDone) {
			ItemInfoContainer switchItem = Main.getItemsManager().getItem("acceptDivertedPower_switchActive");
			this.setIcon(13, new Icon(Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ")));
			this.setIcon(31, new Icon(Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ")));

			this.setIcon(21, new Icon(switchItem.getItem().getItem()));
			this.setIcon(22, new Icon(switchItem.getItem2().getItem()));
			this.setIcon(23, new Icon(switchItem.getItem().getItem()));
		} else {
			ItemInfoContainer switchItem = Main.getItemsManager().getItem("acceptDivertedPower_switch");
			Icon icon = new Icon(switchItem.getItem().getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleSwitchClick();
				}
			});
			this.setIcon(13, icon);
			this.setIcon(31, icon);

			Icon icon1 = new Icon(switchItem.getItem2().getItem());
			icon1.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleSwitchClick();
				}
			});
			this.setIcon(22, icon1);
		}
	}

	@Override
	public void invClosed() {
	}

	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}
}