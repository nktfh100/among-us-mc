package com.nktfh100.AmongUs.info;

import java.util.ArrayList;
import java.util.Arrays;

import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.filoghost.holographicdisplays.api.hologram.Hologram;
import com.nktfh100.AmongUs.enums.SabotageLength;
import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.main.Main;

public class SabotageArena {

	private Arena arena;
	private SabotageLength length;
	private Integer timer;
	private Boolean hasTimer;
	private SabotageTask task1;
	private SabotageTask task2 = null;

	// for SabotageLength.DOUBLE
	private ArrayList<Boolean> isTaskDone = new ArrayList<Boolean>();
	// for SabotageLength.DOUBLE_SAME_TIME
	private ArrayList<Boolean> isTaskActive = new ArrayList<Boolean>();
	private ArrayList<ArrayList<Player>> playersActive = new ArrayList<ArrayList<Player>>();
	private BukkitTask runnable = null;

	public SabotageArena(Arena arena, SabotageTask task1, SabotageTask task2) {
		this.arena = arena;
		this.task1 = task1;
		this.length = task1.getSabotageType().getSabotageLength();
		this.timer = task1.getTimer();
		this.hasTimer = task1.getHasTimer();
		if (this.length != SabotageLength.SINGLE) {
			this.task2 = task2;
		}
		if (this.length == SabotageLength.DOUBLE_SAME_TIME) {
			this.isTaskActive = new ArrayList<Boolean>(Arrays.asList(false, false));
			this.playersActive = new ArrayList<ArrayList<Player>>(Arrays.asList(new ArrayList<Player>(), new ArrayList<Player>()));
		} else if (this.length == SabotageLength.DOUBLE) {
			this.isTaskDone.add(false);
			this.isTaskDone.add(false);
		}
	}

	public void taskDone(Player player) {
		if (this.length == SabotageLength.SINGLE) {
			this.hideHolo(0);
		} else {
			this.hideHolo(0);
			this.hideHolo(1);
			this.isTaskActive.set(0, false);
			this.isTaskActive.set(1, false);
			this.playersActive.set(0, new ArrayList<Player>());
			this.playersActive.set(1, new ArrayList<Player>());
		}
		this.arena.getSabotageManager().endSabotage(true, false, player);
		this.arena.updateScoreBoard();
		this.arena.getSabotageManager().updateBossBar();
	}

	public void taskDone(Integer id, Player player) { // for SabotageLength.DOUBLE
		if (this.length == SabotageLength.DOUBLE) {
			this.isTaskDone.set(id, true);
			this.hideHolo(id);
			if (this.isTaskDone.get(0) && this.isTaskDone.get(1)) {
				this.arena.getSabotageManager().endSabotage(true, false, player);
				this.isTaskDone.set(0, false);
				this.isTaskDone.set(0, false);
				this.hideHolo(0);
				this.hideHolo(1);
			}
		}
		this.arena.updateScoreBoard();
		this.arena.getSabotageManager().updateBossBar();
	}

	public void hideHolo(Integer id) {
		if (id == 0) {
			Hologram task1Holo = this.task1.getHolo();
			task1Holo.getVisibilitySettings().clearIndividualVisibilities();
			task1Holo.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
		} else if (this.task2 != null) {
			Hologram task2Holo = this.task2.getHolo();
			task2Holo.getVisibilitySettings().clearIndividualVisibilities();
			task2Holo.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
		}
	}

	public void showHolos() {
		Hologram task1Holo = this.task1.getHolo();
		task1Holo.getVisibilitySettings().clearIndividualVisibilities();
		task1Holo.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);

		if (this.task2 != null) {
			Hologram task2Holo = this.task2.getHolo();
			task2Holo.getVisibilitySettings().clearIndividualVisibilities();
			task2Holo.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
		}
	}

	public Boolean getTaskDone(Integer i) {
		return this.isTaskDone.get(i);
	}

	public Boolean getTaskActive(Integer i) {
		return this.isTaskActive.get(i);
	}

	private void setTaskActive(Integer i, Boolean is) {
		this.isTaskActive.set(i, is);
	}

	public void addPlayerActive(Player player, Integer i) {
		if(!this.playersActive.get(i).contains(player) ) {
			this.playersActive.get(i).add(player);			
		}
		this.setTaskActive(i, true);
		SabotageArena saboArena = this;
		if (this.getTaskActive(0) && this.getTaskActive(1)) {
			this.arena.getSabotageManager().setIsTimerPaused(true);
			this.runnable = new BukkitRunnable() {
				@Override
				public void run() {
					if (saboArena.getTaskActive(0) && saboArena.getTaskActive(1)) {
						saboArena.taskDone(player);
					}
				}
			}.runTaskLater(Main.getPlugin(), 30L);
		}
		this.arena.getSabotageManager().updateBossBar();
	}

	public void removePlayerActive(Player player, Integer i) {
		this.playersActive.get(i).remove(player);
		if (this.playersActive.get(i).size() == 0) {
			this.setTaskActive(i, false);
			if (this.runnable != null) {
				this.runnable.cancel();
				this.arena.getSabotageManager().setIsTimerPaused(false);
			}
		}
		this.arena.getSabotageManager().updateBossBar();
	}

	public ArrayList<Hologram> getHolos() {
		ArrayList<Hologram> holos = new ArrayList<Hologram>();
		holos.add(this.task1.getHolo());
		if (this.task2 != null) {
			holos.add(this.task2.getHolo());
		}
		return holos;
	}

	public SabotageType getType() {
		return this.getTask1().getSabotageType();
	}

	public SabotageLength getLength() {
		return this.length;
	}

	public SabotageTask getTask1() {
		return this.task1;
	}

	public SabotageTask getTask2() {
		return this.task2;
	}

	public Arena getArena() {
		return this.arena;
	}

	public Integer getTimer() {
		return timer;
	}

	public Boolean getHasTimer() {
		return hasTimer;
	}
}
