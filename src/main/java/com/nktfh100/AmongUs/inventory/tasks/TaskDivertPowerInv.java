package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.LocationName;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskDivertPowerInv extends TaskInvHolder {

	private final static ArrayList<Integer> locationsSlots = new ArrayList<Integer>(Arrays.asList(1, 3, 5, 7));
	private final static ArrayList<Integer> topFillBetweenSlots = new ArrayList<Integer>(Arrays.asList(10, 12, 14, 16));
	private final static ArrayList<Integer> leverSlots = new ArrayList<Integer>(Arrays.asList(19, 21, 23, 25));
	private final static ArrayList<Integer> bottomFillBetweenSlots = new ArrayList<Integer>(Arrays.asList(28, 30, 32, 34));

	private ArrayList<String> locations = new ArrayList<String>();
	private Integer activeLever = 0;
	private String activeLocation = "";
	private Boolean isLeverActive = false;
	private Boolean isDone = false;

	public TaskDivertPowerInv(Arena arena, TaskPlayer taskPlayer, ArrayList<String> locations_, String activeLocation_, Integer activeLever_) {
		super(36, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		if (activeLocation == null) {
			this.activeLocation = generateActiveLocation(taskPlayer);
		} else {
			this.activeLocation = activeLocation_;
		}

		if (locations_ == null || locations_.size() == 0) {
			this.locations = generateLocations(arena, this.activeLocation);
		} else {
			this.locations = locations_;
		}

		if (activeLever_ != null) {
			this.activeLever = generateLever(this.locations, this.activeLocation);
		} else {
			this.activeLever = activeLever_;
		}

		this.update();
	}

	public static String generateActiveLocation(TaskPlayer taskPlayer) {
		if (taskPlayer.getTasks().size() > 1) {
			if (taskPlayer.getState() < taskPlayer.getTasks().size() - 1) {
				return taskPlayer.getTasks().get(taskPlayer.getState() + 1).getLocationName().getName();
			}
		}
		return taskPlayer.getActiveTask().getLocationName().getName();
	}

	public static ArrayList<String> generateLocations(Arena arena, String activeLocation) {
		ArrayList<String> out = new ArrayList<String>();
		out.add(activeLocation);
		ArrayList<String> arenaLocs = new ArrayList<String>();
		for (LocationName locName : arena.getLocations().values()) {
			arenaLocs.add(locName.getName());
		}
		Collections.shuffle(arenaLocs);
		for (String loc_ : arenaLocs) {
			if (out.size() < 4) {
				if (!out.contains(loc_)) {
					out.add(loc_);
				}
			} else {
				break;
			}
		}

		while (out.size() < 4) {
			out.add(" ");
		}
		Collections.shuffle(out);
		return out;
	}

	public static Integer generateLever(ArrayList<String> locations, String activeLocation) {
		for (int i = 0; i < 4; i++) {
			if (activeLocation.equalsIgnoreCase(locations.get(i))) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskDivertPowerInv taskInv = this;
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
		TaskDivertPowerInv divertPowerInv = this;

		ItemInfo locationTopItem = Main.getItemsManager().getItem("divertPower_topLocation").getItem();
		ItemInfo leverItem = Main.getItemsManager().getItem("divertPower_lever").getItem();
		ItemInfoContainer activeLeverItem = Main.getItemsManager().getItem("divertPower_activeLever");
		ItemInfoContainer fillBetweenItem = Main.getItemsManager().getItem("divertPower_fillBetween");

		for (int i = 0; i < 4; i++) {
			Icon icon = new Icon(locationTopItem.getItem(this.locations.get(i), null));
			this.setIcon(locationsSlots.get(i), icon);
		}

		for (int i = 0; i < 4; i++) { // top fill between
			Icon icon = new Icon(this.activeLever == i && this.isDone ? activeLeverItem.getItem2().getItem() : this.isLeverActive && this.activeLever == i ? fillBetweenItem.getItem2().getItem() : fillBetweenItem.getItem().getItem());
			if (this.isLeverActive && i == this.activeLever) {
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						Main.getSoundsManager().playSound("taskDivertPower_moveLever", player, player.getLocation());
						divertPowerInv.setIsLeverActive(false);
						divertPowerInv.setIsDone(true);
						divertPowerInv.update();
						divertPowerInv.checkDone();
					}
				});
			}
			this.setIcon(topFillBetweenSlots.get(i), icon);
		}

		for (int i = 0; i < 4; i++) { // levers
			Icon icon;
			if (this.activeLever == i) {
				icon = new Icon(this.isDone ? fillBetweenItem.getItem().getItem() : this.isLeverActive ? activeLeverItem.getItem2().getItem() : activeLeverItem.getItem().getItem());
				if (!this.isLeverActive) {
					icon.addClickAction(new ClickAction() {
						@Override
						public void execute(Player player) {
							Main.getSoundsManager().playSound("taskDivertPower_clickLever", player, player.getLocation());
							divertPowerInv.setIsLeverActive(true);
							divertPowerInv.update();
						}
					});
				}
			} else {
				icon = new Icon(leverItem.getItem());
			}
			this.setIcon(leverSlots.get(i), icon);
		}

		for (int i = 0; i < 4; i++) { // bottom fill between
			Icon icon = new Icon(fillBetweenItem.getItem().getItem());
			this.setIcon(bottomFillBetweenSlots.get(i), icon);
		}

		this.inv.setItem(8, Main.getItemsManager().getItem("divertPower_info").getItem().getItem());
	}

	@Override
	public void invClosed() {
	}

	public Boolean getIsLeverActive() {
		return this.isLeverActive;
	}

	public void setIsLeverActive(Boolean is) {
		this.isLeverActive = is;
	}

	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}
}