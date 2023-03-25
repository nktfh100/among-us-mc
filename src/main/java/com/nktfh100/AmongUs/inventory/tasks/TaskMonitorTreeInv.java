package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

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

public class TaskMonitorTreeInv extends TaskInvHolder {

	private static final ArrayList<ArrayList<Integer>> barsSlots = new ArrayList<ArrayList<Integer>>();
	private static final ArrayList<String> barColors = new ArrayList<String>(Arrays.asList("Yellow", "Green", "Red", "Blue"));

	static {
		barsSlots.add(new ArrayList<Integer>(Arrays.asList(46, 37, 28, 19, 10)));
		barsSlots.add(new ArrayList<Integer>(Arrays.asList(48, 39, 30, 21, 12)));
		barsSlots.add(new ArrayList<Integer>(Arrays.asList(50, 41, 32, 23, 14)));
		barsSlots.add(new ArrayList<Integer>(Arrays.asList(52, 43, 34, 25, 16)));
	}

	private ArrayList<Integer> barsHeight = new ArrayList<Integer>(Arrays.asList(-1, -1, -1, -1));
	private ArrayList<Integer> barsTarget = new ArrayList<Integer>(Arrays.asList(Utils.getRandomNumberInRange(1, 4), Utils.getRandomNumberInRange(1, 4), Utils.getRandomNumberInRange(1, 4), Utils.getRandomNumberInRange(1, 4)));
	private Integer clickedBar = -1;
	private Boolean isDone = false;

	public TaskMonitorTreeInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.update();
	}

	public void handleBarClick(Integer i) {
		if (this.isDone) {
			return;
		}
		if (this.barsHeight.get(i) + 1 != this.barsTarget.get(i)) {
			this.clickedBar = i;
			Main.getSoundsManager().playSound("taskMonitorTree_barClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		}
		this.update();
	}

	public void handleTargetClick(Integer i) {
		if (this.isDone) {
			return;
		}
		if (this.clickedBar != -1) {
			Main.getSoundsManager().playSound("taskMonitorTree_barClickDone", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
			this.barsHeight.set(i, this.barsTarget.get(i) - 1);
			this.clickedBar = -1;
			Boolean isDone_ = true;
			for (int i1 = 0; i1 < 4; i1++) {
				if (this.barsHeight.get(i1) + 1 != this.barsTarget.get(i1)) {
					isDone_ = false;
					break;
				}
			}
			this.isDone = isDone_;
		}
		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskMonitorTreeInv inv = this;
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
		TaskMonitorTreeInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("monitorTree_info").getItem().getItem());
		ItemInfoContainer barBGItem = Main.getItemsManager().getItem("monitorTree_barBG");
		ItemStack barBgItemS = barBGItem.getItem().getItem();

		for (int i = 0; i < 4; i++) {
			ItemInfoContainer barItem = Main.getItemsManager().getItem("monitorTree_bar" + barColors.get(i));
			ItemStack barItem2S = barItem.getItem2().getItem();

			Integer height = this.barsHeight.get(i);
			Integer target = this.barsTarget.get(i);
			final Integer i_ = i;
			for (int i1 = 0; i1 < 5; i1++) {
				if (i1 == 0) {
					ItemStack item_ = barItem.getItem().getItem();
					if (clickedBar == i) {
						Utils.enchantedItem(item_, Enchantment.DURABILITY, 1);
					}
					Icon icon = new Icon(item_);
					if (height + 1 != target) {
						icon.addClickAction(new ClickAction() {
							@Override
							public void execute(Player player) {
								inv.handleBarClick(i_);
							}
						});
					}
					this.setIcon(barsSlots.get(i).get(i1), icon);
					continue;
				}
				if (height + 1 != target && i1 == target) {
					Icon icon = new Icon(barBGItem.getItem2().getItem());
					icon.addClickAction(new ClickAction() {
						@Override
						public void execute(Player player) {
							inv.handleTargetClick(i_);
						}
					});
					this.setIcon(barsSlots.get(i).get(i1), icon);
					continue;
				}
				if (i1 > height + 1) {
					this.setIcon(barsSlots.get(i).get(i1), new Icon(barBgItemS));
				} else {
					this.setIcon(barsSlots.get(i).get(i1), new Icon(barItem2S));
				}
			}
		}

	}

	@Override
	public void invClosed() {
	}
}