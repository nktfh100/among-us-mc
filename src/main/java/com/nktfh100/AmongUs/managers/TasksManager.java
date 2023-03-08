package com.nktfh100.AmongUs.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.nktfh100.AmongUs.enums.SabotageType;
import com.nktfh100.AmongUs.enums.TaskLength;
import com.nktfh100.AmongUs.info.Arena;
import com.nktfh100.AmongUs.info.PlayerInfo;
import com.nktfh100.AmongUs.info.QueuedTasksVariant;
import com.nktfh100.AmongUs.info.Task;
import com.nktfh100.AmongUs.info.TaskPlayer;
import com.nktfh100.AmongUs.inventory.tasks.TaskAcceptDivertedPowerInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskCalibrateDistributorInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskChartCourseInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskCleanO2Inv;
import com.nktfh100.AmongUs.inventory.tasks.TaskClearAsteroidsInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskDataInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskDivertPowerInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskEmptyGarbageInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskFillCanistersInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskFixWeatherNodeInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskFuelInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskInsertKeysInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskInspectSampleInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskMonitorTreeInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskOpenWaterwaysInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskPrimeShieldsInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskRebootWifiInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskRecordTemperatureInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskRefuelInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskRepairDrillInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskReplaceWaterJug;
import com.nktfh100.AmongUs.inventory.tasks.TaskScanBoardingPassInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskScanInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskStabilizeSteeringInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskStartReactorInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskStoreArtifactsInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskSwipeCardInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskSwitchWeatherNodeInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskUnlockManifoldsInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskWiringInv;
import com.nktfh100.AmongUs.main.Main;

public class TasksManager {

	private Arena arena;
	// player_uuid - active tasks
	private HashMap<String, ArrayList<TaskPlayer>> tasks = new HashMap<String, ArrayList<TaskPlayer>>();

	public TasksManager(Arena arena) {
		this.arena = arena;
	}

	public void giveTasks() {
		for (PlayerInfo pInfo : arena.getPlayersInfo()) {
			ArrayList<TaskPlayer> playerTasks = new ArrayList<TaskPlayer>();
			for (TaskLength tl : TaskLength.values()) {
				ArrayList<String> allTasksOfLength = arena.getTasksLength(tl);
				Collections.shuffle(allTasksOfLength);
				Integer numOfTasksToAdd = arena.getTasksNum(tl);
				for (int i = 0; i < allTasksOfLength.size(); i++) {
					if (numOfTasksToAdd <= 0) {
						break;
					}

					Task taskSelected = arena.getTask(allTasksOfLength.get(i));
					if (!taskSelected.getIsEnabled()) {
						continue;
					}
					ArrayList<Task> tasksQueued = new ArrayList<Task>(Arrays.asList(taskSelected));
					QueuedTasksVariant qtv = taskSelected.getRandomTaskVariant();
					if (qtv != null) {
						for (Task t : qtv.getQueuedTasksTasks()) {
							tasksQueued.add(t);
						}
					}

					if (isAddingTaskOk(playerTasks, tasksQueued)) {
						TaskPlayer tp = new TaskPlayer(pInfo, tasksQueued, qtv == null ? -1 : qtv.getId());
						playerTasks.add(tp);
						tp.getActiveTask().getHolo().showTo(pInfo.getPlayer());
						numOfTasksToAdd--;
					}
				}
			}
			this.tasks.put(pInfo.getPlayer().getUniqueId().toString(), playerTasks);
		}
	}

	public Boolean isAddingTaskOk(ArrayList<TaskPlayer> tasks_, ArrayList<Task> newTasks) {
		for (TaskPlayer oldTP : tasks_) {
			for (Task newT : newTasks) {
				for (Task oldT : oldTP.getTasks()) {
					if (oldT.getId().equals(newT.getId())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public void taskHoloClick(Player player, Task taskClicked) {
		PlayerInfo pInfo = Main.getPlayersManager().getPlayerInfo(player);
		if (pInfo.getIsImposter() || pInfo.getIsInCameras() || pInfo.getArena().getIsInMeeting()) {
			return;
		}

		TaskPlayer taskPlayer = null;
		for (TaskPlayer tp : this.getTasksForPlayer(player)) {
			if (tp.getActiveTask().getId().equals(taskClicked.getId())) {
				taskPlayer = tp;
				break;
			}
		}
		if (taskPlayer == null) {
			return;
		}
		if (taskPlayer.getIsDone()) {
			for (Task t : taskPlayer.getTasks()) {
				t.getHolo().hideTo(player);
			}
			return;
		}

		Main.getSoundsManager().playSound("taskInvOpen", player, player.getLocation());

		switch (taskPlayer.getActiveTask().getTaskType()) {
		case WIRING:
			player.openInventory(new TaskWiringInv(this.arena, taskPlayer).getInventory());
			return;
		case DOWNLOAD_DATA:
		case UPLOAD_DATA:
			player.openInventory(new TaskDataInv(this.arena, taskPlayer).getInventory());
			return;
		case UNLOCK_MANIFOLDS:
			player.openInventory(new TaskUnlockManifoldsInv(this.arena, taskPlayer, taskPlayer.getNumbers_()).getInventory());
			return;
		case DIVERT_POWER:
			player.openInventory(new TaskDivertPowerInv(this.arena, taskPlayer, taskPlayer.getLocations_(), taskPlayer.getActiveLocation_(), taskPlayer.getActiveLever_()).getInventory());
			return;
		case ACCEPT_DIVERTED_POWER:
			player.openInventory(new TaskAcceptDivertedPowerInv(this.arena, taskPlayer).getInventory());
			return;
		case PRIME_SHIELDS:
			player.openInventory(new TaskPrimeShieldsInv(this.arena, taskPlayer, taskPlayer.getSquares_()).getInventory());
			return;
		case CALIBRATE_DISTRIBUTOR:
			player.openInventory(new TaskCalibrateDistributorInv(this.arena, taskPlayer).getInventory());
			return;
		case EMPTY_GARBAGE:
			player.openInventory(new TaskEmptyGarbageInv(this.arena, taskPlayer).getInventory());
			return;
		case CLEAN_O2:
			player.openInventory(new TaskCleanO2Inv(this.arena, taskPlayer, taskPlayer.getLeaves_()).getInventory());
			return;
		case REFUEL:
			player.openInventory(new TaskRefuelInv(this.arena, taskPlayer, taskPlayer.getFuelProgress_()).getInventory());
			return;
		case FUEL:
			player.openInventory(new TaskFuelInv(this.arena, taskPlayer, taskPlayer.getFuelProgress_()).getInventory());
			return;
		case INSPECT_SAMPLE:
			player.openInventory(new TaskInspectSampleInv(this.arena, taskPlayer).getInventory());
			return;
		case START_REACTOR:
			player.openInventory(new TaskStartReactorInv(this.arena, taskPlayer, taskPlayer.getMoves_()).getInventory());
			return;
		case SCAN:
			player.openInventory(new TaskScanInv(this.arena, taskPlayer).getInventory());
			return;
		case CLEAR_ASTEROIDS:
			player.openInventory(new TaskClearAsteroidsInv(this.arena, taskPlayer).getInventory());
			return;
		case SWIPE_CARD:
			player.openInventory(new TaskSwipeCardInv(this.arena, taskPlayer).getInventory());
			return;
		case CHART_COURSE:
			player.openInventory(new TaskChartCourseInv(this.arena, taskPlayer).getInventory());
			return;
		case STABILIZE_STEERING:
			player.openInventory(new TaskStabilizeSteeringInv(this.arena, taskPlayer).getInventory());
			return;
		case FILL_CANISTERS:
			player.openInventory(new TaskFillCanistersInv(this.arena, taskPlayer).getInventory());
			return;
		case INSERT_KEYS:
			player.openInventory(new TaskInsertKeysInv(this.arena, taskPlayer).getInventory());
			return;
		case REPLACE_WATER_JUG:
			player.openInventory(new TaskReplaceWaterJug(this.arena, taskPlayer).getInventory());
			return;
		case RECORD_TEMPERATURE:
			player.openInventory(new TaskRecordTemperatureInv(this.arena, taskPlayer, taskPlayer.getActiveTask().getIsHot()).getInventory());
			return;
		case REPAIR_DRILL:
			player.openInventory(new TaskRepairDrillInv(this.arena, taskPlayer, taskPlayer.getActiveTask().getIsHot()).getInventory());
			return;
		case MONITOR_TREE:
			player.openInventory(new TaskMonitorTreeInv(this.arena, taskPlayer).getInventory());
			return;
		case OPEN_WATERWAYS:
			player.openInventory(new TaskOpenWaterwaysInv(this.arena, taskPlayer).getInventory());
			return;
		case REBOOT_WIFI:
			player.openInventory(new TaskRebootWifiInv(this.arena, taskPlayer).getInventory());
			return;
		case FIX_WEATHER_NODE:
			player.openInventory(new TaskFixWeatherNodeInv(this.arena, taskPlayer).getInventory());
			return;
		case SWITCH_WEATHER_NODE:
			player.openInventory(new TaskSwitchWeatherNodeInv(this.arena, taskPlayer).getInventory());
			return;
		case SCAN_BOARDING_PASS:
			player.openInventory(new TaskScanBoardingPassInv(this.arena, taskPlayer).getInventory());
			return;
		case STORE_ARTIFACTS:
			player.openInventory(new TaskStoreArtifactsInv(this.arena, taskPlayer).getInventory());
			return;
		default:
			break;
		}

		taskPlayer.taskDone();
		this.updateTasksDoneBar(true);
	}

	public void updateTasksDoneBar(Boolean canWin) {
		Integer totalTasks = 0;
		Integer tasksDone = 0;

		for (String tasks_key : this.tasks.keySet()) {
			PlayerInfo pInfo = Main.getPlayersManager().getPlayerByUUID(tasks_key);
			if (pInfo == null || pInfo.getIsImposter()) {
				continue;
			}
			ArrayList<TaskPlayer> tasks_ = this.tasks.get(tasks_key);
			for (TaskPlayer tp : tasks_) {
				if (tp.getIsDone()) {
					tasksDone++;
				}
			}
			totalTasks += tasks_.size();
		}
		double progress = (double) tasksDone / (double) totalTasks;

		if (progress >= 0 && progress <= 1) {
			if (this.arena.getSabotageManager().getIsSabotageActive() && this.arena.getSabotageManager().getActiveSabotage().getType() == SabotageType.COMMUNICATIONS) {
				this.arena.getTasksBossBar().setProgress(0);
			} else {
				this.arena.getTasksBossBar().setProgress(progress);
			}
		}
		if (progress >= 1 && canWin) {
			this.arena.gameWin(false);
		}
	}

	public void removeTasksForPlayer(Player p) {
		if (this.tasks.get(p.getUniqueId().toString()) != null) {
			this.tasks.get(p.getUniqueId().toString()).clear();
		}
	}

	public ArrayList<TaskPlayer> getTasksForPlayer(Player p) {
		ArrayList<TaskPlayer> tp_ = tasks.get(p.getUniqueId().toString());
		if (tp_ == null) {
			tp_ = new ArrayList<TaskPlayer>();
		}
		return tp_;
	}

	public Collection<ArrayList<TaskPlayer>> getAllTasks() {
		return this.tasks.values();
	}

	public void delete() {
		for (ArrayList<TaskPlayer> tasks_ : this.tasks.values()) {
			for (TaskPlayer tp : tasks_) {
				tp.delete();
			}
		}
		this.tasks = null;
	}

}
