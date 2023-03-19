package com.nktfh100.AmongUs.inventory.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.ItemInfo;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.ClickAction;
import com.nktfh100.AmongUs.inventory.Icon;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskClearAsteroidsInv extends TaskInvHolder {

	private final static ArrayList<Integer> slots = new ArrayList<Integer>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
	private final static ArrayList<Integer> spawnSlots = new ArrayList<Integer>(Arrays.asList(29, 30, 31, 32, 33, 34, 38, 39, 40, 41, 42, 43, 25, 24));
	private Boolean isDone = false;
	private HashMap<Integer, Integer> asteroids = new HashMap<Integer, Integer>();
	private BukkitTask runnable = null;
	private DustOptions dust = new DustOptions(Main.getConfigManager().getAsteroidsParticleColor(), 1.5F);

	public TaskClearAsteroidsInv(Arena arena, TaskPlayer taskPlayer) {
		super(54, Main.getMessagesManager().getGameMsg("taskInvTitle", arena, Utils.getTaskPlaceholders(taskPlayer)), arena, taskPlayer);
		Utils.fillInv(this.inv);
		this.inv.setItem(8, Main.getItemsManager().getItem("clearAsteroids_info").getItem().getItem());
		TaskClearAsteroidsInv inv = this;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (inv.getIsDone() || inv.getPlayerInfo() == null || !inv.getPlayerInfo().getIsIngame()) {
					this.cancel();
					return;
				}
				inv.tick();
			}
		}.runTaskTimer(Main.getPlugin(), 5L, 16L);
		this.update();
	}

	public void handleClick(Player player, Integer id) {
		if (this.isDone) {
			return;
		}
		Main.getSoundsManager().playSound("taskClearAsteroidsClick", player, player.getLocation());
		if (id != null && this.asteroids.get(id) != null) {
			this.asteroids.remove(id);
			this.taskPlayer.setAsteroidsDestroyed_(this.taskPlayer.getAsteroidsDestroyed_() + 1);
			Main.getSoundsManager().playSound("taskClearAsteroidsDestroy", player, player.getLocation());
		}
		if (this.arena.getEnableVisualTasks() && this.taskPlayer.getActiveTask().getEnableVisuals() && !pInfo.isGhost()) {
			long finish = System.currentTimeMillis();
			long timeElapsed = finish - this.taskPlayer.getActiveTask().getAsteroidsLastTime();
			if (timeElapsed > 800) {
				this.taskPlayer.getActiveTask().setAsteroidsLastTime(System.currentTimeMillis());
				this.playVisuals();
			}
		}
		if (this.taskPlayer.getAsteroidsDestroyed_() >= 20) {
			this.isDone = true;
			this.checkDone();
		}
		this.update();
	}

	public void tick() {

		// Move asteroids
		Iterator<Entry<Integer, Integer>> iter = this.asteroids.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			Integer slot = entry.getValue();
			if (slot <= 16) {
				iter.remove();
			} else if (slot != 10 && slot != 19 && slot != 28 && slot != 37) {
				if (!this.asteroids.values().contains(slot - 10)) {
					this.asteroids.put(entry.getKey(), slot - 10);
				}
			} else {
				iter.remove();
			}
		}

		// Create new asteroids
		if (this.asteroids.size() <= 6 && (Math.random() >= 0.5 || this.asteroids.size() <= 2)) {
			for (int i = 0; i < 15; i++) {
				Integer slot = spawnSlots.get(Utils.getRandomNumberInRange(0, spawnSlots.size() - 1));
				if (!this.asteroids.values().contains(slot)) {
					this.asteroids.put((int) (Math.random() * 9999), slot);
					break;
				}
			}
		}

		this.update();
	}

	public void playVisuals() {
		Location canonnLoc = this.taskPlayer.getActiveTask().getActiveCannon() == 0 ? this.taskPlayer.getActiveTask().getCannon1().clone() : this.taskPlayer.getActiveTask().getCannon2().clone();
		if (canonnLoc == null || canonnLoc.getWorld() == null) {
			return;
		}
		Vector vector = canonnLoc.getDirection().multiply(1);

		canonnLoc.getWorld().spawnParticle(Particle.BLOCK_CRACK, canonnLoc.getX(), canonnLoc.getY(), canonnLoc.getZ(), 30, 0.3D, 0.3D, 0.3D, Main.getConfigManager().getAsteroidsParticleMaterial());

		if (this.taskPlayer.getActiveTask().getCannon2() != null) {
			this.taskPlayer.getActiveTask().setActiveCannon(this.taskPlayer.getActiveTask().getActiveCannon() == 0 ? 1 : 0);
		}

		new BukkitRunnable() {
			Double progress = 0D;

			@Override
			public void run() {
				if (progress > 20) {
					this.cancel();
					return;
				}
				for (int i = 0; i < 5; i++) {
					canonnLoc.getWorld().spawnParticle(Particle.REDSTONE, canonnLoc.getX(), canonnLoc.getY(), canonnLoc.getZ(), 5, 0.1D, 0.1D, 0.1D, dust);
					canonnLoc.add(vector);
					progress += 1;
				}
			}
		}.runTaskTimer(Main.getPlugin(), 0L, 1L);
	}

	@Override
	public Boolean checkDone() {
		if (this.isDone) {
			this.taskPlayer.taskDone();
			TaskClearAsteroidsInv taskInv = this;
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
		TaskClearAsteroidsInv inv = this;
		ItemInfo asteroidItem = Main.getItemsManager().getItem("clearAsteroids_asteroid").getItem();
		ItemStack asteroidItemS = asteroidItem.getItem();
		ItemInfo backgroundItem = Main.getItemsManager().getItem("clearAsteroids_background").getItem();
		ItemStack backgroundItemS = backgroundItem.getItem();

		for (Integer slot : slots) {
			Icon icon = new Icon(backgroundItemS);
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleClick(player, null);
				}
			});
			this.setIcon(slot, icon);
		}

		for (Integer id : this.asteroids.keySet()) {
			Icon icon = new Icon(asteroidItemS);
			icon.addClickAction(new ClickAction() {
				@Override
				public void execute(Player player) {
					inv.handleClick(player, id);
				}
			});
			this.setIcon(this.asteroids.get(id), icon);
		}

		ItemInfo infoItem = Main.getItemsManager().getItem("clearAsteroids_destroyed").getItem();
		ItemStack infoItemS = infoItem.getItem(this.taskPlayer.getAsteroidsDestroyed_() + "", null);
		infoItemS.setAmount(this.taskPlayer.getAsteroidsDestroyed_() > 0 ? this.taskPlayer.getAsteroidsDestroyed_() : 1);
		this.setIcon(49, new Icon(infoItemS));
	}

	@Override
	public void invClosed() {
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
	}

	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}
}