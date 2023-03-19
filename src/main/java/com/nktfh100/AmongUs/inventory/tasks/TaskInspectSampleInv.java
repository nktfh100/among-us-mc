package com.nktfh100.AmongUs.inventory.tasks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskInspectSampleInv extends TaskInvHolder {

	private Boolean isDone = false;
	private BukkitTask runnable = null;

	public TaskInspectSampleInv(Arena arena, TaskPlayer taskPlayer) {
		super(36, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		TaskInspectSampleInv inv = this;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (inv.getIsDone()) {
					this.cancel();
					return;
				}
				inv.update();
			}
		}.runTaskTimer(Main.getPlugin(), 20L, 20L);
		this.update();
	}

	public void handleStartClick() {
		if (this.isDone || this.taskPlayer.getInspectIsRunning_()) {
			return;
		}
		Main.getSoundsManager().playSound("taskInspectSampleStartClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		this.taskPlayer.setInspectIsRunning_(true);

		this.update();
	}

	public void handleSelectClick(Integer clickedI) {
		if (this.isDone || this.taskPlayer.getInspectIsRunning_()) {
			return;
		}

		if (clickedI == this.taskPlayer.getInspectAnomaly_()) {
			Main.getSoundsManager().playSound("taskInspectSampleSelectRight", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
			this.isDone = true;
			this.checkDone();
		} else {
			Main.getSoundsManager().playSound("taskInspectSampleSelectWrong", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
			this.taskPlayer.updateTasksVars();
		}

		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskInspectSampleInv taskInv = this;
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
		TaskInspectSampleInv inv = this;
		Boolean isRunning = this.taskPlayer.getInspectIsRunning_();

		ItemInfoContainer bottleItem = Main.getItemsManager().getItem("inspectSample_tube");
		ItemInfoContainer buttonItem = Main.getItemsManager().getItem("inspectSample_selectButton");
		ItemStack buttonItemS = buttonItem.getItem().getItem();
		if (this.taskPlayer.getInspectTimer_() == 0) {
			buttonItemS = buttonItem.getItem3().getItem();
		} else if (isRunning) {
			buttonItemS = buttonItem.getItem2().getItem();
		}

		ItemStack bottleItemS = Utils.createItem(Material.GLASS_BOTTLE, bottleItem.getItem().getTitle(), 1, bottleItem.getItem().getLore());
		if (isRunning || this.taskPlayer.getInspectTimer_() == 0) {
			bottleItemS = new ItemStack(Material.POTION);
			Utils.addItemFlag(bottleItemS, ItemFlag.HIDE_POTION_EFFECTS);

			PotionMeta pm = (PotionMeta) bottleItemS.getItemMeta();
			pm.setBasePotionData(new PotionData(PotionType.AWKWARD));
			bottleItemS.setItemMeta(pm);
			Utils.setItemName(bottleItemS, bottleItem.getItem2().getTitle(), bottleItem.getItem2().getLore());
		}
		int slot_ = 11;
		for (int i = 0; i < 5; i++) {
			// bottles
			if (this.taskPlayer.getInspectAnomaly_() == i) {
				ItemStack potion = new ItemStack(Material.POTION);
				PotionMeta pm = (PotionMeta) potion.getItemMeta();
				pm.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
				potion.setItemMeta(pm);
				Utils.setItemName(potion, bottleItem.getItem3().getTitle(), bottleItem.getItem3().getLore());
				Utils.addItemFlag(potion, ItemFlag.HIDE_POTION_EFFECTS);
				this.inv.setItem(slot_, potion);
			} else {
				this.inv.setItem(slot_, bottleItemS);
			}

			// buttons
			Icon btnIcon = new Icon(buttonItemS);
			if (!isRunning && this.taskPlayer.getInspectTimer_() == 0) {
				final Integer clicked = i;
				btnIcon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.handleSelectClick(clicked);
					}
				});
			}
			this.setIcon(slot_ + 9, btnIcon);
			slot_++;
		}

		String timer_ = this.taskPlayer.getInspectTimer_() + "";
		ItemInfoContainer infoItem = Main.getItemsManager().getItem("inspectSample_bottomETA");
		ItemStack infoItemS = isRunning ? infoItem.getItem2().getItem(timer_, null) : infoItem.getItem().getItem(timer_, null);
		if (this.taskPlayer.getInspectTimer_() == 0) {
			infoItemS = infoItem.getItem3().getItem(timer_, null);
		}
		if (isRunning) {
			infoItemS.setAmount(this.taskPlayer.getInspectTimer_());
		}
		this.inv.setItem(31, infoItemS);

		ItemInfoContainer startBtnItem = Main.getItemsManager().getItem("inspectSample_startButton");
		ItemStack startBtnItemS = isRunning ? startBtnItem.getItem2().getItem() : startBtnItem.getItem().getItem();
		if (this.taskPlayer.getInspectTimer_() == 0) {
			startBtnItemS = startBtnItem.getItem3().getItem();
		}

		Icon icon = new Icon(startBtnItemS);
		if (!isRunning && this.taskPlayer.getInspectTimer_() != 0) {
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleStartClick();
				}
			});
		}
		this.setIcon(34, icon);

		this.inv.setItem(8, Main.getItemsManager().getItem("inspectSample_info").getItem().getItem());
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