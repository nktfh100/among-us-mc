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

public class TaskChartCourseInv extends TaskInvHolder {

	private static final ArrayList<ArrayList<Integer>> pointsSlots = new ArrayList<ArrayList<Integer>>();

	static {
		pointsSlots.add(new ArrayList<Integer>(Arrays.asList(10, 19, 28)));
		pointsSlots.add(new ArrayList<Integer>(Arrays.asList(12, 21, 30)));
		pointsSlots.add(new ArrayList<Integer>(Arrays.asList(14, 23, 32)));
		pointsSlots.add(new ArrayList<Integer>(Arrays.asList(16, 25, 34)));
	}

	private ArrayList<Integer> activeSlots = new ArrayList<Integer>();
	private Integer activePoint = 0;
	private Boolean isShipClicked = false;
	private Boolean isDone = false;

	public TaskChartCourseInv(Arena arena, TaskPlayer taskPlayer) {
		super(45, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		for (int i = 0; i < 4; i++) {
			this.activeSlots.add(pointsSlots.get(i).get(Utils.getRandomNumberInRange(0, 2)));
		}

		this.update();
	}

	public void shipClick() {
		Main.getSoundsManager().playSound("taskChartCourseShipClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		if (!this.isShipClicked && !this.isDone) {
			this.isShipClicked = true;
			this.update();
		}
	}

	public void pointClick(Integer pointId) {
		Main.getSoundsManager().playSound("taskChartCoursePointClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		if (this.isShipClicked && !this.isDone) {
			this.isShipClicked = false;
			if (this.activePoint == pointId - 1) {
				this.activePoint = pointId;
				if (pointId == 3) {
					this.isDone = true;
					this.checkDone();
				}
			}

			this.update();
		}
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskChartCourseInv inv = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					Player player = inv.getTaskPlayer().getPlayerInfo().getPlayer();
					if (player.getOpenInventory().getTopInventory() == inv.getInventory()) {
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

		this.inv.setItem(8, Main.getItemsManager().getItem("chartCourse_info").getItem().getItem());

		ItemInfoContainer shipItem = Main.getItemsManager().getItem("chartCourse_ship");
		ItemInfoContainer pointItem = Main.getItemsManager().getItem("chartCourse_point");
		ItemStack pointItemS = pointItem.getItem().getItem();

		TaskChartCourseInv inv = this;

		for (int i = 0; i < 4; i++) {
			Icon icon = new Icon(pointItemS);
			final Integer id_ = i;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.pointClick(id_);
				}
			});
			this.setIcon(this.activeSlots.get(i), icon);
		}

		ItemInfoContainer midItem = Main.getItemsManager().getItem("chartCourse_middle");
		ItemStack midItemS = midItem.getItem().getItem();

		this.inv.setItem(20, midItemS);
		this.inv.setItem(22, midItemS);
		this.inv.setItem(24, midItemS);

		ItemStack shipItemS = shipItem.getItem().getItem();
		if (this.isShipClicked) {
			Utils.enchantedItem(shipItemS, Enchantment.DURABILITY, 1);
		}
		Icon icon = new Icon(shipItemS);
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.shipClick();
			}
		});
		this.setIcon(this.activeSlots.get(this.activePoint), icon);
	}

	@Override
	public void invClosed() {
	}

}