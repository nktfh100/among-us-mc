package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.nktfh100.AmongUs.enums.TaskType;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskDataInv extends TaskInvHolder {

	private ArrayList<String> estimatedTimes;
	private Integer activeEstimatedTime = 0;
	private Integer percentage = 0;
	private TaskType type;
	private BukkitTask runnable;

	public TaskDataInv(Arena arena, TaskPlayer taskPlayer) {
		super(27, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Main.getMessagesManager().getTaskName(taskPlayer.getActiveTask().getTaskType().toString()),
				taskPlayer.getActiveTask().getLocationName().getName()), arena, taskPlayer);

		this.estimatedTimes = Main.getMessagesManager().getEstimatedTimes();
		Utils.fillInv(this.inv);

		this.type = taskPlayer.getActiveTask().getTaskType();

		this.inv.setItem(8, Main.getItemsManager().getItem(this.type == TaskType.DOWNLOAD_DATA ? "downloadData_info" : "uploadData_info").getItem().getItem());

		ItemInfo leftItem = Main.getItemsManager().getItem(this.type == TaskType.DOWNLOAD_DATA ? "downloadData_left" : "uploadData_left").getItem();
		String leftTitle = (this.type == TaskType.DOWNLOAD_DATA ? leftItem.getTitle(taskPlayer.getActiveTask().getLocationName().getName()) : leftItem.getTitle());
		ArrayList<String> leftLore = (this.type == TaskType.DOWNLOAD_DATA ? leftItem.getLore(taskPlayer.getActiveTask().getLocationName().getName()) : leftItem.getLore());
		this.inv.setItem(10, Utils.createItem(Material.OAK_SIGN, leftTitle, 1, leftLore));

		ItemStack progressItemS = Main.getItemsManager().getItem(this.type == TaskType.DOWNLOAD_DATA ? "downloadData_progress" : "uploadData_progress").getItem().getItem();
		for (int i = 11; i < 16; i++) {
			this.inv.setItem(i, progressItemS);
		}

		ItemInfo rightItem = Main.getItemsManager().getItem(this.type == TaskType.DOWNLOAD_DATA ? "downloadData_right" : "uploadData_right").getItem();
		this.inv.setItem(16, rightItem.getItem());

		ItemInfo startItem = Main.getItemsManager().getItem(this.type == TaskType.DOWNLOAD_DATA ? "downloadData_start" : "uploadData_start").getItem();
		Icon icon = new Icon(startItem.getItem());
		TaskDataInv dataInv = this;
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				dataInv.start(player);
			}
		});
		this.setIcon(22, icon);
	}

	public void start(Player player) {
		Main.getSoundsManager().playSound("taskDataStart", player, player.getLocation());
		this.removeIcon(22);
		this.inv.setItem(22, Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
		TaskDataInv DataInv = this;
		this.runnable = new BukkitRunnable() { // 11 seconds

			@Override
			public void run() {
				Integer newPerc = DataInv.getPercentage() + 10;
				if (newPerc <= 100) {
					DataInv.setPercentage(newPerc);
					if (DataInv.getActiveEstimatedTime() + 1 < estimatedTimes.size()) {
						DataInv.setActiveEstimatedTime(DataInv.getActiveEstimatedTime() + 1);
					}
				} else {
					DataInv.checkDone();
				}
			}
		}.runTaskTimer(Main.getPlugin(), 10L, 20L);
	}

	@Override
	public void update() {
		ItemInfoContainer progressItem = Main.getItemsManager().getItem(this.type == TaskType.DOWNLOAD_DATA ? "downloadData_progress" : "uploadData_progress");
		ItemStack progressItemS = progressItem.getItem().getItem();
		ItemStack progressItem2S = progressItem.getItem2().getItem();

		int slotI = 11;
		for (int i = 0; i < 100; i += 20) {
			if (i > this.percentage) {
				this.inv.setItem(slotI, progressItemS);
			} else {
				this.inv.setItem(slotI, progressItem2S);
			}
			slotI++;
		}

		ItemInfo startItem = Main.getItemsManager().getItem(this.type == TaskType.DOWNLOAD_DATA ? "downloadData_start" : "uploadData_start").getItem2();
		this.inv.setItem(22, startItem.getItem(estimatedTimes.get(this.activeEstimatedTime), null));
	}

	@Override
	public Boolean checkDone() {
		if (this.percentage >= 100) {
			this.runnable.cancel();
			this.taskPlayer.taskDone();
			this.taskPlayer.getPlayerInfo().getPlayer().closeInventory();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void invClosed() {
		if (this.runnable != null) {
			this.runnable.cancel();
		}
	}

	public Arena getArena() {
		return arena;
	}

	public TaskPlayer getTaskPlayer() {
		return taskPlayer;
	}

	public Integer getPercentage() {
		return percentage;
	}

	public void setPercentage(Integer percentage) {
		this.percentage = percentage;
		this.update();
	}

	public Integer getActiveEstimatedTime() {
		return activeEstimatedTime;
	}

	public void setActiveEstimatedTime(Integer activeEstimatedTime) {
		this.activeEstimatedTime = activeEstimatedTime;
	}

	public TaskType getType() {
		return type;
	}
}