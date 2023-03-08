package com.nktfh100.AmongUs.info;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import com.nktfh100.AmongUs.enums.StatInt;
import com.nktfh100.AmongUs.inventory.tasks.TaskCleanO2Inv;
import com.nktfh100.AmongUs.inventory.tasks.TaskDivertPowerInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskFixWeatherNodeInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskPrimeShieldsInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskStartReactorInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskUnlockManifoldsInv;
import com.nktfh100.AmongUs.inventory.tasks.TaskWiringInv;
import com.nktfh100.AmongUs.main.Main;
import com.nktfh100.AmongUs.managers.MessagesManager;
import com.nktfh100.AmongUs.utils.Utils;

public class TaskPlayer {

	private PlayerInfo pInfo;
	private ArrayList<Task> tasks = new ArrayList<Task>();
	private Integer state = 0;
	private Integer selectedQueuedVariant;
	// for wires
	private ArrayList<String> wires_ = null;
	// for divert power
	private String activeLocation_ = null;
	private ArrayList<String> locations_ = null;
	private Integer activeLever_ = null;
	// for unlock manifolds
	private ArrayList<Integer> numbers_ = null;
	// for prime shields
	private ArrayList<Boolean> squares_ = null;
	// for clean o2
	private ArrayList<Integer> leaves_ = null;
	// for refuel / fuel
	private Integer fuelProgress_ = null;
	// for inspect sample
	private Integer inspectTimer_ = null;
	private Boolean inspectIsRunning_ = null;
	private Integer inspectAnomaly_ = null;
	// for start reactor
	private ArrayList<Integer> moves_ = null;
	private Integer reactorState_ = null;
	// for asteroids
	private Integer asteroidsDestroyed_ = null;
	// for fill canisters
	private Integer canistersLeft_ = null;
	// for replace water jug
	private Integer waterJugProgress_ = null;
	// for reboot wifi
	private Integer rebootTimer_ = null;
	private Boolean rebootIsRunning_ = null;
	// for fix weather node
	private ArrayList<Integer> maze_ = null;

	public TaskPlayer(PlayerInfo pInfo, ArrayList<Task> tasksQueued, Integer selectedQueuedVariant) {
		this.state = 0;
		this.pInfo = pInfo;
		this.tasks = tasksQueued;
		this.selectedQueuedVariant = selectedQueuedVariant;
		this.updateTasksVars();
	}

	public String getName() {
		MessagesManager msgsManager = Main.getMessagesManager();
		ChatColor color = this.getColor();
		Task activeTask = this.tasks.get(this.state);
		String body = "";
		String extraString = "";
		if (this.tasks.size() > 1) {
			extraString = " (" + this.state + "/" + this.tasks.size() + ")";
		}
		body = activeTask.getLocationName() + ": " + msgsManager.getTaskName(activeTask.getTaskType().toString()) + extraString;

		return color + body;
	}

	public ChatColor getColor() {
		ChatColor color = ChatColor.RED;
		if (this.state == this.tasks.size()) {
			color = ChatColor.GREEN;
		} else if (this.tasks.size() > 1 && this.state > 0) {
			color = ChatColor.YELLOW;
		}
		return color;
	}

	public void taskDone() {
		if (this.state >= this.tasks.size() || this.pInfo.getIsImposter()) {
			return;
		}
		this.getActiveTask().getHolo().hideTo(this.pInfo.getPlayer());
		String name_ = Main.getMessagesManager().getTaskName(this.getActiveTask().getTaskType().toString());
		this.pInfo.getPlayer().sendTitle(Main.getMessagesManager().getGameMsg("finishTaskTitle", this.pInfo.getArena(), name_, this.getActiveTask().getLocationName().getName()),
				Main.getMessagesManager().getGameMsg("finishTaskSubTitle", this.pInfo.getArena(), name_, this.getActiveTask().getLocationName().getName()), 15, 40, 20);
		this.state++;
		this.pInfo.updateScoreBoard();
		Main.getSoundsManager().playSound("taskCompleted", this.pInfo.getPlayer(), this.pInfo.getPlayer().getLocation());
		if (!this.getIsDone()) {
			this.getActiveTask().getHolo().showTo(this.pInfo.getPlayer());
			this.updateTasksVars();
		} else {
			this.pInfo.getArena().getTasksManager().updateTasksDoneBar(true);
			Boolean allTasksDone = true;
			for (TaskPlayer tp : this.pInfo.getArena().getTasksManager().getTasksForPlayer(this.pInfo.getPlayer())) {
				if (!tp.getIsDone()) {
					allTasksDone = false;
					break;
				}
			}
			if (allTasksDone) {
				Main.getConfigManager().executeCommands("completedAllTasks", this.pInfo.getPlayer());
				Main.getCosmeticsManager().addCoins("completedAllTasks",  this.pInfo.getPlayer());
			}
		}
		this.pInfo.updateUseItemState(this.pInfo.getPlayer().getLocation());
		Main.getConfigManager().executeCommands("taskCompleted", this.pInfo.getPlayer());
		this.pInfo.getStatsManager().plusOneStatInt(StatInt.TASKS_COMPLETED);
		Main.getCosmeticsManager().addCoins("taskCompleted",  this.pInfo.getPlayer());
	}

	public void updateTasksVars() {
		this.wires_ = null;
		this.activeLocation_ = null;
		this.locations_ = null;
		this.activeLever_ = null;
		this.numbers_ = null;
		this.squares_ = null;
		this.leaves_ = null;
		this.fuelProgress_ = null;
		this.inspectTimer_ = null;
		this.inspectIsRunning_ = null;
		this.inspectAnomaly_ = null;
		this.moves_ = null;
		this.reactorState_ = null;
		this.asteroidsDestroyed_ = null;
		this.canistersLeft_ = null;
		this.waterJugProgress_ = null;
		this.rebootTimer_ = null;
		this.rebootIsRunning_ = null;
		this.maze_ = null;
		switch (this.getActiveTask().getTaskType()) {
		case WIRING:
			this.wires_ = TaskWiringInv.generateWires();
			break;
		case DIVERT_POWER:
			this.activeLocation_ = TaskDivertPowerInv.generateActiveLocation(this);
			this.locations_ = TaskDivertPowerInv.generateLocations(pInfo.getArena(), this.activeLocation_);
			this.activeLever_ = TaskDivertPowerInv.generateLever(this.locations_, this.activeLocation_);
			break;
		case UNLOCK_MANIFOLDS:
			this.numbers_ = TaskUnlockManifoldsInv.generateNumbers();
			break;
		case PRIME_SHIELDS:
			this.squares_ = TaskPrimeShieldsInv.generateShields();
			break;
		case CLEAN_O2:
			this.leaves_ = TaskCleanO2Inv.generateLeaves();
			break;
		case REFUEL:
		case FUEL:
			this.fuelProgress_ = 0;
			break;
		case INSPECT_SAMPLE:
			this.inspectTimer_ = 60;
			this.inspectIsRunning_ = false;
			this.inspectAnomaly_ = -1;
			break;
		case START_REACTOR:
			this.moves_ = TaskStartReactorInv.generateMoves();
			this.reactorState_ = 0;
			break;
		case CLEAR_ASTEROIDS:
			this.asteroidsDestroyed_ = 0;
			break;
		case FILL_CANISTERS:
			this.canistersLeft_ = 2;
			break;
		case REPLACE_WATER_JUG:
			this.waterJugProgress_ = 0;
			break;
		case REBOOT_WIFI:
			this.rebootTimer_ = 60;
			this.rebootIsRunning_ = false;
			break;
		case FIX_WEATHER_NODE:
			this.maze_ = TaskFixWeatherNodeInv.generateMaze();
			break;
		default:
			break;
		}
	}

	public Boolean getIsDone() {
		return this.state >= this.tasks.size();
	}

	public Task getActiveTask() {
		if (this.tasks.size() == 1) {
			return this.tasks.get(0);
		} else {
			if (this.tasks.size() == this.state) {
				return this.tasks.get(this.state - 1);
			} else {
				return this.tasks.get(this.state);
			}
		}
	}

	public void delete() {
		this.pInfo = null;
		this.tasks = null;
		this.state = null;
		this.selectedQueuedVariant = null;
		this.wires_ = null;
		this.activeLocation_ = null;
		this.locations_ = null;
		this.activeLever_ = null;
		this.numbers_ = null;
		this.squares_ = null;
		this.leaves_ = null;
		this.fuelProgress_ = null;
		this.inspectTimer_ = null;
		this.inspectIsRunning_ = null;
		this.inspectAnomaly_ = null;
		this.moves_ = null;
		this.reactorState_ = null;
		this.asteroidsDestroyed_ = null;
		this.canistersLeft_ = null;
		this.waterJugProgress_ = null;
		this.rebootTimer_ = null;
		this.rebootIsRunning_ = null;
		this.maze_ = null;
	}

	public ArrayList<String> getWires_() {
		return this.wires_;
	}

	public String getActiveLocation_() {
		return this.activeLocation_;
	}

	public ArrayList<String> getLocations_() {
		return this.locations_;
	}

	public Integer getActiveLever_() {
		return this.activeLever_;
	}

	public ArrayList<Task> getTasks() {
		return this.tasks;
	}

	public PlayerInfo getPlayerInfo() {
		return this.pInfo;
	}

	public Integer getState() {
		return state;
	}

	public ArrayList<Integer> getNumbers_() {
		return numbers_;
	}

	public ArrayList<Boolean> getSquares_() {
		return this.squares_;
	}

	public ArrayList<Integer> getLeaves_() {
		return this.leaves_;
	}

	public void setFuelProgress_(Integer to) {
		this.fuelProgress_ = to;
	}

	public Integer getFuelProgress_() {
		return fuelProgress_;
	}

	public Integer getInspectTimer_() {
		return inspectTimer_;
	}

	public void setInspectTimer_(Integer inspectTimer__) {
		if (this.inspectTimer_ > 0 && inspectTimer__ == 0) {
			this.inspectAnomaly_ = Utils.getRandomNumberInRange(0, 3);
			this.inspectIsRunning_ = false;
		}
		this.inspectTimer_ = inspectTimer__;
	}

	public Boolean getInspectIsRunning_() {
		return inspectIsRunning_;
	}

	public void setInspectIsRunning_(Boolean inspectIsRunning) {
		this.inspectIsRunning_ = inspectIsRunning;
	}

	public Integer getInspectAnomaly_() {
		return inspectAnomaly_;
	}

	public Integer getReactorState_() {
		return reactorState_;
	}

	public void setReactorState_(Integer reactorState_) {
		this.reactorState_ = reactorState_;
	}

	public ArrayList<Integer> getMoves_() {
		return moves_;
	}

	public Integer getAsteroidsDestroyed_() {
		return asteroidsDestroyed_;
	}

	public void setAsteroidsDestroyed_(Integer asteroidsDestroyed) {
		this.asteroidsDestroyed_ = asteroidsDestroyed;
	}

	public Integer getSelectedQueuedVariant() {
		return this.selectedQueuedVariant;
	}

	public Integer getCanistersLeft_() {
		return canistersLeft_;
	}

	public void setCanistersLeft_(Integer canistersLeft) {
		this.canistersLeft_ = canistersLeft;
	}

	public Integer getWaterJugProgress_() {
		return waterJugProgress_;
	}

	public void setWaterJugProgress_(Integer waterJugProgress_) {
		this.waterJugProgress_ = waterJugProgress_;
	}

	public Integer getRebootTimer_() {
		return rebootTimer_;
	}

	public void setRebootTimer_(Integer rebootTimer_) {
		this.rebootTimer_ = rebootTimer_;
	}

	public Boolean getRebootIsRunning_() {
		return rebootIsRunning_;
	}

	public void setRebootIsRunning_(Boolean rebootIsRunning_) {
		this.rebootIsRunning_ = rebootIsRunning_;
	}

	public ArrayList<Integer> getMaze_() {
		return maze_;
	}

	public void setMaze_(ArrayList<Integer> maze_) {
		this.maze_ = maze_;
	}
}
