package com.nktfh100.AmongUs.inventory.tasks;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskSwipeCardInv extends TaskInvHolder {

	private Boolean isCardClicked = false;
	private Boolean isDone = false;

	public TaskSwipeCardInv(Arena arena, TaskPlayer taskPlayer) {
		super(27, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Main.getMessagesManager().getTaskName(taskPlayer.getActiveTask().getTaskType().toString()),
				taskPlayer.getActiveTask().getLocationName().getName()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		this.update();
	}

	public void handleBoxClick() {
		if (this.isDone || !this.isCardClicked) {
			return;
		}

		Main.getSoundsManager().playSound("taskSwipeCardBoxClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());

		this.isDone = true;

		this.checkDone();
		this.update();
	}

	public void handleCardClick() {
		if (this.isDone || this.isCardClicked) {
			return;
		}

		Main.getSoundsManager().playSound("taskSwipeCardClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());

		this.isCardClicked = true;

		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskSwipeCardInv taskInv = this;
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
		TaskSwipeCardInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("swipeCard_info").getItem().getItem());

		ItemStack cardItemS = this.isCardClicked ? Main.getItemsManager().getItem("swipeCard_card").getItem2().getItem() : Main.getItemsManager().getItem("swipeCard_card").getItem().getItem();
		Utils.addItemFlag(cardItemS, ItemFlag.values());
		if (this.isCardClicked) {
			Utils.enchantedItem(cardItemS, Enchantment.DURABILITY, 1);
		}
		Icon cardIcon = new Icon(cardItemS);
		if (!this.isCardClicked) {
			cardIcon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleCardClick();
				}
			});
		}
		this.setIcon(10, cardIcon);

		Icon boxIcon = new Icon(Main.getItemsManager().getItem("swipeCard_box").getItem().getItem());
		if (this.isCardClicked) {
			boxIcon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleBoxClick();
				}
			});
		}
		this.setIcon(16, boxIcon);
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