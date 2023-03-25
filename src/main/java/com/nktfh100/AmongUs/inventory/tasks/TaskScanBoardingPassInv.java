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

public class TaskScanBoardingPassInv extends TaskInvHolder {

	private static final ArrayList<Integer> passSlots = new ArrayList<Integer>(Arrays.asList(10, 11, 12, 19, 21, 28, 29, 30));
	private static final ArrayList<Integer> scannerSlots = new ArrayList<Integer>(Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34));
	private Boolean isPassClicked = false;
	private Boolean isDone = false;

	public TaskScanBoardingPassInv(Arena arena, TaskPlayer taskPlayer) {
		super(45, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.update();
	}

	public void handlePassClick() {
		if (this.isDone || this.isPassClicked) {
			return;
		}

		Main.getSoundsManager().playSound("taskScanBoardingPass_cardClick", this.getPlayerInfo().getPlayer(), this.getPlayerInfo().getPlayer().getLocation());
		this.isPassClicked = true;
		this.update();
	}

	public void handleScannerClick() {
		if (this.isDone || !this.isPassClicked) {
			return;
		}

		Main.getSoundsManager().playSound("taskScanBoardingPass_scannerClick", this.getPlayerInfo().getPlayer(), this.getPlayerInfo().getPlayer().getLocation());

		this.isDone = true;

		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskScanBoardingPassInv inv = this;
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
		TaskScanBoardingPassInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("scanBoardingPass_info").getItem().getItem());

		ItemInfoContainer scannerItemInfo = Main.getItemsManager().getItem("scanBoardingPass_scanner");
		ItemStack scannerItemS = scannerItemInfo.getItem().getItem();
		if (this.isDone) {
			scannerItemS = scannerItemInfo.getItem3().getItem();
		} else if (this.isPassClicked) {
			scannerItemS = scannerItemInfo.getItem2().getItem();
		}
		Icon scannerIcon = new Icon(scannerItemS);
		scannerIcon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleScannerClick();
			}
		});
		for (Integer slot : scannerSlots) {
			this.setIcon(slot, scannerIcon);
		}

		ItemInfoContainer cardItemInfo = Main.getItemsManager().getItem("scanBoardingPass_card");
		ItemStack cardItemS = cardItemInfo.getItem().getItem();
		if (this.isPassClicked) {
			cardItemS = Utils.createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
		}
		Icon cardIcon = new Icon(cardItemS);
		if (!this.isPassClicked) {
			cardIcon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handlePassClick();
				}
			});
		}
		for (Integer slot : passSlots) {
			this.setIcon(slot, cardIcon);
		}
		if (this.isPassClicked) {
			this.setIcon(20, cardIcon);
		} else {
			this.setIcon(20, new Icon(cardItemInfo.getItem2().getItem(this.getPlayerInfo().getPlayer().getName(), null)));
		}
	}

	@Override
	public void invClosed() {
	}
}