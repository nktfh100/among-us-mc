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

public class TaskInsertKeysInv extends TaskInvHolder {

	private static final ArrayList<Integer> keysSlots = new ArrayList<Integer>(Arrays.asList(11, 12, 13, 14, 15, 20, 21, 22, 23, 24));

	private Boolean isDone = false;

	public TaskInsertKeysInv(Arena arena, TaskPlayer taskPlayer) {
		super(36, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);

		this.update();
	}

	public void keyClick(Integer i) {
		if (this.isDone) {
			return;
		}
		if (i == this.getPlayerInfo().getJoinedId()) {
			this.isDone = true;
			Main.getSoundsManager().playSound("taskInsertKeysClick", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		}
		this.update();
		this.checkDone();
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskInsertKeysInv inv = this;
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
		TaskInsertKeysInv inv = this;

		this.inv.setItem(8, Main.getItemsManager().getItem("insertKeys_info").getItem().getItem());

		ItemInfoContainer keyItem = Main.getItemsManager().getItem("insertKeys_key");
		ItemStack keyItemS = keyItem.getItem().getItem();

		Integer i = 0;
		for (Integer slot : keysSlots) {
			Boolean isPlayerKey = i == this.getPlayerInfo().getJoinedId();
			ItemStack item_ = isPlayerKey ? keyItem.getItem2().getItem() : keyItemS;
			if (isPlayerKey && this.isDone) {
				item_ = Utils.enchantedItem(item_, Enchantment.DURABILITY, 1);
			}
			Icon icon = new Icon(item_);
			if (isPlayerKey) {
				final Integer i_ = i;
				icon.addClickAction(new ClickAction() {
					@Override
					public void execute(Player player) {
						inv.keyClick(i_);
					}
				});
			}
			this.setIcon(slot, icon);
			i++;
		}

	}

	@Override
	public void invClosed() {
	}
}