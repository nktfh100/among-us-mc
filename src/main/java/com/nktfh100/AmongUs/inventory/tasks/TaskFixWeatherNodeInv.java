package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.enchantments.Enchantment;
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

public class TaskFixWeatherNodeInv extends TaskInvHolder {

	private Integer activeLocation = 0;
	private Boolean isHeadClicked = false;
	private Boolean isDone = false;

	public TaskFixWeatherNodeInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.update();
	}

	public static ArrayList<Integer> generateMaze() {
		ArrayList<Integer> out = new ArrayList<Integer>();
		Boolean isOk = false;
		Integer slot = 10;
		out.add(1);
		out.add(10);
		while (!isOk) {
			float rand = new Random().nextFloat();
			int nextMove = 0; // down
			if (rand >= 0.33333333333 && rand <= 0.66666666666) {
				nextMove = 1; // up
			}
			if (rand > 0.66666666666) {
				nextMove = 2; // right
			}
			if (slot == 7 || slot == 16 || slot == 25 || slot == 34 || slot == 43) {
				nextMove = 0;
			}
			if (slot == 45 || slot == 46) {
				nextMove = 2;
			}

			if (nextMove == 0 && slot >= 36 && slot <= 42) {
				nextMove = 1;
			}

			if (nextMove == 1 && (slot >= 10 && slot <= 16)) {
				nextMove = 0;
			}

			if (nextMove == 1 && out.contains(slot - 10)) {
				nextMove = 2;
			}

			if (nextMove == 0 && out.contains(slot + 9)) {
				nextMove = 2;
			}

			if (nextMove == 1 && out.contains(slot - 9)) {
				nextMove = 2;
			}

			if (nextMove == 0) {
				slot = slot + 9; // down
			}
			if (nextMove == 1) {
				slot = slot - 9; // up
			}
			if (nextMove == 2) {
				slot = slot + 1; // right
			}

			if (slot < 53) {
				out.add(slot);
			}

			if (slot >= 52) {
				isOk = true;
				break;
			}
		}
		return out;
	}

	public void handleHeadClick() {
		if (this.isDone || this.isHeadClicked) {
			return;
		}
		this.isHeadClicked = true;
		this.update();
	}

	public void handleTargetClick() {
		if (this.isDone) {
			return;
		}
		Main.getSoundsManager().playSound("taskFixWeatherNode_click", this.getPlayerInfo().getPlayer(), this.getPlayerInfo().getPlayer().getLocation());
		this.isHeadClicked = false;
		this.activeLocation++;
		if (this.activeLocation >= this.taskPlayer.getMaze_().size() - 1) {
			this.isDone = true;
		}
		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskFixWeatherNodeInv inv = this;
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
		TaskFixWeatherNodeInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("fixWeatherNode_info").getItem().getItem());

		ItemStack mazeBGItemS = Main.getItemsManager().getItem("fixWeatherNode_mazeBG").getItem().getItem();
		ItemInfoContainer mazeItemInfo = Main.getItemsManager().getItem("fixWeatherNode_maze");
		ItemStack mazeItem = mazeItemInfo.getItem().getItem();
		ItemStack mazeHeadItemS = mazeItemInfo.getItem2().getItem();
		ItemStack mazeTargetItemS = mazeItemInfo.getItem3().getItem();
		if (this.isHeadClicked) {
			Utils.enchantedItem(mazeHeadItemS, Enchantment.DURABILITY, 1);
		}

		int i = 0;
		for (Integer slot : this.taskPlayer.getMaze_()) {
			if (this.activeLocation == i) {
				Icon icon = new Icon(mazeHeadItemS);
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.handleHeadClick();
					}
				});
				this.setIcon(slot, icon);
			} else if (this.activeLocation + 1 == i && this.isHeadClicked) {
				Icon icon = new Icon(mazeTargetItemS);
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.handleTargetClick();
					}
				});
				this.setIcon(slot, icon);
			} else if (this.activeLocation < i) {
				Icon icon = new Icon(mazeBGItemS);
				this.setIcon(slot, icon);
			} else {
				Icon icon = new Icon(mazeItem);
				this.setIcon(slot, icon);
			}
			i++;
		}

	}

	@Override
	public void invClosed() {
	}
}