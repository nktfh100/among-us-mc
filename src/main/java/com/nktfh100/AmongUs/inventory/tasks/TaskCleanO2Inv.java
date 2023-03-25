package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;

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

public class TaskCleanO2Inv extends TaskInvHolder {

	private static ArrayList<Integer> leavesSlots = new ArrayList<Integer>(Arrays.asList(10, 39, 28, 37, 11, 12, 13, 14, 15, 19, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42, 16, 25, 34, 43));

	private ArrayList<Integer> leaves = new ArrayList<Integer>();

	public TaskCleanO2Inv(Arena arena, TaskPlayer taskPlayer, ArrayList<Integer> leaves_) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.leaves = leaves_;
		this.update();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Integer> generateLeaves() {
		ArrayList<Integer> out = new ArrayList<Integer>();
		ArrayList<Integer> slotavailable = (ArrayList<Integer>) leavesSlots.clone();
		for (int i = 0; i < Utils.getRandomNumberInRange(9, 12); i++) {
			Integer id = Utils.getRandomNumberInRange(0, slotavailable.size() - 1);
			if (!out.contains(id)) {
				out.add(id);
				slotavailable.remove((int) id);
			}

		}
		return out;
	}

	public void leafClick(Player player, Integer id) {

		Main.getSoundsManager().playSound("taskCleanO2LeafClick", player, player.getLocation());

		this.leaves.removeIf(s -> s == id);

		this.checkDone();
		this.update();
	}

	@Override
	public Boolean checkDone() {
		if (this.leaves.size() > 0) {
			return false;
		}
		this.taskPlayer.taskDone();
		TaskCleanO2Inv inv = this;
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

	@Override
	public void update() {

		this.inv.setItem(8, Main.getItemsManager().getItem("cleanO2_info").getItem().getItem());

		ItemInfoContainer leafItem = Main.getItemsManager().getItem("cleanO2_leaf");
		ItemStack leafItemS = leafItem.getItem().getItem();
		ItemStack leafItem2S = leafItem.getItem2().getItem();

		TaskCleanO2Inv inv = this;

		for (Integer slot : leavesSlots) {
			this.setIcon(slot, new Icon(leafItem2S));
		}

		for (int i = 0; i < this.leaves.size(); i++) {
			Integer slot = leavesSlots.get(this.leaves.get(i));
			Icon icon = new Icon(leafItemS);
			final Integer id_ = this.leaves.get(i);
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.leafClick(player, id_);
				}
			});
			this.setIcon(slot, icon);
		}
	}

	@Override
	public void invClosed() {
	}

}