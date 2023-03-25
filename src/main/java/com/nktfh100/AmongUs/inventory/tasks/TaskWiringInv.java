package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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

public class TaskWiringInv extends TaskInvHolder {

	private static final HashMap<String, Integer> wiresRightSlots = new HashMap<String, Integer>();
	static {
		wiresRightSlots.put("red", 16);
		wiresRightSlots.put("blue", 25);
		wiresRightSlots.put("yellow", 34);
		wiresRightSlots.put("pink", 43);
	}

	// for the 2 sides
	private ArrayList<HashMap<String, Boolean>> wiresStates = new ArrayList<HashMap<String, Boolean>>();

	private String activeWire = "";

	public TaskWiringInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);

		TaskWiringInv inv = this;

		this.wiresStates.add(new HashMap<String, Boolean>());
		for (String colorStr : wiresRightSlots.keySet()) {
			this.wiresStates.get(0).put(colorStr, false);
		}
		this.wiresStates.add(new HashMap<String, Boolean>());
		for (String colorStr : wiresRightSlots.keySet()) {
			this.wiresStates.get(1).put(colorStr, false);
		}

		for (String key : wiresRightSlots.keySet()) {
			ItemInfoContainer wireItemInfo = Main.getItemsManager().getItem("wiring_" + key);
			Icon icon = new Icon(wireItemInfo.getItem().getItem());
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleWireRightClick(key);
				}
			});
			this.setIcon(wiresRightSlots.get(key), icon);
		}

		this.update();
	}

	@Override
	public Boolean checkDone() {
		for (Boolean wireState : this.wiresStates.get(0).values()) {
			if (wireState == false) {
				return false;
			}
		}
		TaskWiringInv taskInv = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = taskInv.getTaskPlayer().getPlayerInfo().getPlayer();
				if (player.getOpenInventory().getTopInventory() == taskInv.getInventory()) {
					player.closeInventory();
				}
			}
		}.runTaskLater(Main.getPlugin(), 15L);
		this.taskPlayer.taskDone();
		return true;
	}

	public void handleWireLeftClick(String clickedColor) {
		if (this.wiresStates.get(0).get(clickedColor)) {
			return;
		}
		this.activeWire = clickedColor;
		Main.getSoundsManager().playSound("taskWiringClick", this.taskPlayer.getPlayerInfo().getPlayer(), this.taskPlayer.getPlayerInfo().getPlayer().getLocation());
		this.update();
	}

	public void handleWireRightClick(String clickedColor) {
		if (this.activeWire.isEmpty()) {
			return;
		}
		if (!this.activeWire.equals(clickedColor)) {
			Main.getSoundsManager().playSound("taskWiringDisconnect", this.taskPlayer.getPlayerInfo().getPlayer(), this.taskPlayer.getPlayerInfo().getPlayer().getLocation());
			this.activeWire = "";
			this.update();
			return;
		}

		this.wiresStates.get(0).put(clickedColor, true);
		this.wiresStates.get(1).put(clickedColor, true);
		this.activeWire = "";
		Main.getSoundsManager().playSound("taskWiringConnect", this.taskPlayer.getPlayerInfo().getPlayer(), this.taskPlayer.getPlayerInfo().getPlayer().getLocation());

		this.update();
		this.checkDone();
	}

	public static ArrayList<String> generateWires() {
		ArrayList<String> out = new ArrayList<String>(wiresRightSlots.keySet());
		Collections.shuffle(out);
		return out;
	}

	@Override
	public void update() {
		TaskWiringInv inv = this;
		this.inv.setItem(8, Main.getItemsManager().getItem("wiring_info").getItem().getItem());

		Integer sideSlot = 10;
		for (String colorStr : this.taskPlayer.getWires_()) {
			ItemInfoContainer wireItemInfo = Main.getItemsManager().getItem("wiring_" + colorStr);
			ItemStack item = wireItemInfo.getItem().getItem();
			if (this.activeWire.equals(colorStr)) {
				Utils.enchantedItem(item, Enchantment.DURABILITY, 1);
			}
			Icon icon = new Icon(item);
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleWireLeftClick(colorStr);
				}
			});
			this.setIcon(sideSlot, icon);

			ItemStack middleItem = wireItemInfo.getItem2().getItem();

			Integer rightColorSlot = wiresRightSlots.get(colorStr);
			Integer slot_ = sideSlot;
			if (this.wiresStates.get(0).get(colorStr)) {
				// middle cables
				Boolean isOk = false;
				int i = 0;
				while (!isOk) {
					Integer nextMove = 1;

					if (slot_ > rightColorSlot) {
						// this means the right color is above
						nextMove = 0;
					} else if (slot_ < rightColorSlot) {
						// this means the right is either same in the same line or below
						if (rightColorSlot - slot_ <= 9) {
							// same line
							nextMove = 1;
						} else {
							// below
							nextMove = 2;
						}
					}

					if (nextMove == 0) {
						// up
						slot_ -= 8;
					} else if (nextMove == 1) {
						// right
						slot_ += 1;
					} else if (nextMove == 2) {
						// down
						slot_ += 10;
					}

					this.inv.setItem(slot_, middleItem);

					if (slot_ == 15 || slot_ == 24 || slot_ == 33 || slot_ == 42) {
						isOk = true;
						break;
					}
					if (i > 5) {
						break;
					}
					i++;
				}
			}

			sideSlot += 9;
		}
	}

	@Override
	public void invClosed() {
	}

}