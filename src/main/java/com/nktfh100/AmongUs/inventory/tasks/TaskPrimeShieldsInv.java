package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

public class TaskPrimeShieldsInv extends TaskInvHolder {

	private static ArrayList<ArrayList<Integer>> slots = new ArrayList<ArrayList<Integer>>();

	static {
		slots.add(new ArrayList<Integer>(Arrays.asList(10, 11, 19, 20)));
		slots.add(new ArrayList<Integer>(Arrays.asList(3, 4, 12, 13)));
		slots.add(new ArrayList<Integer>(Arrays.asList(14, 15, 23, 24)));
		slots.add(new ArrayList<Integer>(Arrays.asList(28, 29, 37, 38)));
		slots.add(new ArrayList<Integer>(Arrays.asList(39, 40, 48, 49)));
		slots.add(new ArrayList<Integer>(Arrays.asList(32, 33, 41, 42)));
		slots.add(new ArrayList<Integer>(Arrays.asList(21, 22, 30, 31)));
	}

	private ArrayList<Boolean> squares = new ArrayList<Boolean>();
	private Boolean isDone = false;

	public TaskPrimeShieldsInv(Arena arena, TaskPlayer taskPlayer, ArrayList<Boolean> squares_) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);

		if (squares_ == null || squares_.size() == 0) {
			this.squares = generateShields();
		} else {
			this.squares = squares_;
		}

		this.update();
	}

	public static ArrayList<Boolean> generateShields() {
		ArrayList<Boolean> out = new ArrayList<Boolean>();
		Integer offNum = Utils.getRandomNumberInRange(3, 6);
		for (int i = 0; i < 7; i++) {
			if (offNum > 0) {
				out.add(false); // red
				offNum--;
			} else {
				out.add(true); // white
			}
		}
		Collections.shuffle(out);
		return out;
	}

	public void handleSquareClick(Player player, Integer clicked) {
		if (this.isDone) {
			return;
		}
		Boolean newSquareState = !this.squares.get(clicked);
		this.squares.set(clicked, newSquareState);

		if (newSquareState) {
			Main.getSoundsManager().playSound("taskPrimeShieldsClickOn", player, player.getLocation());
		} else {
			Main.getSoundsManager().playSound("taskPrimeShieldsClickOff", player, player.getLocation());
		}

		Boolean isDone_ = true;
		for (Boolean square : this.squares) {
			if (!square) {
				isDone_ = false;
				break;
			}
		}
		if (isDone_) {
			this.isDone = true;
		}

		this.checkDone();
		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			if (arena.getEnableVisualTasks() && this.taskPlayer.getActiveTask().getEnableVisuals() && !this.pInfo.isGhost()) {
				this.arena.turnPrimeShieldsOn();
			}
			this.taskPlayer.taskDone();
			TaskPrimeShieldsInv taskInv = this;
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
		
		this.inv.setItem(8, Main.getItemsManager().getItem("primeShields_info").getItem().getItem());
		
		TaskPrimeShieldsInv inv = this;
		ItemInfoContainer squareItem = Main.getItemsManager().getItem("primeShields_square");
		for (int i = 0; i < this.squares.size(); i++) {
			Boolean square = this.squares.get(i);
			ItemStack squareItemS = square ? squareItem.getItem().getItem() : squareItem.getItem2().getItem();
			Icon icon = new Icon(squareItemS);
			final Integer squareI = i;
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleSquareClick(player, squareI);
				}
			});
			for (Integer slot_ : slots.get(i)) {
				this.setIcon(slot_, icon);
			}
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