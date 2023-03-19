package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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

public class TaskRebootWifiInv extends TaskInvHolder {

	private static final ArrayList<Integer> screenSlots = new ArrayList<Integer>(Arrays.asList(29, 30, 31, 38, 39, 40, 47, 48, 49));

	private Boolean isLeverClicked = false;
	private Boolean isDone = false;
	private BukkitTask runnable = null;

	public TaskRebootWifiInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		TaskRebootWifiInv inv = this;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (inv == null || inv.getIsDone()) {
					this.cancel();
					return;
				}
				inv.update();
			}
		}.runTaskTimer(Main.getPlugin(), 20L, 20L);
		this.update();
	}

	public void handleLeverClick() {
		if (this.isDone || this.isLeverClicked) {
			return;
		}
		if (!this.taskPlayer.getRebootIsRunning_()) {
			this.isLeverClicked = true;
		} else if (this.taskPlayer.getRebootTimer_() == 0) {
			this.isLeverClicked = true;
		}

		this.update();
	}

	public void handleLeverTargetClick() {
		if (this.isDone || !this.isLeverClicked) {
			return;
		}
		this.isLeverClicked = false;

		if (this.taskPlayer.getRebootIsRunning_() && this.taskPlayer.getRebootTimer_() == 0) {
			this.isDone = true;
//			Main.getSoundsManager().playSound("taskInspectSampleStartClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		} else if (!this.taskPlayer.getRebootIsRunning_()) {
			this.taskPlayer.setRebootIsRunning_(true);
		}

		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskRebootWifiInv taskInv = this;
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
		TaskRebootWifiInv inv = this;
		Boolean isRunning = this.taskPlayer.getRebootIsRunning_();

		ItemInfoContainer leverItemInfo = Main.getItemsManager().getItem("rebootWifi_lever");

		ClickAction leverCA = new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleLeverClick();
			}
		};
		ClickAction leverTargetCA = new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleLeverTargetClick();
			}
		};
		if (!isRunning) {
			ItemStack item_ = leverItemInfo.getItem().getItem();
			if (this.isLeverClicked) {
				Utils.enchantedItem(item_, Enchantment.DURABILITY, 1);
			}
			Icon icon = new Icon(item_);
			icon.addClickAction(leverCA);
			this.setIcon(15, icon);
			this.setIcon(16, icon);

			icon = new Icon(Main.getItemsManager().getItem("rebootWifi_leverTarget").getItem().getItem());
			icon.addClickAction(leverTargetCA);
			this.setIcon(51, icon);
			this.setIcon(52, icon);
		} else {
			ItemStack item_ = null;
			if (this.taskPlayer.getRebootTimer_() > 0) {
				item_ = leverItemInfo.getItem2().getItem(this.taskPlayer.getRebootTimer_() + "", null);
			} else if (!this.isDone) {
				item_ = leverItemInfo.getItem3().getItem();
			} else {
				item_ = Main.getItemsManager().getItem("rebootWifi_leverDone").getItem().getItem();
			}

			if (this.isLeverClicked) {
				Utils.enchantedItem(item_, Enchantment.DURABILITY, 1);
			}
			Icon icon = new Icon(item_);
			icon.addClickAction(leverCA);
			this.setIcon(this.isDone ? 15 : 51, icon);
			this.setIcon(this.isDone ? 16 : 52, icon);

			if (!this.isDone) {
				icon = new Icon(this.taskPlayer.getRebootTimer_() == 0 ? Main.getItemsManager().getItem("rebootWifi_leverTarget").getItem().getItem() : Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
				icon.addClickAction(leverTargetCA);
				this.setIcon(15, icon);
				this.setIcon(16, icon);
			} else {
				icon = new Icon(Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
				this.setIcon(51, icon);
				this.setIcon(52, icon);
			}
		}

		ItemInfoContainer topScreenItemInfo = Main.getItemsManager().getItem("rebootWifi_screenTop");
		ItemStack topScreenItemS = null;
		if (!isRunning) {
			topScreenItemS = topScreenItemInfo.getItem().getItem();
		} else if (isRunning) {
			if (this.isDone) {
				topScreenItemS = Main.getItemsManager().getItem("rebootWifi_screenTopDone").getItem().getItem();
			} else {
				if (this.taskPlayer.getRebootTimer_() == 0) {
					topScreenItemS = topScreenItemInfo.getItem3().getItem(this.taskPlayer.getRebootTimer_() + "", null);
				} else {
					topScreenItemS = topScreenItemInfo.getItem2().getItem(this.taskPlayer.getRebootTimer_() + "", null);
					topScreenItemS.setAmount(this.taskPlayer.getRebootTimer_() > 0 ? this.taskPlayer.getRebootTimer_() : 1);
				}
			}
		}

		for (int i = 0; i < 3; i++) {
			this.inv.setItem(11 + i, topScreenItemS);
		}

		ItemInfoContainer bottomScreenItemInfo = Main.getItemsManager().getItem("rebootWifi_screenBottom");
		ItemStack bottomScreenItemS = null;
		if (!isRunning) {
			bottomScreenItemS = bottomScreenItemInfo.getItem().getItem();
		} else if (isRunning) {
			if (this.isDone) {
				bottomScreenItemS = bottomScreenItemInfo.getItem3().getItem();
			} else {
				bottomScreenItemS = bottomScreenItemInfo.getItem2().getItem();
			}
		}

		for (Integer slot : screenSlots) {
			this.inv.setItem(slot, bottomScreenItemS);
		}
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