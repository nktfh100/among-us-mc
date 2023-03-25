package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.events.PacketContainer;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfoContainer;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Packets;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskEmptyGarbageInv extends TaskInvHolder {

	private static ArrayList<ArrayList<Integer>> slots = new ArrayList<ArrayList<Integer>>();

	static {
		slots.add(new ArrayList<Integer>(Arrays.asList(37, 38, 39, 40, 41)));
		slots.add(new ArrayList<Integer>(Arrays.asList(28, 29, 30, 31, 32)));
		slots.add(new ArrayList<Integer>(Arrays.asList(19, 20, 21, 22, 23)));
		slots.add(new ArrayList<Integer>(Arrays.asList(10, 11, 12, 13, 14)));
	}

	private Boolean isDone = false;
	private ArrayList<Integer> randomRow = this.generateRandomRow();
	private Integer topRow = 3;
	private Boolean isRunning = false;
	private BukkitTask runnable = null;

	public TaskEmptyGarbageInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer), taskPlayer.getPlayerInfo().getPlayer()), arena, taskPlayer);
		Utils.fillInv(this.inv);
		TaskEmptyGarbageInv inv = this;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (inv.getIsDone()) {
					this.cancel();
					return;
				}
				if (inv.getIsRunning()) {
					inv.tick();
				}
			}
		}.runTaskTimer(Main.getPlugin(), 10L, 22L);
		this.update();
	}

	private ArrayList<Integer> generateRandomRow() {
		ArrayList<Integer> out = new ArrayList<Integer>();
		Integer offNum = Utils.getRandomNumberInRange(1, 4);
		for (int i = 0; i < 5; i++) {
			if (offNum > 0) {
				out.add(1);
				offNum--;
			} else {
				out.add(0);
			}
		}
		Collections.shuffle(out);
		return out;
	}

	public void handleClick(Player player) {
		if (this.isDone) {
			return;
		}
		Main.getSoundsManager().playSound("taskEmptyGarbageLeverClick", player, player.getLocation());
		this.isRunning = !this.isRunning;

		this.checkDone();
		this.update();
	}

	public void tick() {
		this.topRow--;
		if (this.topRow < 0) {
			this.isDone = true;
		}
		this.randomRow = this.generateRandomRow();
		this.checkDone();
		this.update();
	}

	public void playVisuals() {
		PacketContainer packet = Packets.PARTICLES(this.pInfo.getPlayer().getLocation().add(0, 1.3, 0), Particle.BLOCK_CRACK, Bukkit.createBlockData(Material.PODZOL), 60, 0.5F, 0.5F, 0.5F);
		Packets.sendPacket(this.pInfo.getPlayer(), packet);
		for (PlayerInfo pInfo_ : arena.getPlayersInfo()) {
			if (pInfo != pInfo_) {
				if (this.arena.getEnableReducedVision()) {
					if (pInfo_.isGhost() || !pInfo_.getPlayersHidden().contains(this.pInfo.getPlayer())) {
						Packets.sendPacket(pInfo_.getPlayer(), packet);
					}
				} else {
					Packets.sendPacket(pInfo_.getPlayer(), packet);
				}
			}
		}
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			if (this.arena.getEnableVisualTasks() && this.taskPlayer.getActiveTask().getEnableVisuals() && !pInfo.isGhost()) {
				this.playVisuals();
			}
			this.taskPlayer.taskDone();
			TaskEmptyGarbageInv taskInv = this;
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

		this.inv.setItem(8, Main.getItemsManager().getItem("emptyGarbage_info").getItem().getItem());

		TaskEmptyGarbageInv inv = this;
		ItemInfoContainer garbageItem = Main.getItemsManager().getItem("emptyGarbage_garbage");
		ItemStack garbageItemS = garbageItem.getItem().getItem();
		ItemStack garbageItemS2 = garbageItem.getItem2().getItem();

		for (int i = 0; i < slots.size(); i++) {
			for (Integer slot : slots.get(i)) {
				if (i <= this.topRow) {
					this.inv.setItem(slot, garbageItemS);
				} else {
					this.inv.setItem(slot, garbageItemS2);
				}
			}
		}
		if (this.topRow < 3) {
			int randomI = 0;
			for (Integer slot : slots.get(this.topRow + 1)) {
				if (this.randomRow.get(randomI) == 0) {
					this.inv.setItem(slot, garbageItemS);
				} else {
					this.inv.setItem(slot, garbageItemS2);
				}
				randomI++;
			}
		}

		ItemInfoContainer leverItem = Main.getItemsManager().getItem("emptyGarbage_lever");
		Icon icon = new Icon(this.isRunning ? leverItem.getItem2().getItem() : leverItem.getItem().getItem());
		icon.addClickAction(new ClickAction() {
			@Override
			public void execute(Player player) {
				inv.handleClick(player);
			}
		});
		this.setIcon(25, icon);

		ItemInfoContainer leverTopItem = Main.getItemsManager().getItem("emptyGarbage_leverTop");
		icon = new Icon(this.isRunning ? leverTopItem.getItem2().getItem() : leverTopItem.getItem().getItem());
		this.setIcon(16, icon);

		ItemInfoContainer leverBottomItem = Main.getItemsManager().getItem("emptyGarbage_leverBottom");
		icon = new Icon(this.isRunning ? leverBottomItem.getItem().getItem() : leverBottomItem.getItem2().getItem());
		this.setIcon(34, icon);
	}

	@Override
	public void invClosed() {
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
	}

	public Boolean getIsRunning() {
		return this.isRunning;
	}

	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}
}